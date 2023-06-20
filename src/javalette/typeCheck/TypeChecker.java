package javalette.typeCheck;

import javalette.Absyn.*;


import java.lang.Void;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TypeChecker {
  public void typeCheck(javalette.Absyn.Prog prog) {
    Env env = new Env();
    prog.accept(new ProgVisitor(), env);
  }

  public class ProgVisitor implements javalette.Absyn.Prog.Visitor<Void, Env> {
    public Void visit(Program p, Env env) {
      // create an empty environment
      env.emptyEnv();

      p.accept(new PrimitiveFunctionVisitor(), env);

      // Defind all function in the env
      for (javalette.Absyn.TopDef x : p.listtopdef_) {
        x.accept(new TopDefVisitor1(), env);
      }
      // Check all functions
      for (javalette.Absyn.TopDef x : p.listtopdef_) {
        x.accept(new TopDefVisitor2(), env);
      }
      return null;
    }
  }


  public class PrimitiveFunctionVisitor implements javalette.Absyn.Prog.Visitor<Void, Env> {
    // Here we define all primitive function with arguments and return type
    public Void visit(Program p, Env env) {
      env.defineFun("printInt", new FunType(TypeCode.CVoid, new LinkedList<>(List.of(TypeCode.CInt))));
      env.defineFun("printDouble", new FunType(TypeCode.CVoid, new LinkedList<>(List.of(TypeCode.CDouble))));
      env.defineFun("printString", new FunType(TypeCode.CVoid, new LinkedList<>(List.of(TypeCode.CString))));
      env.defineFun("readInt", new FunType(TypeCode.CInt, new LinkedList<>()));
      env.defineFun("readDouble", new FunType(TypeCode.CDouble, new LinkedList<>()));
      return null;
    }
  }


  public class TopDefVisitor1 implements TopDef.Visitor<Void, Env> {
    public Void visit(FnDef p, Env env) {
      if (env.isFunDefined(p.ident_)) {
        throw new TypeException("function " + p.ident_ + " is duplicated on line " + p.line_num + ".");
      }
      // declare the function in environment
      env.defineFun(
              p.ident_,
              new FunType(
                      p.type_.accept(new TypeVisitor(), env),
                      p.listarg_.stream()
                              .map(arg -> ((Argument) arg).type_.accept(new TypeVisitor(), env))
                              .collect(Collectors.toCollection(LinkedList<TypeCode>::new))
              )
      );
      return null;
    }

    public Void visit(javalette.Absyn.StructDef p, Env env)
    {
      // declare the name of the struct
      if (env.isStructDeclared(p.ident_))
        throw new TypeException("structure " + p.ident_ + " is duplicated on line " + p.line_num + ".");
      env.declareStruct(p.ident_);
      return null;
    }
    public Void visit(javalette.Absyn.PtrTypeDef p, Env env)
    {
      if (env.isPtrDeclared(p.ident_2))
        throw new TypeException("pointer " + p.ident_2 + " is duplicated on line " + p.line_num + ".");
      // declare the name of the pointer
      env.declarePtr(p.ident_2, p.ident_1);
      return null;
    }
  }

  public class TopDefVisitor2 implements TopDef.Visitor<Void, Env> {
    public Void visit(FnDef p, Env env) {
      //p.ident_;
      env.newBlock();
      env.setCurrentFun(p.ident_);
      for (Arg arg : p.listarg_) {
        arg.accept(new ArgVisitor(), env);
      }
      boolean isReturned = p.blk_.accept(new BlkVisitor(), env);
      if (!env.lookupFun(env.getCurrentFun()).val.equals(TypeCode.CVoid) && !isReturned) {
        throw new TypeException("Function " + p.ident_ + " on line " + p.line_num + " does not always return.");
      }
      // add return statment
      if (env.lookupFun(env.getCurrentFun()).val == TypeCode.CVoid && !isReturned)
        ((Block) p.blk_).liststmt_.add(new VRet());

      env.unsetCurrentFun();
      env.endBlock();
      return null;
    }

    public Void visit(javalette.Absyn.StructDef p, Env env)
    {
      env.setCurrentdeclareStruct(p.ident_);
      for (javalette.Absyn.Member x: p.listmember_) {
        x.accept(new MemberVisitor(), env);
      }
      env.unsetCurrentdeclareStruct();
      return null;
    }
    public Void visit(javalette.Absyn.PtrTypeDef p, Env env)
    {
      if (!env.isStructDeclared(p.ident_1))
        throw new TypeException("Structure " + p.ident_1 + " is not defined, line " + p.line_num + ".");
      env.addStructToPtr(env.structType(p.ident_1), env.ptrType(p.ident_2));
      return null;
    }
  }

  public class MemberVisitor implements javalette.Absyn.Member.Visitor<Void,Env>
  {
    public Void visit(javalette.Absyn.StructMember p, Env env)
    {
      if (env.isMemberDefined(p.ident_))
        throw new TypeException("member " + p.ident_ + " is duplicated on line " + p.line_num + ".");

      env.addMember(p.ident_, p.type_.accept(new TypeVisitor(), env));
      return null;
    }
  }

  public class ArgVisitor implements javalette.Absyn.Arg.Visitor<TypeCode, Env> {
    public TypeCode visit(Argument p, Env env) { /* Code for argument goes here */

      TypeCode ty = p.type_.accept(new TypeVisitor(), env);
      if (ty == TypeCode.CVoid) {
        throw new TypeException("Arguments can not be of type void in function on line " + p.line_num + ".");
      }
      if (env.isVarDeclaredInLatestBlock(p.ident_))
        throw new TypeException("Argument " + p.ident_ + " is duplicate on line " + p.line_num + ".");
      // declare argument in context
      env.declareVar(p.ident_, ty);

      //p.ident_;
      return ty;
    }
  }

  public class BlkVisitor implements javalette.Absyn.Blk.Visitor<Boolean, Env> {
    public Boolean visit(Block p, Env env) { /* Code for Block goes here */
      env.newBlock();
      boolean isReturned = false;
      for (javalette.Absyn.Stmt x : p.liststmt_) {
        isReturned = isReturned || x.accept(new StmtVisitor(), env);
      }
      env.endBlock();
      return isReturned;
    }
  }

  public class StmtVisitor implements javalette.Absyn.Stmt.Visitor<Boolean, Env> {
    public Boolean visit(Empty p, Env env) { /* Code for Empty goes here */
      return false;
    }

    public Boolean visit(BStmt p, Env env) { /* Code for BStmt goes here */
      return p.blk_.accept(new BlkVisitor(), env);
    }

    public Boolean visit(Decl p, Env env) { /* Code for Decl goes here */

      // check type and disallow void
      TypeCode tp = p.type_.accept(new TypeVisitor(), env);
      if (tp == TypeCode.CVoid) {
        throw new TypeException("Variable can not be defined as void on line " + p.line_num + ".");
      }
      env.setDeclareType(tp);

      // declare Var
      for (javalette.Absyn.Item x : p.listitem_) {
        x.accept(new ItemVisitor(), env);
      }

      env.unsetDeclareType();
      return false;
    }

    public Boolean visit(javalette.Absyn.Ass p, Env env) { /* Code for Ass goes here */
      //p.ident_;
      if (!env.isVarDeclared(p.ident_)) {
        throw new TypeException("Variable " + p.ident_ + " is not declared on line " + p.line_num + ".");
      }
      if (!env.lookupVar(p.ident_).equals(p.expr_.accept(new ExprVisitor(), env))) {
        throw new TypeException("Variable " + p.ident_ + " assigned with incorrect type on line " + p.line_num + ".");
      }
      return false;
    }

    public Boolean visit(javalette.Absyn.Incr p, Env env) { /* Code for Incr goes here */
      //p.ident_;
      if (!env.isVarDeclared(p.ident_)) {
        throw new TypeException("Can not increment undeclared variable " + p.ident_ + " on line " + p.line_num + ".");
      }
      if (!env.lookupVar(p.ident_).equals(TypeCode.CInt)) {
        throw new TypeException("Can not increment variable of type  " + env.lookupVar(p.ident_).toString() + "on line " + p.line_num + ".");
      }
      return false;
    }


    public Boolean visit(javalette.Absyn.Decr p, Env env) { /* Code for Decr goes here */
      //p.ident_;
      if (!env.isVarDeclared(p.ident_)) {
        throw new TypeException("Can not decrement undeclared variable : " + p.ident_ + " on line " + p.line_num + ".");
      }
      if (!env.lookupVar(p.ident_).equals(TypeCode.CInt)) {
        throw new TypeException("Can not decrement variable of type " + env.lookupVar(p.ident_).toString() + " on line " + p.line_num + ".");
      }
      return false;
    }

    public Boolean visit(javalette.Absyn.Ret p, Env env) { /* Code for Ret goes here */
      if (!p.expr_.accept(new ExprVisitor(), env).equals(env.lookupFun(env.getCurrentFun()).val))
        throw new TypeException("Return has incorrect type on line " + p.line_num + ".");
      p.expr_.accept(new ExprVisitor(), env);
      return true;
    }

    public Boolean visit(javalette.Absyn.VRet p, Env env) { /* Code for VRet goes here */
      if (!TypeCode.CVoid.equals(env.lookupFun(env.getCurrentFun()).val))
        throw new TypeException("Return have incorrect type on line " + p.line_num + ".");
      return true;
    }

    public Boolean visit(javalette.Absyn.Cond p, Env env) { /* Code for Cond goes here */
      if (!p.expr_.accept(new ExprVisitor(), env).equals(TypeCode.CBool)) {
        throw new TypeException("Condition \"" + p.expr_ + "\"needs to be a boolean on line " + p.line_num + ".");
      }
      p.stmt_.accept(new StmtVisitor(), env);
      return false;
    }

    public Boolean visit(javalette.Absyn.CondElse p, Env env) { /* Code for CondElse goes here */
      if (!p.expr_.accept(new ExprVisitor(), env).equals(TypeCode.CBool)) {
        throw new TypeException("Condition \"" + p.expr_ + "\"needs to be a boolean on line " + p.line_num + ".");
      }
      boolean isReturned1 = p.stmt_1.accept(new StmtVisitor(), env);
      boolean isReturned2 = p.stmt_2.accept(new StmtVisitor(), env);

      return isReturned1 && isReturned2;
    }

    public Boolean visit(javalette.Absyn.While p, Env env) { /* Code for While goes here */
      if (!p.expr_.accept(new ExprVisitor(), env).equals(TypeCode.CBool)) {
        throw new TypeException("Condition \"" + p.expr_ + "\" needs to be a boolean on line " + p.line_num + ".");
      }
      p.stmt_.accept(new StmtVisitor(), env);
      return false;
    }

    public Boolean visit(javalette.Absyn.SExp p, Env env) { /* Code for SExp goes here */
      if (!p.expr_.accept(new ExprVisitor(), env).equals(TypeCode.CVoid))
        throw new TypeException("Incorrect statement on line " + p.line_num + ".");
      return false;
    }

    public Boolean visit(javalette.Absyn.ArrayAss p, Env env) { /* Code for ArrayAss goes here */

      TypeCode type = p.expr_1.accept(new ExprVisitor(), env);
      if (!(type instanceof TypeArrayOf))
        throw new TypeException("Array assignement with not an array on line " + p.line_num + ".");

      for(javalette.Absyn.Bre x : p.listbre_) {
        x.accept(new BreVisitor(), env);
        type = ((TypeArrayOf)type).typeCode;
      }


      if (!type.equals(p.expr_2.accept(new ExprVisitor(), env)))
        throw new TypeException("Array assigned with incorrect type on line " + p.line_num + ".");

      return false;

    }

    public Boolean visit(javalette.Absyn.ForEach p, Env env) { /* Code for ForEach goes here */
      TypeCode typeArray = p.expr_.accept(new ExprVisitor(), env);
      if (!(typeArray instanceof TypeArrayOf))
        throw new TypeException("Try to foreach on a non array variable on line" + p.line_num + ".");

      TypeCode typeIterator = p.type_.accept(new TypeVisitor(), env);
      if (!((TypeArrayOf) typeArray).typeCode.equals(typeIterator))
        throw new TypeException("type of iterator and array different on line " + p.line_num + ".");
      env.newBlock();
      env.declareVar(p.ident_, typeIterator);
      p.stmt_.accept(new StmtVisitor(), env);
      env.endBlock();
      return false;
    }

    public Boolean visit(javalette.Absyn.StructAss p, Env env)
    {
      TypeCode typePtr = p.expr_1.accept(new ExprVisitor(), env);
      if (!(typePtr instanceof TypePtr))
        throw new TypeException("Structure assignment with not a pointer structure on line " + p.line_num + ".");

      TypeCode typeStruct = ((TypePtr)typePtr).struct();

      if (!env.isMemberDefined(typeStruct, p.ident_))
        throw new TypeException("Member " + p.ident_ + " doesn't exist on line " + p.line_num + ".");

      TypeCode typeMember = env.getMemberType(typeStruct, p.ident_);

      if (!typeMember.equals(p.expr_2.accept(new ExprVisitor(), env)))
        throw new TypeException("Structure assigned with incorrect type on line " + p.line_num + ".");

      return false;
    }
  }

  public class ItemVisitor implements javalette.Absyn.Item.Visitor<Void, Env> {
    public Void visit(javalette.Absyn.NoInit p, Env env) { /* Code for NoInit goes here */
      //p.ident_;
      if (env.isVarDeclaredInLatestBlock(p.ident_)) {
        throw new TypeException("Variable " + p.ident_ + " on line " + p.line_num + " is already declared in this block.");
      }
      env.declareVar(p.ident_, env.getCurrentDeclareType());
      return null;
    }

    public Void visit(javalette.Absyn.Init p, Env env) { /* Code for Init goes here */
      //p.ident_;

      if (!p.expr_.accept(new ExprVisitor(), env).equals(env.getCurrentDeclareType())) {
        throw new TypeException("Variable " + p.ident_ + " on line " + p.line_num + " is initialized with an incorrect type.");
      }
      if (env.isVarDeclaredInLatestBlock(p.ident_)) {
        throw new TypeException("Variable " + p.ident_ + " on line " + p.line_num + " is already declared in this block.");
      }
      env.declareVar(p.ident_, env.getCurrentDeclareType());
      return null;
    }
  }
  public class TypeBaseVisitor implements javalette.Absyn.TypeBase.Visitor<TypeCode,Env>
  {
    public TypeCode visit(javalette.Absyn.Int p, Env env) { /* Code for Int goes here */
      return TypeCode.CInt;
    }

    public TypeCode visit(javalette.Absyn.Doub p, Env env) { /* Code for Doub goes here */
      return TypeCode.CDouble;
    }

    public TypeCode visit(javalette.Absyn.Bool p, Env env) { /* Code for Bool goes here */
      return TypeCode.CBool;
    }

    public TypeCode visit(javalette.Absyn.Void p, Env env) { /* Code for Void goes here */
      return TypeCode.CVoid;
    }
public TypeCode visit(javalette.Absyn.Ptr p, Env env)
    {
      if (!env.isPtrDeclared(p.ident_))
        throw new TypeException("Pointer " + p.ident_ + " is not defined on line " + p.line_num + ".");

      return env.ptrType(p.ident_);
    }
  }

  public class TypeVisitor implements javalette.Absyn.Type.Visitor<TypeCode, Env> {

    public TypeCode visit(javalette.Absyn.Base p, Env env) {
      return p.typebase_.accept(new TypeBaseVisitor(), env);
    }
    public TypeCode visit(javalette.Absyn.Fun p, Env env) { /* Code for Fun goes here */
      return p.type_.accept(new TypeVisitor(), env);
    }

    public TypeCode visit(javalette.Absyn.Array p, Env env)
    { /* Code for ArrayType goes here */
      return env.typeArrayOf(p.typebase_.accept(new TypeBaseVisitor(), env), p.listbr_.size());
    }
    
  }

  public class ExprVisitor implements javalette.Absyn.Expr.Visitor<TypeCode, Env> {
    public TypeCode visit(javalette.Absyn.EVar p, Env env) { /* Code for EVar goes here */
      //p.ident_;
      if (!env.isVarDeclared(p.ident_)) {
        throw new TypeException("Variable " + p.ident_ + " on line " + p.line_num + " is not declared.");
      }
      return env.lookupVar(p.ident_);
    }

    public TypeCode visit(javalette.Absyn.ELitInt p, Env env) { /* Code for ELitInt goes here */
      //p.integer_;
      return TypeCode.CInt;
    }

    public TypeCode visit(javalette.Absyn.ELitDoub p, Env env) { /* Code for ELitDoub goes here */
      //p.double_;
      return TypeCode.CDouble;
    }

    public TypeCode visit(javalette.Absyn.ELitTrue p, Env env) { /* Code for ELitTrue goes here */
      return TypeCode.CBool;
    }

    public TypeCode visit(javalette.Absyn.ELitFalse p, Env env) { /* Code for ELitFalse goes here */
      return TypeCode.CBool;
    }

    public TypeCode visit(javalette.Absyn.EApp p, Env env) { /* Code for EApp goes here */
      //p.ident_;
      // check if function is defined
      if (!env.isFunDefined(p.ident_)) {
        throw new TypeException("Function " + p.ident_ + " on line " + p.line_num + " is not defined");
      }

      // check all arguments
      List<TypeCode> typesArguments = env.lookupFun(p.ident_).args;

      // check number of arguments
      if (typesArguments.size() != p.listexpr_.size()) {
        throw new TypeException("Number of arguments does not match in function " + p.ident_ + " on line " + p.line_num + ".");
      }

      Iterator<TypeCode> typeCodeIterator = typesArguments.iterator();
      for (javalette.Absyn.Expr x : p.listexpr_) {
        if (!typeCodeIterator.next().equals(x.accept(new ExprVisitor(), env))) {
          throw new TypeException("Types of arguments do not match in function " + p.ident_ + " on line " + p.line_num + ".");
        }
      }
      // return type of the function
      return env.lookupFun(p.ident_).val;
    }

    public TypeCode visit(javalette.Absyn.EString p, Env env) { /* Code for EString goes here */
      //p.string_;
      return TypeCode.CString;
    }

    public TypeCode visit(javalette.Absyn.Neg p, Env env) { /* Code for Neg goes here */
      if (p.expr_.accept(new ExprVisitor(), env) == TypeCode.CInt) return TypeCode.CInt;
      if (p.expr_.accept(new ExprVisitor(), env) == TypeCode.CDouble) return TypeCode.CDouble;
      throw new TypeException("Opposite of non numerical value :" + p.expr_ + " on line " + p.line_num + ".");
    }

    public TypeCode visit(javalette.Absyn.Not p, Env env) { /* Code for Not goes here */
      if (p.expr_.accept(new ExprVisitor(), env) == TypeCode.CBool) return TypeCode.CBool;
      throw new TypeException("Negation of non boolean value :" + p.expr_ + " on line " + p.line_num + ".");
    }

    public TypeCode visit(javalette.Absyn.EMul p, Env env) { /* Code for EMul goes here */
      TypeCode t1 = p.expr_1.accept(new ExprVisitor(), env);
      TypeCode t2 = p.expr_2.accept(new ExprVisitor(), env);

      if (!t1.equals(t2))
        throw new TypeException("multiply 2 values with different types on line " + p.line_num + ".");

      if (!p.mulop_.accept(new MulOpVisitor(), t1))
        throw new TypeException("multiply 2 values with incorrect types on line " + p.line_num + ".");

      return t1;
    }

    public TypeCode visit(javalette.Absyn.EAdd p, Env env) { /* Code for EAdd goes here */
      TypeCode t1 = p.expr_1.accept(new ExprVisitor(), env);
      TypeCode t2 = p.expr_2.accept(new ExprVisitor(), env);

      if (!t1.equals(t2))
        throw new TypeException("add 2 values with different types on line " + p.line_num + ".");

      if (!p.addop_.accept(new AddOpVisitor(), t1))
        throw new TypeException("add 2 values with incorrect types on line " + p.line_num + ".");

      return t1;
    }

    public TypeCode visit(javalette.Absyn.ERel p, Env env) { /* Code for ERel goes here */
      TypeCode t1 = p.expr_1.accept(new ExprVisitor(), env);
      TypeCode t2 = p.expr_2.accept(new ExprVisitor(), env);


      if (!t1.equals(t2))
        throw new TypeException("compare 2 values with different types on line " + p.line_num + ".");

      if (!p.relop_.accept(new RelOpVisitor(), t1))
        throw new TypeException("compare 2 values with incorrect types on line " + p.line_num + ".");

      return TypeCode.CBool;
    }

    public TypeCode visit(javalette.Absyn.EAnd p, Env env) { /* Code for EAnd goes here */
      if (p.expr_1.accept(new ExprVisitor(), env) == TypeCode.CBool && p.expr_2.accept(new ExprVisitor(), env) == TypeCode.CBool)
        return TypeCode.CBool;
      throw new TypeException("\"And\" to non boolean values on line " + p.line_num + ".");
    }

    public TypeCode visit(javalette.Absyn.EOr p, Env env) { /* Code for EOr goes here */
      if (p.expr_1.accept(new ExprVisitor(), env) == TypeCode.CBool && p.expr_2.accept(new ExprVisitor(), env) == TypeCode.CBool)
        return TypeCode.CBool;
      throw new TypeException("\"Or\" to non boolean values on line " + p.line_num + ".");
    }

    public TypeCode visit(javalette.Absyn.EArray p, Env env) { /* Code for EArray goes here */
      TypeCode type = p.expr_.accept(new ExprVisitor(), env);
      if (!(type instanceof TypeArrayOf))
        throw new TypeException("Array expression with not an array on line " + p.line_num + ".");
	
      for(javalette.Absyn.Bre x : p.listbre_) {
          x.accept(new BreVisitor(), env);
          type = ((TypeArrayOf)type).typeCode;
      }
    return type;
    }

    public TypeCode visit(javalette.Absyn.EArrayInit p, Env env) { /* Code for ArrayInit goes here */
      TypeCode type = p.typebase_.accept(new TypeBaseVisitor(), env);
      if (type.equals(TypeCode.CVoid))
        throw new TypeException("Array of void are not permitted, line " + p.line_num + ".");
      for (javalette.Absyn.Bre x: p.listbre_) {
        x.accept(new BreVisitor(), env);
      }
      return env.typeArrayOf(type, p.listbre_.size());
    }
	public TypeCode visit(javalette.Absyn.EAttribute p, Env env) {
      if (p.ident_.equals("length")) {
        TypeCode type = p.expr_.accept(new ExprVisitor(), env);
        if (!(type instanceof TypeArrayOf))
          throw new TypeException("Can not use '.lenght' attribute with a non array expression on line " + p.line_num + ".");
        return TypeCode.CInt;
      }

      throw new TypeException("Invalid attribute " + p.ident_ + " on line " + p.line_num + ".");
    }

    public TypeCode visit(javalette.Absyn.EReference p, Env env)
    {
      TypeCode typePtr = p.expr_.accept(new ExprVisitor(), env);
      if (!(typePtr instanceof TypePtr))
        throw new TypeException("Reference on non pointer type on line " + p.line_num + ".");

      TypeCode typeStruct = ((TypePtr)typePtr).struct();

      if (!env.isMemberDefined(typeStruct, p.ident_))
        throw new TypeException("Member " + p.ident_ + " doesn't exist on line " + p.line_num + ".");

      return env.getMemberType(typeStruct, p.ident_);
    }
    public TypeCode visit(javalette.Absyn.ENullPointer p, Env env)
    {
      if (!env.isPtrDeclared(p.ident_))
        throw new TypeException("Pointer type " + p.ident_ + " doesn't exist on line " + p.line_num + ".");

      return env.ptrType(p.ident_);
    }

    public TypeCode visit(javalette.Absyn.EStructInit p, Env env)
    {
      if (!env.isStructDeclared(p.ident_))
        throw new TypeException("Create a structure with incorrect type on line " + p.line_num + ".");

      TypeCode typeStruct = env.structType(p.ident_);
      // return a pointer
      return env.structToPtr(typeStruct);
    }
  }

  public class AddOpVisitor implements javalette.Absyn.AddOp.Visitor<Boolean, TypeCode> {
    public Boolean visit(javalette.Absyn.Plus p, TypeCode ty) { /* Code for Plus goes here */
      return (ty == TypeCode.CInt || ty == TypeCode.CDouble);
    }

    public Boolean visit(javalette.Absyn.Minus p, TypeCode ty) { /* Code for Minus goes here */
      return (ty == TypeCode.CInt || ty == TypeCode.CDouble);
    }
  }

  public class MulOpVisitor implements javalette.Absyn.MulOp.Visitor<Boolean, TypeCode> {
    public Boolean visit(javalette.Absyn.Times p, TypeCode ty) { /* Code for Times goes here */
      return (ty == TypeCode.CInt || ty == TypeCode.CDouble);
    }

    public Boolean visit(javalette.Absyn.Div p, TypeCode ty) { /* Code for Div goes here */
      return (ty == TypeCode.CInt || ty == TypeCode.CDouble);
    }

    public Boolean visit(javalette.Absyn.Mod p, TypeCode ty) { /* Code for Mod goes here */
      return (ty == TypeCode.CInt);
    }
  }

  public class RelOpVisitor implements javalette.Absyn.RelOp.Visitor<Boolean, TypeCode> {
    public Boolean visit(javalette.Absyn.LTH p, TypeCode ty) { /* Code for LTH goes here */
      return (ty == TypeCode.CInt || ty == TypeCode.CDouble);
    }

    public Boolean visit(javalette.Absyn.LE p, TypeCode ty) { /* Code for LE goes here */
      return (ty == TypeCode.CInt || ty == TypeCode.CDouble);
    }

    public Boolean visit(javalette.Absyn.GTH p, TypeCode ty) { /* Code for GTH goes here */
      return (ty == TypeCode.CInt || ty == TypeCode.CDouble);
    }

    public Boolean visit(javalette.Absyn.GE p, TypeCode ty) { /* Code for GE goes here */
      return (ty == TypeCode.CInt || ty == TypeCode.CDouble);
    }

    public Boolean visit(javalette.Absyn.EQU p, TypeCode ty) {
      return (ty != TypeCode.CVoid && !(ty instanceof TypeArrayOf));
    }

    public Boolean visit(javalette.Absyn.NE p, TypeCode ty) {
      return (ty != TypeCode.CVoid && !(ty instanceof TypeArrayOf));
    }
  }

  public class BrVisitor implements javalette.Absyn.Br.Visitor<Void,Env>
  {
    public Void visit(javalette.Absyn.BrR p, Env env)
    { /* Code for BrR goes here */
      return null;
    }
  }
  public class BreVisitor implements javalette.Absyn.Bre.Visitor<Void,Env>
  {
    public Void visit(javalette.Absyn.BreR p, Env env)
    { /* Code for BreR goes here */
      if (!p.expr_.accept(new ExprVisitor(), env).equals(TypeCode.CInt))
        throw new TypeException("array index must be an integer on line " + p.line_num + ".");
      return null;
    }
  }
}
