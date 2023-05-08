package hero;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

import org.apache.commons.math3.stat.descriptive.*;

import com.alee.utils.*;
import com.jgoodies.common.base.*;

/**
 * This class control the array of sensor inside of the screen. This class is responsible for reading all the sensor
 * configured in the {@link DrawingPanel} passed as argument in the {@link #createSensorsArray(DrawingPanel)} method.
 * <p>
 * Although this class are the eyes of the trooper, numerical values must be retries throw {@link PokerSimulator}. the
 * poker simulator values are populated during the reading process using the method
 * {@link PokerSimulator#addCard(String, String)} at every time that a change in the Environment is detected.
 * 
 * @author terry
 *
 */
public class SensorsArray {

	/**
	 * Read/see only numeric sensors. Numeric sensors are chips, pot, calls Etc
	 */
	public final static String TYPE_NUMBERS = "Numbers";
	/**
	 * Read/see only text sensors, names of the villains mainly
	 */
	public final static String TYPE_TEXT = "Text";

	/**
	 * Read/see TODO: read only villains information (all)
	 * 
	 * @see #TYPE_CARDS
	 */
	public final static String TYPE_VILLANS = "Villans";

	/**
	 * Read/see only cards areas. this type is only for hero cards and community cards
	 * 
	 */
	public final static String TYPE_CARDS = "Cards";
	/**
	 * Read/see only actions areas (call button, raise, continue)
	 */
	public final static String TYPE_ACTIONS = "Actions";

	/**
	 * the read operation source is from screen
	 */
	public static final String FROM_ROBOT = "FROM_ROBOT";
	/**
	 * the read operation source is from configuration file
	 */
	public static final String FROM_FILE = "FROM_FILE";
	/**
	 * the read operation source is from screen shots directory
	 */
	public static final String FROM_SCREENSHOT = "FROM_SCREENSHOT";


	private TreeMap<String, ScreenSensor> screenSensors;
	private Robot robot;
	private Border readingBorder, lookingBorder, standByBorder;
	private ShapeAreas screenAreas;
	private PokerSimulator pokerSimulator;
	private String readSource;
	private File readSourceFile;
	DescriptiveStatistics performaceStatistic = new DescriptiveStatistics(10);
			
	public SensorsArray(PokerSimulator pokerSimulator) {
		this.readSource = FROM_ROBOT;
		this.pokerSimulator = pokerSimulator;
		this.robot = Hero.getNewRobot();
		this.readingBorder = new LineBorder(Color.BLUE, 2);
		this.lookingBorder = new LineBorder(Color.ORANGE, 2);
		this.standByBorder = new LineBorder(new JPanel().getBackground(), 2);
		this.screenSensors = new TreeMap<>();

		File tableFile = new File(Constants.PPT_FILE);
		ShapeAreas shapeAreas = new ShapeAreas(tableFile);
		shapeAreas.read();
		setShapeAreas(shapeAreas);
	}
	public String getReadSource() {
		return readSource;
	}

	public void setReadSource(String readSource) {
		this.readSource = readSource;
	}

	/**
	 * set the array mode. this mode indicate from where this instance muss read the values.
	 * <li>Battle: all the values are read from the screen.
	 * <li>Simulation: all values are setted by {@link HeroBot}
	 * 
	 * @param mode - the mode private String arrayMode; public void setSimulationMode(String mode) {
	 *        Preconditions.checkArgument(mode.equals("Battle") || mode.equals("Simulation"), "Illegal Array mode " +
	 *        mode); this.arrayMode = mode; }
	 */
	/**
	 * initialize this sensor array. clearing all sensor and all variables
	 */
	public void clearEnvironment() {
		screenSensors.values().forEach((ss) -> ss.clearEnvironment());
	}

	/**
	 * Return the number of current villains active seats.
	 * 
	 * @see #isSeatActive(int)
	 * @return - num of active villains + me
	 */
	public int getActiveSeats() {
		int av = 0;
		for (int i = 1; i <= getVillans(); i++) {
			av += isSeatActive(i) ? 1 : 0;
		}
		// at this point at least must be 1 villains active set
		if (av == 0)
			Hero.heroLogger.severe("Fail to detect active seats");
		return av;
	}

