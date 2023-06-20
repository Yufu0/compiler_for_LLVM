package javalette.llvm.instruction;

public class Store extends MemoryAccess {
    String type;
    String value;
    String typePtr;
    String pointer;

    public Store(String type, String value, String typePtr, String pointer) {
        this.type = type;
        this.value = value;
        this.typePtr = typePtr;
        this.pointer = pointer;
    }

    @Override
    public String write() {
        return "store " + type + " " + value + ", " + typePtr + " " + pointer;
    }
}
