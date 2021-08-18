import java.util.HashSet;
import java.util.List;

/**
 * Get input from the Jack tokenizer and emits the output to the output file
 */
public class CompilationEngine {
    private   String tokens;
    private  String outFilePath;
    private String outputXmlStr;
    private  List tokenList;
    private  Integer cur;
    private  Integer depth;
    private static HashSet<String> statementSets;
    private static HashSet<String> termKeyWordSets;
    private static HashSet<String> operationSets;

    public CompilationEngine(String inFilePath) throws CompilerException {
        this.tokens = JackTokenizer.fileToString(inFilePath);
        this.tokenList = JackTokenizer.parseToken(tokens);
        this.cur = 0;
        this.depth = 0;
        this.outFilePath = inFilePath.substring(0,inFilePath.indexOf(".jack"))+".xml";
        this.outputXmlStr = "";

    }

    static {
        statementSets = new HashSet<>();
        statementSets.add("do");
        statementSets.add("if");
        statementSets.add("while");
        statementSets.add("let");
        statementSets.add("return");

        termKeyWordSets = new HashSet<>();
        termKeyWordSets.add("true");
        termKeyWordSets.add("false");
        termKeyWordSets.add("null");
        termKeyWordSets.add("this");

        operationSets = new HashSet<>();
        operationSets.add("+");
        operationSets.add("-");
        operationSets.add("*");
        operationSets.add("/");
        operationSets.add("&");
        operationSets.add("|");
        operationSets.add("<");
        operationSets.add(">");
        operationSets.add("=");


    }







    /**
     * Get the cur token
     * @return
     */
    public JackTokenizer.Token getCurToken(){
        return (JackTokenizer.Token) tokenList.get(cur);
    }

    public String getOutputXmlStr() {
        return outputXmlStr;
    }

    public String getOutFilePath() {
        return outFilePath;
    }

    public String getTokens(){return tokens;}

    public HashSet<String> getStatementSets(){ return statementSets;}
    public HashSet<String> getTermKeyWordSets(){ return termKeyWordSets;}
    public HashSet<String> getOperationSets(){return operationSets;}



    /**
     * Get the next token for the cur pointer
     * @return
     * @throws Exception
     */
    public JackTokenizer.Token getNextToken() throws CompilerException {
        if (cur+1>tokenList.size()){
            throw new CompilerException("Next token Index out of bound");
        }
        else {
            return (JackTokenizer.Token) tokenList.get(cur+1);
        }
    }


    /**
     * Move to the cur token to the next one
     */
    public  void moveNext(){
        this.cur ++;
    }

    /**
     * Get the whole token list
     * @return
     */
    public List getTokenList() {
        return tokenList;
    }

    public void nextDepth(){
        this.depth++;
    }

    public void prevDepth() {
        if (this.depth > 0) {
            this.depth--;
        }
    }




    /**
     * Compile the output for the curToken
     * @param token
     */
    public void complieOutElement(JackTokenizer.Token token){
        //Add the level space to the final output string
        String spaceBeforeToken = ParseHelper.repeatString(" ",this.depth);
        this.outputXmlStr += spaceBeforeToken;
        // Add the token
        this.outputXmlStr += token.toXml();
        this.outputXmlStr += "\n";

    }

    public void complieOut(String str){
        this.outputXmlStr += ParseHelper.repeatString(" ",this.depth);
        this.outputXmlStr += str;
        this.outputXmlStr += "\n";
    }

