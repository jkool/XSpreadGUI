package xspread.util;

import javafx.scene.paint.Color;

public class ColorUtils {
	public static Color jet(double v, double vmin, double vmax)
	{
		   Color c;
		   double dv;

		   if(vmin==0 && vmax==0){
			   vmax=1;
		   }
		   
		   if (v < vmin)
		      v = vmin;
		   if (v > vmax)
		      v = vmax;
		   dv = vmax - vmin;

		   if (v < (vmin + 0.25 * dv)) {
		      c = Color.color(0d, 4 * (v - vmin) / dv,1d);
		   } else if (v < (vmin + 0.5 * dv)) {
			   c = Color.color(0d, 1d,1 + 4 * (vmin + 0.25 * dv - v) / dv);
		   } else if (v < (vmin + 0.75 * dv)) {
			   c = Color.color(4 * (v - vmin - 0.5 * dv) / dv, 1d, 0d);
		   } else {
			   c = Color.color(1d, 1 + 4 * (vmin + 0.75 * dv - v) / dv, 0d);
		   }
		   return(c);
	}
}
