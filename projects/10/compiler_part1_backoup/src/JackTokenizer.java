/**Ignore all comments and white spaces in the input stream, and serializes into Jack-languages token.
 *  The token types are specified according to the jack grammar
 *
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

//import static com.company.ParseHelper.listToString;


public class JackTokenizer {



    Pattern pattern = Pattern.compile("[0-9]*");
    private final static BigInteger MAX_INT_LITERAL = new BigInteger("32767");

    /**Define all the keywords

     */
    private  static  HashSet<String> keyWordsSet;
    static  {
        keyWordsSet = new HashSet<>();
        keyWordsSet.add("class");
        keyWordsSet.add("constructor");
        keyWordsSet.add("function");
        keyWordsSet.add("method");
        keyWordsSet.add("field");
        keyWordsSet.add("static");
        keyWordsSet.add("var");
        keyWordsSet.add("int");
        keyWordsSet.add("char");
        keyWordsSet.add("boolean");
        keyWordsSet.add("void");
        keyWordsSet.add("true");
        keyWordsSet.add("false");
        keyWordsSet.add("null");
        keyWordsSet.add("this");
        keyWordsSet.add("let");
        keyWordsSet.add("do");
        keyWordsSet.add("if");
        keyWordsSet.add("else");
        keyWordsSet.add("while");
        keyWordsSet.add("return");

    }

    private static HashSet<Character>  symbolSets;
    static {
        symbolSets = new HashSet<>();
        symbolSets.add('{');
        symbolSets.add('}');
        symbolSets.add('(');
        symbolSets.add(')');
        symbolSets.add('[');
        symbolSets.add(']');
        symbolSets.add('.');
        symbolSets.add(',');
        symbolSets.add(';');
        symbolSets.add('+');
        symbolSets.add('-');
        symbolSets.add('*');
        symbolSets.add('/');
        symbolSets.add('&');
        symbolSets.add('|');
        symbolSets.add('<');
        symbolSets.add('>');
        symbolSets.add('=');
        symbolSets.add('~');

    }

    public static class Token {
        /**
         * Define all the token types
         */

        public enum Type {

            keyword, symbol, identifier, integerConstant, stringConstant;
        }

        private final String value;
        private final Type type;
        private final int columNumber;
        private final int lineNumber;

        private Token(String value, Type type, int lineNumber, int columNumber) {
            this.value = value;
            this.type = type;
            this.lineNumber = lineNumber;
            this.columNumber = columNumber;
        }

        public Type getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public int getColumNumber() {
            return columNumber;
        }

        @Override
        public final String toString() {
            return toXml();
        }

        public String toXml() {
            return "<" + type + ">" + escapeXML(value) + "</" + type + ">";
        }

        private static String escapeXML(String value) {
            return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
        }
    }



    /**
     *Function: Read the file
     *In: filename: the location of the file
     *Out: List contains each row of the file
     */
    public static final List<String> readFile(String filename) {
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
     *Function: Clean the content, remove empty line and "//" and /**  and
     * In: read list
     * Out: cleaned list, each ith  element represent the string of the i th line
     */
    public List<String> dataClean(List<String> cmdList)
    {
        List<String> cleanedList=new ArrayList<>();
        for (String str : cmdList) {
            if(str.length()>=2){
                if(str.contains("//")||str.contains("/**")){
                    // If the // it at the end of the Jack language, but are in the same line
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
     * Take the cleanedLineList and split the " " in each line
     * @param cleanedLineList
     * @return
     */
    public List<String> lineToWords(List<String> cleanedLineList){
        List<String> tokenList = new ArrayList<>();
        //Iterate the list
        for(String s:cleanedLineList){
            for (String t :s.split(" ")){
                tokenList.add(t);
            }
        }
        return  tokenList;

    }

    /**
     * Check if
     * @param str
     * @return
     */
    public  boolean isNumeric(String str){

        return pattern.matcher(str).matches();
    }



    /**
     * Take the token string stream and put the tokens to the token list
     * @param tokens
     * @return
     */
    public static List<Token> parseToken(String tokens) throws CompilerException {
        List<Token> tokenList = new ArrayList<>();
        int start = 0 ;
        int end = tokens.length();
        int pos = 0 ;
        int lineStartIndex = 0;
        int lineNum = 1;
        boolean escaping = false;
        boolean inString = false;
        boolean inSingleLineComment = false;
        boolean inMultiLineComment = false;
        //Iterate the token string
        while (pos < end) {
            // get the char for the pos
            char c = tokens.charAt(pos);
            if (inString) {
                // if is changing line
                if (c == '\n') {
                    throw new CompilerException("End of line found inside string literal", lineNum, start - lineStartIndex + 1);
                }
                if (escaping) {
                    escaping = false;
                } else {
                    // if already get out of the string
                    if (c == '\\') {
                        escaping = true;
                    }
                    //If reached the end of the string
                    else if (c == '\"') {
                        inString = false;
                        tokenList.add(new Token(tokens.substring(start, pos), Token.Type.stringConstant, lineNum, start - lineStartIndex + 1));
                        start = pos + 1;
                    }
                }

            } else {
                // if is in single line comment
                if (inSingleLineComment) {
                    //if going the change the line
                    if (c == '\n') {
                        start = pos + 1;
                        lineNum += 1;
                        lineStartIndex = pos + 1;
                        inSingleLineComment = false;
                    }
                    // if is line multiline comment
                } else if (inMultiLineComment) {
                    if (c == '\n') {
                        lineNum++;
                        lineStartIndex = pos + 1;
                    }
                    // if reached the line "*/", means got to the end of the multi line comment
                    else if (c == '*' && pos + 1 < end && tokens.charAt(pos + 1) == '/') {
                        start = pos + 2;
                        pos++;
                        inMultiLineComment = false;
                    }
                    // if meet to the "//" ,generate the element before "//"
                } else if (c == '/' && pos + 1 < tokens.length() && tokens.charAt(pos + 1) == '/') {
                    if (pos != start) {
                        tokenList.add(createTokenFrom(tokens.substring(start, pos), lineNum, start - lineStartIndex + 1));
                    }
                    inSingleLineComment = true;
                    // if meet "/**", generate the element before "/**"
                } else if (c == '/' && pos + 2 < tokens.length() && tokens.charAt(pos + 1) == '*' && tokens.charAt(pos + 2) == '*') {
                    if (pos != start) {
                        tokenList.add(createTokenFrom(tokens.substring(start, pos), lineNum, start - lineStartIndex + 1));
                    }
                    inMultiLineComment = true;
                    // if meet the char of  ' " ', means met a new string, add the element before " to the token list
                } else if (c == '\"') {
                    if (pos != start) {
                        tokenList.add(createTokenFrom(tokens.substring(start, pos), lineNum, start - lineStartIndex + 1));
                    }
                    start = pos + 1;
                    inString = true;
                    // if meet the space, add the pre elements to the token lists
                } else if (isSpace(c)) {
                    if (pos != start) {
                        tokenList.add(createTokenFrom(tokens.substring(start, pos), lineNum, start - lineStartIndex + 1));
                    }
                    start = pos + 1;
                    if (c == '\n') {
                        lineNum += 1;
                        lineStartIndex = pos + 1;
                    }
                } else if (symbolSets.contains(c)) {
                    if (pos != start) {
                        tokenList.add(createTokenFrom(tokens.substring(start, pos), lineNum, start - lineStartIndex + 1));
                    }
                    // add the current c as token to the list
                    tokenList.add(new Token(tokens.substring(pos, pos + 1), Token.Type.symbol, lineNum, start - lineStartIndex + 1));
                    start = pos + 1;
                }
            }
            pos++;
        }
        if (inString){
            throw  new CompilerException("Non terminated string iteral", lineNum, start - lineStartIndex + 1);
        }

        if(pos !=start){
            tokenList.add(createTokenFrom(tokens.substring(start, pos), lineNum, start - lineStartIndex + 1));
        }
        return tokenList;
    }


    private static Token createTokenFrom(String s, int line, int column) throws CompilerException {
        if (s.charAt(0) > 47 && s.charAt(0) < 58) { // starts with number
            try {
                //set the range to 0-32767
                if (Integer.parseInt(s)>32767){
                    throw new CompilerException("Integer literal cannot exceed " + MAX_INT_LITERAL, line, column);
                }
                return new Token(s, Token.Type.integerConstant, line, column);
            } catch (NumberFormatException nfe) {
                throw new CompilerException("Invalid token found: " + s, line, column);
            }
        } else if (keyWordsSet.contains(s)) {
            return new Token(s, Token.Type.keyword, line, column);
        } else {
            return new Token(s, Token.Type.identifier, line, column);
        }
    }

    /**
     * Take the element in the token and check if it is space
     * @param c
     * @return
     */
    private static boolean isSpace(char c){
        return  (c == ' ' || c == '\t' || c == '\n' || c == '\r') ;
    }


    /**
     * Take in the file path, read the file and transfer to the String format for parsing
     * @param path
     * @return
     */
    public static String fileToString(String path){
        List<String> vmList= readFile(path);
        return ParseHelper.listToString(vmList);
    }

    public static void main(String[] args) throws CompilerException {
        JackTokenizer jackTokenizer = new JackTokenizer();
        ParseHelper parseHelper = new ParseHelper();
        List<String> vmList= readFile("/Users/futianshu/Desktop/nand2tetris/projects/10/Compliler_Part1/src/com/company/Main2.jack");
        //List<String> cleanedList = jackTokenizer.dataClean(vmList);
        //List<String> tokenList = jackTokenizer.lineToWords(cleanedList);
        //for(String s : vmList){
            //System.out.println("Token: "+s+" Token type:"+jackTokenizer.getTokenType(s));
        //}
        //String cmdString = listToString(vmList);
        //System.out.println(cmdString);
        //cmdString = cmdString.replace("&lt",'<'+"");
        //cmdString = cmdString.replace("&bg",'>'+"");
        //String cmdString = "let a = Array.new(length);";
        //List<Token> tokenList = parseToken(cmdString);

        //for (Token t: tokenList){
        //    System.out.println(t);
        //}


    }

}
