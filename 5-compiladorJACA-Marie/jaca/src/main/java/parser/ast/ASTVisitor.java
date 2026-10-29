package parser.ast;

import parser.ast.expression.BinaryOpNode;
import parser.ast.expression.NumberNode;
import parser.ast.expression.UnaryMinusNode;
import parser.ast.expression.VariableNode;
import parser.ast.statement.AssignNode;
import parser.ast.statement.BlockNode;
import parser.ast.statement.IfNode;
import parser.ast.statement.PrintNode;
import parser.ast.statement.ReadNode;
import parser.ast.statement.WhileNode;

public interface ASTVisitor<T> {
    // Comandos continuam Void
    T visit(BlockNode node);

    T visit(AssignNode node);

    T visit(IfNode node);

    T visit(WhileNode node);

    T visit(PrintNode node);

    T visit(ReadNode node);

    // Expressões agora retornam o tipo genérico T (que será Value)
    T visit(NumberNode node);

    T visit(VariableNode node);

    T visit(BinaryOpNode node);

    T visit(UnaryMinusNode node);
}
