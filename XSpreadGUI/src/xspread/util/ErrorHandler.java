package xspread.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ErrorHandler {
	public static void showException(Throwable th) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(th.getClass().getSimpleName());
		alert.setContentText(th.getMessage());
		alert.showAndWait();
	}
	
	public static void showWarning(String messageHead, String messageBody){
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Warning");
		alert.setHeaderText(messageHead);
		alert.setContentText(messageBody);
		alert.showAndWait();
	}
}
