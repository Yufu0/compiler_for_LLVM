package javalette.llvm;

import javalette.llvm.instruction.*;

import java.util.Iterator;
import java.util.List;

public class AllocateArray {
    String baseType;
    int dim;
    Iterator<Var> itSizes;
    LlvmEnv llvmEnv;

    public AllocateArray(String baseType, int dim, List<Var> listSizes, LlvmEnv llvmEnv) {
        this.baseType = baseType;
        this.dim = dim;
        this.itSizes = listSizes.iterator();
        this.llvmEnv = llvmEnv;
    }

    public Var allocate() {
        Var size = itSizes.next();

        // branch to another block
        llvmEnv.incrCurrentCountLabel();
        String label = "lab" + llvmEnv.getCurrentCountLabel();
        llvmEnv.addInstruction(new Br(
                label
        ));
        llvmEnv.newBlock(label);


        // calculate the size
        llvmEnv.incrCurrentCountTmp();
        Var sizeElemTmp = new Var(llvmEnv.getTypeArrayPtr(baseType, dim-1), "%t" + llvmEnv.getCurrentCountTmp());
        llvmEnv.addInstruction(new Getelementptr(
                sizeElemTmp.name,
                llvmEnv.getTypeArrayStruct(baseType, dim),
                List.of(new Var(llvmEnv.getTypeArrayStruct(baseType, dim) + "*", "null"),
                        new Var("i32", "0"),
                        new Var("i32", "1"),
                        new Var("i32", size.name))
        ));
        llvmEnv.incrCurrentCountTmp();
        Var sizeElem = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
        llvmEnv.addInstruction(new Ptrtoint(
                sizeElem.name,
                sizeElemTmp.type+"*",
                sizeElemTmp.name,
                sizeElem.type
        ));


        // allocate a struct array
        llvmEnv.incrCurrentCountTmp();
        Var arrayPtrTmp = new Var("i8", "%t" + llvmEnv.getCurrentCountTmp());
        llvmEnv.addInstruction(new Calloc(
                arrayPtrTmp.name,
                "1",
                sizeElem.name
        ));

        // convert to array type
        llvmEnv.incrCurrentCountTmp();
        Var arrayPtr = new Var(llvmEnv.getTypeArrayPtr(baseType, dim), "%t" + llvmEnv.getCurrentCountTmp());
        llvmEnv.addInstruction(new Bitcast(
                arrayPtr.name,
                arrayPtrTmp.type + "*",
                arrayPtrTmp.name,
                arrayPtr.type
        ));

        // store size to struct
        llvmEnv.incrCurrentCountTmp();
        Var lengthPtr = new Var("i32","%t" + llvmEnv.getCurrentCountTmp());
        llvmEnv.addInstruction(new Getelementptr(
                lengthPtr.name,
                llvmEnv.getTypeArrayStruct(baseType, dim),
                List.of(arrayPtr,
                        new Var("i32", "0"),
                        new Var("i32", "0"))
        ));
        llvmEnv.addInstruction(new Store(
                size.type,
                size.name,
                lengthPtr.type + "*",
                lengthPtr.name
        ));


        Var arrayPtrFinal = arrayPtr;

        if (itSizes.hasNext()) {

            // label for loop
            llvmEnv.incrCurrentCountLabel();
            String labelBegin = "lab" + llvmEnv.getCurrentCountLabel();

            llvmEnv.incrCurrentCountLabel();
            String labelFor = "lab" + llvmEnv.getCurrentCountLabel();

            // label after loop
            llvmEnv.incrCurrentCountLabel();
            String labelEndFinal = "lab" + llvmEnv.getCurrentCountLabel();
            String labelEnd = labelEndFinal;

            // index on the array
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

            Var subArray = arrayPtr;


            // compare to size
            llvmEnv.incrCurrentCountTmp();
            Var cmp = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
            llvmEnv.addInstruction(new Icmp(
                    cmp.name,
                    "slt",
                    size.type,
                    "0",
                    size.name
            ));

            llvmEnv.addInstruction(new BrCond(
                    cmp.name,
                    labelBegin,
                    labelEnd
            ));

            while (itSizes.hasNext()) {
                // pre loop
                llvmEnv.newBlock(labelBegin);

                // Load i
                llvmEnv.incrCurrentCountTmp();
                Var index_loop = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Load(
                        index_loop.name,
                        index_loop.type,
                        index.type + "*",
                        index.name
                ));

                // compare to size
                llvmEnv.incrCurrentCountTmp();
                cmp = new Var("i1", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Icmp(
                        cmp.name,
                        "slt",
                        size.type,
                        index_loop.name,
                        size.name
                ));

                llvmEnv.addInstruction(new BrCond(
                        cmp.name,
                        labelFor,
                        labelEnd
                ));


                // loop allocation
                llvmEnv.newBlock(labelFor);



                // define the loop element
                llvmEnv.incrCurrentCountTmp();
                Var elem = new Var(llvmEnv.getTypeArrayPtr(baseType, dim-1),"%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Getelementptr(
                        elem.name,
                        llvmEnv.getTypeArrayStruct(baseType, dim),
                        List.of(subArray,
                                new Var("i32", "0"),
                                new Var("i32", "1"),
                                index_loop)
                ));

                dim--;
                size = itSizes.next();



                // calculate the size of 1 elem
                llvmEnv.incrCurrentCountTmp();
                sizeElemTmp = new Var(llvmEnv.getTypeArrayPtr(baseType, dim-1), "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Getelementptr(
                        sizeElemTmp.name,
                        llvmEnv.getTypeArrayStruct(baseType, dim),
                        List.of(new Var(llvmEnv.getTypeArrayStruct(baseType, dim) + "*", "null"),
                                new Var("i32", "0"),
                                new Var("i32", "1"),
                                new Var("i32", size.name))
                ));
                llvmEnv.incrCurrentCountTmp();
                sizeElem = new Var("i32", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Ptrtoint(
                        sizeElem.name,
                        sizeElemTmp.type+"*",
                        sizeElemTmp.name,
                        sizeElem.type
                ));

