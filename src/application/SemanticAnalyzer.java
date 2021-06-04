package application;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//SemanticAnalyzer checks the semantics of the program
public class SemanticAnalyzer {
	private ArrayList<Variable> declarations;
	private int stack;
	private ArrayList<Word> exprStack;
	private ArrayList<Word> opStack;
	private boolean bool_infinite_and; //for boolean with infinite arity
	private boolean bool_infinite_or;
	private ArrayList<Word> exprCopy; //copies the whole expression
	
	
	SemanticAnalyzer(){
		this.declarations = new  ArrayList<Variable>();
		this.declarations.add(new Variable("IT"));
	}
	
	//return list of variable declarations
	ArrayList<Variable> getDeclarations(){
		return this.declarations;
	}
	
	//add to arraylist
	void addVar(String name){
		this.declarations.add(new Variable(name));
	}
	
	//sets variable value
	void setVarValue(String name, String value) {
		for(Variable variable: this.declarations) {
			if(variable.getName().equals(name)) {
				variable.setValue(value);
				break;
			}
		}
	}
	
	//sets variable type (must be called only in variable declaration or in assignment)
	void setVarType(String name, String type, int linenum) {
		for(Variable variable: this.declarations) {
			if(variable.getName().equals(name)) {
				if(type.equals("variable")) { //if "variable" is the type, get that variable's type 
					String valType = this.getVarType(variable.getValue()); //type of variable value
					String valValue = this.getVarValue(variable.getValue()); //type of variable value
					//variable value must be a valid variable name
					if(valType!=null && valValue!=null) {
						variable.setType(valType); //sets variable type
						variable.setValue(valValue); //sets variable value
						break;
					}else {
						//error here. Assignment of undefined variable to a variable.
						Interpreter.printError("Line "+linenum+": Reference to undefined variable: "+variable.getValue()+".");
						Parser.error = true;
					}
				}
				variable.setType(type); //sets variable type
				break;
			}
		}
	}
	//get the variable type of the given name
	String getVarType(String name) {
		 for(Variable variable: this.declarations) {
			 if(variable.getName().equals(name)) {
				 return variable.getType();
			 }
		 }
		 return null;
	 }
	//get the variable value of the given name
	 String getVarValue(String name) {
		 for(Variable variable: this.declarations) {
			 if(variable.getName().equals(name)) {
				 return variable.getValue();
			 }
		 }
		 return null;
	}
	 
	 //checks if variable is valid && used in expression
	 boolean isValid(String name, int linenum) {
		 for(Variable variable: this.declarations) {
			 if(variable.getName().equals(name)) {
				 return true;
			 }
		 }
		 Interpreter.printError("Line "+linenum+": Reference to undefined variable: "+name+".");
		 Parser.error = true;
		 return false;
	 }
	//check of the variable is already added in the declarations
	 boolean checkVar(String name) {
		 for(Variable variable: this.declarations) {
			 if(variable.getName().equals(name)) {
				 return true;
			 }
		 }
		return false;
	 }
	
