package codegen.vtable;

import compiler.SemanticError;

import java.util.HashMap;

public class VTable {
    private final HashMap<String, Func> table;

    public VTable() {
        this.table = new HashMap<>();
    }

    public void addFunction(String name, Func func) {
        table.put(name, func);
    }

    public Func getFunction(String name) throws Exception {
        Func code = table.get(name);
        if (code == null) {
            throw new SemanticError("Function " + name + " not defined!");
        }
        return code;
    }
}
