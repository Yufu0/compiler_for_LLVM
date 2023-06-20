package javalette.llvm.instruction;

public class Calloc extends MemoryAccess {
    String array;
    String num;
    String sizeElem;

    public Calloc(String array, String num, String sizeElem) {
        this.array = array;
        this.num = num;
        this.sizeElem = sizeElem;
    }

    @Override
    public String write() {
        return array + " = call i8* @calloc(i32 " + num + ", i32 " + sizeElem +")";
    }
}