	/**
	 * Return the number of current active villains.
	 * 
	 * @see #isActive(int)
	 * @see #getActiveSeats()
	 */
	public int getActiveVillans() {
		int av = 0;
		for (int i = 1; i <= getVillans(); i++) {
			if (isActive(i))
				av++;
		}
		return av;
	}

	/**
	 * return where in the table, the dealer button are. If hero has the button, this method return 0.
	 * 
	 * @return where the dealer button are or -1 for a fail in thable position detection
	 */
	public int getDealerButtonPosition() {
		int vil = getVillans();
		int bp = -1;
		bp = getSensor("hero.button").isEnabled() ? 0 : -1;
		for (int i = 1; i <= vil; i++) {
			bp = getSensor("villan" + i + ".button").isEnabled() ? i : bp;
		}
		if (bp == -1) {
			Hero.heroLogger.severe("Fail to detect table position.");
		}
		return bp;
	}

	public Robot getRobot() {
		return robot;
	}

	/**
	 * store a copy of the {@link #capturedImage} to file. The result of this operation is used by
	 * 
	 * @param hand - hand number
	 * @param street - street number
	 */
	public void saveSample(int hand, int street) {
		try {
			String ext = "png";
			Rectangle rec = new Rectangle(0, 0, 1920, 1080);
			BufferedImage image = robot.createScreenCapture(rec);
			String fn = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss").format(LocalDateTime.now()) + " Hand " + hand
					+ " Street " + street + "." + ext;
			fn = FileUtils.getAvailableName(Constants.SCREEN_SHOTS_FOLDER, fn);
			File outputfile = new File(Constants.SCREEN_SHOTS_FOLDER + fn);
			ImageIO.write(image, ext, outputfile);
		} catch (Exception e) {
			Hero.heroLogger.severe(e.getMessage());
		}
	}

	/**
	 * return the {@link ScreenSensor} by name. The name comes from property <code>name</code>
	 * 
	 * @param sensorName - screen sensor name
	 * 
	 * @return the screen sensor instance or <code>null</code> if no sensor is found.
	 */
	public ScreenSensor getSensor(String sensorName) {
		ScreenSensor ss = screenSensors.get(sensorName);
		Preconditions.checkNotNull(ss, "No sensor name " + sensorName + " was found.");
		return ss;
	}

	/**
	 * This method return a list of all action sensors currently enables. For example. if a Environment with 10 binary
	 * sensors, calling this method return <code>1469</code> means that the sensors 1, 4, 6 and 9 are enabled. all
	 * others are disabled.
	 * 
	 * @return list of binary sensors enabled public String getEnabledActions() { String onlist = "";
	 * 
	 *         List<ScreenSensor> sslist = screenSensors.values().stream().filter(ScreenSensor::isActionArea)
	 *         .collect(Collectors.toList()); for (int i = 0; i < sslist.size(); i++) { ScreenSensor ss =
	 *         screenSensors.get("binary.sensor" + i); onlist += ss.isEnabled() ? "" : i; } return onlist; }
	 */

	public ShapeAreas getScreenAreas() {
		return screenAreas;
	}

	/**
	 * Retrieve a list of sensor.s names according to the <code>subString</code> argument.
	 * <p>
	 * For example, to retrieve all "call" sensors, pass to this method ".call" will return sensor hero.call,
	 * villan1.call etc. <code>null</code> argument return all configured sensors.
	 * 
	 * <p>
	 * the list is sorted
	 * 
	 * @param subString - sub string to look inside of the sensor name
	 * 
	 * @return list of sensors or empty list if no sensor was found
	 */
	public List<ScreenSensor> getSensors(String subString) {
		List<ScreenSensor> list;
		if (subString == null)
			list = screenSensors.values().stream().collect(Collectors.toList());
		else
			list = screenSensors.values().stream().filter(ss -> ss.getName().contains(subString))
					.collect(Collectors.toList());
		return list;
	}

