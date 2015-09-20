package xspread.util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import xspread.MainGUI;
import xspread.application.Item;
import xspread.application.SpreadProperties;

public class CalibrationUI {

	private int ncols = 0;// cols
	private int nrows = 0;// rows
	private int rheight = 50;
	private int rwidth = 50;
	private TextField distanceField = new TextField();
	private TextField rateField = new TextField();
	private TextField countField = new TextField();
	private TextField valueField = new TextField();
	private SplitPane root;
	private Set<Tile> tileSet = new TreeSet<Tile>();
	private Set<Tile> calibratedTileSet = new TreeSet<Tile>();
	private String species = "";
	private SpreadProperties spref;
	private ComboBox<String> speciesList = new ComboBox<String>();
	private MainGUI parent;
	private boolean selecting;
	private Map<String, Map<Double, Map<Double, Number[]>>> calvals;
	private Map<String, Map<Double, Map<Double, Number[]>>> selvals;
	private TableView<TableRow> table = new TableView<TableRow>();
	private NumberFormat nf = new DecimalFormat("0.##");
	private NumberFormat nf2 = new DecimalFormat("0.######");
	ObservableList<TableRow> data = FXCollections.observableArrayList();

	public void clearFields(){
		distanceField.clear();
		rateField.clear();
		countField.clear();
		valueField.clear();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void getGallery(boolean getSelected) {

		// / You will need the database information because you need either the
		// average score or the frequency of exceeding the threshold.

		root = new SplitPane();
		BorderPane rhs = new BorderPane();
		root.setOrientation(Orientation.HORIZONTAL);
		root.setDividerPosition(0, .7);

		// distanceField = new TextField();
		distanceField.setEditable(false);

		// rateField = new TextField();
		rateField.setEditable(false);

		// distanceField = new TextField();
		countField.setEditable(false);

		// rateField = new TextField();
		valueField.setEditable(false);
		
		clearFields();

		
		final Button save = new Button("Save");
		save.setOnMouseClicked(event -> {
			saveToProperties();
		});
		
		final Button reset = new Button("Reset");
		reset.setOnMouseClicked(event -> {
			getGallery(true);
		});
		
		final Button clear = new Button("Clear");
		clear.setOnMouseClicked(event -> {

				clearFields();
				Iterator<Tile> it = tileSet.iterator();
				while(it.hasNext()){
					Tile tile = it.next();
					tile.selected=false;
					tile.setStroke(Color.BLACK);
				}
				calibratedTileSet.clear();
				getGallery(false);
		});

		GridPane grid = new GridPane();
		grid.setVgap(4);
		grid.setHgap(10);
		grid.setPadding(new Insets(5, 5, 5, 5));
		Label speciesLabel = new Label("Species ");
		grid.add(speciesLabel, 0, 0);
		grid.add(speciesList, 1, 0);
		Label distLabel = new Label("Distances ");
		distLabel.setMinWidth(50);
		grid.add(distLabel, 0, 2);
		grid.add(distanceField, 1, 2);
		Label rateLabel = new Label("Rates ");
		rateLabel.setMinWidth(50);
		grid.add(rateLabel, 0, 3);
		grid.add(rateField, 1, 3);

		Label countLabel = new Label("Count ");
		countLabel.setMinWidth(50);
		grid.add(countLabel, 0, 5);
		grid.add(countField, 1, 5);
		Label valueLabel = new Label("Avg Value ");
		valueLabel.setMinWidth(50);
		grid.add(valueLabel, 0, 6);
		grid.add(valueField, 1, 6);
		
		grid.add(reset, 0, 8);
		grid.add(save, 2, 8);
		grid.add(clear,1,8);

		SplitPane sp = new SplitPane();
		sp.setOrientation(Orientation.VERTICAL);
		sp.setDividerPosition(0, 0.5);

		table = new TableView<TableRow>();
		table.setEditable(false);
		table.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		TableColumn dist_col = new TableColumn("Distances");
		dist_col.setMinWidth(80);
		dist_col.setCellValueFactory(new PropertyValueFactory<TableRow, String>(
				"distance"));
		TableColumn rate_col = new TableColumn("Rates");
		rate_col.setMinWidth(80);
		rate_col.setCellValueFactory(new PropertyValueFactory<TableRow, String>(
				"rate"));
		TableColumn count_col = new TableColumn("Count");
		count_col.setMinWidth(80);
		count_col
				.setCellValueFactory(new PropertyValueFactory<TableRow, String>(
						"count"));
		TableColumn val_col = new TableColumn("Average Value");
		val_col.setMinWidth(80);
		val_col.setCellValueFactory(new PropertyValueFactory<TableRow, String>(
				"value"));

		if (spref.getSpeciesList().isEmpty() || species.isEmpty()) {
			ErrorHandler
					.showWarning("Species List Is Empty",
							"The species list is empty.  Calibration will not be performed.");
		}

		else {
			//GridPane gp = makePColor();
			table.setItems(data);
			table.getColumns().addAll(dist_col, rate_col, count_col, val_col);
			sp.getItems().addAll(grid, table);
			rhs.setCenter(sp);
			root.getItems().addAll(makePColor(getSelected), rhs);
		}
		parent.getCUITab().setContent(root);
	}

	public GridPane makePColor(boolean showSelected) {
		GridPane gp = new GridPane();

		if (spref.getSpeciesList().isEmpty()) {
			return gp;
		}

		double[] rates = spref.getRates().get(species);
		double[] distances = spref.getDistances().get(species);

		nrows = rates.length;
		ncols = distances.length;

		int maxCount = getMax();

		Map<Double, Map<Double, Number[]>> calsp = calvals.get(species);
		Map<Double, Map<Double, Number[]>> selsp = selvals.get(species);

		data.clear();

		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {

				Tile tile = new Tile(i, j, rwidth, rheight);
				tileSet.add(tile);

				if (calsp.containsKey(rates[i])
						&& calsp.get(rates[i]).containsKey(distances[j])) {
					int count = calsp.get(rates[i]).get(distances[j])[0]
							.intValue();
					double val = calsp.get(rates[i]).get(distances[j])[1]
							.doubleValue();
					tile.setFill(ColorUtils.jet(count, 0, maxCount));
					tile.setCount(count);
					tile.setValue(val);

					if (showSelected&&selsp.containsKey(rates[i])
							&& selsp.get(rates[i]).containsKey(distances[j])) {
						tile.highlight();
						tile.selected = true;

						data.add(new TableRow(new Double(distances[j])
								.toString(), new Double(rates[i]).toString(),
								calvals.get(species).get(rates[i])
										.get(distances[j])[0].toString(), nf2.format(calvals
										.get(species).get(rates[i])
										.get(distances[j])[1])));
					}
				} else {
					tile.setFill(Color.GRAY);
					tile.setValue(Double.NaN);
				}
				gp.add(tile, j, i);
			}
		}

		updateFields();

		// gp.setGridLinesVisible(true);
		gp.setSnapToPixel(false);
		// gp.setHgap(1);
		// gp.setVgap(1);
		// gp.setStyle("-fx-background-color: BLACK;");
		gp.setPrefWidth(ncols * rwidth + ncols);
		gp.setPrefHeight(nrows * rheight + nrows);

		return gp;
	}

