package javalette.llvm;

import javalette.llvm.instruction.Instruction;
import java.util.*;
import java.util.stream.Collectors;


public class LlvmEnv {
    private HashMap<String, Var> signature;
    private LinkedList<HashMap<String, Var>> contexts;
    private Program program;
    private Function currentFunction;
    private Block currentBlock;
    private String currentDeclareType;
    private int currentCountVar;
    private int currentCountTmp;
    private int currentCountLabel;
    private int currentCountGlobalConstant;
    private Var op1;
    private Var op2;
    private Map<String, String> mapElemContained;
    private Map<String,Integer> mapArrayTypeDefinition;

    private String currentdeclareStruct;

    private Map<String, String> declaredStruct;

    private Map<String, String> declaredPtr;

    private Map<String, Map<String, String>> structMember;

    private Map<String, Map<String, Integer>> structMemberPosition;

    private Map<String, String> structToPtr;
    private Map<String, String> prtToStruct;

    public LlvmEnv() {
        this.signature = new HashMap<>();
        this.contexts = new LinkedList<>();
        this.program = new Program();
        this.currentFunction = null;
        this.currentBlock = null;
        this.currentDeclareType = null;
        this.currentCountVar = 0;
        this.currentCountTmp = 0;
        this.currentCountLabel = 0;
        this.currentCountGlobalConstant = 0;
        this.op1 = null;
        this.op2 = null;
        this.mapElemContained = new HashMap<>();
        this.mapArrayTypeDefinition = new HashMap<>();
        currentdeclareStruct = null;
        declaredStruct = new HashMap<>();
        declaredPtr = new HashMap<>();
        structMember = new HashMap<>();
        structMemberPosition = new HashMap<>();
        structToPtr = new HashMap<>();
        prtToStruct = new HashMap<>();
    }

    public Var getOp1() {
        return op1;
    }

    public void setOp1(Var op1) {
        this.op1 = op1;
    }

    public Var getOp2() {
        return op2;
    }

    public void setOp2(Var op2) {
        this.op2 = op2;
    }

    public void unsetOp() {
        op1 = null;
        op2 = null;
    }

    public Block getCurrentBlock() {
        return currentBlock;
    }

    public Program getProgram() {
        return program;
    }

    public int getCurrentCountVar() {
        return currentCountVar;
    }

    public void resetCurrentCountVar() {
        this.currentCountVar = 0;
    }

    public int getCurrentCountTmp() {
        return currentCountTmp;
    }

    public void resetCurrentCountTmp() {
        this.currentCountTmp = 0;
    }

    public int getCurrentCountLabel() {
        return currentCountLabel;
    }

    public void resetCurrentCountLabel() {
        this.currentCountLabel = 0;
    }

    public void incrCurrentCountVar() {
        this.currentCountVar++;
    }

    public void incrCurrentCountTmp() {
        this.currentCountTmp++;
    }

    public void incrCurrentCountLabel() {
        this.currentCountLabel++;
    }

    public Var lookupVar(String id) {
        Iterator<HashMap<String, Var>> iterator = contexts.iterator();
        if (!iterator.hasNext()) return null;
        HashMap<String, Var> context = iterator.next();
        while (!context.containsKey(id) && iterator.hasNext())
            context = iterator.next();
        if (!context.containsKey(id)) return null;
        return context.get(id);
    }

    public void declareVar(String id, Var var) {
        contexts.getFirst().put(id, var);
    }

    public void addInstruction(Instruction instruction) {
        getCurrentBlock().instructions.add(instruction);
    }

    public void setDeclareType(String ty) {
        currentDeclareType = ty;
    }
    public String getCurrentDeclareType() {
        return currentDeclareType;
    }
    public void unsetDeclareType() {
        currentDeclareType = null;
    }

    public void newContext() {
        contexts.addFirst(new HashMap<>());
    }

    public void endContext() {
        contexts.removeFirst();
    }

    public void newBlock(String label) {
        currentBlock = new Block(label, new ArrayList<>());
        currentFunction.blocks.add(currentBlock);
    }

    public void newFunction(String type, String name) {
        Function function = new Function(type, name);
        program.getFunctions().add(function);
        currentFunction = function;
    }

    public Function getCurrentFunction() {
        return currentFunction;
    }

    public Var lookupFun(String id) {
        return signature.get(id);
    }

    public void defineFun(String id, String type) {
        signature.put(id, new Var(type, "@" + id));
    }

    public void declareFun(String id, String type, List<Var> args) {
        FunctionDeclaration fd = new FunctionDeclaration(type, id, args);
        getProgram().getFunctionDeclarations().add(fd);
    }

