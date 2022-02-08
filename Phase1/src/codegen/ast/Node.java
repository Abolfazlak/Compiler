package codegen.ast;

import codegen.symboltable.SymbolInfo;

import java.util.List;

public interface Node {
    NodeType getNodeType();

    SymbolInfo getSI();

    void setSI(SymbolInfo symbolInfo);

    void addChild(Node node);

    void addChild(Node... nodes);

    List<Node> getChildren();

    Node getChild(int index);

    Node getParent();

    void setParent(Node parent);

}
