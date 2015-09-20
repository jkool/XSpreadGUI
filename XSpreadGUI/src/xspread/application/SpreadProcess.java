/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package xspread.application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import xspread.impl.Disperser_Continuous2D;
import xspread.impl.RasterMosaic;
import xspread.impl.output.ExperimentWriter_Text;
import xspread.impl.output.MosaicWriter_Raster;
import xspread.impl.output.StatsWriter_Text;
import xspread.impl.process.Process_Containment;
import xspread.impl.process.Process_Costing;
import xspread.impl.process.Process_Dispersal;
import xspread.impl.process.Process_GroundControl;
import xspread.impl.process.Process_Growth;
import xspread.impl.process.Process_Infestation;
import xspread.impl.process.Process_Monitor;
import xspread.impl.random.RandomGenerator_Exponential;
import xspread.impl.random.RandomGenerator_Kernel;
import xspread.impl.random.RandomGenerator_Poisson;
import xspread.impl.random.RandomGenerator_Uniform;
import xspread.postprocess.CalibrationAnalysis;

/**
 * Principal class and entry point for running the SPREAD model. This class is
 * mainly used to interpret input from the Properties file, set of the requisite
 * objects, and generate multiple Experiments according to the input parameters.
 */

@SuppressWarnings("rawtypes")
public class SpreadProcess extends Task {

	private Mosaic mosaic;
	private MosaicWriter mosaicWriter;
	private SpreadProperties sp;
	private DoubleProperty distProgress = new SimpleDoubleProperty(0);
	private DoubleProperty rateProgress = new SimpleDoubleProperty(0);
	private DoubleProperty pairProgress = new SimpleDoubleProperty(0);
	private StringProperty distProgressText = new SimpleStringProperty("Starting...");
	private StringProperty rateProgressText = new SimpleStringProperty("Starting...");
	private StringProperty pairProgressText = new SimpleStringProperty("Starting...");
	private String status = "";
	private int distance_rep = 1;
	private int rate_rep = 1;
	private int pair_set = 1;
	private int replicate = 1;
	private boolean finished = false;
	private CalibrationAnalysis ca;

	public SpreadProcess(SpreadProperties sp) {
		this.sp = sp;
		updateProgress(0.0,1.0);
		
	}

	/**
	 * Initializes required objects - e.g. the Mosaic, OutputWriters etc.
	 */	

