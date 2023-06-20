// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public class StructAss  extends Stmt {
  public final Expr expr_1, expr_2;
  public final String ident_;
  public int line_num, col_num, offset;
  public StructAss(Expr p1, String p2, Expr p3) { expr_1 = p1; ident_ = p2; expr_2 = p3; }

  public <R,A> R accept(javalette.Absyn.Stmt.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof javalette.Absyn.StructAss) {
      javalette.Absyn.StructAss x = (javalette.Absyn.StructAss)o;
      return this.expr_1.equals(x.expr_1) && this.ident_.equals(x.ident_) && this.expr_2.equals(x.expr_2);
    }
    return false;
  }

  public int hashCode() {
    return 37*(37*(this.expr_1.hashCode())+this.ident_.hashCode())+this.expr_2.hashCode();
  }


}