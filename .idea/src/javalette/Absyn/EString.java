// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public class EString  extends Expr {
  public final String string_;
  public int line_num, col_num, offset;
  public EString(String p1) { string_ = p1; }

  public <R,A> R accept(javalette.Absyn.Expr.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof javalette.Absyn.EString) {
      javalette.Absyn.EString x = (javalette.Absyn.EString)o;
      return this.string_.equals(x.string_);
    }
    return false;
  }

  public int hashCode() {
    return this.string_.hashCode();
  }


}
