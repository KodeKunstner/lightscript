package murlen.util.fscriptME;

import java.util.Hashtable;
import java.util.Vector;
import java.lang.*;

/**
 * <b>Parser - Does the parsing - i.e it's the brains of the code.</b>
 * <p>
 * <I>Copyright (C) 2002 murlen.</I></p>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.</p>
 * <p>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.</p>
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.,59 Temple Place, Suite 330, Boston MA 0211-1307 USA
 * </p>
 * @author murlen
 * @version 0.5
 *
 ** modifications by Joachim Van der Auwera
 * 14.08.2001 added support for indexed variables
 * 20.08.2001 -clean handling of setVar with null value
 *   - cleaner handling of if with null condition
 *   - make sure running empty script does nothing
 *   - extra info when throwing an exception (with surrounding lines)
 *   - changed operator prioritues for && and ||
 *   - fixed bug in parseIf with handling of nesting of if clauses with else
 *   - check for missing endif or endwhile (caused infinit loops)
 *   - check for a null to prevernt excepion in parseExpr
 * 28.08.2001
 *   - call to host.getVar() replaced by host.getVarEntry() (and added
 *     proper exception handling, in that case)
 * 31.08.2001
 *   - test on if condition being of correct type re-introduced
 * 10.09.2001
 *   - added < <= > >= on strings
 */
class Parser {
    
    
    //simple data class used internally to store function defs
    class FuncEntry {
        int startLine; //start line of function
        int endLine; //end line of function
        Vector paramNames; //list of parameter names
        Hashtable params; //Hashtable of parameters
        
        public FuncEntry() {
            startLine=0;
            endLine=0;
            paramNames=new Vector(4);
            params=new Hashtable();
        }
        
        public String toString() {
            String s=new String();
            
            s=startLine + " " ;
            s=s+endLine + " ";
            s=s+paramNames + " ";
            s=s+params;
            
            return s;
        }
    }
    
    //exception that occurs when someone calls return
    class RetException extends FSException {}
    
    //exception thrown by call to exit
    class ExitException extends RetException{}
    
    private LineLoader code; //the code
    private LexAnn tok; //tokenizer
    
    private int maxLine;
    private Hashtable vars; //function local variables
    private Hashtable gVars; //global variables
    private static Hashtable opPrio; //operator priority table
    private FScript host; //link to hosting FScript object
    private Hashtable funcs; //function map
    private Object retVal=null; //return value
    
    private String error[];
    /** Public constructor
     * @param h a reference to the FScript object
     */
    Parser(FScript h) {
        vars=new Hashtable();
        gVars=null;
        funcs=new Hashtable();
        host=h;
        
        setPrio();
        
    }
    
    //only used for function calls - note it is private
    private Parser(FScript h,Hashtable l,Hashtable g, Hashtable f) {
        vars=l;
        gVars=g;
        funcs=f;
        host=h;
    }
    
    /**
     * Sets the LineLoader clas to be used for input
     * @param in - the class
     */
    void setCode(LineLoader in) {
        code=in;
    }
    
    /**
     *The main parsing function
     *@param from - the start line number
     *@param to - the end line number
     *returns an Object (currently a Integer or String) depending
     *on the return value of the code parsed, or null if none.
     */
    Object parse(int from,int to) throws FSException {
        
        // nothing to do when starting beond the code end
        if (code.lineCount()<=from) return null;
        
        
        maxLine=to;
        code.setCurLine(from);
        tok=new LexAnn(code.getLine());
        checkLine(code.getLine());
        getNextToken();
	
        while (tok.ttype!=LexAnn.TT_EOF) {
            
            //a script must always start with a word...
            
            try {
                parseStmt();
	    }
	    catch (ExitException e){
		throw e;
	        
            } catch (RetException e) {
                return retVal;
            }
            
            getNextToken();
        }
        
        return retVal;
        
        
    }
    
    /**
     * Resets the parser state.
     */
    void reset(){
        if (vars!=null){
            vars.clear();
        }
        if (gVars!=null){
            gVars.clear();
        }
    }
    
