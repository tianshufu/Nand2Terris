

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

/**
 * Generate assembly code from the pared VM commands
 */
public class CodeWriter {

    private int comPareCount=0;
    private static Hashtable<String,String> segmentTable;
    static {
        segmentTable=new Hashtable<>();
        segmentTable.put("local","LCL");
        segmentTable.put("argument","ARG");
        segmentTable.put("this","THIS");
        segmentTable.put("that","THAT");

    }



    /**
     * Convert the push and pop command to the asm code
     * @param segment
     * @param index
     * @return
     */
    public List<String> WritePushPop(Parser.cmdType ctype,String segment,int index){
        List<String> ansList = new ArrayList();
        if (ctype.equals(Parser.cmdType.C_PUSH)){
            // Push constant index
            if(segment.equals("constant")||segment.equals("temp")){
                if(segment.equals("constant")){
                    ansList.add("@"+index);
                }
                else if(segment.equals("temp")){
                    int offset=index+5;
                    ansList.add("@"+offset);
                }
                ansList.add("D=A");
                ansList.add("@SP");
                ansList.add("A=M");
                ansList.add("M=D");
                ansList.add("@SP");
                ansList.add("M=M+1");
                return  ansList;
            }

            if(segment.equals("pointer")){
                if(index==0){
                    //Get the value stored in this to D
                    ansList.add("@THIS");
                }
                else if(index==1){
                    ansList.add("@THAT");
                }
                ansList.add("D=M");
                //Put the value in D to where SP points at
                ansList.add("@SP");
                ansList.add("A=M");
                ansList.add("M=D");
                ansList.add("@SP");
                ansList.add("M=M+1");
            }

            if(segment.equals("static")){
                ansList.add("@Static."+index);
                ansList.add("D=M");
                ansList.add("@SP");
                ansList.add("A=M");
                ansList.add("M=D");
                ansList.add("@SP");
                ansList.add("M=M+1");
            }

            if(segment.equals("local")||segment.equals("argument")||segment.equals("this")||segment.equals("that")){
                String syntax="@"+segmentTable.get(segment);
                ansList.add("@"+index);
                //Set the index to D
                ansList.add("D=A");
                ansList.add(syntax);
                ansList.add("A=M");
                //Add the offset to get the real address
                ansList.add("D=D+A");
                ansList.add("A=D");
                //Store RAM[LCL+i] tp D
                ansList.add("D=M");
                //Store the value to sp
                ansList.add("@SP");
                ansList.add("A=M");
                ansList.add("M=D");
                //Move up the pointer
                ansList.add("@SP");
                ansList.add("M=M+1");
                return  ansList;

            }

            if(segment.equals("static")){

            }

        }

        else if (ctype.equals(Parser.cmdType.C_POP)){

            if(segment.equals("temp")){
               ansList.add("@SP");
               //Move down the pointer and get the value
               ansList.add("M=M-1");
               ansList.add("A=M");
               ansList.add("D=M");
               int offSet=5+index;
               ansList.add("@"+offSet);
               ansList.add("M=D");
               return  ansList;

            }

            if(segment.equals("static")){
                ansList.add("@SP");
                ansList.add("M=M-1");
                ansList.add("A=M");
                ansList.add("D=M");
                ansList.add("@Static."+index);
                ansList.add("M=D");
                return  ansList;
            }

            if(segment.equals("pointer")){
                ansList.add("@SP");
                ansList.add("M=M-1");
                ansList.add("A=M");
                ansList.add("D=M");
                if(index==0){
                    ansList.add("@THIS");
                }
                else if(index==1){
                    ansList.add("@THAT");
                }
                ansList.add("M=D");
                return ansList;

            }

            if(segment.equals("local")||segment.equals("argument")||segment.equals("this")||segment.equals("that")){
                String syntax="@"+segmentTable.get(segment);
               //Get the i,exp:@0
                ansList.add("@"+index);
                ansList.add("D=A");
                //Get the base address
                ansList.add(syntax);
                ansList.add("A=M");
                //ADD  base+i
                ansList.add("D=D+A");
                //Set LCL to the new address
                ansList.add(syntax);
                ansList.add("M=D");
                //Move SP down
                ansList.add("@SP");
                ansList.add("M=M-1");
                ansList.add("A=M");
                ansList.add("D=M");
                //Go to the new adress
                ansList.add(syntax);
                ansList.add("A=M");
                //Store the value from the pointer to the new address
                ansList.add("M=D");
                //?Update the base in LCL
                ansList.add("@"+index);
                ansList.add("D=A");
                ansList.add(syntax);
                ansList.add("A=M");
                ansList.add("D=A-D");
                ansList.add(syntax);
                ansList.add("M=D");
                return  ansList;
            }
        }


        return ansList ;
    }

