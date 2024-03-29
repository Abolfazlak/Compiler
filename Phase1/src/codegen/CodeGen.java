package codegen;

import codegen.ast.*;
import codegen.ast.Literal;
import codegen.symboltable.BlockType;
import codegen.symboltable.SymbolInfo;
import codegen.symboltable.Scope;
import codegen.symboltable.SymbolTable;
import codegen.vtable.Func;
import codegen.vtable.VTable;
import compiler.SemanticError;

import java.io.Writer;

public class CodeGen {

    private static final SymbolTable spaghettiStack = new SymbolTable();
    private static final VTable vTable = new VTable();
    private static final int USER_INPUT_SIZE = 20;
    public static String dataSeg = ".data\n";
    public static String textSeg = ".text\n";
    private static int readLineCounter = 0;

    static {
        dataSeg += "\tnewLine: \t.asciiz \t\"\\n\"\n";
        dataSeg += "\tbool_0: \t.asciiz \t\"false\"\n";
        dataSeg += "\tbool_1: \t.asciiz \t\"true\"\n";
        dataSeg += "\tzeroDouble: \t.double \t0.0\n";
        textSeg += "\tldc1\t$f0, zeroDouble\n";
        textSeg += "\tjal\tmain\n";
        textSeg += "PrintBool:\n" +
                "\tbeq\t$a0, 0, Print_Bool0\n" +
                "\tla\t$v1, bool_1\n" +
                "\tb\tPrint_Bool1\n" +
                "Print_Bool0:\n" +
                "\tla\t$v1, bool_0\n" +
                "Print_Bool1:\n" +
                "\tjr\t$ra\n";
    }

    public static void cgen(Node node) throws Exception {
        switch (node.getNodeType()) {
            case START -> cgenStart(node);
            case METHOD_DECLARATION -> cgenMethodDeclaration(node, false);
            case BLOCK -> cgenBlock(node);
            case VARIABLE_DECLARATION -> cgenVariableDecl(node);
            case IDENTIFIER -> cgenIdentifier(node);
            case ASSIGN -> cgenAssign(node);
            case READ_INTEGER -> cgenReadInteger(node);
            case READ_LINE -> cgenReadLine(node);
            case EXPRESSION_STATEMENT -> cgenExpressionStatement(node);
            case LITERAL -> cgenLiteral((Literal) node);
            case UNARY_MINUS -> cgenUnaryMinus(node);
            case ADDITION, SUBTRACTION, MULTIPLICATION, DIVISION, MOD -> cgenArithmeticCalculation(node);
            case PRINT_STATEMENT -> cgenPrint(node);
            case ARGUMENT -> cgenArgument(node);
            case FUNCTION_CALL -> cgenFunctionCall(node);
            case PARAMETERS -> cgenParameters(node);
            case RETURN_STATEMENT -> cgenReturn(node);
            case IF_STATEMENT -> cgenIfStatement(node);
            case WHILE_STATEMENT -> cgenWhileStatement(node);
            case FOR_STATEMENT -> cgenForStatement(node);
            case BREAK_STATEMENT -> cgenBreak(node);
            case CONTINUE_STATEMENT -> cgenContinue(node);
            case EQUAL, NOT_EQUAL -> cgenEqNeq(node);
            case GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL -> cgenCompare(node);
            case BOOLEAN_AND, BOOLEAN_OR, BOOLEAN_NOT -> cgenBooleanCalculation(node);
            default -> cgenAllChildren(node);
        }
    }

    private static void cgenStart(Node node) throws Exception {
        for (Node child : node.getChildren()) {
            if (child.getNodeType().equals(NodeType.METHOD_DECLARATION))
                cgenMethodDeclaration(child, true);
        }
        for (Node child : node.getChildren()) {
            cgen(child);
        }
    }

