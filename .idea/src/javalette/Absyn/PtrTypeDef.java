// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public class PtrTypeDef  extends TopDef {
  public final String ident_1, ident_2;
  public int line_num, col_num, offset;
  public PtrTypeDef(String p1, String p2) { ident_1 = p1; ident_2 = p2; }

  public <R,A> R accept(javalette.Absyn.TopDef.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof javalette.Absyn.PtrTypeDef) {
      javalette.Absyn.PtrTypeDef x = (javalette.Absyn.PtrTypeDef)o;
      return this.ident_1.equals(x.ident_1) && this.ident_2.equals(x.ident_2);
    }
    return false;
  }

  public int hashCode() {
    return 37*(this.ident_1.hashCode())+this.ident_2.hashCode();
  }


}
