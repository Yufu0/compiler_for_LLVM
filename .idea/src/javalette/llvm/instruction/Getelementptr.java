package javalette.llvm.instruction;

import javalette.llvm.Var;

import java.util.Iterator;
import java.util.List;

public class Getelementptr extends MemoryAccess {
    String result;
    String type;
    List<Var> pointers;

    public Getelementptr(String result, String type, List<Var> pointers) {
        this.result = result;
        this.pointers = pointers;
        this.type = type;
    }

    @Override
    public String write() {
        String str = result + " = getelementptr " + type;
        Iterator<Var> it = pointers.iterator();
        for (Var var : pointers) {
            str += ", " + var.type + " " + var.name;
        }
        return str;
    }
}