    private static void cgenMethodDeclaration(Node node, boolean isFirstPass) throws Exception {
        PrimitiveNode returnNode = (PrimitiveNode) node.getChild(0);
        Type methodType = returnNode.getType();
        IdentifierNode identifierNode = (IdentifierNode) node.getChild(1);
        String methodName = identifierNode.getValue();

        if (isFirstPass) {
            Func code = new Func(methodName, "", methodType);
            spaghettiStack.enterScope(String.valueOf(node.getChild(1)), BlockType.METHOD, true);
            vTable.addFunction(methodName, code);
            cgen(node.getChild(2));
            spaghettiStack.leaveScope();
        } else {
            textSeg += methodName + ":\n";

            pushRegistersA();
            //arguments
            spaghettiStack.enterScope(String.valueOf(node.getChild(1)), BlockType.METHOD, false);
            cgen(node.getChild(3));
            if (methodName.equals("main")) {
                textSeg += "\t# This line is going to signal end of program.\n";
                textSeg += "\tli\t$v0, 10\n";
                textSeg += "\tsyscall\n";
            } else {
                popRegistersA();
                textSeg += "\tjr\t$ra\n";
            }
        }
    }

    private static void cgenLiteral(Literal node) throws Exception {
        SymbolInfo symbolInfo = new SymbolInfo(node.getType(), null);
        symbolInfo.setValue(String.valueOf(node));
        node.setSI(symbolInfo);
        node.getParent().setSI(symbolInfo);

        if (symbolInfo.getType().equals(PrimitiveType.STRING)) {
            String stringLabel = spaghettiStack + "_StringLiteral" + SymbolTable.getCurrentScope().getStringLiteralCounter();
            dataSeg += "\t" + stringLabel + ":\t.asciiz\t" + symbolInfo.getValue() + '\n';
            textSeg += "\tla\t$v1, " + stringLabel + '\n';
            SymbolTable.getCurrentScope().addStringLiteralCounter();
        } else {
            textSeg += "\tli\t$v1, " + symbolInfo.getValue() + '\n';
        }

        ((ExpressionNode) node.getParent()).setIsIdentifier();
    }

    private static void cgenUnaryMinus(Node node) throws Exception {
        cgen(node.getChild(0));
        node.setSI(node.getChild(0).getSI());
        node.getParent().setSI(node.getChild(0).getSI());
        textSeg += "\tnot\t$v1, $v1\n";
        textSeg += "\taddi\t$v1, $v1, 1\n";
    }

    private static void cgenIdentifier(Node node) throws Exception {
        IdentifierNode identifierNode = (IdentifierNode) node;
        String entry = identifierNode.getValue();
        if (node.getParent().getNodeType().equals(NodeType.FUNCTION_CALL)) {
            cgenAllChildren(node);
            textSeg += "\taddi\t$sp, $sp, -4\n";
            textSeg += "\tsw\t$ra, 0($sp)\n";
            textSeg += "\tjal\t" + node + '\n';
            textSeg += "\tlw\t$ra, 0($sp)\n";
            textSeg += "\taddi\t$sp, $sp, 4\n";
            node.getParent().setSI(new SymbolInfo(null, null, true));
        } else if (node.getParent().getNodeType().equals(NodeType.EXPRESSION_STATEMENT)) {
            SymbolInfo symbolInfo = spaghettiStack.getSI(entry);
            node.setSI(symbolInfo);
            ((ExpressionNode) node.getParent()).setIsIdentifier();
            if (spaghettiStack.getSI(entry).isArgument()) {
                int argumentPlace = spaghettiStack.getSI(entry).getArgumentPlace() - 1;
                textSeg += "\tmove\t$v1, $a" + argumentPlace + "\n";
            } else {
                textSeg += "\tlw\t$v1, " + spaghettiStack.getEntryScope(entry) + "." + entry + "\n";
            }
        }
    }

    private static void cgenExpressionStatement(Node node) throws Exception {
        cgen(node.getChild(0));
        node.setSI(node.getChild(0).getSI());
    }

