package parser.ast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import parser.ast.expression.BinaryOpNode;
import parser.ast.expression.ExprNode;
import parser.ast.expression.NumberNode;
import parser.ast.expression.UnaryMinusNode;
import parser.ast.expression.VariableNode;
import parser.ast.statement.AssignNode;
import parser.ast.statement.BlockNode;
import parser.ast.statement.IfNode;
import parser.ast.statement.PrintNode;
import parser.ast.statement.ReadNode;
import parser.ast.statement.WhileNode;

import parser.sym;

/**
 * Traduz a AST de Jaca para um programa em assembly de Marie.
 *
 * Convenções:
 * - todo valor é um inteiro de 16 bits com sinal (a única palavra de dados do Marie);
 * - booleanos são 0 (falso) e 1 (verdadeiro);
 * - o resultado de uma expressão fica sempre no acumulador (AC);
 * - o Marie não tem multiplicação, divisão nem resto: viram sub-rotinas (JnS);
 * - tipos são verificados em tempo de compilação (INT ou BOOL).
 */
public class Compiler implements ASTVisitor<Compiler.Type> {

    public enum Type {
        INT, BOOL
    }

    public static class CompileException extends RuntimeException {
        public CompileException(String message) {
            super(message);
        }
    }

    private static final long MIN_VALUE = -32768;
    private static final long MAX_VALUE = 32767;

    // Uma linha do programa gerado: rótulos + instrução (ou só comentário)
    private static class Line {
        final List<String> labels = new ArrayList<>();
        final String op;
        final String arg;

        Line(String op, String arg) {
            this.op = op;
            this.arg = arg;
        }
    }

    // Onde está um operando: rótulo de memória e tipo
    private static class Operand {
        final String label;
        final Type type;
        final boolean temp;

        Operand(String label, Type type, boolean temp) {
            this.label = label;
            this.type = type;
            this.temp = temp;
        }
    }

    private final List<Line> code = new ArrayList<>();
    private final List<String> pendingLabels = new ArrayList<>();
    private final Map<String, Type> variables = new LinkedHashMap<>();
    private final Map<Long, String> constants = new TreeMap<>();

    private int labelCount = 0;
    private int depth = 0;
    private int maxDepth = 0;
    private boolean usesMul = false;
    private boolean usesDiv = false;

    // ---------------------------------------------------------------- API

    public String compile(ASTNode root) {
        root.accept(this);
        comment("fim do programa");
        emit("Halt");
        if (usesMul) {
            emitMultiply();
        }
        if (usesDiv) {
            emitDivide();
        }
        emitData();
        return render();
    }

    // ------------------------------------------------------------ comandos

    @Override
    public Type visit(BlockNode node) {
        for (ASTNode stmt : node.statements) {
            stmt.accept(this);
        }
        return null;
    }

    @Override
    public Type visit(AssignNode node) {
        comment(node.id + " = ...");
        Type type = node.expression.accept(this);
        declare(node.id, type);
        emit("Store", variableLabel(node.id));
        return null;
    }

    @Override
    public Type visit(IfNode node) {
        comment("if");
        String elseLabel = newLabel();
        genCondition(node.condition, elseLabel, "A condição do IF");
        node.thenBlock.accept(this);
        if (node.elseBlock != null) {
            String endLabel = newLabel();
            emit("Jump", endLabel);
            label(elseLabel);
            node.elseBlock.accept(this);
            label(endLabel);
        } else {
            label(elseLabel);
        }
        return null;
    }

    @Override
    public Type visit(WhileNode node) {
        comment("while");
        String topLabel = newLabel();
        String endLabel = newLabel();
        label(topLabel);
        genCondition(node.condition, endLabel, "A condição do WHILE");
        node.block.accept(this);
        emit("Jump", topLabel);
        label(endLabel);
        return null;
    }

    @Override
    public Type visit(PrintNode node) {
        comment("print");
        node.expr.accept(this);
        emit("Output");
        return null;
    }

    @Override
    public Type visit(ReadNode node) {
        comment("read " + node.id);
        emit("Input");
        declare(node.id, Type.INT);
        emit("Store", variableLabel(node.id));
        return null;
    }

    // ----------------------------------------------------------- expressões

    @Override
    public Type visit(NumberNode node) {
        emit("Load", constantLabel(integerValue(node)));
        return Type.INT;
    }

    @Override
    public Type visit(VariableNode node) {
        Type type = typeOf(node.name);
        emit("Load", variableLabel(node.name));
        return type;
    }

    @Override
    public Type visit(UnaryMinusNode node) {
        Long constant = constantValue(node);
        if (constant != null) {
            emit("Load", constantLabel(constant));
            return Type.INT;
        }
        Type type = node.expr.accept(this);
        requireInt("-", type);
        String temp = temp(depth);
        emit("Store", temp);
        emit("Clear");
        emit("Subt", temp);
        return Type.INT;
    }

