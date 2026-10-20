package compiler.scanner;

public class NumberToken extends Token {

    public final String lexeme;

    public NumberToken(String lexeme, int line, int column) {
        super(Tag.NUMBER, line, column);
        this.lexeme = lexeme;
    }

    @Override
    public String toString() {
        return "<" + tag + ", " + lexeme + ">";
    }
}