    private static void cgenAssign(Node node) throws Exception {
        IdentifierNode identifierNode = (IdentifierNode) node.getChild(0);
        ExpressionNode expressionNode = (ExpressionNode) node.getChild(1);

        cgen(expressionNode);

        String entry = identifierNode.toString();
        SymbolInfo identifierSymbolInfo = spaghettiStack.getSI(entry);

        if (!identifierSymbolInfo.getType().equals(expressionNode.getSI().getType()))
            throw new SemanticError("Type of assign doesn't match " + identifierSymbolInfo.getType() + " -> " + expressionNode.getSI().getType());

        if (expressionNode.getSI().getType().equals(PrimitiveType.STRING)) {
            identifierSymbolInfo.setValue(expressionNode.getResultName());
        }

        if ((expressionNode.getResultName() != null) && expressionNode.getResultName().equals("\"ReadLine()\"")) {
            String stringLabel = "readLine_number" + (readLineCounter - 1);   // because we want  read line that we ++ it
            textSeg += "\tla\t$v1, " + stringLabel + '\n';
        }

        textSeg += "\tsw\t$v1  " + spaghettiStack.getEntryScope(entry).toString() + "." + entry + "\t\t\t\t\t\t# End assign\n";
        if (node.getChildren().size() == 3)
            cgen(node.getChild(2));
    }

    private static void cgenReadInteger(Node node) throws Exception {
        SymbolInfo symbolInfo = new SymbolInfo(PrimitiveType.INT, null);
        symbolInfo.setValue("readInteger()");
        node.setSI(symbolInfo);
        pushRegistersA();
        textSeg += "\tli\t$v0, 5\n";
        textSeg += "\tsyscall\n";
        textSeg += "\tmove\t$v1, $v0\n";
        popRegistersA();
        ((ExpressionNode) node.getParent()).setIsIdentifier();
    }

    private static void cgenReadLine(Node node) throws Exception {
        dataSeg += "\treadLine_number" + readLineCounter + ":\t.space\t" + USER_INPUT_SIZE + '\n';
        SymbolInfo symbolInfo = new SymbolInfo(PrimitiveType.STRING, null);
        symbolInfo.setValue("readLine()");
        node.setSI(symbolInfo);
        ((ExpressionNode) node.getParent()).setIsIdentifier();
        pushRegistersA();
        textSeg += "\tli\t$v0, 8\n";
        textSeg += "\tla\t$a0, " + "readLine_number" + readLineCounter++ + '\n';
        textSeg += "\tli\t$a1, " + USER_INPUT_SIZE + '\n';
        textSeg += "\tsyscall\n";
        textSeg += "\tmove\t$v1, $v0\n";
        popRegistersA();
        ((ExpressionNode) node.getParent()).setIsIdentifier();
    }

    private static void cgenVariableDecl(Node node) throws Exception {
        Type typePrimitive = ((PrimitiveNode) node.getChild(0)).getType();
        IdentifierNode identifierNode = (IdentifierNode) node.getChild(1);

        String data_id = spaghettiStack + "." + identifierNode.getValue() + ':';
        dataSeg += '\t' + data_id + '\t' + typePrimitive.getSignature() + '\t' + typePrimitive.getInitialValue() + '\n';

        if (node.getParent().getNodeType().equals(NodeType.ARGUMENT)) { // inside arguments declarations
            SymbolInfo symbolInfo = new SymbolInfo(typePrimitive, identifierNode);
            symbolInfo.setArgumentTrue(SymbolTable.getCurrentScope().getArgumentCounter());
            spaghettiStack.addEntry(identifierNode.getValue(), symbolInfo);

            vTable.getFunction(node.getParent().getParent().getParent().getChild(1).toString()).addArgument((PrimitiveType) typePrimitive);
        } else {
            SymbolInfo symbolInfo = new SymbolInfo(typePrimitive, identifierNode);
            spaghettiStack.addEntry(identifierNode.getValue(), symbolInfo);
        }
    }

    private static void cgenBlock(Node node) throws Exception {
        cgenAllChildren(node);
        spaghettiStack.leaveScope();
    }

