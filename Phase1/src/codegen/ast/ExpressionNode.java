package codegen.ast;

import compiler.SemanticError;

import java.util.stream.Stream;

public class ExpressionNode extends SimpleNode {
    private boolean isIdentifier;
    private String resultName;
    private Type type;

    public ExpressionNode() {
        super(NodeType.EXPRESSION_STATEMENT);
    }

    public void setIsIdentifier() throws Exception {
        if (this.getChild(0).getNodeType() == NodeType.VAR_USE) {
            IdentifierNode id = ((IdentifierNode) this.getChild(0).getChild(0));
            if (!id.getValue().startsWith("%")) {
                resultName = "%" + id.getValue();
            } else {
                resultName = id.getValue();
            }
            if (id.getSI() == null)
                throw new SemanticError(id.getValue() + " not declared");
            type = id.getSI().getType();
        } else if (Stream.of(NodeType.ADDITION, NodeType.SUBTRACTION, NodeType.MULTIPLICATION, NodeType.DIVISION, NodeType.MOD).anyMatch(nodeType -> this.getChild(0).getNodeType().equals(nodeType))) {
            type = this.getChild(0).getSI().getType();
            resultName = this.getChild(0).getSI().getValue();
            this.setSI(this.getChild(0).getSI());
        } else if (this.getChild(0).getNodeType().equals(NodeType.IDENTIFIER)) {
            type = this.getChild(0).getSI().getType();
            resultName = this.getChild(0).getSI().getValue();
            this.setSI(this.getChild(0).getSI());
        } else if (this.getChild(0).getNodeType().equals(NodeType.READ_LINE)) {
            type = this.getChild(0).getSI().getType();
            resultName = "\"ReadLine()\"";
        } else {
            //EXPR -> LITERAL
            if (!this.getChild(0).getSI().getValue().equals("readInteger()")) {
                Literal literal = (Literal) this.getChild(0);
                resultName = this.getChild(0).toString();
                type = literal.getType();
            } else {
                type = this.getChild(0).getSI().getType();
            }

        }
        isIdentifier = true;
    }

    public String getResultName() {
        return resultName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}
