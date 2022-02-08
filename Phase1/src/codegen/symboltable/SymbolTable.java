package codegen.symboltable;

import compiler.SemanticError;

public class SymbolTable {
    private static final Scope root = new Scope("RootScope", null, BlockType.ROOT);
    private static Scope currentScope;
    public SymbolTable() {
        currentScope = root;
    }

    public static Scope getCurrentScope() {
        return currentScope;
    }

    public Scope getEntryScope(String entry) throws Exception {
        Scope scopeCrawler = currentScope;
        while (true) {
            if (scopeCrawler.getScope().get(entry) != null)
                return scopeCrawler;
            else {
                if (scopeCrawler.getParent() != null)
                    scopeCrawler = scopeCrawler.getParent();
                else break;
            }
        }
        throw new SemanticError(entry + " not defined!");
    }

    public SymbolInfo getSI(String entry) throws Exception {
        Scope scopeCrawler = currentScope;
        while (true) {
            if (scopeCrawler.getScope().get(entry) != null)
                return scopeCrawler.getScope().get(entry);
            else {
                if (scopeCrawler.getParent() != null)
                    scopeCrawler = scopeCrawler.getParent();
                else break;
            }
        }
        throw new SemanticError(entry + " not defined!");
    }

    public void addEntry(String entry, SymbolInfo symbolInfo) throws Exception {
        if (currentScope.getScope().containsKey(entry)) {
            throw new SemanticError("current scope already contains an entry for " + entry);
        }
        currentScope.getScope().put(entry, symbolInfo);
    }

    public void enterScope(String name, BlockType blockType, boolean isFirstPass) {
        Scope newScope = null;
        if (isFirstPass) {
            newScope = new Scope(name + "Scope", currentScope, blockType);
            currentScope.addChild(newScope);
        } else {
            for (Scope child : currentScope.getChildren()) {
                newScope = child;
                if (child.getName().equals(name + "Scope"))
                    break;
            }
        }
        currentScope = newScope;
    }

    public void leaveScope() {
        currentScope = currentScope.getParent();
    }

    @Override
    public String toString() {
        return currentScope.getName();
    }
}
