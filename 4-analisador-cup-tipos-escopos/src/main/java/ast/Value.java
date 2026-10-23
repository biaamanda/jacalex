package ast;

public class Value {

    public final Type type;
    public final Object value;

    public Value(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    public Value(Double value) {
        this(Type.DBL, value);
    }

    public Value(String value) {
        this(Type.STR, value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}