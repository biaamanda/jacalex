package parser.ast.expression;

import parser.ast.ASTVisitor;

// Nó para variáveis (ID)
public class VariableNode extends ExprNode {
    public String name;

    public VariableNode(String name) {
        this.name = name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        // O Java chama visit(VariableNode)
        return visitor.visit(this);
    }
}