package javalette.typeCheck;

import java.util.Objects;

public class TypeArrayOf extends TypeCode {
    public TypeCode typeCode;

    public TypeArrayOf(TypeCode type) {
        super(type.name + "[]");
        this.typeCode = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TypeArrayOf that = (TypeArrayOf) o;
        return that.typeCode.equals(this.typeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), typeCode);
    }
}
