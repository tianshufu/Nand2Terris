import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Reads and outputs the result
 */
public class JackAnalyzer {
    private static void compile(File f) throws CompilerException {
        if (f.getName().endsWith(".jack")) {
            String filePath = f.getPath();
            CompilationEngine compilationEngine = new CompilationEngine(filePath);
            compilationEngine.complieClass();
            writeFile(compilationEngine.getOutFilePath(),compilationEngine.getOutputXmlStr());
            //System.out.println(getOutputXmlStr());
        }
    }

    public static void writeFile(String fileName, String fileContent){
        try {

            //String content = "This is the content to write into file";

            File file = new File(fileName);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(fileContent);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception {
        File f = new File(args[0]);
        if (!f.exists()) {
            System.err.println("File not found!");
            System.exit(1);
        }

        if (f.isDirectory()) {
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (file.isFile()) {
                    compile(file);
                }
            }
        } else if (f.isFile()) {
            compile(f);
        }

    }

}