	@Override
	public Boolean call() throws IOException {

		// Set up the Mosaic (currently only implemented as a raster)
		mosaic = new RasterMosaic();
		Mosaic reference = new RasterMosaic();

		List<String> spList = sp.getSpeciesList();

		mosaic.setSpeciesList(spList);
		reference.setSpeciesList(spList);

		// We loop through rather than setting the map directly because some
		// string keyword
		// parsing is necessary when setting up the maps (e.g. ALL, NONE).

		for (int i = 0; i < spList.size(); i++) {
			String species = spList.get(i);
			mosaic.setPresenceMap(sp.getPresenceFiles().get(species), species);
			mosaic.setAgeMap(sp.getAgeFiles().get(species), species);
			mosaic.setHabitatMap(sp.getHabitatFiles().get(species), species);
			mosaic.setManagementMap(sp.getManagementFiles().get(species),
					species);

			reference.setPresenceMap(sp.getReferenceFiles().get(species),
					species);
		}
		
		if(mosaic.getPatches().isEmpty()){
			throw new IOException("No patches for processing.");
		}

		mosaicWriter = new MosaicWriter_Raster();

		// Set the output root folder

		String outputFolder = sp.getOutputFolder();
		mosaicWriter.setFolder(outputFolder);

		ExperimentWriter_Text ew = new ExperimentWriter_Text();
		StatsWriter_Text sw = new StatsWriter_Text();
		
		ew.setReferenceMosaic(reference);

		if (sp.isOverwritingOutput()) {
			File f = new File(outputFolder);
			if (!f.exists()) {
				f.mkdirs();
			}
		}

		ew.setWriteCoverMaps(sp.isWritingCoverMaps());
		ew.setWriteStageMaps(sp.isWritingStageMaps());
		ew.setWriteMonitoredMaps(sp.isWritingMonitoredMaps());
		ew.setWriteRasterHeader(sp.isWritingRasterHeader());

		// If we explicitly say to not save the properties file, but one
		// exists in the same location, then delete it to avoid confusion.
		if (!sp.isSavingPropertiesToFile()) {
			File f = new File(outputFolder + "/" + "properties.txt");
			if (f.exists()) {
				f.delete();
			}
		}

		else {
			sp.writeTextFile(new File(outputFolder + "/" + "properties.txt"));
		}

		String outputFile = sp.getOutputFile();
		String outputPath = outputFolder + "/" + outputFile;

		ew.setOutputFolder(outputFolder);
		ew.setOutputFile(outputFile);

		if (!sp.isOverwritingOutput()) {
			if (new File(outputPath).exists()) {
				throw new IOException(
						"Overwrite is currently disabled, but output file "
								+ outputPath
								+ " already exists. Please check the file, or set Overwrite_output as True in the properties file.");
			}
		}

		try {
			ew.open(new TreeSet<String>(spList));
		} catch (IOException e2) {
			throw new IOException(
					"Output file "
							+ ew.getOutputFile()
							+ " could not be accessed for writing.  Please check the path exists, that the file is not in use, and that you have write permission.");
		}

		int reps = sp.getReplicates();
		long startTime = sp.getStartTime();
		long timeIncrement = sp.getStepInterval();
		long endTime = sp.getEndTime();

		if (endTime <= startTime) {
			throw new IllegalArgumentException("ERROR:  End time (" + endTime
					+ ") must be greater than the start time(" + startTime
					+ ")");
		}

		if (timeIncrement <= 0) {
			throw new IllegalArgumentException("ERROR:  Time increment ("
					+ timeIncrement + ") must be greater than zero.");
		}

		ew.setWriteFrequencyMap(sp.isWritingFrequencyMap());

		// Adding steps to the process chain. This makes it easy to
		// add additional steps interactively and switch order.

		List<Process> processes = new ArrayList<Process>();

		// Adding growth

		Process_Growth pg = new Process_Growth();
		pg.setThresholds(sp.getAge_stage());
		mosaic.updateInfestationStages(sp.getAge_stage());

		// Adding dispersal

		Process_Dispersal pd = new Process_Dispersal();
		pd.addToCoreControlList(sp.getCoreControl());
		pd.setWaitTimes(sp.getWaitTime());

		// Adding monitoring

		Process_Monitor pm = new Process_Monitor();
		pm.setContainmentCutoff(sp.getContainmentCutoff());
		pm.setCoreBufferSize(sp.getCoreBufferSize());
		pm.setCheckFrequency(sp.getManagementFrequency());
		pm.addToGroundControlIgnore(sp.getGroundControlIgnore());
		pm.addToContainmentIgnore(sp.getContainmentIgnore());
		pm.setPDiscovery(sp.getP_detection());

		// Adding ground control actions

		Process_GroundControl pgc = new Process_GroundControl();
		pgc.setCheckFrequency(sp.getManagementFrequency());
		pgc.addToIgnoreList(sp.getGroundControlIgnore());
		pgc.addToCoreControlList(sp.getCoreControl());

		// Adding containment actions

		Process_Containment pcc = new Process_Containment();
		pcc.setCheckFrequency(sp.getManagementFrequency());
		pcc.addToIgnoreList(sp.getContainmentIgnore());

		// Adding cost accounting

		Process_Costing pcst = new Process_Costing();
		pcst.setCheckFrequency(sp.getManagementFrequency());

		pcst.setContainmentCost(sp.getContainmentCost());
		pcst.setContainmentLabour(sp.getContainmentLabour());
		pcst.setGroundControlCosts(sp.getGroundControlCost());
		pcst.setGroundControlLabour(sp.getGroundControlLabour());

		processes.add(pm);
		processes.add(pgc);
		processes.add(pcc);
		processes.add(pcst);
		processes.add(pg);
		processes.add(pd);

		// Adding infestation step

		processes.add(new Process_Infestation());

		boolean writeTrace = sp.isWritingTrace();

		// If the Run-type is Paired, then the arrays of distances and rates are
		// run as a paired set, therefore there is only one loop.

		Map<String, double[]> distances = sp.getDistances();
		Map<String, double[]> rates = sp.getRates();

		updateMessage("Starting run...");
		int ndistances = distances.get(spList.get(0)).length;
		
		sw.setOutputFolder(outputFolder);
		String sw2_output = sp.getTraceBaseName();
		sw.setOutputFile(sw2_output);
		sw.open(new HashSet<String>(spList));

		if (sp.getRunType().equalsIgnoreCase("Paired")) {

			pair_set = 1;

			// Since species are co-running we assume that array lengths are the
			// same for all species
			for (int i = 0; i < ndistances; i++) {
				if(isCancelled()){return false;}

				System.out.println("Processing pair set " + (i + 1) + " of "
						+ ndistances);

				pairProgress.set((double) (i + 1)
						/ (double) ndistances);
				
				class PairRunnable implements Runnable{int i = 0; PairRunnable(int i){this.i=i;}@Override
				public void run(){
					pairProgressText.set("Processing pair " + (i+1) + " of " + ndistances);
				}}
				
				Platform.runLater(new PairRunnable(i));

				for (int j = 0; j < spList.size(); j++) {
					if (isCancelled()){return false;}
					
					Disperser_Continuous2D dc2 = new Disperser_Continuous2D();
					RandomGenerator_Exponential distanceGenerator = new RandomGenerator_Exponential();
					distanceGenerator
							.setLambda(1 / distances.get(spList.get(j))[i]);
					RandomGenerator angleGenerator = new RandomGenerator_Uniform();
					RandomGenerator_Kernel rk = new RandomGenerator_Kernel();
					if (sp.hasChangedDirectionArray()) {
						rk.setRotate(true);
						rk.setWeights(sp.getDirections().get(spList.get(j)));
						angleGenerator = rk;
					}

					RandomGenerator_Poisson numberGenerator = new RandomGenerator_Poisson();
					numberGenerator.setLambda(rates.get(spList.get(j))[i]);

					dc2.setAngleGenerator(angleGenerator);
					dc2.setDistanceGenerator(distanceGenerator);
					dc2.setNumberGenerator(numberGenerator);

					mosaic.setDisperser(spList.get(j), dc2);
				}

				replicate = 1;

				for (int n = 0; n < reps; n++) {
					if (sp.isPrintingReplicates()) {
						System.out.println("\t\tReplicate " + (n + 1) + " of "
								+ reps);
					}

					Experiment e = new Experiment();

					double[] dist_vec = new double[spList.size()];
					double[] rate_vec = new double[spList.size()];

					for (int j = 0; j < spList.size(); j++) {
						dist_vec[j] = distances.get(spList.get(j))[i];
						rate_vec[j] = rates.get(spList.get(j))[i];
					}

					if (writeTrace) {
						//sw = new StatsWriter_Text();
						//sw.setOutputFolder(outputFolder);
						//sw.setDistances(dist_vec);
						//sw.setRates(rate_vec);
						//sw.setReplicate(n);
						
						sw.setDistances(dist_vec);
						sw.setRates(rate_vec);
						sw.setReplicate(n);
						
						//String sw_output = sp.getTraceBaseName() + "_" + i
						//		+ "_" + n;
						//sw.setOutputFile(sw_output);
						//try {
							//sw.open(new HashSet<String>(spList));
							//sw.setRunID(i * distances.get(spList.get(0)).length
							//		+ n);
							sw.setRunID(i * distances.get(spList.get(0)).length
									+ n);
						//} catch (IOException e1) {
						//	System.out
						//			.println("Could not write statistics to trace file "
						//					+ outputFolder
						//					+ "/"
						//					+ sw_output
						//					+ ".  Skipping.");
						//	continue;
						//}
						e.setStatsWriter(sw);
						e.writeTraceFile(writeTrace);
					}

					e.setMosaic(mosaic.clone());
					e.setStartTime(startTime);
					e.setTimeIncrement(timeIncrement);
					e.setEndTime(endTime);
					mosaicWriter.setWriteHeader(sp.isWritingRasterHeader());
					e.setOutputWriter(mosaicWriter);
					e.setProcesses(processes);
					e.writeEachTimeStep(sp.isWritingEachTimeStep());
					e.writeEachMgtStep(sp.isWritingEachMgtStep());

					int id = (i * reps) + n;

					e.setIdentifier(id + "_" + n);

					ew.setDistances(dist_vec);
					ew.setRates(rate_vec);
					ew.setReplicate(n);
					ew.setID(id);

					e.setExperimentWriter(ew);

					e.run();

					pcst.resetCost();
					pcst.resetLabour();

					//if (writeTrace) {
					//	sw.close();
					//}
					rate_rep++;
				}
				updateMessage("Processed pair " + (i + 1) + " of "
						+ distances.get(spList.get(0)).length);
				updateProgress((i + 1), distances.get(spList.get(0)).length);
				pair_set++;
			}
			sw.close();
			ew.close();
		}

		// Otherwise, we perform a calibration-type run where permutations of
		// the distance and rate arrays are used.
		// This is the default setting.

		else {

			distance_rep = 1;
			rate_rep = 1;

			for (int i = 0; i < ndistances; i++) {
				if(isCancelled()){return false;}
				System.out.println("Processing distance class " + (i + 1)
						+ " of " + ndistances);

				distProgress.set((double) (i + 1)
						/ (double) ndistances);
				
				class DistRunnable implements Runnable{int i = 0; DistRunnable(int i){this.i=i;}@Override
				public void run(){
					distProgressText.set("Processing distance class " + (i+1) + " of " + ndistances);
				}}
				
				Platform.runLater(new DistRunnable(i));

				for (int j = 0; j < rates.get(spList.get(0)).length; j++) {
					
					int nrates = rates.get(spList.get(0)).length;
					if(isCancelled()){return false;}
					System.out.println("\tProcessing rate class " + (j + 1)
							+ " of " + nrates);
					
					rateProgress.set((double) (j + 1)
							/ (double) nrates);
					
					class RateRunnable implements Runnable{int j = 0; RateRunnable(int j){this.j=j;}@Override
					public void run(){
						rateProgressText.set("Processing rate class " + (j+1) + " of " + nrates);
					}}
					
					Platform.runLater(new RateRunnable(j));
					
					for (int k = 0; k < spList.size(); k++) {
						
						if(isCancelled()){return false;}

						Disperser_Continuous2D dc2 = new Disperser_Continuous2D();
						RandomGenerator_Exponential distanceGenerator = new RandomGenerator_Exponential();
						distanceGenerator.setLambda(1 / distances.get(spList
								.get(k))[i]);
						// RandomGenerator_Determined distanceGenerator = new
						// RandomGenerator_Determined();
						// distanceGenerator.setValue(1);
						// /////////////////////////////////////////////////////////////////////////////////////////////////////
						// Change to options!
						RandomGenerator angleGenerator = new RandomGenerator_Uniform();
						RandomGenerator_Kernel rk = new RandomGenerator_Kernel();
						if (sp.hasChangedDirectionArray()) {
							rk.setRotate(true);
							rk.setWeights(sp.getDirections().get(spList.get(k)));
							angleGenerator = rk;
						}

						RandomGenerator_Poisson numberGenerator = new RandomGenerator_Poisson();
						numberGenerator.setLambda(rates.get(spList.get(k))[j]);

						dc2.setAngleGenerator(angleGenerator);
						dc2.setDistanceGenerator(distanceGenerator);
						dc2.setNumberGenerator(numberGenerator);

						mosaic.setDisperser(spList.get(k), dc2);
					}

					replicate = 1;

					for (int n = 0; n < reps; n++) {
						
						if(isCancelled()){return false;}
						
						if (sp.isPrintingReplicates()) {
							System.out.println("\t\tReplicate " + (n + 1)
									+ " of " + reps);
						}

						Experiment e = new Experiment();

						double[] dist_vec = new double[spList.size()];
						double[] rate_vec = new double[spList.size()];

						for (int k = 0; k < spList.size(); k++) {
							dist_vec[k] = distances.get(spList.get(k))[i];
							rate_vec[k] = rates.get(spList.get(k))[j];
						}

						if (writeTrace) {
						//	sw = new StatsWriter_Text();
						//	sw.setOutputFolder(outputFolder);
						//	sw.setDistances(dist_vec);
						//	sw.setRates(rate_vec);
						//	sw.setReplicate(n);
							
							sw.setDistances(dist_vec);
							sw.setRates(rate_vec);
							sw.setReplicate(n);
							
						//	String sw_output = sp.getTraceBaseName() + "_" + i
						//			+ "_" + j + "_" + n;
						//	sw.setOutputFile(sw_output);
						//	try {
						//		sw.open(new HashSet<String>(spList));
						//		sw.setRunID(i
						//				* distances.get(spList.get(0)).length
						//				+ j * (rates.get(spList.get(0)).length)
						//				+ n);
								
								sw.setRunID(i
										* distances.get(spList.get(0)).length
										+ j * (rates.get(spList.get(0)).length)
										+ n);
						//	} catch (IOException e1) {
						//		System.out
						//				.println("Could not write statistics to trace file "
						//						+ outputFolder
						//						+ "/"
						//						+ sw_output + ".  Skipping.");
						//		continue;
						//	}
							e.setStatsWriter(sw);
							e.writeTraceFile(writeTrace);
						}

						e.setMosaic(mosaic.clone());
						e.setStartTime(startTime);
						e.setTimeIncrement(timeIncrement);
						e.setEndTime(endTime);
						mosaicWriter.setWriteHeader(sp.isWritingRasterHeader());
						e.setOutputWriter(mosaicWriter);
						e.setProcesses(processes);
						e.writeEachTimeStep(sp.isWritingEachTimeStep());
						e.writeEachMgtStep(sp.isWritingEachMgtStep());

						int id = (i*nrates*reps)+(j*reps)+n;

						e.setIdentifier(id + "_" + n);

						ew.setDistances(dist_vec);
						ew.setRates(rate_vec);
						ew.setReplicate(n);
						ew.setID(id);

						e.setExperimentWriter(ew);

						e.run();

						pcst.resetCost();
						pcst.resetLabour();

						//if (writeTrace) {
						//	sw.close();
						//}
					}
					rate_rep++;
				}
				//updateMessage("Processed distance " + (i + 1) + " of "
				//		+ distances.get(spList.get(0)).length);
				//updateProgress((i + 1), distances.get(spList.get(0)).length);
				distance_rep++;
			}

			sw.close();
			ew.close();

			// Post-process calibration results
			if(isCancelled()){return false;}
			ca = new CalibrationAnalysis();
			ca.setCriterionField(sp.getCriterionField());
			ca.setErrorCutoff(sp.getErrorCutoff());
			ca.setErrorType(sp.getErrorType());
			ca.setPercentile(sp.getPercentile());
			ca.setPerformanceCriterion(sp.getPerformanceCriterion());
			ca.setOutputFolder(outputFolder);
			ca.setStatsTable(outputFile);
			ca.setSpeciesList(spList);
			ca.go();
		}

		System.out.println("\nComplete.");
		finished = true;
		return finished;
	}

