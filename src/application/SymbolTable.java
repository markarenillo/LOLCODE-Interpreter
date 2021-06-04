package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

//SymbolTable is used for storing information of variables such as their value
public class SymbolTable {
	private ObservableList<Word> symbolList;
	public SymbolTable(){
		this.symbolList = FXCollections.observableArrayList();
	}
	//add word to symboList
	void add(Word word) {
		//check if word is already added
		if(checker(word)) {
			int ind = getIndex(word);
			this.symbolList.set(ind,word);

		}else {
			this.symbolList.add(word);
		}	
	}
	
	//return list
	ObservableList<Word> getList() {
		return this.symbolList;
	}
	//check if word is already added in the symbolList
	boolean checker(Word word) {
		 for (Word w : this.symbolList) { 
			 if(w.getValue1().equals(word.getValue1())) {
				 return true; 
			 }
		 }
		 return false;
	 }
	//get index of the word in the symbolList
	int getIndex(Word word) {
		 int i = 0;
		 for (Word w : this.symbolList) { 
			 if(w.getValue1().equals(word.getValue1())) {
				 return i;
			 }
			 i++;
		 }
		 return -1;
	 }


}
