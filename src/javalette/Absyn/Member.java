// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public abstract class Member implements java.io.Serializable {
  public abstract <R,A> R accept(Member.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(javalette.Absyn.StructMember p, A arg);

  }

}