package application;

//Variable is used for storing variable and its properties
public class Variable {
	private String name; 
	private String value;
	private String type;
	
	Variable(String name){
		this.name = name;
	}
	//get variable name
	String getName() {
		return this.name;
	}
	//set variable value
	void setValue(String value) {
		this.value = value;
	}
	//get variable value
	String getValue() {
		return this.value;
	}
	//set variable type
	void setType(String type){
		this.type = type;
	}
	//get variable type
	String getType(){
		return this.type;
	}
	
	
}