	public void setParentGUI(MainGUI gui) {
		this.parent = gui;
	}

	private void updateFields() {
		Iterator<Tile> it = calibratedTileSet.iterator();
		StringBuffer dbuf = new StringBuffer();
		StringBuffer rbuf = new StringBuffer();
		StringBuffer cbuf = new StringBuffer();
		StringBuffer vbuf = new StringBuffer();

		while (it.hasNext()) {	
			Tile tile = it.next();
			dbuf.append(spref.getDistances().get(species)[tile.getColumn()]
					+ ", ");
			rbuf.append(spref.getRates().get(species)[tile.getRow()] + ", ");
			cbuf.append(tile.getCount().toString()
					+ ", ");
			vbuf.append(nf.format(tile.getValue().doubleValue()) + ", ");
		}

		if (dbuf.toString().isEmpty()) {
			distanceField.setText("[]");
		} else {
			distanceField.setText("["
					+ dbuf.toString().substring(0, dbuf.length() - 2) + "]");
		}
		if (rbuf.toString().isEmpty()) {
			rateField.setText("[]");
		} else {
			rateField.setText("["
					+ rbuf.toString().substring(0, rbuf.length() - 2) + "]");
		}
		if (cbuf.toString().isEmpty()) {
			countField.setText("[]");
		} else {
			countField.setText("["
					+ cbuf.toString().substring(0, cbuf.length() - 2) + "]");
		}
		if (vbuf.toString().isEmpty()) {
			valueField.setText("[]");
		} else {
			valueField.setText("["
					+ vbuf.toString().substring(0, vbuf.length() - 2) + "]");
		}
		
	}