    public Var declareString(String str) {
        currentCountGlobalConstant++;
        String name = "@s" + currentCountGlobalConstant;
        String type = "[" + (str.length() + 1) + " x i8]";
        program.getGlobalConstants().add(new GlobalConstant(name, type, str + "\\00"));
        Var var = new Var(type, name);
        return var;
    }

    public void defineTypeArray(String baseType, int dim) {
        // if not array return (dim = 0)
        if (dim == 0)
            return;
        // call recursively
        defineTypeArray(baseType, dim - 1);

        if (mapArrayTypeDefinition.containsKey(baseType)) mapArrayTypeDefinition.put(baseType, Math.max(mapArrayTypeDefinition.get(baseType), dim));
        else mapArrayTypeDefinition.put(baseType, dim);

        // add in lookup table
        mapElemContained.put(getTypeArrayStruct(baseType, dim), getTypeArrayPtr(baseType, dim-1));
        mapElemContained.put(getTypeArrayPtr(baseType, dim), getTypeArrayStruct(baseType, dim));
    }

    public void writeDefinedTypeArray() {
        mapArrayTypeDefinition.forEach((baseType, dim) -> {
            for (int i = 1; i <= dim; i++) {
                program.getTypeDefinitions().add(new TypeDefinition(
                        getTypeArrayPtr(baseType, i),
                        getTypeArrayStruct(baseType, i) + "*"
                ));
            }
            for (int i = 1; i <= dim; i++) {
                program.getTypeDefinitions().add(new TypeDefinition(
                        getTypeArrayStruct(baseType, i),
                        "{i32, [0 x " + getTypeArrayPtr(baseType, i-1) + "]}"
                ));
            }
        });
    }

    public String getTypeArrayPtr(String baseType, int dim) {
        if (dim == 0) return baseType;
        if (baseType.startsWith("%")) baseType = baseType.split("%")[1];
        return "%array_" + dim + "_" + baseType;
    }
    public String getTypeArrayStruct(String baseType, int dim) {
        if (dim == 0) return baseType;
        if (baseType.startsWith("%")) baseType = baseType.split("%")[1];
        return "%struct_array_" + dim + "_" + baseType;
    }

    public String lookupElemContained(String ty) {
        return mapElemContained.get(ty);
    }

    public void declareStruct(String name) {
        String type = "%struct__" + name;
        declaredStruct.put(name, type);
        structMember.put(type, new HashMap<>());
        structMemberPosition.put(type, new HashMap<>());
    }

    public void declarePtr(String name, String nameStruct) {
        String typePtr = "%ptr__" + name;
        declaredPtr.put(name, typePtr);
    }

    public boolean isDeclaredPtr(String name) {
        return declaredPtr.containsValue(name);
    }

    public void setCurrentdeclareStruct(String name) {
        currentdeclareStruct = declaredStruct.get(name);
    }

    public void unsetCurrentdeclareStruct() {
        currentdeclareStruct = null;
    }

    public String structType(String name) {
        return declaredStruct.get(name);
    }

    public void addStructToPtr(String struct, String ptr) {
        structToPtr.put(struct, ptr);
        prtToStruct.put(ptr, struct);
    }

    public String structToPtr(String struct) {
        return structToPtr.get(struct);
    }

    public String prtToStruct(String ptr) {
        return prtToStruct.get(ptr);
    }

    public String ptrType(String name) {
        return declaredPtr.get(name);
    }

    public void addMember(String name, String type) {
        structMember.get(currentdeclareStruct).put(name, type);
        int position = structMemberPosition.get(currentdeclareStruct).size();
        structMemberPosition.get(currentdeclareStruct).put(name, position);
    }

    public Set<String> getMembers(String struct) {
        return structMember.get(struct).keySet();
    }

    public String getMemberType(String struct, String name) {
        return structMember.get(struct).get(name);
    }

    public int getMemberPosition(String struct, String name) {
        return structMemberPosition.get(struct).get(name);
    }

    public void writeDefinedTypeStruct() {
        for (String structType : declaredStruct.values()) {
            String definition = "{ ";
            List<String> members = structMemberPosition.get(structType).entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (members.size() > 0) {
                definition += getMemberType(structType, members.get(0));
                for (int i = 1; i < members.size(); i++) {
                    definition += ", " + getMemberType(structType, members.get(i));
                }
            }
            definition += " }";
            program.getTypeDefinitions().add(new TypeDefinition(
                    structType,
                    definition
            ));
        }
    }

    public void writeDefinedTypePtr() {
        for (String ptrType : declaredPtr.values()) {
            program.getTypeDefinitions().add(new TypeDefinition(
                    ptrType,
                    prtToStruct(ptrType) + "*"
            ));
        }
    }
}