	/**
	 * Return the number of villains configured in this table.
	 * 
	 * @see HeroPanel
	 * 
	 * @return total villains
	 */
	public int getVillans() {
		return (int) screenSensors.keySet().stream().filter(sn -> sn.startsWith("villan") && sn.contains("name"))
				.count();
	}

	/**
	 * return <code>true</code> if the player identified as id argument is active (hero or villain). A PLAYER IS ACTIVE
	 * IF HE HAS CARDS IN THIS HANDS. if a player fold his card. this method will not count that player. from this
	 * method point of view. the player is in the game, but in this particular moment are not active.
	 * 
	 * @param id - villain id or 0 for hero
	 * @return true if the player is active
	 */
	public boolean isActive(int id) {
		String prefix = id == 0 ? "hero" : "villan" + id;
		ScreenSensor vc1 = getSensor(prefix + ".card1");
		ScreenSensor vc2 = getSensor(prefix + ".card2");
		return vc1.isEnabled() && vc2.isEnabled();
	}

	/**
	 * return <code>true</code> if the villanId seat is active. A seat is active if there are a villan sittion on it.
	 * this method check the villain name sensor and the villain chip sensor. if both are active, the seat is active.
	 * <p>
	 * from this method point of view, there are a villain sittin on a seat currently playing or not. maybe he abandom
	 * the action
	 * 
	 * @param villanId - the seat as configured in the ppt file. villan1 is at hero.s left
	 * @see #getActiveVillans()
	 * @return numers of villains active seats
	 */
	public boolean isSeatActive(int villanId) {
		// ScreenSensor vname = getSensor("villan" + villanId + ".name");
		// ScreenSensor vchip = getSensor("villan" + villanId + ".chips");
		// return vname.isEnabled() && vchip.isEnabled();

		// TODO: temporal until now, i.m not using the name sensor for nothing. so, an
		// active seat is when the chip
		// sensor is enabled
		ScreenSensor vchip = getSensor("villan" + villanId + ".chips");
		return vchip.isEnabled();
	}

	/**
	 * Shortcut to get the enable/disable status from a sensor
	 * 
	 * @param sensorName - sensor name
	 * 
	 * @return <code>true</code> if the sensor is enabled
	 */
	public boolean isSensorEnabled(String sensorName) {
		ScreenSensor ss = getSensor(sensorName);
		return ss.isEnabled();
	}

	public void lookActionSensors() {
		long t1 = System.currentTimeMillis();
		List<ScreenSensor> sslist = screenSensors.values().stream().filter(ss -> ss.isActionArea())
				.collect(Collectors.toList());
		readSensors(false, sslist);
		System.out.println("lookActionSensors() " + (System.currentTimeMillis() - t1));
	}

	/**
	 * this metho campture all screeen�s areas without do any ocr operation. Use this mothod to retrive all sensor areas
	 * and set the enable status for fast comparation.
	 * 
	 */
	public void lookTable() {
		long t1 = System.currentTimeMillis();
		List<ScreenSensor> sslist = getSensors(null);
		readSensors(false, sslist);
		long t2 = System.currentTimeMillis() - t1;
		pokerSimulator.setVariable("sensorArray.Look table time", sslist.size() + " sensors " + t2);
	}

