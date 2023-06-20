package javalette.llvm.instruction;

public class Ret extends TerminatorInstruction {
    String type;
    String value;

    public Ret(String type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String write() {
        return "ret " + type + " " + value;
    }
}
