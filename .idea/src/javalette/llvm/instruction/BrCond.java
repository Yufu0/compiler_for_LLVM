package javalette.llvm.instruction;

public class BrCond extends TerminatorInstruction{
    String cond;
    String labelIf;
    String labelEnd;

    public BrCond(String cond, String labelIf, String labelEnd) {
        this.cond = cond;
        this.labelIf = labelIf;
        this.labelEnd = labelEnd;
    }

    @Override
    public String write() {
        return "br i1 " +  cond + ", label %" + labelIf + ", label %" + labelEnd;
    }
}
