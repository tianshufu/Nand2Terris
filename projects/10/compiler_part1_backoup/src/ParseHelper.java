import java.util.ArrayList;
import java.util.List;

/**
 * Class to help parsing the jack files
 */
public class ParseHelper {


    /**
     * Enter a string contains " "  and parse the
     * Example : In : Keyboard.readInt("HOW MANY NUMBERS? ");   Out: "HOW MANY NUMBERS? ",Keyboard.readInt();
     */
    public List<String> stringExtract(String s){
        List<String> tmpList = new ArrayList<>();
        if (s.contains("\"")){
            int firstIndex = s.indexOf("\"");
            //System.out.println(s.indexOf("\""))
            int lastIndex = s.lastIndexOf("\"");
            //get the substring
            String sub = s.substring(firstIndex,lastIndex+1);
            //get the rest of the string
            String left = s.substring(0,firstIndex)+s.substring(lastIndex+1);
            tmpList.add(sub);
            tmpList.add(left);

        }

        return tmpList;
    }

    /**
     * Concat each line to a big string
     * Ref: https://www.jianshu.com/p/2b365772d1b3
     * @param cmdList
     * @return
     */
    public static String listToString(List<String> cmdList){
        return  String.join("\n" , cmdList);
    }


    /**
     * Repeat a string for n time
     * Ref: https://blog.csdn.net/weixin_33770878/article/details/92683553
     * @param str
     * @param n
     * @return
     */
    public static String repeatString(String str, int n) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sb.append(str);
        }
        return sb.substring(0, sb.length());
    }


    public static void main(String[] args) {
        ParseHelper parseHelper = new ParseHelper();
        //String a = "main() {        var Array a";
        System.out.println(repeatString("abc",5));
        //List<String> strsToList1= Arrays.asList(a);
        //System.out.println(strsToList1.get(0));
        //List<String> list  = parseHelper.stringExtract("Keyboard.readInt(\"ENTER THE NEXT NUMBER: \");");
        //System.out.println(list);
    }
}
