import java.util.Collections;

/**
 * Translate string to 16-bits binary code

 */
public class Code {

    /**
     * Translate A command
     *param: address(num)

     */
    public String ACommandTranslateHelper(String address){
        String resultBin = Integer.toBinaryString(Integer.parseInt(address));
        System.out.println(resultBin);
        if (resultBin.length()<15){

            return "0"+String.join("", Collections.nCopies(15- resultBin.length(), "0"))+resultBin;
        }

        else {
            return  "0"+resultBin;
        }

    }

    /**
     * Translate A command to bin
     * @param command
     * @return
     */
    public String ACommandTranslate(String command){
        Parser parser=new Parser();
        String symbol=parser.symbol(command);
        System.out.println(symbol);

        try {
            Double.parseDouble(symbol);
            //if contains Number
            return  ACommandTranslateHelper(symbol);

        }
        catch (Exception e){
            //if not contains number
            String address=SymbolTable.symbolTable.get(symbol);
            if (address!=null){
                return  ACommandTranslateHelper(address);

            }
            else {
                System.out.println(symbol);
                return  ACommandTranslateHelper(symbol);
            }
           // System.out.println(address);
            ///return  ACommandTranslateHelper(address);

        }

    }

    /**
     * Tranlate C command bin
     * @param command
     * @return  translated C command
     */
    public String CommandTranslate(String command){
        Parser parser=new Parser();
        return  "111"+parser.comp(command)+parser.dest(command)+parser.jump(command);

    }

    public  boolean isLetter(String str) {
        String regex = "^[a-zA-Z]+$";
        return str.matches(regex);
    }


    public String LCommandTranslate(String command){
        Parser parser=new Parser();
        String symbol=parser.symbol(command);
        String address=SymbolTable.pseudoCommandTable.get(symbol);
        return  ACommandTranslateHelper(address);

    }


    public static void main(String[] args) {
        Code code= new Code();
        //String Acode=code.ACommandTranslateHelper("3");
       // System.out.println(Acode);
        //String Code=code.ACommandTranslate("@7058");
        //System.out.println(Code);
        String str="Counter";
        //System.out.println(str.matches(".*\\s.*"));
        //System.out.println(str.matches(".*\\d.*"));
        //System.out.println(isLetter("counter"));

    }
}
