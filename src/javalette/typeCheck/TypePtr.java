package javalette.typeCheck;

public class TypePtr extends TypeCode{
    TypeCode struct;
    TypePtr(String name, TypeCode struct) {
        super(name);
        this.struct = struct;
    }

    public TypeCode struct() {
        return struct;
    }

    public void setStruct(TypeCode struct) {
        this.struct = struct;
    }
}
