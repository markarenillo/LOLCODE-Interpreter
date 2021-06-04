package application;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;

//Parser is used to check whether a line is syntactically correct or not
public class Parser {
	ArrayList<Word> stream;
	ArrayList<Regexp> regex;
	String topToken;
	Word topWord;
	int lineNum;
	boolean valid;
	boolean deepValid; 
	String currentVar;
	String currentVal;
	String currentType;
	SemanticAnalyzer semAnalyzer;
	boolean fromArith;
	boolean fromBool;
	ArrayList<Word> currentExpr;
	boolean fromExpr;
	boolean fromPrint;
	String currentPrint;
	boolean fromSwitch;
	boolean ifOMGWTF;
	boolean fromInput;
	SymbolTable symbolTable;
	ArrayList<String> toPrint;
	boolean elseTrue;
	boolean ifelseFinish;
	static boolean error;
	boolean vardecFinish;
	boolean fromIf;
	boolean fromElse;
	boolean fromElseif;
	boolean validAssign;
	boolean switchBreak;
	boolean switchTrue;
	boolean switchFinish;
	boolean omgwtfCase;
	boolean skipSwitchBreak;
	boolean skipSwitchFinish;
	boolean skipOmgwtfCase;
	
	//tokenStream and regex list as parameters
	Parser(ArrayList<Word> stream, ArrayList<Regexp> regex){
		this.stream = stream;
		this.valid = true;
		this.validAssign = true;
		this.deepValid = true;
		this.regex = regex;
		this.fromArith = false;
		this.fromBool = false;
		this.fromSwitch = false;
		this.toPrint = new ArrayList<String>();
		error = false;
	}
	
	//return the updated symboltable
	SymbolTable parseLine(SymbolTable symbolTable){
		this.currentExpr = new ArrayList<Word>();
		this.semAnalyzer = new SemanticAnalyzer();
		this.topToken = this.stream.get(0).getValue1();
		this.topWord = this.stream.get(0);
		this.lineNum = this.stream.get(0).getLineNum();
		this.stream.remove(0);
		this.symbolTable = symbolTable;
		program();
		return this.symbolTable;
	}
	
	//pop and update the current topToken
	boolean nextToken() {
		//if tokenStream is not empty, assign the next token to topToken
		if (!this.stream.isEmpty()) {
			this.topToken = this.stream.get(0).getValue1();
			this.topWord = this.stream.get(0);
			this.lineNum = this.stream.get(0).getLineNum();
			this.stream.remove(0);
			return true;
		}else {
			//else if topToken is KTHXBYE and tokenStream is empty, code is valid
			if(this.topToken.equals("KTHXBYE") && this.stream.isEmpty()) {
				return true;
			//else code is not valid
			}else {
				System.out.println("NOT VALID CODE");
			}
		}
		return false;
	}
	
