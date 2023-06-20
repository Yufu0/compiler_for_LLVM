package javalette.llvm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Program implements LlvmElement {
    private List<GlobalConstant> globalConstants;

    private List<TypeDefinition> typeDefinitions;
    private List<FunctionDeclaration> functionDeclarations;
    private List<Function> functions;

    public Program() {
        globalConstants = new ArrayList<>();
        functionDeclarations = new ArrayList<>();
        functions = new ArrayList<>();
        typeDefinitions = new ArrayList<>();
    }

    public List<TypeDefinition> getTypeDefinitions() {
        return typeDefinitions;
    }

    public List<GlobalConstant> getGlobalConstants() {
        return globalConstants;
    }

    public List<FunctionDeclaration> getFunctionDeclarations() {
        return functionDeclarations;
    }

    public List<Function> getFunctions() {
        return functions;
    }

    @Override
    public String write() {
        String str = "";
        for (TypeDefinition td : typeDefinitions) {
            str += td.write() + "\n";
        }
        str += "\n";
        for (GlobalConstant gc : globalConstants) {
            str += gc.write() + "\n";
        }
        str += "\n";
        for (FunctionDeclaration fd : functionDeclarations) {
            str += fd.write();
        }
        str += "\n";
        for (Function fun : functions) {
            str += fun.write() + "\n";
        }
        return str;
    }
}