	/**
	 * Perform read operation on the {@link ScreenSensor} according to the type of the sensor. The type can be any of
	 * TYPE_ global constant passed as argument. This method perform the OCR operation on the selected areas and update
	 * the {@link PokerSimulator} if it.s necessary.
	 * <p>
	 * After this method execution, the simulator reflect the actual game status
	 * 
	 * @param sensors - type of sensor to read
	 */
	public void read(String type) {
		Collection<ScreenSensor> allSensors = screenSensors.values();
		List<ScreenSensor> slist = new ArrayList<ScreenSensor>();

		// Action areas
		if (TYPE_ACTIONS.equals(type)) {
			slist = allSensors.stream().filter(ss -> ss.isActionArea()).collect(Collectors.toList());
			slist.add(getSensor("hero.card1"));
			slist.add(getSensor("hero.card2"));
			readSensors(true, slist);
		}

		// numeric types retrieve all numbers and update poker simulator
		if (TYPE_NUMBERS.equals(type)) {
			slist = allSensors.stream().filter(ss -> ss.isNumericArea()).collect(Collectors.toList());
			// remove villas sensor. villains sensor are update calling readPlayerStat()
			// method
			slist.removeIf(ss -> ss.getName().startsWith("villan"));
			readSensors(true, slist);

			slist = allSensors.stream().filter(ss -> ss.isButtonArea()).collect(Collectors.toList());
			readSensors(false, slist);

			pokerSimulator.setTablePosition(getDealerButtonPosition(), getActiveVillans());
			pokerSimulator.setPotValue(getSensor("pot").getNumericOCR());
			pokerSimulator.setCallValue(getSensor("hero.call").getNumericOCR());
			pokerSimulator.setHeroChips(getSensor("hero.chips").getNumericOCR());
			pokerSimulator.setRaiseValue(getSensor("hero.raise").getNumericOCR());
		}

		// cards areas sensor will perform a simulation
		if (TYPE_CARDS.equals(type)) {
			pokerSimulator.cardsBuffer.clear();

			// 1 step update enable/disable area
			slist = allSensors.stream().filter(ss -> ss.isCardArea()).collect(Collectors.toList());
			readSensors(false, slist);

			// second step, read Ocr
			slist = allSensors.stream().filter(ss -> ss.isHoleCard() || ss.isComunityCard())
					.collect(Collectors.toList());
			readSensors(true, slist);
			for (ScreenSensor ss : slist) {
				String ocr = ss.getOCR();
				if (ocr != null)
					pokerSimulator.cardsBuffer.put(ss.getName(), ocr);
			}

			pokerSimulator.setNunOfOpponets(getActiveVillans());
			pokerSimulator.runSimulation();
		}
	}

	/**
	 * Perform the read operation for all {@link ScreenSensor} passed int the list argument.
	 * 
	 * @param read - <code>true</code> to perform OCR operation over the selected sensors. <code>false</code> only
	 *        capture the image
	 * @param list - list of sensors to capture
	 * @see ScreenSensor#capture(boolean)
	 * @since 2.3
	 */
	public void readSensors(boolean read, List<ScreenSensor> list) {
		setStandByBorder();
		for (ScreenSensor ss : list) {
			ss.setBorder(read ? readingBorder : lookingBorder);
			ss.capture(read, readSource);
			// update the enable/disable status.
			pokerSimulator.sensorStatus.put(ss.getName(), ss.isEnabled());
			// Measure only effective lecture
			if (ss.isEnabled() && ss.getOCRPerformanceTime() > 0) {
				performaceStatistic.addValue(ss.getOCRPerformanceTime());
			}
		}
		setStandByBorder();
	}

	public void setReadSourceFile(File srcFile) {
		this.readSourceFile = srcFile;
	}

	public File getReadSourceFile() {
		return readSourceFile;
	}
	
	public void testConfigurationFileSensorsAreas() {
		List<ScreenSensor> slist = screenSensors.values().stream().collect(Collectors.toList());
		String curSour = readSource;
		readSource = FROM_FILE;
		readSensors(true, slist);
		readSource = curSour;
	}

	public void testScreenShotsSensorsAreas() {
		List<ScreenSensor> slist = screenSensors.values().stream().collect(Collectors.toList());
		String curSour = readSource;
		readSource = FROM_SCREENSHOT;
		readSensors(true, slist);
		readSource = curSour;
	}

	private void setStandByBorder() {
		screenSensors.values().stream().forEach(ss -> ss.setBorder(standByBorder));
	}

	/**
	 * Create the array of sensors seated in the {@link ShapeAreas}.
	 * <p>
	 * Don't use this method directly. use {@link Trooper#setEnvironment(DrawingPanel)}
	 * 
	 * @param areas - the Environment
	 */
	protected void setShapeAreas(ShapeAreas areas) {
		this.screenAreas = areas;
		this.screenSensors.clear();
		for (Shape shape : screenAreas.getShapes().values()) {
			ScreenSensor ss = new ScreenSensor(this, shape);
			screenSensors.put(ss.getName(), ss);
		}
		setStandByBorder();
	}
}