    private static void cgenAllChildren(Node node) throws Exception {
        for (Node child : node.getChildren()) {
            cgen(child);
        }
    }

    private static void cgenArithmeticCalculation(Node node) throws Exception {
        pushRegistersS();
        ExpressionNode leftChild = (ExpressionNode) node.getChild(0);
        cgen(leftChild);
        if (leftChild.getSI().getType().equals(PrimitiveType.INT)) {
            textSeg += "\tmove\t$s0, $v1\n";
        } else if (leftChild.getSI().getType().equals(PrimitiveType.DOUBLE)) {
            textSeg += "\tmfc1.d\t$s0, $f10\n";
        }

        ExpressionNode rightChild = (ExpressionNode) node.getChild(1);
        cgen(rightChild);
        if (rightChild.getSI().getType().equals(PrimitiveType.INT)) {
            textSeg += "\tmove\t$s2, $v1\n";
        } else if (rightChild.getSI().getType().equals(PrimitiveType.DOUBLE)) {
            textSeg += "\tmfc1.d\t$s2, $f10\n";
        }

        Type type = widen(leftChild, rightChild, false);
        SymbolInfo symbolInfo = new SymbolInfo(type, null);

        if (type.equals(PrimitiveType.INT)) {
            switch (node.getNodeType()) {
                case ADDITION -> textSeg += "\tadd\t$v1, $s0, $s2\n";
                case SUBTRACTION -> textSeg += "\tsub\t$v1, $s0, $s2\n";
                case MULTIPLICATION -> textSeg += "\tmul\t$v1, $s0, $s2\n";
                case DIVISION -> textSeg += "\tdiv\t$v1, $s0, $s2\n";
                case MOD -> textSeg += "\trem\t$v1, $s0, $s2\n";
            }
        } else if (type.equals(PrimitiveType.STRING)) {
            if (!node.getNodeType().equals(NodeType.ADDITION))
                throw new SemanticError("You can just concat strings!");
            String finalString = (leftChild.getResultName() + rightChild.getResultName()).replaceAll("\"", "");
            symbolInfo.setValue(finalString);
            String stringLabel = spaghettiStack + "_StringLiteral" + SymbolTable.getCurrentScope().getStringLiteralCounter();
            dataSeg += "\t" + stringLabel + ":\t.asciiz\t\"" + symbolInfo.getValue() + "\"\n";
            textSeg += "\tla\t$v1, " + stringLabel + '\n';
            SymbolTable.getCurrentScope().addStringLiteralCounter();
        }
        popRegistersS();

        node.setSI(symbolInfo);
        ExpressionNode parent = (ExpressionNode) node.getParent();
        parent.setIsIdentifier();
    }

    private static void cgenPrint(Node node) throws Exception {
        Node inputNodes = node.getChild(0);
        for (Node child : inputNodes.getChildren()) {
            cgen(child.getChild(0));
            switch (child.getChild(0).getSI().getType().getPrimitive()) {
                case INT:
                    textSeg += "\tli\t$v0, 1\n";
                    textSeg += "\tadd\t$a0, $v1, $zero\n";
                    textSeg += "\tsyscall\n";
                    break;
                case DOUBLE:
                    textSeg += "\tli\t$v0, 3\n";
                    textSeg += "\tadd.d\t$f12, $f10, $f0\n";
                    textSeg += "\tsyscall\n";
                    break;
                case BOOL:
                    textSeg += "\tmove\t$a0, $v1\n";
                    textSeg += "\tsw\t$ra, 0($sp)\n";
                    textSeg += "\tjal\tPrintBool\n";
                    textSeg += "\tlw\t$ra, 0($sp)\n";
                case STRING:
                    textSeg += "\tli\t$v0, 4\n";
                    textSeg += "\tmove\t$a0, $v1\n";
                    textSeg += "\tsyscall\n";
                default:
                    break;
            }
        }

        textSeg += "\tli\t$v0, 4\n"
                + "\tla\t$a0, newLine\n"
                + "\tsyscall\n";

        cgen(node.getChild(1));
    }

