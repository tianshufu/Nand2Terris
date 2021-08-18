

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Deal with writing to file
 */
public class WriteFile {

    public void writeFile(String fileName, List<String> binList){
        try {

            //String content = "This is the content to write into file";

            File file = new File(fileName);

            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for(String str: binList){
                bw.write(str+"\n");
            }

            bw.close();

            System.out.println("Done");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        WriteFile writeFile=new WriteFile();

    }
}

