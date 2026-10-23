package ast;

public class PrintCommand extends Command {
    public Expression expr;

    public PrintCommand(Expression expr) {
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}