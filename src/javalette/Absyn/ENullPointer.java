// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public class ENullPointer  extends Expr {
  public final String ident_;
  public int line_num, col_num, offset;
  public ENullPointer(String p1) { ident_ = p1; }

  public <R,A> R accept(javalette.Absyn.Expr.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof javalette.Absyn.ENullPointer) {
      javalette.Absyn.ENullPointer x = (javalette.Absyn.ENullPointer)o;
      return this.ident_.equals(x.ident_);
    }
    return false;
  }

  public int hashCode() {
    return this.ident_.hashCode();
  }


}
