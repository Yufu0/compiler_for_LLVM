// File generated by the BNF Converter (bnfc 2.9.3).

package javalette;

/** All Visitor */

public interface AllVisitor<R,A> extends
  javalette.Absyn.Prog.Visitor<R,A>,
  javalette.Absyn.TopDef.Visitor<R,A>,
  javalette.Absyn.Arg.Visitor<R,A>,
  javalette.Absyn.Member.Visitor<R,A>,
  javalette.Absyn.Blk.Visitor<R,A>,
  javalette.Absyn.Stmt.Visitor<R,A>,
  javalette.Absyn.Item.Visitor<R,A>,
  javalette.Absyn.TypeBase.Visitor<R,A>,
  javalette.Absyn.Type.Visitor<R,A>,
  javalette.Absyn.Expr.Visitor<R,A>,
  javalette.Absyn.AddOp.Visitor<R,A>,
  javalette.Absyn.MulOp.Visitor<R,A>,
  javalette.Absyn.RelOp.Visitor<R,A>,
  javalette.Absyn.Br.Visitor<R,A>,
  javalette.Absyn.Bre.Visitor<R,A>
{}
