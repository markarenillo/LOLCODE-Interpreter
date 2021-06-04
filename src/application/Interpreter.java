package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;

//variables
public class Interpreter{
	private Stage stage;
	private Group root;
	private Scene scene;
	private File selectedFile;
	private Label lexemesLabel;
	private Label symbolTableLabel;
	private TableView table1;
	private TableView table2;
	private static Lexeme lexemes;
	private static SymbolTable symbolTable;
	private TableColumn<Word, String> lexemeCol;
	private TableColumn<Word, String> classCol;
	private TableColumn<Word, String> identifierCol;
	private TableColumn<Word, String> valueCol;
	private static TextArea codeField;
	private static TextArea resultField;
	private ArrayList<Regexp> regex = new ArrayList<Regexp>();
	private boolean lexerErr = false; //set to  true if lexer found errors
	private ArrayList<TokenStream> tokenStreamList = new ArrayList<TokenStream>();
	private ArrayList<Word> lexOutput = new ArrayList<Word>();
	private Parser parser;
	private int lineNum; //current line number in lexer
	private String line; //current line in lexer
	private static StringBuilder fieldContent = new StringBuilder("");
	private int clicks = 0;
	private int executeClicks;

    //initialize variables
    public Interpreter(){
    	this.root = new Group();
		this.scene = new Scene(this.root, 1000,700,Color.web("#F5F5F5"));
		this.lexemesLabel = new Label();
		this.symbolTableLabel = new Label();
		this.table1 = new TableView<>();
		this.table2 = new TableView<>();
		lexemes = new Lexeme();
		symbolTable = new SymbolTable();
		this.lexemeCol  = new TableColumn<Word, String>("Lexeme");
		this.classCol  = new TableColumn<Word, String>("Classification");
		this.identifierCol  = new TableColumn<Word, String>("Identifer");
		this.valueCol  = new TableColumn<Word, String>("Value");
		Interpreter.codeField = new TextArea();
		Interpreter.resultField = new TextArea();
   }
    //setStage 
    public void setStage(Stage stage) {
    	this.stage = stage;
    	//initialize all components in the stage
    	this.initInterpreter();
		this.stage.setTitle("LOLCODE Interpreter");
		this.stage.setScene(this.scene);
		//show buttons
		this.selectFileButton(); 
		this.executeButton(); 
		//show stage
		this.stage.setResizable(false);
		this.stage.show();
    }
    public void initInterpreter() {
    	//text area of LOLCODE (upper left portion)
		codeField.setMinHeight(330);
    	codeField.setMaxWidth(338);
    	codeField.setLayoutX(10);
    	codeField.setLayoutY(51);	
    	//label for Lexemes table
    	this.lexemesLabel.setText("LEXEMES");
    	this.lexemesLabel.setTranslateX(490);
    	this.lexemesLabel.setTranslateY(25);
		this.root.getChildren().add(this.lexemesLabel);
		
		//display Lexemes table
    	this.table1.setMaxHeight(330);
    	this.table1.setMinWidth(310);
        this.table1.setLayoutX(358);
        this.table1.setLayoutY(51);
        
        //label for Symbol table
        this.symbolTableLabel.setText("SYMBOL TABLE");
    	this.symbolTableLabel.setTranslateX(790);
    	this.symbolTableLabel.setTranslateY(25);
		this.root.getChildren().add(this.symbolTableLabel);
        
		//display Symbol table
        this.table2.setMaxHeight(330);
    	this.table2.setMinWidth(310);
        this.table2.setLayoutX(678);
        this.table2.setLayoutY(51);
        
        //result of LOLCODE (lower portion)
        resultField.setMinHeight(270);
    	resultField.setMinWidth(979);
    	resultField.setLayoutX(10);
    	resultField.setLayoutY(420);
    	resultField.setEditable(false);

    	//add components to root
        this.root.getChildren().addAll(this.table1, this.table2, codeField, resultField);
    }
    
