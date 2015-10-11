package xspread.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import xspread.MainGUI;
import xspread.application.SpreadProperties;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class ChartViewer {

	private CSV csv = new CSV("C:/Temp2/STest_BK/para_Trace_0_0.csv");
	private String path = "C:/Temp2/STest_BK";
	private String table = "";
	private String xField = "Time";
	private String yField = "Patches infested by species";
	private SplitPane root;
	private LineChart<Number, Number> lineChart = new LineChart<Number,Number>(new NumberAxis(),new NumberAxis());
	private SpreadProperties spref;
	private ComboBox<String> speciesList;
	private final ComboBox<String> xComboBox;
	private final ComboBox<String> yComboBox;
	private final ComboBox<String> partitionComboBox;
	private String species = "<Empty>";
	public final static BiMap<String, String> fieldLookup = HashBiMap.create();
	private final Button save;
	private MainGUI parent;
	private String partitionBy = "None";
	private String function = "AVG";

	@SuppressWarnings("unchecked")
	public ChartViewer() {
		fieldLookup.put("None", "");
		fieldLookup.put("Time", "Time");
		fieldLookup.put("Distance", "Distance");
		fieldLookup.put("Rate", "Rate");
		fieldLookup.put("Replicate", "Replicate");
		fieldLookup.put("Patches infested by species", "S_Infested");
		fieldLookup.put("Patches infested by any", "P_Infested");
		fieldLookup.put("Undetected Infestations", "S_Undetected");
		fieldLookup.put("Ground control", "Ground");
		fieldLookup.put("Containment", "Containment");
		fieldLookup.put("Core size", "Core");
		fieldLookup.put("Containment total", "Containment_sum");
		fieldLookup.put("No data", "Ndata");
		fieldLookup.put("Cost", "Cost");
		fieldLookup.put("Labour", "Labour");
		fieldLookup.put("Run", "RunID");
		
		final ComboBox<String> speciesComboBox = new ComboBox<String>();
		speciesComboBox.getItems().addAll("ALL");
		speciesComboBox.setValue("ALL");

		xComboBox = new ComboBox<String>();
		xComboBox.getItems().addAll("Time", "Distance", "Rate", "Replicate",
				"Patches infested by species", "Patches infested by any",
				"Undetected Infestations", "Ground control", "Containment",
				"Cost", "Labour");
		xComboBox.setValue(xField);
		xComboBox.setOnAction((event) -> {
			xField = xComboBox.getSelectionModel().getSelectedItem();
			updateSeries();
			});

		yComboBox = new ComboBox<String>();
		yComboBox.getItems().addAll("Time", "Distance", "Rate", "Replicate",
				"Patches infested by species", "Patches infested by any",
				"Undetected Infestations", "Ground control", "Containment",
				"Cost", "Labour");
		yComboBox.setValue(yField);
		yComboBox.setOnAction((event) -> {
			yField = yComboBox.getSelectionModel().getSelectedItem();
			updateSeries();
		});

		partitionComboBox = new ComboBox<String>();
		partitionComboBox.getItems().addAll("None","Time", "Distance", "Rate", "Replicate",
				"Patches infested by species", "Patches infested by any",
				"Undetected Infestations", "Ground control", "Containment",
				"Cost", "Labour", "Run");
		partitionComboBox.setValue("None");
		partitionComboBox.setOnAction((event) -> {
			partitionBy = partitionComboBox.getSelectionModel().getSelectedItem();
			updateSeries();
		});

		save = new Button("Save Image");
		save.setOnAction((event) -> {
			FileChooser fc = new FileChooser();
			List<FileChooser.ExtensionFilter> filters = new ArrayList<FileChooser.ExtensionFilter>();
			filters.add(new FileChooser.ExtensionFilter("PNG files", "*.png"));
			fc.getExtensionFilters().addAll(filters);

			// Show open dialog

			File file = fc.showSaveDialog(new Stage());

			if (file != null) {
				LineChart<Number, Number> lc = (LineChart<Number, Number>) root
						.getItems().get(0);
				WritableImage image = lc.snapshot(new SnapshotParameters(),
						null);
				try {
					ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png",
							file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@SuppressWarnings("unused")
	public boolean updateSeries(){
		lineChart.getData().clear();
		
		//if(table.isEmpty()||!new File(table).exists()){
		//	Platform.runLater(()->{ErrorHandler.showException(new IOException(path + "\\" + table + " does not exist.  Please check the file location and ensure it can be written."));});
		//	return false;
		//}
		
		csv = new CSV(path + "/" + table);
		
		Number[] xdata = new Number[] {};
		Number[] ydata = new Number[] {};
		try {
			xdata = csv.getColumn(fieldLookup.get(xField));
			ydata = csv.getColumn(fieldLookup.get(yField));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		try {
			lineChart.getData().addAll(getSeriesSet());
		} catch (SQLException e) {
			e.printStackTrace();
			//Platform.runLater(()->{ErrorHandler.showException(new IOException("Error reading from trace file: " + path + "\\" + table + ".  Please check the file location and ensure it can be read."));});
		}
		lineChart.setTitle(csv.getName());
		lineChart.getXAxis().setLabel(xField);
		lineChart.getYAxis().setLabel(yField);
		return true;
	}
	

	public Series<Number, Number> getSeries(Number[] xdata, Number[] ydata) {

		final XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
		series.setName(TextUtils.removeExtension(csv.getName()));

		int ct = xdata.length;

		// populating the series with data

		for (int i = 0; i < ct; i++) {
			series.getData().add(
					new XYChart.Data<Number, Number>(xdata[i], ydata[i]));
		}

		return series;
	}

	public void getGallery() {
		
		if(path.isEmpty() || table.isEmpty()){
			ErrorHandler.showException(new IllegalArgumentException("No chart file(s) to display."));
			return;
		}
		
		root = new SplitPane();
		BorderPane rhs = new BorderPane();
		root.setOrientation(Orientation.HORIZONTAL);
		root.setDividerPosition(0, .7);
		
		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel(xField);
		yAxis.setLabel(yField);
		// creating the chart
		lineChart = new LineChart<Number, Number>(xAxis, yAxis);
		lineChart.setTitle(csv.getName());
		lineChart.setAnimated(false);

		GridPane grid = new GridPane();
		grid.setVgap(4);
		grid.setHgap(10);
		grid.setPadding(new Insets(5, 5, 5, 5));
		Label speciesLabel = new Label("Species: ");
		grid.add(speciesLabel, 0, 0);
		grid.add(speciesList, 1, 0);
		grid.add(new Label("X Axis: "), 0, 1);
		grid.add(xComboBox, 1, 1);
		grid.add(new Label("Y Axis: "), 0, 2);
		grid.add(yComboBox, 1, 2);
		grid.add(new Label("Partition by: "), 0, 3);
		grid.add(partitionComboBox, 1, 3);
		final Pane spring = new Pane();
		spring.minHeightProperty().bind(speciesLabel.heightProperty());
		grid.add(spring,1,4);
		grid.add(save,1,5);

		rhs.setCenter(grid);
		if(!updateSeries()){
			return;
		}
		root.getItems().addAll(lineChart, rhs);
		parent.getChartTab().setContent(root);
	}
	
	public void setSpreadPropertiesReference(SpreadProperties sprop) {
		this.spref = sprop;
		path = spref.getOutputFolder();
		if (!spref.getSpeciesList().isEmpty()) {
			species = spref.getSpeciesList().get(0);
			table = species + "_TraceFile.csv";
			ObservableList<String> speciesOptions = FXCollections
					.observableArrayList();
			speciesOptions.addAll(spref.getSpeciesList());
			speciesList = new ComboBox<String>(speciesOptions);
			speciesList.setOnAction(event -> {
				if (!species.equals(speciesList.getValue())) {
					species = speciesList.getValue();
					table = species + "_TraceFile.csv";
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							updateSeries();
						}
					});
				}
			});
			speciesList.setValue(species);
		} else {
			speciesList = new ComboBox<String>(
					FXCollections.observableArrayList("<Empty>"));
			
			speciesList.setOnAction(event -> {
				species = speciesList.getValue();
				updateSeries();
			});
		}
	}
	
	public void setParentGUI(MainGUI gui) {
		this.parent = gui;
	}
	
	public ObservableList<XYChart.Series<Number,Number>> getSeriesSet() throws SQLException {
		
		ObservableList<XYChart.Series<Number,Number>> seriesSet = FXCollections.observableArrayList();
		String csvTable = table.substring(0, table.length()-4);
		Properties props = new Properties();
		props.put("columnTypes",
				"Int,Int,Double,Double,Int,Int,Int,Int,Int,Int,Int,Int,Int,Double,Double");

			try {
				Class.forName("org.relique.jdbc.csv.CsvDriver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			// Identifying the data types for the fields

			// Connect to the csv file

			Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
					+ path, props);

			// Create a statement so we can read the unique resampleIDs

			Statement stmt = conn.createStatement();
			if (partitionBy.equalsIgnoreCase("None")) {
				
				String function_op = function.isEmpty() ? yField : function
						+ "(" + fieldLookup.get(yField) + ")";

				String query2 = "SELECT " + fieldLookup.get(xField) + "," + function_op
						+ " FROM " + csvTable + " GROUP BY " + fieldLookup.get(xField);

				ResultSet qset = stmt.executeQuery(query2);

				XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();

				while (qset.next()) {

					series.getData().add(
							new XYChart.Data<Number, Number>(qset.getDouble(1),
									qset.getDouble(2)));
				}
				series.setName(fieldLookup.get(yField));
				seriesSet.add(series);
			}

			else {

				Statement stmt2 = conn.createStatement();

				String[] tokens = partitionBy.trim().split("\\s*,\\s*");
				
				String query1 = "SELECT " + fieldLookup.get(partitionBy) + " FROM " + csvTable
						+ " GROUP BY " + fieldLookup.get(partitionBy);
				
				ResultSet numset = stmt.executeQuery(query1);

				String function_op = function.isEmpty() ? yField : function
						+ "(" + fieldLookup.get(yField) + ")";

				while (numset.next()) {
					StringBuilder sb = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (int i = 0; i < tokens.length; i++) {
						sb.append(fieldLookup.get(tokens[i]) + "=" + numset.getDouble(i + 1)
								+ " AND ");
						double val = numset.getDouble(i+1);
						Number valn;
						if(val%1==0){
							valn = new Integer((int) val);
						}
						else{valn = new Double(val);};
						sb2.append(tokens[i]+" "+valn.toString() + "_");
					}

					String where = sb.toString().substring(0, sb.length() - 5);
					String query2 = "SELECT " + fieldLookup.get(xField) + "," + function_op
							+ " FROM " + csvTable + " WHERE " + where
							+ " GROUP BY " + fieldLookup.get(xField);

					ResultSet qset = stmt2.executeQuery(query2);

					XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();

					while (qset.next()) {
						series.getData().add(
								new XYChart.Data<Number, Number>(
										qset.getDouble(1), qset.getDouble(2)));
					}
					series.setName(sb2.toString().substring(0, sb2.toString().length()-1));
					seriesSet.add(series);
				}
			}
		
		return seriesSet;
	}
}
