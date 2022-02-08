package codegen.symboltable;

import codegen.ast.Node;
import codegen.ast.Type;

public class SymbolInfo {
    private final Node node;
    private Type type;
    private String value;
    private boolean isArgument;
    private boolean isFunction;
    private int argumentPlace;

    public SymbolInfo(Type type, Node node) {
        this.type = type;
        this.node = node;
        this.isArgument = false;
    }

    public SymbolInfo(Type type, Node node, boolean isFunction) {
        this.type = type;
        this.node = node;
        this.isArgument = false;
        this.isFunction = isFunction;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setArgumentTrue(int argumentPlace) {
        isArgument = true;
        this.argumentPlace = argumentPlace;
    }

    public int getArgumentPlace() {
        return argumentPlace;
    }

    public boolean isArgument() {
        return isArgument;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
