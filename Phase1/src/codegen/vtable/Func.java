package codegen.vtable;

import codegen.ast.PrimitiveType;
import codegen.ast.Type;

import java.util.ArrayList;

public class Func {
    private final String functName;
    private final Type returnType;
    private final String code;
    private final ArrayList<Argument> arguments;
    private int argumentCounter = 0;

    public Func(String functName, String code, Type returnType) {
        this.functName = functName;
        this.code = code;
        this.returnType = returnType;
        this.arguments = new ArrayList<>();
    }

    public Type getReturnType() {
        return returnType;
    }

    public void addArgument(PrimitiveType primitiveType) {
        arguments.add(new Argument(argumentCounter++, primitiveType));
    }

    public PrimitiveType getArgument(int placeInArguments) {
        return arguments.get(placeInArguments).type;
    }
}

class Argument {

    int place;
    PrimitiveType type;

    public Argument(int place, PrimitiveType type) {
        this.place = place;
        this.type = type;
    }
}