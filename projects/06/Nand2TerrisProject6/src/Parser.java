import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class Parser {
    /**
     * Type of the command
     */
    public enum cmdType{
        A_COMMAND, C_COMMAND, L_COMMAND;

    }

    private static final Map<String, String> destMap;
    static
    {
        destMap = new HashMap<String, String>();
        destMap.put("null","000");
        destMap.put("M","001");
        destMap.put("D","010");
        destMap.put("MD","011");
        destMap.put("A","100");
        destMap.put("AM","101");
        destMap.put("AD","110");
        destMap.put("AMD","111");
    }

    private static final Map<String, String> compMap;
    static
    {
        compMap=new HashMap<String, String>();
        //default: a=1,c=101010
        compMap.put("","1101010");
        //when a=1
        compMap.put("M","1110000");
        compMap.put("!M","1110001");
        compMap.put("-M","1110011");
        compMap.put("M+1","1110111");
        compMap.put("M-1","1110010");
        compMap.put("D+M","1000010");
        compMap.put("D-M","1010011");
        compMap.put("M-D","1000111");
        compMap.put("D&M","1000000");
        compMap.put("D|M","1010101");

        //when a=0
        compMap.put("0","0101010");
        compMap.put("1","0111111");
        compMap.put("-1","0111010");
        compMap.put("D","0001100");
        compMap.put("A","0110000");
        compMap.put("!D","0001101");
        compMap.put("!A","0110001");
        compMap.put("-D","0001111");
        compMap.put("-A","0110011");
        compMap.put("D+1","0011111");
        compMap.put("A+1","0110111");
        compMap.put("D-1","0001110");
        compMap.put("A-1","0110010");
        compMap.put("D+A","0000010");
        compMap.put("D-A","0010011");
        compMap.put("A-D","0000111");
        compMap.put("D&A","0000000");
        compMap.put("D|A","0010101");


    }

    private static  final  Map<String,String> jumpMap;
    static
    {
        jumpMap=new HashMap<String, String>();
        jumpMap.put("null","000");
        jumpMap.put("JGT","001");
        jumpMap.put("JEQ","010");
        jumpMap.put("JGE","011");
        jumpMap.put("JLT","100");
        jumpMap.put("JNE","101");
        jumpMap.put("JLE","110");
        jumpMap.put("JMP","111");

    }






    /**
    *Function: Read the file
    *In: filename: the location of the file
    *Out: List contains each row of the file
    */
    public List<String> readFile(String filename) {
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
     * Returns the type of the current
     * command:
     * m A_COMMAND for @Xxx where
     * Xxx is either a symbol or a
     * decimal number
     * m C_COMMAND for
     * dest=comp;jump
     * m L_COMMAND (actually, pseudocommand) for (Xxx) where Xxx
     * is a symbol.

     */
    public cmdType commandType (String command){

        if (command.contains("@")){
            return  cmdType.A_COMMAND;
        }

        else if (command.contains(("("))){
            return  cmdType.L_COMMAND;
        }

        else {
            return cmdType.C_COMMAND;
        }
    }


    /**
     * Returns the symbol or decimal
     * Xxx of the current command
     * @Xxx or (Xxx). Should be called
     * only when commandType() is
     * A_COMMAND or L_COMMAND.

     */
    public String symbol (String command) {
        if (commandType(command) == cmdType.A_COMMAND) {
            return command.substring(1);

        }
        else if (commandType(command) == cmdType.L_COMMAND) {
            return  command.substring(1,command.length()-1);

        } else {
            return "";
        }

    }


    /**
     * Returns the dest mnemonic in
     * the current C-command (8 possibilities). Should be called only
     * when commandType() is C_COMMAND

     */
    public String dest (String command){
        if (commandType(command) == cmdType.C_COMMAND) {
            if (command.contains("=")){
                int indexEqual=command.indexOf("=");
                String destStr=command.substring(0,indexEqual);
                //System.out.println(destStr);
                return destMap.get(destStr);
            }
            return "000";
        }
        else {
            return  "000" ;
        }
    }

    /**
     * Returns the comp mnemonic in
     * the current C-command (28 possibilities). Should be called only
     * when commandType() is
     * C_COMMAND.

     */
    public String comp(String command){
        if (commandType(command) == cmdType.C_COMMAND){
            if (command.contains("=")){
                int indexEqual=command.indexOf("=");
                if (command.contains(";")){
                    int indexEndFlag=command.indexOf(";");
                    System.out.println(command.substring(indexEqual+1,indexEndFlag));
                    return  compMap.get(command.substring(indexEqual+1,indexEndFlag));
                }
                else {
                    return  compMap.get(command.substring(indexEqual+1));
                }
            }

            else {
                int indexEndFlag=command.indexOf(";");
                return  compMap.get(command.substring(0,indexEndFlag));

            }

        }
        return  "1101010";
    }

    /**
     * Returns the jump mnemonic in
     * the current C-command (8 possibilities). Should be called only
     * when commandType() is
     * C_COMMAND.
     */
    public String jump (String command){
        if (commandType(command) == cmdType.C_COMMAND){
            if (command.contains(";")){
                int startIndex=command.indexOf(";");
                return jumpMap.get(command.substring(startIndex+1));
            }
            return "000";
        }
        return "000";
    }


    public static void main(String[] args) {
        Parser parser=new Parser();
        List<String> cmdList;
        List<String> cleanedList;

        cmdList=parser.readFile("/Users/futianshu/Desktop/nand2tetris/projects/06/rect/Rect.asm");
        cleanedList=parser.dataClean(cmdList);
        //cleanedList.add("(Loop)");
        //System.out.println(cleanedList);
        //for(String str:cleanedList){
            //parser.comp(str);
          //  System.out.println("Command:"+str +"Jump:"+parser.jump(str));
       // }
        //System.out.println(parser.commandType(cleanedList[0]));
        System.out.println(parser.comp("D;JGT"));


    }

}







