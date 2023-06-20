package javalette;

import javalette.llvm.LlvmParser;
import javalette.llvm.Program;
import javalette.typeCheck.TypeChecker;

import java.io.*;

public class Compiler {
    public static void main(String[] args) {
        try {
            Reader input;
            if (args.length == 0) input = new InputStreamReader(System.in);
            else input = new FileReader(args[0]);
            // Lexer
            Yylex l = new Yylex(input);
            
            // Parser
            parser p = new parser(l, l.getSymbolFactory());
        
            // Type checker
            javalette.Absyn.Prog parseTree = p.pProg();
            new TypeChecker().typeCheck(parseTree);

            Program program = new LlvmParser().parse(parseTree);

            String stringLLVM = program.write();

            PrintWriter writer = new PrintWriter("program.ll");
            writer.print(stringLLVM);
            writer.close();

        } catch(IOException e) {
            System.err.println("Error: File not found: " + args[0]);
            System.exit(1);
        } catch(Error e) {
            System.out.println("Error: " + e.toString());
            System.err.println("ERROR");
            System.exit(1);
        } catch(Exception e) {
            System.out.println("Error: " + e.toString());
            System.err.println("ERROR");
            e.printStackTrace();
            System.exit(1);
        }
        System.err.println("OK");
        System.exit(0);
    }
}     

