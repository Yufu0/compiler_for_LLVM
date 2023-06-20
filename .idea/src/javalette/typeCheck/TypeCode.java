package javalette.typeCheck;

import java.util.Objects;

public class TypeCode {
    String name;
    public static TypeCode CVoid = new TypeCode("void");
    public static TypeCode CInt = new TypeCode("int");
    public static TypeCode CString = new TypeCode("String");
    public static TypeCode CDouble = new TypeCode("double");
    public static TypeCode CBool = new TypeCode("boolean");

    TypeCode(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeCode typeCode = (TypeCode) o;
        return Objects.equals(name, typeCode.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
