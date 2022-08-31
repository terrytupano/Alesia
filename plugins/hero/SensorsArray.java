package plugins.hero;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.swing.*;
import javax.swing.border.*;

import org.apache.commons.math3.stat.descriptive.*;

import com.jgoodies.common.base.*;

/**
 * This class control the array of sensor inside of the screen. This class is responsable for reading all the sensor
 * configurated in the {@link DrawingPanel} passsed as argument in the {@link #createSensorsArray(DrawingPanel)} method.
 * <p>
 * althout this class are the eyes of the tropper, numerical values must be retrives throw {@link PokerSimulator}. the
 * poker simulator values are populated during the reading process using the method
 * {@link PokerSimulator#addCard(String, String)} at every time that a change in the enviorement is detected.
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
	 * Read/see only text sensors, names of the villans mainly
	 */
	public final static String TYPE_TEXT = "Text";

	/**
	 * Read/see TODO: read only villans information (all)
	 * 
	 * @see #TYPE_CARDS
	 */
	public final static String TYPE_VILLANS = "Villans";

	/**
	 * Read/see only cards areas. this type is only for hero cards and comunity cards
	 * 
	 */
	public final static String TYPE_CARDS = "Cards";
	/**
	 * Read/see only actions areas (call button, raise, continue)
	 */
	public final static String TYPE_ACTIONS = "Actions";
	private TreeMap<String, ScreenSensor> screenSensors;
	private Robot robot;
	private Border readingBorder, lookingBorder, standByBorder;
	private PokerSimulator pokerSimulator;
	private ShapeAreas screenAreas;
	DescriptiveStatistics tesseractTime = new DescriptiveStatistics(10);

	public SensorsArray() {
		this.pokerSimulator = new PokerSimulator();
		this.robot = Hero.getNewRobot();
		this.readingBorder = new LineBorder(Color.BLUE, 2);
		this.lookingBorder = new LineBorder(Color.ORANGE, 2);
		this.standByBorder = new LineBorder(new JPanel().getBackground(), 2);
		this.screenSensors = new TreeMap<>();
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
	public void clearEnviorement() {
		screenSensors.values().forEach((ss) -> ss.clearEnviorement());
		pokerSimulator.clearEnviorement();
	}

	/**
	 * Return the number of current villans active seats.
	 * 
	 * @see #isSeatActive(int)
	 * @return - num of active villans + me
	 */
	public int getActiveSeats() {
		int av = 0;
		for (int i = 1; i <= getVillans(); i++) {
			av += isSeatActive(i) ? 1 : 0;
		}
		// at this point at least must be 1 villan active set
		if (av == 0)
			Hero.heroLogger.severe("Fail to detect active seats");
		return av;
	}

	/**
	 * Return the number of current active villans.
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

	public PokerSimulator getPokerSimulator() {
		return pokerSimulator;
	}
	public Robot getRobot() {
		return robot;
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
	 * This method return a list of all action sensors currently enables. For example. if a enviorement with 10 binary
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
	 * retriva a list of sensor.s names acording to the <code>subString</code> argument.
	 * <p>
	 * For example, to retrive all "call" sensors, pass to this method ".call" will return sensor hero.call,
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
	 * Return the number of villans configurated in this table.
	 * 
	 * @see HeroPanel
	 * 
	 * @return total villans
	 */
	public int getVillans() {
		return (int) screenSensors.keySet().stream().filter(sn -> sn.startsWith("villan") && sn.contains("name"))
				.count();
	}
	/**
	 * return <code>true</code> if the player identifyed as id argument is active (hero or villan). A PLAYER IS ACTIVE
	 * IF HE HAS CARDS IN THIS HANDS. if a player fold his card. this method will not count that player. from this
	 * method point of view. the player is in tha game, but in this particular moment are not active.
	 * 
	 * @param id - villan id or 0 for hero
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
	 * this method check the villan name sensor and the villan chip sensor. if both are active, the seat is active.
	 * <p>
	 * from this method point of view, there are a villan sittin on a seat currently playing or not. maybe he abandom
	 * the action
	 * 
	 * @param villanId - the seat as configured in the ppt file. villan1 is at hero.s left
	 * @see #getActiveVillans()
	 * @return numers of villans active seats
	 */
	public boolean isSeatActive(int villanId) {
		// ScreenSensor vname = getSensor("villan" + villanId + ".name");
		// ScreenSensor vchip = getSensor("villan" + villanId + ".chips");
		// return vname.isEnabled() && vchip.isEnabled();

		// TODO: temporal until now, i.m not using the name sensor for nothing. so, an active seat is when the chip
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
	 * Perform read operation on the {@link ScreenSensor} acoording to the type of the sensor. The type can be any of
	 * TYPE_ global constatn passed as argument. This method perform the OCR operation on the selected areas and update
	 * the {@link PokerSimulator} if it.s necesary.
	 * <p>
	 * After this method execution, the simulator reflect the actual game status
	 * 
	 * @param sensors - type of sensor to read
	 */
	public void read(String type) {
		Collection<ScreenSensor> allSensors = screenSensors.values();
		List<ScreenSensor> slist = new ArrayList<ScreenSensor>();

		// ation areas
		if (TYPE_ACTIONS.equals(type)) {
			slist = allSensors.stream().filter(ss -> ss.isActionArea()).collect(Collectors.toList());
			slist.add(getSensor("hero.card1"));
			slist.add(getSensor("hero.card2"));
			readSensors(true, slist);
		}

		// numeric types retrive all numers and update poker simulator
		if (TYPE_NUMBERS.equals(type)) {
			slist = allSensors.stream().filter(ss -> ss.isNumericArea()).collect(Collectors.toList());
			// remove villas sensor. villans sensor are update calling readPlayerStat() method
			slist.removeIf(ss -> ss.getName().startsWith("villan"));
			readSensors(true, slist);

			slist = allSensors.stream().filter(ss -> ss.isButtonArea()).collect(Collectors.toList());
			readSensors(false, slist);

			pokerSimulator.setTablePosition(getDealerButtonPosition(), getActiveVillans());
			pokerSimulator.setPotValue(getSensor("pot").getNumericOCR());
			pokerSimulator.setCallValue(getSensor("hero.call").getNumericOCR());
			pokerSimulator.setHeroChips(getSensor("hero.chips").getNumericOCR());
			pokerSimulator.setRaiseValue(getSensor("hero.raise").getNumericOCR());

			// the report is update at the end of this method
			// pokerSimulator.updateReport();
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
			ss.capture(read);
			// update the enable/disable status.
			pokerSimulator.sensorStatus.put(ss.getName(), ss.isEnabled());
			// mesure only efective lecture
			if (ss.isEnabled() && ss.getOCRPerformanceTime() > 0) {
				tesseractTime.addValue(ss.getOCRPerformanceTime());
			}
		}
		setStandByBorder();
	}

	/**
	 * this method read all {@link #TYPE_CARDS} areas from the .ppt file and perform the standar ocr operation. fur test
	 * purporse
	 */
	public void testAreas() {
		Hero.isTestMode = true;
		List<ScreenSensor> slist = screenSensors.values().stream().collect(Collectors.toList());
		readSensors(true, slist);
	}

	private void setStandByBorder() {
		screenSensors.values().stream().forEach(ss -> ss.setBorder(standByBorder));
	}

	/**
	 * Create the array of sensors setted in the {@link ShapeAreas}.
	 * <p>
	 * dont use this method directly. use {@link Trooper#setEnviorement(DrawingPanel)}
	 * 
	 * @param areas - the enviorement
	 */
	protected void setShapeAreas(ShapeAreas areas) {
		this.screenAreas = areas;
		this.screenSensors.clear();
		for (Shape shape : screenAreas.getShapes().values()) {
			ScreenSensor ss = new ScreenSensor(this, shape);
			screenSensors.put(ss.getName(), ss);
		}
		setStandByBorder();
		// pokerSimulator.clearEnviorement();
	}
}
