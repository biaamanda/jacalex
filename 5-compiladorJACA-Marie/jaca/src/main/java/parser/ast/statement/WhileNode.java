package parser.ast.statement;

import parser.ast.ASTNode;
import parser.ast.ASTVisitor;
import parser.ast.expression.ExprNode;

public class WhileNode extends ASTNode {
    public ExprNode condition;
    public BlockNode block;

    public WhileNode(ExprNode cond, BlockNode block) {
        this.condition = cond;
        this.block = block;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
