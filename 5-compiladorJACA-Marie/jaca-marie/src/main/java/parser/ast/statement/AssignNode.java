package parser.ast.statement;

import parser.ast.ASTNode;
import parser.ast.ASTVisitor;
import parser.ast.expression.ExprNode;

// Nó de Atribuição (assign_stmt)
public class AssignNode extends ASTNode {
    public String id;
    public ExprNode expression;

    public AssignNode(String id, ExprNode expr) {
        this.id = id;
        this.expression = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this); // Chama o visit da atribuição
    }
}