    //builds the operator priority table
    private void setPrio(){
        if (opPrio==null){
            opPrio=new Hashtable();
            //from low to high
            opPrio.put(new Integer(LexAnn.TT_LOR),new Integer(1));
            opPrio.put(new Integer(LexAnn.TT_LAND),new Integer(2));
            opPrio.put(new Integer(LexAnn.TT_LEQ),new Integer(5));
            opPrio.put(new Integer(LexAnn.TT_LNEQ),new Integer(5));
            opPrio.put(new Integer(LexAnn.TT_LGR),new Integer(5));
            opPrio.put(new Integer(LexAnn.TT_LGRE),new Integer(5));
            opPrio.put(new Integer(LexAnn.TT_LLS),new Integer(5));
            opPrio.put(new Integer(LexAnn.TT_LLSE),new Integer(5));
            opPrio.put(new Integer(LexAnn.TT_PLUS),new Integer(10));
            opPrio.put(new Integer(LexAnn.TT_MINUS),new Integer(10));
            opPrio.put(new Integer(LexAnn.TT_MULT),new Integer(20));
            opPrio.put(new Integer(LexAnn.TT_DIV),new Integer(20));
            opPrio.put(new Integer(LexAnn.TT_MOD),new Integer(20));
        }
    }
    
    //statement - top level thing
    private void parseStmt() throws FSException,RetException {
        
        
        switch(tok.ttype) {
            
            
            case LexAnn.TT_IF:
            case LexAnn.TT_EIF:
            case LexAnn.TT_WHILE:
            case LexAnn.TT_EWHILE:
            case LexAnn.TT_DEFINT:
            case LexAnn.TT_DEFSTRING:
            case LexAnn.TT_DEFFUNC:
	    case LexAnn.TT_EXIT:
            case LexAnn.TT_EDEFFUNC:
            case LexAnn.TT_RETURN: {
                parseKeyWord();
                break;
            }
            case LexAnn.TT_FUNC: {
                parseFunc();
                break;
            }
            case LexAnn.TT_ARRAY: {
                parseArrayAssign();
                break;
            }
            case LexAnn.TT_WORD: {
                parseAssign();
                break;
            }
            case LexAnn.TT_EOL: {
                tok.nextToken();
                break;
            }
            case LexAnn.TT_EOF:{
                //all done
                break;
            }
            default: {
                parseError("Expected identifier");
            }
        }
        
    }
    
    
    private void parseFunc() throws FSException,RetException {
        String name;
        
        name=(String)tok.value;
        
        //should be a '('
        getNextToken();
        
        parseCallFunc(name);
        getNextToken();
    }
    
    private void parseArrayAssign() throws FSException,RetException {
        String name;
        Object index;
        Object val;
        
        name=(String)tok.value;
        getNextToken(); // should be a '['
        getNextToken(); // should be the index
        index=parseExpr();
        getNextToken(); // should be a ']'
        
        //getNextToken();
        if (tok.ttype!=LexAnn.TT_EQ) {
            parseError("Expected '='" );
        } else {
            getNextToken();
            val=parseExpr();
            try {
                host.setVar(name,index,val);
            } catch (Exception e) {
                parseError(e.getMessage());
            }
        }
    }
    
    
    
    
    
    private void parseKeyWord() throws FSException,RetException {
        
        switch (tok.ttype) {
            case LexAnn.TT_DEFINT:
            case LexAnn.TT_DEFSTRING:
            case LexAnn.TT_DEFDOUBLE: {
                parseVarDef();
                break;
            }
            
            case LexAnn.TT_IF: {
                parseIf();
                break;
            }
            case LexAnn.TT_WHILE: {
                parseWhile();
                break;
            }
            case LexAnn.TT_RETURN: {
                parseReturn();
                break;
            }
            case LexAnn.TT_DEFFUNC: {
                parseFunctionDef();
                break;
            }
    	    case LexAnn.TT_EXIT:{
		parseExit();
            }
            default: {
                //we never get here
                parseError("Not a keyword");
            }
        }
    }
    
    
    //handles 'return' statements
    private void parseReturn() throws FSException,RetException {
        
        getNextToken();
        
        retVal=parseExpr();
        throw new RetException();
    }
    
    //handles 'exit' statements
    private void parseExit() throws FSException,RetException {
	getNextToken();
        
        retVal=parseExpr();
	throw new ExitException();
    }
    
    
    //Asignment parser
    private void parseAssign() throws FSException {
        String name;
        Object val;
        
        name=(String)tok.value;
        getNextToken();
        
        if (tok.ttype!=LexAnn.TT_EQ) {
            parseError("Expected '='" );
        } else {
            
            getNextToken();
            val=parseExpr();
            if (hasVar(name)) {
                setVar(name,val);
            } else {
                try {
                    host.setVar(name,null,val);
                } catch (Exception e) {
                    parseError(e.getMessage());
                }
            }
        }
    }
    