	public CalibrationAnalysis getCalibration(){
		return ca;
	}

	public int getDistance_rep() {
		return distance_rep;
	}

	public StringProperty getDistProgressText() {
		return distProgressText;
	}

	public DoubleProperty getDistProp(){
		return distProgress;
	}

	public int getPair_set() {
		return pair_set;
	}

	public int getRate_rep() {
		return rate_rep;
	}

	public StringProperty getRateProgressText() {
		return rateProgressText;
	}

	public DoubleProperty getRateProp(){
		return rateProgress;
	}
	
	public StringProperty getPairProgressText() {
		return pairProgressText;
	}

	public DoubleProperty getPairProp(){
		return pairProgress;
	}

	public int getReplicate() {
		return replicate;
	}

	public String getStatus() {
		return status;
	}

	public boolean isFinished() {
		return finished;
	}
	
	public void setDistance_rep(int distance_rep) {
		this.distance_rep = distance_rep;
	}
	public void setPair_set(int pair_set) {
		this.pair_set = pair_set;
	}
	public void setRate_rep(int rate_rep) {
		this.rate_rep = rate_rep;
	}
	public void setReplicate(int replicate) {
		this.replicate = replicate;
	}

	/**
	 * Performs any necessary clean-up on the mosaic (e.g. closing files).
	 */

	public void shutdown() {
		mosaic.shutdown();
	}
	
	
}