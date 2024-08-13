package gui.list;

import java.awt.image.*;

class SelectedImageFilter extends RGBImageFilter {
	  // public SelectedImageFilter() {
	  //   canFilterIndexColorModel = false;
	  // }

	  @Override public int filterRGB(int x, int y, int argb) {
	    // Color color = new Color(argb, true);
	    // float[] array = new float[4];
	    // color.getComponents(array);
	    // return new Color(array[0], array[1], array[2] * .5f, array[3]).getRGB();
	    return (argb & 0xFF_FF_FF_00) | ((argb & 0xFF) >> 1);
	  }
	}
