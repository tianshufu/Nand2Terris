
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;

public class VMTranslator {
    private   Parser parser;
    public static void main(String[] args) {
        File fileName = new File(args[0]);
        CodeWriter codeWriter=new CodeWriter();
        String fileNameStr=fileName.getName();
        File fileOut;
        ArrayList<File> files = new ArrayList<>();
        if (args.length != 1){
            throw new IllegalArgumentException("Inaccurate usage. Please enter in the following format: java VMTranslator (directory/filename)");}
        else if (fileName.isFile() && !(args[0].endsWith(".vm"))){
            throw new IllegalArgumentException("Not the correct file type. Please enter a .vm file or a directory containing .vm files. ");}
        else {
            if (args[0].endsWith(".vm")) {
                String fullFath=fileName.getAbsolutePath();
                String parentPath=fullFath.substring(0,fullFath.lastIndexOf(fileNameStr));
                String asmName=parentPath+fileNameStr.substring(0,fileNameStr.lastIndexOf(".vm"))+".asm";
                codeWriter.model(fullFath,asmName);
            }
        }
    }



}
