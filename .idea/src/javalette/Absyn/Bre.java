// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public abstract class Bre implements java.io.Serializable {
  public abstract <R,A> R accept(Bre.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(javalette.Absyn.BreR p, A arg);

  }

}
