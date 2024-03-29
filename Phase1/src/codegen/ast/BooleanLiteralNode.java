package codegen.ast;

public class BooleanLiteralNode extends Literal {
    private final boolean value;
    private final int intVal;

    public BooleanLiteralNode(String value) {
        super(PrimitiveType.BOOL);
        this.value = Boolean.parseBoolean(value);
        intVal = this.value ? 1 : 0;
    }
    @Override
    public String toString() {
        return intVal + "";
    }
}
