package application;

//Word is used for storing lexeme and its properties
public class Word{
	private String value1; //lexeme
	private String value2; //type
	private int lineNum; //line number
	private int pos; //position (index) in the line
	private String dtype; //data type
	
	public Word(String value1, String value2, int lineNum, int pos){
		this.value1 = value1;
		this.value2 = value2;
		this.lineNum = lineNum;
		this.pos = pos;
	}
	//get the lexeme
	public String getValue1() {
		return this.value1;
	}
	//get the type
	public String getValue2() {
		return this.value2;
	}
	//get the specified line number
	public int getLineNum() {
		return this.lineNum;
	}
	
	//get the position(index) of the word in the line
	public int getPos() {
		return this.pos;
	}
	
	//set the lexeme for expression evaluation
	void setValue(String val) {
		this.value1 = val;
	}
	//set the type for expression evaluation
	void setValue2(String val) {
		this.value2 = val;
	}
	//get data type of the word
	String getdType() {
		return this.dtype;
	}
	//set data type of the word
	void setdType(String dtype) {
		this.dtype = dtype;
	}
	
}