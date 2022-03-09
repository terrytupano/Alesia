
package plugins.hero.ozsoft.bots;

import java.util.*;

import org.javalite.activejdbc.*;

import core.*;
import core.datasource.model.*;
import plugins.hero.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.UoALoky.handranking.*;
import plugins.hero.ozsoft.*;

/**
 * Base class for all Texas Hold'em poker bot implementations. this base implementation contain all required variables
 * to allow subclasses record the history of this particular implementation.
 * 
 */
public abstract class Bot implements Client {

	/** Number of hole cards. */
	protected static final int NO_OF_HOLE_CARDS = 2;
	protected static Hashtable<String, Object> parm1 = new Hashtable<>();
	protected PokerSimulator pokerSimulator;
	protected Trooper trooperT;
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

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		this.villans = new ArrayList(players);
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
		// Hand: 67, the table has less players that allow. Restartting the hole table.
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
			backrollSnapSchot();
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

	/**
	 * /** invoqued after contruction. set the simulation parameters for this Bot
	 * 
	 * @param SimulationName - simulation name
	 * @param trooperP - parameters
	 * @param field - field name in {@link TrooperParameter} that is simulated
	 */
	public PokerSimulator setPokerSimulator(String simulationName, TrooperParameter trooperP, String field) {
		this.pokerSimulator = new PokerSimulator();
		this.trooperT = new Trooper(null, pokerSimulator);
		this.simulationName = simulationName;
		this.fieldName = field;
		this.trooperParameter = trooperP;
		this.trooperName = trooperParameter.getString("trooper");
		return pokerSimulator;
	}
	private String fieldName;
	/**
	 * 
	 */
	private void backrollSnapSchot() {
		Alesia.getInstance().openDB("hero");
		LazyList<SimulationResult> last = SimulationResult
				.where("name = ? AND trooper = ?", simulationName, trooperName).orderBy("id DESC").limit(1);
		SimulationResult statistic = SimulationResult.create("name", simulationName, "trooper",
				trooperName);

		if (last.size() > 0) {
			statistic.copyFrom(last.get(0));
		} else {
			// insert 0 element for a correct grapichs and posterior list selection
			statistic.set("hands", 0);
			statistic.set("wins", 0);
			statistic.set("ratio", 0);
			statistic.setString("aditionalValue", trooperParameter.get(fieldName));
			statistic.insert();
			statistic = SimulationResult.create("name", simulationName, "trooper", trooperName);
		}

		int hands = handsT + (statistic.getInteger("hands") == null ? 0 : statistic.getInteger("hands"));
		statistic.set("hands", hands);
		double wins = statistic.getDouble("wins") == null ? 0 : statistic.getDouble("wins");
		wins = wins + player.getCash() - buyIn;
		statistic.set("wins", wins);
		double bb = wins / (bigBlind * 1.0);
		statistic.set("ratio", bb / (hands * 1.0));
		statistic.setString("aditionalValue", trooperParameter.get(fieldName));
		statistic.insert();
	}
}
