package ast;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Stack;

public class InterpreterVisitor implements ASTVisitor {
    private final Stack<HashMap<String, Value>> tableStack = new Stack<>();

    public InterpreterVisitor() {
        tableStack.push(new HashMap<>());
    }

    private Value lookup(String name) {
        ListIterator<HashMap<String, Value>> it = tableStack.listIterator(tableStack.size());
        while (it.hasPrevious()) {
            HashMap<String, Value> table = it.previous();
            if (table.containsKey(name)) {
                return table.get(name);
            }
        }
        throw new RuntimeException("Variável não definida: " + name);
    }

    @Override
    public void visit(Program p) {
        for (Command c : p.commands) {
            c.accept(this);
        }
    }

    @Override
    public void visit(BlockCommand c) {
        tableStack.push(new HashMap<>());
        for (Command command : c.commands) {
            command.accept(this);
        }
        tableStack.pop();
    }

    @Override
    public void visit(AssignCommand c) {
        c.expr.accept(this);
        tableStack.peek().put(c.name, c.expr.getValue());
    }

    @Override
    public void visit(PrintCommand c) {
        c.expr.accept(this);
        System.out.println(">> " + c.expr.getValue());
    }

    @Override
    public void visit(SumExpr e) {
        e.e1.accept(this);
        e.e2.accept(this);
        e.setValue(Operations.add(e.e1.getValue(), e.e2.getValue()));
    }

    @Override
    public void visit(SubExpr e) {
        e.e1.accept(this);
        e.e2.accept(this);
        e.setValue(Operations.subtract(e.e1.getValue(), e.e2.getValue()));
    }

    @Override
    public void visit(MulExpr e) {
        e.e1.accept(this);
        e.e2.accept(this);
        e.setValue(Operations.multiply(e.e1.getValue(), e.e2.getValue()));
    }

    @Override
    public void visit(DivExpr e) {
        e.e1.accept(this);
        e.e2.accept(this);
        e.setValue(Operations.divide(e.e1.getValue(), e.e2.getValue()));
    }

    @Override
    public void visit(ModExpr e) {
        e.e1.accept(this);
        e.e2.accept(this);
        e.setValue(Operations.mod(e.e1.getValue(), e.e2.getValue()));
    }

    @Override
    public void visit(PowExpr e) {
        e.e1.accept(this);
        e.e2.accept(this);
        e.setValue(Operations.power(e.e1.getValue(), e.e2.getValue()));
    }

    @Override
    public void visit(UMinusExpr e) {
        e.expr.accept(this);
        e.setValue(Operations.negate(e.expr.getValue()));
    }

    @Override
    public void visit(SinExpr e) {
        e.expr.accept(this);
        e.setValue(Operations.sin(e.expr.getValue()));
    }

    @Override
    public void visit(CosExpr e) {
        e.expr.accept(this);
        e.setValue(Operations.cos(e.expr.getValue()));
    }

    @Override
    public void visit(NumberExpr e) {
    }

    @Override
    public void visit(StringExpr e) {
    }

    @Override
    public void visit(PiExpr e) {
    }

    @Override
    public void visit(IdExpr e) {
        e.setValue(lookup(e.name));
    }
}
