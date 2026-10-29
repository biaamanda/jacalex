package parser.ast.statement;

import parser.ast.ASTNode;
import parser.ast.ASTVisitor;
import parser.ast.expression.ExprNode;

// Nó do IF (if_stmt)
public class IfNode extends ASTNode {
    public ExprNode condition;
    public BlockNode thenBlock;
    public BlockNode elseBlock; // Pode ser null

    public IfNode(ExprNode cond, BlockNode thenB, BlockNode elseB) {
        this.condition = cond;
        this.thenBlock = thenB;
        this.elseBlock = elseB;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}