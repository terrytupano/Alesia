package plugins.hero;

import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Base class to send programaticily events throw the mouse or the keyboard.
 * each action recived by this class is the name of the sensor to perform the
 * action.
 * <p>
 * a secuence of 1 o more action separeted by ; whit the following format:
 * <p>
 * <code>action_name;action_name:c=#;action_name:k=text</code>
 * <li>action name alone - perform 1 click using the mouse over the action area.
 * <li>action_name,dc - Perform a double click over the action area
 * <li>action_name,c=# - Perform the number # of click over the action area
 * <li>action_name,k=text - write the text text. The area must be previously
 * selected using double or simple click
 * 
 * @author terry
 *
 */
@SuppressWarnings("deprecation")
public class RobotActuator {

	private Robot robot;
	private int mouseDelay = 200;
	private int keyStrokeDelay = 20;
	private Trooper trooper;

	public RobotActuator(Trooper trooper) {
		this.trooper = trooper;
		this.robot = Hero.getNewRobot();
		robot.setAutoDelay(40);
		robot.setAutoWaitForIdle(true);
	}

	/**
	 * Perform the secuence of command asociated with the {@link TrooperAction}
	 * passsed as argument.
	 * 
	 * @see #perform(String)
	 * @param trooperAction - the action
	 */
	public void perform(TrooperAction trooperAction) {
		perform(trooperAction.robotCommand);
	}

	/**
	 * Perform the secuence of commands passed as argument as argument. The command
	 * structure is in the class documentation. This method dont dont verify the
	 * command format. it will try of fullfill the action. check the logger entry to
	 * verify if the secuence was complete.
	 * 
	 * @param commands - the commands to perform
	 */
	public void perform(String command) {
		String[] commandss = command.split(";");
		for (String cmd : commandss) {
			String temp[] = cmd.split("[,]");
			String action = temp[0];
			String actValue = temp.length > 1 ? temp[1] : "";

			Shape fig = trooper.getSensorsArray().getScreenAreas().getShapes().get(action);
			if (fig == null) {
				Hero.heroLogger.severe("RobotActuator: Action " + action + " not found.");
				continue;
			}

			Point p = fig.getRandomPoint();
//			System.out.println(p);
			mouseMove(p.x, p.y);

			// perform double clicks
			if (actValue.startsWith("dc")) {
				doubleClick();
				Hero.heroLogger.fine("Action " + action + " double click performed.");
				continue;
			}

			// perform clicks by click number or action name only
			if (actValue.startsWith("c=") || actValue.equals("")) {
				int clicks = actValue.equals("") ? 1 : Integer.parseInt(actValue.substring(2));
				for (int c = 0; c < clicks; c++) {
					doClick();
				}
				Hero.heroLogger.fine("Action " + action + " " + clicks + " click(s)  performed.");
				continue;
			}

			// write the text
			if (actValue.startsWith("k=")) {
				String text = actValue.substring(2);
				type(text);
				Hero.heroLogger.fine("Action " + action + " Text= " + text + " writed.");
				continue;
			}
		}
	}

	/**
	 * Perform mouse left click. In test mode, this method send the
	 * {@link KeyEvent#VK_CONTROL} using the keyboard to signal only. the property
	 * "show location of pointer when press control key" must be set on in mouse
	 * properties
	 */
	public void doClick() {
		if (!trooper.getSensorsArray().isLive()) {
			type(KeyEvent.VK_CONTROL);
			return;
		}
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.delay(mouseDelay);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		robot.delay(mouseDelay);
	}

	/**
	 * Perform mouse left click. In test mode, this method send the
	 * {@link KeyEvent#VK_CONTROL} using the keyboard to signal only. the property
	 * "show location of pointer when press control key" must be set on in mouse
	 * properties
	 */
	public void doubleClick() {
		if (!trooper.getSensorsArray().isLive()) {
			type(KeyEvent.VK_CONTROL);
			return;
		}
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.delay(keyStrokeDelay);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
		robot.delay(keyStrokeDelay);
		robot.mousePress(InputEvent.BUTTON1_MASK);
		robot.delay(keyStrokeDelay);
		robot.mouseRelease(InputEvent.BUTTON1_MASK);
	}

	/**
	 * Same as {@link Robot#mouseMove(int, int)} but whit safe dalay
	 * 
	 * @param x - X Position
	 * @param y - Y Position
	 */
	public void mouseMove(int x, int y) {
		robot.mouseMove(x, y);
		robot.delay(mouseDelay);
	}

	/**
	 * Perform key press on the keyboard. This key must be any of the
	 * {@link KeyEvent} key codes
	 * 
	 * @param vk - the key code to type
	 */
	public void type(int vk) {
		robot.keyPress(vk);
		robot.delay(keyStrokeDelay);
		robot.keyRelease(vk);
		robot.delay(keyStrokeDelay);
	}

	/**
	 * Type the text <code>str</code> using the keyboard. This method only process
	 * the characters from A-Z and numbers. To sent especial key, use
	 * {@link #type(int)} method.
	 * 
	 * @param str - text to type
	 */
	public void type(String str) {
		byte[] bytes = str.getBytes();
		for (byte b : bytes) {
			int code = b;
			// A-Z convertion
			if ((code > 96 && code < 123)) {
				code = code - 32;
			}
			if (!trooper.getSensorsArray().isLive())
				type(KeyEvent.VK_CONTROL);
			else
				type(code);
			// robot.delay(mouseDelay);
			// robot.keyPress(code);
			// robot.delay(keyStrokeDelay);
			// robot.keyRelease(code);
		}
	}
}