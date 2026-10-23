package ast;

public abstract class Command {
    public abstract void accept(ASTVisitor v);
}