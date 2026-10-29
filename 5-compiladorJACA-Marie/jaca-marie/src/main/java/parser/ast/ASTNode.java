package parser.ast;

public abstract class ASTNode {
    // Método genérico: <T> indica o tipo que o Visitor vai retornar ao avaliar este
    // nó
    public abstract <T> T accept(ASTVisitor<T> visitor);
}