    //select LOLCODE file button
    private void selectFileButton() {
		Button select = new Button("Select LOLCODE File");
		select.setLayoutX(10);
		select.setLayoutY(18);
		select.setMinWidth(338);
		this.root.getChildren().add(select);
		//mouseEvent of button
		select.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				 FileChooser fileChooser = new FileChooser();
				 fileChooser.setTitle("Open Resource File");
				 fileChooser.getExtensionFilters().addAll(
						 //LOLCODE files only
						  new ExtensionFilter("LOLCODE Files", "*.lol"));
				 //chosen file
				 selectedFile = fileChooser.showOpenDialog(stage);
				 if (selectedFile == null) {
					    System.out.println("No file selected!");
				}else {
						//call Lexical Analyzer, and others
						try {
							clicks++;
							if(clicks>1) {
								clearUI();
							}
							executeClicks = 0;
							readCode(selectedFile);	
							
						}catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
			}
		});
	}
    //execute button
    private void executeButton() {
		Button execute = new Button("EXECUTE");
		execute.setLayoutX(10);
		execute.setLayoutY(388);
		execute.setMinWidth(979);
		this.root.getChildren().add(execute);
		execute.setOnMouseClicked(new EventHandler<MouseEvent>(){
			public void handle(MouseEvent e){
				if(clicks>0)executeClicks++;
				//execute LOLCODE
				if(executeClicks==1) {
					try {
						setRegex();
						lexer(selectedFile);
						//error detection in lexical analysis
						if(lexerErr != true) {
							setTable(0);
							setTable(1);
						}
						if(lexerErr != true) {
							createListOfTokenStreams(Interpreter.lexemes.getTokens()); //creates list of sorted tokenStream
							for(Word w: lexOutput) {
								if(w.getValue1().equals("\\n") == false) {
									w.setValue(w.getValue1().substring(1,w.getValue1().length()-1));
								}
								if(w.getValue1().equals("R")) {
									w.setValue(" R");
								}
							}
							parser = new Parser(lexOutput, regex);
							symbolTable = parser.parseLine(symbolTable);
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		
	}
    //initialize table
    private void setTable(int indicator) {
    	//0 for lexeme table
    	if(indicator == 0) {
	    	//set columns
    		this.root.getChildren().remove(this.table1);
	         this.lexemeCol.setCellValueFactory(new PropertyValueFactory<>("value1"));
	         this.lexemeCol.setMinWidth(105);
	         this.classCol.setCellValueFactory(new PropertyValueFactory<>("value2"));
	         this.classCol.setMinWidth(80);
	         //set data in the table
	         this.table1.setItems(this.lexemes.getList());
	         //add columns in the table
	         this.table1.getColumns().addAll(lexemeCol, classCol);
	         //display table
	         this.table1.setMaxHeight(330);
	     	 this.table1.setMinWidth(310);
	         this.table1.setLayoutX(358);
	         this.table1.setLayoutY(51);
	         this.root.getChildren().add(this.table1);
	     //1 for symbol table
    	}else if(indicator == 1){
    		//set columns
    		this.root.getChildren().remove(this.table2);
            this.identifierCol.setCellValueFactory(new PropertyValueFactory<>("value1"));
            this.identifierCol.setMinWidth(105);
            this.valueCol.setCellValueFactory(new PropertyValueFactory<>("value2"));
            this.valueCol.setMinWidth(80);
            //set data in the table
            this.table2.setItems(this.symbolTable.getList());
            //add columns in the table
            this.table2.getColumns().addAll(identifierCol, valueCol);
             //display table
            this.table2.setMaxHeight(330);
        	this.table2.setMinWidth(310);
            this.table2.setLayoutX(678);
            this.table2.setLayoutY(51);

            this.root.getChildren().add(this.table2);
    	}
    }
    
    //set all Regex of LOLCODE
    private void setRegex() {
    	Regexp re1 = new Regexp(" HAI ", "Code Delimiter", true);
    	Regexp re2 = new Regexp(" KTHXBYE ", "Code Delimiter", true);
    	Regexp re4 = new Regexp(" OBTW ", "Comment Delimiter", true);
    	Regexp re3 = new Regexp(" BTW ", "Comment Delimiter", true);			//
    	Regexp re5 = new Regexp(" TLDR ", "Comment Delimiter", true);
    	Regexp re6 = new Regexp(" I HAS A ", "Variable Declaration", true);
    	Regexp re7 = new Regexp(" ITZ ", "Variable Assignment", true);
    	Regexp re9 = new Regexp(" SUM OF ", "Arithmetic Operator", true);
    	Regexp re10 = new Regexp(" DIFF OF ", "Arithmetic Operator", true);
    	Regexp re11 = new Regexp(" PRODUKT OF ", "Arithmetic Operator", true);
    	Regexp re12 = new Regexp(" QUOSHUNT OF ", "Arithmetic Operator", true);
    	Regexp re13 = new Regexp(" MOD OF ", "Arithmetic Operator", true);
    	Regexp re14 = new Regexp(" BIGGR OF ", "Arithmetic Operator", true);
    	Regexp re15 = new Regexp(" SMALLR OF ", "Arithmetic Operator", true);
    	Regexp re16 = new Regexp(" BOTH OF ", "Boolean Operator", true);
    	Regexp re17 = new Regexp(" EITHER OF ", "Boolean Operator", true);
    	Regexp re18 = new Regexp(" WON OF ", "Boolean Operator", true);
    	Regexp re19 = new Regexp(" NOT ", "Boolean Operator", true);
    	Regexp re20 = new Regexp(" ANY OF ", "Boolean Operator", true);
    	Regexp re21 = new Regexp(" ALL OF ", "Boolean Operator", true);
    	Regexp re22 = new Regexp(" MKAY ", "Boolean Delimiter", true);
    	Regexp re23 = new Regexp(" BOTH SAEM ", "Comparison Operator", true);
    	Regexp re24 = new Regexp(" DIFFRINT ", "Comparison Operator", true);
    	Regexp re25 = new Regexp(" SMOOSH ", "Concatenation Operator", true);
    	Regexp re26 = new Regexp(" MAEK ", "Typecast Operator", true);
    	Regexp re28 = new Regexp(" IS NOW A ", "Typecast Operator", true);
    	Regexp re29 = new Regexp(" GIMMEH ", "Input Keyword", true);
    	Regexp re30 = new Regexp(" VISIBLE ", "Output Keyword", true);
    	Regexp re31 = new Regexp(" O RLY\\? ", "Condition Delimiter", true);
    	Regexp re32 = new Regexp(" YA RLY ", "If Keyword", true);
    	Regexp re33 = new Regexp(" MEBBE ", "Elseif Keyword", true);
    	Regexp re34 = new Regexp(" NO WAI ", "Else Keyword", true);
    	Regexp re35 = new Regexp(" OIC ", "Condition Delimiter", true);
    	Regexp re36 = new Regexp(" WTF\\? ", "Condition Delimiter", true);
    	Regexp re54 = new Regexp(" GTFO ", "Break Keyword", true);
    	Regexp re38 = new Regexp(" OMGWTF ", "Default Keyword", true);
    	Regexp re37 = new Regexp(" OMG ", "Case Keyword", true);					//
    	Regexp re39 = new Regexp(" IM IN YR ", "Loop Delimiter", true);
    	Regexp re40 = new Regexp(" UPPIN ", "Increment Keyword", true);
    	Regexp re41 = new Regexp(" NERFIN ", "Decrement Keyword", true);
    	Regexp re43 = new Regexp(" TIL ", "Identifier", true);
    	Regexp re44 = new Regexp(" WILE ", "Identifier", true);
    	Regexp re45 = new Regexp(" IM OUTTA YR ", "Loop Delimiter", true);
    	Regexp re46 = new Regexp(" WIN | FAIL ", "Troof Literal", true);
    	Regexp re47 = new Regexp(" NUMBR | NUMBAR | YARN | TROOF ", "Data Type", true);
    	Regexp re42 = new Regexp(" YR ", "Loop Delimiter", true);					//
    	Regexp re8 = new Regexp(" R ", "Variable Assignment", true);          	//
    	Regexp re53 = new Regexp(" AN ", "Separator", true);			//
    	Regexp re27 = new Regexp(" A ", "Typecast Operator", true);				//
    	Regexp re52 = new Regexp(" IT ", "Variable Identifier", true);			//
    	Regexp re50 = new Regexp(" \"[^\"]*\" ", "Yarn Literal", false);
    	Regexp re51 = new Regexp(" [a-zA-Z][a-zA-Z0-9_]* ", "Variable Identifier", false);
    	Regexp re48 = new Regexp(" -?[0-9]+\\.[0-9]+ ", "Numbar Literal", false);
    	Regexp re49 = new Regexp(" -?[0-9]+ ", "Numbr Literal", false);
    	this.regex.add(re1);
    	this.regex.add(re2);
    	this.regex.add(re4);
    	this.regex.add(re3);
    	this.regex.add(re5);
    	this.regex.add(re6);
    	this.regex.add(re7);
    	this.regex.add(re9);
    	this.regex.add(re10);
    	this.regex.add(re11);
    	this.regex.add(re12);
    	this.regex.add(re13);
    	this.regex.add(re14);
    	this.regex.add(re15);
    	this.regex.add(re16);
    	this.regex.add(re17);
    	this.regex.add(re18);
    	this.regex.add(re19);
    	this.regex.add(re20);
    	this.regex.add(re21);
    	this.regex.add(re22);
    	this.regex.add(re23);
    	this.regex.add(re24);
    	this.regex.add(re25);
    	this.regex.add(re26);
    	this.regex.add(re28);
    	this.regex.add(re29);
    	this.regex.add(re30);
    	this.regex.add(re31);
    	this.regex.add(re32);
    	this.regex.add(re33);
    	this.regex.add(re34);
    	this.regex.add(re35);
    	this.regex.add(re36);
    	this.regex.add(re54);
    	this.regex.add(re38);
    	this.regex.add(re37);
    	this.regex.add(re39);
    	this.regex.add(re40);
    	this.regex.add(re41);
    	this.regex.add(re43);
    	this.regex.add(re44);
    	this.regex.add(re45);
    	this.regex.add(re46);
    	this.regex.add(re47);
    	this.regex.add(re42);
    	this.regex.add(re8);
    	this.regex.add(re53);
    	this.regex.add(re27);
    	this.regex.add(re52);
    	this.regex.add(re50);
    	this.regex.add(re51);
    	this.regex.add(re48);
    	this.regex.add(re49);
    }
    
    //display the source code in the codeField
    private void readCode(File selectedFile) throws Exception{
    	String code = "";
    	BufferedReader brInput = new BufferedReader(new FileReader(selectedFile));
		while((line=brInput.readLine())!=null) {
			code = code + line + "\n"; 
		}
		brInput.close();
		codeField.setText(code);
		
    }
    
    //lexical analyzer
    private void lexer(File selectedFile) throws Exception {
    	BufferedReader brInput;
		String line;
		brInput = new BufferedReader(new FileReader(selectedFile));
		boolean ifOBTW = false;
		this.lineNum = 1; //tracks line number
		while((line=brInput.readLine())!=null) {
			//remove tab
			if(line.contains("\u0020"))line = line.trim();
			
			//add space in the start and end of the line
			line = " "+line +" ";
			this.line = line;
			
			//OBTW-TLDR comment
			Pattern obtwPattern = Pattern.compile("OBTW");
		    Matcher obtwMatcher = obtwPattern.matcher(line);
		    Pattern tldrPattern = Pattern.compile("TLDR");
		    Matcher tldrMatcher = tldrPattern.matcher(line);
		    Pattern btwPattern = Pattern.compile("BTW");
		    Matcher btwMatcher = btwPattern.matcher(line);
		    //remove OBTW-TLDR comment
			if(obtwMatcher.find()){
				line = "";
				ifOBTW=true;
				this.lineNum++;
				continue;
			}else if(tldrMatcher.find()) {
				line = "";
				ifOBTW=false;
				this.lineNum++;
				continue;
			}
			if(ifOBTW==true) {
				line = "";
				this.lineNum++;
				continue;
			}
			//removes BTW and succeeding characters from the current line 
			if(btwMatcher.find()) { 
				StringBuffer text = new StringBuffer(line);
    			int index = line.indexOf("BTW");
    			int index2 = line.length();
    			text.replace(index,index2 ,"");
    			line =  text.toString();
			}
			
			for(Regexp regex: this.regex) {
				if(line.trim().isEmpty())	break; //line = whitespace
			    boolean hasString = true;
			    while(hasString) {
			    	Pattern pattern = Pattern.compile(regex.getPattern());
				    Matcher matcher = pattern.matcher(line);
				    while (matcher.find() && regex.getType() == "Yarn Literal"){ //match string literals
				    	int posQ1 = line.indexOf(matcher.group()); //finds position of matched string in the line (open quotes)
				    	int posLit = line.indexOf(matcher.group().substring(1, matcher.group().length()-1)); //start of yarn literal
				    	Word yarn = new Word(matcher.group(), "Yarn Literal", this.lineNum, posQ1);

				    	Word q1 = new Word(matcher.group().substring(1,2), "String Delimiter", this.lineNum, posQ1); //open quotation
			    		Word lit = new Word(matcher.group().substring(2,matcher.group().length()-2), regex.getType(), this.lineNum, posLit); //string literal

				    	if(Interpreter.lexemes.checker(q1)==false) {
				    		Interpreter.lexemes.add(q1);
				    	}
			    		if(Interpreter.lexemes.checker(lit)==false) {
				    		Interpreter.lexemes.add(lit);
				    	}
			    		Interpreter.lexemes.addToTokStr(yarn);
			    		
			    		//remove matched keyword in the line
			    		String rep = " ".repeat(matcher.group().length()); //replacement
				    	StringBuffer sbuff = new StringBuffer(line);
				    	int indexBegin = line.indexOf(matcher.group());
				    	int indexEnd = indexBegin + matcher.group().length();
				    	sbuff.replace(indexBegin, indexEnd, rep); //replace matched word with spaces
				    	line = sbuff.toString(); //update line with previous word removed
						
			    	}
				    //check if the current line has some remaining yarn literals
					Matcher matcher2 = pattern.matcher(line);
					if(matcher2.find() && regex.getType() == "Yarn Literal") {
						hasString = true;
					}else {
						hasString = false;
						
					}
			    }
			}
			for(Regexp regex: this.regex) {
				if(line.trim().isEmpty())	break; //line = whitespace
				Pattern pattern = Pattern.compile(regex.getPattern());
			    Matcher matcher = pattern.matcher(line);
			    while (matcher.find() && regex.isKeyword()){ //match keywords including some literals in line
			    		int posW = line.indexOf(matcher.group());
			    		Word word = new Word(matcher.group(), regex.getType(), this.lineNum, posW);
				    	if(Interpreter.lexemes.checker(word)==false) {
				    		if(regex.getType().equals("Output Keyword")) {
				    			Word symbolWord = new Word("IT", "", -1, -1);
				    			Interpreter.symbolTable.add(symbolWord);
				    		}
				    		Interpreter.lexemes.add(word);
				    	}
				    	Interpreter.lexemes.addToTokStr(word);
				    	
				    	//remove matched keyword in the line
				    	String rep = " ".repeat(matcher.group().length()); //replacement
				    	StringBuffer sbuff = new StringBuffer(line);
				    	int indexBegin = line.indexOf(matcher.group());
				    	int indexEnd = indexBegin + matcher.group().length();
				    	sbuff.replace(indexBegin, indexEnd, rep); //replace matched word with spaces
				    	line = sbuff.toString(); //update line with previous word removed

		    	}
			}
			
				for(Regexp regex: this.regex) {
					ArrayList<String> matched = new ArrayList<String>();
					if(line.trim().isEmpty())	break; //line = whitespace
					boolean hasVar = true;
					while(hasVar) {
						Pattern pattern = Pattern.compile(regex.getPattern());
					    Matcher matcher = pattern.matcher(line);
						while(matcher.find() && !regex.isKeyword()){ //match variable identifiers and some literals
							matched.add(matcher.group());
		    				int posW = line.indexOf(matcher.group());
		    				Word word = new Word(matcher.group(), regex.getType(), this.lineNum, posW);
		    				if(Interpreter.lexemes.checker(word)==false) {
		    					//word.setPos(line.indexOf(matcher.group()));
					    		if(regex.getType().equals("Variable Identifier")==true) {
					    			Word symbolWord = new Word(matcher.group(), "", -1, -1);
					    			symbolWord.setValue(symbolWord.getValue1().substring(1,symbolWord.getValue1().length()-1));
					    			Interpreter.symbolTable.add(symbolWord);
					    		}
					    		Interpreter.lexemes.add(word);
					    	}
		    				Interpreter.lexemes.addToTokStr(word);
		    				//remove matched keyword in the line
					    	String rep = " ".repeat(matcher.group().length()); //replacement
					    	StringBuffer sbuff = new StringBuffer(line);
					    	int indexBegin = line.indexOf(matcher.group());
					    	int indexEnd = indexBegin + matcher.group().length();
					    	sbuff.replace(indexBegin, indexEnd, rep); //replace matched word with spaces
					    	line = sbuff.toString(); //update line with previous word removed
					    	
				    	}
						//check if the current line has some remaining variable identifier
						 Matcher matcher2 = pattern.matcher(line);
						if(matcher2.find()) {
							hasVar = true;
						}else {
							hasVar = false;
						}
					}
					
				}		
			if(!line.trim().isEmpty()) { //if there is still unmatched words left in the line, unrecognized
				printError("Line "+this.lineNum +": Unrecognized token");
				this.lexerErr = true;
				break;
				
			}
			this.lineNum++;
			
		}
		brInput.close();
    }

//    private void printTokens() {
//    	ArrayList<Word> tokens = Interpreter.lexemes.getTokens();
//    	System.out.println("\n==============TOKENS==============\n");
//    	for(int i=0; i<tokens.size(); i++) {
//    		System.out.println(tokens.get(i).getLineNum()+" "+tokens.get(i).getPos()+" "+tokens.get(i).getValue1()+" "+tokens.get(i).getValue2());
//    	}
//    }
    
   //sort token stream
    private void createListOfTokenStreams(ArrayList<Word> tokens) { 
    	int cnt = 1;
    	ArrayList<Word> currTokens = new ArrayList<Word>();
    	while(cnt <= tokens.get(tokens.size()-1).getLineNum()) { //while cnt is not the last line
    		currTokens.clear();
    		for(Word w: tokens) {
        		if(cnt == w.getLineNum()) {
        			currTokens.add(w); //add all tokens that are on the same line to currTokens
        		}
        	}
    		
    		//arrange tokens based on pos 
    		int[] posArray = new int[currTokens.size()];
    		for(int i=0; i<currTokens.size(); i++) {
    			posArray[i] = currTokens.get(i).getPos();
    		}
    		Arrays.sort(posArray); //sort indices in ascending order
    		TokenStream tokenStream = new TokenStream(); //creates instance of TokenStream
    		for(int i=0; i<posArray.length; i++) {
    			for(Word w:currTokens) {
    				if(posArray[i] == w.getPos()) {
    					tokenStream.addToken(w); //adds token in tokenStream (sorted)
    					break;
    				}
    			}
    		}
    		
    		//adds tokenStream to list of tokenStreams
    		this.tokenStreamList.add(tokenStream);
    		cnt++;
    	}
    	
    	//creates the list of words in the whole code for parsing
    	for(TokenStream ts: this.tokenStreamList) {
    		if(ts.getStream().isEmpty()) continue;
    		Word prev = ts.getStream().get(0);
    		for(Word w: ts.getStream()) {
    			this.lexOutput.add(w);
    			prev = w;
    		}
    		Word newline = new Word("\\n", "Linebreak", prev.getLineNum(), prev.getPos()+1);
    		this.lexOutput.add(newline); //adds new line after every token stream
    	}
    	this.lexOutput.remove(this.lexOutput.size()-1); //removes newline after last line of code
    }
    
    //display result of the program in the resultField
    static void printResult(ArrayList<String> toPrint) {
    	resultField.setStyle("-fx-text-fill: black");
    	String line = "";
    	for(String s: toPrint) {
    		line = line.concat(s.replace("\"",""));
    	}
    	fieldContent.append(line+"\n");
    	resultField.setText(fieldContent.toString());
    }
    
    //print error message in the resultField
    static void printError(String errMessage) {
    	resultField.setText(errMessage+"\n");
    	resultField.setStyle("-fx-text-fill: red");
    }
    
    //clear UI for every new source code is selected
    void clearUI() {
    	this.root.getChildren().clear();
    	this.table1 = new TableView<>();
		this.table2 = new TableView<>();
		lexemes = new Lexeme();
		symbolTable = new SymbolTable();
		this.lexemeCol  = new TableColumn<Word, String>("Lexeme");
		this.classCol  = new TableColumn<Word, String>("Classification");
		this.identifierCol  = new TableColumn<Word, String>("Identifer");
		this.valueCol  = new TableColumn<Word, String>("Value");
		Interpreter.codeField.clear();;
		Interpreter.resultField.clear();
		this.regex = new ArrayList<Regexp>();
		this.lexerErr = false; //set to  true if lexer found errors
		this.tokenStreamList = new ArrayList<TokenStream>();
		this.lexOutput = new ArrayList<Word>();
		this.lineNum = 0; //current line number in lexer
		this.line = ""; //current line in lexer
		fieldContent = new StringBuilder("");
		this.initInterpreter();
		this.selectFileButton(); 
		this.executeButton(); 
		
    }
    
}