    @Override
    public Type visit(BinaryOpNode node) {
        if (isRelational(node.op)) {
            // Valor booleano: 1 se a comparação vale, 0 caso contrário
            String falseLabel = newLabel();
            String endLabel = newLabel();
            genCondition(node, falseLabel, "");
            emit("Load", constantLabel(1));
            emit("Jump", endLabel);
            label(falseLabel);
            emit("Load", constantLabel(0));
            label(endLabel);
            return Type.BOOL;
        }

        Operand right = rightOperand(node.right);
        Type leftType = node.left.accept(this);
        if (right.temp) {
            depth--;
        }
        String name = operatorName(node.op);
        requireInt(name, leftType);
        requireInt(name, right.type);

        switch (node.op) {
            case sym.PLUS:
                emit("Add", right.label);
                break;
            case sym.MINUS:
                emit("Subt", right.label);
                break;
            case sym.TIMES:
                usesMul = true;
                emit("Store", "MulA");
                emit("Load", right.label);
                emit("Store", "MulB");
                emit("JnS", "MulS");
                break;
            case sym.DIV:
                usesDiv = true;
                emit("Store", "DivA");
                emit("Load", right.label);
                emit("Store", "DivB");
                emit("JnS", "DivS");
                emit("Load", "DivQ");
                break;
            case sym.MOD:
                usesDiv = true;
                emit("Store", "DivA");
                emit("Load", right.label);
                emit("Store", "DivB");
                emit("JnS", "DivS");
                emit("Load", "DivR");
                break;
            default:
                throw new CompileException("Operador binário desconhecido (token " + node.op + ").");
        }
        return Type.INT;
    }

    // ------------------------------------------------- condições e desvios

    // Gera o salto para falseLabel quando a condição é falsa; se for verdadeira, segue em frente.
    private void genCondition(ExprNode cond, String falseLabel, String where) {
        if (cond instanceof BinaryOpNode node && isRelational(node.op)) {
            compare(node);
            // AC = esquerda - direita; Skipcond pula a próxima instrução se a condição de AC vale
            switch (node.op) {
                case sym.LT:
                    emit("Skipcond", "000");
                    emit("Jump", falseLabel);
                    break;
                case sym.GT:
                    emit("Skipcond", "800");
                    emit("Jump", falseLabel);
                    break;
                case sym.EQ:
                    emit("Skipcond", "400");
                    emit("Jump", falseLabel);
                    break;
                case sym.NEQ:
                    branchIf("400", falseLabel);
                    break;
                case sym.LEQ:
                    branchIf("800", falseLabel);
                    break;
                case sym.GEQ:
                    branchIf("000", falseLabel);
                    break;
                default:
                    throw new CompileException("Operador relacional desconhecido.");
            }
            return;
        }
        Type type = cond.accept(this);
        if (type != Type.BOOL) {
            throw new CompileException(where + " precisa ser um booleano (use uma comparação).");
        }
        branchIf("400", falseLabel);
    }

    // Salta para falseLabel quando a condição de Skipcond vale em AC (usa um rótulo auxiliar).
    private void branchIf(String skipcond, String falseLabel) {
        String ok = newLabel();
        emit("Skipcond", skipcond);
        emit("Jump", ok);
        emit("Jump", falseLabel);
        label(ok);
    }

    // Deixa (esquerda - direita) em AC e verifica os tipos da comparação.
    private void compare(BinaryOpNode node) {
        Operand right = rightOperand(node.right);
        Type leftType = node.left.accept(this);
        if (right.temp) {
            depth--;
        }
        String name = operatorName(node.op);
        if (node.op == sym.EQ || node.op == sym.NEQ) {
            if (leftType != right.type) {
                throw new CompileException("Não é possível comparar tipos diferentes com '" + name + "'.");
            }
        } else {
            requireInt(name, leftType);
            requireInt(name, right.type);
        }
        emit("Subt", right.label);
    }

    // ------------------------------------------------------------ operandos

    // Operando direito: variável ou constante é usado direto da memória;
    // qualquer outra expressão é calculada e guardada num temporário.
    private Operand rightOperand(ExprNode expr) {
        Long constant = constantValue(expr);
        if (constant != null) {
            return new Operand(constantLabel(constant), Type.INT, false);
        }
        if (expr instanceof VariableNode variable) {
            return new Operand(variableLabel(variable.name), typeOf(variable.name), false);
        }
        Type type = expr.accept(this);
        String temp = temp(depth);
        emit("Store", temp);
        depth++;
        return new Operand(temp, type, true);
    }

