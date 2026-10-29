package parser.ast;

public class Value {
    private final Object value;

    public Value(Object value) {
        this.value = value;
    }

    public boolean isBoolean() {
        return value instanceof Boolean;
    }

    public boolean isDouble() {
        return value instanceof Double;
    }

    public Boolean asBoolean() {
        return (Boolean) value;
    }

    public Double asDouble() {
        return (Double) value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}