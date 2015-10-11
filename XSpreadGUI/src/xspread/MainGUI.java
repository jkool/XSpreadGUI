package xspread;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.swing.JOptionPane;

import xspread.application.SpreadProcess;
import xspread.application.SpreadProperties;
import xspread.postprocess.CalibrationAnalysis;
import xspread.util.CalibrationUI;
import xspread.util.ChartViewer;
import xspread.util.ErrorHandler;
import xspread.util.ImageGallery;
import xspread.views.RootLayoutController;

public class MainGUI extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
	private SpreadProperties sp;
	private TabPane tp;
	private Tab cuiTab;
	private Tab igTab;
	private Tab chartTab;
	private boolean resultsVisible = false;
	private CalibrationAnalysis calref;

	/**
	 * Starts the main window
	 */

	@Override
	public void start(Stage primaryStage) {
		String vstring = System.getProperty("java.version");
		if (versionCompare(vstring, "1.8.0") < 0) {
			JOptionPane.showMessageDialog(null,
					"Java 1.8.0 (version 8) or higher is required to run this software.\n               Version "
							+ vstring + " is currently installed.  Exiting.");
			System.exit(0);
		}

		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("XSpread");

		// Create a default properties file

		sp = new SpreadProperties();

		// Set up the root layout

		initRootLayout();
		calibrationQuery();
		showProperties();
		// showOverview();
	}

	/**
	 * Sets up the root layout
	 */

	public void initRootLayout() {
		try {

			// Loads the layout framework form the RootLayout.fxml file.

			FXMLLoader loader = new FXMLLoader();

			String layoutPath = "views/RootLayout.fxml";

			URL url = MainGUI.class.getResource(layoutPath);

			// if the file cannot be loaded, throw an exception.

			if (url == null) {
				throw new IOException(
						"The program layout file is not available at: \n\n"
								+ layoutPath
								+ "\n\nPlease ensure that the file location is correct and accessible.\n\n");
			}

			loader.setLocation(url);

			rootLayout = (BorderPane) loader.load();

			// Set up the main container window

			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);

			RootLayoutController controller = loader.getController();
			controller.setMainGUI(this);

			// Use the CDU icon

			// if (new File("./resource files/CDU_32.png").exists()) {
			// primaryStage.getIcons().add(
			// new Image("file:resource files/CDU_32.png"));
			// }

			primaryStage
					.getIcons()
					.add(new Image(
							MainGUI.class
									.getResourceAsStream("/xspread/resources/CDU_32.png")));

			primaryStage.show();

		} catch (Exception e) {
			ErrorHandler.showException(e);
			System.exit(-1);
		}
	}

	/**
	 * Perform a model run
	 * 
	 * @throws InterruptedException
	 */

	@SuppressWarnings("unchecked")
	public void run() throws InterruptedException {

		if (sp.getSpeciesList().isEmpty()) {
			ErrorHandler.showWarning("Species List Is Empty",
					"The species list is empty.  No processing will occur.");
			return;
		}

		if (resultsVisible) {
			clearResults();
		}

		// Create a new running window

		Stage secondaryStage = new Stage();
		secondaryStage.getIcons().add(
				new Image(MainGUI.class
						.getResourceAsStream("/xspread/resources/CDU_32.png")));
		secondaryStage.setResizable(false);
		secondaryStage.setTitle("Running...");
		secondaryStage.setAlwaysOnTop(true);
		Group secondRoot = new Group();
		Scene scene = new Scene(secondRoot, 380, 110, Color.WHITE);
		GridPane mainPane = new GridPane();
		secondRoot.getChildren().add(mainPane);

		// Start the run process

		SpreadProcess sproc = new SpreadProcess(sp);
		
		secondaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
			@Override
			public void handle(WindowEvent event) {
				sproc.cancel(true);
				System.out.println("Cancelled.");
				secondaryStage.close();
			}
		});

		if(sp.getRunType().equalsIgnoreCase("Calibration")){
			// Set up the progress bar

			ProgressBar pb = new ProgressBar();
			pb.setProgress(0.0d);
			pb.setMinWidth(140);
			pb.setPadding(new Insets(20, 10, 10, 10));
			// pb.progressProperty().unbind();
			pb.progressProperty().bind(sproc.getDistProp()); // Retrieves
																	// progress from
																	// SpreadProcess
			
			ProgressBar pb2 = new ProgressBar();
			pb2.setProgress(0.0d);
			pb2.setMinWidth(140);
			pb2.setPadding(new Insets(10, 10, 10, 10));
			pb2.progressProperty().bind(sproc.getRateProp()); // Retrieves
			// progress from
			// SpreadProcess
			
			sproc.setOnSucceeded(event -> {
				pb.progressProperty().unbind();
				pb.setProgress(0);
				secondaryStage.close();
				calref = sproc.getCalibration();
				new Thread(new DispThread()).start();
			});

			sproc.setOnFailed(event -> {
				secondaryStage.close();
				ErrorHandler.showException(sproc.getException());
			});
			
			Label distanceLabel = new Label("Starting...");
			distanceLabel.textProperty().bind(sproc.getDistProgressText());
			distanceLabel.setPadding(new Insets(2, 0, 0, 5));
			
			Label rateLabel = new Label("Starting...");
			rateLabel.textProperty().bind(sproc.getRateProgressText());
			rateLabel.setPadding(new Insets(0, 0, 2, 5));
			
			mainPane.add(distanceLabel, 0, 0);
			mainPane.add(pb, 1, 0);
			mainPane.add(rateLabel, 0, 1);
			mainPane.add(pb2, 1, 1);

			final Button cancelButton = new Button("Cancel");
			mainPane.add(cancelButton, 3, 2);

			cancelButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					sproc.cancel(true);
					pb.progressProperty().unbind();
					pb.setProgress(0);
					pb2.progressProperty().unbind();
					pb2.setProgress(0);
					System.out.println("Cancelled.");
					secondaryStage.close();
				}
			});
		}
		
		else{
			// Set up the progress bar

			ProgressBar pb = new ProgressBar();
			pb.setProgress(0.0d);
			pb.setMinWidth(140);
			pb.setPadding(new Insets(20, 10, 10, 10));
			pb.progressProperty().bind(sproc.getPairProp()); // Retrieves
																	// progress from
																	// SpreadProcess
			
			sproc.setOnSucceeded(event -> {
				pb.progressProperty().unbind();
				pb.setProgress(0);
				secondaryStage.close();
				calref = sproc.getCalibration();
				new Thread(new DispThread()).start();
			});

			sproc.setOnFailed(event -> {
				secondaryStage.close();
				ErrorHandler.showException(sproc.getException());
			});
			
			Label pairLabel = new Label("Starting...");
			pairLabel.textProperty().bind(sproc.getPairProgressText());
			
			mainPane.add(pairLabel, 0, 0);
			mainPane.add(pb, 1, 0);

			final Button cancelButton = new Button("Cancel");
			mainPane.add(cancelButton, 3, 1);

			cancelButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					sproc.cancel(true);
					pb.progressProperty().unbind();
					pb.setProgress(0);
					System.out.println("Cancelled.");
					secondaryStage.close();
				}
			});
		}

		secondaryStage.setScene(scene);
		secondaryStage.show();

		// Set up the processing thread

		new Thread(sproc).start();

	}

	public void showProperties() {

		// rootLayout.setCenter(treeTableView);
		TabPane tp = (TabPane) rootLayout.getCenter();
		Tab t = tp.getTabs().get(0);
		t.setContent(sp.asTreeTableView());

		Image play_img = new Image(
				MainGUI.class
						.getResourceAsStream("/xspread/resources/play_2.png"));
		ImageView iv = new ImageView(play_img);
		iv.setPreserveRatio(true);
		iv.setFitHeight(16);
		Button play = new Button("", iv);
		play.setOnAction(event -> {
			try {
				this.run();
			} catch (Exception e) {
				// handled explicitly elsewhere.
			}
		});
		rootLayout.setBottom(play);
		play.setAlignment(Pos.BOTTOM_LEFT);
	}

	public static void main(String[] args) {
		launch(args);
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public void loadDataFromFile(File parameters) {

		try {
			sp.readTextFile(parameters);
		} catch (Exception e) {
			ErrorHandler.showException(e);
		}

		showProperties();
	}

	public void saveDataToFile(File file) {
		try {
			sp.writeTextFile(file);
		} catch (IOException e) {
			ErrorHandler.showException(e);
		}
	}

	public void setupProperties() {

	}

	public void calibrationQuery() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Calibration");
		alert.setHeaderText("Do you want to run in calibration mode?");
		alert.setContentText("Choose wisely.");

		ButtonType buttonTypeYes = new ButtonType("Yes");
		ButtonType buttonTypeNo = new ButtonType("No");

		alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);
		
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == buttonTypeYes) {
			sp.setRunType("calibration");
		} else {
			sp.setRunType("paired");
		}
	}

	public BorderPane getRootLayout() {
		return rootLayout;
	}

	public TabPane getTabs() {
		return tp;
	}

	public Tab getCUITab() {
		return cuiTab;
	}

	public Tab getImageGalleryTab() {
		return igTab;
	}

	public Tab getChartTab() {
		return chartTab;
	}

	public void clearResults() {
		tp.getTabs().remove(1, tp.getTabs().size());
		resultsVisible = false;
	}

	public class DispThread extends Thread {
		@Override
		public void run() {
			tp = (TabPane) rootLayout.getCenter();

			if (sp.getRunType().equalsIgnoreCase("Calibration")) {
				cuiTab = new Tab("Calibration Results");
				CalibrationUI cui = new CalibrationUI();
				cui.setParentGUI(MainGUI.this);
				cui.setSpreadPropertiesReference(sp);
				cui.setCalVals(calref.getCalVals());
				cui.setSelVals(calref.getSelected());
				cui.getGallery(true);
				//tp.getTabs().add(cuiTab);
				Platform.runLater(()->{tp.getTabs().add(cuiTab);});
			}

			igTab = new Tab("Image Gallery");
			ImageGallery ig = new ImageGallery();
			ig.setParentGUI(MainGUI.this);
			ig.setSpreadPropertiesReference(sp);
			//ig.getGallery();
			ig.getGallery();
			//tp.getTabs().add(igTab);
			Platform.runLater(()->{tp.getTabs().add(igTab);});

			if(sp.isWritingTrace()){
			chartTab = new Tab("Plots");
			ChartViewer cv = new ChartViewer();
			cv.setParentGUI(MainGUI.this);
			cv.setSpreadPropertiesReference(sp);
			//cv.getGallery();
			Platform.runLater(()->{cv.getGallery();});
			//tp.getTabs().add(chartTab);
			Platform.runLater(()->{tp.getTabs().add(chartTab);});

			resultsVisible = true;
			}	
		}
	}

	/**
	 * Compares two version strings.
	 * 
	 * Use this instead of String.compareTo() for a non-lexicographical
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 * 
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 * 
	 * @param str1
	 *            a string of ordinal numbers separated by decimal points.
	 * @param str2
	 *            a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less
	 *         than str2. The result is a positive integer if str1 is
	 *         _numerically_ greater than str2. The result is zero if the
	 *         strings are _numerically_ equal.
	 */
	private Integer versionCompare(String str1, String str2) {
		String[] vals1 = str1.split("\\.|_");
		String[] vals2 = str2.split("\\.|_");
		int i = 0;
		// set index to first non-equal ordinal or length of shortest version
		// string
		while (i < vals1.length && i < vals2.length
				&& vals1[i].equals(vals2[i])) {
			i++;
		}
		// compare first non-equal ordinal number
		if (i < vals1.length && i < vals2.length) {
			int diff = Integer.valueOf(vals1[i]).compareTo(
					Integer.valueOf(vals2[i]));
			return Integer.signum(diff);
		}
		// the strings are equal or one string is a substring of the other
		// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
		else {
			return Integer.signum(vals1.length - vals2.length);
		}
	}
}