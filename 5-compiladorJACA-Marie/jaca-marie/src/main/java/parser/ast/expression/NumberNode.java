package parser.ast.expression;

import parser.ast.ASTVisitor;

// Nó para números literais (NUMBER)
public class NumberNode extends ExprNode {
    public Double value;

    public NumberNode(Double val) {
        this.value = val;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        // O Java sabe que 'this' é um NumberNode e chama visit(NumberNode)
        return visitor.visit(this);
    }
}