	//RULE: <program> ::= HAI <linebreak> <statement> KTHXBYE
	//check if the succeeding tokens follow this rule
	void program() {
		if(this.topToken.equals("HAI")) {
			//update topToken
			nextToken();
			//check if succeeding tokens follow lineBreak() && statement() syntax
			if(lineBreak() && statement()) {
			//else display error
			}else {
				if(this.validAssign == false) {
					Interpreter.printError("Line "+this.lineNum+": Invalid assignment operation syntax.");
				}
			}
		}else {
			System.out.println("NOT VALID CODE");
		}
	}
	//check if topToken is "KTHXBYE"
	boolean bye() {
		if(this.topToken.equals("KTHXBYE")) {
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	//check if topToken is a linebreak
	boolean lineBreak() {
		//display error if toptoken is not a linebreak after a variable declaration
		if(this.vardecFinish && !this.topToken.equals("\\n")) {
			Interpreter.printError("Line "+this.lineNum+": Expected: newline; Got: "+this.topWord.getValue2()+"("+this.topToken+").");
			error = true;
			return false;
		}
		else if(this.topToken.equals("\\n")) {
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	//RULE: <statement>::= <print> <statement> | <input> <statement> | <vardec> <statement> | <flow> <statement> | <assignment> <statement> | <expr> <statement> | <linebreak> <statement> | <linebreak> 
	//check if the succeeding tokens follow this rule
	boolean statement(){
		//return true if an error is encountered
		if(error) return true;
		//check if the succeeding tokens follow print() && lineBreak() syntax
		if(print() && lineBreak()) {
			//deactivate print statement flag (used for printing/displaying tokens)
			this.fromPrint = false;
			if(bye()) {
				return true;
			}
			//check if the succeeding tokens follows another statement rule
			if(statement()){
				return true;
			}
			
		}
		//if error is encountered after checking the print() && lineBreak() syntax, return false
		if(this.deepValid == false) {
			return false;
		}
		if(error) return true;
		
		//check if the succeeding tokens follow input() && lineBreak()syntax
		if(input() && lineBreak()) {
			if(bye()) {
				return true;
			}
			//check if the succeeding tokens follows another statement rule
			if(statement()) {
				return true;
			}
		}
		//if error is encountered after checking the input() && lineBreak() syntax, return false
		if(this.deepValid == false) {
			return false;
		}
		if(error) return true;
		//check if the succeeding tokens follow vardec() && lineBreak()syntax
		if(vardec() && lineBreak()) {
			if(bye()) {
				return true;
			}
			//check if the succeeding tokens follows another statement rule
			if(statement()) {
				return true;
			}
		}
		//if error is encountered after checking the vardec() && lineBreak() syntax, return false
		if(this.deepValid ==false) {
			return false;
		}
		if(error) return true;
		//check if the succeeding tokens follow flow() && lineBreak()syntax
		if(flow() && lineBreak()) {
			if(bye()) {
				return true;
			}
			//check if the succeeding tokens follows another statement rule
			if(statement()) {
				return true;
			}
		}
		//if error is encountered after checking the flow() && lineBreak() syntax, return false
		if(this.deepValid== false ) {
			return false;
		}
		if(error) return true;
		
		//check if the succeeding tokens follow expr() && lineBreak()syntax
		if(expr() && lineBreak()) {
			if(error) return true;
			//deactivate expression statement flag (used for semantic analysing)
			this.fromExpr = false;
			//evaluate the expression
			ArrayList<String> ans = this.semAnalyzer.evaluate(this.currentExpr);
			//assign answer to IT
			this.semAnalyzer.setVarValue("IT", ans.get(0));
			this.semAnalyzer.setVarType("IT", ans.get(1), this.topWord.getLineNum());
			Word symbol = new Word("IT",ans.get(0),0,0);
			this.symbolTable.add(symbol);
			
			this.currentExpr.clear();
			if(bye()) {
				return true;
			}
			//check if the succeeding tokens follows another statement rule
			if(statement()) {
				return true;
			}
		}
		
		//if error is encountered after checking the expr() && lineBreak() syntax, return false
		if(this.deepValid ==  false ) {
			return false;
		}
		if(error) return true;
		//check if the succeeding tokens follow assignment() && lineBreak()syntax
		if(assignment() && lineBreak()) {
			this.fromExpr = false;
			if(bye()) {
				return true;
			}
			//check if the succeeding tokens follows another statement rule
			if(statement()) {
				return true;
			}
		}
		
		//if error is encountered after checking the assignment() && lineBreak() syntax, return false
		if(this.deepValid == false ) {
			return false;
		}
		if(error) return true;
		
		this.valid = false;
		return false;	
	}

	//RULE:	<print> ::= VISIBLE <vis_ops>
	//check if the succeeding tokens follow this rule
	boolean print() {
		//check if topToken is "VISIBLE"
		if(this.topToken.equals("VISIBLE")) {
			nextToken();
			//activate print statement flag for printing tokens
			this.fromPrint = true;
			//check if succeeding tokens follow visOps syntax
			if(visOps()) {
				if(error) return true;
				//print result
				Interpreter.printResult(this.toPrint);
				this.currentPrint = "";
				this.toPrint.clear();
				return true;
			}else {
				//display error in print syntax
				Interpreter.printError("Line "+this.lineNum+": Invalid start of expression.");
				error = true;
			}
			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}

	//RULE: <vis_ops>::= <literal> <vis_ops> | <expr> <vis_ops> | <var> <vis_ops> | <var> | <expr> | <literal>
	//check if the succeeding tokens follow this rule
	boolean visOps() {
		//check if topToken is a literal
		if(literal()) {
			if(this.topToken.equals("\\n")) {
				return true;
			}
			if(visOps()) {
				return true;
			}
		}
		//check if the succeeding tokens is an expression
		else if(expr()) {
			if(error) return true;
			//deactivate expression statement flag to evaluate the current expression
			this.fromExpr = false;
			ArrayList<String> ans = this.semAnalyzer.evaluate(this.currentExpr);
			this.currentExpr.clear();
			this.currentPrint = ans.get(0);
			this.toPrint.add(this.currentPrint);
			if(this.topToken.equals("\\n")) {
				return true;
			}
			if(visOps()) {
				return true;
			}
		}
		//check if topToken is a variable
		else if(var()) {
			if(this.topToken.equals("\\n")) {
				return true;
			}
			if(visOps()) {
				return true;
			}
		}
		this.valid = false;
		return false;
	}
	//RULE:	<var> =  IT | varident
	//check if the succeeding tokens follow this rule
	boolean var(){
		//check if topToken is "IT"
		if(this.topToken.equals("IT")) {
			this.currentVar = this.topToken; //holds recent read variable name
			this.currentType = "variable";
			
			//if expression flag is activated, add topToken to arrayList currentExpr
			if(this.fromExpr) {
				//error if variable is null
				if(this.semAnalyzer.getVarValue(this.topToken)==null) {
	    			//display error message
	    			Interpreter.printError("Line "+this.topWord.getLineNum()+": Reference to uninitialized variable: "+this.topToken+".");
	    			error = true;
	    		}
				this.currentExpr.add(this.topWord);
//				this.fromExpr = false;
			}
			//if print flag is activated, add topToken to arrayList currentPrint
			else if(this.fromPrint) {
				if(this.semAnalyzer.checkVar("IT")) {
					//error if variable is null
					if(this.semAnalyzer.getVarValue(this.topToken)==null) {
						//display error message
		    			Interpreter.printError("Line "+this.topWord.getLineNum()+": Reference to uninitialized variable: "+this.topToken+".");
		    			error = true;
		    		}
					this.currentPrint = this.semAnalyzer.getVarValue("IT");
					this.toPrint.add(this.currentPrint);
				}
			//if input flag is activated, display input dialog, and assign the input to variable
			}else if(this.fromInput) {
				this.inputDialog(this.currentVar);
				this.fromInput = false;
			}
			nextToken();
			return true;
		}
		//check if toptoken is a variable identifier
		if(varident()) {
			return true;
		}
		this.valid = false;
		return false;
		
	}
	//check if variable identifier
	boolean varident() {
		//check if topToken is a keyword
		if(checkRegex(this.topToken)) {
			this.valid = false;
			return false;
		//check if topToken is a linebreak
		}else if(this.topToken.equals("\\n")){
			this.valid = false;
			return false;
		}
		//initialize pattern
		Pattern pattern = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");
	    Matcher matcher = pattern.matcher(this.topToken);
	    //check if pattern and topToken is matched
		if(matcher.find()) {
			//if expression flag is activated, add topToken to arrayList currentExpr
			if(this.fromExpr) {
				//check if variable is valid
				if(this.semAnalyzer.isValid(this.topToken, this.topWord.getLineNum())) {
					if(this.semAnalyzer.getVarValue(this.topToken)==null) {
						//display error message
		    			Interpreter.printError("Line "+this.topWord.getLineNum()+": Reference to uninitialized variable: "+this.topToken+".");
		    			error = true;
		    		}
					this.currentExpr.add(this.topWord);
				}
			}
			//if print flag is activated, add topToken to arrayList currentPrint
			else if(this.fromPrint) {
				if(this.semAnalyzer.isValid(this.topToken, this.topWord.getLineNum())) {
					if(this.semAnalyzer.getVarValue(this.topToken)==null) {
						//display error message
		    			Interpreter.printError("Line "+this.topWord.getLineNum()+": Reference to uninitialized variable: "+this.topToken+".");
		    			error = true;
		    		}
					this.currentPrint = this.semAnalyzer.getVarValue(this.topToken);
					this.toPrint.add(this.currentPrint);
				}
			//if input flag is activated, display input dialog, and assign input to variable
			}else if(this.fromInput) {
				this.inputDialog(this.topToken);
				this.fromInput = false;
			}
			
			this.currentVar = this.topToken; //holds recent read variable name
			this.currentType = "variable";
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	//RULE: <input> ::= GIMMEH <var> <linebreak>
	//check if the succeeding tokens follow this rule
	boolean input() {
		//if topToken is GIMMEH
		if(this.topToken.equals("GIMMEH")) {
			nextToken();
			//activate input flag to get input from user
			this.fromInput = true;
			//check if topToken is a variable
			if(var()) {
				return true;
			//display error in input statement syntax if topToken is a linebreak
			}else if(this.topToken.equals("\\n")){
				Interpreter.printError("Line "+this.lineNum+": Expected: identifier; Got: newline.");
				error = true;
			//display error in input statement syntax if topToken is a linebreak
			}else {
				Interpreter.printError("Line "+this.lineNum+": Expected: identifier; Got: "+this.topToken+".");
				error = true;
			}
			this.deepValid = false;
		}
		this.valid = false;
		return false;	
	}
	
	//RULE: <vardec> ::= I HAS A varident <linebreak> | I HAS A varident ITZ <literal> <linebreak> | I HAS A varident ITZ varident <linebreak> |I HAS A varident ITZ <expr> <linebreak>
	//check if the succeeding tokens follow this rule
	boolean vardec() {
		//check if topToken is I HAS A
		if(this.topToken.equals("I HAS A")) {
			nextToken();
			//check if topToken is a variable identifier
			if(varident()){
				String name = this.currentVar; //holds the variable name in case varident/expr is the second operand
				if(this.topToken.equals("ITZ")) {
					nextToken();
					//check if toptoken is a literal
					if(literal()) {
						//if variable is already declared, display error
						if(this.semAnalyzer.checkVar(name)) {
							Interpreter.printError("Line "+this.lineNum+": Variable "+name+" already declared.");
							error = true;
						//else assign value to the variable
						}else {
							this.semAnalyzer.addVar(this.currentVar); //adds variable name
							this.semAnalyzer.setVarValue(this.currentVar, this.currentVal); //sets value of variable
							this.semAnalyzer.setVarType(this.currentVar, this.currentType, this.topWord.getLineNum()); //sets type of variable
							Word symbol = new Word(this.currentVar, this.currentVal,0,0);
							this.symbolTable.add(symbol);
						}
						this.vardecFinish = true;
						return true;
					//check if toptoken is a varident
					}else if(varident()) {
						//if variable is already declared, display error
						if(this.semAnalyzer.checkVar(name)) { 
							Interpreter.printError("Line "+this.lineNum+": Variable "+name+" already declared.");
							error = true;
						//else assign value to the variable
						}else {
							this.semAnalyzer.addVar(name); //adds variable name
							this.semAnalyzer.setVarValue(name, this.currentVar); //sets value of variable
							this.semAnalyzer.setVarType(name, this.currentType, this.topWord.getLineNum()); //sets type of variable
							Word symbol = new Word(name,this.semAnalyzer.getVarValue(this.currentVar) ,0,0);
							this.symbolTable.add(symbol);
						}
						this.vardecFinish = true;
						return true;
					}else if(expr()) {
						if(error) return true;
						this.fromExpr = false;
						//evaluate expression
						ArrayList<String> ans = this.semAnalyzer.evaluate(this.currentExpr);
						//if variable is already declared, display error
						if(this.semAnalyzer.checkVar(name)) { 
							Interpreter.printError("Line "+this.lineNum+": Variable "+name+" already declared.");
							error = true;
						//else assign value to the variable
						}else {
							this.semAnalyzer.addVar(name); //adds variable name
							this.semAnalyzer.setVarValue(name, ans.get(0)); //sets value of variable
							this.semAnalyzer.setVarType(name, ans.get(1), this.topWord.getLineNum()); //sets type of variable
							Word symbol = new Word(name, ans.get(0),0,0);
							this.symbolTable.add(symbol);
						}
						this.vardecFinish = true;
						return true;
					}else {
						//ERROR IN VARDEC STATEMENT SYNTAX
						Interpreter.printError("Line "+this.lineNum+": Invalid start of expression.");
						error = true;
						return false;
					}
				//if topToken is a linebreak
				}else if(this.topToken.equals("\\n")){
					//if variable is not added, add variable to the list
					if(this.semAnalyzer.checkVar(this.currentVar) == false) { //new variable
						this.semAnalyzer.addVar(this.currentVar); //adds variable name
					//else display error
					}else {
						Interpreter.printError("Line "+this.lineNum+": Variable "+name+" already declared.");
						error = true;
					}
					this.vardecFinish = true;
					return true;
				//else display error in vardec statement syntax
				}else {
					//ERROR IN VARDEC STATEMENT SYNTAX
					Interpreter.printError("Line "+this.lineNum+": Expected: newline; Got: "+this.topWord.getValue2()+"("+this.topToken+").");
					error = true;
					return false;
				}
			}
			this.deepValid = false;	
		}
		this.valid = false;
		return false;
	}
	
	//RULE: <literal> ::= numbr | numbar | yarn | troof
	//check if the succeeding tokens follow this rule
	boolean literal() {
		//check if topToken is a numbr
		if(numbr()) {
			return true;
		//check if topToken is a numbar
		}else if(numbar()) {
			return true;
		//check if topToken is a yarn
		}else if(yarn()) {
			return true;
		//check if topToken is a troof
		}else if(troof()) {
			return true;
		}
			this.valid = false;
			return false;
		
	}
	//check if topToken is a numbr
	boolean numbr() {
		Pattern pattern = Pattern.compile("^-?[0-9]+$");
	    Matcher matcher = pattern.matcher(this.topToken);
	    //if pattern matches the topToken
		if(matcher.find()) {		
			//if expression flag is activated, add toptoken to arraylist currentExpr
			if(this.fromExpr) {
				this.currentExpr.add(this.topWord);
			}
			//if print flag is activated, add toptoken to arraylist currentPrint
			else if(this.fromPrint) {
				this.currentPrint = this.topToken;
				this.toPrint.add(this.currentPrint);
			}
			this.currentVal = this.topToken;
			this.currentType = "numbr";
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	//check if topToken is a numbar
	boolean numbar() {
		Pattern pattern = Pattern.compile("^-?[0-9]+.[0-9]+$");
	    Matcher matcher = pattern.matcher(this.topToken);
	    //if pattern matches the topToken
		if(matcher.find()) {
			//if expression flag is activated, add toptoken to arraylist currentExpr
			if(this.fromExpr) {
				this.currentExpr.add(this.topWord);
			}
			//if print flag is activated, add toptoken to arraylist currentPrint
			else if(this.fromPrint) {
				this.currentPrint = this.topToken;
				this.toPrint.add(this.currentPrint);
			}
			this.currentVal = this.topToken;
			this.currentType = "numbar";
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	//check if topToken is a yarn
	boolean yarn() {
		Pattern pattern = Pattern.compile("\"[^\"]*\"");
	    Matcher matcher = pattern.matcher(this.topToken);
	    //if pattern matches the topToken
		if(matcher.find()) {
			//if expression flag is activated, add toptoken to arraylist currentExpr
			if(this.fromExpr) {
				this.currentExpr.add(this.topWord);
			}
			//if print flag is activated, add toptoken to arraylist currentPrint
			else if(this.fromPrint) {
				this.currentPrint = this.topToken.replace("\"", "");
				this.toPrint.add(this.currentPrint);
			}
			
			
			this.currentVal = this.topToken;
			this.currentType = "yarn";
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	//check if topToken is a yarn
	boolean troof() {
		Pattern pattern = Pattern.compile("^WIN$|^FAIL$");
	    Matcher matcher = pattern.matcher(this.topToken);
	    //if pattern matches the topToken
		if(matcher.find()) {
			
			//if expression flag is activated, add toptoken to arraylist currentExpr
			if(this.fromExpr) {
				this.currentExpr.add(this.topWord);
			}
			//if print flag is activated, add toptoken to arraylist currentPrint
			else if(this.fromPrint) {
				this.currentPrint = this.topToken;
				this.toPrint.add(this.currentPrint);
			}
			this.currentVal = this.topToken;
			this.currentType = "troof";
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	

	//RULE: <expr> ::= <arith> <linebreak> | <bool> <linebreak> | <bool_in> <linebreak> | <comp> <linebreak> | <concat> <linebreak> | <rel> <linebreak>
	//check if the succeeding tokens follow this rule
	boolean expr() {
		//check if succeeding tokens is an arithmetic expression
		if(arith()) {
			return true;
		}
		//check if succeeding tokens is a boolean expression
		if(bool()) {
			return true;
		}
		//check if succeeding tokens is a boolean_in expression
		if(boolIn()) {
//			this.fromExpr = false;
			return true;
		}
		//check if succeeding tokens is an comparison expression
		if(comp_op()) {
//			this.fromExpr = false;
			return true;
		}
		//check if succeeding tokens is a concatenation expression
		if(concat()) {
//			this.fromExpr = false;
			return true;
		}
		this.valid = false;
		return false;
	}
	//RULE: <arith> ::= SUM OF <arith_op> AN <arith_op> | DIFF OF <arith_op> AN <arith_op> | PRODUKT OF <arith_op> AN <arith_op> | QUOSHUNT OF <arith_op> AN <arith_op> | MOD OF <arith_op> AN<arith_op> | BIGGR OF <arith_op> AN <arith_op> | SMALLR OF <arith_op> AN <arith_op>
	//check if the succeeding tokens follow this rule
	boolean arith() {
		//check if topToken is SUM OF
		if(this.topToken.equals("SUM OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding token is an arithmetic operand
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					//display an error if not an arithmetic operand
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		//check if topToken is DIFF OF
		else if(this.topToken.equals("DIFF OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding token is an arithmetic operand
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					//display an error if not an arithmetic operand
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		//check if topToken is PRODUKT OF
		else if(this.topToken.equals("PRODUKT OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding token is an arithmetic operand
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					//display an error if not an arithmetic operand
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		//check if topToken is QUOSHUNT OF
		else if(this.topToken.equals("QUOSHUNT OF")) {
			this.fromExpr = true;

			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding token is an arithmetic operand
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					//display an error if not an arithmetic operand
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		//check if topToken is MOD OF
		else if(this.topToken.equals("MOD OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding token is an arithmetic operand
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					//display an error if not an arithmetic operand
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		//check if topToken is BIGGR OF
		else if(this.topToken.equals("BIGGR OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding token is an arithmetic operand
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					//display an error if not an arithmetic operand
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		//check if topToken is SMALLR OF
		else if(this.topToken.equals("SMALLR OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding token is an arithmetic operand
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					//display an error if not an arithmetic operand
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	
	//	RULE: <arith_op> ::= SUM OF <arith_op> AN <arith_op> | DIFF OF <arith_op> AN <arith_op> | PRODUKT OF
	//		<arith_op> AN <arith_op> | QUOSHUNT OF <arith_op> AN <arith_op> | MOD OF <arith_op> AN
	//		<arith_op> | BIGGR OF <arith_op> AN <arith_op> | SMALLR OF <arith_op> AN <arith_op> |
	//		<num> | <var>		
	//check if the succeeding tokens follow this rule
	boolean arith_op() {
		//check if toptoken is a number
		if(num()) {
			return true;
		}
		//check if toptoken is a variable
		else if(var()) {
			return true;
		}
		//check if toptoken is arithmetic statement
		else if(this.topToken.equals("SUM OF")) {
//			this.semAnalyzer.pushArith("SUM OF");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("DIFF OF")) {
//			this.semAnalyzer.pushArith("DIFF OF");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					}else {
						exprError();
						return false;
					} 
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}

		}
		else if(this.topToken.equals("PRODUKT OF")) {
//			this.semAnalyzer.pushArith("PRODUKT OF");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("QUOSHUNT OF")) {
//			this.semAnalyzer.pushArith("QUOSHUNT OF");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("MOD OF")) {
//			this.semAnalyzer.pushArith("MOD OF");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		
		else if(this.topToken.equals("BIGGR OF")) {
//			this.semAnalyzer.pushArith("BIGGR OF");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("SMALLR OF")) {
//			this.semAnalyzer.pushArith("SMALLR OF");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(arith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(arith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		this.valid = false;
		return false;
	}
	//RULE: <num> ::= numbr | numbar
	//check if the succeeding tokens follow this rule
	boolean num() {
		//check if toptoken is a numbr
		if(numbr()) {
			return true;
		}
		//check if toptoken is a numbar
		if(numbar()) {
			return true;
		}
		this.valid = false;
		return false;
	}
	
	//<bool> ::= BOTH OF <bool_op> AN <bool_op> | EITHER OF <bool_op> AN <bool_op> | WON OF <bool_op> AN <bool_op> | NOT <bool_op>
	//check if the succeeding tokens follow this rule
	boolean bool() {
		//check if toptoken is BOTH OF
		if(this.topToken.equals("BOTH OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding tokens follows bool_op() && AN syntax
			if(bool_op() && this.topToken.equals("AN")) {
				nextToken();
				//check if toptoken is boolean operator
				if(bool_op()) {
					return true;
				//else display an error
				}else {
					exprError();
					return false;
				}				
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		//check if toptoken is EITHER OF
		else if(this.topToken.equals("EITHER OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding tokens follows bool_op() && AN syntax
			if(bool_op() && this.topToken.equals("AN")) {
				nextToken();
				//check if toptoken is boolean operator
				if(bool_op()) {
					return true;
				//else display an error
				}else {
					exprError();
					return false;
				}	
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		//check if toptoken is WON OF
		else if(this.topToken.equals("WON OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding tokens follows bool_op() && AN syntax
			if(bool_op() && this.topToken.equals("AN")) {
				nextToken();
				//check if toptoken is boolean operator
				if(bool_op()) {
					return true;
				//else display an error
				}else {
					exprError();
					return false;
				}		
			}else {
				exprError();
				return false;
			}
		}
		//check if toptoken is NOT
		else if(this.topToken.equals("NOT")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if toptoken is boolean operator
			if(bool_op()) {
				return true;
			//else display an error
			}else { 
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	
	//RULE: <bool_op> ::= BOTH OF <bool_op> AN <bool_op> | EITHER OF <bool_op> AN <bool_op> | WON OF <bool_op> AN <bool_op> | NOT <bool_op> | troof | <var>
	//check if the succeeding tokens follow this rule
	boolean bool_op() {
		//check if toptoken is a troof
		if(troof()) {
			return true;
		}
		//check if toptoken is a variable
		else if(var()) {
			return true;
		}
		//check if succeeding tokens is a boolean statement
		else if(this.topToken.equals("BOTH OF")) {
//			this.semAnalyzer.pushBool("BOTH OF", "operator");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(bool_op() && this.topToken.equals("AN")) {
				nextToken();
				if(bool_op()) {
					return true;
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("EITHER OF")) {
//			this.semAnalyzer.pushBool("EITHER OF", "operator");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(bool_op() && this.topToken.equals("AN")) {
				nextToken();
				if(bool_op()) {
					return true;
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("WON OF")) {
//			this.semAnalyzer.pushBool("WON OF", "operator");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(bool_op() && this.topToken.equals("AN")) {
				nextToken();
				if(bool_op()) {
					return true;
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("NOT")) {
//			this.semAnalyzer.pushBool("NOT", "operator");
			this.currentExpr.add(this.topWord);
			nextToken();
			if(bool_op()) {
				return true;
			}else {
				exprError();
				return false;
			}
		}
		this.valid = false;
		return false;
	}
	
	//RULE: <bool_in> ::= ALL OF <bool> AN <bool> <anblock> MKAY | ANY OF <bool> AN <bool> <anblock> MKAY | ALL OF <bool> AN <bool> <anblock> | ANY OF <bool> AN <bool> <anblock> 
	//check if the succeeding tokens follow this rule
	boolean boolIn(){
		//check if toptoken is ALL OF
		if(this.topToken.equals("ALL OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding tokens follow anBlock syntax
			if(anBlock()) {
				//then check if topToken is MKAY
				if(this.topToken.equals("MKAY")) {
					nextToken();
					return true;
				}else {
					//ERROR IN BOOLEAN OPERATION SYNTAX
					Interpreter.printError("Line "+this.lineNum+": Expected: keyword(MKAY); Got: "+this.topToken+".");
					error = true;
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}		
		//check if toptoken is ANY OF
		else if(this.topToken.equals("ANY OF")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if succeeding tokens follow anBlock syntax
			if(anBlock()) {
				//then check if topToken is MKAY
				if(this.topToken.equals("MKAY")) {
					nextToken();
					return true;
				}else {
					//ERROR IN BOOLEAN OPERATION SYNTAX
					Interpreter.printError("Line "+this.lineNum+": Expected: keyword(MKAY); Got: "+this.topToken+".");
					error = true;
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	//RULE: <anblock> ::= AN <bool> || AN <bool> <anblock>
	//check if the succeeding tokens follow this rule
	boolean anBlock() {
		//check if toptoken is a boolean operator
		if(bool_op()) {
			if(this.topToken.equals("AN")) {
				nextToken();
				anBlock();
			}
			return true;
		}
		this.valid = false;
		return false;
	
	}
	
	//RULE: <comp_op> ::= BOTH SAEM <operand> <comp> | DIFFRINT <operand> <comp> | BOTH SAEM <operand> <rel> | DIFFRINT <operand> <rel> 
	//check if the succeeding tokens follow this rule
	boolean comp_op() {
		//check if toptoken is BOTH SAEM
		if(this.topToken.equals("BOTH SAEM")) { 
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if toptoken is an operand
			if(operand()) {
				//check if succeeding toptoken is a comparison statement
				if(comp()) {
					return true;
				//check if succeeding toptoken is a relation statement
				}else if(rel()) {
					return true;
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		//check if toptoken is DIFFRINT
		else if(this.topToken.equals("DIFFRINT")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if toptoken is an operand
			if(operand()) {
				//check if succeeding toptoken is a comparison statement
				if(comp()) {
					return true;
				//check if succeeding toptoken is a relation statement
				}else if(rel()) {
					return true;
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	
	//RULE: <comp> ::= AN <operand>
	//check if the succeeding tokens follow this rule
	boolean comp(){
		//check if toptoken is AN
		if(this.topToken.equals("AN")) {
			nextToken();
			//check if toptoken is an operand
			if(operand()) {
				return true;
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	//<rel> ::= AN BIGGR OF <operand> AN <operand> | AN SMALLR OF <operand> AN <operand>
	//check if the succeeding tokens follow this rule
	boolean rel(){
		//check if toptoken is BIGGR OF
		if(this.topToken.equals("BIGGR OF")) {
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if  toptoken is an operand
			if(operand()) {
				//check if  toptoken is AN
				if(this.topToken.equals("AN")) {
					nextToken();
					//check if  toptoken is an operand
					if(operand()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		//check if toptoken is SMALLR OF
		else if(this.topToken.equals("SMALLR OF")) {
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if  toptoken is an operand
			if(operand()) {
				//check if  toptoken is AN
				if(this.topToken.equals("AN")) {
					nextToken();
					//check if  toptoken is an operand
					if(operand()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	
	//RULE: <concat> ::= SMOOSH <str>
	//check if the succeeding tokens follow this rule
	boolean concat() {
		//check if  toptoken is SMOOSH
		if(this.topToken.equals("SMOOSH")) {
			this.fromExpr = true;
			this.currentExpr.add(this.topWord);
			nextToken();
			//check if  toptoken is a string
			if(str()) {
				return true;
			}else {
				literalError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	//RULE: <str> ::= literal AN <str> | literal
	//check if the succeeding tokens follow this rule
	boolean str() {
		//check if  toptoken is a literal
		if(literal()) {
			if(this.topToken.equals("AN")) {
				nextToken();
				if(str()) {
					return true;
				}else {
					literalError();
					return false;
				}
			}
			return true;
		}
		this.valid = false;
		return false;
	}
	
	//RULE: <operand> ::= <var> | <literal> | <arith> | <bool> | <bool_in> | <comp> | <concat> | <rel>
	//check if the succeeding tokens follow this rule
	boolean operand() {
		if(var()) {
			return true;
		}
		else if(literal()) {
			return true;
		}
		else if(arith()) {
			return true;
		}
		else if(bool()) {
			return true;
		}
		else if(boolIn()) {
			return true;
		}
		else if(comp_op()) {
			return true;
		}
		else if(concat()) {
			return true;
		}
		this.valid = false;
		return false;
	}
	//RULE: <flow> ::= <ifelse> <linebreak> | <switch> <linebreak>
	//check if the succeeding tokens follow this rule
	boolean flow() {
		//check if the succeeding tokens follow ifelse syntax
		if(ifelse()) {
			return true;
		} 
		//check if the succeeding tokens follow switch syntax
		else if(swtch()) {
			return true;
		}
		this.valid = false;
		return false;
	}
	
	//methods with skip is used in flow. this would check the syntax of the codeblocks without analyzing its semantics
	boolean skipVisOps() {
		if(skipLiteral()) {
			if(this.topToken.equals("\\n")) {
				return true;
			}
			if(skipVisOps()) {
				return true;
			}
		}
		else if(skipExpr()) {
			if(this.topToken.equals("\\n")) {
				return true;
			}
			if(skipVisOps()) {
				return true;
			}
		}
		else if(skipVar()) {
			if(this.topToken.equals("\\n")) {
				return true;
			}
			if(skipVisOps()) {
				return true;
			}
		}
		this.valid = false;
		return false;
	}
	//skip Input method
	boolean skipInput() {
		if(this.topToken.equals("GIMMEH")) {
			nextToken();
			if(skipVar()) {
				return true;
			}else if(this.topToken.equals("\\n")){
				//ERROR IN INPUT STATEMENT SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: identifier; Got: newline.");
				error = true;
			}else {
				//ERROR IN INPUT STATEMENT SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: identifier; Got: "+this.topToken+".");
				error = true;
			}
			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	//skip Print method
	boolean skipPrint() {
		if(this.topToken.equals("VISIBLE")) {
			nextToken();
			if(skipVisOps()) {
				return true;
			}else {
				//ERROR IN PRINT SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Invalid start of expression.");
				error = true;
			}
			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	//skip Elseifblock method
	boolean skipElseifblock() {
		 if(this.topToken.equals("MEBBE")){
			 nextToken();
			 if(this.topToken.equals("\\n")) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: expression; Got: newline.");
				error = true;
				return false;
			 }else if(!skipExpr()) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Invalid start of expression.");
				error = true;
				return false; 
			 }
			 else if(skipExpr() && lineBreak() && skipBlock()) {
				 if(this.topToken.equals("NO WAI")||this.topToken.equals("OIC")) {
					 return true;
				 }
				 if(skipElseifblock()) {
					 return true;
				 }
			 }else {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: keyword(NO WAI); Got: "+this.topToken+".");
				error = true;
				return false;
			 }
			 this.valid = false;
			 return false;
		 }
		 this.valid = false;
		 return false;
	}
	//skip Elseblock method
	boolean skipElseblock() {
		 if(this.topToken.equals("NO WAI")){
			 nextToken();
			 if(lineBreak() && skipBlock()){
				 if(this.topToken.equals("OIC")) {
					 nextToken();
					 return true;
				 }else {
					//ERROR IN IFELSE SYNTAX
					Interpreter.printError("Line "+this.lineNum+": Expected: keyword(OIC); Got: "+this.topToken+".");
					error = true;
					return false; 
				 }
			 }
			 this.valid = false;
			 return false;
		 }
		 this.valid = false;
		 return false;
	}
	//skip Ifblock method
	boolean skipIfblock() {
		 if(this.topToken.equals("YA RLY")){
			 nextToken();
			 if(!this.topToken.equals("\\n")) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: newline; Got: "+this.topToken+".");
				error = false;
			 }else if(lineBreak() && skipBlock()) {
				 return true;
			 }else {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: codeblock; Got: "+this.topToken+".");
				error = false;
			 }
			 this.valid = false;
			 return false;
		 }
		 this.valid = false;
		 return false;
	}
	
	//skip Ifelse method
	boolean skipIfelse() {
		if(this.topToken.equals("O RLY?")){
			nextToken();
			if(lineBreak()) {
				//execute IF
				if(skipIfblock()) {
					//skip ELSE but checks grammar
					if(skipElse()) {
						return true;
					}
				}
			}else if(lineBreak()) {
				//skip IF but checks grammar
				if(skipIf()) {
					//execute ELSE
					if(skipElseblock()) {
						return true;
					}
				}
			}else if(!this.topToken.equals("\\n")) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: newline; Got: "+this.topToken+".");
				error = false;
			}else exprError();
			
			this.deepValid = false;
			this.valid = false;
			return false;
		}
		this.valid = false;
		return false;
	}
	
	//skip codeblock method
	boolean skipCodeblock(){
		if(skipPrint()&& lineBreak()) {
			this.fromPrint = false;
			if(skipCodeblock()) {
				return true;
			}
			return true;
		}else if(skipInput()&&lineBreak()) {
			if(skipCodeblock()) {
				return true;
			}
			return true;
		}else if(skipAssignment()&& lineBreak()) {
			if(skipCodeblock()) {
				return true;
			}
			return true;
		}
		else if(skipFlow()&& lineBreak()) {
			if(skipCodeblock()) {
				return true;
			}
			return true;
		}
		else if(skipExpr()&& lineBreak()) {
			if(skipCodeblock()) {
				return true;
			}
			return true;
		}else if(this.topToken.equals("GTFO")){ //checks break keyword
			this.skipSwitchBreak = true;
			nextToken();
			if(lineBreak()){
				if(this.topToken.equals("OIC") || this.topToken.equals("OMG") || this.topToken.equals("OMGWTF")) return true;
				else {
//					switchError();
					return false;
				}
			}else{
				newlineError();
				return false;
			}
		}else if(this.topToken.equals("OMG")){ //checks another case after
			return true;
		}else if(this.topToken.equals("OMGWTF")){ //checks default case after
			this.skipOmgwtfCase = true;
			return true;
		}else if(this.topToken.equals("OIC")){ //switch statement finished
			this.skipSwitchFinish = true;
			return true;
		}
		return false;
	}
	//skip Omgwtf method
	boolean skipOmgwtf(){
		if(this.topToken.equals("OMGWTF")){
			nextToken();
			if(lineBreak()){
				if(skipCodeblock()){
					if(this.topToken.equals("OIC")){
						nextToken();
						if(this.topToken.equals("\\n")){
							this.switchFinish = true;
							return true;
						}else{
							newlineError();
							return false;
						}
					}else{
						oicError();
						return false;
					}
				}
			}else{
				newlineError();
				return false;
			}
		}
		return false;
	}
	
	//skip Omg method
	boolean skipOmg(){
		if(this.topToken.equals("OMG")){
			nextToken();
			if(skipLiteral()){
				if(lineBreak()){
					if(skipCodeblock()){
						if(this.topToken.equals("OMG")){
							if(skipOmg()){
								return true;
							}
						}else if(this.topToken.equals("OMGWTF")){
							if(skipOmgwtf()){
								return true;
							}
						}else if(this.topToken.equals("OIC")){
							if(this.skipOmgwtfCase){
								nextToken();
								if(this.topToken.equals("\\n")){
									this.skipSwitchFinish = true;
									return true;
								}else{
									newlineError();
									return false;
								}
							}else{
								omgwtfError();
								return false;
							}
						}
					}
				}else{
					newlineError();
					return false;
				}
			}else{
				literalError();
				return false;
			}
		}
		return false;
	}
	//skip Switch method
	boolean skipSwtch(){
		if(this.topToken.equals("WTF?")){
			nextToken();
			if(lineBreak()){
				if(!this.topToken.equals("OMG")){
					omgError();
					return false;
				}else if(skipOmg()){
					return true;
				}else{
					switchError();
					return false;
				}
			}else{
				newlineError();
				return false;
			}
		}
		return false;
	}
	
	//skip Flow method
	boolean skipFlow() {
		if(skipIfelse()) {
			return true;
		}
		else if(skipSwtch()) {
			return true;
		}
		this.valid = false;
		return false;
	}
	//skip Num method
	boolean skipNum() {
		if(skipNumbr()) {
			return true;
		}
		if(skipNumbar()) {
			return true;
		}
		this.valid = false;
		return false;
	}
	
	//skip Arith_op method
	boolean skipArith_op() {
		if(skipNum()) {
			return true;
		}
		else if(skipVar()) {
			return true;
		}
		else if(this.topToken.equals("SUM OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("DIFF OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}

		}
		else if(this.topToken.equals("PRODUKT OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("QUOSHUNT OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("MOD OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		
		else if(this.topToken.equals("BIGGR OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("SMALLR OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		this.valid = false;
		return false;
	}
	//skip Arith method
	boolean skipArith() {
		if(this.topToken.equals("SUM OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		else if(this.topToken.equals("DIFF OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		else if(this.topToken.equals("PRODUKT OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		else if(this.topToken.equals("QUOSHUNT OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		else if(this.topToken.equals("MOD OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		
		else if(this.topToken.equals("BIGGR OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		else if(this.topToken.equals("SMALLR OF")) {
			nextToken();
			if(skipArith_op()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipArith_op()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	//skip Bool_op method
	boolean skipBool_op() {

		if(skipTroof()) {
			return true;
		}
		else if(skipVar()) {
			return true;
		}
		else if(this.topToken.equals("BOTH OF")) {
			nextToken();
			if(skipBool_op() && this.topToken.equals("AN")) {
				nextToken();
				if(skipBool_op()) {
					return true;
				}else {
					exprError();
					return false;
				}				
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("EITHER OF")) {
			nextToken();
			if(skipBool_op() && this.topToken.equals("AN")) {
				nextToken();
				if(skipBool_op()) {
					return true;
				}else {
					exprError();
					return false;
				}	
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("WON OF")) {
			nextToken();
			if(skipBool_op() && this.topToken.equals("AN")) {
				nextToken();
				if(skipBool_op()) {
					return true;
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		else if(this.topToken.equals("NOT")) {
			nextToken();
			if(skipBool_op()) {
				return true;
			}else {
				exprError();
				return false;
			}
		}
		this.valid = false;
		return false;
	}
	//skip Bool method
	boolean skipBool() {
		if(this.topToken.equals("BOTH OF")) {
			nextToken();
			if(skipBool_op() && this.topToken.equals("AN")) {
				nextToken();
				if(skipBool_op()) {
					return true;
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		else if(this.topToken.equals("EITHER OF")) {
			nextToken();
			if(skipBool_op() && this.topToken.equals("AN")) {
				nextToken();
				if(skipBool_op()) {
					return true;
				}else {
					exprError();
					return false;
				}	
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		else if(this.topToken.equals("WON OF")) {
			nextToken();
			if(skipBool_op() && this.topToken.equals("AN")) {
				nextToken();
				if(skipBool_op()) {
					return true;
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		else if(this.topToken.equals("NOT")) {
			nextToken();
			if(skipBool_op()) {
				return true;
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	
	//skip AnBlock method
	boolean skipAnBlock() {
		if(skipBool_op()) {
			if(this.topToken.equals("AN")) {
				nextToken();
				skipAnBlock();
			}else {
				exprError();
				return false;
			}
			return true;
		}
		this.valid = false;
		return false;
	}
	
	//skip BoolIn method
	boolean skipBoolIn() {
		if(this.topToken.equals("ALL OF")) {
			nextToken();
			if(skipAnBlock()) {
				if(this.topToken.equals("MKAY")) {
					nextToken();
					return true;
				}else{
					//ERROR IN BOOLEAN OPERATION SYNTAX
					Interpreter.printError("Line "+this.lineNum+": Expected: keyword(MKAY); Got: "+this.topToken+".");
					error = true;
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}		

		else if(this.topToken.equals("ANY OF")) {
			nextToken();
			if(skipAnBlock()) {
				if(this.topToken.equals("MKAY")) {
					nextToken();
					return true;
				}
				return true;
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	//skip Operand method
	boolean skipOperand() {
		if(skipVar()) {
			return true;
		}
		else if(skipLiteral()) {
			return true;
		}
		else if(skipArith()) {
			return true;
		}
		else if(skipBool()) {
			return true;
		}
		else if(skipBoolIn()) {
			return true;
		}
		else if(skipComp_op()) {
			return true;
		}
		else if(skipConcat()) {
			return true;
		}
		this.valid = false;
		return false;
	}
	//skip Comp method
	boolean skipComp() {
		if(this.topToken.equals("AN")) {
			nextToken();
			if(skipOperand()) {
				return true;
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	
	//skip Rel method
	boolean skipRel() {
		if(this.topToken.equals("BIGGR OF")) {
			nextToken();
			if(skipOperand()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipOperand()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		else if(this.topToken.equals("SMALLR OF")) {
			nextToken();
			if(skipOperand()) {
				if(this.topToken.equals("AN")) {
					nextToken();
					if(skipOperand()) {
						return true;
					}else {
						exprError();
						return false;
					}
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	
	//skip Comp_op method
	boolean skipComp_op() {
		if(this.topToken.equals("BOTH SAEM")) { 
			nextToken();
			if(skipOperand()) {
				if(skipComp()) {
					return true;
				}else if(skipRel()) {
					return true;
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		else if(this.topToken.equals("DIFFRINT")) {
			nextToken();
			if(skipOperand()) {
				if(skipComp()) {
					return true;
				}else if(skipRel()) {
					return true;
				}else {
					exprError();
					return false;
				}
			}else {
				exprError();
				return false;
			}
		}
		this.valid = false;
		return false;
	}
	
	//skip SkipStr method
	boolean skipStr() {
		if(skipLiteral()) {
			if(this.topToken.equals("AN")) {
				nextToken();
				if(skipStr()) {
					return true;
				}
				return true;
			}
			return true;
		}
		this.valid = false;
		return false;
	}
	
	//skip Concat method
	boolean skipConcat() {
		if(this.topToken.equals("SMOOSH")) {
			nextToken();
			if(skipStr()) {
				return true;
			}else {
				exprError();
				return false;
			}
//			this.deepValid = false;
		}
		this.valid = false;
		return false;
	}
	
	//skip Expr method
	boolean skipExpr() {
		if(skipArith()) {
			return true;
		}
		if(skipBool()) {
			return true;
		}
		if(skipBoolIn()) {
			return true;
		}
		if(skipComp_op()) {
			return true;
		}
		if(skipConcat()) {
			return true;
		}
		this.valid = false;
		return false;
	}
	//skip Varident method
	boolean skipVarident() {
		if(checkRegex(this.topToken)) {
			this.valid = false;
			return false;
		}else if(this.topToken.equals("\\n")){
			this.valid = false;
			return false;
		}
		Pattern pattern = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");
	    Matcher matcher = pattern.matcher(this.topToken);
		if(matcher.find()) {
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	
	//skip Numbr method
	boolean skipNumbr() {
		Pattern pattern = Pattern.compile("^-?[0-9]+$");
	    Matcher matcher = pattern.matcher(this.topToken);
		if(matcher.find()) {
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	//skip Numbar method
	boolean skipNumbar() {
		Pattern pattern = Pattern.compile("^-?[0-9]+.[0-9]+$");
	    Matcher matcher = pattern.matcher(this.topToken);
		if(matcher.find()) {
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	
	//skip Yarn method
	boolean skipYarn() {
		Pattern pattern = Pattern.compile("\"[^\"]*\"");
	    Matcher matcher = pattern.matcher(this.topToken);
		if(matcher.find()) {
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	//skip Troof method
	boolean skipTroof() {
		Pattern pattern = Pattern.compile("^WIN$|^FAIL$");
	    Matcher matcher = pattern.matcher(this.topToken);
		if(matcher.find()) {
			nextToken();
			return true;
		}else {
			this.valid = false;
			return false;
		}
	}
	
	//skip Literal method
	boolean skipLiteral() {
		if(skipNumbr()) {
			return true;
		}else if(skipNumbar()) {
			return true;
		}else if(skipYarn()) {
			return true;
		}else if(skipTroof()) {
			return true;
		}
		this.valid = false;
		return false;
	}
	
	//skip Var method
	boolean skipVar() {
		if(this.topToken.equals("IT")) {
			nextToken();
			return true;
		}
		if(varident()) {
			return true;
		}
		this.valid = false;
		return false;
	}
	
	//skip Assignment method
	boolean skipAssignment() {
		 if(skipVar()) {
			 if(this.topToken.equals(" R")){
				 nextToken();
				 if(skipLiteral()) {
						return true;
				}else if(skipVarident()) {
					return true;
				}else if(skipExpr()) {
					return true;
				}
				 this.deepValid = false;
			 }else {
				 
			 }
		 }
		 this.valid = false;
		 return false;
	}
	
	//skip Block method
	boolean skipBlock() {
		if(skipPrint() && lineBreak()) {
			if(skipBlock()) {
				return true;
			}
			return true;
		}
		if(skipInput()&&lineBreak()) {
			if(skipBlock()) {
				return true;
			}
			return true;
		}
		if(skipFlow() && lineBreak()) {
			if(skipBlock()) {
				return true;
			}
			return true;
		}
		if(skipExpr()&& lineBreak()) {
			if(skipBlock()) {
				return true;
			}
			return true;
		}
		if(skipAssignment() && lineBreak()) {
			if(skipBlock()) {
				return true;
			}
			return true;
		}

		if(this.topToken.equals("NO WAI") ||  this.topToken.equals("OIC") || this.topToken.equals("MEBBE")) {
			return true;
		}
		
	 this.valid = false;
	 return false;

	}
	
	//skip Else method
	boolean skipElse() {
		if(this.topToken.equals("NO WAI")) {
			nextToken();
			if(!this.topToken.equals("\\n")) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: newline; Got: "+this.topToken+".");
				error = false;
			}else if(lineBreak() && skipBlock()) {
				if(this.topToken.equals("OIC")) {
					nextToken();
					return true;
				}else {
					//ERROR IN IFELSE SYNTAX
					Interpreter.printError("Line "+this.lineNum+": Expected: keyword(OIC); Got: "+this.topToken+".");
					error = true;
					return false;
				}
			}
		}
		
		this.deepValid = false;
		this.valid = false;
		return false;
	}
	//skip If method
	boolean skipIf() {
		if(this.topToken.equals("YA RLY")) {
			nextToken();
			if(!this.topToken.equals("\\n")) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: newline; Got: "+this.topToken+".");
				error = false;
			}else if(lineBreak() && skipBlock()) {
				if(this.topToken.equals("NO WAI") || this.topToken.equals("MEBBE")) {
					return true;
				}else {
					//ERROR IN IFELSE SYNTAX
					Interpreter.printError("Line "+this.lineNum+": Expected: keyword(NO WAI); Got: "+this.topToken+".");
					error = true;
					return false;
				}
			}
		}
		
		this.deepValid = false;
		this.valid = false;
		return false;
	}
	
	//RULE: <ifelse> ::= O RLY? <linebreak> <if> <elseif> <else> | O RLY? <linebreak> <if> <else>
	//check if the succeeding tokens follow this rule
	boolean ifelse() {
		//check if toptoken is O RLY?
		if(this.topToken.equals("O RLY?")){
			this.elseTrue = false;
			//check IT if WIN
			boolean ifTrue = false;
			if(this.semAnalyzer.getVarValue("IT").equals("WIN")) {
				ifTrue = true; //execute YA RLY
				this.ifelseFinish = true;
			}
			this.elseTrue = true;
			nextToken();
			if(ifTrue && lineBreak()) {
				//execute IF
				if(ifblock()) {
					//skip ELSE IF block/s but checks grammar
					if(skipElseifblock()) {
						return true;
					}
					//skip ELSE but checks grammar
					if(skipElse()) {
						return true;
					}
				}
			}
			
			else if(!ifTrue && lineBreak()) {
				//skip IF
				if(skipIf()) {
					//execute ELSE IF
					if(elseifblock()) {
						//skip ELSE but checks grammar
						if(this.elseTrue && !this.ifelseFinish) {
							if(elseblock()) {
								return true;
							}
						}
						else if(skipElse()) {
							return true;
						}
					}
					else if(this.elseTrue && elseblock()) {
						return true;
					}
				}
				
			}else if(!this.topToken.equals("\\n")) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: newline; Got: "+this.topToken+".");
				error = false;
			}else exprError();
			
			this.deepValid = false;
			this.valid = false;
			return false;
		}
		this.valid = false;
		return false;
	}
	
	//RULE: <if> ::= YA RLY <linebreak> <codeblock> <linebreak>
	//check if the succeeding tokens follow this rule
	 boolean ifblock() {
		 if(this.topToken.equals("YA RLY")){
			 this.fromIf = true;
			 nextToken();
			 if(!this.topToken.equals("\\n")) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: newline; Got: "+this.topToken+".");
				error = false;
			 }else if(lineBreak() && ifcodeblock()) {
				 return true;
			 }else {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: codeblock; Got: "+this.topToken+".");
				error = false;
			 }
			 this.valid = false;
			 return false;
		 }
		 this.valid = false;
		 return false;
	 }
	 // RULE: <elseif> ::= MEBBE <linebreak> <codeblock> <linebreak> <elseif> | MEBBE <linebreak> <codeblock> <linebreak>
	//check if the succeeding tokens follow this rule
	 boolean elseifblock() {
		 if(this.topToken.equals("MEBBE")){
			 this.fromElseif = true;
			 nextToken();
			 boolean elseifTrue = false;
			 if(expr()) {
				if(error) return true;
				this.fromExpr = false;
				ArrayList<String> ans = this.semAnalyzer.evaluate(this.currentExpr);
				//assign answer to IT
				this.semAnalyzer.setVarValue("IT", ans.get(0));
				this.semAnalyzer.setVarType("IT", ans.get(1), this.topWord.getLineNum());
				Word symbol = new Word("IT",ans.get(0),0,0);
				this.symbolTable.add(symbol);
				this.currentExpr.clear();
				 
				//check value of IT if WIN
				 if(this.semAnalyzer.getVarValue("IT").equals("WIN")) {
					 elseifTrue = true; //execute ELSE IF block
					 this.ifelseFinish = true;
				 }
				 else this.elseTrue = true; 
				 if(elseifTrue && lineBreak()) {
					 if(ifcodeblock()) {
						 if(this.topToken.equals("NO WAI")||this.topToken.equals("OIC")) {
							 return true;
						 }
						 if(skipElseifblock()) { //skip other ELSE IF blocks
							 return true;
						 }
					 }
				 }else if(!elseifTrue && lineBreak()) {
					 //skipCurrent ELSE IF block, check next ELSE IF block or leave ELSE IF if next is ELSE block
					 if(skipBlock()) {
						 if(this.topToken.equals("NO WAI")||this.topToken.equals("OIC")) {
							 return true;
						 }
						 if(elseifblock()) { //check next ELSE IF block
							 return true;
						 }
					 }
				 }else {
					//ERROR IN IFELSE SYNTAX
					Interpreter.printError("Line "+this.lineNum+": Expected: newline; Got: "+this.topToken+".");
					error = true;
					return false;
				 }
			 }else exprError();
			 this.valid = false;
			 return false;
		 }
		 this.elseTrue = true; //no MEBBE blocks
		 this.valid = false;
		 return false;
	 }
	 
	 //RULE: <elseblock> := NO WAI <linebreak> <codeblock> <linebreak> OIC
	//check if the succeeding tokens follow this rule
	 boolean elseblock() {
		 if(this.topToken.equals("NO WAI")){
			 this.fromElse = true;
			 nextToken();
			 if(lineBreak() && ifcodeblock()){
				 if(this.topToken.equals("OIC")) {
					 nextToken();
					 return true;
				 }
			 }
			 this.valid = false;
			 return false;
		 }
		 this.valid = false;
		 return false;
	 }
	 
	 //RULE: <switch> ::= WTF? <linebreak> OMG <literal> <linebreak> <codeblock> <omg> 
	//check if the succeeding tokens follow this rule
	 boolean swtch(){
		if(this.topToken.equals("WTF?")){
			nextToken();
			if(lineBreak()){
				if(!this.topToken.equals("OMG")){
					omgError();
					return false;
				}else if(omg()){
					return true;
				}
			}else{
				newlineError();
				return false;
			}
		}
		return false;
	}
	 
	//RULE: <omg> ::=  OMG <literal> <linebreak> <codeblock> <omg> | <omgwtf>
	//check if the succeeding tokens follow this rule
	 boolean omg(){
		if(this.topToken.equals("OMG")){ //checks "OMG"
			nextToken();
			String lit = this.topToken;
			if(literal()){ //checks literal
				if(lineBreak()){ //checks "\\n"
					boolean ifTrue = false;
					
					if(this.switchTrue && !this.switchBreak){ //proceed executing codeblock
						ifTrue = true;
					}else if(this.switchTrue && this.switchBreak){ //does not execute codeblock, but checks grammar
						ifTrue = false;
					}
					//check IT value = literal
					else if(this.semAnalyzer.getVarValue("IT").equals(lit)) {
						ifTrue = true;
						this.switchTrue = true; //execute all code blocks until GTFO or OIC
					}

					if(ifTrue){ //execute all code blocks until GTFO or OIC
						if(codeblock()){ //checks valid codeblock
							if(this.topToken.equals("OMG")) { 
								if(omg()){
									return true;
								}
							}else if(this.topToken.equals("OMGWTF")){
								if(omgwtf()){
									return true;
								}
							}else if(this.topToken.equals("OIC")){
								if(this.omgwtfCase){ //check if default case was reached
									nextToken();
									if(this.topToken.equals("\\n")){
										this.switchFinish = true;
										return true;
									}else{
										newlineError();
										return false;
									}
								}else{
									omgwtfError();
									return false;
								}
							}else{
								switchError();
								return false;
							}

						}else{
							switchError();
							return false;
						}

					//does not execute codeblock but checks grammar
					}else{ //check next OMG block
						if(skipCodeblock()){
							if(this.topToken.equals("OMG")) { 
								if(omg()){
									return true;
								}
							}else if(this.topToken.equals("OMGWTF")){
								if(omgwtf()){
									return true;
								}
							}else if(this.topToken.equals("OIC")){
								if(this.omgwtfCase){ //check if default case was reached
									nextToken();
									if(this.topToken.equals("\\n")){
										this.switchFinish = true;
										return true;
									}else{
										newlineError();
										return false;
									}
								}else{
									omgwtfError();
									return false;
								}
							}else{
								switchError();
								return false;
							}
						}else{
							switchError();
							return false;
						}
					}				
				}else{
					newlineError();
					return false;
				}
			}else{
				literalError();
				return false;
			}
		}
		return false;
	}
	 
	// RULE: <omgwtf ::= OMGWTF <linebreak> <codeblock> OIC
	//check if the succeeding tokens follow this rule
	 boolean omgwtf(){
		if(this.topToken.equals("OMGWTF")){
			nextToken();
			if(lineBreak()){
				//check if switchBreak was never assigned True, meaning False on all previous OMG cases, and GTFO was never reached
				if(!this.switchBreak){
					if(codeblock()){
						if(this.topToken.equals("OIC")){
							nextToken();
							if(this.topToken.equals("\\n")){
								this.switchFinish = true;
								return true;
							}else{
								newlineError();
								return false;
							}
						}else{
							oicError();
							return false;
						}
					}else{
						switchError();
						return false;
					}
				}else{
					if(skipCodeblock()){
						if(this.topToken.equals("OIC")){
							nextToken();
							if(this.topToken.equals("\\n")){
								this.switchFinish = true;
								return true;
							}else{
								newlineError();
								return false;
							}
						}else{
							oicError();
							return false;
						}
					}else{
						switchError();
						return false;
					}
				}
			}else{
				newlineError();
				return false;
			}
		}
		return false;
	}
	 
	 //RULE: <assignment> ::= <var> R <literal> <linebreak> | <var> R <var> <linebreak> | <var> R <expr> <linebreak>
	 //check if the succeeding tokens follow this rule 
	 boolean assignment() {
		 //check if the toptoken is a variable
		 if(var()) {
			 if(this.topToken.equals(" R")){
				 String name = this.currentVar;
				 nextToken();
				 if(literal()) {
					if(this.semAnalyzer.checkVar(this.currentVar)) { //existing variable, update value and type
						this.semAnalyzer.setVarValue(this.currentVar, this.currentVal); //sets value of variable
						this.semAnalyzer.setVarType(this.currentVar, this.currentType, this.topWord.getLineNum()); //sets type of variable
						Word symbol = new Word(this.currentVar, this.currentVal,0,0);
						this.symbolTable.add(symbol);
					//if variable  not declared, display error
					}else {
						Interpreter.printError("Line "+this.lineNum+": Cannot assign to undeclared variable: "+name+".");
						error = true;
					}
					this.validAssign = true;
					return true;
				}else if(varident()) {
					if(this.semAnalyzer.checkVar(name)) { //existing variable, update value and type
						this.semAnalyzer.setVarValue(name, this.currentVar); //sets value of variable
						this.semAnalyzer.setVarType(name, this.currentType, this.topWord.getLineNum()); //sets type of variables		
						Word symbol = new Word(name,this.semAnalyzer.getVarValue(this.currentVar) ,0,0);
						this.symbolTable.add(symbol);
					//if variable  not declared, display error
					}else {
						Interpreter.printError("Line "+this.lineNum+": Cannot assign to undeclared variable: "+name+".");
						error = true;
					}
					this.validAssign = true;
					return true;
				}else if(expr()) {
					if(error) return true;
					this.fromExpr = false;
					ArrayList<String> ans = this.semAnalyzer.evaluate(this.currentExpr);
					
					if(this.semAnalyzer.checkVar(name)) { //existing variable, update value and type
						this.semAnalyzer.setVarValue(name, ans.get(0)); //sets value of variable
						this.semAnalyzer.setVarType(name, ans.get(1), this.topWord.getLineNum()); //sets type of variable
						Word symbol = new Word(name,ans.get(0),0,0);
						this.symbolTable.add(symbol);
					//if variable  not declared, display error
					}else {
						Interpreter.printError("Line "+this.lineNum+": Cannot assign to undeclared variable: "+name+".");
						error = true;
					}
					this.validAssign = true;
					return true;
				}
				 this.deepValid = false;
			 }
			 this.validAssign = false;
		 }

		 this.valid = false;
		 return false;
	 }
	 
	//<ifcodeblock> ::= <print> <linebreak> <ifcodeblock>  | <flow> <linebreak> <ifcodeblock> | <assignment> <linebreak> <ifcodeblock> | <expr>  <linebreak> <ifcodeblock> | epsilon
	//check if the succeeding tokens follow this rule
	 boolean ifcodeblock() {
			if(print() && lineBreak()) {
				this.fromPrint = false;
				if(ifcodeblock()) {
					return true;
				}
				return true;
			}
			if(input() && lineBreak()) {
				if(ifcodeblock()) {
					return true;
				}
			}
			if(flow() && lineBreak()) {
				if(ifcodeblock()) {
					return true;
				}
				return true;
			}
			if(expr()&& lineBreak()) {
				if(error) return true;
				this.fromExpr = false;
				ArrayList<String> ans = this.semAnalyzer.evaluate(this.currentExpr);
				//assign answer to IT
				this.semAnalyzer.setVarValue("IT", ans.get(0));
				this.semAnalyzer.setVarType("IT", ans.get(1), this.topWord.getLineNum());
				Word symbol = new Word("IT",ans.get(0),0,0);
				this.symbolTable.add(symbol);
				this.currentExpr.clear();
				if(ifcodeblock()) {
					return true;
				}
				return true;
			}
			if(assignment() && lineBreak()) {
				if(ifcodeblock()) {
					return true;
				}
				return true;
			}
			
			if(this.fromIf && this.topToken.equals("MEBBE") || this.topToken.equals("NO WAI")){
				this.fromIf = false;
				return true;
			}else if(this.fromIf) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: keyword(NO WAI); Got: "+this.topToken+".");
				error = true;
				return false;
			}else if(this.fromElseif && this.topToken.equals("MEBBE") || this.topToken.equals("NO WAI")) {
				this.fromElseif = false;
				return true;
			}else if(this.fromElseif) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: keyword(NO WAI); Got: "+this.topToken+".");
				error = true;
				return false;
			}else if(this.fromElse && this.topToken.equals("OIC")) {
				this.fromElse = false;
				return true;
			}else if(this.fromElse) {
				//ERROR IN IFELSE SYNTAX
				Interpreter.printError("Line "+this.lineNum+": Expected: keyword(OIC); Got: "+this.topToken+".");
				error = true;
				return false;
			}
						
		 this.valid = false;
		 return false;
	 }
     //<codeblock> ::= <print> <linebreak> <codeblock>  | <input> <linebreak> <codeblock> | <flow> <linebreak> <codeblock> | <assignment> <linebreak> <codeblock> | <expr>  <linebreak> <codeblock> | epsilon
	//check if the succeeding tokens follow this rule
	 boolean codeblock(){
			if(print()&& lineBreak()) {
				this.fromPrint = false;
				if(codeblock()) {
					return true;
				}
				return true;
			}else if(input()&&lineBreak()) {
				if(codeblock()) {
					return true;
				}
				return true;
			}else if(assignment()&& lineBreak()) {
				if(codeblock()) {
					return true;
				}
				return true;
			}
			else if(flow()&& lineBreak()) {
				if(codeblock()) {
					return true;
				}
				return true;
			}
			else if(expr()&& lineBreak()) {
				if(error) return true;
				this.fromExpr = false;
				ArrayList<String> ans = this.semAnalyzer.evaluate(this.currentExpr);
				//assign answer to IT
				this.semAnalyzer.setVarValue("IT", ans.get(0));
				this.semAnalyzer.setVarType("IT", ans.get(1), this.topWord.getLineNum());
				Word symbol = new Word("IT",ans.get(0),0,0);
				this.symbolTable.add(symbol);
				this.currentExpr.clear();
				if(codeblock()) {
					return true;
				}
				return true;
			}else if(this.topToken.equals("GTFO")){ //checks break keyword
				this.switchBreak = true;
				nextToken();
				if(lineBreak()){
					if(this.topToken.equals("OIC") || this.topToken.equals("OMG") || this.topToken.equals("OMGWTF")) return true;
					else {
						switchError();
						return false;
					}
				}else{
					newlineError();
					return false;
				}
			}else if(this.topToken.equals("OMG")){ //checks another case after
				return true;
			}else if(this.topToken.equals("OMGWTF")){ //checks default case after
				this.omgwtfCase = true;
				return true;
			}else if(this.topToken.equals("OIC")){ //switch statement finished
				this.switchFinish = true;
				return true;
			}
			return false;
		}
	
	//check if a variable identifier matches a keyword
	boolean checkRegex(String word) {
		String checkword = " "+ word + " ";
		 for(Regexp regex: this.regex) {
			 if(regex.getPattern().equals(checkword)) {
				 return true;
			 }
		 }
		 return false;
	 }
	//display input dialog
	void inputDialog(String var) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("LOLCODE Input");
		dialog.setHeaderText("Input");
		dialog.setContentText("Enter your input:");
		dialog.initModality(Modality.APPLICATION_MODAL); 
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
		    if(this.semAnalyzer.checkVar(var)) { //existing variable, update value and type
		    	this.semAnalyzer.setVarValue(var, result.get());
				this.semAnalyzer.setVarType(var, "Yarn Literal", this.topWord.getLineNum()); //sets type of variable
				Word symbol = new Word(var, result.get(),0,0);
				this.symbolTable.add(symbol);
			}else {
				Interpreter.printError("Line "+this.lineNum+": Variable not declared.");
				error = true;
			}
		}
	}
	
	//expression error message
	void exprError() {
		Interpreter.printError("Line "+this.lineNum+": Invalid start of expression.");
		error = true;
	}
	//literal error message
	void literalError(){
		Interpreter.printError("Line "+this.lineNum+": Expected: literal; Got: "+this.topToken+".");
		error = true;
	}
	//newline error message
	void newlineError(){
		Interpreter.printError("Line "+this.lineNum+": Expected: newline; Got: "+this.topToken+".");
		error = true;
	}
	//omg error message
	void omgError(){
		Interpreter.printError("Line "+this.lineNum+": Expected: keyword(OMG); Got: "+this.topToken+".");
		error = true;
	}
	//omgwtf error message
	void omgwtfError(){
		Interpreter.printError("Line "+this.lineNum+": Expected: keyword(OMGWTF); Got: "+this.topToken+".");
		error = true;
	}
	//switch error message
	void switchError(){
		Interpreter.printError("Line "+this.lineNum+": Invalid switch-case statement.");
		error = true;	
	}
	//oic error message
	void oicError(){
		Interpreter.printError("Line "+this.lineNum+": Expected: keyword(OIC); Got: "+this.topToken+".");
		error = true;
	}
}