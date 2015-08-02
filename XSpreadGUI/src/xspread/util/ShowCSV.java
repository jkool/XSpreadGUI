package xspread.util;

import java.io.BufferedReader;
import java.io.FileReader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

public class ShowCSV extends Application{

    @Override
	public void start(Stage primaryStage) {
    final BorderPane root = new BorderPane();
    final TableView<ObservableList<StringProperty>> table = new TableView<>();
    
    /*final TextField urlTextEntry = new TextField();
    urlTextEntry.setPromptText("Enter URL of tab delimited file");
    final CheckBox headerCheckBox = new CheckBox("Data has header line");
    urlTextEntry.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        populateTable(table, urlTextEntry.getText(),
            headerCheckBox.isSelected());
      }
    });*/
    populateTable(table, "C:/Temp2/STest_BK/para_mycoolstats.csv",
            true);
    root.setCenter(table);
    Scene scene = new Scene(root, 600, 400);
    primaryStage.setScene(scene);
    primaryStage.show();
  }

   public void populateTable(
      final TableView<ObservableList<StringProperty>> table,
      final String filename, final boolean hasHeader) {
    table.getItems().clear();
    table.getColumns().clear();
    table.setPlaceholder(new Label("Loading..."));
    Task<Void> task = new Task<Void>() {
   
      @Override
	protected Void call() throws Exception {
    	  BufferedReader in = new BufferedReader(new FileReader(filename));
        // Header line
        if (hasHeader) {
          final String headerLine = in.readLine();
          final String[] headerValues = headerLine.split(",");
          Platform.runLater(new Runnable() {
     
            @Override
			public void run() {
              for (int column = 0; column < headerValues.length; column++) {
                table.getColumns().add(
                    createColumn(column, headerValues[column]));
              }
            }
          });
        }

        // Data:

        String dataLine;
        while ((dataLine = in.readLine()) != null) {
          final String[] dataValues = dataLine.split(",");
          Platform.runLater(new Runnable() {
         
            @Override
			public void run() {
              // Add additional columns if necessary:
              for (int columnIndex = table.getColumns().size(); columnIndex < dataValues.length; columnIndex++) {
                table.getColumns().add(createColumn(columnIndex, ""));
              }
              // Add data to table:
              ObservableList<StringProperty> data = FXCollections
                  .observableArrayList();
              for (String value : dataValues) {
                data.add(new SimpleStringProperty(value));
              }
              table.getItems().add(data);
            }
          });
        }
        
        in.close();
        return null;
      }
    };
    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.start();
  }

  private TableColumn<ObservableList<StringProperty>, String> createColumn(
      final int columnIndex, String columnTitle) {
    TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>();
    String title;
    if (columnTitle == null || columnTitle.trim().length() == 0) {
      title = "Column " + (columnIndex + 1);
    } else {
      title = columnTitle;
    }
    column.setText(title);
    column
        .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<StringProperty>, String>, ObservableValue<String>>() {
          @Override
          public ObservableValue<String> call(
              CellDataFeatures<ObservableList<StringProperty>, String> cellDataFeatures) {
            ObservableList<StringProperty> values = cellDataFeatures.getValue();
            if (columnIndex >= values.size()) {
              return new SimpleStringProperty("");
            } else {
              return cellDataFeatures.getValue().get(columnIndex);
            }
          }
        });
    return column;
  }
  
  public static void main(String[] args) {
      Application.launch(args);
  }
}