    //Handles function execution
    Object callFunction(String name,Vector params) throws FSException{
        FuncEntry fDef;
        int n;
        int oldLine;
        Object val;
        
        val=null;
        
        //Check we have a definition for the function
        if (funcs.containsKey(name)) {
            
            fDef=(FuncEntry)funcs.get(name);
            
            //Check params and def match
            if (fDef.paramNames.size()!=params.size()) {
                parseError("Expected " +
                fDef.paramNames.size() +
                " parameters, Found " + params.size());
            }
            
            //Create a new parser instance to handle call
            Parser p;
            Hashtable locals=new Hashtable();
            
            //Push the params into the local scope
            for (n=0;n<fDef.paramNames.size();n++) {
                locals.put(fDef.paramNames.elementAt(n),
                params.elementAt(n));
            }
            //watch for recursive calls
            if (gVars==null) {
                p=new Parser(host,locals,vars,funcs);
            } else {
                p=new Parser(host,locals,gVars,funcs);
            }
            //cache the current execution point
            oldLine=code.getCurLine();
            p.setCode(code);
            
            //let it rip
            val=p.parse(fDef.startLine+1,fDef.endLine-1);
            
            //reset execution point
            code.setCurLine(oldLine);
            
            
            
        } else {//calls into super class  code...}
            try {
                val=host.callFunction(name,params);
	    } catch (ExitException e){
		throw e;
            } catch (Exception e) {
                parseError(e.getMessage());
            }
        }
        
        return val;
        
    }
    
    //Handle calls to a function
    private Object parseCallFunc(String name) throws FSException {
        
        Vector params=new Vector(4);
        
        //Set up the parameters
        do {
            getNextToken();
            if (tok.ttype==',') {
                getNextToken();
            } else if (tok.ttype==')') {
                break;
            }
            params.addElement(parseExpr());
        } while (tok.ttype==',');
        
        
        
        return callFunction(name,params);
    }
    
    //handles function definitions
    private void parseFunctionDef() throws FSException {
        
        FuncEntry fDef=new FuncEntry();
        Object val;
        String name,fName;
        
        fDef.startLine=code.getCurLine();
        
        getNextToken();
        
        //should be the function name
        if (tok.ttype!=LexAnn.TT_FUNC) {
            parseError("Expected function start identifier");
        }
        fName=(String)tok.value;
        getNextToken();
        
        //should be a '('
        if (tok.ttype!='(') {
            parseError("Expected (");
        }
        
        getNextToken();
        //parse the header...
        while(tok.ttype!=')') {
            if (tok.ttype!=LexAnn.TT_DEFINT&&tok.ttype!=LexAnn.TT_DEFSTRING) {
                parseError("Expected type name");
            }
            
            val=null; //keep the compiler happy..
            
            if (tok.ttype==LexAnn.TT_DEFINT) {
                val=new Integer(0);
            } else if (tok.ttype==LexAnn.TT_DEFSTRING) {
                val=new String("");
            }
            
            getNextToken();
            
            if (tok.ttype!=LexAnn.TT_WORD) {
                parseError("Expected function parameter name identifier");
            }
            
            name=(String)tok.value;
            
            fDef.paramNames.addElement(name);
            fDef.params.put(name,val);
            
            getNextToken();
            if (tok.ttype==',') getNextToken();
        }
        
        //now we just skip to the endfunction
        
        while ((tok.ttype!=LexAnn.TT_EDEFFUNC)&&(tok.ttype!=LexAnn.TT_EOF)) {
            getNextToken();
            if (tok.ttype==LexAnn.TT_DEFFUNC)
                parseError("Nested functions are illegal");
        }
        
        fDef.endLine=code.getCurLine();
        getNextToken();
        
        funcs.put(fName,fDef);
        
    }
    
