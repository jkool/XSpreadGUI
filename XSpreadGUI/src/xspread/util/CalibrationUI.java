package xspread.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javafx.application.Platform;
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
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
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
	private SplitPane root;
	private Set<Tile> tileIndex = new TreeSet<Tile>();
	private String species = "";
	private SpreadProperties spref;
	private ComboBox<String> speciesList = new ComboBox<String>();
	private MainGUI parent;
	private boolean selecting;
	private Map<String,Map<Double,Map<Double,Integer>>> calvals;
	private Map<String,Map<Double,Map<Double,Integer>>> selvals;

	public void getGallery() {
		
		/// You will need the database information because you need either the average score or the frequency of exceeding the threshold.
		
		root = new SplitPane();
		BorderPane rhs = new BorderPane();
		root.setOrientation(Orientation.HORIZONTAL);
		root.setDividerPosition(0, .7);

		//distanceField = new TextField();
		distanceField.setEditable(false);
		//rateField = new TextField();
		rateField.setEditable(false);
		final Button save = new Button("Save");
		save.setOnMouseClicked(event -> {
			saveToProperties();
		});

		GridPane grid = new GridPane();
		grid.setVgap(4);
		grid.setHgap(10);
		grid.setPadding(new Insets(5, 5, 5, 5));
		Label speciesLabel = new Label("Species ");
		grid.add(speciesLabel, 0, 0);
		grid.add(speciesList, 1, 0);
		Label distLabel = new Label("Distances ");
		distLabel.setMinWidth(57);
		grid.add(distLabel, 0, 2);
		grid.add(distanceField, 1, 2);
		Label rateLabel = new Label("Rates ");
		rateLabel.setMinWidth(57);
		grid.add(rateLabel, 0, 3);
		grid.add(rateField, 1, 3);
		grid.add(save, 0, 5);
		rhs.setCenter(grid);
		
		if(spref.getSpeciesList().isEmpty()||species.isEmpty()){
			ErrorHandler.showWarning("Species List Is Empty", "The species list is empty.  Calibration will not be performed.");
		}
		else{
			root.getItems().addAll(makePColor(), rhs);
		}
		parent.getCUITab().setContent(root);
	}
	
    public GridPane makePColor() {
		GridPane gp = new GridPane();

		if(spref.getSpeciesList().isEmpty()){return gp;}
		
		double[] rates = spref.getRates().get(species);
		double[] distances = spref.getDistances().get(species);

		nrows = rates.length;
		ncols = distances.length;
		
		int maxVal = getMax();
		
		Map<Double,Map<Double,Integer>> calsp = calvals.get(species);
		Map<Double,Map<Double,Integer>> selsp = selvals.get(species);

		for (int i = 0; i < nrows; i++) {
			for (int j = 0; j < ncols; j++) {
				
				
				Tile tile = new Tile(i, j, rwidth, rheight);
				
				if(calsp.containsKey(rates[i]) && calsp.get(rates[i]).containsKey(distances[j])){
					int val = calsp.get(rates[i]).get(distances[j]);
					tile.setFill(ColorUtils.jet(val,0,maxVal));
				    tile.setValue(val);
					
					if(selsp.containsKey(rates[i]) && selsp.get(rates[i]).containsKey(distances[j])){
		                tile.highlight();
		                tile.selected=true;
					}
				}
				else{
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

    public void setParentGUI(MainGUI gui){
    	this.parent=gui;
    }

	private void updateFields(){
		Iterator<Tile> it = tileIndex.iterator();
		StringBuffer dbuf = new StringBuffer();
		StringBuffer rbuf = new StringBuffer();
		
		while(it.hasNext()){
			Tile tile = it.next();
			dbuf.append(spref.getDistances().get(species)[tile.getColumn()] + ", ");
			rbuf.append(spref.getRates().get(species)[tile.getRow()] + ", ");
		}
		
		if(dbuf.toString().isEmpty()){distanceField.setText("[]");}
		else{distanceField.setText("[" + dbuf.toString().substring(0, dbuf.length()-2) + "]");}
		if(rbuf.toString().isEmpty()){rateField.setText("[]");}
		else{rateField.setText("[" + rbuf.toString().substring(0, rbuf.length()-2) + "]");}		
	}
	
	@SuppressWarnings("rawtypes")
	private void saveToProperties(){		
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
			
			if(darr.length==0 || rarr.length==0){
				ErrorHandler.showWarning("No values selected", "No values were selected for forwarding to the parameter file.");
			}
		
			if(spref==null){
				ErrorHandler.showException(new Exception("The properties file has not been linked to the calibration interface.  There is no destination for the selected values."));
			}
					
			TreeItem<Item> distanceNode = JavaFXUtils.findNode("dispersal distances", spref.getRoot());
			TreeItem<Item> rateNode = JavaFXUtils.findNode("dispersal rates", spref.getRoot());
			
			TreeItem<Item> dsp = JavaFXUtils.findNode(species, distanceNode);
			TreeItem<Item> rsp = JavaFXUtils.findNode(species, rateNode);
			
			spref.getDistances().put(species, darr);
			spref.getRates().put(species, rarr);
			
			dsp.setValue(new Item<>(species, distanceField.getText()));
			rsp.setValue(new Item<>(species, rateField.getText()));
			
			spref.setRunType("paired");
			TreeItem<Item> runNode = JavaFXUtils.findNode("run type (paired vs. calibration)", spref.getRoot());
			runNode.setValue(new Item<>("run type (paired vs. calibration)","paired"));
			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Notice");
			alert.setHeaderText(null);
			alert.setContentText("Parameter file values set for species "+species+".  The run type was also changed to 'Paired'.  Please ensure that distance and rate array sizes match for other species if present.");

			alert.showAndWait();
	}
	
	public void setSpreadPropertiesReference(SpreadProperties sprop){
		this.spref=sprop;
		if(!spref.getSpeciesList().isEmpty()){species = spref.getSpeciesList().get(0);
		ObservableList<String> speciesOptions = FXCollections.observableArrayList(spref.getSpeciesList());
		speciesList = new ComboBox<String>(speciesOptions);
		speciesList.setValue(species);
		speciesList.setOnAction(event->{
			if(!species.equals(speciesList.getValue())){
			species = speciesList.getValue();
			distanceField.clear();
			rateField.clear();
			Platform.runLater(new Runnable(){public void run(){getGallery();}});}
		});
		}
		else{speciesList = new ComboBox<String>(FXCollections.observableArrayList("<Empty>"));}	
	}
	
	public void setCalVals(Map<String,Map<Double,Map<Double,Integer>>> map){
		this.calvals=map;
	}
	
	public void setSelVals(Map<String,Map<Double,Map<Double,Integer>>> map){
		this.selvals=map;
	}
	
	
	private int getMax(){
		int val = 0;
		Map<Double,Map<Double,Integer>> map1 = calvals.get(species);
		Iterator<Double> it = map1.keySet().iterator();
		while(it.hasNext()){
			Map<Double,Integer> map2 = map1.get(it.next());
			Iterator<Double> it2 = map2.keySet().iterator();
			while(it2.hasNext()){
				val = Math.max(val,map2.get(it2.next()));
			}
		}
		return val;
	}	
	
	class Tile extends Rectangle implements Comparable<Tile> {
		int row;
		int column;
		boolean selected = false;
		boolean held = false;
		Number val = 0;

		Tile(int row_, int column_, int height, int width) {
			super(height, width);
			row = row_;
			column = column_;
			held = false;
			
			setStroke(Color.BLACK);
			setStrokeWidth(1);

			this.setOnMousePressed(event -> {

			            Tile tile = (Tile) event.getSource();

			            if( !tile.selected) {
			                tile.highlight();
			                tile.selected=true;
			                selecting=true;
			                
			            } else {
			                tile.unhighlight();
			                tile.selected=false;
			                selecting=false;
			            }
			        });
			
		   this.setOnDragDetected(event -> {
		            Tile tile = (Tile) event.getSource();
		            tile.startFullDrag();
		        });
		   
	       this.setOnMouseDragEntered(event -> {

	            Tile tile = (Tile) event.getSource();
	            
	            if(selecting) {
	                tile.highlight();
	                tile.selected=true;
	            } else {
	                tile.unhighlight();
	                tile.selected=false;
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
		
		public int compareTo(Tile other){
			if(this.row<other.row){return -1;}
			if(this.row>other.row){return 1;}
			if(this.column<other.column){return -1;}
			if(this.column>other.column){return 1;}
			return 0;
		}
		
		boolean equals(Tile other){
			return compareTo(other)==0;
		}

		public void highlight() {
        	setStroke(Color.WHITE);
        	tileIndex.add(this);
        }

		boolean isSelected() {
			return selected;
		}

		private void removeItem(Tile tile){
			tileIndex.remove(tile);
        }

        void setColumn(int column_) {
			column = column_;
		}

        void setRow(int row_) {
			row = row_;
		}
        
        void setSelected(boolean select_) {
			selected = select_;
		}
        
        void setValue(Number val){
        	this.val=val;
        }
		
		public void unhighlight() {
            setStroke(Color.BLACK);
            removeItem(this);
        }
	}
}
