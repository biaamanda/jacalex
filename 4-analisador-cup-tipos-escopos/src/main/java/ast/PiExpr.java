package ast;

public class PiExpr extends Expression {

    public PiExpr() {
        this.setValue(new Value(Math.PI));
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}