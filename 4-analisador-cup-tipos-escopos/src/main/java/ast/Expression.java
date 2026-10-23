package ast;

public abstract class Expression {
    private Value value;

    public void setValue(Value value) {
        this.value = value;
    }

    public Value getValue() {
        return this.value;
    }

    public abstract void accept(ASTVisitor v);
}