/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package xspread.postprocess;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javafx.application.Platform;
import xspread.util.ErrorHandler;

/**
 * Generates a calibration analysis file comparing simulated output to reference
 * values.
 */

public class CalibrationAnalysis {

	private String outputFolder = "C:/Temp/Spread_Output";
	// private String table = "stats";
	private String table = "output.txt";
	private String resampleField = "Replicate";
	private String criterionField = "K_standard";
	private String errorType = "Quantity_disagreement";
	private String spreadRateField = "Rate";
	private String spreadDistanceField = "Distance";
	private double percentile = 0.1d;
	//private double errorCutoff = 0.05d;
	private double errorCutoff = 0.03d;
	//private double performanceCriterion = .8d;
	private double performanceCriterion = .5d;
	private String outputFile = "calibration.csv";
	private List<String> speciesList;
	private Map<String, Map<Double, Map<Double, Number[]>>> calVals = new TreeMap<String, Map<Double, Map<Double, Number[]>>>();
	private Map<String, Map<Double, Map<Double, Number[]>>> selected = new TreeMap<String, Map<Double, Map<Double, Number[]>>>();

	public static void main(String[] args) {
		CalibrationAnalysis ca = new CalibrationAnalysis();
		ca.outputFolder = "C:/TempX";
		ca.outputFile = "output.csv";
		ca.speciesList = new ArrayList<String>();
		ca.speciesList.add("demo");
		ca.go();
	}

