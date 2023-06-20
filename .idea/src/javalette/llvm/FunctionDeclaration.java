package javalette.llvm;

import java.util.List;

public class FunctionDeclaration implements LlvmElement {
    String type;
    String name;
    List<Var> arguments;

    public FunctionDeclaration(String type, String name, List<Var> arguments) {
        this.type = type;
        this.name = name;
        this.arguments = arguments;
    }

    @Override
    public String write() {
        String str = "declare " + type + " " + name + "(";
        if (arguments.size() > 0) {
            str += arguments.get(0).type + " " + arguments.get(0).name;
            for (int i = 1; i < arguments.size(); i++) {
                str += ", " + arguments.get(i).type + " " + arguments.get(i).name;
            }
        }

        str += ")\n";
        return str;
    }
}
