import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

public class JackCompiler {
    //private final CompilationEngine compilationEngine;
    private final String outFilePath;
    private int cur;
    private String tokens;
    private final VmWriter vmWriter;
    private String className;
    private String subName;
    private String subKind;
    private String subType;
    private int ifLabelCount;
    private int whileLableCount;

    private final List tokenList;
    private final SymbolTable classTable;
    private final SymbolTable subTable;
    private final Hashtable<String, SymbolTable.Declaration.Scope> scopeTable;

    private static HashSet<String> statementSets;
    private static HashSet<String> termKeyWordSets;
    private static HashSet<String> operationSets;





    public JackCompiler(String inFilePath) throws CompilerException {
        //compilationEngine = new CompilationEngine(inFilePath);
        this.tokens = JackTokenizer.fileToString(inFilePath);
        this.tokenList = JackTokenizer.parseToken(tokens);
        this.cur = 0;
        vmWriter = new VmWriter();
        outFilePath = inFilePath.substring(0,inFilePath.indexOf(".jack"))+".vm";
        //tokenList = getTokenList();
        classTable = new SymbolTable();
        subTable = new SymbolTable();
        className = "";
        subName = "";
        subType="";
        subKind="";
        ifLabelCount = 0;
        whileLableCount = 0;
        scopeTable = new Hashtable<>();
        scopeTable.put("local",SymbolTable.Declaration.Scope.local);
        scopeTable.put("argument",SymbolTable.Declaration.Scope.argument);
        scopeTable.put("static",SymbolTable.Declaration.Scope.statiz);
        scopeTable.put("field",SymbolTable.Declaration.Scope.field);

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

    public String getOutFilePath(){ return outFilePath;}

    /**
     * Move to the cur token to the next one
     */
    public  void moveNext(){
        this.cur ++;
    }


    /**
     * Get prepared for while code
     * @return
     */
    public List<String> ifLable(){
        List<String> resList = new ArrayList<>();
        String ifEnd = "IF_END"+ifLabelCount;
        String ifS = "IF_"+ifLabelCount;
        resList.add(ifEnd);
        resList.add(ifS);
        ifLabelCount += 1;
        return  resList;
    }

    /**
     * Get prepared for while code
     * @return
     */
    public List<String> whileLable(){
        List<String> resList = new ArrayList<>();
        String expStr = "WHILE_EXP"+whileLableCount;
        String endStr = "WHILE_END"+whileLableCount;
        resList.add(expStr);
        resList.add(endStr);
        whileLableCount += 1;
        return resList;
    }

    private static void vmCompile(File f) throws CompilerException {
        if (f.getName().endsWith(".jack")) {
            String filePath = f.getPath();
            //CompilationEngine compilationEngine = new CompilationEngine(filePath);
            JackCompiler jackCompiler = new JackCompiler(filePath);

            jackCompiler.writeClassCode();
            // turn list to string
            String codesStr = ParseHelper.listToString(jackCompiler.vmWriter.getCodes());
            JackAnalyzer.writeFile(jackCompiler.getOutFilePath(),codesStr);

        }
    }


    /**
     * Write class code
     * Param: 'class' className '{'  classVarDec* subroutineDec* '}'
     * Rules 1: Handling variable's life cycle
     * 1.1"Static variables": Seen by all the class subroutines; must
     *  exist throughout the program's execution
     * 1.2"local variables": During run-time, each time a subroutine is invoked
     *  it must get a fresh set of local variables; each time a subroutine returns,
     *  its local variables must be recycled
     * 1.3"Argument variables": Same as local variables
     * 1.4 "Field variables": Unique to object-oriented languages
     *
     */
    public void writeClassCode() throws CompilerException{
        classTable.startSubroutine();
        // parse class
        moveNext();
        // parse class name
        className = getCurToken().getValue();
        moveNext();
        // parse symbol '{'
        moveNext();
        // parse class val des
        //parse class valDes, start parsing if the start token is "static" or "field"
        while((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && ((getCurToken().getValue().equals("static"))|| (getCurToken().getValue().equals("field")))){
            writeClassVarDecCode();
            // no need to move next here, since the function already did this
        }
        // parse class method
        // parse methods : "method", "constructor", "function"
        while ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && ((getCurToken().getValue().equals("method")) || (getCurToken().getValue().equals("constructor")) || (getCurToken().getValue().equals("function")))){
            subKind = getCurToken().getValue();
            //writeSubroutineBodyCode();
            writeSubroutineCode();
            //compileSubroutine();
        }

        // parse symbol '}'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("}"))){
            moveNext();
        } else {
            throw new CompilerException("missing } in class",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }


    }

    /**Write VM code for var declaration, update the symbol table
     * No code is generated here!
     * Form: 'var' type varName (',' varName)* ';'
     * Exp: field int direction;
     */
    public void writeClassVarDecCode(){
        // Initial the parameters
        String varName;
        String varType;
        SymbolTable.Declaration.Scope scope;
        // parse key word ,exp: field
        String varKindStr  =  getCurToken().getValue();
        scope = scopeTable.get(varKindStr);
        moveNext();
        //parse type, exp: int
        varType = getCurToken().getValue();
        moveNext();
        // parse val name
        varName = getCurToken().getValue();
        moveNext();
        // update the table
        classTable.define(varName,varType,scope);

        // parse left val name, example: field int x,y;
        while(!((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";")))){
            // parse symbol ","
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(","))){
                moveNext();
            }
            // parse val name
            if (getCurToken().getType() == JackTokenizer.Token.Type.identifier){
                varName = getCurToken().getValue();
                classTable.define(varName,varType,scope);
                moveNext();
            }
        }
        // parse symbol ';'
        moveNext();
    }

    /**
     * Compile subroutine
     * Param: ('function'|'method'| 'constructor') ('void' | type) subroutineName '(' parameterList ')' subroutineBody
     * Exp1:  constructor Square new(int Ax, int Ay, int Asize){ ...}
     * Exp2:  method int distance(Point other)
     * @throws CompilerException
     */
    public void writeSubroutineCode() throws CompilerException{
        int fieldCount = 0;
        subTable.startSubroutine();

        // parse key word
        subKind = getCurToken().getValue();
        // only parse when the token is: 'keyword'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && ((getCurToken().getValue().equals("method")) )){
            classTable.define("this",className,scopeTable.get("argument"));
        }
        moveNext();

        // parse type, exp: int , store the type in the 'subType' , use it when parsing the sub
        this.subType = getCurToken().getValue();
        moveNext();

        // parse subroutine name
        this.subName = getCurToken().getValue();
        moveNext();


        // parse "("
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("("))){
            moveNext();
        } else {
            System.out.println(getCurToken().getValue()+" : line num:"+getCurToken().getLineNumber()+" col num:"+getCurToken().getColumNumber());
            throw new CompilerException("missing ( in subroutine",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }


        // parse parameters list
        writeParameterListCode();

        // parse ")"
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(")"))){
            moveNext();
        } else {
            throw new CompilerException("missing ) in subroutine",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // write code for subRoutine body
        writeSubroutineBodyCode();




    }


    /**
     * Write code for  a subroutine body
     * param :  '{' varDec* statements '}'
     * Compiling constructors:
     * 1. The compiler create the subroutine's symbol table
     * 2. figure out the size of an object of this class(n) (based on the field count)
     * and wrties code that class Memory.alloc(n),this OS method finds a memory block
     * required size, and returns its base address
     * 3. Anchor this at the base dress
     *
     */
    public void  writeSubroutineBodyCode() throws CompilerException{
        int nLocals = 0;
        // parse '{'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("{"))){
            moveNext();
        } else {
            throw new CompilerException("missing { in  subroutine body",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse var declaration  ,exp:  var Array a;
        while ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("var"))){
            // no need to move next here, since the sub function already does this
            nLocals += writeVarDeCode();
        }

        // define function, the subName is passed from it's parent process
        String functionName = this.className+"."+this.subName;
        // write the function
        vmWriter.writeFunction(functionName,nLocals);
        if(this.subKind.equals("constructor")){
            // get the field count
            int fieldCount = classTable.varCount(SymbolTable.Declaration.Scope.field);
            vmWriter.writePush("constant",fieldCount);
            vmWriter.writeCall("Memory.alloc",1);
            vmWriter.writePop("pointer",0);
        } else if(this.subKind.equals("method")){
            vmWriter.writePush("argument",0);
            vmWriter.writePush("pointer",0);
        }
        // parse statements
        writeStatementsCode();

        // parse '}'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("}"))){
            moveNext();
        } else {
            System.out.println(getCurToken().getValue()+" : line num:"+getCurToken().getLineNumber()+" col num:"+getCurToken().getColumNumber());
            throw new CompilerException("missing } in  subroutine body",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }


    }

    /**
     *
     * Writes code for  a sequence of statements, does not handle '{' '}'
     *
     /* @throws CompilerException
     */
    public void writeStatementsCode() throws CompilerException{
        // if the cur token is "do","if","while","let,"return"
        while (getCurToken().getType()==JackTokenizer.Token.Type.keyword && statementSets
                .contains(getCurToken().getValue())){
            if (getCurToken().getValue().equals("do")){
                writeDoCode();
            } else if (getCurToken().getValue().equals("if")){
                writeIfCode();
            } else if (getCurToken().getValue().equals("while")){
                writeWhileCode();
            } else if (getCurToken().getValue().equals("return")){
                writeReturnCode();
            } else if (getCurToken().getValue().equals("let")){
                writeLetCode();
            } else {
                throw new CompilerException("Invalid statement key words",getCurToken().getLineNumber(),getCurToken().getColumNumber());
            }
        }
    }

    /**
     * Write code for a do statement
     * Form: 'do' subroutineCall
     * subroutineCall: subroutineName'(' expressionList ')' | (className | varName) '.' subroutineName '(' expressionList ')'
     * Exp: do Output.printString("THE AVERAGE IS: ");
     * Compiling subroutine call: subName (arg1,arg2,...); THe caller (a VM function)
     * must push the argument arg1,arg2,..., onto the stack, and then
     * call the subroutine
     */
    public void writeDoCode() throws CompilerException{
        List<String> funcName = new ArrayList<>();
        int argsCount = 0;
        String varName;
        String varType;
        SymbolTable.Declaration.Scope varKind;
        int varId;
        // check and parse do
        if ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("do"))){
            moveNext();
        } else {
            throw new CompilerException("Missing do in do statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse subroutine name before '('
        while (!((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("(")))){
            //complieOutElement(getCurToken())
            // add the subroutine name to the func name
            if(getCurToken().getType() != JackTokenizer.Token.Type.symbol){
                funcName.add(getCurToken().getValue());
            }
            moveNext();
        }

        // parse '('
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("("))){

            moveNext();
        } else {
            throw new CompilerException("Missing ( in the do statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // sub call
        String callName = "";
        varName = funcName.get(0);
        // if only one func has been called ,exp: do  moveSquare();
        if (funcName.size() == 1){
            vmWriter.writePush("pointer",0);
            callName = className+"."+varName;
            argsCount += 1;
            // else if there is a sub process, exp: do Output.printString("THE AVERAGE IS: ");
        } else if(funcName.size() == 2){
            if (subTable.indexOf(varName) >= 0){
                varType = subTable.typeOf(varName);
                varKind =subTable.kindOf(varName);
                varId = subTable.indexOf(varName);
                callName = varType+"."+funcName.get(1);
                argsCount += 1;
                pushBasedOnType(varKind,varId);

            } else if(classTable.indexOf(varName) >= 0){
                varType = classTable.typeOf(varName);
                varKind = classTable.kindOf(varName);
                varId = classTable.indexOf(varName);
                callName = varType+"."+funcName.get(1);
                argsCount += 1;
                // push the variable based on the type
                pushBasedOnType(varKind,varId);

            } else {
                callName = funcName.get(0)+"."+funcName.get(1);
            }
        }

        // parse expression list
        argsCount += writeExpressionListCode();

        // check and parse ')'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(")"))){
            moveNext();
        } else {
            throw new CompilerException("Missing ) in the do statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // check and parse ';'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";"))){
            moveNext();
        } else {
            throw new CompilerException("Missing ; in the do statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // write call name
        vmWriter.writeCall(callName,argsCount);
        vmWriter.writePop("temp",0);

    }

    /**
     * Write code for a let statement
     * Form: 'let' varName ('['expression']')? '=' expression ';'
     * Exp: let a[i] = Keyboard.readInt("ENTER THE NEXT NUMBER: ");
     * Rules:
     * 1. General solution for generating array access
     * // let arr[expression1] = expression2
     *  push arr  -> VM code for computing and pushing the value of expression1 -> add ->
     *  VM code for computing and pushing the value of expression2  -> pop temp 0 (temp 0 = the value of the expression2)
     *  -> pop pointer1 -> push temp 0 -> pop that 0
     */
    public void writeLetCode() throws CompilerException{
        String varName = "";
        String varType = "";
        SymbolTable.Declaration.Scope varKindScope;
        int varIdx = 0;
        boolean isArray = false;
        // check and parse let
        if ((getCurToken().getType()==JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("let"))){
            moveNext();
        } else {
            throw new CompilerException("Missing let in the let statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse varName
        varName = getCurToken().getValue();
        // search the var name in the table, i
        if (subTable.indexOf(varName) >= 0){
            varType = subTable.typeOf(varName);
            varKindScope = subTable.kindOf(varName);
            varIdx = subTable.indexOf(varName);
        } else if (classTable.indexOf(varName) >= 0){
            varType = classTable.typeOf(varName);
            varKindScope =classTable.kindOf(varName);
            varIdx = classTable.indexOf(varName);
        } else {
            throw new CompilerException("invalid var statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // move next
        moveNext();

        // parse expression '[expression]' ,exp:  let a[i] = Keyboard.readInt("ENTER THE NEXT NUMBER: ");
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("["))){
             isArray = true;
             // parse '['
             moveNext();
            // compile expression
            writeExpressionCode();

            // parse and write code for the array , push the base address of the array to the stack
            pushBasedOnType(varKindScope,varIdx);
            vmWriter.writeArithmetic("+");

            // parse and compile ']'
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("]"))){
                moveNext();
            } else {
                throw new CompilerException("Missing ] in the let statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
            }
        }

        // parse and write code for '='
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("="))){
            moveNext();
        } else {
            throw new CompilerException("Missing = in the let statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse expression
        writeExpressionCode();
        // pop value from the stack
        if (isArray){
            vmWriter.writePop("temp",0);
            vmWriter.writePop("pointer",1);
            vmWriter.writePush("temp",0);
            vmWriter.writePop("that",0);
        } else {
            if(varKindScope == null){
                System.out.println("varname:"+varName);
            }
            //System.out.println("varkind scope:"+varKindScope.toString());
            //System.out.println(getCurToken().getValue()+" : line num:"+getCurToken().getLineNumber()+" col num:"+getCurToken().getColumNumber());
            popBasedOnType(varKindScope,varIdx);
        }

        // parse ';'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";"))){
            moveNext();
        } else {
            throw new CompilerException("Missing ; in the let statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

    }

    /**
     * Compile code  a while statement
     * parm :  'while' '(' expression ')' '{' statements'}'
     * Rules:
     *  label L1
     *           compiled (expression)
     *           not
     *          if-goto L2
     *          compiled(statements)
     *          goto L1
     *  lable L2
     */
    public void writeWhileCode() throws CompilerException{
        // Get prepared for 'L1' and 'L2'
        List<String> labelList = whileLable();
        // parse 'while'
        moveNext();
        // parse '("
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("("))){
            moveNext();
        } else {
            throw new CompilerException("Missing ( in the while statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse expression
        // write 'label L1 '
        vmWriter.writeLabel(labelList.get(0));
        // write expression
        writeExpressionCode();
        // write not
        vmWriter.writeSigArithmetic("~");
        // write 'if-goto L2'
        vmWriter.writeIf(labelList.get(1));

        //check and  parse ')'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(")"))){
            moveNext();
        } else {
            throw new CompilerException("Missing ) in the while statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse '{'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("{"))){
            moveNext();
        } else {
            throw new CompilerException("Missing { in the while statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse and write codes for statement
        writeStatementsCode();
        // write code for goto 'L1'
        vmWriter.writeGoto(labelList.get(0));
        // write label 'L2'
        vmWriter.writeLabel(labelList.get(1));

        // parse '}'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("}"))){
            moveNext();
        } else {
            throw new CompilerException("Missing } in the while statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

    }


    /**
     * Compile a return statement
     *  Form: 'return' expression? ';'
     */
    public void writeReturnCode() throws CompilerException{
        // parse 'return'
        moveNext();

        // parse expression if exists , parse if not ';'
        if(!(getCurToken().getType() == JackTokenizer.Token.Type.symbol && getCurToken().getValue().equals(";"))){
            writeExpressionCode();
        }

        // parse the end sign ';'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";"))){
            moveNext();
        } else {
            throw new CompilerException("Missing ; in return  statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // check the type
        if (subType.equals("void")){
            vmWriter.writePush("constant",0);
        }

        vmWriter.writeReturn();
    }



    /**
     * Write code for if statement, including 'else'
     * Form: 'if' '(' expression ')' '{' statements'}' ('else' '{' statements '}' )?
     * Rules:
     *      if(expression)
     *          statements1
     *      else
     *          statements2
     */
    public void writeIfCode() throws CompilerException{
        // get the if label
        List<String> ifLabelList = ifLable();
        // parse if
        moveNext();

        // parser '('
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("("))){
            moveNext();
        } else {
            throw new CompilerException("Missing ( in the if statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // parse expression
        writeExpressionCode();
        //vmWriter.writeSigArithmetic("~");

        // parse ')'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(")"))){
            moveNext();
        } else {
            throw new CompilerException("Missing ) in the if statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse '{'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("{"))){
            moveNext();
        } else {
            throw new CompilerException("Missing { in the if statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // parse statement1, write "IF_i"
        vmWriter.writeIf(ifLabelList.get(1));
        // write statement code
        writeStatementsCode();
        // write goto code
        vmWriter.writeGoto(ifLabelList.get(0));

        // check and  parse '}'
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("}"))){
            moveNext();
        } else {
            throw new CompilerException("Missing } in the if statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        vmWriter.writeLabel(ifLabelList.get(1));
        // parse 'else' if exists
        if ((getCurToken().getType()==JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("else"))) {
            // parse 'else'
            moveNext();
            // check and parse '{'
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("{"))){
                moveNext();
            } else {
                throw new CompilerException("Missing { in the if else statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
            }
            // parse statements
            writeStatementsCode();
            // parse '}'
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals("}"))){
                moveNext();
            } else {
                throw new CompilerException("Missing } in the if else  statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
            }
        }

        vmWriter.writeLabel(ifLabelList.get(0));

        }


    /**
     * Write the push code based on the type of the varKind
     * @param varKindScope
     */
    private void pushBasedOnType (SymbolTable.Declaration.Scope varKindScope,int varIdx) throws CompilerException {
        if (varIdx<0){
            throw new CompilerException("invalid varidx in the table",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        // push the variable based on the type
        if (varKindScope.equals(SymbolTable.Declaration.Scope.field)){
            vmWriter.writePush("this",varIdx);
        } else if(varKindScope.equals(SymbolTable.Declaration.Scope.local)){
            vmWriter.writePush("local",varIdx);
        } else if(varKindScope.equals(SymbolTable.Declaration.Scope.statiz)){
            vmWriter.writePush("static",varIdx);
        } else if(varKindScope.equals(SymbolTable.Declaration.Scope.argument)){
            vmWriter.writePush("argument",varIdx);
        }

    }

    /**
     * Write the pop code based on the type of the varKind
     * @param varKindScope
     */
    private void popBasedOnType(SymbolTable.Declaration.Scope varKindScope,int varIdx) throws CompilerException{
        if(varKindScope == null){
            throw new CompilerException("missing varkind type ",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        if (varIdx<0){
            throw new CompilerException("invalid varidx in the table",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }
        //System.out.println(getCurToken()+"  "+varKindScope.toString()+" "+varIdx +getCurToken().getLineNumber()+" "+getCurToken().getColumNumber());
        // push the variable based on the type
        if (varKindScope.equals(SymbolTable.Declaration.Scope.field)){
            vmWriter.writePop("this",varIdx);
        } else if(varKindScope.equals(SymbolTable.Declaration.Scope.local)){
            vmWriter.writePop("local",varIdx);
        } else if(varKindScope.equals(SymbolTable.Declaration.Scope.statiz)){
            vmWriter.writePop("static",varIdx);
        } else if(varKindScope.equals(SymbolTable.Declaration.Scope.argument)){
            vmWriter.writePop("argument",varIdx);
        }
    }


    /**
     * Compile code for  the expression list , could be null
     * Parm: (expression (',', expresion)*)?
     * Exp: do Screen.drawRectangle(x, y, x, y);
     * @return
     */
    public int writeExpressionListCode() throws CompilerException {
        int argsCount = 0;
        // check if is ')' ,means the end
        if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(")"))) {
            return argsCount;
        }
        // parse the first expression
        argsCount += 1;
        writeExpressionCode();

        // parse the rest (',' expression)*
        while (getCurToken().getType()==JackTokenizer.Token.Type.symbol && getCurToken().getValue().equals(",")){
            // parse the ','
            moveNext();
            // parse expression
            writeExpressionCode();
            // update the args count
            argsCount += 1;
        }
        return argsCount;
    }

    /**
     * Wrtie code for  an expression
     * Form: term (op term)*
     * Exp: size = Asize;     ,  b = 4+c+d
     * Rules:
     * 1. if exp is number n: output "push n"
     * 2. if exp is a variable var : output "push var"
     * 3. if exp is exp1 op exp2 : codeWrite(exp1),codeWrite(exp2), output 'op'
     * 4. if exp is op exp : codeWrite(exp), output "op"
     * 5. if exp is "f(exp1,exp2,...)" : codeWrite(exp1),codeWrite(exp2),..., output 'call f '
     *
     */
    public void writeExpressionCode() throws CompilerException{
        // parse term
        writeTermCode();

        // parse op
        while ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && operationSets.contains(getCurToken().getValue())){
            // get the op
            String op = getCurToken().getValue();
            moveNext();
            // write code for term
            writeTermCode();
            // write code for op
            // Check '*' and '/' because these two ops need to use system call
            if (op.equals("*")){
                vmWriter.writeCall("Math.multiply",2);
            } else if (op.equals("/")){
                vmWriter.writeCall("Math.divide",2);
            } else {
                vmWriter.writeArithmetic(op);
            }

        }

    }


    /**
     * Compile code for term
     * Form :  integerConstant | StringConstant | keywordConstant | varName | varName'[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
     * Exp: Screen.drawRectangle(x, y, x, y);
     * Rules:
     * 1. String constants are created using the OS constructor String.new(length)
     * 2. For key word 'true', is mapped on -1, can be obtained by push 1 followed by neg
     **/
    public void writeTermCode() throws CompilerException{
        String varName;
        String varType ="";
        SymbolTable.Declaration.Scope varKindScope;
        int varId;
        int parmCount = 0;
        // parse int const
        if (getCurToken().getType() == JackTokenizer.Token.Type.integerConstant){
            String intValStr = getCurToken().getValue();
            Integer intVal = Integer.parseInt(intValStr);
            // write the int code
            vmWriter.writePush("constant",intVal);
            moveNext();
            // parse string const
        } else if(getCurToken().getType() == JackTokenizer.Token.Type.stringConstant) {
            String strVal = getCurToken().getValue();
            vmWriter.writePush("constant",strVal.length());
            vmWriter.writeCall("String.new",1);
            for(int i = 0; i<strVal.length(); i++){
                    int ithAsc = strVal.charAt(i);
                    vmWriter.writePush("constant",ithAsc);
                    vmWriter.writeCall("String.appendChar",2);
            }
            moveNext();
            // parse key word: true, false, null, this
        } else if ((getCurToken().getType() == JackTokenizer.Token.Type.keyword)){
            if ((termKeyWordSets.contains(getCurToken().getValue()))){
                if(getCurToken().getValue().equals("true")){
                    vmWriter.writePush("constant",0);
                    vmWriter.writeSigArithmetic("~");
                } else if(getCurToken().getValue().equals("false")){
                    vmWriter.writePush("constant",0);
                } else if(getCurToken().getValue().equals("null")){
                    vmWriter.writePush("constant",0);
                } else if(getCurToken().getValue().equals("this")){
                    vmWriter.writePush("pointer",0);
                }
                moveNext();
            } else {
                throw new CompilerException("invalid expression defined", getCurToken().getLineNumber(), getCurToken().getColumNumber());
            }
            // parse symbols
        } else if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol)){

            // if start with '(', then parse the sub expression
            if (getCurToken().getValue().equals("(")){
                moveNext();
                // write expression code
                writeExpressionCode();
                // parse ')'
                if (getCurToken().getValue().equals(")")){
                    moveNext();
                } else {
                    throw new CompilerException("invalid term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }
                // if starts with '-' or '~' , means the latter part is a term
            } else if((getCurToken().getValue().equals("-")) || (getCurToken().getValue().equals("~"))){
                // parse unaryOp , exp: '-'
                String op = getCurToken().getValue();
                moveNext();
                // parse term
                writeTermCode();
                // write the Op
                vmWriter.writeSigArithmetic(op);
            }
        } else if((getCurToken().getType() == JackTokenizer.Token.Type.identifier)){
            // parse var name ,exp: let a[i] = 5 ;   -> "a"
            String termName = getCurToken().getValue();
            moveNext();

            // Deal with "[" , exp:  let a[i] = Keyboard.readInt("ENTER THE NEXT NUMBER: "); -> a[i]
            if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals("[")){
                // parse subroutineName or varName , first check the subTable
                if(subTable.indexOf(termName) >=0 ){
                    varType = subTable.typeOf(termName);
                    varKindScope =subTable.kindOf(termName);
                    varId = subTable.indexOf(termName);
                } else if(classTable.indexOf(termName) >=0 ){
                    varType = classTable.typeOf(termName);
                    varKindScope = classTable.kindOf(termName);
                    varId = classTable.indexOf(termName);
                } else {
                    throw new CompilerException("invalid var statement in term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }

                // parse '['
                moveNext();
                // parse expresion
                writeExpressionCode();
                // parse var type
                pushBasedOnType(varKindScope,varId);
                // create 'add' code
                vmWriter.writeArithmetic("+");
                vmWriter.writePop("pointer",1);
                vmWriter.writePush("that",0);

                // check and parse the ']'
                if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals("]")){
                    moveNext();
                } else {
                    throw new CompilerException("Missing ] in the term ",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }


                // Deal with '.' ,exp: exp: Keyboard.readInt ...
            } else if((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals(".")){
                // parse '.'
                moveNext();

                // parse subroutine name , exp: .readInt
                String termFunc;
                if (getCurToken().getType() == JackTokenizer.Token.Type.identifier){
                   termFunc = getCurToken().getValue();
                    moveNext();
                } else {
                    throw  new CompilerException("Invalid subroutine name in term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }

                // write code for the call name
                // if could be found in the sub table

                if(subTable.indexOf(termName) >=0 ){
                    varType = subTable.typeOf(termName);
                    varKindScope = subTable.kindOf(termName);
                    varId = subTable.indexOf(termName);
                    parmCount += 1;
                    pushBasedOnType(varKindScope,varId);
                } else if(classTable.indexOf(termName) >=0 ){
                    varType = classTable.typeOf(termName);
                    varKindScope = classTable.kindOf(termName);
                    varId = classTable.indexOf(termName);
                    parmCount += 1;
                    pushBasedOnType(varKindScope,varId);
                } else {
                    varName = termName;
                }

                // parse '('
                if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals("(")){
                    moveNext();
                } else {
                    throw  new CompilerException("Missing ( in term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }

                // parse expression list , exp: do Screen.drawRectangle(x, y, x, y); -> (x,y,x,y)
                parmCount += writeExpressionListCode();

                // parse ')'
                if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals(")")){
                    moveNext();
                } else {
                    throw  new CompilerException("Missing ) in term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }

                // write var code
                String callName = termName+"."+termFunc;
                vmWriter.writeCall(callName,parmCount);

             // Deal with '(' ,exp: do draw();
            } else if((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals("(")){
                // parse '('
                moveNext();
                // parse expression list
                vmWriter.writePush("pointer",0);
                // update the param count
                parmCount = writeExpressionListCode()+1;

                // parse ')'
                if ((getCurToken().getType() == JackTokenizer.Token.Type.symbol) && getCurToken().getValue().equals(")")){
                    moveNext();
                } else {
                    throw new CompilerException("Missing ) in term",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }


            } else {
                // parse subroutine name or var name
                if (subTable.indexOf(termName) >=0 ){
                    varType = subTable.typeOf(termName);
                    varKindScope = subTable.kindOf(termName);
                    varId = subTable.indexOf(termName);
                } else if (classTable.indexOf(termName) >=0 ){
                    varType = classTable.typeOf(termName);
                    varKindScope = classTable.kindOf(termName);
                    varId = classTable.indexOf(termName);
                } else {
                    throw new CompilerException("invalid var statement",getCurToken().getLineNumber(),getCurToken().getColumNumber());
                }
                // write the push code
                pushBasedOnType(varKindScope,varId);
            }

            }

        }




    /**Compiles code for a var declaration
     * Form: 'var' type varName (',' varName)* ';'
     * Exp1 : var int length;
     * Exp2: var int i,j ;
     * @return
     */
    public int writeVarDeCode() throws CompilerException{
        int nLocals = 1 ;
        String varName;
        String varKind ="local";
        String varType;
        // check and parse key word Var
        if ((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && (getCurToken().getValue().equals("var"))){
            moveNext();
        } else {
            throw new CompilerException("invalid var defined",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        // parse var type, exp: 'int'
        varType = getCurToken().getValue();
        moveNext();

        // parse var name, exp: length
        varName = getCurToken().getValue();
        moveNext();

        // update the symbol table
        subTable.define(varName,varType,scopeTable.get(varKind));

        // parse the rest of the vars
        // parse key words before the syntax  ';'
        while (!((getCurToken().getType()==JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";")))){
            //parse and skip syntax ','  get to the var name
            moveNext();
            // get the var name
            varName = getCurToken().getValue();
            subTable.define(varName,varType,scopeTable.get(varKind));
            // update the variable count
            nLocals += 1 ;
            // keep moving
            moveNext();
        }

        // check  and skip the end ';'
        if ((getCurToken().getType()==JackTokenizer.Token.Type.symbol) && (getCurToken().getValue().equals(";"))){
            moveNext();
        } else {
            //System.out.println(getCurToken().getValue()+" : line num:"+getCurToken().getLineNumber()+" col num:"+getCurToken().getColumNumber());
            throw new CompilerException("Missing end symbol ; in the var declaration",getCurToken().getLineNumber(),getCurToken().getColumNumber());
        }

        return nLocals;

    }

    /**
     * Write code for parameter lists , update the table and returns the number of parameter
     * Forms: ((type varName) (',' type varName)*)?
     * Exp: Square new(int Ax, int Ay, int Asize)
     */
    public int writeParameterListCode(){
        String varName = "";
        //SymbolTable.Declaration.Scope =
        String varKind = "argument";
        String varType = "";
        int paramCount = 0 ;
        // while not reaching the end of the list ')'
        while (!getCurToken().getValue().equals(")")){

            // parse first element type, exp: int or char or void or boolean
            if((getCurToken().getType() == JackTokenizer.Token.Type.keyword) && ((getCurToken().getValue().equals("int")) || (getCurToken().getValue().equals("char")) ||  (getCurToken().getValue().equals("boolean") || (getCurToken().getValue().equals("void"))))){
                varType = getCurToken().getValue();
                moveNext();
            }

            // parse element name , exp: Ax
            if(getCurToken().getType() == JackTokenizer.Token.Type.identifier){
                varName = getCurToken().getValue();
                moveNext();
                // update the count
                paramCount +=1 ;
            }

            // add the param to the Symbol table
            classTable.define(varName,varType,scopeTable.get(varKind));

            // check ','
            if (getCurToken().getType() == JackTokenizer.Token.Type.symbol && getCurToken().getValue().equals(",")){
                moveNext();
            } else {
                break;
            }
        }

        return paramCount;

    }

    public static void main(String[] args) throws CompilerException {
        /**
        JackCompiler jackCompiler = new JackCompiler("/Users/futianshu/Desktop/nand2tetris/projects/10/compiler_part1_backoup/src/test/Main.jack");
        //System.out.println(jackCompiler.tokenList);
        jackCompiler.writeClassCode();
        System.out.println(jackCompiler.vmWriter.getCodes());
        //jackCompiler.complieClass();
        //CompilationEngine compilationEngine = new CompilationEngine("Users/futianshu/Desktop/nand2tetris/projects/10/Compliler_Part1/src/com/company/Main2.jack");
        //List<String> tokenList = jackCompiler.getTokenList();
        //List<String> codes = jackCompiler.vmWriter.getCodes();
        //System.out.println(getTokenList());

        //String tokens =  JackTokenizer.fileToString("/Users/futianshu/Desktop/nand2tetris/projects/10/Compliler_Part1/src/com/company/Main.jack");
        //System.out.println(tokens);
         **/
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
                    vmCompile(file);
                }
            }
        } else if (f.isFile()) {
            vmCompile(f);
        }


    }


}
