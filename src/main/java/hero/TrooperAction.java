package hero;

import core.*;

/**
 * this class encapsulate all required values for the trooper to perform an action.
 * 
 */
public class TrooperAction {

	public static final TrooperAction FOLD = new TrooperAction("fold", 0d);
	public static final TrooperAction CHECK = new TrooperAction("call", 0d);

	public static final String FLOD = "fold";
	public static final String CALL = "call";
	public static final String RAISE = "raise";
	public static final String BET = "bet";
	public static final String POT = "pot";
	public static final String ALL_IN = "allIn";

	/**
	 * the name for this action
	 */
	public final String name;
	/**
	 * the amount (cost)
	 */
	public final double amount;
	/**
	 * the string sequence for {@link RobotActuator} class
	 */
	public final String robotCommand;
	/**
	 * pot odds are the ratio of the current size of the pot to the cost of this action ammount
	 */
	public double potOdds;

	/**
	 * Constructor. the robot command for this action will be the name
	 * 
	 * @param name   The action's name.
	 * @param amount - the cost for this action.
	 */
	public TrooperAction(String name, double amount) {
		// avoid ammounts like 2,99999 from calculations
		String amountS = TResources.twoDigitFormat.format(amount);

		// boolean isInt = amount == Math.rint(amount);
		// round value to look natural (don't write 12345. write 12340 or 12350)

		String command = name;
		if (POT.equals(name))
			command = "raise.pot;raise";
		if (ALL_IN.equals(name))
			command = "raise.allin;raise";
		if (BET.equals(name))
			command = "raise.text:dc;raise.text:k=" + amountS + ";raise";

		this.amount = amount;
		this.name = name;
		this.robotCommand = command;
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