	@SuppressWarnings("rawtypes")
	private void saveToProperties() {
		double[] darr = {}, rarr = {};

		try {
			darr = TextUtils.parseNumericArray(distanceField.getText());
		} catch (IllegalArgumentException | IOException e) {
			ErrorHandler.showException(e);
			return;
		}

		try {
			rarr = TextUtils.parseNumericArray(rateField.getText());
		} catch (IllegalArgumentException | IOException e) {
			ErrorHandler.showException(e);
			return;
		}

		if (darr.length == 0 || rarr.length == 0) {
			ErrorHandler
					.showWarning("No values selected",
							"No values were selected for forwarding to the parameter file.");
		}

		if (spref == null) {
			ErrorHandler
					.showException(new Exception(
							"The properties file has not been linked to the calibration interface.  There is no destination for the selected values."));
		}

		TreeItem<Item> distanceNode = JavaFXUtils.findNode(
				"dispersal distances", spref.getRoot());
		TreeItem<Item> rateNode = JavaFXUtils.findNode("dispersal rates",
				spref.getRoot());

		TreeItem<Item> dsp = JavaFXUtils.findNode(species, distanceNode);
		TreeItem<Item> rsp = JavaFXUtils.findNode(species, rateNode);

		spref.getDistances().put(species, darr);
		spref.getRates().put(species, rarr);

		dsp.setValue(new Item<>(species, distanceField.getText()));
		rsp.setValue(new Item<>(species, rateField.getText()));

		spref.setRunType("paired");
		TreeItem<Item> runNode = JavaFXUtils.findNode(
				"run type (paired vs. calibration)", spref.getRoot());
		runNode.setValue(new Item<>("run type (paired vs. calibration)",
				"paired"));

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Notice");
		alert.setHeaderText(null);
		alert.setContentText("Parameter file values set for species "
				+ species
				+ ".  The run type was also changed to 'Paired'.  Please ensure that distance and rate array sizes match for other species if present.");

		alert.showAndWait();
	}

