package javalette.llvm.instruction;

public class Ptrtoint extends Instruction {
    String result;
    String typeInit;
    String valueInit;
    String toType;

    public Ptrtoint(String result, String typeInit, String valueInit, String toType) {
        this.result = result;
        this.typeInit = typeInit;
        this.valueInit = valueInit;
        this.toType = toType;
    }

    @Override
    public String write() {
        return result + " = ptrtoint " + typeInit + " " + valueInit + " to " + toType;
    }
}
