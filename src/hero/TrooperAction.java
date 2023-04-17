package hero;

/**
 * this class encapsule all required values for the trooper to perform an action.
 * 
 */
public class TrooperAction {

	public static final TrooperAction FOLD = new TrooperAction("fold", 0d);
	public static final TrooperAction CHECK = new TrooperAction("call", 0d);

	/**
	 * the name for this action
	 */
	public final String name;
	/**
	 * the amount (cost)
	 */
	public final double amount;
	/**
	 * the string secuence for {@link RobotActuator} class
	 */
	public final String robotCommand;
	/**
	 * cmputed expected value for this action
	 */
	public double expectedValue;

	/**
	 * Constructor. the robot comand for this action will be the name
	 * 
	 * @param name The action's name.
	 * @param amount - the cost for this action.
	 */
	public TrooperAction(String name, double amount) {
		this(name, name, amount);
	}

	/**
	 * Constructor.
	 * 
	 * @param name The action's name.
	 * @param robotCmd The command secuence for the {@link RobotActuator} class
	 * @param amount The action's amount.
	 */
	public TrooperAction(String name, String robotCmd, double amount) {
		this.name = name;
		this.robotCommand = robotCmd;
		this.amount = amount;
	}

	@Override
	public String toString() {
		return String.format("%s %4.2f", name, amount);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TrooperAction))
			return false;
		return ((TrooperAction) obj).toString().equals(toString());
	}
}
