package xspread.application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import xspread.util.ErrorHandler;
import xspread.util.JavaFXUtils;
import xspread.util.TextUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

//import javafx.scene.control.TreeItem;
//import xspread.MainGUI.Item;

public class SpreadProperties {

	private List<String> speciesList = new ArrayList<>();
	private String runType = "calibration";
	private Map<String, String> presenceFiles = new TreeMap<>();
	private Map<String, String> ageFiles = new TreeMap<>();
	private Map<String, String> habitatFiles = new TreeMap<>();
	private Map<String, String> referenceFiles = new TreeMap<>();
	private Map<String, String> managementFiles = new TreeMap<>();
	private String outputFolder = "./output";
	private String outputFile = "output.txt";
	private int replicates = 10;
	private long startTime = 0l;
	private long endTime = 10l;
	private long stepInterval = 1l;
	private Map<String, double[]> distances = new TreeMap<>();
	private Map<String, double[]> rates = new TreeMap<>();
	private Map<String, double[]> directions = new TreeMap<>();
	private Map<String, long[]> age_stage = new TreeMap<>();
	private Map<String, double[]> p_detection = new TreeMap<>();
	private List<String> coreControl = new LinkedList<>();
	private double containmentCutoff = 8;
	private double coreBufferSize = 3;
	private int managementFrequency = 1;
	private double containmentCost = 0;
	private double containmentLabour = 0;
	private String criterionField = "K_standard";
	private String errorType = "Quantity_disagreement";
	private double percentile = 0.1d;
	private double errorCutoff = 0.03d;
	private double performanceCriterion = .8d;
	private List<String> containmentIgnore = new ArrayList<>();
	private List<String> groundControlIgnore = new ArrayList<>();
	private Map<String, double[]> groundControlCost = new TreeMap<>();
	private Map<String, double[]> groundControlLabour = new TreeMap<>();
	private Map<String, Long> waitTime = new TreeMap<>();
	private boolean overwriteOutput = true;
	private boolean savePropertiesToFile = true;
	private boolean writeEachTimeStep = false;
	private boolean writeRasterHeader = true;
	private boolean printReplicates = false;
	private boolean writeEachMgtStep = false;
	private boolean writeCoverMaps = true;
	private boolean writeFrequencyMaps = true;
	private boolean writeMonitoringMaps = true;
	private boolean writeStageMaps = true;
	private boolean writeTrace = true;
	private boolean hasChangedDirectionArray = false;
	private String traceBaseName = "TraceFile";
	private final static Map<String, String> defaults = new HashMap<String, String>();
	private final static BiMap<String, String> fieldLookup = HashBiMap.create();
	@SuppressWarnings("rawtypes")
	private static TreeItem<Item> root;
	@SuppressWarnings("rawtypes")
	private TreeItem<Item> cal;

	public SpreadProperties() {
		speciesList.add("default");
		presenceFiles.put("default", "ALL");
		ageFiles.put("default", "NONE");
		habitatFiles.put("default", "ALL");
		referenceFiles.put("default", "NONE");
		managementFiles.put("default", "NONE");
		distances.put("default", new double[] { 500, 500, 500, 500, 500, 500,
				500, 500, 500, 500, 500 });
		rates.put("default", new double[] { 30, 40, 50, 60, 70, 80, 90, 100,
				110, 120, 130 });
		directions.put("default", new double[] { 1, 1, 1, 1, 1, 1, 1, 1 });
		age_stage.put("default", new long[] { 5l, 8l });
		p_detection.put("default", new double[] { 0d, 0d, 0d });
		waitTime.put("default", 1l);
		groundControlCost.put("default", new double[] { 0d, 0d, 0d });
		groundControlLabour.put("default", new double[] { 0d, 0d, 0d });

		defaults.put("species", "default");
		defaults.put("run type (paired vs. calibration)", "calibration");
		defaults.put("presence files", "ALL");
		defaults.put("age files", "NONE");
		defaults.put("habitat files", "ALL");
		defaults.put("reference files", "NONE");
		defaults.put("management files", "NONE");
		defaults.put("dispersal distances",
				"[500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0, 500.0]");
		defaults.put("dispersal rates",
				"[30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0, 110.0, 120.0, 130.0]");
		defaults.put("direction kernels",
				"[1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0]");
		defaults.put("age at stage information", "[5, 8]");
		defaults.put("probability of detection at stage", "[0.0, 0.0, 0.0]");
		defaults.put("ground control cost", "[0.0, 0.0, 0.0]");
		defaults.put("ground control labour", "[0.0, 0.0, 0.0]");
		defaults.put("wait time before spread", "1");
		defaults.put("criterion field", "K_standard");
		defaults.put("error type", "Quantity_disagreement");
		defaults.put("percentile threshold", "0.1");
		defaults.put("error cutoff", "0.05");
		defaults.put("performance criterion threshold", "0.5");

		fieldLookup.put("speciesList", "species");
		fieldLookup.put("runType", "run type (paired vs. calibration)");
		fieldLookup.put("presenceFiles", "presence files");
		fieldLookup.put("ageFiles", "age files");
		fieldLookup.put("habitatFiles", "habitat files");
		fieldLookup.put("referenceFiles", "reference files");
		fieldLookup.put("managementFiles", "management files");
		fieldLookup.put("distances", "dispersal distances");
		fieldLookup.put("rates", "dispersal rates");
		fieldLookup.put("directions", "direction kernels");
		fieldLookup.put("age_stage", "age at stage information");
		fieldLookup.put("p_detection", "probability of detection at stage");
		fieldLookup.put("groundControlCost", "ground control cost");
		fieldLookup.put("groundControlLabour", "ground control labour");
		fieldLookup.put("waitTime", "wait time before spread");
		fieldLookup.put("criterionField", "criterion field");
		fieldLookup.put("errorType", "calibration error measurement type");
		fieldLookup.put("percentile", "percentile threshold");
		fieldLookup.put("errorCutoff", "calibration cutoff value");
		fieldLookup.put("performanceCriterion",
				"calibration performance criterion");
	}

