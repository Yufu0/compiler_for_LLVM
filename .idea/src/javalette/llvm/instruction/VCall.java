package javalette.llvm.instruction;

import javalette.llvm.Var;

import java.util.List;

public class VCall extends Instruction {
    String type;
    String nameFun;
    List<Var> args;

    public VCall(String type, String nameFun, List<Var> args) {
        this.type = type;
        this.nameFun = nameFun;
        this.args = args;
    }

    @Override
    public String write() {
        String str = "call " +  type + " " + nameFun + "(";
        if (args.size() > 0) {
            str += args.get(0).type + " " + args.get(0).name;
            for (int i = 1; i < args.size(); i++) {
                str += ", " + args.get(i).type + " " + args.get(i).name;
            }
        }
        str += ")";
        return str;
    }
}
