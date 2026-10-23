package ast;

public class UMinusExpr extends Expression {
    public Expression expr;

    public UMinusExpr(Expression e) {
        this.expr = e;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}