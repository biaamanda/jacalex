package ast;

public class SubExpr extends Expression {
    public Expression e1, e2;

    public SubExpr(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}