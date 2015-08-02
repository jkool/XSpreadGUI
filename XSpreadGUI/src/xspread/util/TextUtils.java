package xspread.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class TextUtils {
	public static List<Double> arr2list(double[] da) {
		ArrayList<Double> list = new ArrayList<Double>();
		for (double d : da) {
			list.add(d);
		}
		return list;
	}

	/**
	 * Checks whether a String is a number
	 * 
	 * @param str
	 * @return
	 */

	public static boolean isNumeric(String str) {
		String strtmp = str.trim();
		NumberFormat formatter = NumberFormat.getInstance();
		ParsePosition pos = new ParsePosition(0);
		formatter.parse(strtmp, pos);
		return strtmp.length() == pos.getIndex();
	}

	public static List<double[]> parseMultiNumericArray(String string)
			throws IllegalArgumentException, IOException, FileNotFoundException {
		List<double[]> ma = new ArrayList<double[]>();

		if (string.contains(";")) {
			StringTokenizer stk = new StringTokenizer(string, ";");
			while (stk.hasMoreTokens()) {
				ma.addAll(parseMultiNumericArray(stk.nextToken()));
			}
		}

		else if (string.startsWith("file:")) {
			String filestring = string.substring(string.indexOf("file:") + 5);
			if (filestring.length() == 5) {
				throw new IllegalArgumentException(
						"File tag used (file:) but no path was provided after the tag (empty string).  Please check the properties file and referenced files");
			}
			File f = new File(filestring);
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String ln = br.readLine();
				while (ln != null) {
					ma.addAll(parseMultiNumericArray(ln));
					ln = br.readLine();
				}
			} catch (FileNotFoundException e) {
				throw new FileNotFoundException("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be found.");
			} catch (IOException e) {
				throw new IOException("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be read or accessed.");
			}
		}

		else {
			ma.add(parseNumericArray(string));
		}

		return ma;
	}

	/**
	 * Parses a String to generate arrays. Brace brackets indicate {start, stop,
	 * number of items}. Round brackets indicate (start,interval,number of
	 * items). Square brackets are used to directly specify the array.
	 * 
	 * @param string
	 * @return
	 */

	public static double[] parseNumericArray(String string)
			throws IllegalArgumentException, IOException, FileNotFoundException {

		double[] values;

		if (string.startsWith("file:")) {
			ArrayList<Double> da = new ArrayList<Double>();
			String filestring = string.substring(string.indexOf("file:") + 5);
			if (filestring.length() == 5) {
				throw new IllegalArgumentException(
						"File tag used (file:) but no path was provided after the tag (empty string).  Please check the properties file and referenced files");
			}
			File f = new File(filestring);
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String ln = br.readLine();
				while (ln != null) {
					da.addAll(arr2list(parseNumericArray(ln)));
					ln = br.readLine();
				}
			} catch (FileNotFoundException e) {
				throw new FileNotFoundException("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be found.");
			} catch (IOException e) {
				throw new IOException("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be read or accessed.");
			}
			values = new double[da.size()];
			for (int i = 0; i < da.size(); i++) {
				values[i] = da.get(i).doubleValue();
			}
			return values;
		}

		// parse brace brackets for {start,stop, number of items}

		if (string.startsWith("{") && string.endsWith("}")) {
			StringTokenizer stk = new StringTokenizer(string, "{,}");
			if (stk.countTokens() != 3) {
				throw new IllegalArgumentException(
						"Incorrect parameter values "
								+ string
								+ ".  Brace bracket notation {} indicates a range using min,max and number of values and takes only 3 parameters.");
			}
			double min = Double.parseDouble(stk.nextToken());
			double max = Double.parseDouble(stk.nextToken());

			if (max <= min) {
				throw new IllegalArgumentException(
						"Maximum (2nd) value must be greater than the minimum (1st) value.");
			}

			int quantity = Integer.parseInt(stk.nextToken());
			double interval = (max - min) / (quantity - 1);
			values = new double[quantity];
			values[0] = min;
			values[quantity - 1] = max;
			for (int i = 1; i < quantity - 1; i++) {
				values[i] = min + i * interval;
			}
			return values;
		}

		// parse square brackets for directly specifying the array

		else if (string.startsWith("[") && string.endsWith("]")) {
			StringTokenizer stk = new StringTokenizer(string, "[,]");
			values = new double[stk.countTokens()];
			int ct = 0;
			while (stk.hasMoreTokens()) {
				values[ct] = Double.parseDouble(stk.nextToken());
				ct++;
			}
			return values;
		}

		// parse round brackets for (start,interval,number of items)

		else if (string.startsWith("(") && string.endsWith(")")) {
			StringTokenizer stk = new StringTokenizer(string, "(,)");
			if (stk.countTokens() != 3) {
				throw new IllegalArgumentException(
						"Incorrect parameter values "
								+ string
								+ ".  Round bracket notation () indicates a range using min,interval and number of values and takes only 3 parameters.");
			}

			double min = Double.parseDouble(stk.nextToken());
			double interval = Double.parseDouble(stk.nextToken());
			int quantity = Integer.parseInt(stk.nextToken());

			values = new double[quantity];
			for (int i = 0; i < quantity; i++) {
				values[i] = i * interval + min;
			}

			return values;
		}

		// if a single value, wrap as an array

		else if (isNumeric(string)) {
			return new double[] { Double.parseDouble(string) };
		}

		else {
			throw new IllegalArgumentException(
					"Parameter array values "
							+ string
							+ " could not be parsed.  Please use a single number; a range: {min,max,number of values}; a range: (min,interval,number of values), or a comma separated list surrounded by square brackets []");
		}
	}

	public static List<String> parseStringArray(String string)
			throws IllegalArgumentException, IOException, FileNotFoundException {

		List<String> list = new ArrayList<String>();

		if (string == null) {
			return list;
		}

		if (string.startsWith("file:")) {
			String filestring = string.substring(string.indexOf("file:") + 5);
			if (filestring.length() == 5) {
				throw new IllegalArgumentException(
						"File tag used (file:) but no path was provided after the tag (empty string).  Please check the properties file and referenced files");
			}
			File f = new File(filestring);
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String ln = br.readLine();
				while (ln != null) {
					list.add(ln);
					br.readLine();
				}
			} catch (FileNotFoundException e) {
				throw new FileNotFoundException("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be found.");
			} catch (IOException e) {
				throw new IOException("File entry "
						+ string.substring(string.indexOf("file:") + 5)
						+ " could not be read or accessed.");
			}
		}

		StringTokenizer stk = new StringTokenizer(string, "[,]");
		while (stk.hasMoreTokens()) {
			list.add(stk.nextToken().trim());
		}
		return list;
	}

	public static boolean isTextFile(File filename) {

		boolean result = false;

		FileReader inputStream = null;

		try {
			inputStream = new FileReader(filename);

			int c;
			while ((c = inputStream.read()) != -1) {

				// (9)Horizontal tab (10)Line feed (11)Vertical tab (13)Carriage
				// return (32)Space (126)tilde
				if (c == 9 || c == 10 || c == 11 || c == 13
						|| (c >= 32 && c <= 126)) {
					result = true;

					// (153) ² (160) (255) No break space
				} else if (c == 153 || c >= 160 && c <= 255) {
					result = true;

				} else {
					System.out.println(c + " " + (char) c);
					result = false;
					break;
				}

			}
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		} finally {

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ex) {
				}
			}
		}
		return result;
	}

	public static String list2ArrayString(List<String> list) {
		if (list == null) {
			return "null";
		}
		if (list.size() == 0) {
			return "";
		}
		if (list.size() == 1) {
			return list.get(0);
		}
		return Arrays.toString(list.toArray(new String[list.size()]));
	}

	public static String list2MultiArrayString(List<String> list) {
		if (list == null) {
			return "null";
		}
		if (list.size() == 0) {
			return "";
		}
		if (list.size() == 1) {
			return list.get(0);
		}

		StringBuffer sb = new StringBuffer();

		for (String s : list) {
			sb.append(s);
			sb.append(";");
		}

		return sb.substring(0, sb.length() - 1);
	}

	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + "s", s);
	}

	public static String removeExtension(String s) {

		String separator = System.getProperty("file.separator");
		String filename;

		// Remove the path upto the filename.
		int lastSeparatorIndex = s.lastIndexOf(separator);
		if (lastSeparatorIndex == -1) {
			filename = s;
		} else {
			filename = s.substring(lastSeparatorIndex + 1);
		}

		// Remove the extension.
		int extensionIndex = filename.lastIndexOf(".");
		if (extensionIndex == -1)
			return filename;

		return filename.substring(0, extensionIndex);
	}
}