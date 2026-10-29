package parser.ast.statement;

import java.util.List;

import parser.ast.ASTNode;
import parser.ast.ASTVisitor;

// Nó para listas de comandos (statement_list)
public class BlockNode extends ASTNode {
    public List<ASTNode> statements;

    public BlockNode(List<ASTNode> stmts) {
        this.statements = stmts;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this); // Chama o visit do bloco
    }
}
