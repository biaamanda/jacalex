package ast;

public class Operations {

    public static Value add(Value v1, Value v2) {
        if (v1.type == Type.DBL && v2.type == Type.DBL) {
            return new Value((Double) v1.value + (Double) v2.value);
        }
        return new Value(v1.toString() + v2.toString());
    }

    public static Value subtract(Value v1, Value v2) {
        checkNumbers("subtração", v1, v2);
        return new Value((Double) v1.value - (Double) v2.value);
    }

    public static Value multiply(Value v1, Value v2) {
        checkNumbers("multiplicação", v1, v2);
        return new Value((Double) v1.value * (Double) v2.value);
    }

    public static Value divide(Value v1, Value v2) {
        checkNumbers("divisão", v1, v2);
        return new Value((Double) v1.value / (Double) v2.value);
    }

    public static Value mod(Value v1, Value v2) {
        checkNumbers("resto", v1, v2);
        return new Value((Double) v1.value % (Double) v2.value);
    }

    public static Value power(Value v1, Value v2) {
        checkNumbers("potência", v1, v2);
        return new Value(Math.pow((Double) v1.value, (Double) v2.value));
    }

    public static Value negate(Value v) {
        checkNumber("negação", v);
        return new Value(-(Double) v.value);
    }

    public static Value sin(Value v) {
        checkNumber("seno", v);
        return new Value(Math.sin((Double) v.value));
    }

    public static Value cos(Value v) {
        checkNumber("cosseno", v);
        return new Value(Math.cos((Double) v.value));
    }

    private static void checkNumbers(String operation, Value v1, Value v2) {
        if (v1.type != Type.DBL || v2.type != Type.DBL) {
            throw new RuntimeException("Tipos incompatíveis para " + operation + ": "
                    + v1.type + " e " + v2.type);
        }
    }

    private static void checkNumber(String operation, Value v) {
        if (v.type != Type.DBL) {
            throw new RuntimeException("Tipo incompatível para " + operation + ": " + v.type);
        }
    }
}