	public SpreadProperties(File file) throws IOException {
		readTextFile(file);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TreeTableView asTreeTableView() {
		root = asPrmsJFXTree();

		TreeTableColumn<Item, String> parameters = new TreeTableColumn<>(
				"Parameters");

		parameters
				.setCellValueFactory(new TreeItemPropertyValueFactory<Item, String>(
						"key"));

		parameters.setMinWidth(250);
		parameters.setPrefWidth(300);
		parameters.setMaxWidth(450);

		TreeTableColumn<Item, String> values = new TreeTableColumn<>("Values");

		values.setCellValueFactory(new TreeItemPropertyValueFactory<Item, String>(
				"value"));

		values.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());

		values.setOnEditCommit(new EventHandler<CellEditEvent<Item, String>>() {
			@Override
			public void handle(final CellEditEvent<Item, String> event) {

				final Item item = event.getRowValue().getValue();
				try {
					String key = (String) item.getKey();

					// Special case: changing the species list

					if (key.equalsIgnoreCase("species")) {
						List<String> tempList = TextUtils
								.parseStringArray(event.getNewValue());

						List<String> adds = new ArrayList(tempList);
						adds.removeAll(speciesList);

						List<String> deletes = new ArrayList(speciesList);
						deletes.removeAll(tempList);

						TreeItem<Item> root = event.getTreeTableView()
								.getRoot();

						SpreadProperties.addNodes(adds, root);
						SpreadProperties.removeNodes(deletes, root);
						SpreadProperties.this.update(root);

						speciesList = tempList;
						item.setValue(event.getNewValue());
					}

					// Handle nested entries (linked to a species)

					if (speciesList.contains(key)) {
						TreeItem<Item> treeItem = event.getTreeTablePosition()
								.getTreeItem().getParent();
						String parentName = (String) treeItem.getValue()
								.getKey();

						switch (parentName.toLowerCase()) {
						case "presence files":
							presenceFiles.put(key, event.getNewValue());
							break;
						case "age files":
							ageFiles.put(key, event.getNewValue());
							break;
						case "habitat files":
							habitatFiles.put(key, event.getNewValue());
							break;
						case "reference files":
							referenceFiles.put(key,
									event.getNewValue());
							break;
						case "management files":
							referenceFiles.put(key,
									event.getNewValue());
							break;
						case "dispersal distances":
							distances.put(key, TextUtils
									.parseNumericArray(event
											.getNewValue()));
							break;
						case "dispersal rates":
							rates.put(key, TextUtils
									.parseNumericArray(event
											.getNewValue()));
							break;
						case "direction kernels":
							directions.put(key, TextUtils
									.parseNumericArray(event
											.getNewValue()));
							break;
						case "age at stage information":
							double[] da = TextUtils
									.parseNumericArray(event
											.getNewValue());
							long[] la = new long[da.length];
							for (int i = 0; i < da.length; i++) {
								la[i] = (long) da[i];
							}
							age_stage.put(key, la);
							break;
						case "probability of detection at stage":
							p_detection.put(key, TextUtils
									.parseNumericArray(event
											.getNewValue()));
							break;
						case "ground control cost":
							groundControlCost.put(key, TextUtils
									.parseNumericArray(event
											.getNewValue()));
							break;
						case "ground control labour":
							groundControlLabour.put(key, TextUtils
									.parseNumericArray(event
											.getNewValue()));
							break;
						case "wait time before spread":
							waitTime.put(key, Long.parseLong(event
									.getNewValue()));
							break;
						}
						item.setValue(event.getNewValue());
					}

					// Otherwise it's a single entry

					else {
						switch (key.toLowerCase()) {

						case "run type (paired vs. calibration)":
							if (!(event.getNewValue().equalsIgnoreCase(
									"calibration") || event.getNewValue()
									.equalsIgnoreCase("paired"))) {
								throw new IllegalArgumentException(
										"Run type values can only be 'Calibration' or 'Paired'.");
							} else {
								runType = event.getNewValue();

								// hide or show calibration parameters

								if (runType.equalsIgnoreCase("Paired")) {
									cal = JavaFXUtils.findNode(
											"calibration parameters", root);
									cal.getParent().getChildren().remove(cal);
								}

								if (runType.equalsIgnoreCase("Calibration")) {
									if (!root.getChildren().contains(cal)) {
										root.getChildren().add(cal);
									}
								}

								item.setValue(event.getNewValue());
							}
							break;

						case "overwrite output":
							validateBoolean(event, "overwriteOutput");
							break;

						case "output directory":
							String dirName = event.getNewValue();
							File dir = new File(dirName);
							if (!dir.exists()) {
								Alert alert = new Alert(AlertType.CONFIRMATION);
								alert.setTitle("Warning");
								alert.setHeaderText("Directory "
										+ event.getNewValue()
										+ " does not exist.");
								alert.setContentText("Would you like to create the directory?");
								ButtonType buttonTypeYes = new ButtonType("Yes");
								ButtonType buttonTypeCancel = new ButtonType(
										"No", ButtonData.CANCEL_CLOSE);
								alert.getButtonTypes().setAll(buttonTypeYes,
										buttonTypeCancel);
								Optional<ButtonType> result = alert
										.showAndWait();
								if (result.get() == buttonTypeYes) {
									if (!dir.mkdir()) {
										ErrorHandler
												.showException(new IOException(
														"Directory "
																+ dirName
																+ " could not be created."));
										item.setValue(event.getOldValue());
										break;
									} else {
										item.setValue(dirName);
										outputFolder = dirName;
										break;
									}
								} else {
									item.setValue(event.getOldValue());
									break;
								}
							}

							item.setValue(dirName);
							outputFolder = dirName;
							break;

						case "output file":
							item.setValue(event.getNewValue());
							outputFile = event.getNewValue();
							break;

						case "save properties to file on run":
							validateBoolean(event, "savePropertiesToFile");
							break;

						case "write cover maps":
							validateBoolean(event, "writeCoverMaps");
							break;

						case "write stage maps":
							validateBoolean(event, "writeStageMaps");
							break;

						case "write frequency maps":
							validateBoolean(event, "writeFrequencyMaps");
							break;

						case "write monitoring maps":
							validateBoolean(event, "writeMonitoringMaps");
							break;

						case "write output for each time step":
							validateBoolean(event, "writeEachTimeStep");
							break;

						case "add header data to raster files":
							validateBoolean(event, "writeRasterHeader");
							break;

						case "write trace files":
							validateBoolean(event, "writeTrace");
							if (!Boolean.parseBoolean(event.getNewValue())) {
								Platform.runLater(() -> {
									ErrorHandler
											.showWarning(
													"Charts will be unavailable",
													"If trace files are turned off, charts cannot be generated.");
								});
							}
							break;

						case "replicates":
							replicates = Integer.parseInt(event.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "start time":
							startTime = Long.parseLong(event.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "end time":
							endTime = Long.parseLong(event.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "step interval":
							stepInterval = Long.parseLong(event.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "containment cutoff value":
							containmentCutoff = Double.parseDouble(event
									.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "core buffer size":
							coreBufferSize = Double.parseDouble(event
									.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "containment cost":
							containmentCost = Double.parseDouble(event
									.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "containment labour":
							containmentLabour = Double.parseDouble(event
									.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "containment species ignored":
							containmentIgnore = TextUtils
									.parseStringArray(event.getNewValue());
							item.setValue(event.getNewValue());
							break;
							
						case "ground control species ignored":
							groundControlIgnore = TextUtils
									.parseStringArray(event.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "criterion field":
							criterionField = event.getNewValue();
							item.setValue(event.getNewValue());
							break;

						case "calibration error measurement type":
							errorType = event.getNewValue();
							item.setValue(event.getNewValue());
							break;

						case "percentile threshold":
							percentile = Double.parseDouble(event.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "calibration cutoff value":
							errorCutoff = Double.parseDouble(event
									.getNewValue());
							item.setValue(event.getNewValue());
							break;

						case "calibration performance criterion":
							performanceCriterion = Double.parseDouble(event
									.getNewValue());
							item.setValue(event.getNewValue());
							break;
						}
					}

				} catch (IllegalArgumentException e) {
					ErrorHandler.showException(e);
					item.setValue(event.getOldValue());
				} catch (FileNotFoundException e) {
					ErrorHandler.showException(e);
					item.setValue(event.getOldValue());
				} catch (IOException e) {
					ErrorHandler.showException(e);
					item.setValue(event.getOldValue());
				} finally {
					values.setVisible(false);
					values.setVisible(true);
				}
			}
		});

		TreeTableView<Item> treeTableView = new TreeTableView<Item>(root);
		treeTableView.setShowRoot(false);
		treeTableView
				.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
		treeTableView.setEditable(true);
		treeTableView.getColumns().setAll(parameters, values);
		return treeTableView;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TreeItem<Item> asPrmsJFXTree() {

		// Root

		TreeItem<Item> root = new TreeItem<Item>(new Item<String, String>("",
				""));
		root.setExpanded(true);

		// Species

		TreeItem<Item> speciesItem = new TreeItem<Item>(new Item<>("species",
				TextUtils.list2ArrayString(speciesList)));

		TreeItem<Item> runTypeItem = new TreeItem<Item>(new Item<>(
				"run type (paired vs. calibration)", runType));
		TreeItem<Item> inputItem = new TreeItem<Item>(new Item<>("input data",
				""));

		// Input files

		TreeItem<Item> presenceFilesItem = new TreeItem<Item>(new Item<>(
				"presence files", ""));

		for (String s : speciesList) {
			presenceFilesItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, presenceFiles.get(s))));
		}

		TreeItem<Item> ageFilesItem = new TreeItem<Item>(new Item<>(
				"age files", ""));

		for (String s : speciesList) {
			ageFilesItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, ageFiles.get(s))));
		}

		TreeItem<Item> habitatFilesItem = new TreeItem<Item>(new Item<>(
				"habitat files", ""));

		for (String s : speciesList) {
			habitatFilesItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, habitatFiles.get(s))));
		}

		TreeItem<Item> referenceFilesItem = new TreeItem<Item>(new Item<>(
				"reference files", ""));

		for (String s : speciesList) {
			referenceFilesItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, referenceFiles.get(s))));
		}

