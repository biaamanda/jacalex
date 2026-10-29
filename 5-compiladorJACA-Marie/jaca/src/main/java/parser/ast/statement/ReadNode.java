package parser.ast.statement;

import parser.ast.ASTNode;
import parser.ast.ASTVisitor;

public class ReadNode extends ASTNode {
    public String id;

    public ReadNode(String id) {
        this.id = id;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
