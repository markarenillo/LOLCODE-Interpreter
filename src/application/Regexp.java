package application;

//Regexp stores the pattern of the keywords and literals
public class Regexp {
	private String pattern;
	private String type;
	private boolean keyword;
	
	Regexp(String pattern, String type, boolean key){
		this.pattern = pattern;
		this.type = type;
		this.keyword = key; 
	}
	
	//get the regex pattern
	String getPattern() {
		return this.pattern;
	}
	//get type of pattern
	String getType() {
		return this.type;
	}
	//return true if regex is a keyword, otherwise false
	boolean isKeyword() {
		return this.keyword;
	}
}