    //Expression parser
    private Object parseExpr() throws FSException{
        
        ETreeNode curNode=null;
        boolean end=false;
        boolean skipTok=false;
        Object val;
        boolean negate=false; //flag for unary minus
        boolean not=false;//flag for unary not.
        boolean prevOp=true;//flag - true if previous value was an operator
        
        
        
        
        
        while (!end){
            
            switch (tok.ttype) {
                
                //the various possible 'values'
                case LexAnn.TT_INTEGER:
                case LexAnn.TT_DOUBLE:
                case LexAnn.TT_STRING:
                case LexAnn.TT_WORD:
                case LexAnn.TT_FUNC:
                case LexAnn.TT_ARRAY:{
                    
                    if (!prevOp){
                        parseError("Expected Operator");
                    } else {
                        
                        val=null;
                        ETreeNode node=new ETreeNode();
                        node.type=ETreeNode.E_VAL;
                        
                        switch (tok.ttype){
                            //numbers - just get them
                            case LexAnn.TT_INTEGER:{
                                val=(Integer)tok.value;
                                break;
                            }
                            //functions - evaluate them
                            case LexAnn.TT_FUNC:{
                                String name=(String)tok.value;
                                getNextToken();
                                val=parseCallFunc(name);
                                break;
                            }
                            //arrays - evaluate them
                            case LexAnn.TT_ARRAY:{
                                String name=(String)tok.value;
                                getNextToken(); //should be a '['
                                getNextToken(); //should be the index
                                Object index=parseExpr();
                                try {
                                    val=host.getVar(name,index);
                                } catch (Exception e) {
                                    parseError(e.getMessage());
                                }
                                break;
                            }
                            //variables - resolve them
                            case LexAnn.TT_WORD:{
                                if (hasVar((String)tok.value)) {
                                    val=getVar((String)tok.value);
                                } else {
                                    try {
                                        val=host.getVar((String)tok.value,null);
                                    } catch (Exception e) {
                                        parseError(e.getMessage());
                                    }
                                }
                                break;
                            }
                            //strings - just get again
                            case LexAnn.TT_STRING:{
                                val=(String)tok.value;
                                break;
                            }
                        }
                        
                        //unary not
                        if (not){
                            if (val instanceof Integer){
                                if (((Integer)val).intValue()!=0){
                                    val=new Integer(0);
                                } else {
                                    val=new Integer(1);
                                }
                                not=false;
                            } else {
                                parseError("Type mismatch for !");
                            }
                        }
                        
                        //unary minus
                        if (negate){
                            if (val instanceof Integer){
                                val=new Integer(-((Integer)val).intValue());
                            } else {
                                parseError("Type mistmatch for unary -");
                            }
                        }
                        
                        node.value=val;
                        
                        if (curNode!=null){
                            if (curNode.left==null){
                                curNode.left=node;
                                node.parent=curNode;
                                curNode=node;
                                
                            } else if (curNode.right==null){
                                curNode.right=node;
                                node.parent=curNode;
                                curNode=node;
                                
                            }
                        } else {
                            curNode=node;
                        }
                        
                        prevOp=false;
                    }
                    break;
                }
                                                                                                /*operators - have to be more carefull with these.
                                                                                                We build an expression tree - inserting the nodes at the right
                                                                                                points to get a reasonable approximation to correct operator
                                                                                                precidence*/
                case LexAnn.TT_LEQ:
                case LexAnn.TT_LNEQ:
                case LexAnn.TT_MULT:
                case LexAnn.TT_DIV:
                case LexAnn.TT_MOD:
                case LexAnn.TT_PLUS:
                case LexAnn.TT_MINUS:
                case LexAnn.TT_LGR:
                case LexAnn.TT_LGRE:
                case LexAnn.TT_LLSE:
                case LexAnn.TT_LLS:
                case LexAnn.TT_NOT:
                case LexAnn.TT_LAND:
                case LexAnn.TT_LOR: {
                    if (prevOp){
                        if (tok.ttype==LexAnn.TT_MINUS){
                            negate=true;
                        } else if (tok.ttype==LexAnn.TT_NOT){
                            not=true;
                        } else {
                            parseError("Expected expression");
                        }
                    } else {
                        
                        ETreeNode node=new ETreeNode();
                        
                        node.type=ETreeNode.E_OP;
                        node.value=new Integer(tok.ttype);
                        
                        if (curNode.parent!=null){
                            
                            int curPrio=getPrio(tok.ttype);
                            int parPrio=
                            getPrio(((Integer)curNode.parent.value).intValue());
                            
                            if (curPrio<=parPrio){
                                //this nodes parent is the current nodes grandparent
                                node.parent=curNode.parent.parent;
                                //our nodes left leg is now linked into the current nodes
                                //parent
                                node.left=curNode.parent;
                                //hook into grandparent
                                if (curNode.parent.parent!=null){
                                    curNode.parent.parent.right=node;
                                }
                                
                                //the current nodes parent is now us (because of above)
                                curNode.parent=node;
                                //set the current node.
                                curNode=node;
                            } else {
                                //current node's parent's right is now us.
                                curNode.parent.right=node;
                                //our nodes left is the current node.
                                node.left=curNode;
                                //our nodes parent is the current node's parent.
                                node.parent=curNode.parent;
                                //curent nodes parent is now us.
                                curNode.parent=node;
                                //set the current node.
                                curNode=node;
                            }
                        } else {
                            //our node's left is the current node
                            node.left=curNode;
                            //current node's parent is us now
                            //we don't have to set our parent, as it is null.
                            curNode.parent=node;
                            //set current node
                            curNode=node;
                        }
                        prevOp=true;
                    }
                    break;
                }
                case '(':
                    //start of an bracketed expression, recursively call ourself
                    //to get a value
                {
                    getNextToken();
                    val=parseExpr();
                    
                    ETreeNode node=new ETreeNode();
                    node.value=val;
                    node.type=ETreeNode.E_VAL;
                    
                    if (curNode!=null){
                        if (curNode.left==null){
                            curNode.left=node;
                            node.parent=curNode;
                            curNode=node;
                            
                        } else if (curNode.right==null){
                            curNode.right=node;
                            node.parent=curNode;
                            curNode=node;
                            
                        }
                    } else {
                        curNode=node;
                    }
                    prevOp=false;
                    break;
                }
                
                default: {
                    end=true;
                }
                
            }
            if (!end){
                tok.nextToken();
            }
        }
        
        //find the top of the tree we just built.
        if (curNode==null) parseError("Missing Expression");
        while(curNode.parent!=null){
            curNode=curNode.parent;
        }
        return evalETree(curNode);
        
    }
    
