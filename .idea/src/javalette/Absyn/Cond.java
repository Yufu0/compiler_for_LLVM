// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public class Cond  extends Stmt {
  public final Expr expr_;
  public final Stmt stmt_;
  public int line_num, col_num, offset;
  public Cond(Expr p1, Stmt p2) { expr_ = p1; stmt_ = p2; }

  public <R,A> R accept(javalette.Absyn.Stmt.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof javalette.Absyn.Cond) {
      javalette.Absyn.Cond x = (javalette.Absyn.Cond)o;
      return this.expr_.equals(x.expr_) && this.stmt_.equals(x.stmt_);
    }
    return false;
  }

  public int hashCode() {
    return 37*(this.expr_.hashCode())+this.stmt_.hashCode();
  }


}
