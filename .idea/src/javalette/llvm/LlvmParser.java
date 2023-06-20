package javalette.llvm;

import javalette.Absyn.Arg;
import javalette.Absyn.FnDef;
import javalette.Absyn.PtrTypeDef;
import javalette.Absyn.TopDef;
import javalette.llvm.instruction.*;


import java.lang.Void;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LlvmParser {

    public Program parse(javalette.Absyn.Prog prog) {
        LlvmEnv llvmEnv = new LlvmEnv();
        return prog.accept(new ProgVisitor(), llvmEnv);
    }
    public class ProgVisitor implements javalette.Absyn.Prog.Visitor<Program,LlvmEnv>
    {
        public Program visit(javalette.Absyn.Program p, LlvmEnv llvmEnv) {

            p.accept(new PrimitiveFunctionVisitor(), llvmEnv);

            for (javalette.Absyn.TopDef x: p.listtopdef_) {
                x.accept(new TopDefVisitor1(), llvmEnv);
            }

            for (javalette.Absyn.TopDef x: p.listtopdef_) {
                x.accept(new TopDefVisitor2(), llvmEnv);
            }

            // write typedef pointers
            llvmEnv.writeDefinedTypePtr();

            // write typedef array
            llvmEnv.writeDefinedTypeArray();

            // write typedef structure
            llvmEnv.writeDefinedTypeStruct();

            return llvmEnv.getProgram();
        }
    }

    public class PrimitiveFunctionVisitor implements javalette.Absyn.Prog.Visitor<Void, LlvmEnv> {
        // Here we define all primitive function with arguments and return type
        public Void visit(javalette.Absyn.Program p, LlvmEnv llvmEnv)  {
            llvmEnv.defineFun("printInt", "void");
            llvmEnv.defineFun("printDouble", "void");
            llvmEnv.defineFun("printString", "void");
            llvmEnv.defineFun("readInt","i32");
            llvmEnv.defineFun("readDouble", "double");

            llvmEnv.declareFun("@printInt", "void", List.of(new Var("i32", "%x")));
            llvmEnv.declareFun("@printDouble", "void", List.of(new Var("double", "%x")));
            llvmEnv.declareFun("@printString", "void", List.of(new Var("i8*", "%s")));
            llvmEnv.declareFun("@readInt", "i32", new ArrayList<>());
            llvmEnv.declareFun("@readDouble", "double", new ArrayList<>());
            llvmEnv.declareFun("@calloc", "i8*", List.of(new Var("i32", ""), new Var("i32", "")));

            return null;
        }
    }

    public class TopDefVisitor1 implements TopDef.Visitor<Void, LlvmEnv> {
        public Void visit(FnDef p, LlvmEnv llvmEnv)  {
            String type = p.type_.accept(new TypeVisitor(), llvmEnv);
            llvmEnv.defineFun(p.ident_, type);
            return null;
        }
        public Void visit(javalette.Absyn.StructDef p, LlvmEnv llvmEnv)
        { /* Code for StructDef goes here */
            llvmEnv.declareStruct(p.ident_);
            return null;
        }
        public Void visit(javalette.Absyn.PtrTypeDef p, LlvmEnv llvmEnv)
        { /* Code for PtrTypeDef goes here */
            llvmEnv.declarePtr(p.ident_2, p.ident_1);
            return null;
        }
    }

    public class TopDefVisitor2 implements TopDef.Visitor<Void, LlvmEnv> {
        public Void visit(javalette.Absyn.FnDef p, LlvmEnv llvmEnv) {
            String type = p.type_.accept(new TypeVisitor(), llvmEnv);
            llvmEnv.defineFun(p.ident_, type);
            llvmEnv.newFunction(type, "@" + p.ident_);

            llvmEnv.resetCurrentCountLabel();
            llvmEnv.resetCurrentCountVar();
            llvmEnv.resetCurrentCountTmp();

            llvmEnv.newContext();
            String label = "entry";
            llvmEnv.newBlock(label);

            for (Arg arg : p.listarg_) {
                arg.accept(new ArgVisitor(), llvmEnv);
            }

            p.blk_.accept(new BlkVisitor(), llvmEnv);
            llvmEnv.endContext();

            return null;
        }
        public Void visit(javalette.Absyn.StructDef p, LlvmEnv llvmEnv)
        { /* Code for StructDef goes here */
            llvmEnv.setCurrentdeclareStruct(p.ident_);
            for (javalette.Absyn.Member x: p.listmember_) {
                x.accept(new MemberVisitor(), llvmEnv);
            }
            llvmEnv.unsetCurrentdeclareStruct();
            return null;
        }
        public Void visit(PtrTypeDef p, LlvmEnv llvmEnv)
        { /* Code for PtrTypeDef goes here */
            llvmEnv.addStructToPtr(llvmEnv.structType(p.ident_1), llvmEnv.ptrType(p.ident_2));
            return null;
        }
    }

    public class MemberVisitor implements javalette.Absyn.Member.Visitor<Void, LlvmEnv>
    {
        public Void visit(javalette.Absyn.StructMember p, LlvmEnv llvmEnv)
        {
            llvmEnv.addMember(p.ident_, p.type_.accept(new TypeVisitor(), llvmEnv));
            return null;
        }
    }
    
    public class ArgVisitor implements javalette.Absyn.Arg.Visitor<Void,LlvmEnv>
    {
        public Void visit(javalette.Absyn.Argument p, LlvmEnv llvmEnv) {
            String type = p.type_.accept(new TypeVisitor(), llvmEnv);
            Var arg = new Var(type, "%a_" + p.ident_);
            llvmEnv.getCurrentFunction().arguments.add(arg);

            llvmEnv.incrCurrentCountVar();
            Var var = new Var(arg.type, "%v" + llvmEnv.getCurrentCountVar());

            llvmEnv.declareVar(p.ident_, var);

            // Allocate
            llvmEnv.addInstruction(new Alloca(
                    var.name,
                    var.type
            ));

            // Store
            llvmEnv.addInstruction(new Store(
                    arg.type,
                    arg.name,
                    var.type + "*",
                    var.name
            ));

            return null;
        }
    }
    public class BlkVisitor implements javalette.Absyn.Blk.Visitor<Boolean,LlvmEnv>
    {
        public Boolean visit(javalette.Absyn.Block p, LlvmEnv llvmEnv) {
            llvmEnv.newContext();
            boolean isReturned = false;
            for (javalette.Absyn.Stmt x: p.liststmt_) {
                if (!isReturned) {
                    isReturned = x.accept(new StmtVisitor(), llvmEnv);
                }
            }
            llvmEnv.endContext();

            return isReturned;
        }
    }
    public class StmtVisitor implements javalette.Absyn.Stmt.Visitor<Boolean,LlvmEnv>
    {
        public Boolean visit(javalette.Absyn.Empty p, LlvmEnv llvmEnv) { /* Code for Empty goes here */
            return false;
        }
        public Boolean visit(javalette.Absyn.BStmt p, LlvmEnv llvmEnv) {
            return p.blk_.accept(new BlkVisitor(), llvmEnv);
        }
        public Boolean visit(javalette.Absyn.Decl p, LlvmEnv llvmEnv) {
            String type = p.type_.accept(new TypeVisitor(), llvmEnv);
            llvmEnv.setDeclareType(type);

            for (javalette.Absyn.Item x: p.listitem_) {
                x.accept(new ItemVisitor(), llvmEnv);
            }

            llvmEnv.unsetDeclareType();
            return false;
        }
        public Boolean visit(javalette.Absyn.Ass p, LlvmEnv llvmEnv) {
            Var var = p.expr_.accept(new ExprVisitor(), llvmEnv);

            // Store
            llvmEnv.addInstruction(new Store(
                    var.type,
                    var.name,
                    var.type + "*",
                    llvmEnv.lookupVar(p.ident_).name
            ));
            return false;
        }
        public Boolean visit(javalette.Absyn.Incr p, LlvmEnv llvmEnv) {
            // Load
            llvmEnv.incrCurrentCountTmp();
            Var var1 = new Var(llvmEnv.lookupVar(p.ident_).type, "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Load(
                    var1.name,
                    var1.type,
                    var1.type + "*",
                    llvmEnv.lookupVar(p.ident_).name
            ));

            // Var + 1
            llvmEnv.incrCurrentCountTmp();
            Var var2 = new Var(var1.type, "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Add(
                    var2.name,
                    var2.type,
                    var1.name,
                    "1"
            ));

            // Store
            llvmEnv.addInstruction(new Store(
                    var2.type,
                    var2.name,
                    var2.type + "*",
                    llvmEnv.lookupVar(p.ident_).name
            ));

            return false;
        }
        public Boolean visit(javalette.Absyn.Decr p, LlvmEnv llvmEnv) {
            // Load
            llvmEnv.incrCurrentCountTmp();
            Var var1 = new Var(llvmEnv.lookupVar(p.ident_).type, "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Load(
                    var1.name,
                    var1.type,
                    var1.type + "*",
                    llvmEnv.lookupVar(p.ident_).name
            ));

            // Var + 1
            llvmEnv.incrCurrentCountTmp();
            Var var2 = new Var(var1.type, "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Sub(
                    var2.name,
                    var2.type,
                    var1.name,
                    "1"
            ));

            // Store
            llvmEnv.addInstruction(new Store(
                    var2.type,
                    var2.name,
                    var2.type + "*",
                    llvmEnv.lookupVar(p.ident_).name
            ));

            return false;
        }
        public Boolean visit(javalette.Absyn.Ret p, LlvmEnv llvmEnv) { /* Code for Ret goes here */
            Var var = p.expr_.accept(new ExprVisitor(), llvmEnv);

            llvmEnv.addInstruction(new Ret(
                    var.type,
                    var.name
            ));
            return true;
        }
        public Boolean visit(javalette.Absyn.VRet p, LlvmEnv llvmEnv) { /* Code for VRet goes here */
            llvmEnv.addInstruction(new Ret(
                    "void",
                    ""));
            return true;
        }
        public Boolean visit(javalette.Absyn.Cond p, LlvmEnv llvmEnv) { /* Code for Cond goes here */
            Var var = p.expr_.accept(new ExprVisitor(), llvmEnv);

            llvmEnv.incrCurrentCountLabel();
            String labelIf = "lab" + llvmEnv.getCurrentCountLabel();

            llvmEnv.incrCurrentCountLabel();
            String labelEnd = "lab" + llvmEnv.getCurrentCountLabel();

            llvmEnv.addInstruction(new BrCond(
                    var.name,
                    labelIf,
                    labelEnd
            ));

            llvmEnv.newBlock(labelIf);
            boolean isReturned = p.stmt_.accept(new StmtVisitor(), llvmEnv);
            if (!isReturned) {
                llvmEnv.addInstruction(new Br(
                        labelEnd
                ));
            }
            llvmEnv.newBlock(labelEnd);

            return false;
        }
        public Boolean visit(javalette.Absyn.CondElse p, LlvmEnv llvmEnv) {
            Var var = p.expr_.accept(new ExprVisitor(), llvmEnv);

            llvmEnv.incrCurrentCountLabel();
            String labelIf = "lab" + llvmEnv.getCurrentCountLabel();

            llvmEnv.incrCurrentCountLabel();
            String labelElse = "lab" + llvmEnv.getCurrentCountLabel();

            llvmEnv.incrCurrentCountLabel();
            String labelEnd = "lab" + llvmEnv.getCurrentCountLabel();

            llvmEnv.addInstruction(new BrCond(
                    var.name,
                    labelIf,
                    labelElse
            ));

            llvmEnv.newBlock(labelIf);
            boolean isReturned1 = p.stmt_1.accept(new StmtVisitor(), llvmEnv);
            if (!isReturned1) {
                llvmEnv.addInstruction(new Br(
                        labelEnd
                ));
            }

            llvmEnv.newBlock(labelElse);
            boolean isReturned2 = p.stmt_2.accept(new StmtVisitor(), llvmEnv);
            if (!isReturned2) {
                llvmEnv.addInstruction(new Br(
                        labelEnd
                ));
            }

            if (!isReturned1 || !isReturned2) {
                llvmEnv.newBlock(labelEnd);
            }

            return isReturned1 && isReturned2;
        }
        public Boolean visit(javalette.Absyn.While p, LlvmEnv llvmEnv) { /* Code for While goes here */
            Var var = p.expr_.accept(new ExprVisitor(), llvmEnv);

            llvmEnv.incrCurrentCountLabel();
            String labelWhile = "lab" + llvmEnv.getCurrentCountLabel();

            llvmEnv.incrCurrentCountLabel();
            String labelEnd = "lab" + llvmEnv.getCurrentCountLabel();

            llvmEnv.addInstruction(new BrCond(
                    var.name,
                    labelWhile,
                    labelEnd
            ));

            llvmEnv.newBlock(labelWhile);
            boolean isReturned = p.stmt_.accept(new StmtVisitor(), llvmEnv);
            if (!isReturned) {
                var = p.expr_.accept(new ExprVisitor(), llvmEnv);
                llvmEnv.addInstruction(new BrCond(
                        var.name,
                        labelWhile,
                        labelEnd
                ));
            }
            llvmEnv.newBlock(labelEnd);

            return false;
        }
        public Boolean visit(javalette.Absyn.SExp p, LlvmEnv llvmEnv) { /* Code for SExp goes here */
            p.expr_.accept(new ExprVisitor(), llvmEnv);
            return false;
        }

        public Boolean visit(javalette.Absyn.ArrayAss p, LlvmEnv llvmEnv) {
            Var value = p.expr_2.accept(new ExprVisitor(), llvmEnv);
            Var array = p.expr_1.accept(new ExprVisitor(), llvmEnv);

            int dim = p.listbre_.size();
			for(javalette.Absyn.Bre x : p.listbre_) {
                Var index = x.accept(new BreVisitor(), llvmEnv);
                String structType = llvmEnv.lookupElemContained(array.type);
                String elemType = llvmEnv.lookupElemContained(structType);

                llvmEnv.incrCurrentCountTmp();
                Var ptr = new Var(elemType, "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Getelementptr(
                        ptr.name,
                        structType,
                        List.of(array,
                                new Var("i32", "0"),
                                new Var("i32", "1"),
                                index)
                ));
                llvmEnv.incrCurrentCountTmp();
                array = new Var(llvmEnv.lookupElemContained(structType), "%t" + llvmEnv.getCurrentCountTmp());
                if (dim > 1) {
                    llvmEnv.addInstruction(new Load(
                            array.name,
                            array.type,
                            ptr.type+"*",
                            ptr.name
                    ));
                } else {
                    llvmEnv.addInstruction(new Store(
                            value.type,
                            value.name,
                            ptr.type+"*",
                            ptr.name
                    ));
                }
                dim--;

            }
            return false;
        }
        public Boolean visit(javalette.Absyn.ForEach p, LlvmEnv llvmEnv)
        { /* Code for ForEach goes here */
            Var array = p.expr_.accept(new ExprVisitor(), llvmEnv);
            String structType = llvmEnv.lookupElemContained(array.type);
            llvmEnv.incrCurrentCountTmp();
            Var ptr = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Getelementptr(
                    ptr.name,
                    structType,
                    List.of(array,
                            new Var("i32", "0"),
                            new Var("i32", "0"))
            ));
            llvmEnv.incrCurrentCountTmp();
            Var length = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Load(
                    length.name,
                    length.type,
                    ptr.type+"*",
                    ptr.name
            ));

            llvmEnv.incrCurrentCountVar();
            Var index = new Var("i32", "%v" + llvmEnv.getCurrentCountVar());

            llvmEnv.addInstruction(new Alloca(
                    index.name,
                    index.type
            ));
            llvmEnv.addInstruction(new Store(
                    index.type,
                    "0",
                    index.type + "*",
                    index.name
            ));


            // label for loop
            llvmEnv.incrCurrentCountLabel();
            String labelFor = "lab" + llvmEnv.getCurrentCountLabel();

            // label after loop
            llvmEnv.incrCurrentCountLabel();
            String labelEnd = "lab" + llvmEnv.getCurrentCountLabel();

            llvmEnv.addInstruction(new Br(
                    labelFor
            ));

            llvmEnv.newContext();
            llvmEnv.newBlock(labelFor);

            // Load i
            llvmEnv.incrCurrentCountTmp();
            Var index_loop = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Load(
                    index_loop.name,
                    index_loop.type,
                    index.type + "*",
                    index.name
            ));

            // define the loop element
            llvmEnv.incrCurrentCountTmp();
            Var elem = new Var(p.type_.accept(new TypeVisitor(), llvmEnv),
                    "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Getelementptr(
                    elem.name,
                    structType,
                    List.of(array,
                            new Var("i32", "0"),
                            new Var("i32", "1"),
                            index_loop)
            ));
            llvmEnv.declareVar(p.ident_, elem);

            p.stmt_.accept(new StmtVisitor(), llvmEnv);

            // i++
            llvmEnv.incrCurrentCountTmp();
            Var index_loop_2 = new Var(index_loop.type, "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Add(
                    index_loop_2.name,
                    index_loop_2.type,
                    index_loop.name,
                    "1"
            ));

            // Store i
            llvmEnv.addInstruction(new Store(
                    index_loop_2.type,
                    index_loop_2.name,
                    index.type + "*",
                    index.name
            ));


            // compare to length
            llvmEnv.incrCurrentCountTmp();
            Var cmpInLoop = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Icmp(
                    cmpInLoop.name,
                    "slt",
                    index_loop_2.type,
                    index_loop_2.name,
                    length.name
            ));
            llvmEnv.addInstruction(new BrCond(
                    cmpInLoop.name,
                    labelFor,
                    labelEnd
            ));
            llvmEnv.newBlock(labelEnd);

            llvmEnv.endContext();

            return false;
        }
        public Boolean visit(javalette.Absyn.StructAss p, LlvmEnv llvmEnv)
        {
            Var struct = p.expr_1.accept(new ExprVisitor(), llvmEnv);
            Var value = p.expr_2.accept(new ExprVisitor(), llvmEnv);


            llvmEnv.incrCurrentCountTmp();
            Var ptr = new Var(value.type, "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Getelementptr(
                    ptr.name,
                    llvmEnv.prtToStruct(struct.type),
                    List.of(struct,
                            new Var("i32", "0"),
                            new Var("i32", String.valueOf(llvmEnv.getMemberPosition(llvmEnv.prtToStruct(struct.type), p.ident_)))
                    )
            ));

            llvmEnv.addInstruction(new Store(
                    value.type,
                    value.name,
                    ptr.type+"*",
                    ptr.name
            ));
            return false;
        }
    }
    public class ItemVisitor implements javalette.Absyn.Item.Visitor<Void,LlvmEnv>
    {
        public Void visit(javalette.Absyn.NoInit p, LlvmEnv llvmEnv) {
            llvmEnv.incrCurrentCountVar();
            Var var = new Var(llvmEnv.getCurrentDeclareType(), "%v" + llvmEnv.getCurrentCountVar());
            // Allocate
            llvmEnv.addInstruction(new Alloca(
                    var.name,
                    var.type
            ));

            llvmEnv.declareVar(p.ident_, var);


            // Store
            if (llvmEnv.getCurrentDeclareType() == "i32" || llvmEnv.getCurrentDeclareType() == "i1") {
                llvmEnv.incrCurrentCountTmp();
                llvmEnv.addInstruction(new Store(
                        var.type,
                        "0",
                        var.type + "*",
                        var.name
                ));
            }

            if (llvmEnv.getCurrentDeclareType() == "double") {
                llvmEnv.incrCurrentCountTmp();
                llvmEnv.addInstruction(new Store(
                        var.type,
                        "0.0",
                        var.type + "*",
                        var.name
                ));
            }

            if (llvmEnv.getCurrentDeclareType().startsWith("%array")) {
                int dim = Integer.parseInt(llvmEnv.getCurrentDeclareType().split("_")[1]);
                String baseType = llvmEnv.getCurrentDeclareType().split("%array_" + dim + "_")[1];
                if (!baseType.equals("i32") && !baseType.equals("i1") && !baseType.equals("double")) baseType = "%" + baseType;
                Var emptyArray = new AllocateArray(baseType, dim, List.of(new Var("i32","0")), llvmEnv).allocate();
                llvmEnv.incrCurrentCountTmp();
                llvmEnv.addInstruction(new Store(
                        emptyArray.type,
                        emptyArray.name,
                        var.type+"*",
                        var.name
                ));
            }
            return null;
        }
        public Void visit(javalette.Absyn.Init p, LlvmEnv llvmEnv) { /* Code for Init goes here */
            llvmEnv.incrCurrentCountVar();
            Var var = new Var(llvmEnv.getCurrentDeclareType(), "%v" + llvmEnv.getCurrentCountVar());
            Var value = p.expr_.accept(new ExprVisitor(), llvmEnv);

            // Allocate
            llvmEnv.addInstruction(new Alloca(
                    var.name,
                    var.type
            ));

            llvmEnv.declareVar(p.ident_, var);

            // Store
            llvmEnv.incrCurrentCountTmp();
            llvmEnv.addInstruction(new Store(
                    value.type,
                    value.name,
                    var.type + "*",
                    var.name
            ));
            return null;
        }
    }

    public class TypeBaseVisitor implements javalette.Absyn.TypeBase.Visitor<String,LlvmEnv>
    {
        public String visit(javalette.Absyn.Int p, LlvmEnv llvmEnv) {
            return "i32";
        }
        public String visit(javalette.Absyn.Doub p, LlvmEnv llvmEnv) {
            return "double";
        }
        public String visit(javalette.Absyn.Bool p, LlvmEnv llvmEnv) {
            return "i1";
        }
        public String visit(javalette.Absyn.Void p, LlvmEnv llvmEnv) {
            return "void";
        }
public String visit(javalette.Absyn.Ptr p, LlvmEnv llvmEnv)
        {
            return llvmEnv.ptrType(p.ident_);
        }

    }

    public class TypeVisitor implements javalette.Absyn.Type.Visitor<String,LlvmEnv>
    {
        public String visit(javalette.Absyn.Base p,LlvmEnv llvmEnv)
        { /* Code for Base goes here */
            return p.typebase_.accept(new TypeBaseVisitor(), llvmEnv);
        }
        public String visit(javalette.Absyn.Fun p, LlvmEnv llvmEnv) {
            return p.type_.accept(new TypeVisitor(), llvmEnv);
        }
        public String visit(javalette.Absyn.Array p, LlvmEnv llvmEnv)
        { /* Code for Array goes here */
            String type = p.typebase_.accept(new TypeBaseVisitor(), llvmEnv);
            int dim = p.listbr_.size();
            llvmEnv.defineTypeArray(type, dim);
            if (type.startsWith("%")) type = type.split("%")[1];
            return "%array_" + dim + "_" + type;
        }
        
    }
    public class ExprVisitor implements javalette.Absyn.Expr.Visitor<Var,LlvmEnv>
    {
        public Var visit(javalette.Absyn.EVar p, LlvmEnv llvmEnv) {
            Var var = llvmEnv.lookupVar(p.ident_);
            llvmEnv.incrCurrentCountTmp();
            Var value = new Var(var.type, "%t" + llvmEnv.getCurrentCountTmp());

            llvmEnv.addInstruction(new Load(
                    value.name,
                    value.type,
                    var.type + "*",
                    var.name
            ));
            return value;
        }
        public Var visit(javalette.Absyn.ELitInt p, LlvmEnv llvmEnv) {
            return new Var("i32", p.integer_.toString());
        }
        public Var visit(javalette.Absyn.ELitDoub p, LlvmEnv llvmEnv) {
            return new Var("double", p.double_.toString());
        }
        public Var visit(javalette.Absyn.ELitTrue p, LlvmEnv llvmEnv) { /* Code for ELitTrue goes here */
            return new Var("i1", "1");
        }
        public Var visit(javalette.Absyn.ELitFalse p, LlvmEnv llvmEnv) { /* Code for ELitFalse goes here */
            return new Var("i1", "0");
        }
        public Var visit(javalette.Absyn.EApp p, LlvmEnv llvmEnv) {
            List<Var> args = new ArrayList<>();
            for (javalette.Absyn.Expr x: p.listexpr_) {
                args.add(x.accept(new ExprVisitor(), llvmEnv));
            }

            Var fun = llvmEnv.lookupFun(p.ident_);
            Var var;

            if (fun.type == "void") {
                var = new Var(fun.type, "");;
                llvmEnv.addInstruction(new VCall(
                        var.type,
                        fun.name,
                        args
                ));
            } else {
                llvmEnv.incrCurrentCountTmp();
                var = new Var(fun.type, "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Call(
                        var.name,
                        var.type,
                        fun.name,
                        args
                ));
            }
            return var;
        }
        public Var visit(javalette.Absyn.EString p, LlvmEnv llvmEnv) {
            Var str = llvmEnv.declareString(p.string_);

            llvmEnv.incrCurrentCountTmp();
            Var var = new Var("i8*", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Getelementptr(
                    var.name,
                    str.type,
                    List.of(new Var(str.type+"*", str.name), new Var("i32", "0"), new Var("i32", "0"))
            ));
            return var;
        }
        public Var visit(javalette.Absyn.Neg p, LlvmEnv llvmEnv) {
            Var var = p.expr_.accept(new ExprVisitor(), llvmEnv);
            Var result = null;

            llvmEnv.incrCurrentCountTmp();
            if (var.type == "i32") {
                result = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Sub(
                        result.name,
                        result.type,
                        "0",
                        var.name
                ));
            }

            if (var.type == "double") {
                result = new Var("double", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fneg(
                        result.name,
                        result.type,
                        var.name
                ));
            }
            return result;
        }
        public Var visit(javalette.Absyn.Not p, LlvmEnv llvmEnv) {
            Var var = p.expr_.accept(new ExprVisitor(), llvmEnv);
            Var result = null;

            llvmEnv.incrCurrentCountTmp();
            if (var.type == "i1") {
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Add(
                        result.name,
                        result.type,
                        "1",
                        var.name
                ));
            }

            return result;
        }
        public Var visit(javalette.Absyn.EMul p, LlvmEnv llvmEnv) {
            Var var1 = p.expr_1.accept(new ExprVisitor(), llvmEnv);
            Var var2 = p.expr_2.accept(new ExprVisitor(), llvmEnv);

            llvmEnv.setOp1(var1);
            llvmEnv.setOp2(var2);
            Var result =  p.mulop_.accept(new MulOpVisitor(), llvmEnv);
            llvmEnv.unsetOp();

            return result;
        }
        public Var visit(javalette.Absyn.EAdd p, LlvmEnv llvmEnv) {
            Var var1 = p.expr_1.accept(new ExprVisitor(), llvmEnv);
            Var var2 = p.expr_2.accept(new ExprVisitor(), llvmEnv);

            llvmEnv.setOp1(var1);
            llvmEnv.setOp2(var2);
            Var result = p.addop_.accept(new AddOpVisitor(), llvmEnv);
            llvmEnv.unsetOp();

            return result;
        }
        public Var visit(javalette.Absyn.ERel p, LlvmEnv llvmEnv) {
            Var var1 = p.expr_1.accept(new ExprVisitor(), llvmEnv);
            Var var2 = p.expr_2.accept(new ExprVisitor(), llvmEnv);

            llvmEnv.setOp1(var1);
            llvmEnv.setOp2(var2);
            Var result = p.relop_.accept(new RelOpVisitor(), llvmEnv);
            llvmEnv.unsetOp();

            return result;
        }
        public Var visit(javalette.Absyn.EAnd p, LlvmEnv llvmEnv) {
            llvmEnv.incrCurrentCountLabel();
            String label1 = "lab" + llvmEnv.getCurrentCountLabel();
            llvmEnv.incrCurrentCountLabel();
            String label2 = "lab" + llvmEnv.getCurrentCountLabel();
            Var var1 = p.expr_1.accept(new ExprVisitor(), llvmEnv);
            llvmEnv.incrCurrentCountTmp();
            Var result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Alloca(
                    result.name,
                    result.type
            ));
            llvmEnv.addInstruction(new Store(
                    var1.type,
                    var1.name,
                    result.type + "*",
                    result.name
            ));
            llvmEnv.addInstruction(new BrCond(
                    var1.name,
                    label1,
                    label2
            ));

            llvmEnv.newBlock(label1);
            Var var2 = p.expr_2.accept(new ExprVisitor(), llvmEnv);
            llvmEnv.addInstruction(new Store(
                    var2.type,
                    var2.name,
                    result.type + "*",
                    result.name
            ));
            llvmEnv.addInstruction(new Br(
                    label2
            ));
            llvmEnv.newBlock(label2);
            llvmEnv.incrCurrentCountTmp();
            Var resultval = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Load(
                    resultval.name,
                    resultval.type,
                    result.type + "*", 
                    result.name
            ));
            return resultval;
        }
        public Var visit(javalette.Absyn.EOr p, LlvmEnv llvmEnv) {
            llvmEnv.incrCurrentCountLabel();
            String label1 = "lab" + llvmEnv.getCurrentCountLabel();
            llvmEnv.incrCurrentCountLabel();
            String label2 = "lab" + llvmEnv.getCurrentCountLabel();
            Var var1 = p.expr_1.accept(new ExprVisitor(), llvmEnv);
            llvmEnv.incrCurrentCountTmp();
            Var result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Alloca(
                    result.name,
                    result.type
            ));
            llvmEnv.addInstruction(new Store(
                    var1.type,
                    var1.name,
                    result.type + "*",
                    result.name
            ));
            llvmEnv.addInstruction(new BrCond(
                    var1.name,
                    label2,
                    label1
            ));

            llvmEnv.newBlock(label1);
            Var var2 = p.expr_2.accept(new ExprVisitor(), llvmEnv);
            llvmEnv.addInstruction(new Store(
                    var2.type,
                    var2.name,
                    result.type + "*",
                    result.name
            ));
            llvmEnv.addInstruction(new Br(
                    label2
            ));
            llvmEnv.newBlock(label2);
            llvmEnv.incrCurrentCountTmp();
            Var resultval = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Load(
                    resultval.name,
                    resultval.type,
                    result.type + "*", 
                    result.name
            ));
            return resultval;
        }
        public Var visit(javalette.Absyn.EArray p, LlvmEnv llvmEnv) { /* Code for EArray goes here */
            Var array = p.expr_.accept(new ExprVisitor(), llvmEnv);

            for (javalette.Absyn.Bre x : p.listbre_) {
                Var index = x.accept(new BreVisitor(), llvmEnv);
                String structType = llvmEnv.lookupElemContained(array.type);
                String elemType = llvmEnv.lookupElemContained(structType);

                llvmEnv.incrCurrentCountTmp();
                Var ptr = new Var(elemType, "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Getelementptr(
                        ptr.name,
                        structType,
                        List.of(array,
                                new Var("i32", "0"),
                                new Var("i32", "1"),
                                index)
                ));
                llvmEnv.incrCurrentCountTmp();
                array = new Var(elemType, "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Load(
                        array.name,
                        array.type,
                        ptr.type + "*",
                        ptr.name
                ));
            }
            return array;
        }

        public Var visit(javalette.Absyn.EArrayInit p, LlvmEnv llvmEnv)
        { /* Code for EArrayInit goes here */
            String baseType = p.typebase_.accept(new TypeBaseVisitor(), llvmEnv);
            int dim = p.listbre_.size();
            llvmEnv.defineTypeArray(baseType, dim);
            List<Var> listExprSize = p.listbre_.stream().map(bre -> bre.accept(new BreVisitor(), llvmEnv)).collect(Collectors.toList());

            AllocateArray a = new AllocateArray(baseType, dim, listExprSize, llvmEnv);

            return a.allocate();
        }

        public Var visit(javalette.Absyn.EAttribute p, LlvmEnv llvmEnv)
        { /* Code for ArrayLength goes here */
            if (p.ident_.equals("length")) {
                Var array = p.expr_.accept(new ExprVisitor(), llvmEnv);
                String structType = llvmEnv.lookupElemContained(array.type);
                llvmEnv.incrCurrentCountTmp();
                Var ptr = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Getelementptr(
                        ptr.name,
                        structType,
                        List.of(array,
                                new Var("i32", "0"),
                                new Var("i32", "0"))
                ));
                llvmEnv.incrCurrentCountTmp();
                Var value = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Load(
                        value.name,
                        value.type,
                        ptr.type + "*",
                        ptr.name
                ));
                return value;
            }
            return null;
        }
        public Var visit(javalette.Absyn.EReference p, LlvmEnv llvmEnv)
        { /* Code for EReference goes here */
            Var struct = p.expr_.accept(new ExprVisitor(), llvmEnv);
            String type = llvmEnv.getMemberType(llvmEnv.prtToStruct(struct.type), p.ident_);

            llvmEnv.incrCurrentCountTmp();
            Var ptr = new Var(type, "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Getelementptr(
                    ptr.name,
                    llvmEnv.prtToStruct(struct.type),
                    List.of(struct,
                            new Var("i32", "0"),
                            new Var("i32", String.valueOf(llvmEnv.getMemberPosition(llvmEnv.prtToStruct(struct.type), p.ident_)))
                    )
            ));

            llvmEnv.incrCurrentCountTmp();
            Var value = new Var(type, "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Load(
                    value.name,
                    value.type,
                    ptr.type+"*",
                    ptr.name
            ));
            return value;
        }
        public Var visit(javalette.Absyn.ENullPointer p, LlvmEnv llvmEnv)
        {
            // create a null pointer
            llvmEnv.incrCurrentCountTmp();
            String ptrType = llvmEnv.ptrType(p.ident_);
            return new Var(ptrType, "null");
        }

        public Var visit(javalette.Absyn.EStructInit p, LlvmEnv llvmEnv)
        {
            String structType = llvmEnv.structType(p.ident_);
            String ptrType = llvmEnv.structToPtr(structType);

            // calculate the size
            llvmEnv.incrCurrentCountTmp();
            Var sizeTmp = new Var(structType, "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Getelementptr(
                    sizeTmp.name,
                    structType,
                    List.of(new Var(structType + "*", "null"),
                            new Var("i32", "1")
                    )
            ));

            llvmEnv.incrCurrentCountTmp();
            Var size = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Ptrtoint(
                    size.name,
                    sizeTmp.type+"*",
                    sizeTmp.name,
                    size.type
            ));

            // allocate a struct
            llvmEnv.incrCurrentCountTmp();
            Var structPtrTmp = new Var("i8", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Calloc(
                    structPtrTmp.name,
                    "1",
                    size.name
            ));

            // convert to struct type
            llvmEnv.incrCurrentCountTmp();
            Var structPtr = new Var(ptrType, "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Bitcast(
                    structPtr.name,
                    structPtrTmp.type + "*",
                    structPtrTmp.name,
                    structPtr.type
            ));

            for (String member : llvmEnv.getMembers(structType)) {
                String memberType = llvmEnv.getMemberType(structType, member);
                // init member
                llvmEnv.incrCurrentCountTmp();
                Var ptr = new Var(memberType, "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Getelementptr(
                        ptr.name,
                        structType,
                        List.of(structPtr,
                                new Var("i32", "0"),
                                new Var("i32", String.valueOf(llvmEnv.getMemberPosition(structType, member)))
                        )
                ));

                // type of member
                if (memberType == "i32" || memberType == "i1") {
                    llvmEnv.addInstruction(new Store(
                            ptr.type,
                            "0",
                            ptr.type + "*",
                            ptr.name
                    ));
                }

                if (memberType == "double") {
                    llvmEnv.addInstruction(new Store(
                            ptr.type,
                            "0.0",
                            ptr.type + "*",
                            ptr.name
                    ));
                }

                if (memberType.startsWith("%array")) {
                    int dim = Integer.parseInt(memberType.split("_")[1]);
                    String baseType = memberType.split("%array_" + dim + "_")[1];
                    if (!baseType.equals("i32") && !baseType.equals("i1") && !baseType.equals("double")) baseType = "%" + baseType;
                    Var emptyArray = new AllocateArray(baseType, dim, List.of(new Var("i32", "0")), llvmEnv).allocate();
                    llvmEnv.addInstruction(new Store(
                            emptyArray.type,
                            emptyArray.name,
                            ptr.type + "*",
                            ptr.name
                    ));
                }

                if (llvmEnv.isDeclaredPtr(memberType)) {
                    llvmEnv.addInstruction(new Store(
                            ptr.type,
                            "null",
                            ptr.type + "*",
                            ptr.name
                    ));
                }
            }
            return structPtr;
        }
    }
    public class AddOpVisitor implements javalette.Absyn.AddOp.Visitor<Var,LlvmEnv>
    {
        public Var visit(javalette.Absyn.Plus p, LlvmEnv llvmEnv) {
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            llvmEnv.incrCurrentCountTmp();

            if (var1.type == "i32") {
                result = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Add(
                        result.name,
                        result.type,
                        var1.name,
                        var2.name
                ));
            }

            if (var1.type == "double") {
                result = new Var("double", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fadd(
                        result.name,
                        result.type,
                        var1.name,
                        var2.name
                ));
            }

            return result;
        }
        public Var visit(javalette.Absyn.Minus p, LlvmEnv llvmEnv) { /* Code for Minus goes here */
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            llvmEnv.incrCurrentCountTmp();

            if (var1.type == "i32") {
                result = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Sub(
                        result.name,
                        result.type,
                        var1.name,
                        var2.name
                ));
            }

            if (var1.type == "double") {
                result = new Var("double", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fsub(
                        result.name,
                        result.type,
                        var1.name,
                        var2.name
                ));
            }

            return result;
        }
    }
    public class MulOpVisitor implements javalette.Absyn.MulOp.Visitor<Var,LlvmEnv>
    {
        public Var visit(javalette.Absyn.Times p, LlvmEnv llvmEnv) {
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            llvmEnv.incrCurrentCountTmp();

            if (var1.type == "i32") {
                result = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Mul(
                        result.name,
                        result.type,
                        var1.name,
                        var2.name
                ));
            }

            if (var1.type == "double") {
                result = new Var("double", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fmul(
                        result.name,
                        result.type,
                        var1.name,
                        var2.name
                ));
            }

            return result;
        }
        public Var visit(javalette.Absyn.Div p, LlvmEnv llvmEnv) {
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            llvmEnv.incrCurrentCountTmp();

            if (var1.type == "i32") {
                result = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Sdiv(
                        result.name,
                        result.type,
                        var1.name,
                        var2.name
                ));
            }

            if (var1.type == "double") {
                result = new Var("double", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fdiv(
                        result.name,
                        result.type,
                        var1.name,
                        var2.name
                ));
            }

            return result;
        }
        public Var visit(javalette.Absyn.Mod p, LlvmEnv llvmEnv) {
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            llvmEnv.incrCurrentCountTmp();

            if (var1.type == "i32") {
                result = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Srem(
                        result.name,
                        result.type,
                        var1.name,
                        var2.name
                ));
            }
            return result;
        }
    }
    public class RelOpVisitor implements javalette.Absyn.RelOp.Visitor<Var,LlvmEnv>
    {
        public Var visit(javalette.Absyn.LTH p, LlvmEnv llvmEnv) {
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            llvmEnv.incrCurrentCountTmp();

            if (var1.type == "i32") {
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Icmp(
                        result.name,
                        "slt",
                        "i32",
                        var1.name,
                        var2.name
                ));
            }

            if (var1.type == "double") {
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fcmp(
                        result.name,
                        "olt",
                        "double",
                        var1.name,
                        var2.name
                ));
            }

            return result;
        }
        public Var visit(javalette.Absyn.LE p, LlvmEnv llvmEnv) {
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            llvmEnv.incrCurrentCountTmp();

            if (var1.type == "i32") {
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Icmp(
                        result.name,
                        "sle",
                        "i32",
                        var1.name,
                        var2.name
                ));
            }

            if (var1.type == "double") {
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fcmp(
                        result.name,
                        "ole",
                        "double",
                        var1.name,
                        var2.name
                ));
            }

            return result;
        }
        public Var visit(javalette.Absyn.GTH p, LlvmEnv llvmEnv) {
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            llvmEnv.incrCurrentCountTmp();

            if (var1.type == "i32") {
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Icmp(
                        result.name,
                        "sgt",
                        "i32",
                        var1.name,
                        var2.name
                ));
            }

            if (var1.type == "double") {
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fcmp(
                        result.name,
                        "ogt",
                        "double",
                        var1.name,
                        var2.name
                ));
            }

            return result;
        }
        public Var visit(javalette.Absyn.GE p, LlvmEnv llvmEnv) {
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            llvmEnv.incrCurrentCountTmp();

            if (var1.type == "i32") {
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Icmp(
                        result.name,
                        "sge",
                        var1.type,
                        var1.name,
                        var2.name
                ));
            }

            if (var1.type == "double") {
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fcmp(
                        result.name,
                        "oge",
                        var1.type,
                        var1.name,
                        var2.name
                ));
            }

            return result;
        }
        public Var visit(javalette.Absyn.EQU p, LlvmEnv llvmEnv) {
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            if (var1.type == "i32" || var1.type == "i1") {
                llvmEnv.incrCurrentCountTmp();
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Icmp(
                        result.name,
                        "eq",
                        var1.type,
                        var1.name,
                        var2.name
                ));
            }

            if (var1.type == "double") {
                llvmEnv.incrCurrentCountTmp();
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fcmp(
                        result.name,
                        "oeq",
                        var1.type,
                        var1.name,
                        var2.name
                ));
            }

            if (llvmEnv.isDeclaredPtr(var1.type)) {
                llvmEnv.incrCurrentCountTmp();
                Var var1Int = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Ptrtoint(
                        var1Int.name,
                        var1.type,
                        var1.name,
                        var1Int.type
                ));
                llvmEnv.incrCurrentCountTmp();
                Var var2Int = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Ptrtoint(
                        var2Int.name,
                        var2.type,
                        var2.name,
                        var2Int.type
                ));
                llvmEnv.incrCurrentCountTmp();
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Icmp(
                        result.name,
                        "eq",
                        var1Int.type,
                        var1Int.name,
                        var2Int.name
                ));
            }

            return result;
        }
        public Var visit(javalette.Absyn.NE p, LlvmEnv llvmEnv) {
            Var var1 = llvmEnv.getOp1();
            Var var2 = llvmEnv.getOp2();
            Var result = null;

            if (var1.type == "i32" || var1.type == "i1") {
                llvmEnv.incrCurrentCountTmp();
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Icmp(
                        result.name,
                        "ne",
                        var1.type,
                        var1.name,
                        var2.name
                ));
            }

            if (var1.type == "double") {
                llvmEnv.incrCurrentCountTmp();
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Fcmp(
                        result.name,
                        "one",
                        var1.type,
                        var1.name,
                        var2.name
                ));
            }

            if (llvmEnv.isDeclaredPtr(var1.type)) {
                llvmEnv.incrCurrentCountTmp();
                Var var1Int = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Ptrtoint(
                        var1Int.name,
                        var1.type,
                        var1.name,
                        var1Int.type
                ));
                llvmEnv.incrCurrentCountTmp();
                Var var2Int = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Ptrtoint(
                        var2Int.name,
                        var2.type,
                        var2.name,
                        var2Int.type
                ));
                llvmEnv.incrCurrentCountTmp();
                result = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Icmp(
                        result.name,
                        "ne",
                        var1Int.type,
                        var1Int.name,
                        var2Int.name
                ));
            }

            return result;
        }
    }


    public class BrVisitor implements javalette.Absyn.Br.Visitor<Void,LlvmEnv>
    {
        public Void visit(javalette.Absyn.BrR p, LlvmEnv llvmEnv)
        { /* Code for BrR goes here */
            return null;
        }
    }
    public class BreVisitor implements javalette.Absyn.Bre.Visitor<Var,LlvmEnv> {
        public Var visit(javalette.Absyn.BreR p, LlvmEnv llvmEnv) { /* Code for BreR goes here */
            return p.expr_.accept(new ExprVisitor(), llvmEnv);
        }
    }
}
