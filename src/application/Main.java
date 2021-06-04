package application;


import javafx.application.Application;
import javafx.stage.Stage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
    	//Instantiate Interpreter
    	Interpreter interpreter = new Interpreter();
    	//set stage
    	interpreter.setStage(primaryStage);
    }


    public static void main(String[] args){
        launch(args);
    }
    
    
}