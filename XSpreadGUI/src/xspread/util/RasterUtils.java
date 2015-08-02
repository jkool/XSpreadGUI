package xspread.util;

public class RasterUtils {

	private static final String[] cols = { "FFFFFF", "00FF00", "0000FF",
		"FF0000", "01FFFE", "FFA6FE", "FFDB66", "006401", "010067",
		"95003A", "007DB5", "FF00F6", "FFEEE8", "774D00", "90FB92",
		"0076FF", "D5FF00", "FF937E", "6A826C", "FF029D", "FE8900",
		"7A4782", "7E2DD2", "85A900", "FF0056", "A42400", "00AE7E",
		"683D3B", "BDC6FF", "263400", "BDD393" };
	
	public static int algoColour(int index) {

		if (index == -9999) {
			return java.awt.Color.BLACK.getRGB();
		}

		return new java.awt.Color(Integer.parseInt(cols[index].substring(0, 2),
				16), Integer.parseInt(cols[index].substring(2, 4), 16),
				Integer.parseInt(cols[index].substring(4, 6), 16), 255)
				.getRGB();
	}
	
	public static int rampColour(double val, double vmin, double vmax){
		
		if (val == -9999) {
			return java.awt.Color.BLACK.getRGB();
		}
		
		if(val == 0){
			return java.awt.Color.WHITE.getRGB();
		}
		
		return ColorUtils.jet(val, vmin, vmax).hashCode();
	}
}
