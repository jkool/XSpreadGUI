/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package xspread.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class RasterWriter {
	private boolean writeHeader = true;

	public void setWriteHeader(boolean writeHeader) {
		this.writeHeader = writeHeader;
	}

	public void writeRaster(String filename, double[][] data, double xll,
			double yll, double size, String ndata) throws IOException {
		writeRaster(filename, Raster.getTempRaster(data, xll, yll, size, ndata));
	}
	
	public void writeIntegerRaster(String filename, double[][] data, double xll,
			double yll, double size, String ndata) throws IOException {
		writeIntegerRaster(filename, Raster.getTempRaster(data, xll, yll, size, ndata));
	}

	public void writeRaster(String filename, Raster r) throws IOException {
		File f = new File(filename);
		if (f.exists())
			f.delete();
		if (!f.createNewFile())
			System.err.println("Could not create file.");

		PrintStream o = new PrintStream(f);

		if (writeHeader) {
			o.println("ncols " + r.getCols());
			o.println("nrows " + r.getRows());
			o.println("xllcorner " + r.getXll());
			o.println("yllcorner " + r.getYll());
			o.println("cellsize " + r.getCellsize());
			o.println("NODATA_value " + r.getNDATA());
		}

		for (double[] row : r.getData()) {
			StringBuffer b = new StringBuffer();
			for (int i = 0; i < row.length; i++) {
				if (Double.isNaN(row[i]))
					b.append(r.getNDATA());
				else
					b.append(row[i]);
				if (i < row.length - 1)
					b.append(" ");
			}
			o.println(b);
		}
		o.close();
	}
	
	public void writeIntegerRaster(String filename, Raster r) throws IOException {
		File f = new File(filename);
		if (f.exists())
			f.delete();
		if (!f.createNewFile())
			System.err.println("Could not create file.");

		PrintStream o = new PrintStream(f);

		if (writeHeader) {
			o.println("ncols " + r.getCols());
			o.println("nrows " + r.getRows());
			o.println("xllcorner " + r.getXll());
			o.println("yllcorner " + r.getYll());
			o.println("cellsize " + r.getCellsize());
			o.println("NODATA_value " + r.getNDATA());
		}

		for (double[] row : r.getData()) {
			StringBuffer b = new StringBuffer();
			for (int i = 0; i < row.length; i++) {
				if (Double.isNaN(row[i]))
					b.append(r.getNDATA());
				else
					b.append((int) row[i]);
				if (i < row.length - 1)
					b.append(" ");
			}
			o.println(b);
		}
		o.close();
	}
}