	 //evaluate boolean NOT 
	 Word evalNot(ArrayList<Word> expr) {
		 expr.remove(0); //removes NOT operator
		 ArrayList<Word> notStack = new ArrayList<Word>(); //initialize stack for NOT operation
		 int mystack = 0; //count of operands

		 while(!expr.isEmpty() && mystack < 1) {
			 Word w = expr.get(0); //gets top word
			 //get value of variable 
			 if(w.getValue2().equals("Variable Identifier")) {
				 w.setValue(this.getVarValue(w.getValue1())); //assigns value of variable
				 w.setdType(this.getVarType(w.getValue1())); //assigns data type of variable to word
			 }
			 //for troof literals
			 if(w.getValue1().equals("WIN") || w.getValue1().equals("FAIL")) {
				 notStack.add(0, w); //adds to stack
				 mystack++; //increment count of operand
				 expr.remove(0); //removes operand
			 }else if(w.getValue2().equals("Boolean Operator")) {
				 if(w.getValue1().equals("BOTH OF")) {
					 notStack.add(evalBothOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("EITHER OF")) {
					 notStack.add(evalEitherOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("WON OF")) {
					 notStack.add(evalWonOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("NOT")) {
					 notStack.add(evalNot(expr));
					 mystack++;
				 }
			 }
		 }
		 
		 //evaluate NOT operation
		 Word ans = notStack.get(0); //initial answer
		 Word op1 = notStack.get(0);
		 //NOT operation
		 if(op1.getValue1().equals("WIN")) ans.setValue("FAIL");
		 else ans.setValue("WIN");
		 return ans;
	 }
	 //evaluate boolean WON OF
	 Word evalWonOf(ArrayList<Word> expr) {
		 expr.remove(0); //removes WON OF operator
		 ArrayList<Word> wonOfStack = new ArrayList<Word>(); //initialize stack for WON OF operation
		 int mystack = 0; //count of operands
		 
		 while(!expr.isEmpty() && mystack < 2) {
			 Word w = expr.get(0); //gets top word
			 //get value of variable 
			 if(w.getValue2().equals("Variable Identifier")) {
				 w.setValue(this.getVarValue(w.getValue1())); //assigns value of variable
				 w.setdType(this.getVarType(w.getValue1())); //assigns data type of variable to word
			 }
			 
			 //for troof literals
			 if(w.getValue1().equals("WIN") || w.getValue1().equals("FAIL")) {
				 wonOfStack.add(0, w); //adds to stack
				 mystack++; //increment count of operand
				 expr.remove(0); //removes operand
			 }else if(w.getValue2().equals("Boolean Operator")) {
				 if(w.getValue1().equals("BOTH OF")) {
					 wonOfStack.add(evalBothOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("EITHER OF")) {
					 wonOfStack.add(evalEitherOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("WON OF")) {
					 wonOfStack.add(evalWonOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("NOT")) {
					 wonOfStack.add(evalNot(expr));
					 mystack++;
				 }
			 }
		 }
		 
		 //evaluate WON OF operation
		 Word ans = wonOfStack.get(0); //initial answer
		 Word op2 = wonOfStack.get(0);
		 Word op1 = wonOfStack.get(1);
		 //XOR operation
		 if(op1.getValue1().equals("WIN") && op2.getValue1().equals("FAIL") || op1.getValue1().equals("FAIL") && op2.getValue1().equals("WIN")) ans.setValue("WIN");
		 else ans.setValue("FAIL");
		 return ans;
	 }
	 
	 //evaluate boolean EITHER OF 
	 Word evalEitherOf(ArrayList<Word> expr) {
		 expr.remove(0); //removes EITHER OF operator
		 ArrayList<Word> eitherOfStack = new ArrayList<Word>(); //initialize stack for EITHER OF operation
		 int mystack = 0; //count of operands
		 
		 while(!expr.isEmpty() && mystack < 2) {
			 Word w = expr.get(0); //gets top word

			 //get value of variable 
			 if(w.getValue2().equals("Variable Identifier")) {
				 w.setValue(this.getVarValue(w.getValue1())); //assigns value of variable
				 w.setdType(this.getVarType(w.getValue1())); //assigns data type of variable to word
			 }
			 
			 //for troof literals
			 if(w.getValue1().equals("WIN") || w.getValue1().equals("FAIL")) {
				 eitherOfStack.add(0, w); //adds to stack
				 mystack++; //increment count of operand
				 expr.remove(0); //removes operand
			 }else if(w.getValue2().equals("Boolean Operator")) {
				 if(w.getValue1().equals("BOTH OF")) {
					 eitherOfStack.add(evalBothOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("EITHER OF")) {
					 eitherOfStack.add(evalEitherOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("WON OF")) {
					 eitherOfStack.add(evalWonOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("NOT")) {
					 eitherOfStack.add(evalNot(expr));
					 mystack++;
				 }
			 }
		 }
		 
		 //evaluate EITHER OF operation
		 Word ans = eitherOfStack.get(0); //initial answer
		 Word op2 = eitherOfStack.get(0);
		 Word op1 = eitherOfStack.get(1);
		 //OR operation
		 if(op1.getValue1().equals("WIN") || op2.getValue1().equals("WIN")) ans.setValue("WIN");
		 else ans.setValue("FAIL");
		 return ans;
	 }
	 
	 //evaluate boolean BOTH OF
	 Word evalBothOf(ArrayList<Word> expr){
		 expr.remove(0); //removes BOTH OF operator
		 ArrayList<Word> bothOfStack = new ArrayList<Word>(); //initialize stack for BOTH OF operation
		 int mystack = 0; //count of operands
		 
		 while(!expr.isEmpty() && mystack < 2) {
			 Word w = expr.get(0); //gets top word
			 
			 //get value of variable 
			 if(w.getValue2().equals("Variable Identifier")) {
				 w.setValue(this.getVarValue(w.getValue1())); //assigns value of variable
				 w.setdType(this.getVarType(w.getValue1())); //assigns data type of variable to word
				
			 }
			 
			 //for troof literals
			 if(w.getValue1().equals("WIN") || w.getValue1().equals("FAIL")) {
				 bothOfStack.add(0, w); //adds to stack
				 mystack++; //increment count of operand
				 expr.remove(0); //removes operand
			 
			 }else if(w.getValue2().equals("Boolean Operator")) {
				 if(w.getValue1().equals("BOTH OF")) {
					 bothOfStack.add(evalBothOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("EITHER OF")) {
					 bothOfStack.add(evalEitherOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("WON OF")) {
					 bothOfStack.add(evalWonOf(expr));
					 mystack++;
				 }else if(w.getValue1().equals("NOT")) {
					 bothOfStack.add(evalNot(expr));
					 mystack++;
				 }
			 }
		 }
		 
		 //evaluate BOTH OF operation
		 Word ans = bothOfStack.get(0); //initial answer
		 Word op2 = bothOfStack.get(0);
		 Word op1 = bothOfStack.get(1);
		 //AND operation
		 if(op1.getValue1().equals("WIN") && op2.getValue1().equals("WIN")) return ans;
		 else ans.setValue("FAIL");
		 return ans;	 
	 }
	 //evaluate boolean ALL OF and ANY OF
	 ArrayList<String> evalBoolIn(ArrayList<Word> expr, String first){
		 this.exprCopy = expr;
		 ArrayList<String> ans = new ArrayList<String>();
		 if(first.equals("ALL OF")) {
			 this.bool_infinite_and = true;
		 }else this.bool_infinite_or = true;
		 this.exprCopy.remove(0); //remove bool_in operator from expression
		 
		 while(!this.exprCopy.isEmpty()) {
			Word w = this.exprCopy.get(0); //get each word from expression
			//encounters operator
			if(w.getValue2().equals("Boolean Operator")) {
				 if(w.getValue1().equals("BOTH OF")) {
					 Word bothOfAns = evalBothOf(this.exprCopy);
					 if(this.bool_infinite_and && bothOfAns.getValue1().equals("FAIL") || this.bool_infinite_or && bothOfAns.getValue1().equals("WIN")) {
						 //return FAIL
						 ans.add(bothOfAns.getValue1());
						 ans.add(bothOfAns.getValue2());
						 return ans;
					 }//else ignore
					 else continue;
					 
				 }else if(w.getValue1().equals("EITHER OF")) {
					 Word eitherOfAns = evalEitherOf(this.exprCopy);
					 if(this.bool_infinite_and && eitherOfAns.getValue1().equals("FAIL") || this.bool_infinite_or && eitherOfAns.getValue1().equals("WIN")) {
						 //return FAIL || return WIN
						 ans.add(eitherOfAns.getValue1());
						 ans.add(eitherOfAns.getValue2());
						 return ans;
					 }//else ignore
					 else continue;
					 
				 }else if(w.getValue1().equals("WON OF")) {
					 Word wonOfAns = evalWonOf(this.exprCopy);
					 if(this.bool_infinite_and && wonOfAns.getValue1().equals("FAIL") || this.bool_infinite_or && wonOfAns.getValue1().equals("WIN")) {
						 //return FAIL || return WIN
						 ans.add(wonOfAns.getValue1());
						 ans.add(wonOfAns.getValue2());
						 return ans;
					 }//else ignore
					 else continue;
					 
				 }else if(w.getValue1().equals("NOT")) {
					 Word notAns = evalNot(this.exprCopy);
					 if(this.bool_infinite_and && notAns.getValue1().equals("FAIL") || this.bool_infinite_or && notAns.getValue1().equals("WIN")) {
						 //return FAIL || return WIN
						 ans.add(notAns.getValue1());
						 ans.add(notAns.getValue2());
						 return ans;
					 }//else ignore
					 else continue;
				 }
			 }
			 
			 //encounters operand, either return or ignore
			 //get value if variable identifier
			 else if(w.getValue2().equals("Variable Identifier")) {
				 w.setValue(this.getVarValue(w.getValue1())); //assigns value of variable
				 w.setdType(this.getVarType(w.getValue1())); //assigns data type of variable to word
			 }
			 
			 //troof literals
			 if(w.getValue1().equals("WIN") || w.getValue1().equals("FAIL")) {
				 if((this.bool_infinite_and && w.getValue1().equals("FAIL"))){ //ALL OF ... FAIL ... result = FAIL
					 //return FAIL
					 ans.add(w.getValue1());
					 ans.add(w.getValue2());
					 return ans;
				
				 }else if(this.bool_infinite_or && w.getValue1().equals("WIN")) { //ANY OF ... WIN ... result = WIN
					 //return WIN
					 ans.add(w.getValue1());
					 ans.add(w.getValue2());
					 return ans;
					 
				 }else this.exprCopy.remove(0); //remove operand, ignore
			 }
		 }
		 
		 //if did not return, meaning WIN for ALL OF and FAIL for ANY OF
		 if(this.bool_infinite_and) { //ALL OF
			 //return WIN
			 ans.add("WIN");
			 ans.add("TROOF");
		
		 }else{ //ANY OF
			//return FAIL
			 ans.add("FAIL");
			 ans.add("TROOF");
		 }
		 return ans;
	 }
	 
	 //return data type of lexeme
	 String getDataTypeForVarIdent(String value){
		 	Pattern pattern = Pattern.compile("^-?[0-9]+$");
		    Matcher matcher = pattern.matcher(value);
			if(matcher.find()) {
				return "Numbr Literal";
			}
			Pattern pattern2 = Pattern.compile("^-?[0-9]+.[0-9]+$");
		    Matcher matcher2 = pattern2.matcher(value);
		    if(matcher2.find()) {
				return "Numbar Literal";
			}
		    Pattern pattern3 = Pattern.compile("\"[^\"]*\"");
		    Matcher matcher3 = pattern3.matcher(value);
			if(matcher3.find()) {
				return "Yarn Literal";
			}
			Pattern pattern4 = Pattern.compile("^WIN$|^FAIL$");
		    Matcher matcher4 = pattern4.matcher(value);
			if(matcher4.find()) {
				return "Troof Literal";
			}
			return "";
	 }
	 
	 //evaluate expression statements
	 ArrayList<String> evaluate(ArrayList<Word> expr){
		 this.bool_infinite_and = false;
		 this.bool_infinite_or = false;
		 this.stack = 0;
		 this.exprStack = new ArrayList<Word>();
		 this.opStack = new ArrayList<Word>(); //operator stack
		 ArrayList<String> ans = new ArrayList<String>(); //value and data type
		 int exprSize = expr.size();
		 //check if boolean infinity
		 if(!expr.isEmpty() && (expr.get(0).getValue1().equals("ALL OF") || expr.get(0).getValue1().equals("ANY OF"))) {
			 ans = evalBoolIn(expr, expr.get(0).getValue1());
			 return ans;
		 }
		 
		 while(!expr.isEmpty()) { 
			 Word w = expr.get(0);
			 //add operator in the stack
			 if(w.getValue2().equals("Arithmetic Operator") || w.getValue2().equals("Boolean Operator") || w.getValue2().equals("Comparison Operator") || w.getValue2().equals("Concatenation Operator")) { 
				 this.exprStack.add(0, w);
				 expr.remove(0);
				 this.opStack.add(0, w);
				 this.stack = 0;
			 }else{ // add operand
				 if(w.getValue2().equals("Variable Identifier")) {
					 w.setValue(this.getVarValue(w.getValue1())); //assigns value of variable
				 }
				 w.setdType(this.getDataTypeForVarIdent(w.getValue1()));
				 this.exprStack.add(0, w);
				 expr.remove(0);
				 this.stack++;
				 
				//NOT operator that requires one operand
				 if(stack == 1 && this.opStack.get(0).getValue1().equals("NOT")) { //if current operator is NOT, evaluate
					 this.opStack.remove(0); //remove top operator
					 Word operand = this.exprStack.get(0);
					 this.exprStack.remove(0); //removes operand
					 this.exprStack.remove(0); //removes operator
					 
					 //perform operation
					 if(operand.getValue1().equals("WIN")) w.setValue("FAIL");						 
					 else	w.setValue("WIN"); //FAIL to WIN
					 
					 this.exprStack.add(0, w); //replace top of stack with result of operation
					 this.stack = 0;
				
					 
				//Concatenation Operation that requires 1 or more operands
				 }else if(stack == 1 && this.opStack.get(0).getValue1().equals("SMOOSH")) {
					 
					 Word op = this.exprStack.get(0);
					 this.opStack.remove(0); //remove top operator
					 this.exprStack.remove(0); //remove operator
					 this.exprStack.remove(0); //remove operand
					 //if next word in expr stack is a literal, concatenate all succeeding words  
					 if(exprSize>2 && (expr.get(0).getValue2().equals("Yarn Literal") || expr.get(0).getValue2().equals("Troof Literal") || expr.get(0).getValue2().equals("Numbr Literal") || expr.get(0).getValue2().equals("Numbar Literal"))) {
						 String concat = op.getValue1().replace("\"", "");
						 while(expr.size()!=0 && (expr.get(0).getValue2().equals("Yarn Literal") || expr.get(0).getValue2().equals("Troof Literal") || expr.get(0).getValue2().equals("Numbr Literal") || expr.get(0).getValue2().equals("Numbar Literal")  )) {
							 concat+=expr.get(0).getValue1().replace("\"", ""); //concatenate
							 expr.remove(0); //remove the yarn literal in stack
						 }
						 w.setValue(concat);
					 }else {
						 w.setValue(op.getValue1().replace("\"", ""));
					 }
					 this.exprStack.add(0, w); //replace top of stack with result of operation
					 this.stack = 0;
					 
				//Operations that requires 2 operands
				 }else if(stack == 2) {
					 
					 //Arithmetic Operations
					 Word operand2 = this.exprStack.get(0);
					 Word operand1 = this.exprStack.get(1);
					 if(this.opStack.get(0).getValue1().equals("SUM OF")) {
						 this.opStack.remove(0); //remove top operator
						 
						 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						//catch operand error
						 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal")) || !(operand2.getdType().equals("Numbr Literal") || operand2.getdType().equals("Numbar Literal"))) {
							 Word invalidop;
							 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal"))) invalidop = operand1;
							 else invalidop = operand2;
							 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
							 Parser.error = true;
							 break;
						 }
						 
						//if at least one of the operand is float, the answer should be float
						 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
							 float op1Float = Float.parseFloat(op1.getValue1());
							 float op2Float = Float.parseFloat(op2.getValue1());
							 float opF = op1Float + op2Float;
							 w.setValue(Float.toString(opF));
						//else answer is int
						 }else {
							 int op1Int = Integer.parseInt(op1.getValue1());
							 int op2Int = Integer.parseInt(op2.getValue1());
							 int opI = op1Int + op2Int;
							 w.setValue(Integer.toString(opI));
						 }
						 this.exprStack.add(0, w);
						 this.stack = 0;
	
				     }else if(this.opStack.get(0).getValue1().equals("DIFF OF")) {
				    	 this.opStack.remove(0);
				    	 
				    	 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						//catch operand error
						 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal")) || !(operand2.getdType().equals("Numbr Literal") || operand2.getdType().equals("Numbar Literal"))) {
							 Word invalidop;
							 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal"))) invalidop = operand1;
							 else invalidop = operand2;
							 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
							 Parser.error = true;
							 break;
						 }
						 //if at least one of the operand is float, the answer should be float
						 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
							 float op1Float = Float.parseFloat(op1.getValue1());
							 float op2Float = Float.parseFloat(op2.getValue1());
							 float opF = op1Float - op2Float;
							 w.setValue(Float.toString(opF));
						//else answet is int
						 }else {
							 int op1Int = Integer.parseInt(op1.getValue1());
							 int op2Int = Integer.parseInt(op2.getValue1());
							 int opI = op1Int - op2Int;
							 w.setValue(Integer.toString(opI));
						 }
						 this.exprStack.add(0, w);
						 this.stack = 0;
				     }else if(this.opStack.get(0).getValue1().equals("PRODUKT OF")) {
				    	 this.opStack.remove(0); //remove top operator
				    	 
				    	 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						//catch operand error
						 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal")) || !(operand2.getdType().equals("Numbr Literal") || operand2.getdType().equals("Numbar Literal"))) {
							 Word invalidop;
							 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal"))) invalidop = operand1;
							 else invalidop = operand2;
							 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
							 Parser.error = true;
							 break;
						 }
						 //if at least one of the operand is float, the answer should be float
						 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
							 float op1Float = Float.parseFloat(op1.getValue1());
							 float op2Float = Float.parseFloat(op2.getValue1());
							 float opF = op1Float * op2Float;
							 w.setValue(Float.toString(opF));
						//else answer is int
						 }else {
							 int op1Int = Integer.parseInt(op1.getValue1());
							 int op2Int = Integer.parseInt(op2.getValue1());
							 int opI = op1Int * op2Int;
							 w.setValue(Integer.toString(opI));
						 }
						 this.exprStack.add(0, w);
						 this.stack = 0;
				     }else if(this.opStack.get(0).getValue1().equals("QUOSHUNT OF")) {
				    	 this.opStack.remove(0); //remove top operator
				    	 
				    	 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						//catch operand error
						 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal")) || !(operand2.getdType().equals("Numbr Literal") || operand2.getdType().equals("Numbar Literal"))) {
							 Word invalidop;
							 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal"))) invalidop = operand1;
							 else invalidop = operand2;
							 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
							 Parser.error = true;
							 break;
						 }
						 //if at least one of the operand is float, the answer should be float
						 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
							 float op1Float = Float.parseFloat(op1.getValue1());
							 float op2Float = Float.parseFloat(op2.getValue1());
							 float opF = op1Float / op2Float;
							 w.setValue(Float.toString(opF));
						//else answer is int
						 }else {
							 int op1Int = Integer.parseInt(op1.getValue1());
							 int op2Int = Integer.parseInt(op2.getValue1());
							 int opI = op1Int / op2Int;
							 w.setValue(Integer.toString(opI));
						 }
						 this.exprStack.add(0, w);
						 this.stack = 0;
						 
				     }else if(this.opStack.get(0).getValue1().equals("MOD OF")) {
				    	 this.opStack.remove(0); //remove top operator
				    	 
				    	 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						//catch operand error
						 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal")) || !(operand2.getdType().equals("Numbr Literal") || operand2.getdType().equals("Numbar Literal"))) {
							 Word invalidop;
							 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal"))) invalidop = operand1;
							 else invalidop = operand2;
							 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
							 Parser.error = true;
							 break;
						 }
						 //if at least one of the operand is float, the answer should be float
						 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
							 float op1Float = Float.parseFloat(op1.getValue1());
							 float op2Float = Float.parseFloat(op2.getValue1());
							 float opF = op1Float % op2Float;
							 w.setValue(Float.toString(opF));
						//else answer is int
						 }else {
							 int op1Int = Integer.parseInt(op1.getValue1());
							 int op2Int = Integer.parseInt(op2.getValue1());
							 int opI = op1Int % op2Int;
							 w.setValue(Integer.toString(opI));
						 }
						 this.exprStack.add(0, w);
						 this.stack = 0;
						 
				     }else if(this.opStack.get(0).getValue1().equals("BIGGR OF")) {
				    	 this.opStack.remove(0); //remove top operator
				    	 
				    	 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						//catch operand error
						 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal")) || !(operand2.getdType().equals("Numbr Literal") || operand2.getdType().equals("Numbar Literal"))) {
							 Word invalidop;
							 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal"))) invalidop = operand1;
							 else invalidop = operand2;
							 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
							 Parser.error = true;
							 break;
						 }
						 //if at least one of the operand is float, the answer should be float
						 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
							 float op1Float = Float.parseFloat(op1.getValue1());
							 float op2Float = Float.parseFloat(op2.getValue1());
							 float opF = Math.max(op1Float,op2Float);
							 w.setValue(Float.toString(opF));
						//else answer is int
						 }else {
							 int op1Int = Integer.parseInt(op1.getValue1());
							 int op2Int = Integer.parseInt(op2.getValue1());
							 int opI = Math.max(op1Int,op2Int);
							 w.setValue(Integer.toString(opI));
						 }
						 this.exprStack.add(0, w);
						 this.stack = 0;
						 
				     }else if(this.opStack.get(0).getValue1().equals("SMALLR OF")) {
				    	 this.opStack.remove(0); //remove top operator
				    	 
				    	 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						//catch operand error
						 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal")) || !(operand2.getdType().equals("Numbr Literal") || operand2.getdType().equals("Numbar Literal"))) {
							 Word invalidop;
							 if(!(operand1.getdType().equals("Numbr Literal") || operand1.getdType().equals("Numbar Literal"))) invalidop = operand1;
							 else invalidop = operand2;
							 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
							 Parser.error = true;
							 break;
						 }
						//if at least one of the operand is float, the answer should be float
						 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
							 float op1Float = Float.parseFloat(op1.getValue1());
							 float op2Float = Float.parseFloat(op2.getValue1());
							 float opF = Math.min(op1Float,op2Float);
							 w.setValue(Float.toString(opF));
						//else answer is int
						 }else {
							 int op1Int = Integer.parseInt(op1.getValue1());
							 int op2Int = Integer.parseInt(op2.getValue1());
							 int opI = Math.min(op1Int,op2Int);
							 w.setValue(Integer.toString(opI));
						 }
						 this.exprStack.add(0, w);
						 this.stack = 0;
				      }
					 
					 //Boolean Operations
				     else if(this.opStack.get(0).getValue1().equals("BOTH OF")) { //perform AND operation
				    	 this.opStack.remove(0); //remove top operator
						 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						 //AND operation
						 if(op1.getValue1().equals("WIN") && op2.getValue1().equals("WIN")) w.setValue("WIN");
						 else w.setValue("FAIL");
						 
						 this.exprStack.add(0, w);
						 this.stack = 0;
					 }else if(this.opStack.get(0).getValue1().equals("EITHER OF")) { //perform OR operation
						 this.opStack.remove(0); //remove top operator
						 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						 //OR operation
						 if(op1.getValue1().equals("WIN") || op2.getValue1().equals("WIN")) w.setValue("WIN");
						 else w.setValue("FAIL");
						 
						 this.exprStack.add(0, w);
						 this.stack = 0;
					 }else if(this.opStack.get(0).getValue1().equals("WON OF")) { //perform XOR operation
						 this.opStack.remove(0); //remove top operator
						 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						 //XOR operation
						 if(op1.getValue1().equals("WIN") && op2.getValue1().equals("FAIL") || op1.getValue1().equals("FAIL") && op2.getValue1().equals("WIN")) w.setValue("WIN");
						 else w.setValue("FAIL");
						 
						 this.exprStack.add(0, w);
						 this.stack = 0;
					 }
					 
					 //Comparison Operations
					 //Both Saem Operation
					 else if(this.opStack.get(0).getValue1().equals("BOTH SAEM")) {
						 this.opStack.remove(0);
						 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator

						 //if operand is NUMBAR
						 if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal")) {
							 float op1Float = Float.parseFloat(op1.getValue1());
							 float op2Float = Float.parseFloat(op2.getValue1());
							 if(op1Float == op2Float) {
								 w.setValue("WIN");
							 }else {
								 w.setValue("FAIL");
							 }	
						 //if operand is NUMBR
						 }else {
							 try {
								 int op1Int = Integer.parseInt(op1.getValue1());
								 int op2Int = Integer.parseInt(op2.getValue1());
								 if(op1Int == op2Int) {
									 w.setValue("WIN");
								 }else {
									 w.setValue("FAIL");
								 }
							//if operand is YARN or TROOF
							 }catch(NumberFormatException e) {
								 if(op1.getdType().equals("Yarn Literal") && op2.getdType().equals("Yarn Literal")) {
									 if(op1.getValue1().equals(op2.getValue1())) {
										 w.setValue("WIN");
									 }else {
										 w.setValue("FAIL");
									 }
								 }else if(op1.getdType().equals("Troof Literal") && op2.getdType().equals("Troof Literal")) {
									 if(op1.getValue1().equals(op2.getValue1())) {
										 w.setValue("WIN");
									 }else {
										 w.setValue("FAIL");
									 }
								 }else {
									 w.setValue("FAIL"); 
								 }
							 }
						 }
							 
						 w.setdType("Troof Literal");
						 this.exprStack.add(0, w);
						 this.stack = 0;
						
					//Diffrint Operation	
				     }else if(this.opStack.get(0).getValue1().equals("DIFFRINT")) {
				    	 this.opStack.remove(0);
				    	 Word op2 = this.exprStack.get(0);
						 Word op1 = this.exprStack.get(1);
						 this.exprStack.remove(0); //removes op2
						 this.exprStack.remove(0); //removes op1
						 this.exprStack.remove(0); //removes operator
						 
						 //if operand is NUMBAR
						 if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
							 float op1Float = Float.parseFloat(op1.getValue1());
							 float op2Float = Float.parseFloat(op2.getValue1());
							 if(op1Float != op2Float) {
								 w.setValue("WIN");
							 }else {
								 w.setValue("FAIL");
							 }	
						//if operand is NUMBR
						 }else {
							try {
								 int op1Int = Integer.parseInt(op1.getValue1());
								 int op2Int = Integer.parseInt(op2.getValue1());
								 if(op1Int != op2Int) {
									 w.setValue("WIN");
								 }else {
									 w.setValue("FAIL");
								 }
							//if operand is YARN OR TROOF
							 }catch(NumberFormatException e) {
								 if(op1.getdType().equals("Yarn Literal") && op2.getdType().equals("Yarn Literal")) {
									 if(op1.getValue1().equals(op2.getValue1())) {
										 w.setValue("FAIL");
									 }else {
										 w.setValue("WIN");
									 }
								 }else if(op1.getdType().equals("Troof Literal") && op2.getdType().equals("Troof Literal")) {
									 if(op1.getValue1().equals(op2.getValue1())) {
										 w.setValue("FAIL");
									 }else {
										 w.setValue("WIN");
									 }
								 }else {
									 w.setValue("WIN");
								 }
							 }
						 }
						 w.setdType("Troof Literal");
						 this.exprStack.add(0, w);
						 this.stack = 0;			 
				     } 	 
				 } 
			 }
		 }
		 
		 //Evaluate final operation for the remaining operands in the stack
		 if(this.exprStack.size() == 1) {
			ans.add(this.exprStack.get(0).getValue1()); //value
			ans.add(this.exprStack.get(0).getValue2()); //type --not final--			
			return ans;
		 }else {
			 
			 Word boolop2 = this.exprStack.get(0);
			 Word boolop1 = this.exprStack.get(1);
			 
			//Boolean  Operations
		    if(this.opStack.get(0).getValue1().equals("BOTH OF")) {
		    	 this.opStack.remove(0); //remove top operator
				//AND operation
				 if(boolop1.getValue1().equals("WIN") && boolop2.getValue1().equals("WIN")) boolop2.setValue("WIN");
				 else boolop2.setValue("FAIL");
				 this.exprStack.clear();
				 this.exprStack.add(0, boolop2);
			 }else if(this.opStack.get(0).getValue1().equals("EITHER OF")) {
				 this.opStack.remove(0); //remove top operator
				//OR operation
				 if(boolop1.getValue1().equals("WIN") || boolop2.getValue1().equals("WIN")) boolop2.setValue("WIN");
				 else boolop2.setValue("FAIL");
				 this.exprStack.clear();
				 this.exprStack.add(0, boolop2);
			 }else if(this.opStack.get(0).getValue1().equals("WON OF")) {
				 this.opStack.remove(0); //remove top operator
				//XOR operation
				 if(boolop1.getValue1().equals("WIN") && boolop2.getValue1().equals("FAIL") || boolop1.getValue1().equals("FAIL") && boolop2.getValue1().equals("WIN")) boolop2.setValue("WIN");
				 else boolop2.setValue("FAIL");
				 
				 this.exprStack.clear();
				 this.exprStack.add(0, boolop2);
			 }
			 
			 while(exprStack.size() != 1) {
				 Word op2 = this.exprStack.get(0);
				 Word op1 = this.exprStack.get(1);
				 
				 //Arithmetic Operations
				if(this.opStack.get(0).getValue1().equals("SUM OF")) {
					 this.opStack.remove(0); //remove top operator
					 
					 this.exprStack.remove(0); //removes op2
					 this.exprStack.remove(0); //removes op1
					 this.exprStack.remove(0); //removes operator
					//catch operand error
					 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal")) || !(op2.getdType().equals("Numbr Literal") || op2.getdType().equals("Numbar Literal"))) {
						 Word invalidop;
						 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal"))) invalidop = op1;
						 else invalidop = op2;
						 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
						 Parser.error = true;
						 break;
					 }
					 //if at least one of the operand is float, the answer should be float
					 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
						 float op1Float = Float.parseFloat(op1.getValue1());
						 float op2Float = Float.parseFloat(op2.getValue1());
						 float opF = op1Float + op2Float;
						 op2.setValue(Float.toString(opF));
								 
					//else answer is int
					 }else {
						 int op1Int = Integer.parseInt(op1.getValue1());
						 int op2Int = Integer.parseInt(op2.getValue1());
						 int opI = op1Int + op2Int;
						 op2.setValue(Integer.toString(opI));
					 }
					 this.exprStack.add(0, op2);
					 this.stack = 0;
	
			     }else if(this.opStack.get(0).getValue1().equals("DIFF OF")) {
			    	 this.opStack.remove(0); //remove top operator
			    	 
			    	 this.exprStack.remove(0); //removes op2
					 this.exprStack.remove(0); //removes op1
					 this.exprStack.remove(0); //removes operator
					//catch operand error
					 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal")) || !(op2.getdType().equals("Numbr Literal") || op2.getdType().equals("Numbar Literal"))) {
						 Word invalidop;
						 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal"))) invalidop = op1;
						 else invalidop = op2;
						 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
						 Parser.error = true;
						 break;
					 }
					 //if at least one of the operand is float, the answer should be float
					 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
						 float op1Float = Float.parseFloat(op1.getValue1());
						 float op2Float = Float.parseFloat(op2.getValue1());
						 float opF = op1Float - op2Float;
						 op2.setValue(Float.toString(opF));
					//else answer is int
					 }else {
						 int op1Int = Integer.parseInt(op1.getValue1());
						 int op2Int = Integer.parseInt(op2.getValue1());
						 int opI = op1Int - op2Int;
						 op2.setValue(Integer.toString(opI));
					 }
					 this.exprStack.add(0, op2);
					 this.stack = 0;
			     }else if(this.opStack.get(0).getValue1().equals("PRODUKT OF")) {
			    	 this.opStack.remove(0); //remove top operator
			    	 this.exprStack.remove(0); //removes op2
					 this.exprStack.remove(0); //removes op1
					 this.exprStack.remove(0); //removes operator
					 
					//catch operand error
					 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal")) || !(op2.getdType().equals("Numbr Literal") || op2.getdType().equals("Numbar Literal"))) {
						 Word invalidop;
						 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal"))) invalidop = op1;
						 else invalidop = op2;
						 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
						 Parser.error = true;
						 break;
					 }
					 //if at least one of the operand is float, the answer should be float
					 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
						 float op1Float = Float.parseFloat(op1.getValue1());
						 float op2Float = Float.parseFloat(op2.getValue1());
						 float opF = op1Float * op2Float;
						 op2.setValue(Float.toString(opF));
					//else answer is int
					 }else {
						 int op1Int = Integer.parseInt(op1.getValue1());
						 int op2Int = Integer.parseInt(op2.getValue1());
						 int opI = op1Int * op2Int;
						 op2.setValue(Integer.toString(opI));
					 }
					 this.exprStack.add(0, op2);
					 this.stack = 0;
			     }else if(this.opStack.get(0).getValue1().equals("QUOSHUNT OF")) {
			    	 this.opStack.remove(0); //remove top operator
			    	 
			    	 this.exprStack.remove(0); //removes op2
					 this.exprStack.remove(0); //removes op1
					 this.exprStack.remove(0); //removes operator
					//catch operand error
					 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal")) || !(op2.getdType().equals("Numbr Literal") || op2.getdType().equals("Numbar Literal"))) {
						 Word invalidop;
						 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal"))) invalidop = op1;
						 else invalidop = op2;
						 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
						 Parser.error = true;
						 break;
					 }
					 //if at least one of the operand is float, the answer should be float
					 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
						 float op1Float = Float.parseFloat(op1.getValue1());
						 float op2Float = Float.parseFloat(op2.getValue1());
						 float opF = op1Float / op2Float;
						 op2.setValue(Float.toString(opF));
					//else answer is int
					 }else {
						 int op1Int = Integer.parseInt(op1.getValue1());
						 int op2Int = Integer.parseInt(op2.getValue1());
						 int opI = op1Int / op2Int;
						 op2.setValue(Integer.toString(opI));
					 }
					 this.exprStack.add(0, op2);
					 this.stack = 0;
					 
			     }else if(this.opStack.get(0).getValue1().equals("MOD OF")) {
			    	 this.opStack.remove(0); //remove top operator
			    	 this.exprStack.remove(0); //removes op2
					 this.exprStack.remove(0); //removes op1
					 this.exprStack.remove(0); //removes operator
					//catch operand error
					 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal")) || !(op2.getdType().equals("Numbr Literal") || op2.getdType().equals("Numbar Literal"))) {
						 Word invalidop;
						 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal"))) invalidop = op1;
						 else invalidop = op2;
						 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
						 Parser.error = true;
						 break;
					 }
					 //if at least one of the operand is float, the answer should be float
					 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
						 float op1Float = Float.parseFloat(op1.getValue1());
						 float op2Float = Float.parseFloat(op2.getValue1());
						 float opF = op1Float % op2Float;
						 op2.setValue(Float.toString(opF));
					//else answer is int
					 }else {
						 int op1Int = Integer.parseInt(op1.getValue1());
						 int op2Int = Integer.parseInt(op2.getValue1());
						 int opI = op1Int % op2Int;
						 op2.setValue(Integer.toString(opI));
					 }
					 this.exprStack.add(0, op2);
					 this.stack = 0;
					 
			     }else if(this.opStack.get(0).getValue1().equals("BIGGR OF")) {
			    	 this.opStack.remove(0); //remove top operator
			    	 this.exprStack.remove(0); //removes op2
					 this.exprStack.remove(0); //removes op1
					 this.exprStack.remove(0); //removes operator
					//catch operand error
					 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal")) || !(op2.getdType().equals("Numbr Literal") || op2.getdType().equals("Numbar Literal"))) {
						 Word invalidop;
						 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal"))) invalidop = op1;
						 else invalidop = op2;
						 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
						 Parser.error = true;
						 break;
					 }
					 //if at least one of the operand is float, the answer should be float
					 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
						 float op1Float = Float.parseFloat(op1.getValue1());
						 float op2Float = Float.parseFloat(op2.getValue1());
						 float opF = Math.max(op1Float,op2Float);
						 op2.setValue(Float.toString(opF));
					//else answer is int
					 }else {
						 int op1Int = Integer.parseInt(op1.getValue1());
						 int op2Int = Integer.parseInt(op2.getValue1());
						 int opI = Math.max(op1Int,op2Int);
						 op2.setValue(Integer.toString(opI));
					 }
					 this.exprStack.add(0, op2);
					 this.stack = 0;
					 
			     }else if(this.opStack.get(0).getValue1().equals("SMALLR OF")) {
			    	 this.opStack.remove(0); //remove top operator
			    	 this.exprStack.remove(0); //removes op2
					 this.exprStack.remove(0); //removes op1
					 this.exprStack.remove(0); //removes operator
					 
					//catch operand error
					 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal")) || !(op2.getdType().equals("Numbr Literal") || op2.getdType().equals("Numbar Literal"))) {
						 Word invalidop;
						 if(!(op1.getdType().equals("Numbr Literal") || op1.getdType().equals("Numbar Literal"))) invalidop = op1;
						 else invalidop = op2;
						 Interpreter.printError("Line "+invalidop.getLineNum()+": Arithmetic operation error: "+invalidop.getValue1()+" is not a numbr or numbar.");
						 Parser.error = true;
						 break;
					 }
					//if at least one of the operand is float, the answer should be float
					 else if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
						 float op1Float = Float.parseFloat(op1.getValue1());
						 float op2Float = Float.parseFloat(op2.getValue1());
						 float opF = Math.min(op1Float,op2Float);
						 op2.setValue(Float.toString(opF));
					//else answer is int
					 }else {
						 int op1Int = Integer.parseInt(op1.getValue1());
						 int op2Int = Integer.parseInt(op2.getValue1());
						 int opI = Math.min(op1Int,op2Int);
						 op2.setValue(Integer.toString(opI));
					 }
					 this.exprStack.add(0, op2);
					 this.stack = 0;
			      }
				 
				 //Comparison Operations
				//Both Saem Operation
				 else if(this.opStack.get(0).getValue1().equals("BOTH SAEM")) {
					 this.opStack.remove(0);
					 this.exprStack.remove(0); //removes op2
					 this.exprStack.remove(0); //removes op1
					 this.exprStack.remove(0); //removes operator
					 //if operand is a NUMBAR
					 if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal") ) {
						 float op1Float = Float.parseFloat(op1.getValue1());
						 float op2Float = Float.parseFloat(op2.getValue1());
						 if(op1Float == op2Float) {
							 op2.setValue("WIN");
						 }else {
							 op2.setValue("FAIL");
						 }	
					//if operand is a NUMBR
					 }else{
						 try {
							 int op1Int = Integer.parseInt(op1.getValue1());
							 int op2Int = Integer.parseInt(op2.getValue1());
							 if(op1Int == op2Int) {
								 op2.setValue("WIN");
							 }else {
								 op2.setValue("FAIL");
							 }
							//if operand is YARN or TROOF
						 }catch(NumberFormatException e) {
							 if(op1.getdType().equals("Yarn Literal") && op2.getdType().equals("Yarn Literal")) {
								 if(op1.getValue1().equals(op2.getValue1())) {
									 op2.setValue("WIN");
								 }else {
									 op2.setValue("FAIL");
								 }
							 }else if(op1.getdType().equals("Troof Literal") && op2.getdType().equals("Troof Literal")) {
								 if(op1.getValue1().equals(op2.getValue1())) {
									 op2.setValue("WIN");
								 }else {
									 op2.setValue("FAIL");
								 }
							 }else {
								 op2.setValue("FAIL");
							 }
						 }
					 }
					 op2.setdType("Troof Literal");
					 this.exprStack.add(0, op2);
					 this.stack = 0;
					 
				//Diffrint Operation
			     }else if(this.opStack.get(0).getValue1().equals("DIFFRINT")) {
			    	 this.opStack.remove(0);
					 this.exprStack.remove(0); //removes op2
					 this.exprStack.remove(0); //removes op1
					 this.exprStack.remove(0); //removes operator
					 
					//if operand is a NUMBAR
					 if(op1.getdType().equals("Numbar Literal") || op2.getdType().equals("Numbar Literal")) {
						 float op1Float = Float.parseFloat(op1.getValue1());
						 float op2Float = Float.parseFloat(op2.getValue1());
						 if(op1Float != op2Float) {
							 op2.setValue("WIN");
						 }else {
							 op2.setValue("FAIL");
						 }	
					//if operand is a NUMBR
					 }else{
						 try {
							 int op1Int = Integer.parseInt(op1.getValue1());
							 int op2Int = Integer.parseInt(op2.getValue1());
							 if(op1Int != op2Int) {
								 op2.setValue("WIN");
							 }else {
								 op2.setValue("FAIL");
							 }
						//if operand is YARN or TROOF
						 }catch(NumberFormatException e) {
							 if(op1.getdType().equals("Yarn Literal") && op2.getdType().equals("Yarn Literal")) {
								 if(op1.getValue1().equals(op2.getValue1())) {
									 op2.setValue("FAIL");
								 }else {
									 op2.setValue("WIN");
								 }
							 }else if(op1.getdType().equals("Troof Literal") && op2.getdType().equals("Troof Literal")) {
								 if(op1.getValue1().equals(op2.getValue1())) {
									 op2.setValue("FAIL");
								 }else {
									 op2.setValue("WIN");
								 }
							 }else {
								 op2.setValue("WIN");
							 }
						 }
					 }
					 op2.setdType("Troof Literal");
					 this.exprStack.add(0, op2);
					 this.stack = 0;
			     
			//Concatenation Operation
			 }else if(this.opStack.get(0).getValue1().equals("SMOOSH")) {
				 Word op = this.exprStack.get(0);
				 this.opStack.remove(0); //remove top operator
				 this.exprStack.remove(0); //remove operator
				 this.exprStack.remove(0); //remove operand
				 //if next word in expr stack is a yarn literal, concatenate all succeeding words  
				 if(exprSize>2 && (exprStack.get(0).getValue2().equals("Yarn Literal") || exprStack.get(0).getValue2().equals("Troof Literal") || exprStack.get(0).getValue2().equals("Numbr Literal") || exprStack.get(0).getValue2().equals("Numbar Literal")  )) {
					 String concat = op.getValue1() ;
					 while(exprStack.size()!=0 && (exprStack.get(0).getValue2().equals("Yarn Literal") || exprStack.get(0).getValue2().equals("Troof Literal") || exprStack.get(0).getValue2().equals("Numbr Literal") || exprStack.get(0).getValue2().equals("Numbar Literal")  )) {
						 concat+=exprStack.get(0).getValue1(); //concatenate
						 exprStack.remove(0); //remove the yarn literal in stack
					 }
					 op1.setValue(concat);
				 }else {
					 op1.setValue(op.getValue1());
				 }
				 this.exprStack.add(0, op1); //replace top of stack with result of operation
				 this.stack = 0;
			 }
			 }
//			 if(Parser.error) break;
			if(Parser.error) {
				ans.add("null");
				ans.add("null");
				return ans;
			}
			ans.add(this.exprStack.get(0).getValue1()); //value
			ans.add(this.exprStack.get(0).getValue2()); //type --not final-- getdType() sana 
			return ans;
		 }
	 }
}
