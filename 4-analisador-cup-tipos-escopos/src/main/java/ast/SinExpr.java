package ast;

public class SinExpr extends Expression {
    public Expression expr;

    public SinExpr(Expression e) {
        this.expr = e;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}