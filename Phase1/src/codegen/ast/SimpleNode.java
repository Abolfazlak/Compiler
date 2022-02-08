package codegen.ast;

import codegen.symboltable.SymbolInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SimpleNode implements Node {

    private final NodeType nodeType;
    private List<Node> children = new ArrayList<>();
    private Node parent;
    private SymbolInfo symbolInfo;

    public SimpleNode(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @Override
    public NodeType getNodeType() {
        return this.nodeType;
    }

    @Override
    public SymbolInfo getSI() {
        return this.symbolInfo;
    }

    @Override
    public void setSI(SymbolInfo symbolInfo) {
        this.symbolInfo = symbolInfo;
    }

    @Override
    public void addChild(Node node) {
        children.add(node);
    }

    @Override
    public void addChild(Node... nodes) {
        Collections.addAll(children, nodes);
    }

    @Override
    public List<Node> getChildren() {
        return children;
    }

    @Override
    public Node getChild(int index) {
        return children.get(index);
    }

    @Override
    public Node getParent() {
        return parent;
    }

    @Override
    public void setParent(Node parent) {
        this.parent = parent;
    }
}
