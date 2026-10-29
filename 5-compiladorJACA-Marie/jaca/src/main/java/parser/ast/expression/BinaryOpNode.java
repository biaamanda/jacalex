package parser.ast.expression;

import parser.ast.ASTVisitor;

public class BinaryOpNode extends ExprNode {
    public int op;
    public ExprNode left;
    public ExprNode right;

    // O construtor agora recebe o token inteiro do CUP
    public BinaryOpNode(int op, ExprNode l, ExprNode r) {
        this.op = op;
        this.left = l;
        this.right = r;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        // O Java chama visit(BinaryOpNode)
        return visitor.visit(this);
    }
}
