package ast;

public class AssignCommand extends Command {
    public String name;
    public Expression expr;

    public AssignCommand(String name, Expression expr) {
        this.name = name;
        this.expr = expr;
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}