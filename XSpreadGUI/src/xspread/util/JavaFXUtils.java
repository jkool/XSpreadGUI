package xspread.util;

import java.nio.IntBuffer;

import xspread.application.Item;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;

public class JavaFXUtils {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static TreeItem<Item> findNode(String identifier, TreeItem<Item> root) {
		if (identifier.isEmpty()) {
			return null;
		}
		for (TreeItem<Item> child : root.getChildren()) {
			Item<String, String> c = child.getValue();
			if (c.getKey().equalsIgnoreCase(identifier)) {
				return child;
			} else {
				TreeItem<Item> item = findNode(identifier, child);
				if (item == null) {
					continue;
				} else {
					return item;
				}
			}
		}
		return null;
	}

	public Image nn_scale(Image image, double zoom) {

		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		IntBuffer src = IntBuffer.allocate(width * height);
		WritablePixelFormat<IntBuffer> pf = PixelFormat.getIntArgbInstance();
		image.getPixelReader().getPixels(0, 0, width, height, pf, src, width);
		int newWidth = (int) ((double) width * zoom);
		int newHeight = (int) ((double) height * zoom);
		int[] dst = new int[newWidth * newHeight];
		int index = 0;
		for (int y = 0; y < height; y++) {
			//index = y * newWidth * z;
			//for (int x = 0; x < width; x++) {
			//	int pixel = src.get();
			//	for (int i = 0; i < z; i++) {
			//		for (int j = 0; j < z; j++) {
			//			dst[index + i + (newWidth * j)] = pixel;
			//		}
			//	}
			//	index += z;
			//}
		}
		WritableImage bigImage = new WritableImage(newWidth, newHeight);
		bigImage.getPixelWriter().setPixels(0, 0, newWidth, newHeight, pf, dst,
				0, newWidth);
		
		return bigImage;
	}
}
