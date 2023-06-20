package javalette.typeCheck;

import java.util.LinkedList;

public class FunType {
    public TypeCode val;
    public LinkedList<TypeCode> args;

    public FunType(TypeCode val, LinkedList<TypeCode> args) {
        this.val = val;
        this.args = args;
    }
}