    /**
     * General function for compiling
     * Param: 'class' className '{'  classVarDec* subroutineDec* '}'
     */
    public void complieClass() throws CompilerException {
        // write <class>
        complieOut("<class>");
        nextDepth();

        //JackTokenizer.Token curToken = getCurToken();
        // parse the class
        if ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("class"))){
            complieOutElement(getCurToken());
            // go to the next token
            moveNext();
        }
        //parse class name
        if (getCurToken().getType() == JackTokenizer.Token.Type.identifier){
            complieOutElement(getCurToken());
            moveNext();
        }

        // parse symbol '{'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("{"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("missing { in class",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        //parse class valDes, start parsing if the start token is "static" or "field"
        while((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && ((getCurToken().getValue().equals("static"))|| (getCurToken().getValue().equals("field")))){
            compileClassVarDec();
            // no need to move next here, since the function already did this
        }

        // parse methods : "method", "constructor", "function"
        while ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && ((getCurToken().getValue().equals("method")) || (getCurToken().getValue().equals("constructor")) || (getCurToken().getValue().equals("function")))){
            compileSubroutine();
        }

        // parse symbol '}'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("}"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("missing } in class",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // end parsing
        prevDepth();
        complieOut("</class>");

    }

    /**
     * Compile for the var Declaration, exp: <classVarDex>static</>
     * Form: 'var' type varName (',' varName)* ';'
     */
    public void compileClassVarDec(){
        complieOut("<classVarDec>");
        // create an indent
        nextDepth();
        //JackTokenizer.Token curToken = getCurToken();
        // parse key word, compile if it is "static" or "field"
        if(getCurToken().getType() == JackTokenizer.Token.Type.keyword){
            if (getCurToken().getValue().equals("static") || getCurToken().getValue().equals("field")){
                complieOutElement(getCurToken());
                moveNext();
            }
        }
        // parse val type , example: boolean
        if((getCurToken().getType() == JackTokenizer.Token.Type.keyword) || (getCurToken().getType() == JackTokenizer.Token.Type.identifier)){
            complieOutElement(getCurToken());
            moveNext();
        }

        // parse val name, example: test
        if (getCurToken().getType() == JackTokenizer.Token.Type.identifier){
            complieOutElement(getCurToken());
            moveNext();
        }

        // parse left val name, example: field int x,y;
        while(!((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";")))){
            // parse symbol ","
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(","))){
                complieOutElement(getCurToken());
                moveNext();
            }
            // parse val name
            if (getCurToken().getType() == JackTokenizer.Token.Type.identifier){
                complieOutElement(getCurToken());
                moveNext();
            }
        }

        //parse the end symbol ";"
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";"))){
            complieOutElement(getCurToken());
            moveNext();
        }

        // compile the end part
        prevDepth();
        complieOut("</classVarDec>");

    }

    /**
     * Compile subroutine
     * Param: ('function'|'method'| 'constructor') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
     * Exp:  constructor Square new(int Ax, int Ay, int Asize){ ...}
     * @throws CompilerException
     */
    public void compileSubroutine() throws CompilerException {
        complieOut("<subroutineDec>");
        // move the indent
        nextDepth();
        // parse key word : "function","method", "constructor"
        if ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && ((getCurToken().getValue().equals("method")) || (getCurToken().getValue().equals("constructor")) || (getCurToken().getValue().equals("function")))){
            complieOutElement(getCurToken());
            moveNext();
        }

        // parse type : int , char , void , boolean
        if((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && ((getCurToken().getValue().equals("int")) || (getCurToken().getValue().equals("char")) ||  (getCurToken().getValue().equals("boolean") || (getCurToken().getValue().equals("void"))))){
            complieOutElement(getCurToken());
            moveNext();
        } else if(getCurToken().getType() == JackTokenizer.Token.Type.identifier){
            complieOutElement(getCurToken());
            moveNext();
        }

        // parse subroutine name
        if(getCurToken().getType() == JackTokenizer.Token.Type.identifier){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("missing subroutine name in subroutine",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse "("
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("("))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("missing ( in subroutine",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // parse parameter lists ,exp: constructor Square new(int Ax, int Ay, int Asize)
        compileParameterList();

        // parse ")"
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(")"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("missing ) in subroutine",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse the sub routine body
        compileSubroutineBody();

        prevDepth();
        complieOut("</subroutineDec>");

    }

    /**
     * Compile's a subroutine body
     * param :  '{' varDec* statements '}'
     */
    public void compileSubroutineBody() throws CompilerException {
        complieOut("<subroutineBody>");
        nextDepth();

        // parse '{'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("{"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("missing { in  subroutine body",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // parse var ,exp:  var Array a;
        while ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("var"))){
            // compile var dec
            compileVarDec();
            //moveNext();
        }

        // parse the statements
        compileStatements();

        // parse '}'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("}"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            System.out.println(outputXmlStr);
            System.out.println(getCurToken().getValue());
            throw new CompilerException("missing } in  subroutine body",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        prevDepth();
        complieOut("</subroutineBody>");

    }



    /**
     * Compile parameter lists
     * Forms: ((type varName) (',' type varName)*)?
     * Exp: Square new(int Ax, int Ay, int Asize)
     */
    public void compileParameterList(){
        complieOut("<parameterList>");
        // create an indent
        nextDepth();
        // while not reaching the end of the list ')'
        while (!getCurToken().getValue().equals(")")){
            // parse first element type, exp: int or char or void or boolean
            if((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && ((getCurToken().getValue().equals("int")) || (getCurToken().getValue().equals("char")) ||  (getCurToken().getValue().equals("boolean") || (getCurToken().getValue().equals("void"))))){
                complieOutElement(getCurToken());
                moveNext();
            }

            // parse the val name , exp: Ax
            if(getCurToken().getType() == JackTokenizer.Token.Type.identifier){
                complieOutElement(getCurToken());
                moveNext();
            }
            // parse ","
            if (getCurToken().getType() == JackTokenizer.Token.Type.symbol && getCurToken().getValue().equals(",")){
                complieOutElement(getCurToken());
                moveNext();
            }
        }
        prevDepth();
        complieOut("</parameterList>");

    }



    /**
     * Compiles a var declaration
     * Form: 'var' type varName (',' varName)* ';'
     * Exp: var int length;
     */
    public void compileVarDec() throws CompilerException{
        complieOut("<varDec>");
        nextDepth();
        // parse "var"
        if ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("var"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("invalid var defined",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse key words before the syntax  ';'
        while (!((getCurToken().getType()==JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";")))){
            complieOutElement(getCurToken());
            moveNext();
        }

        // parse the ';' symbol
        if ((getCurToken().getType()==JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing end symbol ; in the var declaration",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        prevDepth();
        complieOut("</varDec>");

    }

    /**
     * Compiles a sequence of statements, does not handle '{' '}'
     */
    public void compileStatements() throws CompilerException{
        complieOut("<statements>");
        nextDepth();
        // if the cur token is "do","if","while","let,"return"
        while (getCurToken().getType()==JackTokenizer.Token.Type.keyword && statementSets.contains(getCurToken().getValue())){
            if (getCurToken().getValue().equals("do")){
                complieDo();
            } else if (getCurToken().getValue().equals("if")){
                compileIf();
            } else if (getCurToken().getValue().equals("while")){
                compileWhile();
            } else if (getCurToken().getValue().equals("return")){
                compileReturn();
            } else if (getCurToken().getValue().equals("let")){
                compileLet();
            } else {
                throw new CompilerException("Invalid statement key words",getCurToken().getLineNumber(),getCurToken().getColumNumber());
            }
        }

        prevDepth();
        complieOut("</statements>");

    }

    /**
     * Compile a let statement
     * Form: 'let' varName ('['expression']')? '=' expression ';'
     * Exp: let a[i] = Keyboard.readInt("ENTER THE NEXT NUMBER: ");
     */
    public void compileLet() throws CompilerException{
        complieOut("<letStatement>");
        nextDepth();
        // parse let
        if ((getCurToken().getType()==JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("let"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing let in the let statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        //parse var name
        if ((getCurToken().getType()==JackTokenizer.Token.Type.identifier)){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing or invalid var name in let statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse expression '[expression]' ,exp:  let a[i] = Keyboard.readInt("ENTER THE NEXT NUMBER: ");
        // parse '['
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("["))){
            complieOutElement(getCurToken());
            moveNext();
            // compile expression
            compileExpression();
            // compile the ']'
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("]"))){
                complieOutElement(getCurToken());
                moveNext();
            } else {
                throw new CompilerException("Missing ] in the let statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
            }
        }

        // compile '='
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("="))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing = in the let statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        //compile expression
        compileExpression();

        // compile ';'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing ; in the let statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        prevDepth();
        complieOut("</letStatement>");

    }

    /**
     * Compile if statement, including 'else'
     * Form: 'if' '(' expression ')' '{' statements'}' ('else' '{' statements '}' )?
     */
    public  void compileIf() throws CompilerException{
        complieOut("<ifStatement>");
        nextDepth();
        // parse 'if '
        if ((getCurToken().getType()==JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("if"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing if in the if statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parser '('
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("("))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing ( in the if statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse expression
        compileExpression();

        // parse ')'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(")"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing ) in the if statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse '{'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("{"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing { in the if statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse statement
        compileStatements();


        // parse '}'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("}"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing } in the if statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse 'else' if exists
        if ((getCurToken().getType()==JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("else"))){
            // parse 'else'
            complieOutElement(getCurToken());
            moveNext();
            // parse '{'
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("{"))){
                complieOutElement(getCurToken());
                moveNext();
            } else {
                throw new CompilerException("Missing { in the if else statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
            }

            // parse the statement
            compileStatements();
            // parse '}'
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("}"))){
                complieOutElement(getCurToken());
                moveNext();
            } else {
                throw new CompilerException("Missing } in the if else  statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
            }
        }

        prevDepth();
        complieOut("</ifStatement>");

    }

    /**
     * Compile a while statement
     * parm :  'while' '(' expression ')' '{' statements'}'
     */
    public void compileWhile() throws CompilerException{
        complieOut("<whileStatement>");
        nextDepth();
        // parse while
        if ((getCurToken().getType()==JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("while"))){
            complieOutElement(getCurToken());
            moveNext();
        }
        // parse '("
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("("))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing ( in the while statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // parse expression
        compileExpression();
        // parse ')"
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(")"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing ) in the while statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // parse '{'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("{"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing { in the while statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // compile statement
        compileStatements();
        // parse '}'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("}"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing } in the while statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        prevDepth();
        complieOut("</whileStatement>");
    }

    /**
     * Compile a do statement
     * Form: 'do' subroutineCall
     * subroutineCall: subroutineName'(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'
     * Exp: do Output.printString("THE AVERAGE IS: ");
     */
    public void complieDo() throws CompilerException{
        complieOut("<doStatement>");
        nextDepth();
        // parse do
        if ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("do"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing do in do statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // parse subroutine name before '('
        while (!((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("(")))){
            complieOutElement(getCurToken());
            moveNext();
        }
        // parse '('
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("("))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing ( in the do statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // parse expression list
        compileExpressionList();
        // parse ')'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(")"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing ) in the do statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // parse ';'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing ; in the do statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        prevDepth();
        complieOut("</doStatement>");
    }

    /**
     * Compile a return statement
     *  Form: 'return' expression? ';'
     */
    public void compileReturn() throws CompilerException{
        complieOut("<returnStatement>");
        nextDepth();
        // parse return
        if ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("return"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing return in return  statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse expression if exists , parse if not ';'
        if(!(getCurToken().getType() == JackTokenizer.Token.Type.symbol && getCurToken().getValue().equals(";"))){
            compileExpression();
        }

        // parse the end sign ';'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";"))){
            complieOutElement(getCurToken());
            moveNext();
        } else {
            throw new CompilerException("Missing ; in return  statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        prevDepth();
        complieOut("</returnStatement>");
    }

    /**
     * Compiles an expression
     * Form: term (op term)*
     * Exp: size = Asize;     ,  b = 4+c+d
     */
    public void compileExpression() throws CompilerException{
        complieOut("<expression>");
        nextDepth();
        // compile term
        compileTerm();
        // parse op (operations) ,exp: +,- , and expressions , exp: b = (2+5) * (a-b)
        while ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && operationSets.contains(getCurToken().getValue())){
            // parse the op
            complieOutElement(getCurToken());
            moveNext();
            // parse term
            compileTerm();
        }
        prevDepth();
        complieOut("</expression>");
    }

    /**
     * Compile term
     * Form :  integerConstant | StringConstant | keywordConstant | varName | varName'[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
     * Exp: Screen.drawRectangle(x, y, x, y);
     */
    public void compileTerm() throws CompilerException{
        complieOut("<term>");
        nextDepth();
        // parse int const
        if (getCurToken().getType() == JackTokenizer.Token.Type.integerConstant){
            complieOutElement(getCurToken());
            moveNext();
            // parse string const
        } else if(getCurToken().getType() == JackTokenizer.Token.Type.stringConstant) {
            complieOutElement(getCurToken());
            moveNext();
            // parse key word: true, false, null, this
        } else if ((getCurToken().getType() == JackTokenizer.Token.Type.keyword)){
            if ((termKeyWordSets.contains(getCurToken().getValue()))){
                complieOutElement(getCurToken());
                moveNext();
            } else {
                throw new CompilerException("invalid expression defined",getCurToken().getLineNumber(),getCurToken().getColumNumber());
            }

            // parse symbol
        } else if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol)){
            // if start with '(', then parse the sub expression
            if (getCurToken().getValue().equals("(")){
                complieOutElement(getCurToken());
                moveNext();
                // parse expression
                compileExpression();
                // parse ')'
                if (getCurToken().getValue().equals(")")){
                    complieOutElement(getCurToken());
                    moveNext();
                } else {
                    throw new CompilerException("invalid term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }
                // if starts with '-' or '~' , means the latter part is a term
            } else if((getCurToken().getValue().equals("-")) || (getCurToken().getValue().equals("~"))){
                complieOutElement(getCurToken());
                moveNext();
                // parse term
                compileTerm();
            } else {
                throw  new CompilerException("invalid term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
            }

            // Deal with identifier
        } else if((getCurToken().getType() == JackTokenizer.Token.Type.identifier)){
            // parse subroutineName or varName , exp: <identifier> count </identifier>
            complieOutElement(getCurToken());
            moveNext();

            // Deal with "[" , exp:  let a[i] = Keyboard.readInt("ENTER THE NEXT NUMBER: "); -> a[i]
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals("[")){
                // parse [
                complieOutElement(getCurToken());
                moveNext();
                // parse the sub expression
                compileExpression();
                // parse the ']'
                if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals("]")){
                    complieOutElement(getCurToken());
                    moveNext();
                } else {
                    throw new CompilerException("Missing ] in the term ",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }

            }

            // Deal with '.' , exp: Keyboard.readInt ...
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals(".")){
                // parse '.'
                complieOutElement(getCurToken());
                moveNext();

                // parse subroutine name , exp: .readInt
                if (getCurToken().getType() == JackTokenizer.Token.Type.identifier){
                    complieOutElement(getCurToken());
                    moveNext();
                } else {
                    throw  new CompilerException("Invalid subroutine name in term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }

                // parse '('
                if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals("(")){
                    complieOutElement(getCurToken());
                    moveNext();
                } else {
                    //System.out.println(getCurToken().getValue());
                    throw  new CompilerException("Missing ( in term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }

                // parse expression list , exp: do Screen.drawRectangle(x, y, x, y); -> (x,y,x,y)
                compileExpressionList();

                // parse ')'
                if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals(")")){
                    complieOutElement(getCurToken());
                    moveNext();
                } else {
                    throw  new CompilerException("Missing ) in term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }


            }

            // Deal with '(' ,exp: do draw();
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals("(")){
                // parse '('
                complieOutElement(getCurToken());
                moveNext();
                // compile expression list
                compileExpressionList();
                // parse ')'
                if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals(")")){
                    complieOutElement(getCurToken());
                    moveNext();
                } else {
                    throw new CompilerException("Missing ) in term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }

            }
        }
        prevDepth();
        complieOut("</term>");
    }

    /**
     * Compile the expression list , could be null
     * Parm: (expression (',', expresion)*)?
     * Exp: do Screen.drawRectangle(x, y, x, y);
     */
    public void compileExpressionList() throws CompilerException{
        complieOut("<expressionList>");
        nextDepth();
        // check if is ')' ,means the end
        if ((getCurToken().getType() ==  JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(")"))){
            prevDepth();
            complieOut("</expressionList>");
            return;
        }
        //  if not null, compile the first expression
        compileExpression();

        // parse the left expression lists , exp: ,y ,x
        while((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(","))){
            // parse the token
            complieOutElement(getCurToken());
            moveNext();
            // parse the expression
            compileExpression();
        }

        // end the parsing
        prevDepth();
        complieOut("</expressionList>");

    }








    public static void main(String[] args) throws CompilerException {
        //String cmdString = JackTokenizer.fileToString("/Users/futianshu/Desktop/nand2tetris/projects/10/Compliler_Part1/src/com/company/Main.jack");
        CompilationEngine compilationEngine = new CompilationEngine("/Users/futianshu/Desktop/nand2tetris/projects/10/Compliler_Part1/src/com/company/Main2.jack");
        //List tokenList = getTokenList();
        //System.out.println(tokenList);
        //JackTokenizer.Token curToken = getCurToken();
        //complieOutElement(curToken);
        //JackTokenizer.Token nextToken = getNextToken();
        //complieOutElement(nextToken);
       // complieClass();
        //System.out.println(outputXmlStr);
        //String tokens =  JackTokenizer.fileToString("/Users/futianshu/Desktop/nand2tetris/projects/10/Compliler_Part1/src/com/company/Main.jack");
        //System.out.println(tokens);
        //moveNext();
        //System.out.println(getCurToken());



    }
}
