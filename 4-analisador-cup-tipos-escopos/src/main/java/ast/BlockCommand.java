package ast;

import java.util.ArrayList;
import java.util.List;

public class BlockCommand extends Command {
    public List<Command> commands = new ArrayList<>();

    public void add(Command command) {
        commands.add(command);
    }

    @Override
    public void accept(ASTVisitor v) {
        v.visit(this);
    }
}