package javalette.llvm;

import javalette.llvm.instruction.Instruction;

import java.util.List;

public class Block implements LlvmElement {
    String label;
    List<Instruction> instructions;

    public Block(String label, List<Instruction> instructions) {
        this.label = label;
        this.instructions = instructions;
    }

    @Override
    public String write() {
        String str = label + ":";
        for (Instruction inst : instructions) {
            str += "\n    " + inst.write();
        }
        return str;
    }
}
