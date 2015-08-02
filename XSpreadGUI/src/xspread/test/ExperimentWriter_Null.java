/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package xspread.test;

import java.io.IOException;
import java.util.Set;

import xspread.application.Experiment;
import xspread.application.ExperimentWriter;

/**
 * Empty ExperimentWriter class for unit testing.
 */

public class ExperimentWriter_Null implements ExperimentWriter {
	
	@Override
	public void close(){}
	@Override
	public void open(Set<String> speciesList) throws IOException{}
	@Override
	public void write(Experiment exp){}

}
