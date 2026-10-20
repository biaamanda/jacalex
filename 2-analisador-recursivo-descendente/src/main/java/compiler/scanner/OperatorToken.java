package compiler.scanner;

public class OperatorToken extends Token {
    
    public final String lexeme;

    public OperatorToken(Tag tag, String lexeme, int line, int column) {
        super(tag, line, column);
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return "<" + tag + ", " + lexeme + ">";
    }
}