    //convenience function to get operator priority
    private int getPrio(int op){
        return ((Integer)opPrio.get(new Integer(op))).intValue();
    }
    
    //evaluates the expression tree recursively
    private Object evalETree(ETreeNode node) throws FSException{
        Object lVal,rVal;                
        
        if (node.type==ETreeNode.E_VAL){
            return node.value;
        }
        
        lVal=evalETree(node.left);
        rVal=evalETree(node.right);
        
        switch (((Integer)node.value).intValue()){
            //call the various eval functions
            case LexAnn.TT_PLUS:{
                return evalPlus(lVal,rVal);
            }
            case LexAnn.TT_MINUS:{
                return evalMinus(lVal,rVal);
            }
            case LexAnn.TT_MULT:{
                return evalMult(lVal,rVal);
            }
            case LexAnn.TT_DIV:{
                return evalDiv(lVal,rVal);
            }
            case LexAnn.TT_LEQ:{
                return evalEq(lVal,rVal);
            }
            case LexAnn.TT_LNEQ:{
                return evalNEq(lVal,rVal);
            }
            case LexAnn.TT_LLS:{
                return evalLs(lVal,rVal);
            }
            case LexAnn.TT_LLSE:{
                return evalLse(lVal,rVal);
            }
            case LexAnn.TT_LGR:{
                return evalGr(lVal,rVal);
            }
            case LexAnn.TT_LGRE:{
                return evalGre(lVal,rVal);
            }
            case LexAnn.TT_MOD:{
                return evalMod(lVal,rVal);
            }
            case LexAnn.TT_LAND:{
                return evalAnd(lVal,rVal);
            }
            case LexAnn.TT_LOR:{
                return evalOr(lVal,rVal);
            }
        }
        
        return null;
    }
    
    //addition
    private Object evalPlus(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            return new Integer(((Integer)lVal).intValue()
            +((Integer)rVal).intValue());
        }  else if (lVal instanceof String || rVal instanceof String){
            return new String(lVal.toString()+rVal.toString());
        } else {
            parseError("Type Mismatch for operator +");
        }
        
