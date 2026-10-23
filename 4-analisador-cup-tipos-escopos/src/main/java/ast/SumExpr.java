package ast;

public class SumExpr extends Expression {
    public Expression e1, e2;

    public SumExpr(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}