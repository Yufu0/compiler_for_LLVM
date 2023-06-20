package javalette.llvm.instruction;

public class Fcmp extends Comparison {
    String result;
    String cond;
    String type;
    String op1;
    String op2;

    public Fcmp(String result, String cond, String type, String op1, String op2) {
        this.result = result;
        this.cond = cond;
        this.type = type;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String write() {
        return result + " = fcmp " + cond + " " + type + " " + op1 + ", " + op2;
    }
}
