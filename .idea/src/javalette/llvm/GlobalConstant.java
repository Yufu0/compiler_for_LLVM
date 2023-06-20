package javalette.llvm;

public class GlobalConstant implements LlvmElement {
    String name;
    String type;
    String value;

    public GlobalConstant(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    @Override
    public String write() {
        return name + " = internal constant " + type + " c\"" + value + "\"";
    }
}
