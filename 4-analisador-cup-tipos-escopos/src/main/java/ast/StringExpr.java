package ast;

public class StringExpr extends Expression {

    public StringExpr(String value) {
        this.setValue(new Value(value));
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}