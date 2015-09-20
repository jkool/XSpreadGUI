/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package xspread.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.StringTokenizer;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Class representing a Raster object.
 */

public class Raster {

	public static final String DEFAULT_NODATA = "-9999";
	/**
	 * @param data
	 *            - an array of data values (provides dimensions).
	 * @param xll
	 *            - the lower left x-coordinate of the raster.
	 * @param yll
	 *            - the lower left y-coordinate of the raster.
	 * @param size
	 *            - the cell size of the raster.
	 * @return an empty raster using the values of the parameters as currently
	 *         set.
	 */

	public static Raster getTempRaster(double[][] data, double xll, double yll,
			double size) {
		return getTempRaster(data, xll, yll, size, DEFAULT_NODATA);
	}
	/**
	 * Retrieves an empty raster created using the provided parameters
	 * 
	 * @param xll
	 *            - the lower left x-coordinate of the raster.
	 * @param yll
	 *            - the lower left y-coordinate of the raster.
	 * @param size
	 *            - the cell size of the raster.
	 * @param ndata
	 *            - the NoData value to be used by the raster.
	 * @return an empty raster using the values of the parameters as currently
	 *         set.
	 */

	public static Raster getTempRaster(double[][] data, double xll, double yll,
			double size, String ndata) {
		Raster a = new Raster();
		a.data = data;
		a.xll = xll;
		a.yll = yll;
		a.cellsize = size;
		a.NDATA = ndata;
		a.rows = data.length;
		a.cols = data[0].length;
		return a;
	}
	protected double[][] data;
	protected double xll;
	protected double yll;

	protected double cellsize;

	protected int cols;

	protected int rows;

	protected String NDATA;

	/**
	 * Creates an empty raster
	 */
	public Raster() {
	}

	/**
	 * Creates a raster from the given data
	 * 
	 * @param cellsize
	 *            - the cellsize of the data
	 * @param xll
	 *            - the lower left x coordinate of the data
	 * @param yll
	 *            - the lower left y coordinate of the data
	 */
	public Raster(double cellsize, double xll, double yll) {
		this();
		setCellsize(cellsize);
		setXll(xll);
		setYll(yll);
	}

	/**
	 * Creates a raster from the given data
	 * 
	 * @param cellsize
	 *            - the cellsize of the data
	 * @param xll
	 *            - the lower left x coordinate of the data
	 * @param yll
	 *            - the lower left y coordinate of the data
	 * @param numRows
	 *            - the number of rows in the raster
	 * @param numCols
	 *            = the number of columns in the raster
	 */
	public Raster(double cellsize, double xll, double yll, int numRows,
			int numCols) {
		this();
		setCellsize(cellsize);
		setXll(xll);
		setYll(yll);
		setSize(numRows, numCols);
	}

	/**
	 * Creates a raster from the given data
	 * 
	 * @param data
	 * @param cellsize
	 * @param xll
	 * @param yll
	 */

	public Raster(double[][] data, double cellsize, double xll, double yll) {
		this(cellsize, xll, yll);
		setData(data);
	}

	/**
	 * Loads an ASCII raster from a file
	 * 
	 * @param rasterFile
	 *            - the File object to be loaded
	 */

	public Raster(File rasterFile) throws IOException {
		readFile(rasterFile);
	}

	/**
	 * Creates a raster from the given data using the dimensions provided and a
	 * cell size of 1.
	 * 
	 * @param minX
	 *            - the minimum x (horizontal) coordinate
	 * @param minY
	 *            - the minimum y (vertical) coordinate
	 * @param maxX
	 *            - the maximum x (horizontal) coordinate
	 * @param maxY
	 *            - the maximum y (vertical) coordinate
	 */

	public Raster(int minX, int minY, int maxX, int maxY) {
		this();
		setCellsize(1);
		setXll(minX);
		setYll(minY);
		setSize(maxY - minY + 1, maxX - minX + 1);
	}

	/**
	 * Creates a raster from the given data
	 * 
	 * @param data
	 * @param cellsize
	 * @param xll
	 * @param yll
	 */
	public Raster(int[][] data, double cellsize, double xll, double yll) {
		this(cellsize, xll, yll);
		setData(data);
	}

	/**
	 * Loads an ASCII raster from a file
	 * 
	 */

