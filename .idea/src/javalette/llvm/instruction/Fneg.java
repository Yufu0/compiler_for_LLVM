package javalette.llvm.instruction;

public class Fneg extends Operation {
    String result;
    String type;
    String value;

    public Fneg(String result, String type, String value) {
        this.result = result;
        this.type = type;
        this.value = value;
    }

    @Override
    public String write() {
        return result + " = fneg " + type + " " + value;
    }
}
