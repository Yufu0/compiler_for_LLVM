package javalette.llvm.instruction;

public class Br extends TerminatorInstruction{
    String label;

    public Br(String label) {
        this.label = label;
    }

    @Override
    public String write() {
        return "br label %" + label;
    }
}
