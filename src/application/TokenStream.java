package application;

import java.util.ArrayList;

//TokenStream contains tokens to be used for parsing
public class TokenStream {
	private ArrayList<Word> stream;
	
	TokenStream(){
		this.stream = new ArrayList<Word>();
	}
	
	//add token in stream
	void addToken(Word w) {
		this.stream.add(w);
	}
	//get stream
	ArrayList<Word> getStream(){
		return this.stream;
	}
}