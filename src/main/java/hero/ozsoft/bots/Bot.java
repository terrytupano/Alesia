
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

	protected static Hashtable<String, Object> parm1 = new Hashtable<>();
	protected PokerSimulator pokerSimulator;
	protected Trooper trooper;
	protected Player player;
	protected int bigBlind;
	protected int dealerChair;
	protected int pot;
	protected int buyIn;
	protected UoAHand myHole, communityHand, hand;
	protected Table table;
	private int prevCash;

	public TreeMap<String, Integer> simulationVariables;
	public String trooperName;

	/** Number of hole cards. */
	protected static final int HOLE_CARDS = 2;
	/** the villains for this bot. e.g if this bot is Oscar, Hero is a villains */
	protected List<Player> villains;
	/** poker street. preFlop=0, Flop=1 ... */
	protected int street = 0;
	/** keep track the current match cost. the cumulative cost of all actions */
	protected int matchCost = 0;
	/** track the # of hands */
	private int handsT;

	@Override
	public void actorRotated(Player actor) {
		// Auto-generated method stub
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		this.communityHand = hand;
		this.hand = new UoAHand(myHole.toString() + " " + hand.toString());
		this.pot = pot;
	}

	public SimulationResult getBankrollSnapSchot() {
		String variables = simulationVariables.toString();
		variables = variables.substring(1, variables.length() - 1);
		SimulationResult statistic = SimulationResult.create("trooper", trooperName, "variables", variables);
		statistic.set("tableId", table.getTableId());
		statistic.set("hands", handsT);
		statistic.set("tables", 1);
		double wins = player.getCash() - buyIn;
		statistic.set("wins", wins);
		return statistic;
	}

	/**
	 * bassed on the incoming arguments, prepare al sensors of the currnet instace
	 * of {@link PokerSimulator}
	 * 
	 * @param minBet         - the min bet
	 * @param currentBet     - the curent bet
	 * @param allowedActions - the list of alloed actions
	 */
	public void activeteSensors(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		int raiseValue = Math.min(minBet * 2, player.getCash());

		boolean callBol = allowedActions.contains(PlayerAction.CALL) || allowedActions.contains(PlayerAction.CHECK);
		boolean raiseBol = allowedActions.contains(PlayerAction.RAISE)
				|| allowedActions.contains(PlayerAction.BET) && player.getCash() >= raiseValue;
		boolean potBol = raiseBol && player.getCash() >= pot;
		boolean allinBol = raiseBol && player.getCash() >= minBet;

		// A bet is the initial amount and a raise is anything on top of this
		pokerSimulator.sensorStatus.put("raise.pot", potBol);
		pokerSimulator.sensorStatus.put("raise.allin", allinBol);
		pokerSimulator.sensorStatus.put("raise.slider", allinBol);

		// the trooper dont take into account call/check/raise enabled/disabel status.
		// there are actives and/or mutate the text to reflext valid acction
		// call/check are the same
		// check: call buton is active. callvalue=0
		// call: call button is active. callvalue=minbet
		pokerSimulator.callValue = minBet;
		if (allowedActions.contains(PlayerAction.CHECK))
			pokerSimulator.callValue = 0;

		if (!callBol || !raiseBol)
			System.out.println("HeroBot.act()");

		pokerSimulator.potValue = pot;
		pokerSimulator.heroChips = player.getCash();
		pokerSimulator.raiseValue = raiseValue;
		// TODO: hasCard method don't say if the villain folded his cards
		// -------------------------------------------------- !!!!!!!!!
		int villans = (int) villains.stream().filter(p -> p.hasCards()).count();
		// pokerSimulator.setNunOfOpponets(villans);
		pokerSimulator.setTablePosition(dealerChair, player.getChair(), villans);
	}

	/**
	 * return a {@link PlayerAction} bade on the corresponding
	 * {@link TrooperAction}. the method is simply a translation
	 * 
	 * @param trooperAction  - the selected TrooperAction
	 * @param allowedActions - the list of PlayerActions (dicctionary)
	 * @return the PlayerAction
	 */
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
	 * Equivalent of {@link PokerSimulator#loadActions(double, PokerSimulator)}
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
		List<Double> doubles = PokerSimulator.getRaiseSteps(bet, cashToBet);

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
	 * configure this Bot instace and return the associated internal {@link Trooper}
	 * instance configured to run as Bot inside the
	 * 
	 * @param table            - the environment in witch the trooper run
	 * @param trooperParameter - the parameters that the trooper must follow.
	 * 
	 * @return the trooper
	 */

	public Trooper configureBot(Table table, TrooperParameter trooperParameter,
			SimulationParameters simulationParameters) {
		this.table = table;
		this.trooper = new Trooper(trooperParameter);
		this.trooper.setSimulationTable(table);
		this.pokerSimulator = trooper.getPokerSimulator();
		this.trooperName = trooperParameter.getString("trooper");
		this.simulationVariables = new TreeMap<>();

		// according to the variable comma separated values (1 or more) this simulation
		// is simple or multi variable simulation
		String[] variables = simulationParameters.getVariables();
		for (String var : variables) {
			simulationVariables.put(var, Integer.valueOf(trooperParameter.getString(var)));
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
			// clean myHole to allow correct traking in playerUpdated method
			myHole = new UoAHand();
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
	public void handStarted(Player dealer) {
		this.dealerChair = dealer.getChair();
		pokerSimulator.bigBlind = bigBlind;
		pokerSimulator.smallBlind = bigBlind / 2;
		pokerSimulator.buyIn = buyIn;
		pokerSimulator.newHand();
	}

	@Override
	public void playerUpdated(Player player) {
		// update myHole cards only once. thas allow correct traking of # of hands
		// played by this trooper. a hand is whe the trooper has cards. whet the trooper
		// whit his card do, is here not important.
		if (trooperName.equals(player.getName()) && player.getHand().size() == HOLE_CARDS && myHole.size() == 0) {
			handsT++;
			this.myHole = player.getHand();
		}
	}
}
