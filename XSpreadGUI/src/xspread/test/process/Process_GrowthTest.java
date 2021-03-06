/*******************************************************************************
 * Copyright Charles Darwin University 2014. All Rights Reserved.  
 * For review only, not for distribution.
 *******************************************************************************/
package xspread.test.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import xspread.application.Patch;
import xspread.impl.RasterMosaic;
import xspread.impl.process.Process_Growth;

public class Process_GrowthTest {

	RasterMosaic rm = new RasterMosaic();
	Process_Growth pg = new Process_Growth();
	String species = "Test_1";

	@Before
	public void setup() {
		List<String> speciesList = new ArrayList<String>();
		speciesList.add("Test_1");
		speciesList.add("Test_2");
		rm.setSpeciesList(speciesList);
		
		Map<String,long[]> thresholds = new TreeMap<String,long[]>();
		thresholds.put("Test_1", new long[]{5,8});
		thresholds.put("Test_2", new long[]{4,9});
		
		pg.setThresholds(thresholds);
		
		try {
			rm.setPresenceMap("./resource files/test.txt",species);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testProcess() {
		Map<Integer, Patch> cells = rm.getPatches();
		assertFalse(cells.get(0).isInfestedBy(species));
		assertTrue(cells.get(21).isInfestedBy(species));
		assertTrue(cells.get(42).isInfestedBy(species));
		assertEquals(0, cells.get(0).getAgeOfInfestation(species));
		assertEquals(0, cells.get(21).getAgeOfInfestation(species));
		assertEquals(0, cells.get(42).getAgeOfInfestation(species));
		pg.setTimeIncrement(1);
		pg.process(rm);
		assertEquals(0, cells.get(0).getAgeOfInfestation(species));
		assertEquals(1, cells.get(21).getAgeOfInfestation(species));
		assertEquals(1, cells.get(42).getAgeOfInfestation(species));
		assertEquals(1, cells.get(21).getInfestation(species).getStageOfInfestation());
		assertEquals(1, cells.get(21).getInfestation(species).getMaxInfestation());
		pg.process(rm);
		assertEquals(0, cells.get(0).getAgeOfInfestation(species));
		assertEquals(2, cells.get(21).getAgeOfInfestation(species));
		assertEquals(2, cells.get(42).getAgeOfInfestation(species));
		pg.setTimeIncrement(4);
		pg.process(rm);
		assertEquals(0, cells.get(0).getAgeOfInfestation(species));
		assertEquals(6, cells.get(21).getAgeOfInfestation(species));
		assertEquals(6, cells.get(42).getAgeOfInfestation(species));
		assertEquals(2, cells.get(21).getInfestation(species).getStageOfInfestation());
		assertEquals(2, cells.get(21).getInfestation(species).getMaxInfestation());
	}
}
