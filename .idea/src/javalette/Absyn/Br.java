// File generated by the BNF Converter (bnfc 2.9.3).

package javalette.Absyn;

public abstract class Br implements java.io.Serializable {
  public abstract <R,A> R accept(Br.Visitor<R,A> v, A arg);
  public interface Visitor <R,A> {
    public R visit(javalette.Absyn.BrR p, A arg);

  }

}
