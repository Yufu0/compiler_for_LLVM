package javalette.llvm.instruction;

import javalette.llvm.Var;

import java.util.List;

public class Call extends Instruction {
    String name;
    String type;
    String nameFun;
    List<Var> args;

    public Call(String name, String type, String nameFun, List<Var> args) {
        this.name = name;
        this.type = type;
        this.nameFun = nameFun;
        this.args = args;
    }

    @Override
    public String write() {
        String str = name + " = call " +  type + " " + nameFun + "(";
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