	public Raster(String file) throws IOException {
		readFile(new File(file));

	}

	/**
	 * Converts a column index value to its corresponding x coordinate.
	 * 
	 * @param col
	 *            - the index of the column value.
	 * @return the conversion of a column index value to its corresponding x
	 *         coordinate.
	 */

	public int colToX(int col) {
		return col + (int) xll;
	}

	public ImageView continuousImageView() {

		return continuousImageView(getCols(), getRows());
	}

	public ImageView continuousImageView(int width, int height) {

		WritableImage wr = new WritableImage(width, height);
		PixelWriter pw = wr.getPixelWriter();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int mapx = (int) (((double) x / (double) width) * getCols());
				int mapy = (int) (((double) y / (double) height) * getRows());
				double val = getValue(mapy, mapx);
				if (val == Double.NaN) {
					pw.setColor(x, y, Color.GREY);
				} else {
					pw.setColor(x, y, ColorUtils.jet(val, 0, 1));
				}
			}
		}

		ImageView iv = new ImageView();
		iv.setImage(wr);
		iv.setPreserveRatio(true);
		iv.setSmooth(false);
		iv.setCache(true);
		return iv;
	}

	public ImageView continuousImageView_byWidth() {

		return continuousImageView(getCols(), getRows());
	}

	public ImageView discreteImageView() {
		return discreteImageView(getCols(), getRows());
	}

	public ImageView discreteImageView(int width, int height) {
		
		WritableImage wr = new WritableImage(width, height);
		PixelWriter pw = wr.getPixelWriter();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int mapx = (int) (((double) x / (double) width) * getCols());
				int mapy = (int) (((double) y / (double) height) * getRows());
				pw.setArgb(x, y,
						RasterUtils.algoColour((int) getValue(mapy, mapx)));
			}
		}
		
		ImageView iv = new ImageView();
		iv.setImage(wr);
		iv.setPreserveRatio(true);
		iv.setSmooth(false);
		iv.setCache(true);
		return iv;
	}

	public ImageView discreteImageView_byWidth(int width) {
		double scale = (double) width/getCols();
		int height = (int) (scale * getRows());	
		return discreteImageView(width,height);
	}
	
	public ImageView continuousImageView_byWidth(int width) {
		double scale = (double) width/getCols();
		int height = (int) (scale * getRows());	
		return continuousImageView(width,height);
	}
	
	public ImageView discreteImageView_byHeight(int height) {
		double scale = (double) height/getRows();
		int width = (int) (scale * getCols());	
		return discreteImageView(width,height);
	}
	
	public ImageView continuousImageView_byHeight(int height) {
		double scale = (double) height/getRows();
		int width = (int) (scale * getCols());	
		return continuousImageView(width,height);
	}

	/**
	 * @return the cell size of the raster.
	 */

	public double getCellsize() {
		return cellsize;
	}

	/**
	 * @return the number of columns in the raster.
	 */

	public int getCols() {
		return cols;
	}

	/**
	 * @return - the underlying data array
	 */

	public double[][] getData() {
		return data;
	}

	/**
	 * @return the NoData value being used by the raster
	 */

	public String getNDATA() {
		return NDATA;
	}

	/**
	 * @return the number of rows in the raster.
	 */

	public int getRows() {
		return rows;
	}

	/**
	 * Retrieves the value at the given pair of row and index values.
	 * 
	 * @param row
	 *            the index of the row to be retrieved.
	 * @param column
	 *            the index of the column to be retrieved
	 * @return the value at the row/column index provided.
	 */

	public double getValue(int row, int column) {
		if (row < rows && column < cols && row >= 0 && column >= 0)
			return data[row][column];
		return Double.NaN;
	}

	/**
	 * @return the leftmost position of the raster.
	 */

	public double getXll() {
		return xll;
	}

	/**
	 * Returns the value at the given XY coordinate, assuming that x is relative
	 * to Xll and y is relative to Yll
	 * 
	 * @param x
	 *            - the x co-ordinate of the desired value
	 * @param y
	 *            - the y co-ordinate of the desired value
	 * @return - the raster value at the given coordinate pair.
	 */

	public double getXYValue(int x, int y) {
		return getValue(yToRow(y), xToCol(x));
	}

	public double getYll() {
		return yll;
	}

	/**
	 * Sets the parameters of this raster (rows, columns, corner, cellsize,
	 * NDATA etc) to be the same as the other raster. This includes initialising
	 * the data array with NDATAs
	 * 
	 * @param other
	 */
	public void init(Raster other) {
		xll = other.xll;
		yll = other.yll;
		cellsize = other.cellsize;
		NDATA = other.NDATA;
		setSize(other.getRows(), other.getCols());
	}

	/**
	 * Initialises the Raster to Double.NaN (i.e. NDATA)
	 */
	public void initData() {
		initData(Double.NaN);
	}

	/**
	 * Initialises the raster so the entire data array contains 'value'
	 * 
	 * @param value
	 */
	public void initData(double value) {
		data = new double[rows][];
		for (int i = 0; i < rows; i++) {
			data[i] = new double[cols];

			Arrays.fill(data[i], value);
		}
	}

	/**
	 * @param other
	 *            - the comparison raster
	 * @return whether the shape, position and cell size are consistent with
	 *         another raster
	 */

	public boolean isConsistent(Raster other) {
		if (this.rows == other.rows && this.cols == other.cols
				&& this.xll == other.xll && this.yll == other.yll
				&& this.cellsize == other.cellsize) {
			return true;
		}
		return false;
	}

	/**
	 * Parses a header line from an ASCII raster file - used since order cannot
	 * be guaranteed.
	 * 
	 * @param headerLine
	 * @return
	 */

	private String parseHeaderLine(String headerLine) {
		StringTokenizer stk = new StringTokenizer(headerLine, "= \t\n\r\f");
		String key = stk.nextToken();
		if (key.equalsIgnoreCase("nrows")) {
			rows = Integer.parseInt(stk.nextToken());
			return "nrows";
		}
		if (key.equalsIgnoreCase("ncols")) {
			cols = Integer.parseInt(stk.nextToken());
			return "ncols";
		}
		if (key.equalsIgnoreCase("xllcorner")) {
			xll = Double.parseDouble(stk.nextToken());
			return "xll";
		}
		if (key.equalsIgnoreCase("yllcorner")) {
			yll = Double.parseDouble(stk.nextToken());
			return "yll";
		}
		if (key.equalsIgnoreCase("cellsize")) {
			cellsize = Double.parseDouble(stk.nextToken());
			return "cellsize";
		}
		if (key.equalsIgnoreCase("nodata_value")) {
			NDATA = stk.nextToken();
			return "nodata";
		}
		return "";
	}

	/**
	 * Prints the raster to the console.
	 */

	public void print() {
		System.out.println("Rows: " + rows + " cols: " + cols + " cellsize "
				+ cellsize);
		for (double[] row : data) {
			for (double val : row)
				System.out.print(val + " ");
			System.out.println("");
		}

	}

	public void readFile(File rasterFile) throws IOException {
		boolean hasRows = false;
		boolean hasCols = false;
		boolean hasLLX = false;
		boolean hasLLY = false;
		boolean hasCellsize = false;

		try (BufferedReader br = new BufferedReader(new FileReader(rasterFile))) {
			for (int i = 0; i < 6; i++) {
				String var = parseHeaderLine(br.readLine());
				if (var.equalsIgnoreCase("nrows")) {
					hasRows = true;
				}
				;
				if (var.equalsIgnoreCase("ncols")) {
					hasCols = true;
				}
				;
				if (var.equalsIgnoreCase("xll")) {
					hasLLX = true;
				}
				;
				if (var.equalsIgnoreCase("yll")) {
					hasLLY = true;
				}
				;
				if (var.equalsIgnoreCase("cellsize")) {
					hasCellsize = true;
				}
			}

			if (!(hasRows && hasCols && hasLLX && hasLLY && hasCellsize)) {
				throw new IOException("Raster file " + rasterFile.getName()
						+ " does not contain appropriate header information.");
			}
		} catch (FileNotFoundException e) {
			ErrorHandler.showException(e);
		}

		try (BufferedReader br = new BufferedReader(new FileReader(rasterFile))) {

			data = new double[rows][cols];
			int rowct = 0;

			String ln = br.readLine();
			while (ln != null) {
				if (ln.toLowerCase().contains("nrows")) {
					ln = br.readLine();
					continue;
				}
				if (ln.toLowerCase().contains("ncols")) {
					ln = br.readLine();
					continue;
				}
				if (ln.toLowerCase().contains("xll")) {
					ln = br.readLine();
					continue;
				}
				if (ln.toLowerCase().contains("yll")) {
					ln = br.readLine();
					continue;
				}
				if (ln.toLowerCase().contains("cellsize")) {
					ln = br.readLine();
					continue;
				}
				if (ln.toLowerCase().contains("nodata")) {
					ln = br.readLine();
					continue;
				}

				StringTokenizer stk = new StringTokenizer(ln);
				int colct = 0;
				while (stk.hasMoreTokens()) {
					data[rowct][colct] = Double.parseDouble(stk.nextToken());
					colct++;
				}

				ln = br.readLine();
				rowct++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts a row index to a y coordinate value.
	 * 
	 * @param row
	 *            the index of the row
	 * @return the y-coordinate of the given row index.
	 */

	public int rowToY(int row) {
		return (int) yll - (row + 1 - rows);
	}

	/**
	 * Sets the cell size of the raster.
	 * 
	 * @param cellsize
	 *            - the cell size of the raster
	 */

	public void setCellsize(double cellsize) {
		this.cellsize = cellsize;
	}

	/**
	 * Sets the number of columns of the raster.
	 * 
	 * @param cols
	 *            - the number of columns
	 */

	public void setCols(int cols) {
		this.cols = cols;
	}

	/**
	 * Copies the given data into the underlying data array. Also updates the
	 * number of rows and columns.
	 * 
	 * @param data
	 */
	public void setData(double[][] data) {
		rows = data.length;
		cols = data[0].length;
		initData();
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				this.data[i][j] = data[i][j];
	}

	/**
	 * Copies the given data into the underlying data array. Also updates the
	 * number of rows and columns.
	 * 
	 * @param data
	 */
	public void setData(int[][] data) {
		rows = data.length;
		cols = data[0].length;
		initData();
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < cols; j++)
				this.data[i][j] = data[i][j];
	}

	/**
	 * Sets the NoData value.
	 * 
	 * @param nDATA
	 */

	public void setNDATA(String nDATA) {
		NDATA = nDATA;
	}

	/**
	 * Sets the number of rows of the raster
	 * 
	 * @param rows
	 */

	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * Sets the size of the raster, and also initializes the array with NDATA
	 * 
	 * @param nrows
	 * @param columns
	 */
	public void setSize(int nrows, int columns) {
		this.rows = nrows;
		this.cols = columns;
		initData();
	}

	/**
	 * Sets the cell value for a given row and column.
	 * 
	 * @param row
	 *            - the row index
	 * @param column
	 *            - the column index
	 * @param value
	 *            - the value to which the cell will be set
	 */

	public void setValue(int row, int column, double value) {
		if (row < rows && column < cols)
			data[row][column] = value;
	}

	/**
	 * Sets the leftmost position of the raster
	 * 
	 * @param xll
	 */

	public void setXll(double xll) {
		this.xll = xll;
	}

	/**
	 * Sets the cell value using an xy coordinate pair.
	 * 
	 * @param x
	 *            - the x-coordinate value
	 * @param y
	 *            - the y-coordinate value
	 * @param val
	 *            - the value to which the cell will be set
	 */

	public void setXYValue(int x, int y, double val) {
		setValue(yToRow(y), xToCol(x), val);
	}

	/**
	 * Sets the bottom-most position of the rater
	 * 
	 * @param yll
	 */

	public void setYll(double yll) {
		this.yll = yll;
	}

	/**
	 * Converts an x-coordinate to a column index value.
	 * 
	 * @param x
	 *            the x-coordinate value.
	 * @return the column index corresponding to the x-coordinate value.
	 */

	public int xToCol(int x) {
		return x - (int) xll;
	}

	/**
	 * Converts a y coordinate to a row index value.
	 * 
	 * @param y
	 *            the y-coordinate value.
	 * @return the row index corresponding to the y-coordinate value.
	 */

	public int yToRow(int y) {
		return (rows - (y - (int) yll)) - 1;
	}
}