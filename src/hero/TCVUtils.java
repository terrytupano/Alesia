package hero;

import java.awt.*;
import java.awt.image.*;
import java.util.*;

import com.alee.utils.*;

import core.*;
import net.sourceforge.tess4j.util.*;



public class TCVUtils {

	
	static public Properties parameteres = new Properties();

	/**
	 * Compare the images <code>imagea</code> and <code>imageg</code> pixel by pixel returning the percentage of
	 * diference. If the images are equals, return values closer to 0.0, and for complety diferent images, return values
	 * closer to 100 percent.
	 * <p>
	 * the ajust parameter indicate what todo whie the images are of diferent size.
	 * <li><code>true</code> means the image with mayor area will be rescaled to the dimension of the image of the menor
	 * aresa.
	 * <li><code>false</code>in this case, this function compare only the common area. that is, starting from (0,0)
	 * until the dimension of the smaller image
	 * 
	 * @see TColorUtils#getRGBColorDistance(Color, Color)
	 * 
	 * @param imagea
	 * @param imageb
	 * @param ajust
	 * @return
	 */
	public static double getImageDiferences(BufferedImage imagea, BufferedImage imageb, boolean ajust) {
		double diference = 0;
		BufferedImage buffimg_a = TColorUtils.copy(imagea);
		BufferedImage buffimg_b = TColorUtils.copy(imageb);

		// ajust the mayor image to the size of the minor image
		if (ajust) {
			int areaa = buffimg_a.getWidth() * buffimg_a.getHeight();
			int areab = buffimg_b.getWidth() * buffimg_b.getHeight();
			if (areaa > areab)
				buffimg_a = ImageHelper.getScaledInstance(buffimg_a, buffimg_b.getWidth(), buffimg_b.getHeight());
			else
				buffimg_b = ImageHelper.getScaledInstance(buffimg_b, buffimg_a.getWidth(), buffimg_a.getHeight());
		}

		int width = buffimg_a.getWidth() < buffimg_b.getWidth() ? buffimg_a.getWidth() : buffimg_b.getWidth();
		int height = buffimg_a.getHeight() < buffimg_b.getHeight() ? buffimg_a.getHeight() : buffimg_b.getHeight();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgba = buffimg_a.getRGB(x, y);
				int rgbb = buffimg_b.getRGB(x, y);
				diference += TColorUtils.getRGBColorDistance(new Color(rgba), new Color(rgbb));
			}
		}

		// total number of pixels
		int pixels = width * height;
		// normaliye the value of diferent pixel
		double avg_diff = diference / pixels;
		// percentage
		double percent = avg_diff * 100;
		return percent;
	}


	public static BufferedImage getScaledBufferedImage(BufferedImage image) {
		Dimension ndim = getScaledDimension(image.getWidth(), image.getHeight());
		int type = (image.getTransparency() == Transparency.OPAQUE)
				? BufferedImage.TYPE_INT_RGB
				: BufferedImage.TYPE_INT_ARGB;
		BufferedImage tmp = new BufferedImage(ndim.width, ndim.height, type);
		Graphics2D g2 = tmp.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g2.drawImage(image, 0, 0, ndim.width, ndim.height, null);
		g2.dispose();
		return tmp;
	}

	/**
	 * the scale factor form a image taked from the screen at 96dpi to the optimal image resolution 300dpi. Teste but no
	 * visible acuracy detected against the 2.5 factor. leave only because look like the correct procedure.
	 * 
	 * @param width - original width
	 * @param height - original height
	 * @return dimension with the scale size
	 */
	public static Dimension getScaledDimension(int width, int height) {
		int dpi = 300;
		float scale = dpi / 96;
		int scaledWidth = (int) (width * scale);
		int scaledHeight = (int) (height * scale);
		return new Dimension(scaledWidth, scaledHeight);
	}


	public static BufferedImage paintBorder(BufferedImage image) {
		int size = Integer.parseInt(parameteres.getProperty("borderSize", "6"));
		Color color = TColorUtils.getOpaqueRGBColor(parameteres.getProperty("borderColor", "000000"));
		BufferedImage newimagea = ImageUtils.copyToBufferedImage(image);
		Graphics2D g2d = newimagea.createGraphics();
		g2d.drawImage(image, 0, 0, null);
		BasicStroke bs = new BasicStroke(size);
		g2d.setStroke(bs);
		g2d.setColor(color);
		g2d.drawRect(0, 0, image.getWidth(), image.getHeight());
		g2d.dispose();
		return newimagea;
	}

}