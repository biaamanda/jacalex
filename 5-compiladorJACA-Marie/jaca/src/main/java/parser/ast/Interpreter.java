package parser.ast;

import java.util.HashMap;

import parser.ast.expression.BinaryOpNode;
import parser.ast.expression.NumberNode;
import parser.ast.expression.UnaryMinusNode;
import parser.ast.expression.VariableNode;
import parser.ast.statement.AssignNode;
import parser.ast.statement.BlockNode;
import parser.ast.statement.IfNode;
import parser.ast.statement.PrintNode;
import parser.ast.statement.ReadNode;
import parser.ast.statement.WhileNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import parser.sym;

public class Interpreter implements ASTVisitor<Value> {
    // Tabela agora aceita qualquer tipo encapsulado em Value
    private final HashMap<String, Value> symbolTable = new HashMap<>();

    // Para ler a entrada
    private final BufferedReader reader = new BufferedReader(
            new InputStreamReader(System.in));

    @Override
    public Value visit(BlockNode node) {
        for (ASTNode stmt : node.statements) {
            stmt.accept(this); // Executa cada comando da lista
        }
        return null; // Comandos não produzem valor computado
    }

    @Override
    public Value visit(AssignNode node) {
        Value value = node.expression.accept(this);
        symbolTable.put(node.id, value);
        return null;
    }

    @Override
    public Value visit(IfNode node) {
        Value conditionValue = node.condition.accept(this);

        if (!conditionValue.isBoolean()) {
            throw new RuntimeException("Erro de Tipo: A condição do IF precisa ser um Booleano.");
        }

        if (conditionValue.asBoolean()) {
            node.thenBlock.accept(this);
        } else if (node.elseBlock != null) {
            node.elseBlock.accept(this);
        }
        return null;
    }

    @Override
    public Value visit(WhileNode node) {
        Value conditionValue = node.condition.accept(this);
        while (conditionValue.asBoolean()) {
            node.block.accept(this);
            conditionValue = node.condition.accept(this);
        }
        return null;
    }

    @Override
    public Value visit(PrintNode node) {
        Value value = node.expr.accept(this);
        System.out.println(value);
        return null;
    }

    @Override
    public Value visit(ReadNode node) {
        // Exibe o prompt
        System.out.print(node.id + "?: ");
        System.out.flush();
        // Ler a entrada
        try {
            String input = reader.readLine();
            if (input != null) {
                Value valueRead;
                // Tenta identificar se o usuário digitou um booleano ou um número
                if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false")) {
                    valueRead = new Value(Boolean.parseBoolean(input));
                } else {
                    try {
                        valueRead = new Value(Double.parseDouble(input));
                    } catch (NumberFormatException e) {
                        // se não for número nem bool, lança um erro em tempo de execução
                        throw new RuntimeException("Erro em tempo de execução: Entrada inválida para '"
                                + node.id + "'. Esperado número ou booleano.");
                    }
                }
                // Atualizar ou criar na tabela de símbolos (Hashtable/HashMap)
                // O método .put substitui se a chave já existe ou cria uma nova se não existir.
                symbolTable.put(node.id, valueRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("Erro em tempo de execução: Erro em readline() para '"
                    + node.id + "'.");
        }

        return null; // Comandos retornam null no nosso formato de Visitor
    }

    @Override
    public Value visit(NumberNode node) {
        return new Value(node.value);
    }

    @Override
    public Value visit(VariableNode node) {
        if (!symbolTable.containsKey(node.name)) {
            throw new RuntimeException("Erro Semântico: Variável '" + node.name + "' não inicializada.");
        }
        return symbolTable.get(node.name);
    }

    @Override
    public Value visit(BinaryOpNode node) {
        // Avalia recursivamente as sub-expressões da esquerda e da direita
        Value leftVal = node.left.accept(this);
        Value rightVal = node.right.accept(this);
        // --- OPERADORES ARITMÉTICOS (Exigem que ambos os lados sejam Double) ---
        if (node.op == sym.PLUS || node.op == sym.MINUS || node.op == sym.TIMES || node.op == sym.DIV
                || node.op == sym.MOD) {

            // Validação semântica de tipos em tempo de execução
            if (!leftVal.isDouble() || !rightVal.isDouble()) {
                throw new RuntimeException("Erro de Tipo: Operadores aritméticos exigem operandos numéricos. " +
                        "Recebido: " + leftVal + " e " + rightVal);
            }

            double l = leftVal.asDouble();
            double r = rightVal.asDouble();

            if (node.op == sym.PLUS)
                return new Value(l + r);
            if (node.op == sym.MINUS)
                return new Value(l - r);
            if (node.op == sym.TIMES)
                return new Value(l * r);
            if (node.op == sym.DIV) {
                if (r == 0.0) {
                    throw new RuntimeException("Erro em tempo de execução: Divisão por zero.");
                }
                return new Value(l / r);
            }
            if (node.op == sym.MOD)
                return new Value(l % r);
        }
        // --- OPERADORES RELACIONAIS (Exigem Double, retornam Boolean) ---
        if (node.op == sym.GT || node.op == sym.LT || node.op == sym.GEQ || node.op == sym.LEQ) {

            if (!leftVal.isDouble() || !rightVal.isDouble()) {
                throw new RuntimeException("Erro de Tipo: Comparações de magnitude (>, <, >=, <=) exigem números.");
            }

            double l = leftVal.asDouble();
            double r = rightVal.asDouble();

            if (node.op == sym.GT)
                return new Value(l > r);
            if (node.op == sym.LT)
                return new Value(l < r);
            if (node.op == sym.GEQ)
                return new Value(l >= r);
            if (node.op == sym.LEQ)
                return new Value(l <= r);
        }
        // --- OPERADORES DE IGUALDADE (Funcionam para Double ou Boolean,
        // se forem iguais) ---
        if (node.op == sym.EQ || node.op == sym.NEQ) {
            boolean areEqual;

            if (leftVal.isDouble() && rightVal.isDouble()) {
                areEqual = leftVal.asDouble().equals(rightVal.asDouble());
            } else if (leftVal.isBoolean() && rightVal.isBoolean()) {
                areEqual = leftVal.asBoolean().equals(rightVal.asBoolean());
            } else {
                // Se um lado for Double e o outro Boolean, eles nunca são idênticos na tipagem
                // estrita
                throw new RuntimeException("Erro de Tipo: Não é possível comparar a igualdade de tipos diferentes. " +
                        "Tentativa de comparar " + leftVal + " com " + rightVal);
            }

            return new Value(node.op == sym.EQ ? areEqual : !areEqual);
        }

        // Fallback de segurança do compilador
        throw new RuntimeException("Erro: Operador binário desconhecido (ID do Token: " + node.op + ").");
    }

    @Override
    public Value visit(UnaryMinusNode node) {
        Value val = node.expr.accept(this);
        if (!val.isDouble()) {
            throw new RuntimeException("Erro de Tipo: Operador '-' unário aplicado a um Booleano.");
        }
        return new Value(-val.asDouble());
    }

}
