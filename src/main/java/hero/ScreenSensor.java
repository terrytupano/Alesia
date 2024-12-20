package hero;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.imageio.*;
import javax.swing.*;

import com.alee.utils.*;

import core.*;
import net.sourceforge.tess4j.*;
import net.sourceforge.tess4j.util.*;
import datasource.*;

/**
 * This class is a visual representation of the area read from the screen. Group of this class are used and controlled
 * by {@link SensorsArray} class. This class has a dual purpose:
 * <li>The main function is capture and process the image had read from the screen. the bounds, type of process need to
 * perform for this class, etc. come from the {@link Shape} class created based on the configuration file.
 * <li>this class is an of JPanel, so, this allow swing class use this class to build component that displays the
 * information captured and processed by this class.</li>
 * <p>
 * 
 * @author terry
 *
 */
public class ScreenSensor extends JPanel {

	public static String CAPTURED = "captured";
	public static String COLORED = "colored";
	public static String PREPARED = "prepared";

	private Hashtable<String, BufferedImage> images;
	private String showImage;
	private Shape shape;
	private int scaledWidth, scaledHeight;
	private Color backgroundColor;
	private double colorPercent;
	private BufferedImage preparedImage, capturedImage, lastOcrImage;
	private JLabel dataLabel;
	private JLabel imageLabel;
	private String ocrResult;
	private Tesseract iTesseract;
	private long ocrTime = -1;
	private SensorsArray sensorsArray;
	private String currencySymbol;
	// private String decimalSeparator;
	// private String groupSeparator;

	public ScreenSensor(SensorsArray sensorsArray, Shape sha) {
		super(new BorderLayout());
		this.sensorsArray = sensorsArray;
		this.images = new Hashtable<>();
		this.shape = sha;
		this.imageLabel = new JLabel();
		this.dataLabel = new JLabel();
		// this.decimalSeparator = TStringUtils.getString("table.decimal-separator");
		// this.groupSeparator = TStringUtils.getString("table.group-separator");
		dataLabel.setFont(new Font("courier new", Font.PLAIN, 12));
		setName(shape.name);

		Dimension sd = TCVUtils.getScaledDimension(shape.bounds.width, shape.bounds.height);
		this.scaledWidth = sd.width;
		this.scaledHeight = sd.height;

		showImage(CAPTURED);

		// Standard: image at left, data at center
		// if ratio is > 2. the component align are vertical (image at top, data at
		// center)
		double ratio = (float) shape.bounds.width / (float) shape.bounds.height;
		add(imageLabel, ratio > 2 ? BorderLayout.NORTH : BorderLayout.WEST);
		add(dataLabel, BorderLayout.CENTER);
		TrooperParameter params = TrooperParameter.findFirst("trooper = ?", "Hero");
		this.currencySymbol = params.getString("currency");
		this.iTesseract = Hero.getTesseract();
		clearEnvironment();
		update();
	}

	@Override
	public String toString() {
		// overide method to only show vital info
		return getName() + " isEnable=" + isEnabled() + " OCR=" + getOCR();
	}

	/**
	 * Return a random {@link Point} selected inside of the area (0,width) (0,height)
	 * 
	 * @param width - width of the area
	 * @param height - height of the area
	 * @return a random point inside area
	 */
	public static Point getRandCoordenates(int width, int height) {
		int x = (int) Math.random() * width;
		int y = (int) Math.random() * height;
		return new Point(x, y);
	}

	/**
	 * Replace the incoming argument, which is spected to be only number, with the know replacemente due to tesseract
	 * caracter bad recognition. for example, is know that ocr operation detect 800 as a00 or 80o. this method will
	 * return 800 for thath incommin value
	 * 
	 * @param ocrString - posible nomeric value with leters
	 * @return string only numeric
	 */
	private static String replaceWhitNumbers(String ocrString) {
		String rstr = ocrString.replace("z", "2");
		rstr = rstr.replace("Z", "2");
		rstr = rstr.replace("o", "0");
		rstr = rstr.replace("O", "0");
		rstr = rstr.replace("a", "8");
		rstr = rstr.replace("s", "8");
		rstr = rstr.replace("S", "8");
		rstr = rstr.replace("U", "0");
		rstr = rstr.replace("u", "0");
		rstr = rstr.replace("e", "6");
		return rstr;
	}