    private static void cgenArgument(Node node) throws Exception {
        SymbolTable.getCurrentScope().addArgumentCounter();
        cgenVariableDecl(node.getChild(0));
    }

    private static void cgenFunctionCall(Node node) throws Exception {
        cgen(node.getChild(1));
        cgen(node.getChild(0));

        IdentifierNode identifierNode = (IdentifierNode) node.getChild(0);
        SymbolInfo symbolInfo = new SymbolInfo(vTable.getFunction(identifierNode.getValue()).getReturnType(), null, node.getSI().isFunction());
        node.setSI(symbolInfo);
    }

    private static void cgenParameters(Node node) throws Exception {
        for (int i = 0; i < node.getChild(0).getChildren().size(); i++) {
            pushRegistersA();
            cgenAllChildren(node.getChild(0).getChild(i));
            popRegistersA();
            textSeg += "\tmove\t$a" + i + ", $v1\n";

            String functionName = String.valueOf(node.getParent().getChild(0));
            if (!node.getChild(0).getChild(i).getSI().getType().equals(vTable.getFunction(functionName).getArgument(i)))
                throw new SemanticError("Function Call " + functionName + " Argument types doesn't match to the function!");
        }
    }

    private static void cgenReturn(Node node) throws Exception {
        cgen(node.getChild(0));

        Node nodeCrawler = node;
        while (!nodeCrawler.getNodeType().equals(NodeType.METHOD_DECLARATION)) {
            nodeCrawler = nodeCrawler.getParent();
        }

        PrimitiveType returnType = (PrimitiveType) ((PrimitiveNode) nodeCrawler.getChild(0)).getType();
        if (node.getChild(0).getSI() == null) {
            if (!returnType.equals(PrimitiveType.VOID)) {
                throw new SemanticError("Return value hasn't declared!");
            }
        } else if (!node.getChild(0).getSI().getType().equals(returnType)) {
            throw new SemanticError("Return value doesn't match!");
        }

        String methodName = ((IdentifierNode) nodeCrawler.getChild(1)).getValue();

        if (methodName.equals("main")) {
            textSeg += "\t# This line is going to signal end of program.\n";
            textSeg += "\tli\t$v0, 10\n";
            textSeg += "\tsyscall\n";
        } else {
            popRegistersA();
            textSeg += "\tjr\t$ra\n";
        }

        cgen(node.getChild(1));
    }

    private static void cgenIfStatement(Node node) throws Exception {
        SymbolTable.getCurrentScope().addConditionStmtCounter();

        String entry = "IfStmt_" + SymbolTable.getCurrentScope().getConditionStmtCounter();
        String labelName = spaghettiStack + "_" + entry;

        String entryElse = "ElseStmt_" + SymbolTable.getCurrentScope().getConditionStmtCounter();
        String labelNameElse = spaghettiStack + "_" + entryElse;

        spaghettiStack.enterScope(entry, BlockType.CONDITION, true);

        cgen(node.getChild(0));
        SymbolInfo conditionSymbolInfo = node.getChild(0).getSI();
        if (!conditionSymbolInfo.getType().equals(PrimitiveType.BOOL)) {
            throw new SemanticError("Condition isn't boolean ");
        }

        textSeg += "\tbeq\t$v1, 0, " + labelName + '\n';
        cgen(node.getChild(1));

        if (node.getChildren().size() == 4) {
            textSeg += "\tb\t" + labelNameElse + '\n';
        }

        textSeg += labelName + ":\n";

        if (node.getChildren().size() == 3) {
            cgen(node.getChild(2));
        } else {
            spaghettiStack.enterScope(entryElse, BlockType.CONDITION, true);
            cgen(node.getChild(2));
            textSeg += labelNameElse + ":\n";
            cgen(node.getChild(3));
        }

    }

