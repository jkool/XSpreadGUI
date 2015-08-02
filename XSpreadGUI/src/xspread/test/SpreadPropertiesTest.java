package xspread.test;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import xspread.application.SpreadProperties;

public class SpreadPropertiesTest {

	private SpreadProperties sp = new SpreadProperties();
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadTextFile() {
		try {
			sp.readTextFile(new File("C:/Temp2/Test.txt"));
			
			// Add test assertions!!
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