	/**
	 * Capture the region of the screen specified by this sensor. this method is executed at different levels according to
	 * the retrieved information from the screen. The <code>doOcr</code> argument indicate the desire for retrive OCR from
	 * the image. the ocr will retrive if this argument is <code>true</code> and a difference between the last image and
	 * the actual image has been detected.
	 * <li>prepare the image
	 * <li>set the status enabled/disabled for this sensor if the image is considerer enabled. if this sensor is setted
	 * to disable, no more further operations will be performed.
	 * <li>perform the associated OCR operation according to this area type ONLY IF the image has change.
	 * 
	 * @see #getCapturedImage()
	 * 
	 * @param doOcr - <code>true</code> for perform OCR operation (if is available)
	 * @param isLive - <code>true</code> capture the image from window. <code>false</code> capture from file
	 */
	public void capture(boolean doOcr, String readSource) {
		Rectangle bou = shape.bounds;
		long t1 = System.currentTimeMillis();

		// hero is live
		if (SensorsArray.FROM_ROBOT.equals(readSource)) {
			// from the screen
			capturedImage = sensorsArray.getRobot().createScreenCapture(bou);
		}

		// from the ppt file background
		if (SensorsArray.FROM_FILE.equals(readSource)) {
			ImageIcon ii = sensorsArray.getScreenAreas().getBackgroundImage();
			BufferedImage bgimage = ImageUtils.copyToBufferedImage(ii);
			capturedImage = bgimage.getSubimage(bou.x, bou.y, bou.width, bou.height);
		}

		// from the screenshots directory
		if (SensorsArray.FROM_SCREENSHOT.equals(readSource)) {
			try {
				File file = sensorsArray.getReadSourceFile();
				BufferedImage bgimage = ImageIO.read(file);
				capturedImage = bgimage.getSubimage(bou.x, bou.y, bou.width, bou.height);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * color reduction and image treatment before OCR operation or enable/disable action: mandatory for all areas
		 */
		prepareImage();

		/*
		 * by default an area is enabled if against a dark background, there is some activation color. if the white
		 * color is over some %, the action is setted as enabled. use the property enable.when=% to set a different
		 * percentage
		 */
		setEnabled(false);
		if (!(colorPercent > 1)) {
			// if a sensor is disabled, his ocr is null by default. this avoid previous ocr
			// values if the sensor was
			// enabled before
			ocrResult = null;
			update();
			return;
		}
		setEnabled(true);

		// TODO: IMPLEMEEEEEEETTTTTTTTTTTTTTTTTTTTTTTTTTTT
		if (doOcr) {
			double imgdif = 100.0;
			if (lastOcrImage != null)
				imgdif = TCVUtils.getImageDiferences(lastOcrImage, capturedImage, false);
			if ((lastOcrImage == null) || imgdif > 0 || ocrResult == null) {
				doOCR();
				lastOcrImage = capturedImage;
				// saveSample(doOcr, isLive);
			}
		}
		update();
		ocrTime = (System.currentTimeMillis() - t1);
	}

	public BufferedImage getImage(String type) {
		return images.get(type);
	}

	/**
	 * Return the string representation of the {@link #backgroundColor} variable. the format is RRGGBB
	 * 
	 * @return
	 */
	public String getMaxColor() {
		return TColorUtils.getOpaqueRGBColor(backgroundColor);
	}

	/**
	 * Return the int value from this sensor. Some sensor has only numerical information or text/numerical information.
	 * According to this, this method will return that numerical information (if is available) or -1 if not. Also, -1 is
	 * returned if any error is found during the parsing operation.
	 * 
	 * @see #OCRCorrection(ScreenSensor)
	 * 
	 * @return int value or <code>-1</code>
	 */
	public double getNumericOCR() {
		String ocr = getOCR();
		double val = -1;
		try {
			if (ocr != null) {
				// ocr = ocr.replace(groupSeparator, "");
				// ocr = ocr.replace(decimalSeparator, ".");
				val = Double.parseDouble(ocr);
			}
		} catch (Exception e) {
			Hero.heroLogger.fine(getName() + ": Fail getting double value. The OCR is: " + ocr);
		}
		return val;
	}

	/**
	 * Return the optical character recognition extracted from the associated area or <code>null</code> if the OCR is not
	 * available (disabled sensor)
	 * 
	 * @return OCR result or <code>null</code>
	 */
	public String getOCR() {
		return ocrResult;
	}

	public long getOCRPerformanceTime() {
		return ocrTime;
	}

	public boolean isActionArea() {
		return shape.isActionArea;
	}

	public boolean isCardArea() {
		return shape.isCardArea;
	}

	public boolean isButtonArea() {
		return shape.isButtonArea;
	}

	/**
	 * Return <code>true</code> if this sensor is a comunity card sensor
	 * 
	 * @return <code>true</code> if i.m a community card sensor
	 * @since 2.3
	 */
	public boolean isComunityCard() {
		String sn = getName();
		return sn.startsWith("flop") || sn.equals("turn") || sn.equals("river");
	}

	/**
	 * Return <code>true</code> if this sensor is a hole card sensor
	 * 
	 * @return <code>true</code> if i.m a hole card sensor
	 * @since 2.3
	 */
	public boolean isHoleCard() {
		String sn = getName();
		return sn.startsWith("hero.card");
	}

	public boolean isNumericArea() {
		return shape.isOCRNumericArea;
	}

	public boolean isTextArea() {
		return shape.isOCRTextArea;
	}

	/**
	 * set for this sensor that draw the original captured image or the prepared image. this method affect only the
	 * visual representation of the component.
	 * <p>
	 * WARNING: displaying prepared images will invoke more method on the {@link Tesseract} OCR api engine. this will
	 * decrease the system performance 4x.
	 * 
	 * @param so - <code>true</code> to draw the original captured image
	 */
	public void showImage(String type) {
		this.showImage = type;
		// BufferedImage img = images.get(showImage);
		// plus 2 of image border
		Dimension dim = new Dimension(shape.bounds.width + 2, shape.bounds.height + 2);
		// for prepared images
		if (showImage.equals(PREPARED))
			dim = new Dimension(scaledWidth + 2, scaledHeight + 2);
		// if (img != null)
		// dim = new Dimension(img.getWidth() + 2, img.getHeight() + 2);
		imageLabel.setPreferredSize(dim);

	}

	/**
	 * Central method to get OCR operations. This method clear and re sets the variable {@link #ocrResult} according to
	 * the succeed or failure of the ocr operation.
	 */
	private void doOCR() {
		ocrResult = null;
		try {
			if (isCardArea() && isEnabled() && (isComunityCard() || isHoleCard()))
				ocrResult = getCardOCR();
			if (isTextArea() || isNumericArea())
				ocrResult = getTesseractOCR();

		} catch (Exception e) {
			Hero.heroLogger.fine(getName() + ": Fail trying doOCR " + e);
		}
	}

	/**
	 * return the string representation of the car area using a convination of tessearct for rank and color comparation
	 * for suit
	 * 
	 * @return the ocr retrieved from the original file name or <code>null</code>
	 */
	private String getCardOCR() throws Exception {
		// String ocr = getOCRFromImage(preparedImage, Hero.preparedCards);
		String rank = iTesseract.doOCR(preparedImage).trim().toUpperCase();
		Hero.heroLogger.finer(getName() + ": Card OCR performed. Raw OCR without correction = " + rank);

		// rank correction (know errors)
		rank = "G".equals(rank) ? "Q" : rank;
		rank = "B".equals(rank) ? "6" : rank;
		rank = "I0".equals(rank) ? "T" : rank;
		rank = "ID".equals(rank) ? "T" : rank;

		// report error and return empty string
		String suit = "";
		if ("".equals(rank)) {
			Hero.heroLogger.fine(getName() + ": Card OCR Fail. raw OCR = " + rank);
			return "";
		} else {
			String cn = TColorUtils.colorNames.get(backgroundColor);
			// suit como from the baground color
			suit = "red".equals(cn) ? "h" : suit;
			suit = "lime".equals(cn) ? "c" : suit;
			suit = "cyan".equals(cn) ? "d" : suit;
			suit = "black".equals(cn) ? "s" : suit;
			// // only for visual purpose. the regions of interest was already used
			// if (showImage.equals(PREPARED)) {
			// TCVUtils.parameteres.put("rgbToBinaryThreshold", "220");
			// TCVUtils.parameteres.put("removeSegmentsWindowSize", "9");
			// MarvinImage mi = new MarvinImage(preparedImage);
			// TCVUtils.getImageSegments(mi, true);
			// }
		}
		return rank + suit;
	}

	/**
	 * Perform tesseract ocr operation for generic areas.
	 * 
	 * @return the string recognized by tesseract
	 * 
	 * @throws TesseractException
	 */
	private String getTesseractOCR() throws TesseractException {
		String srcocr = iTesseract.doOCR(preparedImage);

		// draw segmented regions (only on prepared image) and ONLY when the prepared
		// image is request to be visible
		if (showImage.equals(PREPARED) && preparedImage != null) {
			int pageIteratorLevel = TessAPI.TessPageIteratorLevel.RIL_WORD;
			// List<Word> wlst = Hero.iTesseract.getWords(preparedImage, pageIteratorLevel);
			List<Rectangle> regions = iTesseract.getSegmentedRegions(preparedImage, pageIteratorLevel);
			Graphics2D g2d = (Graphics2D) preparedImage.getGraphics();
			g2d.setColor(Color.LIGHT_GRAY);
			if (regions != null) {
				for (int i = 0; i < regions.size(); i++) {
					Rectangle region = regions.get(i);
					g2d.drawRect(region.x, region.y, region.width, region.height);
				}
			}
			// Hero.heroLogger.finer(getName() + ": list of words: " + wlst);
			// Hero.heroLogger.finer(getName() + ": Tesseract OCR performed. Regions: " +
			// regions.size() + " OCR=" +
			// srcocr);
		}
		Hero.heroLogger.finer(getName() + ": Tesseract OCR performed. Raw OCR whitout correction=" + srcocr);
		return OCRCorrection(srcocr);
	}

	/**
	 * perform custom corrections accordint of the name or type of sensor. for example, for call sensor is spected this
	 * correction retrive only the numeric value from the second line, ignoring the "call" text
	 * 
	 */
	private String OCRCorrection(String srcocd) {

		// standar procedure for numeric sensors
		if (isNumericArea()) {

			srcocd = Hero.parseNummer(srcocd, currencySymbol);

			// original code
			// srcocd = srcocd.replaceAll("[^" + currencySymbol + "1234567890]", "");
			// // srcocd = srcocd.replaceAll("[^" + currencySymbol + decimalSeparator + "1234567890]", "");
			//
			// // at this point the var mus contain the currency simbol as first caracter. in case of error, the first
			// // caracter maybe is a number. as a fail safe, remove allways the first caracter.
			// if (!"".equals(currencySymbol) && srcocd.length() > 1)
			// srcocd = srcocd.substring(1).trim();
			//
			// // use currency simbol as marker. when the currency simbol is present, assume 2 decimal digits for all
			// // numbers
			// if (!"".equals(currencySymbol)) {
			// int len = srcocd.length();
			// srcocd = srcocd.substring(0, len - 2) + "." + srcocd.substring(len - 2);
			// }
		}

		// standar procedure: remove all blanks caracters
		srcocd = srcocd.replaceAll("\\s", "");

		// for chips numerical areas, and the value is empty string is probably because all-in action, assign 0
		if (isNumericArea() && getName().contains(".chips") && "".equals(srcocd)) {
			srcocd = "0";
		}
		// special treatmen for hero.call sensor
		if ("hero.call".equals(getName()) && isEnabled() && srcocd.equals(""))
			srcocd = "0";

		return srcocd;
	}

	/**
	 * perform image operation to set globals variables relatet with the image previous to OCR, color count operations.
	 * acording to the type of area that this sensor represent, the underling image can be transformed in diferent ways.
	 * <p>
	 * This method set the {@link #preparedImage}, {@link #backgroundColor} and {@link #whitePercent} global variables.
	 * 
	 */
	private void prepareImage() {
		// update global variable. this step is mandatory for all areas because maxcolor
		// and white percent affect the
		// flow of the entire class
		BufferedImage bufimg = TColorUtils.convert3(capturedImage);
		Hashtable<String, Integer> histo = TColorUtils.getHistogram(bufimg);
		this.backgroundColor = TColorUtils.getBackgroundColor(histo, Color.white);

		// test: for card areas, if the card is active and contain the cian color
		// (diamond) set the background color as
		// cyan
		if (isCardArea())
			if (histo.containsKey(TColorUtils.getOpaqueRGBColor(Color.cyan)))
				this.backgroundColor = Color.cyan;

		this.colorPercent = TColorUtils.getColorPercent(histo, shape.enableColor,
				bufimg.getWidth() * bufimg.getHeight());
		images.put(COLORED, bufimg);
		images.put(CAPTURED, capturedImage);

		if (isCardArea()) {
			TCVUtils.parameteres.setProperty("borderColor", "808080");
			bufimg = TCVUtils.paintBorder(capturedImage);
			bufimg = ImageHelper.getScaledInstance(bufimg, scaledWidth, scaledHeight);
			bufimg = ImageHelper.convertImageToGrayscale(bufimg);
		}

		// all ocr areas need scaled instance
		if (isTextArea() || isNumericArea()) {
			bufimg = ImageHelper.getScaledInstance(capturedImage, scaledWidth, scaledHeight);
			bufimg = ImageHelper.convertImageToGrayscale(bufimg);
		}
		images.put(PREPARED, bufimg);
		this.preparedImage = bufimg;
	}

	/**
	 * update the associated UI components with the internal values from this sensor.
	 */
	private void update() {
		BufferedImage sel = images.get(showImage);
		// at init time, sel image can be null
		if (sel != null)
			imageLabel.setIcon(new ImageIcon(sel));

		String maxc = backgroundColor == null ? "null" : TColorUtils.colorNames.get(backgroundColor);
		String secol = TColorUtils.colorNames.get(shape.enableColor);
		String ecol = isEnabled() ? "GREEN" : "GRAY";
		String etex = isEnabled() ? "Enabled" : "Disabled";
		String elin = "<FONT COLOR=" + ecol + ">" + etex + "</FONT>";

		// + "<br>Max color: <FONT style= \"background-color: #"+maxc +"\"><B>" + maxc +
		// "</B></FONT>" + "<br>OCR: " +
		// ocrResult
		String text = "<html>" + getName() + "  " + elin + "<br>Enable: " + secol + " " + colorPercent
				+ "<br>Background: <B style= \"color: #" + maxc + "\">" + maxc + "</B>" + "<br>OCR: " + ocrResult
				+ "</html>";
		dataLabel.setText(text);
	}

	protected void clearEnvironment() {
		ocrResult = null;
		preparedImage = null;
		capturedImage = null;
		lastOcrImage = null;
		setEnabled(false);
		repaint();
	}
}
