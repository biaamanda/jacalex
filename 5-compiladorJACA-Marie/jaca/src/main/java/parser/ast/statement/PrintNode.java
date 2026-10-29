package parser.ast.statement;

import parser.ast.ASTNode;
import parser.ast.ASTVisitor;
import parser.ast.expression.ExprNode;

// Nós de Print e Read
public class PrintNode extends ASTNode {
    public ExprNode expr;

    public PrintNode(ExprNode expr) {
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}