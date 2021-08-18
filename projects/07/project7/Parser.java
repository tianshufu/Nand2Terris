
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * Class to parse the Vm code and turn to assemble code
 */
public class Parser {

    /**
     * Define all the command types
     */
    public enum cmdType{
        C_ARITHMETIC,C_PUSH,C_POP,C_LABEL,C_GOTO,
        C_IF,C_FUNCTION,C_RETURN,C_CALL;

    }




    /**
     * Define all the arithmetic command
     */
    private static  final ArrayList<String> arithmeticCommands;
    static  {
        arithmeticCommands=new ArrayList<>();
        arithmeticCommands.add("add");
        arithmeticCommands.add("sub");
        arithmeticCommands.add("neg");
        arithmeticCommands.add("eq");
        arithmeticCommands.add("gt");
        arithmeticCommands.add("lt");
        arithmeticCommands.add("and");
        arithmeticCommands.add("or");
        arithmeticCommands.add("not");
    }

    /**
     *Function: Read the file
     *In: filename: the location of the file
     *Out: List contains each row of the file
     */
    public final List<String> readFile(String filename) {
        List<String> cmdList= new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String str;
            while ((str = in.readLine()) != null) {
                cmdList.add(str);
            }
        } catch (IOException e) {
        }
        return cmdList;

    }

    /**
     *Function: Clean the content, remove empty line and "//"
     * In: read list
     * Out: cleaned list
     */
    public List<String> dataClean(List<String> cmdList)
    {
        List<String> cleanedList=new ArrayList<>();
        for (String str : cmdList) {
            if(str.length()>=2){
                if(str.contains("//")){
                    str=str.trim();
                    int index=str.indexOf("/");
                    String subString=str.substring(0,index);
                    subString=subString.trim();
                    if (subString.length()>0){
                        cleanedList.add(subString);
                    }

                }
                else if (!str.contains("//")){
                    str=str.trim();
                    cleanedList.add(str);
                }
            }
        }
        return cleanedList;
    }

    /**
     * Return the command type for the passed in String
     * @param
     */
     public cmdType commandType(String command){
         /** Check if it's arithmetic*/
         for(String s:arithmeticCommands){
             if(command.contains(s)){
                 return cmdType.C_ARITHMETIC;
             }
         }
         if(command.contains("push")){
             return cmdType.C_PUSH;
         }

         if(command.contains("pop")){
             return cmdType.C_POP;
         }

         return null;

     }

    /**
     * Returns the first argument of the command
     * Should not be called when the command is C_RETURN
     * @param command
     * @return
     */
     public String arg1(String command){
         if (!commandType(command).equals(cmdType.C_RETURN)){
             String[] cmdList=command.split(" ");
             if(cmdList.length>=1){
                 return  cmdList[0];
             }
         }

         return "";

     }

    /**
     * Get the second argument of the command
     * Only return when command type is: C_PUSH,S_POP,C_FUNCTION,C_CALL
     * @param command
     * @return
     */
     public  String arg2(String command){
         String[] cmdList=command.split(" ");
         if (cmdList.length>=2){
             if(commandType(command).equals(cmdType.C_PUSH)){
                    return cmdList[1];
             }

             else if(commandType(command).equals(cmdType.C_POP)){
                 return cmdList[1];
             }

             else if(commandType(command).equals(cmdType.C_FUNCTION)){
                 return cmdList[1];
             }

             else if(commandType(command).equals(cmdType.C_CALL)){
                 return cmdList[1];
             }



         }

         return  "";

     }

     public  int pushIndex(String command){
         if(commandType(command).equals(cmdType.C_PUSH)||commandType(command).equals(cmdType.C_POP)){
             String[] cmdList=command.split(" ");
             String indexStr=cmdList[2];
             Integer index=Integer.parseInt(indexStr);
             return  index;
         }

         return 0;
     }


    public  void main(String[] args) {
        Parser parser=new Parser();
        List<String> vmList=parser.readFile("/Users/futianshu/Desktop/nand2tetris/projects/07/StackArithmetic/SimpleAdd/SimpleAdd.vm");
        List<String> cleanedList=parser.dataClean(vmList);
        System.out.println(cleanedList);
        System.out.println(parser.pushIndex("push constant 7"));
    }



}