        return null;
    }
    
    //subtraction
    private Object evalMinus(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            return new Integer(((Integer)lVal).intValue()
            -((Integer)rVal).intValue());
        } else {
            parseError("Type Mismatch for operator -");
        }
        
        return null;
    }
    
    //multiplication
    private Object evalMult(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            return new Integer(((Integer)lVal).intValue()
            *((Integer)rVal).intValue());
        } else {
            parseError("Type Mismatch for operator *");
        }
        
        return null;
    }
    
    //modulus
    private Object evalMod(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            return new Integer(((Integer)lVal).intValue()
            %((Integer)rVal).intValue());
        } else {
            parseError("Type Mismatch for operator %");
        }
        
        return null;
    }
    
    //Logical AND
    private Object evalAnd(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            boolean b1,b2;
            b1=((Integer)lVal).intValue()!=0;
            b2=((Integer)rVal).intValue()!=0;
            if (b1 && b2){
                return new Integer(1);
            }else{
                return new Integer(0);
            }
        } else {
            parseError("Type Mismatch for operator &&");
        }
        
        return null;
    }
    
    //Logical Or
    private Object evalOr(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            boolean b1,b2;
            b1=((Integer)lVal).intValue()!=0;
            b2=((Integer)rVal).intValue()!=0;
            if (b1 || b2){
                return new Integer(1);
            }else{
                return new Integer(0);
            }
        } else {
            parseError("Type Mismatch for operator ||");
        }
        
        return null;
    }
    
    //division
    private Object evalDiv(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            return new Integer(((Integer)lVal).intValue()
            /((Integer)rVal).intValue());
        } else {
            parseError("Type Mismatch for operator /");
        }
        
        return null;
    }
    
    //logical equal
    private Object evalEq(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            if (lVal.equals(rVal)){
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else if (lVal instanceof String && rVal instanceof String){
            if (lVal.equals(rVal)){
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else {
            parseError("Type Mismatch for operator ==");
        }
        
        return null;
    }
    
    //<
    private Object evalLs(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            if (((Integer)lVal).intValue()<((Integer)rVal).intValue()){
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else if (lVal instanceof String && rVal instanceof String){
            if (((String)lVal).compareTo((String)rVal)<0) {
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else {
            parseError("Type Mismatch for operator <");
        }
        return null;
    }
    
    //<=
    private Object evalLse(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            if (((Integer)lVal).intValue()<=((Integer)rVal).intValue()){
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else if (lVal instanceof String && rVal instanceof String){
            if (((String)lVal).compareTo((String)rVal)<=0) {
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else {
            parseError("Type Mismatch for operator <=");
        }
        return null;
    }
    
    //>
    private Object evalGr(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            if (((Integer)lVal).intValue()>((Integer)rVal).intValue()){
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else if (lVal instanceof String && rVal instanceof String){
            if (((String)lVal).compareTo((String)rVal)>0) {
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else {
            parseError("Type Mismatch for operator >");
        }
        return null;
    }
    
    //>=
    private Object evalGre(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            if (((Integer)lVal).intValue()>=((Integer)rVal).intValue()){
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        }  else if (lVal instanceof String && rVal instanceof String){
            if (((String)lVal).compareTo((String)rVal)>=0) {
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else {
            parseError("Type Mismatch for operator >=");
        }
        return null;
    }
    
    //logical inequallity
    private Object evalNEq(Object lVal,Object rVal) throws FSException{
        if (lVal instanceof Integer && rVal instanceof Integer){
            if (!lVal.equals(rVal)){
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else if (lVal instanceof String && rVal instanceof String){
            if (!lVal.equals(rVal)){
                return new Integer(1);
            } else {
                return new Integer(0);
            }
        } else {
            parseError("Type Mismatch for operator !=");
        }
        return null;
    }
    
    private void printWTree(ETreeNode node){
        while(node.parent!=null){
            node=node.parent;
        }
        printETree(node);
    }
    
    private void printETree(ETreeNode node){
        
        System.out.println(node);
        if (node.left!=null){
            System.out.print("Left");
            printETree(node.left);
        }
        if (node.right!=null){
            System.out.print("Right");
            printETree(node.right);
        }
    }
    
    private void parseIf() throws FSException,RetException {
        Integer val;
        int depth;
        boolean then=false;
        
        
        getNextToken();
        try {
            val=(Integer)parseExpr();
        } catch (ClassCastException cce) {
            parseError("If condition needs to be Integer");
            return; // just to make sure the compiler doesn't complain
            // as we know parseError throws an exception (stupid compiler)
        }
        
         //handle the one line if-then construct
        if (tok.ttype==LexAnn.TT_THEN){
            getNextToken();
            //is this a single line then (or just a optional then)
            if (tok.ttype!=LexAnn.TT_EOL){
                //single line if then construct - run separately
                //tok.pushBack();
                if (val.intValue()!=0){
                    parseStmt();
                } else {
                    //consume to EOL
                    while(tok.ttype!=LexAnn.TT_EOL){
                        getNextToken();
                    }
                }
                then=true;
            }
        }

        if (!then){
            if (val.intValue()!=0) {
                getNextToken();
                while((tok.ttype!=LexAnn.TT_EIF)&&
                (tok.ttype!=LexAnn.TT_ELSE) &&
                (tok.ttype!=LexAnn.TT_EOF)&&
                (tok.ttype!=LexAnn.TT_ELSIF)) {
                    //run the body of the if
                    parseStmt();
                    getNextToken();
                }
                if (tok.ttype==LexAnn.TT_ELSE || tok.ttype==LexAnn.TT_ELSIF) {
                    //skip else clause -
                    //have to do this taking into acount nesting
                    depth=1;
                    do {
                        getNextToken();
                        if (tok.ttype==LexAnn.TT_IF) depth++;
                        if (tok.ttype==LexAnn.TT_EOF)
                            parseError("can't find endif");
                        if (tok.ttype==LexAnn.TT_EIF) depth--;

                        //A then could indicate a one line
                        //if - then construct, then we don't increment
                        //depth
                        if (tok.ttype==LexAnn.TT_THEN) {

                            getNextToken();
                            if (tok.ttype!=LexAnn.TT_EOL){
                                depth--;
                            }
                            tok.pushBack();
                        }

                    } while(depth>0);
                    getNextToken();
                } else {
                    getNextToken();
                }

            } else {
                //skip to else clause
                depth=1;
                do {
                    getNextToken();
                    if (tok.ttype==LexAnn.TT_IF )depth++;
                    if (tok.ttype==LexAnn.TT_EOF)
                        parseError("can't find endif");
                    if ((tok.ttype==LexAnn.TT_EIF)) depth--;
                    if ((tok.ttype==LexAnn.TT_ELSE || tok.ttype==LexAnn.TT_ELSIF )&& depth==1) depth--;
                    //A then could indicate a one line
                    //if - then construct, then we don't increment
                    //depth
                    if (tok.ttype==LexAnn.TT_THEN) {

                        getNextToken();
                        if (tok.ttype!=LexAnn.TT_EOL){
                            depth--;
                        }
                        tok.pushBack();
                    }

                } while(depth>0);


                if (tok.ttype==LexAnn.TT_ELSE) {
                    getNextToken();
                    getNextToken();
                    //run else clause

                    while(tok.ttype!=LexAnn.TT_EIF) {
                        parseStmt();
                        getNextToken();
                    }
                    getNextToken();
                } else if (tok.ttype==LexAnn.TT_ELSIF){
                	parseIf();
                } else {
                    getNextToken();
                }
            }
        }
    }
    
    private void parseWhile() throws FSException,RetException {
        //parses the while statement
        
        Integer val;
        int startLine;
        int endPos;
        int depth;
        
        startLine=code.getCurLine();
        getNextToken();
        val=(Integer)parseExpr();
        getNextToken();
        
        while (val.intValue()!=0) {
            while((tok.ttype!=LexAnn.TT_EWHILE)&&
            (tok.ttype!=LexAnn.TT_EOF)){
                parseStmt();
                getNextToken();
            }
            
            //reset to start of while loop....
            code.setCurLine(startLine);
            resetTokens();
            getNextToken(); //a 'while' you would imagine.
            val=(Integer)parseExpr();
            getNextToken();
        }
        //skip to endwhile
        depth=1;
        do {
            getNextToken();
            if (tok.ttype==LexAnn.TT_WHILE) depth++;
            if (tok.ttype==LexAnn.TT_EWHILE) depth--;
            if (tok.ttype==LexAnn.TT_EOF)
                parseError("can't find endwhile");
        } while (depth>0);
        
        getNextToken();
        
    }
    
    
    private void parseVarDef() throws FSException {
        
        
        Object val;
        int type=0;
        String name;
        
        val=null;
        
        if (tok.ttype==LexAnn.TT_DEFINT) {
            type=LexAnn.TT_DEFINT;
        } else if (tok.ttype==LexAnn.TT_DEFSTRING) {
            type=LexAnn.TT_DEFSTRING;
        }
        else if (tok.ttype==LexAnn.TT_DEFDOUBLE) {
            type=LexAnn.TT_DEFDOUBLE;
        } else {
            parseError("Expected 'int','string' or 'double'");
        }
        
        
        do {
            getNextToken();
            if (tok.ttype!=LexAnn.TT_WORD) {
                parseError("Expected variable name identifier,");
            }
            
            name=(String)tok.value;
            
            switch (type){
                case LexAnn.TT_DEFINT: {
                    addVar(name,new Integer(0));
                    break;
                }
                case LexAnn.TT_DEFSTRING:{
                    addVar(name,new String(""));
                    break;
                }
            }
            
            getNextToken();
            if (tok.ttype==LexAnn.TT_EQ){
                getNextToken();
                setVar(name,parseExpr());
            } else if (tok.ttype!=',' && tok.ttype!=LexAnn.TT_EOL) {
                parseError("Expected ','");
            }
            
        } while (tok.ttype!=LexAnn.TT_EOL);
        
    }
    
    //format an error message and throw FSException
    private void parseError(String s) throws FSException {
        String t;
        error=new String[6];
        
        t=tok.toString();
        
        //set up our error block
        
        error[0]=s;
        error[1]=(new Integer(code.getCurLine())).toString();
        error[2]=code.getLine();
        error[3]=t;
        error[4]=vars.toString();
        if (gVars!=null) error[5]=gVars.toString();
        
        //then build the display string
        s="\n\t"+s;
        int l=code.getCurLine();
        s=s+ "\n\t\t at line:" + l + " ";
        s+="\n\t\t\t  "+code.getLine(l-2);
        s+="\n\t\t\t  "+code.getLine(l-1);
        s+="\n\t\t\t> "+code.getLine(l)+" <";
        s+="\n\t\t\t  "+code.getLine(l+1);
        s+="\n\t\t\t  "+code.getLine(l+2);
        s=s+ "\n\t\t current token:" + t;
        s=s+ "\n\t\t Variable dump:" + vars;
        if (gVars!=null) {
            s=s+ "\n\t\t Globals:" + gVars;
        }
        throw new FSException(s);
    }
    
    //return the error block
    public String[] getError() {
        return error;
    }
    
    //Other non SM related routines
    
    private boolean pComp(String s) {
        //a compare for (String)tok.value strings - that avoids the null problem
        if (tok.value!=null) {
            return ((String)tok.value).equals(s);
        } else {
            return false;
        }
    }
    
    //misc token access routines
    private void getNextToken()  {
        
        if((tok.ttype==LexAnn.TT_EOL) && (code.getCurLine() < maxLine) ) {
            code.setCurLine(code.getCurLine()+1);
            tok.setString(code.getLine());
            tok.nextToken();
        } else if ((tok.ttype==LexAnn.TT_EOL) && (code.getCurLine()>=maxLine)) {
            tok.ttype=LexAnn.TT_EOF; //the only place this gets set
        }
        else {
            tok.nextToken();
        }
    }
    
    private void resetTokens()  {
        tok.setString(code.getLine());
        tok.nextToken();
    }
    
    
    //variable access routines
    void addVar(String name,Object value) throws FSException {
        
        if (vars.containsKey(name)) {
            parseError("Already defined in this scope: " + name );
        }
        vars.put(name,value);
    }
    
    
    Object getVar(String name) {
        if (vars.containsKey(name)) {
            return vars.get(name);
        } else {
            if (gVars!=null) {
                if (gVars.containsKey(name)) {
                    return gVars.get(name);
                }
            }
        }
        
        
        return null; //shouldn't get here
    }
    
    
    void setVar(String name,Object val) throws FSException {
        
        Object obj;
        
        if (val==null) parseError("set variable "+name+" with null value");
        
        if (vars.containsKey(name)) {
            obj=vars.get(name);
            if (val.getClass() != obj.getClass()) {
                parseError("Incompatible types");
            }
            vars.remove(name);
            vars.put(name,val);
        } else if (gVars.containsKey(name)) {
            obj=gVars.get(name);
            if (val.getClass() != obj.getClass()) {
                parseError("Incompatible types");
            }
            gVars.remove(name);
            gVars.put(name,val);
        }
        
    }
    
    boolean hasVar(String name) {
        if (gVars==null) {
            return vars.containsKey(name);
        } else {
            return vars.containsKey(name) ||
            gVars.containsKey(name);
        }
    }
    
    //Gets the 'return' value from the parser
    Object getReturnValue(){
	    return retVal;
    }
    
    //Can be called from external functions to force an exit
    void exit(Object o) throws FSException{
	    retVal=o;
	    throw new ExitException();
    }
    
    //Checks line for correctly formed ( ) and "
    //this is a little crude (i.e. the rdp should really pick it up)
    //but it's not all that good about it, hence somewhat kludgy fix
    private void checkLine(String line) throws FSException{
        boolean inQuotes=false;
        int brCount=0;
        char chars[];
        int n;
        
        
        if (line!=null){
            if (!line.trim().startsWith("#")){
                chars=line.toCharArray();
                for (n=0;n<chars.length;n++){
                    if (inQuotes){
                        if (chars[n]=='"'){
                            if (n>=1){
                                if (chars[n-1]!='\\'){
                                    inQuotes=false;
                                }
                            }
                        }
                    } else {
                        if (chars[n]=='('){
                            brCount++;
                        } else if (chars[n]==')'){
                            brCount--;
                        } else if (chars[n]=='"'){
                            if (n>=1){
                                if (chars[n-1]!='\\'){
                                    inQuotes=true;
                                }
                            }
                        }
                    }
                }
                
                
                if (inQuotes){
                    parseError("Mismatched quotes");
                }
                
                if (brCount!=0){
                    parseError("Mismatched brackets");
                }
            }
        }
        
    }
    
}



