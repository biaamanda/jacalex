package compiler.scanner;

public class IdentifierToken extends Token {

    public final String lexeme;

    public IdentifierToken(String lexeme, int line, int column) {
        super(Tag.ID, line, column);
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return "<" + tag + ", " + lexeme + ">";
    }
}
