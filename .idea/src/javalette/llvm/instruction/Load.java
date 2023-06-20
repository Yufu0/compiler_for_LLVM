package javalette.llvm.instruction;

public class Load extends MemoryAccess {
    String value;
    String type;
    String typePtr;
    String pointer;

    public Load(String value, String type, String typePtr, String pointer) {
        this.value = value;
        this.type = type;
        this.typePtr = typePtr;
        this.pointer = pointer;
    }

    @Override
    public String write() {
        return value + " = load " + type + ", " + typePtr + " " + pointer;
    }
}
