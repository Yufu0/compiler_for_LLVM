package javalette.llvm.instruction;

public class Sub extends Operation {
    String result;
    String type;
    String op1;
    String op2;

    public Sub(String result, String type, String op1, String op2) {
        this.result = result;
        this.type = type;
        this.op1 = op1;
        this.op2 = op2;
    }

    @Override
    public String write() {
        return result + " = sub " + type + " " + op1 + ", " + op2;
    }
}