    // Valor de uma constante (literal, possivelmente com sinais de menos); null se não for constante.
    private Long constantValue(ExprNode expr) {
        if (expr instanceof NumberNode number) {
            return (long) integerValue(number);
        }
        if (expr instanceof UnaryMinusNode minus) {
            Long inner = constantValue(minus.expr);
            return inner == null ? null : -inner;
        }
        return null;
    }

    private int integerValue(NumberNode number) {
        double value = number.value;
        if (value != Math.rint(value)) {
            throw new CompileException("O Marie só trabalha com inteiros; literal inválido: " + number.value);
        }
        if (value < MIN_VALUE - 1 || value > MAX_VALUE + 1) {
            throw new CompileException("Literal fora do intervalo de 16 bits: " + (long) value);
        }
        return (int) value;
    }

    // ------------------------------------------------------ nomes e tabelas

    private void declare(String name, Type type) {
        Type known = variables.get(name);
        if (known != null && known != type) {
            throw new CompileException("A variável '" + name + "' já é " + known + " e não pode receber " + type + ".");
        }
        variables.put(name, type);
    }

    private Type typeOf(String name) {
        Type type = variables.get(name);
        if (type == null) {
            throw new CompileException("Variável '" + name + "' usada antes de receber um valor.");
        }
        return type;
    }

    // Variáveis do usuário começam com V; os nomes internos nunca começam com V.
    private String variableLabel(String name) {
        return "V" + name;
    }

    private String constantLabel(long value) {
        if (value < MIN_VALUE || value > MAX_VALUE) {
            throw new CompileException("Constante fora do intervalo de 16 bits: " + value);
        }
        String label = (value < 0 ? "CstN" : "CstP") + Math.abs(value);
        constants.put(value, label);
        return label;
    }

    private String temp(int level) {
        maxDepth = Math.max(maxDepth, level + 1);
        return "Tmp" + level;
    }

    private String newLabel() {
        return "Lab" + (++labelCount);
    }

    private boolean isRelational(int op) {
        return op == sym.EQ || op == sym.NEQ || op == sym.GT || op == sym.LT || op == sym.GEQ || op == sym.LEQ;
    }

    private String operatorName(int op) {
        switch (op) {
            case sym.PLUS: return "+";
            case sym.MINUS: return "-";
            case sym.TIMES: return "*";
            case sym.DIV: return "/";
            case sym.MOD: return "%";
            case sym.EQ: return "==";
            case sym.NEQ: return "!=";
            case sym.GT: return ">";
            case sym.LT: return "<";
            case sym.GEQ: return ">=";
            case sym.LEQ: return "<=";
            default: return "?";
        }
    }

    private void requireInt(String operator, Type type) {
        if (type != Type.INT) {
            throw new CompileException("O operador '" + operator + "' exige operandos numéricos, mas recebeu " + type + ".");
        }
    }

    // ----------------------------------------------------- emissão do código

    private void label(String name) {
        pendingLabels.add(name);
    }

    private void comment(String text) {
        code.add(new Line(null, text));
    }

    private void emit(String op) {
        emit(op, null);
    }

    private void emit(String op, String arg) {
        Line line = new Line(op, arg);
        line.labels.addAll(pendingLabels);
        pendingLabels.clear();
        code.add(line);
    }

    // Dado: um rótulo seguido de DEC ou HEX
    private void data(String name, String directive, String value) {
        label(name);
        emit(directive, value);
    }

    private void emitData() {
        comment("variaveis");
        for (String name : variables.keySet()) {
            data(variableLabel(name), "DEC", "0");
        }
        comment("constantes");
        for (Map.Entry<Long, String> constant : constants.entrySet()) {
            data(constant.getValue(), "DEC", String.valueOf(constant.getKey()));
        }
        if (maxDepth > 0) {
            comment("temporarios das expressoes");
            for (int i = 0; i < maxDepth; i++) {
                data("Tmp" + i, "DEC", "0");
            }
        }
        if (usesMul) {
            comment("dados da multiplicacao");
            for (String name : new String[] { "MulA", "MulB", "MulR", "MulN" }) {
                data(name, "DEC", "0");
            }
        }
        if (usesDiv) {
            comment("dados da divisao");
            for (String name : new String[] { "DivA", "DivB", "DivQ", "DivR", "DivN", "DivM" }) {
                data(name, "DEC", "0");
            }
        }
    }

