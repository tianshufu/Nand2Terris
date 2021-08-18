

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

        private final Map<String,SymEntry> entryMap ;
        private final Map<Declaration.Scope,Integer> counters ;

        /**
         * Define the declaration of the symbols
         */
        public static class Declaration{
            public enum Scope{
                statiz("static"), field("this"), local, argument;
                private String str;

                Scope(String str) {
                    this.str = str;
                }

                Scope() {
                    this.str = name();
                }

                @Override
                public String toString() {
                    return str;
                }
            }

            private final String type;
            private final String name;
            private final Scope scope;

            public Declaration(String type,String name,Scope scope){
                this.type = type;
                this.name = name;
                this.scope = scope;


            }



            public String getType(){
                return type;
            }

            public String getName(){
                return name;
            }

            public Scope getScope(){
                return scope;
            }

        }


        public static class SymEntry{
            private Declaration declaration;
            private int index;

            public SymEntry(Declaration declaration, int index){
                this.declaration = declaration;
                this.index = index;
            }

            public Declaration declaration(){
                return declaration;
            }

            public int getIndex() {
                return index;
            }
        }

        /**Creates a new symbol table
         *
         */
        public SymbolTable(){
            this.entryMap = new HashMap<>();
            this.counters = new HashMap<>();
            counters.put(Declaration.Scope.statiz,0);
            counters.put(Declaration.Scope.field,0);
            counters.put(Declaration.Scope.argument,0);
            counters.put(Declaration.Scope.local,0);
        }

        /**Starts a new subroutine scope (resets the subroutine's symbol table)
         *
         */
        public void startSubroutine(){
            this.entryMap.clear();
            counters.put(Declaration.Scope.statiz,0);
            counters.put(Declaration.Scope.field,0);
            counters.put(Declaration.Scope.argument,0);
            counters.put(Declaration.Scope.local,0);
        }


        /**
         * Defines a new identifier of the given name, type and kind
         * and assigns it a running index. STATIC and FIELD identifiers
         * have a class scope, while ARG and VAR identifiers have a subroutine scope
         * @param name
         * @param type
         * @param scope
         */
        public void  define(String name, String type, Declaration.Scope scope){
            assert entryMap.containsKey(name): "duplicate variable name " ;
            Declaration declaration = new Declaration(type,name,scope);
            // get the index from the counter
            int counts = counters.get(scope);
            SymEntry symEntry = new SymEntry(declaration,counts);
            entryMap.put(name,symEntry);
            // update the counter
            counters.put(scope,counts+1);

        }

        /**
         * Returns the number of variables of the
         * given kind already defined in the current
         * scope
         * @param scope
         * @return
         */
        public int varCount (Declaration.Scope scope){
                return counters.get(scope);
        }

        /**
         * Returns the kind of the named identifier in the current scope.
         * If the identifier us unknown in the current scope, returns None
         * @param name
         * @return
         */
        public Declaration.Scope kindOf(String name ){
            if (entryMap.containsKey(name)){
                return entryMap.get(name).declaration().getScope();
            } else {
                return null;
            }

        }

        /**
         * Returns the type of the named identifier in the
         * current scope
         * @param name
         * @return
         */
        public String typeOf(String name){
            if (entryMap.containsKey(name)){
                return entryMap.get(name).declaration.getType();
            } else {
                return null;
            }
        }

        public int indexOf(String name){
            if (entryMap.containsKey(name)){
                return entryMap.get(name).getIndex();
            } else {
               return  -1;
            }
        }

    public static void main(String[] args) {
            SymbolTable symbolTable = new SymbolTable();
            symbolTable.define("x","int",Declaration.Scope.field);
            System.out.println(symbolTable.kindOf("x"));
            System.out.println(symbolTable.indexOf("x"));
            symbolTable.define("y","int",Declaration.Scope.field);
            System.out.println(symbolTable.indexOf("y"));
            symbolTable.define("pointCount","int",Declaration.Scope.statiz);
            System.out.println(symbolTable.indexOf("pointCount"));


    }




}
