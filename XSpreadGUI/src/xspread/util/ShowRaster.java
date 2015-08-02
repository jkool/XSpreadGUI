package xspread.util;

import java.io.IOException;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author John B. Matthews
 */
public class ShowRaster extends Application {

	double dragBaseX, dragBaseY, dragBase2X, dragBase2Y;

	@Override
	public void start(Stage stage) throws IOException {
		// load the image
		Raster raster = RasterReader
				.readRaster("C:/TempX/demo_frequency_test.txt");
				//.readRaster("C:/Temp2/STest_BK/OH_frequency.txt");

		ImageView iv1 = raster.continuousImageView(500,500);

		/*
		 * ImageView iv2 = new ImageView(); iv2.setImage(wr);
		 * iv2.setFitWidth(100); iv2.setPreserveRatio(true);
		 * iv2.setSmooth(true); iv2.setCache(true);
		 * 
		 * ImageView iv3 = new ImageView(); iv3.setImage(wr); Rectangle2D
		 * viewportRect = new Rectangle2D(40, 35, 110, 110);
		 * iv3.setViewport(viewportRect); iv3.setRotate(90);
		 */

		Group root = new Group();
		Scene scene = new Scene(root);
		//scene.setFill(Color.BLACK);

		setImagePan(scene, iv1);

		HBox box = new HBox();
		box.getChildren().add(iv1);
		// box.getChildren().add(iv2);
		// box.getChildren().add(iv3);

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

	private void setImagePan(Scene scene, ImageView iv) {
		iv.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				scene.setCursor(Cursor.MOVE);
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
				scene.setCursor(Cursor.DEFAULT);
			}
		});

		iv.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {

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
				iv.scaleXProperty().set(iv.scaleXProperty().get() * factor);
				iv.scaleYProperty().set(iv.scaleYProperty().get() * factor);
			}
		});
	}
}