	public void setSpreadPropertiesReference(SpreadProperties sprop) {
		this.spref = sprop;
		if (!spref.getSpeciesList().isEmpty()) {
			species = spref.getSpeciesList().get(0);
			ObservableList<String> speciesOptions = FXCollections
					.observableArrayList(spref.getSpeciesList());
			speciesList = new ComboBox<String>(speciesOptions);
			speciesList.setValue(species);
			speciesList.setOnAction(event -> {
				if (!species.equals(speciesList.getValue())) {
					species = speciesList.getValue();
					distanceField.clear();
					rateField.clear();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							getGallery(true);
						}
					});
				}
			});
		} else {
			speciesList = new ComboBox<String>(
					FXCollections.observableArrayList("<Empty>"));
		}
	}

	public void setCalVals(Map<String, Map<Double, Map<Double, Number[]>>> map) {
		this.calvals = map;
	}

	public void setSelVals(Map<String, Map<Double, Map<Double, Number[]>>> map) {
		this.selvals = map;
	}

	private int getMax() {
		int count = 0;
		Map<Double, Map<Double, Number[]>> map1 = calvals.get(species);
		Iterator<Double> it = map1.keySet().iterator();
		while (it.hasNext()) {
			Map<Double, Number[]> map2 = map1.get(it.next());
			Iterator<Double> it2 = map2.keySet().iterator();
			while (it2.hasNext()) {
				count = Math.max(count, map2.get(it2.next())[0].intValue());
			}
		}
		return count;
	}

	class Tile extends Rectangle implements Comparable<Tile> {
		int row;
		int column;
		boolean selected = false;
		boolean held = false;
		Number count = 0;
		Number value = 0;

		Tile(int row_, int column_, int height, int width) {
			super(height, width);
			row = row_;
			column = column_;
			held = false;

			setStroke(Color.BLACK);
			setStrokeWidth(1);

			this.setOnMousePressed(event -> {

				Tile tile = (Tile) event.getSource();

				if (!tile.selected) {
					tile.highlight();
					tile.selected = true;
					selecting = true;

				} else {
					tile.unhighlight();
					tile.selected = false;
					selecting = false;
				}
			});

			this.setOnDragDetected(event -> {
				Tile tile = (Tile) event.getSource();
				tile.startFullDrag();
			});

			this.setOnMouseDragEntered(event -> {

				Tile tile = (Tile) event.getSource();

				if (selecting) {
					tile.highlight();
					tile.selected = true;
				} else {
					tile.unhighlight();
					tile.selected = false;
				}
			});

			this.setOnMouseReleased(event -> {
				updateFields();
			});
		}

		int getColumn() {
			return column;
		}

		int getRow() {
			return row;
		}
		
		Number getCount(){
			return count;
		}
		
		Number getValue(){
			return value;
		}

		@Override
		public int compareTo(Tile other) {
			if (this.row < other.row) {
				return -1;
			}
			if (this.row > other.row) {
				return 1;
			}
			if (this.column < other.column) {
				return -1;
			}
			if (this.column > other.column) {
				return 1;
			}
			return 0;
		}

		boolean equals(Tile other) {
			return compareTo(other) == 0;
		}

		public void highlight() {
			setStroke(Color.WHITE);
			calibratedTileSet.add(this);
		}

		boolean isSelected() {
			return selected;
		}

		private void removeItem(Tile tile) {
			calibratedTileSet.remove(tile);
		}

		void setColumn(int column_) {
			column = column_;
		}

		void setRow(int row_) {
			row = row_;
		}
		
		void setCount(Number count_) {
			count = count_;
		}

		void setSelected(boolean select_) {
			selected = select_;
		}

		void setValue(Number value) {
			this.value = value;
		}

		public void unhighlight() {
			setStroke(Color.BLACK);
			removeItem(this);
		}
	}

	public static class TableRow {

		private final SimpleStringProperty distance;
		private final SimpleStringProperty rate;
		private final SimpleStringProperty count;
		private final SimpleStringProperty value;

		private TableRow(String distance, String rate, String count, String val) {
			this.distance = new SimpleStringProperty(distance);
			this.rate = new SimpleStringProperty(rate);
			this.count = new SimpleStringProperty(count);
			this.value = new SimpleStringProperty(val);
		}

		public String getDistance() {
			return distance.get();
		}

		public void setDistance(String distance) {
			this.distance.set(distance);
		}

		public String getRate() {
			return rate.get();
		}

		public void setRate(String rate) {
			this.rate.set(rate);
		}

		public String getCount() {
			return count.get();
		}

		public void setCount(String count) {
			this.count.set(count);
		}

		public String getValue() {
			return value.get();
		}

		public void setValue(String value) {
			this.value.set(value);
		}
	}
}
