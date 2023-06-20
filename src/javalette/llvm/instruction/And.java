package javalette.llvm.instruction;

public class And extends Operation {
    String result;
    String type;
    String op1;
    String op2;

    public And(String result, String type, String op1, String op2) {
        this.result = result;
        this.type = type;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String write() {
        return result + " = and " + type + " " + op1 + ", " + op2;
    }
}