    // MulA * MulB -> AC. Soma |MulA| por |MulB| vezes e corrige o sinal no fim.
    private void emitMultiply() {
        String one = constantLabel(1);
        comment("sub-rotina de multiplicacao: AC = MulA * MulB");
        data("MulS", "HEX", "000");
        emit("Clear");
        emit("Store", "MulR");
        emit("Store", "MulN");
        emit("Load", "MulB");
        emit("Skipcond", "000");
        emit("Jump", "MulB1");
        emit("Clear");
        emit("Subt", "MulB");
        emit("Store", "MulB");
        emit("Load", one);
        emit("Subt", "MulN");
        emit("Store", "MulN");
        label("MulB1");
        emit("Load", "MulA");
        emit("Skipcond", "000");
        emit("Jump", "MulA1");
        emit("Clear");
        emit("Subt", "MulA");
        emit("Store", "MulA");
        emit("Load", one);
        emit("Subt", "MulN");
        emit("Store", "MulN");
        label("MulA1");
        label("MulL");
        emit("Load", "MulB");
        emit("Skipcond", "800");
        emit("Jump", "MulE");
        emit("Load", "MulR");
        emit("Add", "MulA");
        emit("Store", "MulR");
        emit("Load", "MulB");
        emit("Subt", one);
        emit("Store", "MulB");
        emit("Jump", "MulL");
        label("MulE");
        emit("Load", "MulN");
        emit("Skipcond", "400");
        emit("Jump", "MulNg");
        emit("Load", "MulR");
        emit("JumpI", "MulS");
        label("MulNg");
        emit("Clear");
        emit("Subt", "MulR");
        emit("JumpI", "MulS");
    }

    // DivA / DivB -> DivQ (quociente) e DivR (resto), truncando para zero como o Java.
    // O resto tem o sinal do dividendo. Divisão por zero interrompe o programa (Halt).
    private void emitDivide() {
        String one = constantLabel(1);
        comment("sub-rotina de divisao: DivQ = DivA / DivB e DivR = DivA % DivB");
        data("DivS", "HEX", "000");
        emit("Load", "DivB");
        emit("Skipcond", "400");
        emit("Jump", "DivA0");
        comment("divisao por zero: interrompe o programa");
        emit("Halt");
        label("DivA0");
        emit("Clear");
        emit("Store", "DivN");
        emit("Store", "DivM");
        emit("Store", "DivQ");
        emit("Load", "DivA");
        emit("Skipcond", "000");
        emit("Jump", "DivA1");
        emit("Clear");
        emit("Subt", "DivA");
        emit("Store", "DivA");
        emit("Load", one);
        emit("Store", "DivN");
        emit("Store", "DivM");
        label("DivA1");
        emit("Load", "DivB");
        emit("Skipcond", "000");
        emit("Jump", "DivB1");
        emit("Clear");
        emit("Subt", "DivB");
        emit("Store", "DivB");
        emit("Load", one);
        emit("Subt", "DivN");
        emit("Store", "DivN");
        label("DivB1");
        emit("Load", "DivA");
        emit("Store", "DivR");
        label("DivL");
        emit("Load", "DivR");
        emit("Subt", "DivB");
        emit("Skipcond", "000");
        emit("Jump", "DivC");
        emit("Jump", "DivE");
        label("DivC");
        emit("Store", "DivR");
        emit("Load", "DivQ");
        emit("Add", one);
        emit("Store", "DivQ");
        emit("Jump", "DivL");
        label("DivE");
        emit("Load", "DivN");
        emit("Skipcond", "400");
        emit("Jump", "DivQN");
        emit("Jump", "DivR1");
        label("DivQN");
        emit("Clear");
        emit("Subt", "DivQ");
        emit("Store", "DivQ");
        label("DivR1");
        emit("Load", "DivM");
        emit("Skipcond", "400");
        emit("Jump", "DivRN");
        emit("JumpI", "DivS");
        label("DivRN");
        emit("Clear");
        emit("Subt", "DivR");
        emit("Store", "DivR");
        emit("JumpI", "DivS");
    }

    // ------------------------------------------------------------ saída

    // Vários rótulos na mesma instrução viram um só: os outros são apelidos.
    private String render() {
        Map<String, String> alias = new LinkedHashMap<>();
        for (Line line : code) {
            for (int i = 1; i < line.labels.size(); i++) {
                alias.put(line.labels.get(i), line.labels.get(0));
            }
        }

        StringBuilder out = new StringBuilder();
        out.append("/ Programa Jaca compilado para MARIE\n");
        for (Line line : code) {
            if (line.op == null) {
                out.append("/ ").append(line.arg).append('\n');
                continue;
            }
            String label = line.labels.isEmpty() ? "" : line.labels.get(0) + ",";
            String arg = line.arg == null ? "" : alias.getOrDefault(line.arg, line.arg);
            out.append(String.format("%-12s%s%s\n", label, line.op, arg.isEmpty() ? "" : " " + arg));
        }
        return out.toString();
    }
}
