// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public class Base  extends Type {
  public final TypeBase typebase_;
  public int line_num, col_num, offset;
  public Base(TypeBase p1) { typebase_ = p1; }

  public <R,A> R accept(javalette.Absyn.Type.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof javalette.Absyn.Base) {
      javalette.Absyn.Base x = (javalette.Absyn.Base)o;
      return this.typebase_.equals(x.typebase_);
    }
    return false;
  }

  public int hashCode() {
    return this.typebase_.hashCode();
  }


}
