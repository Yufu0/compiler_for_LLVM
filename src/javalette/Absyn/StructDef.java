// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public class StructDef  extends TopDef {
  public final String ident_;
  public final ListMember listmember_;
  public int line_num, col_num, offset;
  public StructDef(String p1, ListMember p2) { ident_ = p1; listmember_ = p2; }

  public <R,A> R accept(javalette.Absyn.TopDef.Visitor<R,A> v, A arg) { return v.visit(this, arg); }

  public boolean equals(java.lang.Object o) {
    if (this == o) return true;
    if (o instanceof javalette.Absyn.StructDef) {
      javalette.Absyn.StructDef x = (javalette.Absyn.StructDef)o;
      return this.ident_.equals(x.ident_) && this.listmember_.equals(x.listmember_);
    }
    return false;
  }

  public int hashCode() {
    return 37*(this.ident_.hashCode())+this.listmember_.hashCode();
  }


}