    private static void cgenWhileStatement(Node node) throws Exception {
        SymbolTable.getCurrentScope().addLoopStmtCounter();

        String entry = "LoopStmt_" + SymbolTable.getCurrentScope().getLoopStmtCounter();
        String labelNameFirst = spaghettiStack + "_" + entry + "_start";
        String labelNameUpdate = spaghettiStack + "_" + entry + "_update";
        String labelNameEnd = spaghettiStack + "_" + entry + "_end";

        spaghettiStack.enterScope(entry, BlockType.LOOP, true);

        textSeg += labelNameFirst + ":\n";

        cgen(node.getChild(0)); //  calculate condition -> return $v1
        SymbolInfo conditionSymbolInfo = node.getChild(0).getSI();
        if (!conditionSymbolInfo.getType().equals(PrimitiveType.BOOL)) {
            throw new SemanticError("Condition isn't boolean ");
        }

        textSeg += "\tbeq\t$v1, 0, " + labelNameEnd + '\n';

        textSeg += labelNameUpdate + ":\n";

        cgen(node.getChild(1));

        textSeg += "\tb\t" + labelNameFirst + '\n';
        textSeg += labelNameEnd + ":\n";

        cgen(node.getChild(2));

    }

    private static void cgenForStatement(Node node) throws Exception {
        SymbolTable.getCurrentScope().addLoopStmtCounter();

        String entry = "LoopStmt_" + SymbolTable.getCurrentScope().getLoopStmtCounter();
        String labelNameFirst = spaghettiStack + "_" + entry + "_start";
        String labelNameUpdate = spaghettiStack + "_" + entry + "_update";
        String labelNameEnd = spaghettiStack + "_" + entry + "_end";

        spaghettiStack.enterScope(entry, BlockType.LOOP, true);

        cgen(node.getChild(0));

        textSeg += labelNameFirst + ":\n";

        cgen(node.getChild(1)); //  calculate condition -> return $v1
        SymbolInfo conditionSymbolInfo = node.getChild(1).getSI();
        if (!conditionSymbolInfo.getType().equals(PrimitiveType.BOOL)) {
            throw new SemanticError("Condition isn't boolean ");
        }

        textSeg += "\tbeq\t$v1, 0, " + labelNameEnd + '\n';

        cgen(node.getChild(3));

        textSeg += labelNameUpdate + ":\n";
        cgen(node.getChild(2));

        textSeg += "\tb\t" + labelNameFirst + '\n';
        textSeg += labelNameEnd + ":\n";

        cgen(node.getChild(4));
    }

    private static void cgenBreak(Node node) throws Exception {

        // Find Loop Block to get correct labels
        Scope scopeCrawler = SymbolTable.getCurrentScope();
        Node nodeCrawler = node;
        while (!scopeCrawler.getBlockType().equals(BlockType.LOOP)) {
            while (!nodeCrawler.getNodeType().equals(NodeType.BLOCK)) {
                nodeCrawler = nodeCrawler.getParent();
            }
            nodeCrawler = nodeCrawler.getParent();
            scopeCrawler = scopeCrawler.getParent();
        }

        String entry = "LoopStmt_" + scopeCrawler.getParent().getLoopStmtCounter();
        String labelNameEnd = scopeCrawler.getParent() + "_" + entry + "_end";
        textSeg += "\tb\t" + labelNameEnd + "\n";

        // continue parsing
        if (node.getChildren().size() != 0)
            cgen(node.getChild(0));
    }

    private static void cgenContinue(Node node) throws Exception {
        Scope scopeCrawler = SymbolTable.getCurrentScope();
        Node nodeCrawler = node;
        while (!scopeCrawler.getBlockType().equals(BlockType.LOOP)) {
            while (!nodeCrawler.getNodeType().equals(NodeType.BLOCK)) {
                nodeCrawler = nodeCrawler.getParent();
            }
            nodeCrawler = nodeCrawler.getParent();
            scopeCrawler = scopeCrawler.getParent();
        }

        String entry = "LoopStmt_" + scopeCrawler.getParent().getLoopStmtCounter();
        String labelNameEnd = scopeCrawler.getParent() + "_" + entry + "_update";
        textSeg += "\tb\t" + labelNameEnd + "\n";
        if (node.getChildren().size() != 0)
            cgen(node.getChild(0));
    }

