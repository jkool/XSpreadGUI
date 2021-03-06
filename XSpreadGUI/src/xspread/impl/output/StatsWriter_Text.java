/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package xspread.impl.output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import xspread.application.Experiment;
import xspread.application.Mosaic;
import xspread.application.Process;
import xspread.application.StatsWriter;
import xspread.impl.process.Process_Costing;
import xspread.util.ControlType;

/**
 * Used to write Experiment-level output to output files, including number of
 * infested cells, Kappa statistics and end-of-run Raster output.
 */

public class StatsWriter_Text implements StatsWriter {

	private double[] distances;
	private double[] rates;
	private Number replicate = Double.NaN;
	private String outputFolder;
	private String outputFile;
	private Map<String, BufferedWriter> bw_map = new TreeMap<String, BufferedWriter>();;
	private boolean writeTableHeader = true;
	private int runID = -1;

	/**
	 * Close down the output resources.
	 */
	@Override
	public void close() {

		Iterator<String> it = bw_map.keySet().iterator();
		while (it.hasNext()) {
			try {
				String next = it.next();
				BufferedWriter bw = bw_map.get(next);
				bw.flush();
				bw.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		bw_map.clear();
	}


	/**
	 * Sets up and opens the output files for writing
	 * 
	 * @throws IOException
	 */

	@Override
	public void open(Set<String> speciesList) throws IOException {
		Iterator<String> it = speciesList.iterator();

		while (it.hasNext()) {
			String species = it.next();
			bw_map.put(species, new BufferedWriter(new FileWriter(outputFolder
					+ "/" + species + "_" + outputFile + ".csv")));

			if (writeTableHeader) {
				StringBuilder sb = new StringBuilder();
				sb.append("RunID,Time,Distance,Rate,Replicate,");

				//sb.append("N_infested,K_no,K_Allocation,K_quantity,K_histo,K_standard,Chance_agreement,Quantity_agreement,Allocation_agreement, Allocation_disagreement,Quantity_disagreement");
				sb.append("S_infested,P_infested,S_Undetected,Ground,Containment,Core,Containment_sum,Ndata,Cost,Labour");
				sb.append("\n");
				bw_map.get(species).write(sb.toString());
			}
		}
	}
	
	/**
	 * Writes an experiment to output - i.e. a single line in the output table
	 * and/or raster output.
	 * 
	 * @param exp
	 *            - The experiment whose attributes are to be written to output.
	 */

	@Override
	public void write(Experiment exp) {

		Mosaic mosaic = exp.getMosaic();
		List<Process> plist = exp.getProcesses();
		int idx = -1;
		for(int i = 0; i < plist.size(); i++){
			if(plist.get(i) instanceof Process_Costing){
				idx = i;
				break;
			}
		}
		
		Process_Costing pcst = (Process_Costing) plist.get(idx);
		
		List<String> speciesList = mosaic.getSpeciesList();

		for (int i = 0; i < speciesList.size(); i++) {

			String species = speciesList.get(i);

			StringBuilder sb = new StringBuilder();
			sb.append(runID+",");
			sb.append(exp.getTime() + ",");
			sb.append(distances[i] + ",");
			sb.append(rates[i] + ",");
			sb.append((replicate.intValue()+1) + ",");

			// generate statistics

			sb.append(mosaic.getNumberInfestations(species) + ",");
			sb.append(mosaic.getNumberInfestedPatches() + ",");
			sb.append(mosaic.getNumberUndetected(species) + ",");
			mosaic.updateControlTable();
			
			int gc = mosaic.getControlTable().get(species).get(ControlType.GROUND_CONTROL);
			int cont = mosaic.getControlTable().get(species).get(ControlType.CONTAINMENT);
			int core = mosaic.getControlTable().get(species).get(ControlType.CONTAINMENT_CORE);
			
			//int gc = mosaic.getControlled(species, ControlType.GROUND_CONTROL).size();
			//int cont = mosaic.getControlled(species, ControlType.CONTAINMENT).size();
			//int core = mosaic.getControlled(ControlType.CONTAINMENT_CORE).size();
			
			sb.append(gc + ",");
			sb.append(cont + ",");
			sb.append(core+",");
			sb.append((cont+core) + ",");
			sb.append(mosaic.getNumberNoData() + ",");
			
			sb.append(pcst.getCost(mosaic) + ",");
			sb.append(pcst.getLabour(mosaic));
			sb.append("\n");

			try {
				bw_map.get(species).write(sb.toString());
				bw_map.get(species).flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	// Getters and setters

	public String getOutputFile() {
		return outputFile;
	}

	public String getOutputFolder() {
		return outputFolder;
	}
	
	public void setDistances(double[] distances) {
		this.distances = distances;
	}

	@Override
	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	@Override
	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void setRates(double[] rates) {
		this.rates = rates;
	}

	public void setReplicate(Number replicate) {
		this.replicate = replicate;
	}

	public void setRunID(int runID){
		this.runID=runID;
	}
	
	public void setWriteTableHeader(boolean writeTableHeader) {
		this.writeTableHeader = writeTableHeader;
	}
}

