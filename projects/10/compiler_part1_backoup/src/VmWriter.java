import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Class to write vm codes
 */
public class VmWriter {
    private static final Hashtable<String,String> opDict;
    private static final Hashtable<String,String> sopDict;
    private final List<String> codes;

    static {
        opDict = new Hashtable<>();
        opDict.put("+","add");
        opDict.put("-","sub");
        opDict.put("=","eq");
        opDict.put(">","gt");
        opDict.put("<","lt");
        opDict.put("&","and");
        opDict.put("|","or");


        sopDict = new Hashtable<>();
        sopDict.put("-","neg");
        sopDict.put("~","not");

    }

    public List<String> getCodes() {
        return codes;
    }

    /**
     * Creates a new output .vm file and prepare for
     * writrin
     */
    public VmWriter(){
        this.codes = new ArrayList<>();

    }



    /**
     * Write a VM push command
     * @param segment (CONST,ARG,LOCAL,STATIC,THIS,THAT,POINTER,TEMP)
     * @param index
     */
    public void writePush(String segment, Integer index){
        String cmd = "push "+segment+" "+index;
        codes.add(cmd);

    }

    /**
     * Write a VM pop command
     * @param segment (ARG,LOCAL,STATIC,THIS,THAT,POINTER,TEMP)
     * @param index
     */
    public void writePop(String segment, Integer index){
        String cmd = "pop "+segment+" "+index;
        codes.add(cmd);

    }

    /**
     * Writes a VM arithmetic-logical command
     * @param command
     */
    public void writeArithmetic(String command){
        assert (opDict.contains(command)) : "invalid or missing operation";
        String cmd = opDict.get(command);
        codes.add(cmd);
    }

    /**
     * Write a VM sig command : ~ , -
     * @param command
     */
    public void writeSigArithmetic(String command){
        assert (sopDict.contains(command)) : "invalid or missing operation";
        String cmd = sopDict.get(command);
        codes.add(cmd);
    }

    /**
     * Writes a VM label command
     * @param label
     */
    public void writeLabel(String label){
        String cmd = "label "+label;
        codes.add(cmd);
    }

    /**
     * Writes a VM goto command
     * @param label
     */
    public void writeGoto(String label){
        String cmd = "goto "+label;
        System.out.println("written goto code:"+ cmd);
        codes.add(cmd);
    }

    /**
     * Writes a VM if-go command
     * @param label
     */
    public void writeIf(String label){
        String cmd = "if-goto "+label;
        codes.add(cmd);
    }

    /**
     * Writes a VM call command
     * @param name
     * @param nArgs
     */
    public void writeCall(String name, int nArgs){
        String cmd = "call "+name+" "+nArgs;
        codes.add(cmd);
    }

    /**
     * Write a VM function command
     * @param name
     * @param nLocals
     */
    public void writeFunction(String name, int nLocals){
        String cmd = "function "+name+" "+nLocals;
        codes.add(cmd);
    }

    /**
     * Writes a return command
     */
    public void writeReturn(){
        String cmd = "return";
        codes.add(cmd);
    }











    public static void main(String[] args) {
        VmWriter vmWriter = new VmWriter();
        vmWriter.writePush("ARG",0);
        vmWriter.writePush("ARG",1);
        System.out.println(vmWriter.getCodes());
    }

}
