package ast;

public class CosExpr extends Expression {
    public Expression expr;

    public CosExpr(Expression e) {
        this.expr = e;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}