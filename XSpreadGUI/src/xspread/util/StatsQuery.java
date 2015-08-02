package xspread.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class StatsQuery extends Application {

	private String outputFolder = "C:/TempX";
	String function = "AVG";
	String xField = "Rate";
	String yField = "P_infested";
	String table = "demo_TraceFile";

	// String partitionBy = "Rate, Replicate";
	String partitionBy = "Time";

	public void start(Stage stage) throws IOException {

		final NumberAxis xAxis = new NumberAxis();
		final NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel(xField);
		yAxis.setLabel(yField);

		LineChart lineChart = new LineChart<Number, Number>(xAxis, yAxis);
		lineChart.setTitle("Test");
		lineChart.setAnimated(false);
		
		
		

		List<XYChart.Series> ss = new ArrayList<XYChart.Series>();
		
		try {
			ss = getSeriesSet();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(ss.size()>20){
			lineChart.setLegendVisible(false);
		}
		
		lineChart.getData().addAll(ss);
		
		Group root = new Group();
		Scene scene = new Scene(root);
		// scene.setFill(Color.BLACK);

		HBox box = new HBox();
		box.getChildren().add(lineChart);

		root.getChildren().add(box);

		stage.setTitle("ImageView");
		stage.setWidth(500);
		stage.setHeight(500);
		stage.setScene(scene);
		stage.sizeToScene();
		stage.show();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	public List<XYChart.Series> getSeriesSet() throws SQLException {
		
		List<XYChart.Series> seriesSet = new ArrayList<XYChart.Series>();
		Properties props = new Properties();
		props.put("columnTypes",
				"Int,Int,Double,Double,Int,Int,Int,Int,Int,Int,Int,Int,Int,Int,Int,Int");

			try {
				Class.forName("org.relique.jdbc.csv.CsvDriver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			// Identifying the data types for the fields

			// Connect to the csv file

			Connection conn = DriverManager.getConnection("jdbc:relique:csv:"
					+ outputFolder, props);

			// Create a statement so we can read the unique resampleIDs

			Statement stmt = conn.createStatement();
			if (partitionBy.isEmpty()) {

				int ct = 0;
				String function_op = function.isEmpty() ? yField : function
						+ "(" + yField + ")";

				String query2 = "SELECT " + xField + "," + function_op
						+ " FROM " + table + "_Copy GROUP BY " + xField;

				ResultSet qset = stmt.executeQuery(query2);

				XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();

				while (qset.next()) {

					series.getData().add(
							new XYChart.Data<Number, Number>(qset.getDouble(1),
									qset.getDouble(2)));
				}
				ct++;
				seriesSet.add(series);
			}

			else {

				Statement stmt2 = conn.createStatement();

				String[] tokens = partitionBy.trim().split("\\s*,\\s*");
				String query1 = "SELECT " + partitionBy + " FROM " + table
						+ " GROUP BY " + partitionBy;
				ResultSet numset = stmt.executeQuery(query1);

				int ct = 0;

				String function_op = function.isEmpty() ? yField : function
						+ "(" + yField + ")";

				while (numset.next()) {
					StringBuilder sb = new StringBuilder();
					StringBuilder sb2 = new StringBuilder();
					for (int i = 0; i < tokens.length; i++) {
						sb.append(tokens[i] + "=" + numset.getDouble(i + 1)
								+ " AND ");
						double val = numset.getDouble(i+1);
						Number valn;
						if(val%1==0){
							valn = new Integer((int) val);
						}
						else{valn = new Double(val);};
						sb2.append(valn.toString() + "_");
					}

					String where = sb.toString().substring(0, sb.length() - 5);
					String query2 = "SELECT " + xField + "," + function_op
							+ " FROM " + table + " WHERE " + where
							+ " GROUP BY " + xField;

					ResultSet qset = stmt2.executeQuery(query2);

					XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();

					while (qset.next()) {
						series.getData().add(
								new XYChart.Data<Number, Number>(
										qset.getDouble(1), qset.getDouble(2)));
					}
					series.setName(sb2.toString().substring(0, sb2.toString().length()-1));
					ct++;
					seriesSet.add(series);
				}
			}
		
		return seriesSet;

	}
}
