package ast;

import java.util.List;

public class Program {
    public List<Command> commands;

    public Program(List<Command> commands) {
        this.commands = commands;
    }

    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}