package xspread.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class Monitor extends OutputStream
{
    private TextArea    output;
	private Stage stage;
	private PrintStream oldOut;
	private PrintStream oldErr;

    public Monitor(TextArea ta)
    {
		PrintStream ps = new PrintStream(this, true);
		oldOut = System.out;
		oldErr = System.err;
		System.setOut(ps);
		System.setErr(ps);
		
		stage.setScene(new Scene(new Group(ta))); 
		stage.setTitle("Model run");
    	
        this.output = ta;
    }

    @Override
    public void write(int i) throws IOException
    {
        output.appendText(String.valueOf((char) i));
    }
    
    @Override
	public void close(){
    	System.setOut(oldOut);
    	System.setErr(oldErr);
    	stage.close();
    }
}