		TreeItem<Item> managementFilesItem = new TreeItem<Item>(new Item<>(
				"management files", ""));

		for (String s : speciesList) {
			managementFilesItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, managementFiles.get(s))));
		}

		// Output parameters

		TreeItem<Item> outputItem = new TreeItem<Item>(new Item<>(
				"output data", ""));
		TreeItem<Item> outputPathItem = new TreeItem<Item>(new Item<>(
				"output directory", outputFolder));
		TreeItem<Item> outputFileItem = new TreeItem<Item>(new Item<>(
				"output file", outputFile));
		TreeItem<Item> runPrmsItem = new TreeItem<Item>(new Item<>(
				"run parameters", ""));
		TreeItem<Item> replicatesItem = new TreeItem<Item>(new Item<>(
				"replicates", new Integer(replicates).toString()));
		TreeItem<Item> startTimeItem = new TreeItem<Item>(new Item<>(
				"start time", new Long(startTime).toString()));
		TreeItem<Item> endTimeItem = new TreeItem<Item>(new Item<>("end time",
				new Long(endTime).toString()));
		TreeItem<Item> stepIntervalItem = new TreeItem<Item>(new Item<>(
				"step interval", new Long(stepInterval).toString()));
		TreeItem<Item> distancesItem = new TreeItem<Item>(new Item<>(
				"dispersal distances", ""));

		for (String s : speciesList) {
			distancesItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, Arrays.toString(distances
							.get(s)))));
		}

		TreeItem<Item> ratesItem = new TreeItem<Item>(new Item<>(
				"dispersal rates", ""));
		for (String s : speciesList) {
			ratesItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, Arrays.toString(rates
							.get(s)))));
		}

		TreeItem<Item> directionsItem = new TreeItem<Item>(new Item<>(
				"direction kernels", ""));
		for (String s : speciesList) {
			directionsItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, Arrays.toString(directions
							.get(s)))));
		}

		TreeItem<Item> age_stageItem = new TreeItem<Item>(new Item<>(
				"age at stage information", ""));
		for (String s : speciesList) {
			age_stageItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, Arrays.toString(age_stage
							.get(s)))));
		}

		// Management parameters

		TreeItem<Item> p_detectionItem = new TreeItem<Item>(new Item<>(
				"probability of detection at stage", ""));
		for (String s : speciesList) {
			p_detectionItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, Arrays
							.toString(p_detection.get(s)))));
		}

		TreeItem<Item> containmentCutoffItem = new TreeItem<Item>(new Item<>(
				"containment cutoff value",
				new Double(containmentCutoff).toString()));
		TreeItem<Item> coreBufferSizeItem = new TreeItem<Item>(new Item<>(
				"core buffer size", new Double(coreBufferSize).toString()));
		TreeItem<Item> managementItem = new TreeItem<Item>(new Item<>(
				"management", ""));
		TreeItem<Item> costingsItem = new TreeItem<Item>(new Item<>(
				"costing information", ""));
		TreeItem<Item> costsItem = new TreeItem<Item>(new Item<>("cost data",
				""));
		TreeItem<Item> labourItem = new TreeItem<Item>(new Item<>("labour", ""));
		TreeItem<Item> containmentCostItem = new TreeItem<Item>(new Item<>(
				"containment cost", new Double(containmentCost).toString()));
		TreeItem<Item> containmentLabourItem = new TreeItem<Item>(new Item<>(
				"containment labour", new Double(containmentLabour).toString()));
		TreeItem<Item> ignoreItem = new TreeItem<Item>(
				new Item<>("ignores", ""));
		TreeItem<Item> containmentIgnoreItem = new TreeItem<Item>(new Item<>(
				"containment species ignored",
				TextUtils.list2ArrayString(containmentIgnore)));
		TreeItem<Item> groundControlIgnoreItem = new TreeItem<Item>(new Item<>(
				"ground control species ignored",
				TextUtils.list2ArrayString(groundControlIgnore)));
		TreeItem<Item> groundControlCostItem = new TreeItem<Item>(new Item<>(
				"ground control cost", ""));

		for (String s : speciesList) {
			groundControlCostItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, Arrays
							.toString(groundControlCost.get(s)))));
		}

		TreeItem<Item> groundControlLabourItem = new TreeItem<Item>(new Item<>(
				"ground control labour", ""));

		for (String s : speciesList) {
			groundControlLabourItem.getChildren().add(
					new TreeItem<Item>(new Item<>(s, Arrays
							.toString(groundControlLabour.get(s)))));
		}

		TreeItem<Item> waitTimeItem = new TreeItem<Item>(new Item<>(
				"wait time before spread", ""));

		for (String s : speciesList) {
			waitTimeItem.getChildren().add(
					new TreeItem<Item>(
							new Item<>(s, waitTime.get(s).toString())));
		}

		TreeItem<Item> overwriteOutputItem = new TreeItem<Item>(new Item<>(
				"overwrite output", new Boolean(overwriteOutput).toString()));
		TreeItem<Item> savePropertiesToFileItem = new TreeItem<Item>(
				new Item<>("save properties to file on run", new Boolean(
						savePropertiesToFile).toString()));
		TreeItem<Item> writeCoverMapItem = new TreeItem<Item>(new Item<>(
				"write cover maps", new Boolean(writeCoverMaps).toString()));
		TreeItem<Item> writeFrequencyMapItem = new TreeItem<Item>(new Item<>(
				"write frequency maps",
				new Boolean(writeFrequencyMaps).toString()));
		TreeItem<Item> writeManagementMapItem = new TreeItem<Item>(new Item<>(
				"write monitoring maps", new Boolean(writeMonitoringMaps).toString()));
		TreeItem<Item> writeStageMapItem = new TreeItem<Item>(new Item<>(
				"write stage maps", new Boolean(writeStageMaps).toString()));
		TreeItem<Item> writeEachTimeStepItem = new TreeItem<Item>(new Item<>(
				"write output for each time step", new Boolean(
						writeEachTimeStep).toString()));
		TreeItem<Item> writeTraceFilesItem = new TreeItem<Item>(new Item<>(
				"write trace files", new Boolean(writeTrace).toString()));
		TreeItem<Item> writeRasterHeaderItem = new TreeItem<Item>(new Item<>(
				"add header data to raster files", new Boolean(
						writeRasterHeader).toString()));
		TreeItem<Item> calibrationItem = new TreeItem<Item>(new Item<>(
				"calibration parameters", ""));
		TreeItem<Item> criterionFieldItem = new TreeItem<Item>(new Item<>(
				"criterion field", criterionField));
		TreeItem<Item> errorTypeItem = new TreeItem<Item>(new Item<>(
				"calibration error measurement type", errorType));
		TreeItem<Item> percentileItem = new TreeItem<Item>(new Item<>(
				"percentile threshold", Double.toString(percentile)));
		TreeItem<Item> errorCutoffItem = new TreeItem<Item>(new Item<>(
				"calibration cutoff value", Double.toString(errorCutoff)));
		TreeItem<Item> performanceCriterionItem = new TreeItem<Item>(
				new Item<>("calibration performance criterion",
						Double.toString(performanceCriterion)));
		cal = calibrationItem;

		inputItem.getChildren().setAll(presenceFilesItem, ageFilesItem,
				habitatFilesItem, referenceFilesItem, managementFilesItem);
		outputItem.getChildren().setAll(overwriteOutputItem, outputPathItem,
				outputFileItem, savePropertiesToFileItem, writeCoverMapItem,
				writeFrequencyMapItem, writeStageMapItem, writeManagementMapItem,
				writeTraceFilesItem, writeRasterHeaderItem,writeEachTimeStepItem);
		runPrmsItem.getChildren().setAll(replicatesItem, startTimeItem,
				endTimeItem, stepIntervalItem, distancesItem, ratesItem,
				directionsItem, age_stageItem);
		costsItem.getChildren().setAll(containmentCostItem,
				groundControlCostItem);
		labourItem.getChildren().setAll(containmentLabourItem,
				groundControlLabourItem);
		costingsItem.getChildren().setAll(costsItem, labourItem);
		ignoreItem.getChildren().setAll(groundControlIgnoreItem,containmentIgnoreItem);
		managementItem.getChildren().setAll(containmentCutoffItem,
				coreBufferSizeItem, p_detectionItem, costingsItem, ignoreItem,
				waitTimeItem);
		calibrationItem.getChildren().setAll(criterionFieldItem, errorTypeItem,
				percentileItem, errorCutoffItem, performanceCriterionItem);

		root.getChildren().addAll(speciesItem, runTypeItem, inputItem,
				outputItem, runPrmsItem, managementItem);

		if (runType.equalsIgnoreCase("Calibration")) {
			root.getChildren().add(calibrationItem);
		}

		return root;
	}

	public Map<String, long[]> getAge_stage() {
		return age_stage;
	}

	public Map<String, String> getAgeFiles() {
		return ageFiles;
	}

	public double getContainmentCost() {
		return containmentCost;
	}

	public double getContainmentCutoff() {
		return containmentCutoff;
	}

	public List<String> getContainmentIgnore() {
		return containmentIgnore;
	}

	public double getContainmentLabour() {
		return containmentLabour;
	}

	public double getCoreBufferSize() {
		return coreBufferSize;
	}

	public List<String> getCoreControl() {
		return coreControl;
	}

	public Map<String, double[]> getDirections() {
		return directions;
	}

	public Map<String, double[]> getDistances() {
		return distances;
	}

	public long getEndTime() {
		return endTime;
	}

	public Map<String, double[]> getGroundControlCost() {
		return groundControlCost;
	}

	public List<String> getGroundControlIgnore() {
		return groundControlIgnore;
	}

	public Map<String, double[]> getGroundControlLabour() {
		return groundControlLabour;
	}

	public Map<String, String> getHabitatFiles() {
		return habitatFiles;
	}

	public Map<String, String> getManagementFiles() {
		return managementFiles;
	}

	public int getManagementFrequency() {
		return managementFrequency;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public Map<String, double[]> getP_detection() {
		return p_detection;
	}

	public Map<String, String> getPresenceFiles() {
		return presenceFiles;
	}

	public Map<String, double[]> getRates() {
		return rates;
	}

	public Map<String, String> getReferenceFiles() {
		return referenceFiles;
	}

	public int getReplicates() {
		return replicates;
	}

	public String getRunType() {
		return runType;
	}

	public List<String> getSpeciesList() {
		return speciesList;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getStepInterval() {
		return stepInterval;
	}

	public Map<String, Long> getWaitTime() {
		return waitTime;
	}

	/**
	 * Checks whether a String is a number
	 * 
	 * @param str
	 * @return
	 */

	public boolean isOverwritingOutput() {
		return overwriteOutput;
	}

	public boolean isPrintingReplicates() {
		return printReplicates;
	}

	public boolean isSavingPropertiesToFile() {
		return savePropertiesToFile;
	}

	public boolean isWritingEachMgtStep() {
		return writeEachMgtStep;
	}

	public boolean isWritingEachTimeStep() {
		return writeEachTimeStep;
	}

	public boolean isWritingCoverMaps() {
		return writeCoverMaps;
	}

	public boolean isWritingStageMaps() {
		return writeStageMaps;
	}

	public boolean isWritingFrequencyMap() {
		return writeFrequencyMaps;
	}

	public boolean isWritingMonitoredMaps() {
		return writeMonitoringMaps;
	}

	public boolean isWritingRasterHeader() {
		return writeRasterHeader;
	}

	public boolean isWritingTrace() {
		return writeTrace;
	}

	@SuppressWarnings("rawtypes")
	public void update(TreeItem<Item> root) {

		class PrmRunnable implements Runnable {
			SpreadProperties sp;

			PrmRunnable(SpreadProperties sp_) {
				sp = sp_;
			}

			@Override
			public void run() {

				sp.presenceFiles.clear();

				TreeItem<Item> presenceNode = JavaFXUtils.findNode(
						"presence files", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							presenceNode);
					sp.presenceFiles.put(species, (String) node.getValue()
							.getValue());
				}

				sp.ageFiles.clear();

				TreeItem<Item> ageNode = JavaFXUtils
						.findNode("age files", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils
							.findNode(species, ageNode);
					sp.ageFiles.put(species, (String) node.getValue()
							.getValue());
				}

				sp.habitatFiles.clear();

				TreeItem<Item> habitatNode = JavaFXUtils.findNode(
						"habitat files", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							habitatNode);
					sp.habitatFiles.put(species, (String) node.getValue()
							.getValue());
				}

				sp.referenceFiles.clear();

				TreeItem<Item> referenceNode = JavaFXUtils.findNode(
						"reference files", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							referenceNode);
					sp.referenceFiles.put(species, (String) node.getValue()
							.getValue());
				}

				sp.managementFiles.clear();

				TreeItem<Item> managementNode = JavaFXUtils.findNode(
						"presence files", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							managementNode);
					sp.managementFiles.put(species, (String) node.getValue()
							.getValue());
				}

				sp.distances.clear();

				TreeItem<Item> distanceNode = JavaFXUtils.findNode(
						"dispersal distances", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							distanceNode);
					try {
						sp.distances.put(species, TextUtils
								.parseNumericArray((String) node.getValue()
										.getValue()));
					} catch (IllegalArgumentException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (FileNotFoundException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (IOException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					}
				}

				sp.rates.clear();

				TreeItem<Item> rateNode = JavaFXUtils.findNode(
						"dispersal rates", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							rateNode);
					try {
						sp.rates.put(species, TextUtils
								.parseNumericArray((String) node.getValue()
										.getValue()));
					} catch (IllegalArgumentException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (FileNotFoundException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (IOException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					}
				}

				sp.directions.clear();

				TreeItem<Item> directionNode = JavaFXUtils.findNode(
						"direction kernels", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							directionNode);
					try {
						sp.directions.put(species, TextUtils
								.parseNumericArray((String) node.getValue()
										.getValue()));
					} catch (IllegalArgumentException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (FileNotFoundException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (IOException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					}
				}

				sp.age_stage.clear();

				TreeItem<Item> ageStageNode = JavaFXUtils.findNode(
						"age at stage information", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							ageStageNode);
					try {
						double[] da = TextUtils.parseNumericArray((String) node
								.getValue().getValue());
						long[] la = new long[da.length];
						for (int i = 0; i < da.length; i++) {
							la[i] = (long) da[i];
						}
						sp.age_stage.put(species, la);
					} catch (IllegalArgumentException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (FileNotFoundException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (IOException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					}
				}

				sp.p_detection.clear();

				TreeItem<Item> detectionNode = JavaFXUtils.findNode(
						"probability of detection at stage", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							detectionNode);
					try {
						sp.p_detection.put(species, TextUtils
								.parseNumericArray((String) node.getValue()
										.getValue()));
					} catch (IllegalArgumentException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (FileNotFoundException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (IOException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					}
				}

				sp.groundControlCost.clear();

				TreeItem<Item> gcCostNode = JavaFXUtils.findNode(
						"ground control cost", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							gcCostNode);
					try {
						sp.groundControlCost.put(species, TextUtils
								.parseNumericArray((String) node.getValue()
										.getValue()));
					} catch (IllegalArgumentException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (FileNotFoundException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (IOException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					}
				}

				sp.groundControlLabour.clear();

				TreeItem<Item> gcLabourNode = JavaFXUtils.findNode(
						"ground control labour", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							gcLabourNode);
					try {
						sp.groundControlLabour.put(species, TextUtils
								.parseNumericArray((String) node.getValue()
										.getValue()));
					} catch (IllegalArgumentException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (FileNotFoundException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					} catch (IOException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					}
				}

				sp.waitTime.clear();

				TreeItem<Item> waitTimeNode = JavaFXUtils.findNode(
						"wait time before spread", root);
				for (String species : sp.speciesList) {
					TreeItem<Item> node = JavaFXUtils.findNode(species,
							waitTimeNode);
					try {
						sp.waitTime.put(species, Long.parseLong((String) node
								.getValue().getValue()));
					} catch (IllegalArgumentException e) {
						// validation handled during input, otherwise we would
						// need to go back and reset the cell value.
					}
				}
			}
		}

		Platform.runLater(new PrmRunnable(this));
	}

	public void readTextFile(File file) throws IOException {

		if (!file.exists()) {
			throw new IOException("File " + file.getPath() + " was not found.");
		}

		if (!TextUtils.isTextFile(file)) {
			throw new IOException(file.getPath() + " is not a text file.");
		}

		try (FileReader fr = new FileReader(file)) {

			Properties properties = new Properties();
			properties.load(fr);

			// Setting up species

			if (!properties.containsKey("Species")) {
				throw new IllegalArgumentException(
						"Species keyword is required, but was not found in file "
								+ file.getPath() + ".");
			}

			speciesList = TextUtils.parseStringArray(properties
					.getProperty("Species"));

			Set<String> unique = new HashSet<String>(speciesList);

			if (unique.size() != speciesList.size()) {
				throw new IllegalArgumentException(
						"Species names must be unique.  Please check the list for duplicates. ("
								+ (speciesList.size() - unique.size())
								+ " found)");
			}

			runType = properties.getProperty("Run_Type", "calibration");
			if (!runType.equalsIgnoreCase("calibration")
					&& !runType.equalsIgnoreCase("paired")) {
				throw new IllegalArgumentException(
						"Run type values can only be 'Calibration' or 'Paired'.");
			}

			// Input files (presence, age, habitat, reference)

			List<String> presenceList = TextUtils.parseStringArray(properties
					.getProperty("Presence_File"));
			if (presenceList.size() != speciesList.size()) {
				throw new IOException("Presence file list size ("
						+ presenceList.size()
						+ ") must match the species list size ("
						+ speciesList.size() + ").");
			}

			presenceFiles.clear();

			for (int i = 0; i < speciesList.size(); i++) {
				presenceFiles.put(speciesList.get(i), presenceList.get(i));
			}

			List<String> ageList = TextUtils.parseStringArray(properties
					.getProperty("Age_File"));
			if (ageList.size() != speciesList.size()) {
				throw new IOException("Age file list size (" + ageList.size()
						+ ") must match the species list size ("
						+ speciesList.size() + ").");
			}

			ageFiles.clear();

			for (int i = 0; i < ageList.size(); i++) {
				ageFiles.put(speciesList.get(i), ageList.get(i));
			}

			List<String> habitatList = TextUtils.parseStringArray(properties
					.getProperty("Habitat_File"));
			if (habitatList.size() != speciesList.size()) {
				throw new IOException("Habitat file list size ("
						+ habitatList.size()
						+ ") must match the species list size ("
						+ speciesList.size() + ").");
			}

			habitatFiles.clear();

			for (int i = 0; i < habitatList.size(); i++) {
				habitatFiles.put(speciesList.get(i), habitatList.get(i));
			}

			List<String> referenceList = TextUtils.parseStringArray(properties
					.getProperty("Reference_File"));
			if (referenceList.size() != speciesList.size()) {
				throw new IOException("Reference file list size ("
						+ referenceList.size()
						+ ") must match the species list size ("
						+ speciesList.size() + ").");
			}

			referenceFiles.clear();

			for (int i = 0; i < referenceList.size(); i++) {
				referenceFiles.put(speciesList.get(i), referenceList.get(i));
			}

			// Run parameters

			replicates = Integer.parseInt(properties.getProperty("Replicates"));
			startTime = Long.parseLong(properties.getProperty("Start_Time"));
			stepInterval = Long.parseLong(properties
					.getProperty("Step_Interval"));

			if (stepInterval <= 0) {
				throw new IllegalArgumentException(
						"Step interval must be greater than 0 (" + stepInterval
								+ ")");
			}

			endTime = Long.parseLong(properties.getProperty("End_Time"));

			List<double[]> distancesList = TextUtils
					.parseMultiNumericArray(properties.getProperty("Distances"));

			if (distancesList.size() != speciesList.size()) {
				throw new IllegalArgumentException("Distance array list size ("
						+ distancesList.size()
						+ ") must match the species list size ("
						+ speciesList.size() + ").");
			}

			distances.clear();

			for (int i = 0; i < distancesList.size(); i++) {
				distances.put(speciesList.get(i), distancesList.get(i));
			}

			List<double[]> ratesList = TextUtils
					.parseMultiNumericArray(properties.getProperty("Rates"));

			if (ratesList.size() != speciesList.size()) {
				throw new IllegalArgumentException("Rate array list size ("
						+ ratesList.size()
						+ ") must match the species list size ("
						+ speciesList.size() + ").");
			}

			rates.clear();

			for (int i = 0; i < ratesList.size(); i++) {
				rates.put(speciesList.get(i), ratesList.get(i));
			}

			if (properties.containsKey("Direction_Kernel")) {

				hasChangedDirectionArray = true;

				List<double[]> directionList = TextUtils
						.parseMultiNumericArray(properties
								.getProperty("Direction_Kernel"));

				if (directionList.size() != speciesList.size()) {
					throw new IllegalArgumentException(
							"Direction array list size (" + ratesList.size()
									+ ") must match the species list size ("
									+ speciesList.size() + ").");
				}

				directions.clear();

				for (int i = 0; i < directionList.size(); i++) {
					directions.put(speciesList.get(i), directionList.get(i));
				}
			}

			else {
				hasChangedDirectionArray = false;
			}

			// Management parameters

			List<String> managementList = TextUtils.parseStringArray(properties
					.getProperty("Management_File"));
			if (managementList.size() != speciesList.size()) {
				throw new IOException("Management file list size ("
						+ managementList.size()
						+ ") must match the species list size ("
						+ speciesList.size() + ").");
			}

			managementFiles.clear();

			for (int i = 0; i < managementList.size(); i++) {
				managementFiles.put(speciesList.get(i), managementList.get(i));
			}

			coreControl = TextUtils.parseStringArray(properties
					.getProperty("Core_Control"));

			List<String> waitTimesList = TextUtils.parseStringArray(properties
					.getProperty("Wait_Time"));
			if (waitTimesList.size() != speciesList.size()) {
				throw new IOException("Wait time list size ("
						+ waitTimesList.size()
						+ ") must match the species list size ("
						+ speciesList.size() + ").");
			}

			waitTime.clear();

			for (int i = 0; i < waitTimesList.size(); i++) {
				waitTime.put(speciesList.get(i),
						Long.parseLong(waitTimesList.get(i).trim()));
			}

			containmentCutoff = Double.parseDouble(properties.getProperty(
					"Containment_Cutoff", "500000"));
			coreBufferSize = Double.parseDouble(properties.getProperty(
					"Core_Buffer_Size", "750"));
			managementFrequency = Integer.parseInt(properties.getProperty(
					"Management_Frequency", "1"));

			if (!properties.containsKey("Age_Stage")) {
				throw new IllegalArgumentException(
						"Stage at Age (Age_Stage) threshold values must be provided in the properties file.  Exiting");
			}

			List<double[]> age_stageList = TextUtils
					.parseMultiNumericArray(properties.getProperty("Age_Stage"));
			if (age_stageList.size() != speciesList.size()) {
				throw new IllegalArgumentException("Age_Stage file list size ("
						+ age_stageList.size()
						+ ") must match the species list size ("
						+ speciesList.size() + ").");
			}

			age_stage.clear();

			for (int i = 0; i < age_stageList.size(); i++) {
				double[] arr = age_stageList.get(i);
				long[] larr = new long[arr.length];
				for (int j = 0; j < arr.length; j++) {
					larr[j] = (long) arr[j];
				}
				age_stage.put(speciesList.get(i), larr);
			}

			if (properties.containsKey("Ground_Control_Ignore")) {
				setGroundControlIgnore(TextUtils.parseStringArray(properties
						.getProperty("Ground_Control_Ignore")));
			}

			if (properties.containsKey("Containment_Ignore")) {
				containmentIgnore = TextUtils.parseStringArray(properties
						.getProperty("Containment_Ignore"));
			}

			List<double[]> p_discovery = TextUtils
					.parseMultiNumericArray(properties
							.getProperty("p_Detection"));

			if (p_discovery.size() != speciesList.size()) {
				throw new IllegalArgumentException(
						"Detection probability array list size ("
								+ age_stageList.size()
								+ ") must match the species list size ("
								+ speciesList.size() + ").");
			}

			for (int i = 0; i < speciesList.size(); i++) {
				if (age_stage.get(speciesList.get(i)).length != p_discovery
						.get(i).length - 1) {
					throw new IllegalArgumentException(
							"Discovery probabilities must match the number of age thresholds plus 1.  Species "
									+ i
									+ " stage thresholds :"
									+ age_stage.get(i).length
									+ ", p_detection:"
									+ p_discovery.get(i).length);
				}
				p_detection.put(speciesList.get(i), p_discovery.get(i));
			}

			containmentCost = Double.parseDouble(properties.getProperty(
					"Containment_Cost", "0"));
			containmentLabour = Double.parseDouble(properties.getProperty(
					"Containment_Labour", "0"));

			List<double[]> gc_costs = TextUtils
					.parseMultiNumericArray(properties
							.getProperty("Ground_Control_Cost"));

			if (gc_costs.size() != speciesList.size()) {
				throw new IllegalArgumentException("Ground control cost size ("
						+ gc_costs.size()
						+ ") must match the number of species ("
						+ speciesList.size() + ").");
			}

			for (int i = 0; i < speciesList.size(); i++) {
				if (gc_costs.get(i).length != p_discovery.get(i).length) {
					throw new IllegalArgumentException(
							"Ground control cost array size for stage "
									+ i
									+ " ("
									+ gc_costs.get(i).length
									+ ") must match the array size of p_Detection for stage "
									+ i + " (" + p_discovery.get(i).length
									+ ").");
				}
				groundControlCost.put(speciesList.get(i), gc_costs.get(i));
			}

			List<double[]> gc_labour = TextUtils
					.parseMultiNumericArray(properties
							.getProperty("Ground_Control_Labour"));

			if (gc_labour.size() != speciesList.size()) {
				throw new IllegalArgumentException(
						"Ground control labour size (" + gc_labour.size()
								+ ") must match the number of species ("
								+ speciesList.size() + ").");
			}

			for (int i = 0; i < speciesList.size(); i++) {
				if (gc_labour.get(i).length != p_discovery.get(i).length) {
					throw new IllegalArgumentException(
							"Ground control labour array size for stage "
									+ i
									+ " ("
									+ gc_labour.get(i).length
									+ ") must match the array size of p_Detection for stage "
									+ i + " (" + p_discovery.get(i).length
									+ ").");
				}
				groundControlLabour.put(speciesList.get(i), gc_labour.get(i));
			}

			// Output parameters

			if (!properties.containsKey("Output_Folder")) {
				throw new IOException(
						"Missing output folder property in parameter file.  Ensure the file contains an Output_Folder entry.");
			}

			outputFolder = properties.getProperty("Output_Folder");

			if (!properties.containsKey("Output_File")) {
				throw new IOException(
						"Missing output file property in parameter file.  Ensure the file contains an Output_File entry.");
			}

			outputFile = properties.getProperty("Output_File");

			if (properties.containsKey("Overwrite_Output")) {
				overwriteOutput = Boolean.parseBoolean(properties
						.getProperty("Overwrite_Output"));
			}

			if (properties.containsKey("Write_Raster_Header")) {
				writeRasterHeader = Boolean.parseBoolean(properties
						.getProperty("Write_Raster_Header"));
			}

			if (properties.containsKey("Save_Properties_File")) {
				savePropertiesToFile = Boolean.parseBoolean(properties
						.getProperty("Save_Properties_File"));

				// If we explicitly say to not save the properties file, but one
				// exists in the same location, then delete it to avoid
				// confusion.
				if (!savePropertiesToFile) {
					File f = new File(outputFolder + "/" + "properties.txt");
					if (f.exists()) {
						f.delete();
					}
				}
			}

			if (!properties.containsKey("Output_Folder")) {
				throw new IOException(
						"Missing output folder property in parameter file.  Ensure the file contains an Output_Folder entry.");
			}

			if (properties.containsKey("Overwrite_Output")) {
				overwriteOutput = Boolean.parseBoolean(properties
						.getProperty("Overwrite_Output"));
			}

			if (properties.containsKey("Write_Raster_Header")) {
				writeRasterHeader = Boolean.parseBoolean(properties
						.getProperty("Write_Raster_Header"));
			}

			if (properties.containsKey("Save_Properties_File")) {
				savePropertiesToFile = Boolean.parseBoolean(properties
						.getProperty("Save_Properties_File"));

				// If we explicitly say to not save the properties file, but one
				// exists in the same location, then delete it to avoid
				// confusion.
				if (!savePropertiesToFile) {
					File f = new File(outputFolder + "/" + "properties.txt");
					if (f.exists()) {
						f.delete();
					}
				}
			}

			printReplicates = properties.containsKey("Print_Replicates") ? Boolean
					.parseBoolean(properties.getProperty("Print_Replicates"))
					: false;

			writeEachTimeStep = properties.containsKey("Write_Each_Time_Step") ? Boolean
					.parseBoolean(properties
							.getProperty("Write_Each_Time_Step")) : false;

			writeEachMgtStep = properties.containsKey("Write_Each_Mgt_Step") ? Boolean
					.parseBoolean(properties.getProperty("Write_Each_Mgt_Step"))
					: false;

			writeTrace = Boolean.parseBoolean(properties.getProperty(
					"Write_Trace_Files", "true"));

			writeCoverMaps = properties.containsKey("Write_Cover_Maps") ? Boolean
					.parseBoolean(properties.getProperty("Write_Cover_Maps"))
					: true;

			writeFrequencyMaps = properties.containsKey("Write_Frequency_Maps") ? Boolean
					.parseBoolean(properties.getProperty("Write_Frequency_Maps"))
					: true;

			writeStageMaps = properties.containsKey("Write_Stage_Maps") ? Boolean
					.parseBoolean(properties.getProperty("Write_Stage_Maps"))
					: true;

			writeMonitoringMaps = properties.containsKey("Write_Monitoring_Maps") ? Boolean
					.parseBoolean(properties
							.getProperty("Write_Monitoring_Maps")) : true;

			criterionField = properties.getProperty("Criterion_Field",
					"K_standard");
			errorType = properties.getProperty("Calibration_Error_Type",
					"Quantity_disagreement");
			percentile = properties.containsKey("Calibration_Percentile") ? Double
					.parseDouble(properties
							.getProperty("Calibration_Percentile")) : 0.1d;
			errorCutoff = properties.containsKey("Calibration_Error_Cutoff") ? Double
					.parseDouble(properties
							.getProperty("Calibration_Error_Cutoff")) : 0.03d;
			performanceCriterion = properties
					.containsKey("Calibration_Peformance_Criterion") ? Double
					.parseDouble(properties
							.getProperty("Calibration_Performance_Criterion"))
					: 0.8d;
		}
	}

	public void setAge_stage(Map<String, long[]> age_stage) {
		this.age_stage = age_stage;
	}

	public void setAgeFiles(Map<String, String> ageFiles) {
		this.ageFiles = ageFiles;
	}

	public void setContainmentCost(double containmentCost) {
		this.containmentCost = containmentCost;
	}

	public void setContainmentCutoff(double containmentCutoff) {
		this.containmentCutoff = containmentCutoff;
	}

	public void setContainmentIgnore(List<String> containmentIgnore) {
		this.containmentIgnore = containmentIgnore;
	}

	public void setContainmentLabour(double containmentLabour) {
		this.containmentLabour = containmentLabour;
	}

	public void setCoreBufferSize(double coreBufferSize) {
		this.coreBufferSize = coreBufferSize;
	}

	public void setCoreControl(List<String> coreControl) {
		this.coreControl = coreControl;
	}

	public void setDirections(Map<String, double[]> directions) {
		this.directions = directions;
	}

	public void setDistances(Map<String, double[]> distances) {
		this.distances = distances;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setGroundControlCost(Map<String, double[]> groundControlCost) {
		this.groundControlCost = groundControlCost;
	}

	public void setGroundControlIgnore(List<String> groundControlIgnore) {
		this.groundControlIgnore = groundControlIgnore;
	}

	public void setGroundControlLabour(Map<String, double[]> groundControlLabour) {
		this.groundControlLabour = groundControlLabour;
	}

	public void setHabitatFiles(Map<String, String> habitatFiles) {
		this.habitatFiles = habitatFiles;
	}

	public void setManagementFiles(Map<String, String> managementFiles) {
		this.managementFiles = managementFiles;
	}

	public void setManagementFrequency(int managementFrequency) {
		this.managementFrequency = managementFrequency;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void setOverwriteOutput(boolean overwriteOutput) {
		this.overwriteOutput = overwriteOutput;
	}

	public void setP_detection(Map<String, double[]> p_detection) {
		this.p_detection = p_detection;
	}

	public void setPresenceFiles(Map<String, String> presenceFiles) {
		this.presenceFiles = presenceFiles;
	}

	public void setPrintReplicates(boolean printReplicates) {
		this.printReplicates = printReplicates;
	}

	public void setRates(Map<String, double[]> rates) {
		this.rates = rates;
	}

	public void setReferenceFiles(Map<String, String> referenceFiles) {
		this.referenceFiles = referenceFiles;
	}

	public void setReplicates(int replicates) {
		this.replicates = replicates;
	}

	public void setRunType(String runType) {
		this.runType = runType;
	}

	public void setSavePropertiesToFile(boolean savePropertiesToFile) {
		this.savePropertiesToFile = savePropertiesToFile;
	}

	public void setSpeciesList(List<String> speciesList) {
		this.speciesList = speciesList;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setStepInterval(long stepInterval) {
		this.stepInterval = stepInterval;
	}

	public void setWaitTime(Map<String, Long> waitTime) {
		this.waitTime = waitTime;
	}

	public void setWriteEachMgtStep(boolean writeEachMgtStep) {
		this.writeEachMgtStep = writeEachMgtStep;
	}

	public void setWriteEachTimeStep(boolean writeEachTimeStep) {
		this.writeEachTimeStep = writeEachTimeStep;
	}

	public void setWriteFrequencyMap(boolean writeFrequencyMap) {
		this.writeFrequencyMaps = writeFrequencyMap;
	}

	public void setWriteRasterHeader(boolean writeRasterHeader) {
		this.writeRasterHeader = writeRasterHeader;
	}

	public void setWriteTrace(boolean writeTrace) {
		this.writeTrace = writeTrace;
	}

	public void writeTextFile(File file) throws IOException {

		try (FileWriter fw = new FileWriter(file)) {

			fw.write(TextUtils.padRight("Species", 22)
					+ TextUtils.list2ArrayString(speciesList) + "\n");
			fw.write(TextUtils.padRight("Presence_File", 22)
					+ TextUtils.list2ArrayString(shear(presenceFiles)) + "\n");
			fw.write(TextUtils.padRight("Age_File", 22)
					+ TextUtils.list2ArrayString(shear(ageFiles)) + "\n");
			fw.write(TextUtils.padRight("Habitat_File", 22)
					+ TextUtils.list2ArrayString(shear(habitatFiles)) + "\n");
			fw.write(TextUtils.padRight("Reference_File", 22)
					+ TextUtils.list2ArrayString(shear(referenceFiles)) + "\n");
			fw.write(TextUtils.padRight("Management_File", 22)
					+ TextUtils.list2ArrayString(shear(managementFiles)) + "\n");
			fw.write(TextUtils.padRight("Output_Folder", 22) + outputFolder
					+ "\n");
			fw.write(TextUtils.padRight("Output_File", 22) + outputFile + "\n");
			fw.write(TextUtils.padRight("Replicates", 22) + replicates + "\n");
			fw.write(TextUtils.padRight("Start_Time", 22) + startTime + "\n");
			fw.write(TextUtils.padRight("End_Time", 22) + endTime + "\n");
			fw.write(TextUtils.padRight("Step_Interval", 22) + stepInterval
					+ "\n");
			fw.write(TextUtils.padRight("Distances", 22)
					+ TextUtils.list2MultiArrayString(shear(distances)) + "\n");
			fw.write(TextUtils.padRight("Rates", 22)
					+ TextUtils.list2MultiArrayString(shear(rates)) + "\n");
			fw.write(TextUtils.padRight("Direction_Kernel", 22)
					+ TextUtils.list2MultiArrayString(shear(directions)) + "\n");
			fw.write(TextUtils.padRight("Age_Stage", 22)
					+ TextUtils.list2MultiArrayString(shear(age_stage)) + "\n");
			fw.write(TextUtils.padRight("p_Detection", 22)
					+ TextUtils.list2MultiArrayString(shear(p_detection))
					+ "\n");
			fw.write(TextUtils.padRight("Containment_Cutoff", 22)
					+ containmentCutoff + "\n");
			fw.write(TextUtils.padRight("Core_Buffer_Size", 22)
					+ coreBufferSize + "\n");
			fw.write(TextUtils.padRight("Containment_Cost", 22)
					+ containmentCost + "\n");
			fw.write(TextUtils.padRight("Containment_Labour", 22)
					+ containmentLabour + "\n");
			fw.write(TextUtils.padRight("Containment_Ignore", 22)
					+ containmentIgnore + "\n");
			fw.write(TextUtils.padRight("Ground_Control_Cost", 22)
					+ TextUtils.list2MultiArrayString(shear(groundControlCost))
					+ "\n");
			fw.write(TextUtils.padRight("Ground_Control_Labour", 22)
					+ TextUtils
							.list2MultiArrayString(shear(groundControlLabour))
					+ "\n");
			fw.write(TextUtils.padRight("Wait_Time", 22)
					+ TextUtils.list2ArrayString(shear(waitTime)) + "\n");
			fw.write(TextUtils.padRight("Run_Type", 22) + runType + "\n");
			fw.write(TextUtils.padRight("Overwrite_Output", 22)
					+ overwriteOutput + "\n");
			fw.write(TextUtils.padRight("Save_Properties_File", 22)
					+ savePropertiesToFile + "\n");
			fw.write(TextUtils.padRight("Write_Cover_Maps", 22) + writeCoverMaps
					+ "\n");
			fw.write(TextUtils.padRight("Write_Frequency_Maps", 22)
					+ writeFrequencyMaps + "\n");
			fw.write(TextUtils.padRight("Write_Monitoring_Maps", 22)
					+ writeMonitoringMaps + "\n");
			fw.write(TextUtils.padRight("Write_Stage_Maps", 22)
					+ writeStageMaps + "\n");
			fw.write(TextUtils.padRight("Write_Each_Time_Step", 22)
					+ writeEachTimeStep + "\n");
			fw.write(TextUtils.padRight("Write_Trace_Files", 22)
					+ writeTrace + "\n");
			fw.write(TextUtils.padRight("Write_Raster_Header", 22)
					+ writeRasterHeader + "\n");
			fw.close();
		}
	}

	// Shearing the map extracts the values as an array, while guaranteeing that
	// they are returned in the
	// order of the speciesList

	private List<String> shear(Map<?, ?> map) {
		List<String> list = new ArrayList<>();
		if (map.isEmpty()) {
			return list;
		}
		for (String species : speciesList) {

			Object o = map.get(species);
			if (o.getClass().isArray()) {
				if (o.getClass().getName().equals("[Ljava.lang.String")) {
					String[] so = (String[]) o;
					if (so.length == 0) {
						list.add("");
						continue;
					}
					if (so.length == 1) {
						list.add(so[0]);
						continue;
					}
					list.add(Arrays.toString(so));
					continue;
				}

				if (o.getClass().getName().equals("[D")) {
					double[] dbo = (double[]) o;
					if (dbo.length == 0) {
						list.add("");
						continue;
					}
					if (dbo.length == 1) {
						list.add(new Double(dbo[0]).toString());
						continue;
					}
					list.add(Arrays.toString(dbo));
					continue;
				}

				if (o.getClass().getName().equals("[F")) {
					float[] fo = (float[]) o;
					if (fo.length == 0) {
						list.add("");
						continue;
					}
					if (fo.length == 1) {
						list.add(new Float(fo[0]).toString());
						continue;
					}
					list.add(Arrays.toString(fo));
					continue;
				}

				if (o.getClass().getName().equals("[J")) {
					long[] lo = (long[]) o;
					if (lo.length == 0) {
						list.add("");
						continue;
					}
					if (lo.length == 1) {
						list.add(new Long(lo[0]).toString());
						continue;
					}
					list.add(Arrays.toString(lo));
					continue;
				}

				if (o.getClass().getName().equals("[I")) {
					int[] io = (int[]) o;
					if (io.length == 0) {
						list.add("");
						continue;
					}
					if (io.length == 1) {
						list.add(new Integer(io[0]).toString());
						continue;
					}
					list.add(Arrays.toString(io));
					continue;
				}

				if (o.getClass().getName().equals("[Z")) {
					boolean[] zo = (boolean[]) o;
					if (zo.length == 0) {
						list.add("");
						continue;
					}
					if (zo.length == 1) {
						list.add(new Boolean(zo[0]).toString());
						continue;
					}
					list.add(Arrays.toString(zo));
					continue;
				}

				if (o.getClass().getName().equals("[S")) {
					short[] sho = (short[]) o;
					if (sho.length == 0) {
						list.add("");
						continue;
					}
					if (sho.length == 1) {
						list.add(new Short(sho[0]).toString());
						continue;
					}
					list.add(Arrays.toString(sho));
					continue;
				}

				if (o.getClass().getName().equals("[C")) {
					char[] cho = (char[]) o;
					if (cho.length == 0) {
						list.add("");
						continue;
					}
					if (cho.length == 1) {
						list.add(new Character(cho[0]).toString());
						continue;
					}
					list.add(Arrays.toString(cho));
					continue;
				}

				if (o.getClass().getName().equals("[B")) {
					byte[] bo = (byte[]) o;
					if (bo.length == 0) {
						list.add("");
						continue;
					}
					if (bo.length == 1) {
						list.add(new Byte(bo[0]).toString());
						continue;
					}
					list.add(Arrays.toString(bo));
					continue;
				}

				Object[] oa = (Object[]) o;
				if (oa.length == 0) {
					list.add("");
					continue;
				}
				if (oa.length == 1) {
					list.add(oa[0].toString());
					continue;
				}
				list.add(Arrays.toString(oa));
			} else {
				list.add(map.get(species).toString());
			}
		}
		return list;
	}

	public String getTraceBaseName() {
		return traceBaseName;
	}

	public void setTraceBaseName(String traceBaseName) {
		this.traceBaseName = traceBaseName;
	}

	public boolean hasChangedDirectionArray() {
		return hasChangedDirectionArray;
	}

	public void setHasChangedDirectionArray(boolean hasChangedDirectionArray) {
		this.hasChangedDirectionArray = hasChangedDirectionArray;
	}

	@SuppressWarnings("rawtypes")
	private static void removeNodes(List<String> species, TreeItem<Item> root) {
		if (species.size() == 0) {
			return;
		}
		for (TreeItem<Item> child : root.getChildren()) {
			if (species.contains(child.getValue().getKey())) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						root.getChildren().remove(child);
					}
				});
			} else {
				removeNodes(species, child);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private static void addNodes(List<String> species, TreeItem<Item> root) {
		if (species.size() == 0) {
			return;
		}

		for (TreeItem<Item> child : root.getChildren()) {
			if (defaults.keySet().contains(child.getValue().getKey())) {
				for (String sp : species) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							child.getChildren().add(
									new TreeItem<Item>(new Item<>(sp, defaults
											.get(child.getValue().getKey()))));
						}
					});
				}
			} else {
				addNodes(species, child);
			}
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void validateBoolean(CellEditEvent<Item, String> event,
			String variable) {
		final Item item = event.getRowValue().getValue();
		if (!(event.getNewValue().equalsIgnoreCase("true") || event
				.getNewValue().equalsIgnoreCase("false"))) {
			ErrorHandler.showException(new IllegalArgumentException(
					"Overwrite output values can only be 'True' or 'False'."));
			item.setValue(event.getOldValue());

		} else {
			try {
				Field field = this.getClass().getDeclaredField(variable);
				field.setBoolean(this,
						Boolean.parseBoolean(event.getNewValue()));
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			item.setValue(event.getNewValue());
		}

	}

	@SuppressWarnings("rawtypes")
	public TreeItem<Item> getRoot() {
		return root;
	}

	public static String in2out(String term) {
		return fieldLookup.get(term);
	}

	public static String out2in(String term) {
		return fieldLookup.inverse().get(term);
	}

	public String getCriterionField() {
		return criterionField;
	}

	public void setCriterionField(String criterionField) {
		this.criterionField = criterionField;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public double getPercentile() {
		return percentile;
	}

	public void setPercentile(double percentile) {
		this.percentile = percentile;
	}

	public double getErrorCutoff() {
		return errorCutoff;
	}

	public void setErrorCutoff(double errorCutoff) {
		this.errorCutoff = errorCutoff;
	}

	public double getPerformanceCriterion() {
		return performanceCriterion;
	}

	public void setPerformanceCriterion(double performanceCriterion) {
		this.performanceCriterion = performanceCriterion;
	}
}
