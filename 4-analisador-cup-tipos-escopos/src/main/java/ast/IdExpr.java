package ast;

public class IdExpr extends Expression {
    public String name;

    public IdExpr(String name) {
        this.name = name;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}