    private static void cgenEqNeq(Node node) throws Exception {
        pushRegistersS();
        ExpressionNode leftChild = (ExpressionNode) node.getChild(0);
        cgen(leftChild);
        textSeg += "\tmove\t$s0, $v1\n";

        ExpressionNode rightChild = (ExpressionNode) node.getChild(1);
        cgen(rightChild);
        textSeg += "\tmove\t$s2, $v1\n";

        Type type = widen(leftChild, rightChild, true);

        if (node.getNodeType().equals(NodeType.EQUAL))
            textSeg += "\tseq\t$v1, $s0, $s2\n";
        else if (node.getNodeType().equals(NodeType.NOT_EQUAL))
            textSeg += "\tsne\t$v1, $s0, $s2\n";

        if (type.equals(PrimitiveType.STRING)) {
            String leftValue = leftChild.getSI().getValue();
            String rightValue = rightChild.getSI().getValue();
            if (node.getNodeType().equals(NodeType.EQUAL)) {
                if (leftValue.equals(rightValue))
                    textSeg += "\taddi\t$v1, $zero, 1\n";
                else
                    textSeg += "\taddi\t$v1, $zero, 0\n";
            } else {
                if (leftValue.equals(rightValue))
                    textSeg += "\taddi\t$v1, $zero, 0\n";
                else
                    textSeg += "\taddi\t$v1, $zero, 1\n";
            }

        }

        popRegistersS();

        SymbolInfo symbolInfo = new SymbolInfo(PrimitiveType.BOOL, null);
        node.setSI(symbolInfo);
        ExpressionNode parent = (ExpressionNode) node.getParent();
        parent.setSI(symbolInfo);
    }

    private static void cgenCompare(Node node) throws Exception {
        pushRegistersS();
        ExpressionNode leftChild = (ExpressionNode) node.getChild(0);
        cgen(leftChild);
        if (leftChild.getSI().getType().equals(PrimitiveType.INT)) {
            textSeg += "\tmove\t$s0, $v1\n";
        } else if (leftChild.getSI().getType().equals(PrimitiveType.DOUBLE)) {
            textSeg += "\tmfc1.d\t$s0, $f10\n"; // store in $s0, $s1
        }

        ExpressionNode rightChild = (ExpressionNode) node.getChild(1);
        cgen(rightChild);
        if (rightChild.getSI().getType().equals(PrimitiveType.INT)) {
            textSeg += "\tmove\t$s2, $v1\n";
        } else if (rightChild.getSI().getType().equals(PrimitiveType.DOUBLE)) {
            textSeg += "\tmfc1.d\t$s2, $f10\n"; // store in $s2, $s3
        }

        Type type = widen(leftChild, rightChild, false);

        if (type.equals(PrimitiveType.INT)) {
            switch (node.getNodeType()) {
                case GREATER_THAN -> textSeg += "\tsgt\t$v1, $s0, $s2\n";
                case GREATER_THAN_OR_EQUAL -> textSeg += "\tsge\t$v1, $s0, $s2\n";
                case LESS_THAN -> textSeg += "\tslt\t$v1, $s0, $s2\n";
                case LESS_THAN_OR_EQUAL -> textSeg += "\tsle\t$v1, $s0, $s2\n";
            }
        }
        popRegistersS();

        SymbolInfo symbolInfo = new SymbolInfo(PrimitiveType.BOOL, null);
        node.setSI(symbolInfo);
        ExpressionNode parent = (ExpressionNode) node.getParent();
        parent.setSI(symbolInfo);
    }

