
package hero.ozsoft.bots;

import java.util.*;

import datasource.*;
import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.*;
import hero.ozsoft.actions.*;

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
	/** the villains for this bot. e.g if this bot is Oscar, Hero is a villains */
	protected List<Player> villains;
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
	protected Table table;
	protected HashMap<String, Integer> simulationVariables;
	private int prevCash;
	/** track the # of hands */
	private int handsT;

	@Override
	public void actorRotated(Player actor) {
		// TODO Auto-generated method stub
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		this.communityHand = hand;
		this.hand = new UoAHand(myHole.toString() + " " + hand.toString());
		this.pot = pot + bet;
	}

	public SimulationResult getBackrollSnapSchot() {
		SimulationResult statistic = SimulationResult.create("trooper", trooperName, "variables",
				simulationVariables.toString());

		statistic.set("tableId", table.getTableId());
		statistic.set("hands", handsT);
		double wins = player.getCash() - buyIn;
		statistic.set("wins", wins);
		double ratio = wins / handsT;
		statistic.set("ratio", ratio);

		// new simulation parameters
		for (String key : simulationVariables.keySet()) {
			Integer newValue = table.getShuffleVariable();
			simulationVariables.put(key, newValue);
		}

		return statistic;
	}

	public PlayerAction getPlayerAction(TrooperAction trooperAction, Set<PlayerAction> allowedActions) {
		if (trooperAction.equals(TrooperAction.FOLD))
			return PlayerAction.FOLD;
		if (trooperAction.equals(TrooperAction.CHECK))
			return PlayerAction.CHECK;
		if (trooperAction.name.equals("call") && trooperAction.amount > 0) {
			if (allowedActions.contains(PlayerAction.CALL))
				return new CallAction((int) trooperAction.amount);
			if (allowedActions.contains(PlayerAction.BET))
				return new BetAction((int) trooperAction.amount);
		}
		if (trooperAction.name.equals("raise") || trooperAction.name.equals("pot")
				|| trooperAction.name.equals("allIn")) {
			if (allowedActions.contains(PlayerAction.RAISE))
				return new RaiseAction((int) trooperAction.amount);
			if (allowedActions.contains(PlayerAction.BET))
				return new BetAction((int) trooperAction.amount);
		}

		throw new IllegalArgumentException("No correct action selected. Trooper action was" + trooperAction);
	}

	/**
	 * Equivalent of Trooper.loadActions(double)
	 * 
	 * @param minBet         - the minimum bet
	 * @param currentBet     - the current bet
	 * @param cashToBet      - the cash to bet
	 * @param allowedActions - the actions
	 * 
	 * @return available actions to execute
	 */
	public static List<TrooperAction> loadActions(int minBet, int currentBet, double cashToBet,
			Set<PlayerAction> allowedActions) {
		List<TrooperAction> actions = new ArrayList<>();

		int bet = Math.max(minBet, currentBet);
		List<Double> doubles = Trooper.getRaiseSteps(bet, cashToBet);

		if (allowedActions.contains(PlayerAction.CHECK))
			actions.add(TrooperAction.CHECK);
		if (allowedActions.contains(PlayerAction.CALL))
			actions.add(new TrooperAction("call", 0));
		if (allowedActions.contains(PlayerAction.BET) || allowedActions.contains(PlayerAction.RAISE))
			doubles.forEach(d -> actions.add(new TrooperAction("raise", d)));

		// if there is no more option, fold
		if (actions.isEmpty())
			actions.add(TrooperAction.FOLD);

		return actions;
	}

	/**
	 * return a {@link Trooper} instance configured to run as Bot inside the
	 * simulation table.
	 * 
	 * @param table    - the environment in witch the trooper run
	 * @param trooperP - the parameters that the trooper must follow.
	 * 
	 * @return the trooper
	 */

	public Trooper getSimulationTrooper(Table table, TrooperParameter trooperP,
			SimulationParameters simulationParameters) {
		this.table = table;
		this.trooper = new Trooper();
		this.trooper.setSimulationTable(table);
		this.pokerSimulator = trooper.getPokerSimulator();
		this.trooperName = trooperP.getString("trooper");
		this.simulationVariables = new HashMap<>();

		// according to the variable comma separated values (1 or more) this simulation
		// is simple or multi variable simulation
		String[] variables = simulationParameters.getString("simulationVariable").split(",");
		for (String var : variables) {
			simulationVariables.put(var, Integer.valueOf(trooperP.getString(var)));
		}

		return trooper;
	}

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		this.villains = new ArrayList<>(players);
		this.player = players.stream().filter(p -> trooperName.equals(p.getName())).findFirst().get();
		villains.remove(player);
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
