package javalette.llvm;

public class TypeDefinition implements LlvmElement {
    String name;
    String definition;

    public TypeDefinition(String name, String definition) {
        this.name = name;
        this.definition = definition;
    }

    @Override
    public String write() {
        return name + " = type " + definition;
    }
}