    private static void cgenBooleanCalculation(Node node) throws Exception {
        pushRegistersS();
        ExpressionNode leftChild = (ExpressionNode) node.getChild(0);
        cgen(leftChild);
        textSeg += "\tmove\t$s0, $v1\n";

        ExpressionNode rightChild = null;
        if (!node.getNodeType().equals(NodeType.BOOLEAN_NOT)) {
            rightChild = (ExpressionNode) node.getChild(1);
            cgen(rightChild);
            textSeg += "\tmove\t$s2, $v1\n";
            widen(leftChild, rightChild, true);
        } else {
            if (!leftChild.getChild(0).getSI().getType().equals(PrimitiveType.BOOL))
                throw new SemanticError("can't do BOOLEAN_NOT on " + leftChild.getSI().getType());
        }

        switch (node.getNodeType()) {
            case BOOLEAN_AND -> textSeg += "\tand\t$v1, $s0, $s2\n";
            case BOOLEAN_OR -> textSeg += "\tor\t$v1, $s0, $s2\n";
            case BOOLEAN_NOT -> {
                textSeg += "\tneg\t$v1, $s0\n";
                textSeg += "\tadd\t$v1, $v1, 1\n";
            }
        }

        popRegistersS();

        SymbolInfo symbolInfo = new SymbolInfo(PrimitiveType.BOOL, null);
        node.setSI(symbolInfo);
        ExpressionNode parent = (ExpressionNode) node.getParent();
        parent.setSI(symbolInfo);
    }

    private static void pushRegistersS() {
        textSeg += "\taddi\t$sp, $sp, -24\n";
        textSeg += "\tsw\t$s0, 0($sp)\n";
        textSeg += "\tsw\t$s1, 4($sp)\n";
        textSeg += "\tsw\t$s2, 8($sp)\n";
        textSeg += "\tsw\t$s3, 12($sp)\n";
        textSeg += "\tsw\t$s4, 16($sp)\n";
        textSeg += "\tsw\t$s5, 20($sp)\n";
    }

    private static void popRegistersS() {
        textSeg += "\tlw\t$s0, 0($sp)\n";
        textSeg += "\tlw\t$s1, 4($sp)\n";
        textSeg += "\tlw\t$s2, 8($sp)\n";
        textSeg += "\tlw\t$s3, 12($sp)\n";
        textSeg += "\tlw\t$s4, 16($sp)\n";
        textSeg += "\tlw\t$s5, 20($sp)\n";
        textSeg += "\taddi\t$sp, $sp, 24\n";
    }

    private static void pushRegistersA() {
        textSeg += "\taddi\t$sp, $sp, -16\n";
        textSeg += "\tsw\t$a0, 0($sp)\n";
        textSeg += "\tsw\t$a1, 4($sp)\n";
        textSeg += "\tsw\t$a2, 8($sp)\n";
        textSeg += "\tsw\t$a3, 12($sp)\n";
    }

    private static void popRegistersA() {
        textSeg += "\tlw\t$a0, 0($sp)\n";
        textSeg += "\tlw\t$a1, 4($sp)\n";
        textSeg += "\tlw\t$a2, 8($sp)\n";
        textSeg += "\tlw\t$a3, 12($sp)\n";
        textSeg += "\taddi\t$sp, $sp, 16\n";
    }


    private static Type widen(ExpressionNode leftChild, ExpressionNode rightChild, boolean isLogical) throws Exception {
        if (leftChild.getSI().getType().equals(rightChild.getSI().getType())) {
            Type type = leftChild.getSI().getType();
            if ((type.equals(PrimitiveType.INT) || type.equals(PrimitiveType.DOUBLE) || type.equals(PrimitiveType.STRING)) && !isLogical)
                return type;
            else if (isLogical)
                return type;
            throw new SemanticError("can't do operation on " + type);
        }
        throw new SemanticError("can't do operation on " + leftChild.getType() + " and " + rightChild.getType());
    }

    public static void compile(RootNode root, Writer writer) throws Exception {
        cgen(root);
        writer.write(dataSeg);
        writer.write(textSeg);
    }

}
