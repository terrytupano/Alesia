
package hero.ozsoft.bots;

import java.util.*;

import datasource.*;
import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.*;

/**
 * Base class for all Texas Hold'em poker bot implementations. this base
 * implementation contain all required variables to allow subclasses record the
 * history of this particular implementation.
 * 
 */
public abstract class Bot implements Client {

	/** Number of hole cards. */
	protected static final int NO_OF_HOLE_CARDS = 2;
	protected static Hashtable<String, Object> parm1 = new Hashtable<>();
	protected PokerSimulator pokerSimulator;
	protected Trooper trooper;
	protected Player player;
	protected List<Player> villans;
	protected int bigBlind;
	protected int dealer;
	protected int pot;
	protected int buyIn;
	protected String trooperName;
	protected UoAHand myHole, communityHand, hand;

	/** poker street. preFlop=0, Flop=1 ... */
	protected int street = 0;
	/** keep track the current match cost. the cumulative cost of all actions */
	protected int matchCost = 0;
	protected TrooperParameter trooperParameter;
	private int prevCash;
	/** track the # of hands */
	private int handsT;
	private String simulationName;
	private String aditionalValue;
	private List<String> simulationVariables;

	@Override
	public void actorRotated(Player actor) {
		// TODO Auto-generated method stub
	}

	public SimulationResult getBackrollSnapSchot() {
		boolean singleVariable = simulationVariables == null;
		SimulationResult statistic = null;
		if (singleVariable) {
			// aditionalvalue is the variable name
			statistic = SimulationResult.create("trooper", trooperName, "aditionalValue",
					trooperParameter.get(aditionalValue));
		} else {
			// build multiAditionalValues field based on simulated field and current values
			Map<String, Object> map = new TreeMap<>();
			for (String sVar : simulationVariables)
				map.put(sVar, trooperParameter.get(sVar));

			// multiAditionalValues is the names of the list of variables
			statistic = SimulationResult.create("trooper", trooperName, "multiAditionalValues", map.toString());
		}

		statistic.set("hands", handsT);
		double wins = player.getCash() - buyIn;
		statistic.set("wins", wins);
		double ratio = wins / handsT;
		statistic.set("ratio", ratio);

		// new simulation parameters
		if (!singleVariable) {
			for (String variable : simulationVariables) {
				Integer newValue = Table.getShuffleVariable();
				trooperParameter.set(variable, newValue);
			}
		}

		return statistic;
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		this.communityHand = hand;
		this.hand = new UoAHand(myHole.toString() + " " + hand.toString());
		this.pot = pot + bet;
	}

	/**
	 * return a {@link Trooper} instance configured to run as Bot inside the
	 * simulation table.
	 * 
	 * @param simulationTable - the environment in witch the trooper run
	 * @param trooperP        - the parameters that the trooper must follow.
	 * 
	 * @return the trooper
	 */

	public Trooper getSimulationTrooper(Table simulationTable, TrooperParameter trooperP,
			SimulationParameters simulationParameters) {
		this.trooper = new Trooper();
		this.trooper.setSimulationTable(simulationTable);
		this.pokerSimulator = trooper.getPokerSimulator();
		this.trooperParameter = trooperP;
		this.trooperName = trooperParameter.getString("trooper");

		// simulation name
		this.simulationName = simulationParameters.getString("simulationName");

		// according to the variable comma separated values (1 or more) this simulation
		// is simple or multi variable simulation
		String[] variables = simulationParameters.getString("simulationVariable").split(",");
		if (variables.length == 1) {
			// for single variable simulation
			this.aditionalValue = simulationParameters.getString("simulationVariable");
		} else {
			// for multiple variable simulation
			this.simulationVariables = Arrays.asList(variables);
		}

		return trooper;
	}

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		this.villans = new ArrayList<>(players);
		this.player = players.stream().filter(p -> trooperName.equals(p.getName())).findFirst().get();
		villans.remove(player);
		this.bigBlind = bigBlind;
		this.buyIn = player.getCash();
		this.myHole = new UoAHand();
		this.prevCash = buyIn;
	}

	@Override
	public void messageReceived(String message) {

		// hand track
		// Hand: 1, Hero is the dealer.
		// Hand: 67, the table has less players that allow. Restarting the hole table.
		if (message.startsWith("Hand: ")) {
			// String[] tmp = message.split("[,]");
			// handsT = Integer.parseInt(tmp[0].replace("Hand: ", ""));
		}
		// Tim wins 450.
		if (message.contains("wins ")) {
			String[] tmp = message.split("[ ]");
			String name = tmp[0];

			// when the winner is this player
			if (trooperName.equals(name)) {

			}
			prevCash = player.getCash();
		}

		if (message.contains("is the dealer.")) {
			matchCost = 0;
			handsT++;
		}

		// Claudia deals the Flop.
		if (message.contains("Flop.") || message.contains("Turn.") || message.contains("River.")) {
			street++;
		}

		if (message.contains("Restartting the hole table.")) {
			handsT = 0;
			street = 0;
			prevCash = buyIn;
		}

	}

	@Override
	public void playerActed(Player player) {
		if (trooperName.equals(player.getName())) {
			matchCost = prevCash - player.getCash();
			// System.out.println(trooper + " " + matchCost);
		}
	}

	@Override
	public void playerUpdated(Player player) {
		if (trooperName.equals(player.getName()) && player.getHand().size() == NO_OF_HOLE_CARDS) {
			this.myHole = player.getHand();
		}
	}
}
