package ast;

public class NumberExpr extends Expression {

    public NumberExpr(Double value) {
        this.setValue(new Value(value));
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}