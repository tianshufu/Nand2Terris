import java.util.ArrayList;
import java.util.List;

public class General {

    /**
     * Scan the file for the first time
     * Update the symbolTable
     * @param fileName
     */
    public int firstScan(String fileName){
        List<String> cmdList;
        List<String> cleanedList;
        SymbolTable symbolTables=new SymbolTable();
        Parser parser=new Parser();
        cmdList=parser.readFile(fileName);
        cleanedList=parser.dataClean(cmdList);
        int lineNum=0;
        //define the Ram start
        int ramFlag=16;
        ///System.out.println(cleanedList);
        for (String str: cleanedList)
        {
            String symbol=parser.symbol(str);
            if (parser.commandType(str)== Parser.cmdType.L_COMMAND)
            {
                //if command not in the map
                if(SymbolTable.pseudoCommandTable.get(symbol)==null){
                    SymbolTable.pseudoCommandTable.put(symbol,Integer.toString(lineNum));
                }

            }
            else {
                lineNum+=1;
            }
        }

        return  ramFlag;

        }

        /**
         * Second Scan the list
         * Turn the value in the cleanedList to
         * binary code
         */
        public List<String> secondScan (List<String> cleanedList, int ramFlag){
            Parser parser=new Parser();
            Code code=new Code();
            SymbolTable symbolTable=new SymbolTable();
            ArrayList<String> newList=new ArrayList<String>();
            int flag=ramFlag;
            for(String str: cleanedList){
                if(parser.commandType(str)==Parser.cmdType.A_COMMAND){
                    //Extract the @
                    String symbol=parser.symbol(str);
                    //if not number
                    if(code.isLetter(symbol)){
                        //if symbol not in the table
                        if(!symbolTable.contains(symbol)){
                           //add the symbol and flag to the table
                            symbolTable.addEntry(symbol,Integer.toString(flag));
                            flag+=1;
                        }
                    }

                    //if the symbol is psudo command
                    if (SymbolTable.pseudoCommandTable.get(symbol)!=null){
                        System.out.println("L:"+str);
                        newList.add(code.LCommandTranslate(str));
                    }
                    //if normal command
                    else {
                        System.out.println("A:"+str);
                        newList.add(code.ACommandTranslate(str));
                    }

                }
                else if(parser.commandType(str)==Parser.cmdType.C_COMMAND) {
                    newList.add(code.CommandTranslate(str));
                }


            }
            return  newList;

        }



    //public  void  tablePrint()

    public static void main(String[] args) {
        Parser parser=new Parser();
        General general=new General();
        WriteFile writeFile=new WriteFile();
        Code code=new Code();
        List<String> cmdList;
        List<String> cleanedList;
        List<String> newList;
        cmdList=parser.readFile("/Users/futianshu/Desktop/nand2tetris/projects/06/pong/Pong.asm");
        //System.out.println(cmdList);
        cleanedList=parser.dataClean(cmdList);
        System.out.println(cleanedList);
        int ramFlag=general.firstScan("/Users/futianshu/Desktop/nand2tetris/projects/06/pong/Pong.asm");
        ///System.out.println(code.LCommandTranslate("(INFINITE_LOOP)"));


        for (String key: SymbolTable.pseudoCommandTable.keySet()){
            System.out.println("Key:"+key+" "+SymbolTable.pseudoCommandTable.get(key));
        }


        newList=general.secondScan(cleanedList,ramFlag);
        for (String key: SymbolTable.symbolTable.keySet()){
            System.out.println(key+": "+SymbolTable.symbolTable.get(key));

        }
        writeFile.writeFile("/Users/futianshu/Desktop/nand2tetris/projects/06/pong/Pong.hack",newList);
        System.out.println(newList);



    }
}
