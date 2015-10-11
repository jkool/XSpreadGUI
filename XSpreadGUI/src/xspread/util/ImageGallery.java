package xspread.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import xspread.MainGUI;
import xspread.application.SpreadProperties;

public class ImageGallery {

	private SpreadProperties spref;
	private String species;
	private ComboBox<String> speciesList;
	final ComboBox<String> rasterTypeComboBox = new ComboBox<String>();
	private SplitPane root;
	private BorderPane lhs = new BorderPane();
	private String path = "C:/Temp2/Rasters";
	private double dragBaseX, dragBaseY, dragBase2X, dragBase2Y;
	private MainGUI parent;
	private Label selected = null;
	private ImageView selImg = null;
	private boolean killProcess = false;
	private Button save;
	private Stage secondaryStage;
	private ProgressIndicator pi;

	public ImageGallery() {
		rasterTypeComboBox.getItems().addAll("ALL", "Cover", "Frequency",
				"Monitored", "Stage");

		rasterTypeComboBox.setValue("ALL");

		rasterTypeComboBox.setOnAction(event -> {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					getGallery();
				}
			});
		});
	}

	public void getGallery() {

		boolean first = true;

		Platform.runLater(() -> {
			secondaryStage = new Stage();
			killProcess = false;
			secondaryStage
					.getIcons()
					.add(new Image(
							MainGUI.class
									.getResourceAsStream("/xspread/resources/CDU_32.png")));
			secondaryStage.setResizable(false);
			secondaryStage.setTitle("Processing...");
			secondaryStage.setAlwaysOnTop(true);
			Group secondRoot = new Group();
			Scene scene = new Scene(secondRoot, 380, 110, Color.WHITE);
			GridPane mainPane = new GridPane();
			secondRoot.getChildren().add(mainPane);
			secondaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					killProcess = true;
					System.out.println("Cancelled.");
					secondaryStage.close();
				}
			});

			pi = new ProgressIndicator();
			pi.setMinWidth(140);
			pi.setProgress(-1);
			HBox hb = new HBox();
			final Label plabel = new Label("Building images... Please wait.");
			hb.getChildren().add(plabel);
			hb.getChildren().add(pi);
			hb.setAlignment(Pos.CENTER);
			hb.setPadding(new Insets(20, 20, 20, 20));
			mainPane.setAlignment(Pos.CENTER);
			mainPane.add(hb, 0, 0);

			final Button cancelButton = new Button("Cancel");
			mainPane.add(cancelButton, 3, 1);

			cancelButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					killProcess = true;
					System.out.println("Cancelled.");
					secondaryStage.close();
				}
			});

			secondaryStage.setScene(scene);
		});

		long tstart = System.currentTimeMillis();

		root = new SplitPane();
		BorderPane rhs = new BorderPane();
		root.setOrientation(Orientation.HORIZONTAL);
		root.setDividerPosition(0, .7);

		GridPane grid = new GridPane();
		grid.setVgap(4);
		grid.setHgap(10);
		grid.setPadding(new Insets(5, 5, 5, 5));
		grid.add(new Label("Species: "), 0, 0);
		grid.add(speciesList, 1, 0);
		grid.add(new Label("Raster type: "), 0, 1);
		grid.add(rasterTypeComboBox, 1, 1);

		save = new Button("Save Image");

		// if(selImg==null){save.setDisable(true);}
		// else{
		save.setOnAction((event) -> {

			if (selImg == null) {
				ErrorHandler
						.showWarning(
								"No image selected",
								"No image has been selected.  Please click on the image you would like to save.");
				return;
			}

			FileChooser fc = new FileChooser();
			List<FileChooser.ExtensionFilter> filters = new ArrayList<FileChooser.ExtensionFilter>();
			filters.add(new FileChooser.ExtensionFilter("PNG files", "*.png"));
			fc.getExtensionFilters().addAll(filters);

			// Show open dialog

			File file = fc.showSaveDialog(new Stage());

			if (file != null) {
				WritableImage image = selImg.snapshot(new SnapshotParameters(),
						null);
				try {
					ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png",
							file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		// }

		grid.add(save, 1, 3);

		ScrollPane gallery = new ScrollPane();
		TilePane tile = new TilePane();
		tile.setPadding(new Insets(8, 8, 8, 8));
		tile.setHgap(8);
		tile.setVgap(8);
		// tile.setStyle(cssBordering);

		if (species.equalsIgnoreCase("ALL")) {
			for (String sp : spref.getSpeciesList()) {
				tile.getChildren().add(
						addTile(new File(spref.getPresenceFiles().get(sp))));
				tile.getChildren().add(
						addTile(new File(spref.getAgeFiles().get(sp))));
				tile.getChildren().add(
						addTile(new File(spref.getHabitatFiles().get(sp))));
				tile.getChildren().add(
						addTile(new File(spref.getReferenceFiles().get(sp))));
				if (!spref.getManagementFiles().get(sp).equalsIgnoreCase("None")) {
					tile.getChildren().add(
							addTile(new File(spref.getManagementFiles().get(sp))));
				}
			}
		} else {

			tile.getChildren().add(
					addTile(new File(spref.getPresenceFiles().get(species))));
			tile.getChildren().add(
					addTile(new File(spref.getAgeFiles().get(species))));
			tile.getChildren().add(
					addTile(new File(spref.getHabitatFiles().get(species))));
			tile.getChildren().add(
					addTile(new File(spref.getReferenceFiles().get(species))));
			if (!spref.getManagementFiles().get(species).equalsIgnoreCase("None")) {
				tile.getChildren().add(
						addTile(new File(spref.getManagementFiles().get(species))));
			}
		}

		File folder = new File(path);
		String filter = rasterTypeComboBox.getValue().toLowerCase();
		ArrayList<String> filterList = new ArrayList<String>();
		if (!filter.equalsIgnoreCase("all")) {
			filterList.add(".*" + filter + ".*");
		}
		if (!species.equalsIgnoreCase("all")) {
			filterList.add(".*" + species.toLowerCase() + ".*");
		}
		filterList.add(".*\\.txt$");
		File[] listOfFiles = folder.listFiles(new ListFilter(filterList));

		// String cssBordering = "-fx-border-color: WHITE";// \n" //#090a0c
		// + "-fx-border-insets:3;\n"
		// + "-fx-border-radius:7;\n"
		// + "-fx-border-width:1.0";

		int ct = 0;
		double total = (double) listOfFiles.length;

		for (final File file : listOfFiles) {
			if (killProcess) {
				break;
			}
			if (first && System.currentTimeMillis() - tstart > 1000) {
				Platform.runLater(() -> {
					secondaryStage.show();
				});
				first = false;
			}
			Label label = new Label(file.getName());
			ImageView imageView = null;

			if (file.getName().toLowerCase().contains("frequency")
					|| file.getName().toLowerCase().contains("probability")) {
				try {
					imageView = new Raster(file)
							.continuousImageView_byWidth(180);
				} catch (IOException e) {
					continue;
				}
			} else {
				try {
					imageView = new Raster(file).discreteImageView_byWidth(180);
				} catch (IOException e) {
					continue;
				}
			}

			label.setContentDisplay(ContentDisplay.TOP);
			label.setGraphic(imageView);
			addClickBehaviour(label, path);
			// imageView.setStyle(cssBordering);
			tile.getChildren().add(label);
			ct++;
			pi.setProgress((double) ct / total);

		}

		killProcess = false;

		gallery.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); // Horizontal
		gallery.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Vertical
																		// scroll
																		// bar
		gallery.setFitToWidth(true);
		gallery.setContent(tile);

		// grid.add (rhs, 1, 3, 2, 2);

		rhs.setTop(grid);
		rhs.setCenter(gallery);

		TilePane tmp = new TilePane();
		Label tmptxt = new Label("Click thumbnails for larger image");
		tmptxt.setAlignment(Pos.CENTER);
		tmp.getChildren().add(tmptxt);
		tmp.setAlignment(Pos.CENTER);
		lhs.setCenter(tmp);

		root.getItems().addAll(lhs, rhs);
		parent.getImageGalleryTab().setContent(root);
		Platform.runLater(() -> {
			secondaryStage.close();
		});
	}

	private Label addClickBehaviour(Label label, String path) {

		label.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {

				if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {

					if (mouseEvent.getClickCount() == 1) {

						if (selected != null) {
							Label old_select = selected;
							old_select
									.setStyle("-fx-border-color: transparent;");
						}

						String str = path + "/" + label.getText();
						ImageView x_imageView = null;

						if (str.toLowerCase().contains("frequency")
								|| str.toLowerCase().contains("hsm")) {
							try {
								x_imageView = new Raster(str)
										.continuousImageView_byHeight((int) root
												.getHeight() - 10);
								// save.setDisable(false);
							} catch (IOException e) {
								ErrorHandler.showException(e);
							}
						} else {
							try {
								x_imageView = new Raster(str)
										.discreteImageView_byHeight((int) root
												.getHeight() - 10);
								// save.setDisable(false);
							} catch (IOException e) {
								ErrorHandler.showException(e);
							}
						}

						x_imageView.setSmooth(true);
						x_imageView.setPreserveRatio(true);

						selImg = x_imageView;

						setImagePan(x_imageView);
						lhs.setCenter(x_imageView);

						selected = (Label) mouseEvent.getSource();
						selected.setStyle("-fx-border-color: red;");
					}
				}
			}
		});

		return label;
	}

	private void setImagePan(ImageView iv) {
		iv.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				root.setCursor(Cursor.MOVE);
				dragBaseX = iv.translateXProperty().get();
				dragBaseY = iv.translateYProperty().get();
				dragBase2X = event.getSceneX();
				dragBase2Y = event.getSceneY();
			}
		});

		iv.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				iv.setTranslateX(dragBaseX + (event.getSceneX() - dragBase2X));
				iv.setTranslateY(dragBaseY + (event.getSceneY() - dragBase2Y));
			}
		});
		iv.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				root.setCursor(Cursor.DEFAULT);
			}
		});

		iv.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						double dy = event.getDeltaY();
						if (dy == 0) {
							return;
						}
						double factor = 1;
						if (dy > 0) {
							factor = 1.2;
						}
						if (dy < 0) {
							factor = 0.8;
						}
						iv.scaleXProperty().set(
								iv.scaleXProperty().get() * factor);
						iv.scaleYProperty().set(
								iv.scaleYProperty().get() * factor);
					}
				});
			}
		});
	}

	public void setSpreadPropertiesReference(SpreadProperties sprop) {
		this.spref = sprop;
		path = spref.getOutputFolder();

		if (!spref.getSpeciesList().isEmpty()) {
			if (spref.getSpeciesList().size() == 1) {
				species = spref.getSpeciesList().get(0);
			} else {
				species = "ALL";
			}

			ObservableList<String> speciesOptions = FXCollections
					.observableArrayList();
			speciesOptions.add("ALL");
			speciesOptions.addAll(spref.getSpeciesList());
			speciesList = new ComboBox<String>(speciesOptions);
			speciesList.setValue("ALL");
			speciesList.setOnAction(event -> {
				if (!species.equals(speciesList.getValue())) {
					species = speciesList.getValue();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							getGallery();
						}
					});
				}
			});
		} else {
			speciesList = new ComboBox<String>(
					FXCollections.observableArrayList("<Empty>"));
			species = "";
		}
	}

	public void setParentGUI(MainGUI gui) {
		this.parent = gui;
	}

	private Label addTile(File file) {
		Label label = new Label(file.getName());
		ImageView imageView = null;

		if (file.getName().toLowerCase().contains("freq")
				|| file.getName().toLowerCase().contains("probability")
				|| file.getName().toLowerCase().contains("hsm")) {
			try {
				imageView = new Raster(file).continuousImageView_byWidth(200);
			} catch (IOException e) {
				return null;
			}
		} else {
			try {
				imageView = new Raster(file).discreteImageView_byWidth(200);
			} catch (IOException e) {
				return null;
			}
		}

		label.setContentDisplay(ContentDisplay.TOP);
		label.setGraphic(imageView);
		addClickBehaviour(label, file.getParentFile().toString());
		// imageView.setStyle(cssBordering);
		return label;
	}

}
