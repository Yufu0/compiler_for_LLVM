// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public class Int  extends TypeBase {
  public int line_num, col_num, offset;
  public Int() { }

  public <R,A> R accept(javalette.Absyn.TypeBase.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof javalette.Absyn.Int) {
      return true;
    }
    return false;
  }

  public int hashCode() {
    return 37;
  }


}
