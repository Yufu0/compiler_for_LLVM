package javalette.llvm.instruction;

public class Alloca extends MemoryAccess {
    String pointer;
    String type;

    public Alloca(String pointer, String type) {
        this.pointer = pointer;
        this.type = type;
    }

    @Override
    public String write() {
        return pointer + " = alloca " + type;
    }
}
