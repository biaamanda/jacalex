package parser.ast.expression;

import parser.ast.ASTVisitor;

// Nó para Operação Unária (Inversão de sinal)
public class UnaryMinusNode extends ExprNode {
    public ExprNode expr;

    public UnaryMinusNode(ExprNode expr) {
        this.expr = expr;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        // O Java chama visit(UnaryMinusNode)
        return visitor.visit(this);
    }
}