// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public class Ptr  extends TypeBase {
  public final String ident_;
  public int line_num, col_num, offset;
  public Ptr(String p1) { ident_ = p1; }

  public <R,A> R accept(javalette.Absyn.TypeBase.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof javalette.Absyn.Ptr) {
      javalette.Absyn.Ptr x = (javalette.Absyn.Ptr)o;
      return this.ident_.equals(x.ident_);
    }
    return false;
  }

  public int hashCode() {
    return this.ident_.hashCode();
  }


}