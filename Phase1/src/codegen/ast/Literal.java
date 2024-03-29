package codegen.ast;

import codegen.ast.NodeType;
import codegen.ast.SimpleNode;
import codegen.ast.Type;

public abstract class Literal extends SimpleNode {

    private final Type type;

    public Literal(Type type) {
        super(NodeType.LITERAL);
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