    public List<String> writeArithmetic(String command){
        List<String> ansList=new ArrayList<>();
        if(command.equals("add")||command.equals("sub")||command.equals("and")||command.equals("or")){
            ansList.add("@SP");
            ansList.add("A=M");
            //Move down the pointer
            ansList.add("A=A-1");
            ansList.add("A=A-1");
            //Get the value of pointer
            ansList.add("D=M");
            //Move up the pointer
            ansList.add("A=A+1");
            if (command.equals("add")){
                ansList.add("D=D+M");
            }
            else if (command.equals("sub")){
                ansList.add("D=D-M");
            }
            else if(command.equals("and")){
                ansList.add("D=D&M");
            }
            else if(command.equals("or")){
                ansList.add("D=D|M");
            }
            //Update the value of the pointer
            ansList.add("@SP");
            ansList.add("M=M-1");
            ansList.add("M=M-1");
            ansList.add("A=M");
            ansList.add("M=D");
            ansList.add("@SP");
            ansList.add("M=M+1");
            return  ansList;
        }

        else if(command.equals("neg")||command.equals("not")){
            ansList.add("@SP");
            ansList.add("M=M-1");
            ansList.add("A=M");
            if(command.equals("neg")){
                ansList.add("M=-M");
            }
            else if (command.equals("not")){
                ansList.add("M=!M");
            }
            ansList.add("@SP");
            ansList.add("M=M+1");
            return ansList;
        }



        else if(command.equals("eq")||command.equals("gt")||command.equals("lt")){
            ansList.add("@SP");
            ansList.add("A=M");
            ansList.add("A=A-1");
            ansList.add("A=A-1");
            ansList.add("D=M");
            ansList.add("A=A+1");
            ansList.add("D=D-M");
            ansList.add("@SP");
            ansList.add("M=M-1");
            ansList.add("M=M-1");

            ansList.add("@TRUE"+comPareCount);
            //if commands is equal
            if (command.equals("eq")){
                //Jump if D eauqls to 0
                ansList.add("D;JEQ");
            }
            //if lt
            else if (command.equals("lt")){
                ansList.add("D;JLT");
            }

            else if(command.equals("gt")){
                ansList.add("D;JGT");
            }
            //If not true ,set 0
            ansList.add("@SP");
            ansList.add("A=M");
            ansList.add("M=0");
            ansList.add("@END"+comPareCount);
            ansList.add("0;JMP");
            ansList.add("(TRUE"+comPareCount+")");
            ansList.add("@SP");
            ansList.add("A=M");
            ansList.add("M=-1");
            ansList.add("(END"+comPareCount+")");
            //add the pointer
            ansList.add("@SP");
            ansList.add("M=M+1");
            comPareCount+=1;
            return  ansList;
        }
        return ansList;
    }

    public void model(String inputFileName, String outputFileName){
        CodeWriter codeWriter=new CodeWriter();
        Parser parser=new Parser();
        WriteFile writeFile=new WriteFile();
        List<String> vmCodeList=parser.readFile(inputFileName);
        List<String> vmCleanedList=parser.dataClean(vmCodeList);
        List<String> finalAnsList=new ArrayList<>();
        System.out.println(vmCleanedList);
        for(String command:vmCleanedList){
            //Get the command type
            if(parser.commandType(command).equals(Parser.cmdType.C_PUSH)){
                List<String> tmpCmdList=codeWriter.WritePushPop(Parser.cmdType.C_PUSH,parser.arg2(command),parser.pushIndex(command));
                //Add the comment
                finalAnsList.add("//"+command);
                finalAnsList.addAll(tmpCmdList);
            }

            else if(parser.commandType(command).equals(Parser.cmdType.C_POP)){
                List<String> tmpCmdList=codeWriter.WritePushPop(Parser.cmdType.C_POP,parser.arg2(command),parser.pushIndex(command));
                //Add the comment
                finalAnsList.add("//"+command);
                finalAnsList.addAll(tmpCmdList);

            }

            else if (parser.commandType(command).equals(Parser.cmdType.C_ARITHMETIC)){
                List<String> tmpCmdList=codeWriter.writeArithmetic(command);
                //Add the comment
                finalAnsList.add("//"+command);
                finalAnsList.addAll(tmpCmdList);
            }

        }

        writeFile.writeFile(outputFileName,finalAnsList);

    }





    public static void main(String[] args) {
        CodeWriter codeWriter=new CodeWriter();
        Parser parser=new Parser();
        WriteFile writeFile=new WriteFile();
        //System.out.println(parser.pushIndex("pop argument 2"));
        //System.out.println(codeWriter.WritePushPop(Parser.cmdType.C_POP,"argument",2));
        //System.out.println(codeWriter.writeArithmetic("eq"));
        codeWriter.model("/Users/futianshu/Desktop/nand2tetris/projects/07/MemoryAccess/PointerTest/PointerTest.vm","/Users/futianshu/Desktop/nand2tetris/projects/07/MemoryAccess/PointerTest/PointerTet.asm");




        //System.out.println(finalAnsList);
        //writeFile.writeFile("/Users/futianshu/Desktop/nand2tetris/projects/07/MemoryAccess/BasicTest/BasicTest.asm",finalAnsList);
        /**
        List<String> ansList=new ArrayList<>();
        ansList=codeWriter.writeArithmetic("eq");
        System.out.println(ansList);
        System.out.println(codeWriter.writeArithmetic("eq"));
         **/
    }


}

