package javalette.typeCheck;

;

import java.util.*;

public class Env {
    private HashMap<String, FunType> signature;
    private LinkedList<HashMap<String, TypeCode>> contexts;
    private String currentFun;
    private TypeCode currentDeclareTypeCode;

    private TypeCode currentdeclareStruct;

    private Map<String, TypeCode> declaredStruct;

    private Map<String, TypeCode> declaredPtr;

    private Map<TypeCode, Map<String, TypeCode>> structMember;

    private Map<TypeCode, TypeCode> structToPtr;

    public void emptyEnv() {
        signature = new HashMap<>();
        contexts = new LinkedList<>();
        currentFun = null;
        currentDeclareTypeCode = null;
        currentdeclareStruct = null;
        declaredStruct = new HashMap<>();
        declaredPtr = new HashMap<>();
        structMember = new HashMap<>();
        structToPtr = new HashMap<>();
    }
    public boolean isFunDefined(String id) {
        return signature.containsKey(id);
    }
    public boolean isVarDeclared(String id) {
        Iterator<HashMap<String, TypeCode>> iterator = contexts.iterator();
        if (!iterator.hasNext()) return false;
        HashMap<String, TypeCode> context = iterator.next();
        while (!context.containsKey(id) && iterator.hasNext())
            context = iterator.next();
        return context.containsKey(id);
    }

    public boolean isVarDeclaredInLatestBlock(String id) {
        return contexts.getFirst().containsKey(id);
    }
    public TypeCode lookupVar(String id) {
        Iterator<HashMap<String, TypeCode>> iterator = contexts.iterator();
        if (!iterator.hasNext()) return null;
        HashMap<String, TypeCode> context = iterator.next();
        while (!context.containsKey(id) && iterator.hasNext())
            context = iterator.next();
        if (!context.containsKey(id)) return null;
        return context.get(id);
    }
    public FunType lookupFun(String id) {
        return signature.get(id);
    }

    public void newBlock() {
        contexts.addFirst(new HashMap<>());
    }

    public void declareVar(String id, TypeCode ty) {
        contexts.getFirst().put(id,ty);
    }

    public void defineFun(String id, FunType fnTy) {
        signature.put(id, fnTy);
    }

    public void endBlock() {
        contexts.removeFirst();
    }

    public void setDeclareType(TypeCode ty) {
        currentDeclareTypeCode = ty;
    }
    public TypeCode getCurrentDeclareType() {
        return currentDeclareTypeCode;
    }
    public void unsetDeclareType() {
        currentDeclareTypeCode = null;
    }

    public void setCurrentFun(String id) {
        currentFun = id;
    }
    public String getCurrentFun() {
        return currentFun;
    }
    public void unsetCurrentFun() {
        currentFun = null;
    }

    public TypeCode typeArrayOf(TypeCode type, int dim) {
        for (int i = 0; i < dim; i++) {
            type = new TypeArrayOf(type);
        }
        return type;
    }

    public int getDimOfArray(TypeCode ty) {
        if (!(ty instanceof TypeArrayOf)) return 0;
        return 1 + getDimOfArray(((TypeArrayOf)ty).typeCode);
    }

    public boolean isStructDeclared(String name) {
        return declaredStruct.containsKey(name);
    }

    public void declareStruct(String name) {
        TypeCode ty = new TypeStruct(name);
        declaredStruct.put(name, ty);
        structMember.put(ty, new HashMap<>());
    }

    public TypeCode structType(String name) {
        return declaredStruct.get(name);
    }

    public boolean isPtrDeclared(String name) {
        return declaredPtr.containsKey(name);
    }

    public void declarePtr(String name, String nameStruct) {
        TypeCode typePtr = new TypePtr(name, null);
        declaredPtr.put(name, typePtr);
    }

    public TypeCode ptrType(String name) {
        return declaredPtr.get(name);
    }

    public void setCurrentdeclareStruct(String name) {
        currentdeclareStruct = declaredStruct.get(name);
    }
    public void unsetCurrentdeclareStruct() {
        currentdeclareStruct = null;
    }

    public boolean isMemberDefined(String name) {
        return structMember.get(currentdeclareStruct).containsKey(name);
    }

    public boolean isMemberDefined(TypeCode typeStruct, String name) {
        return structMember.get(typeStruct).containsKey(name);
    }

    public void addMember(String name, TypeCode ty) {
        structMember.get(currentdeclareStruct).put(name, ty);
    }

    public TypeCode getMemberType(TypeCode typeStruct, String name) {
        return structMember.get(typeStruct).get(name);
    }

    public TypeCode structToPtr(TypeCode typeStruct) {
        return structToPtr.get(typeStruct);
    }

    public void addStructToPtr(TypeCode struct, TypeCode ptr) {
        structToPtr.put(struct, ptr);
        ((TypePtr)ptr).setStruct(struct);
    }

}
