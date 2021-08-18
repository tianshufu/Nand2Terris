import java.util.*;


/**
 * Keeps a correspondence between symbolic labels and numeric
 * addresses.
 */
public class SymbolTable {
    public   static  final Map<String,String> symbolTable;
    static
    {
        symbolTable=new HashMap<String, String>();
        symbolTable.put("SP","0");
        symbolTable.put("LCL","1");
        symbolTable.put("ARG","2");
        symbolTable.put("THIS","3");
        symbolTable.put("THAT","4");
        symbolTable.put("R0","0");
        symbolTable.put("R1","1");
        symbolTable.put("R2","2");
        symbolTable.put("R3","3");
        symbolTable.put("R4","4");
        symbolTable.put("R5","5");
        symbolTable.put("R6","6");
        symbolTable.put("R7","7");
        symbolTable.put("R8","8");
        symbolTable.put("R9","9");
        symbolTable.put("R10","10");
        symbolTable.put("R11","11");
        symbolTable.put("R12","12");
        symbolTable.put("R13","13");
        symbolTable.put("R14","14");
        symbolTable.put("R15","15");
        symbolTable.put("SCREEN","16384");
        symbolTable.put("KBD","24576");

    }

    /**
     * Used to label destinations of goto
     * commands
     * Declared by the pseudo-command (XXX)
     * This directive defines the symbol XXX
     * to refer to the memory location holding the
     * next instruction in the program

     */

    public   static  final Map<String,String>  pseudoCommandTable;
    static {
        pseudoCommandTable=new HashMap<String, String>();

    }

    public static  final HashSet<Integer> numSet;
    static
    {
        numSet=new HashSet<Integer>();
        for(int i=0;i<=15;i++){
            numSet.add(i);
        }
        numSet.add(16384);
        numSet.add(24576);

    }


    /**
     * Adds the pair (symbol,
     * address) to the table.
     */
    public void addEntry(String symbol,String address){
        symbolTable.put(symbol,address);

    }


    /**
     *
     * @param symbol symbol (string),address (int)
     * @return   Boolean
     * Does the symbol table contain
     * the given symbol?
     */
    public  boolean contains( String symbol){
        return  symbolTable.containsKey(symbol);

    }


    /**
     *
     * @param symbol (String)
     * @return   int
     *  Returns the address associated
     * with the symbol.
     */
    public int GetAddress(String symbol){
        String addressStr=symbolTable.get(symbol);
        try {
            int address=Integer.parseInt(addressStr);
            return  address;
        }
        catch (Exception e){
            return  0;
        }

    }

    public static void main(String[] args) {
         /*
        for (String key: symbolTable.keySet()){
            System.out.println(key+": "+symbolTable.get(key));

        }

        */



    }

}
