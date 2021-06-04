package application;

import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

//Lexeme contains list of lexemes and tokens to be used in Lexeme Table
public class Lexeme {
	private static ObservableList<Word> lexemeList;
	private static ArrayList<Word> tokenStream;
	
	public Lexeme(){
		lexemeList = FXCollections.observableArrayList();
		tokenStream = new ArrayList<Word>();
	}
	//add word to list
	void add(Word word) {
		lexemeList.add(word);
	}
	
	//add word to token stream
	void addToTokStr(Word word) {
		tokenStream.add(word);
	}
	
	//return list
	 static ObservableList<Word> getList() {
		return lexemeList;
	}
	 
	 //get tokenStream
	 ArrayList<Word> getTokens(){
		 return tokenStream;
	 }
	 //check if word is already added in the lexemelist
	 boolean checker(Word word) {
		 for (Word w : lexemeList) { 
			 if(w.getValue1().equals(word.getValue1()) && w.getValue2().equals(word.getValue2())) {
				 return true; 
			 }
		 }
		 return false;
	 }


}