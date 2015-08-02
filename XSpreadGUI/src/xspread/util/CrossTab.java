/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/

package xspread.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class CrossTab {

	private Table<Double, Double, Long> table = HashBasedTable.create();
	private boolean writeLabels = true;
	private String sp = ",";
	
	public static void main(String[] args){
		if(args==null || !(args.length == 1 || args.length==3)){
			System.out.println("Usage: <list file> | OR | <zonal raster> <value raster> <output file>");
			System.exit(-1);
		}
		
		CrossTab ct = new CrossTab();
		
		if(args.length==1){
			ct.readFile(args[0]);
		}
		
		else{
		
			
		File a = new File(args[0]);
		File b = new File(args[1]);
		
		if(!a.exists()){
			System.out.println("File " + a + " does not exist.  Exiting.");
			System.exit(-1);
		}
		
		if(!b.exists()){
			System.out.println("File " + b + " does not exist.  Exiting.");
			System.exit(-1);
		}
			
		Raster zoneRaster = null; 
		Raster valRaster = null; 
		
		try {
			zoneRaster = new Raster(a);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			valRaster = new Raster(b);
		} catch (IOException e) {
			e.printStackTrace();
		}

		ct.crossTab(zoneRaster, valRaster);
		ct.writeToFile(new File(args[2]));
		}
		
		System.out.println("\nComplete. " + new Date(System.currentTimeMillis()));
	}
	
	public void readFile(String path){
		try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))){
			String ln = br.readLine();
			while(ln!=null){
				StringTokenizer stk = new StringTokenizer(ln,",\t\n\f\r");
				if(stk.countTokens()!=3){
					System.out.println("Input row must have three entries - reference, comparison and output file.  " + stk.countTokens() + "found: " + ln);
				}
				
				String reference = stk.nextToken();
				String comparison = stk.nextToken();
				String output = stk.nextToken();
				
				table.clear();
				crossTab(new Raster(reference),new Raster(comparison));
				writeToFile(new File(output));
				System.out.println("Cross-tabulation of " + reference + " and " + comparison + " written to " + output + ".");
				ln=br.readLine();
			}
			
		} catch (FileNotFoundException e) {
			System.out.println("File " + path + "was not found.  Exiting.");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Error reading " + path + " (I/O error).  Exiting.");
			System.exit(-1);
		}
	}

	public void crossTab(Raster zoneRaster, Raster valRaster) {

		for (int i = 0; i < zoneRaster.getRows(); i++) {
			for (int j = 0; j < zoneRaster.getCols(); j++) {
				double zone = zoneRaster.getValue(i, j);
				double val = valRaster.getValue(i, j);
				if (!table.contains(zone, val)) {
					table.put(zone, val, 1l);
				} else {
					table.put(zone, val, table.get(zone, val) + 1l);
				}
			}
		}
	}

	public boolean checkConsistency(Raster zoneRaster, Raster valRaster) {
		if (!zoneRaster.isConsistent(valRaster)) {
			System.out.println("Raster dimensions are inconsistent.");
			System.out.println("\tZone raster:" + "nrows:" + zoneRaster.getRows()
					+ " ncols:" + zoneRaster.getCols() + " llx:" + zoneRaster.xll
					+ " lly:" + zoneRaster.yll + " cellsize:"
					+ zoneRaster.cellsize);
			System.out.println("\tValue raster:" + "nrows:" + valRaster.getRows()
					+ " ncols:" + valRaster.getCols() + " llx:" + valRaster.xll
					+ " lly:" + valRaster.yll + " cellsize:"
					+ valRaster.cellsize);
			System.out.println("WARNING: Values were not tabulated.");
			return false;
		}
		return true;
	}

	public void clearTable() {
		table.clear();
	}

	public Set<Double> getRows() {
		return table.rowKeySet();
	}

	public Set<Double> getCols() {
		return table.columnKeySet();
	}

	public void writeToFile(File outputFile) {
		try (FileWriter fw = new FileWriter(outputFile)) {
			
			TreeSet<Double> sortcols = new TreeSet<Double>(table.columnKeySet());
			
			if (writeLabels) {
				StringBuilder sb = new StringBuilder();
				sb.append("ZONE" + sp);
				for (Double vals : sortcols) {
					sb.append(vals + sp);
				}
				fw.write(sb.toString().substring(0, sb.length() - sp.length()) + "\n");
			}

			TreeSet<Double> sortrows = new TreeSet<Double>(table.rowKeySet());
			
			for (Double zone : sortrows) {
				StringBuilder sb = new StringBuilder();
				if (writeLabels) {
					sb.append(zone + sp);
				}
				
				for (Double vals : sortcols) {
					Long val = table.get(zone, vals);
					sb.append((val==null?0:val) + sp);
				}
				fw.write(sb.toString().substring(0, sb.length() - sp.length()) + "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeLabels(boolean writeLabels) {
		this.writeLabels = writeLabels;
	}
}
