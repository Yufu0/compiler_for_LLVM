package javalette.llvm;


import javalette.llvm.instruction.Instruction;

import java.util.ArrayList;
import java.util.List;

public class Function implements LlvmElement {
    String type;
    String name;
    List<Var> arguments;
    List<Block> blocks;

    public Function(String type, String name) {
        this.type = type;
        this.name = name;
        arguments = new ArrayList<>();
        blocks = new ArrayList<>();
    }

    @Override
    public String write() {
        String str = "define " + type + " " + name + "(";

        if (arguments.size() > 0) {
            str += arguments.get(0).type + " " + arguments.get(0).name;
            for (int i = 1; i < arguments.size(); i++) {
                str += ", " + arguments.get(i).type + " " + arguments.get(i).name;
            }
        }
        str += ") {\n";
        for (Block blk : blocks) {
            str += blk.write() + "\n";
        }
        str += "}\n";
        return str;
    }
}
