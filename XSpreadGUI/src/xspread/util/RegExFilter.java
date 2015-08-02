package xspread.util;

import java.io.File;
import java.io.FilenameFilter;

/*
 * Helper Filter class - used for filtering out file names having the provided
 * extension.
 */

public class RegExFilter implements FilenameFilter {
	private String pattern;

	public RegExFilter(String pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().matches(pattern);
	}
}