                // allocate a struct array
                llvmEnv.incrCurrentCountTmp();
                arrayPtrTmp = new Var("i8", "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Calloc(
                        arrayPtrTmp.name,
                        "1",
                        sizeElem.name
                ));

                // convert to array type
                llvmEnv.incrCurrentCountTmp();
                arrayPtr = new Var(llvmEnv.getTypeArrayPtr(baseType, dim), "%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Bitcast(
                        arrayPtr.name,
                        arrayPtrTmp.type + "*",
                        arrayPtrTmp.name,
                        arrayPtr.type
                ));

                // store size to struct
                llvmEnv.incrCurrentCountTmp();
                lengthPtr = new Var("i32","%t" + llvmEnv.getCurrentCountTmp());
                llvmEnv.addInstruction(new Getelementptr(
                        lengthPtr.name,
                        llvmEnv.getTypeArrayStruct(baseType, dim),
                        List.of(arrayPtr,
                                new Var("i32", "0"),
                                new Var("i32", "0"))
                ));
                llvmEnv.addInstruction(new Store(
                        size.type,
                        size.name,
                        lengthPtr.type + "*",
                        lengthPtr.name
                ));

                // store to elem
                llvmEnv.addInstruction(new Store(
                        arrayPtr.type,
                        arrayPtr.name,
                        elem.type+"*",
                        elem.name
                ));

                subArray = arrayPtr;

                // label after loop
                labelEnd = labelBegin;

                // label for loop
                llvmEnv.incrCurrentCountLabel();
                labelBegin = "lab" + llvmEnv.getCurrentCountLabel();

                llvmEnv.incrCurrentCountLabel();
                labelFor = "lab" + llvmEnv.getCurrentCountLabel();

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

                if (itSizes.hasNext()) {
                    // index on the array
                    llvmEnv.incrCurrentCountVar();
                    index = new Var("i32", "%v" + llvmEnv.getCurrentCountVar());
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

                    llvmEnv.addInstruction(new Br(
                            labelBegin
                    ));
                } else {
                    llvmEnv.addInstruction(new Br(
                            labelEnd
                    ));
                }
            }

            // end all loop
            llvmEnv.newBlock(labelEndFinal);
        }

        return arrayPtrFinal;
    }
}