	public void go() {

		calVals.clear();
		selected.clear();

		System.out.println("\nRunning calibration analysis...\n");

		Iterator<String> it = speciesList.iterator();
		while (it.hasNext()) {

			String species = it.next();

			String trimtable = species + "_" + removeExtension(table);

			try {

				Properties props = new Properties();
				props.put(
						"columnTypes",
						"Int,Double,Double,Int,Int,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double,Double");

				Class.forName("org.relique.jdbc.csv.CsvDriver");

				// Identifying the data types for the fields

				// Connect to the csv file

				Connection conn = DriverManager.getConnection(
						"jdbc:relique:csv:" + outputFolder, props);

				// Create a statement so we can read the unique resampleIDs

				Statement stmt = conn.createStatement();

				ResultSet numset = stmt.executeQuery("SELECT COUNT(*) FROM "
						+ trimtable);
				numset.next();
				int nrows = numset.getInt(1);
				numset.close();

				int trimrows = (int) (nrows * percentile);

				ResultSet repset = stmt.executeQuery("SELECT DISTINCT "
						+ resampleField + " FROM " + trimtable);
				int repct = 0;
				while (repset.next()) {
					repct++;
				}
				repset.close();

				// Ideally, here we'd retrieve a nested table, and run a GROUP
				// BY on it, but csvjdbc doesn't support nested/temporary
				// tables.
				// One option might be to write an in-memory adaptor to treat a
				// ResultSet like a table, but the easier thing for now is to
				// just iterate over the table and use Maps to do the grouping.

				String sql = "SELECT " + spreadRateField + ","
						+ spreadDistanceField + "," + criterionField + ", " + errorType + " FROM "
						+ trimtable + " ORDER BY " + criterionField
						+ " DESC LIMIT " + trimrows;

				ResultSet resampleSet = stmt.executeQuery(sql);

				Map<Double, Map<Double, Number[]>> map = process(resampleSet,
						species, repct);

				writeToFile(map, outputFolder + "/" + species + "_"
						+ removeExtension(outputFile) + ".csv", species);

				// Close resources and notify that replicate is complete.

				resampleSet.close();

				stmt.close();
				conn.close();

				// Basic exception handling

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Processes a single result set.
	 * 
	 * @param resampleSet
	 *            - a set of query results
	 * @param repct
	 *            - the number of replicates
	 * @return
	 * @throws SQLException
	 */

	private Map<Double, Map<Double, Number[]>> process(ResultSet resampleSet,
			String species, int repct) throws SQLException {

		Map<Double, Map<Double, Number[]>> map = new TreeMap<Double, Map<Double, Number[]>>();

		while (resampleSet.next()) {

			double err = resampleSet.getDouble(errorType);
			if (err >= errorCutoff) {
				continue;
			}

			double spread = resampleSet.getDouble(spreadRateField);
			double distance = resampleSet.getDouble(spreadDistanceField);
			double value = resampleSet.getDouble(criterionField);

			if (!map.containsKey(spread)) {
				Map<Double, Number[]> insert = new TreeMap<Double, Number[]>();
				insert.put(distance, new Number[]{1,value});
				map.put(spread, insert);
			} else {
				Map<Double, Number[]> retrieve = map.get(spread);
				if (!retrieve.containsKey(distance)) {
					retrieve.put(distance, new Number[]{1,value});
				} else {
					Number[] na = retrieve.get(distance);
					na[0] = na[0].intValue()+1;
					double oa = na[1].doubleValue();
					double nv = value;
					double oc = na[0].doubleValue()-1;
					double nc = oc+1;
					na[1] = oa*(oc/nc)+nv/nc;
				}
			}
		}

		calVals.put(species, deepCopy(map));

		// Trim map down using requiredReplicates

		int requiredReplicates = (int) (performanceCriterion * repct);

		// First iterate over resampleIDs

		Iterator<Double> it = map.keySet().iterator();
		while (it.hasNext()) {
			Double dbl = it.next();

			// Then iterate over the pair values

			Map<Double, Number[]> submap = map.get(dbl);
			Iterator<Double> it2 = submap.keySet().iterator();
			while (it2.hasNext()) {
				Double val = it2.next();
				if (submap.get(val)[0].intValue() < requiredReplicates) {
					it2.remove();
				}
			}
		}

		// Eliminate empty tables

		it = map.keySet().iterator();

		while (it.hasNext()) {
			Double dbl = it.next();
			if (map.get(dbl).isEmpty()) {
				it.remove();
			}
		}
		selected.put(species, deepCopy(map));
		return map;
	}

	/**
	 * Writes a two field map to csv
	 * 
	 * @param map
	 *            - The Map containing the data values to be written.
	 * @param outputFile
	 *            - String containing the path where the output file should be
	 *            written
	 */

	private void writeToFile(Map<Double, Map<Double, Number[]>> map,
			String outputFile, String species) {

		if (map.size() == 0) {
			System.out
					.println("WARNING:  No Experiments match the required calibration criteria for species "
							+ species + ".  Calibration file was not written.");
			
			try {
				Platform.runLater(() ->{ErrorHandler.showWarning("Empty calibration file", "No Experiments match the required calibration criteria for species "
						+ species + ".  Calibration file was not written.");});
			} catch (Exception e) {
			} finally{
				File f = new File(outputFile);
				Platform.runLater(() ->{f.delete();});
			}
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {

			// Do we want to add a header?
			Iterator<Double> it1 = map.keySet().iterator();
			while (it1.hasNext()) {
				double spreadRate = it1.next();
				Map<Double, Number[]> retrieve = map.get(spreadRate);
				Iterator<Double> it2 = retrieve.keySet().iterator();
				while (it2.hasNext()) {
					double spreadDistance = it2.next();
					bw.write(spreadRate + "," + spreadDistance + ","
							+ retrieve.get(spreadDistance)[0] + "," + retrieve.get(spreadDistance)[1] + "\n");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Getters and setters

	public String getCriterionField() {
		return criterionField;
	}

	public double getErrorCutoff() {
		return errorCutoff;
	}

	public String getErrorType() {
		return errorType;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public double getPercentile() {
		return percentile;
	}

	public double getPerformanceCriterion() {
		return performanceCriterion;
	}

	public String getResampleField() {
		return resampleField;
	}

	public String getSpreadDistanceField() {
		return spreadDistanceField;
	}

	public String getSpreadRateField() {
		return spreadRateField;
	}

	public String getTable() {
		return table;
	}

	public void setCriterionField(String criterionField) {
		this.criterionField = criterionField;
	}

	public void setErrorCutoff(double errorCutoff) {
		this.errorCutoff = errorCutoff;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void setPercentile(double percentile) {
		this.percentile = percentile;
	}

	public void setPerformanceCriterion(double performanceCriterion) {
		this.performanceCriterion = performanceCriterion;
	}

	public void setResampleField(String resampleField) {
		this.resampleField = resampleField;
	}

	public void setSpeciesList(List<String> speciesList) {
		this.speciesList = speciesList;
	}

	public void setSpreadDistanceField(String spreadDistanceField) {
		this.spreadDistanceField = spreadDistanceField;
	}

	public void setSpreadRateField(String spreadRateField) {
		this.spreadRateField = spreadRateField;
	}

	public void setStatsTable(String table) {
		this.table = table;
	}

	/**
	 * Removes the file extension from a String
	 * 
	 * @param s
	 * @return
	 */

	private static String removeExtension(String s) {

		String separator = System.getProperty("file.separator");
		String filename;

		// Remove the path up to the filename.
		int lastSeparatorIndex = s.lastIndexOf(separator);
		if (lastSeparatorIndex == -1) {
			filename = s;
		} else {
			filename = s.substring(lastSeparatorIndex + 1);
		}

		// Remove the extension.
		int extensionIndex = filename.lastIndexOf(".");
		if (extensionIndex == -1)
			return filename;

		return filename.substring(0, extensionIndex);
	}

	public Map<String, Map<Double, Map<Double, Number[]>>> getSelected() {
		return selected;
	}

	public Map<String, Map<Double, Map<Double, Number[]>>> getCalVals() {
		return calVals;
	}

	private Map<Double, Map<Double, Number[]>> deepCopy(
			Map<Double, Map<Double, Number[]>> original) {
		Map<Double, Map<Double, Number[]>> copy = new TreeMap<Double, Map<Double, Number[]>>();
		Iterator<Double> oi = original.keySet().iterator();
		while (oi.hasNext()) {
			Map<Double, Number[]> distmap_copy = new TreeMap<Double, Number[]>();
			double rate = oi.next();
			Map<Double, Number[]> distmap = original.get(rate);
			Iterator<Double> dmi = distmap.keySet().iterator();
			while (dmi.hasNext()) {
				double dist = dmi.next();
				int val = distmap.get(dist)[0].intValue();
				double avg = distmap.get(dist)[1].doubleValue();
				distmap_copy.put(dist, new Number[]{val,avg});
			}
			copy.put(rate, distmap_copy);
		}
		return copy;
	}
	
	public int getNSelected(){
		return selected.size();
	}

}