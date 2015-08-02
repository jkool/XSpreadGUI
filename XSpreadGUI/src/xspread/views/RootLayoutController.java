package xspread.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import xspread.MainGUI;

public class RootLayoutController {
	
	private MainGUI mgui;
	
	public void setMainGUI(MainGUI mgui){
		this.mgui=mgui;
	}
	
	@FXML
	private void handleOpen(){
		FileChooser fc = new FileChooser();
		
		List<FileChooser.ExtensionFilter> filters = new ArrayList<FileChooser.ExtensionFilter>();
		filters.add(new FileChooser.ExtensionFilter("Parameter files", "*.prm"));
		filters.add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
		filters.add(new FileChooser.ExtensionFilter("All files", "*.*"));
		fc.getExtensionFilters().addAll(filters);
		
		//Show open dialog
		
		File file = fc.showOpenDialog(mgui.getPrimaryStage());
		
		if(file != null){
			mgui.loadDataFromFile(file);
		}	
	}
	
	@FXML
	private void handleSave(){
		FileChooser fc = new FileChooser();
		List<FileChooser.ExtensionFilter> filters = new ArrayList<FileChooser.ExtensionFilter>();
		filters.add(new FileChooser.ExtensionFilter("Parameter files", "*.prm"));
		filters.add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
		fc.getExtensionFilters().addAll(filters);
		
		//Show open dialog
		
		File file = fc.showSaveDialog(mgui.getPrimaryStage());
		
		if(file != null){
			mgui.saveDataToFile(file);
		}	
	}
	
	@FXML
	private void handleRun(){
		try {
			mgui.run();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@FXML
	private void handleClose(){
		mgui.getPrimaryStage().close();
	}
}
