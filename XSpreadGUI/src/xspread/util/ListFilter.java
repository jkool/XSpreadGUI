package xspread.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

/*
 * Helper Filter class - used for filtering out file names having the provided
 * extension.
 */


public class ListFilter implements FilenameFilter {
	private List<String> patterns;

	public ListFilter(List<String> patterns) {
		this.patterns = patterns;
	}
	

	public boolean accept(File dir, String name) {
		
		if(patterns.isEmpty()){return true;}
		
		for (String pattern:patterns){
			if(name.toLowerCase().matches(pattern)){
				continue;
			}
			return false;
		}
		return true;
	}
}