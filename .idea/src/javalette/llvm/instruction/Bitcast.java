package javalette.llvm.instruction;

public class Bitcast extends Instruction{
    String result;
    String typeInit;
    String valueInit;
    String toType;

    public Bitcast(String result, String typeInit, String valueInit, String toType) {
        this.result = result;
        this.typeInit = typeInit;
        this.valueInit = valueInit;
        this.toType = toType;
    }

    @Override
    public String write() {
        return result + " = bitcast " + typeInit + " " + valueInit + " to " + toType;
    }
}
