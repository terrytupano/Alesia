
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
	protected Trooper trooper;
	protected Player player;
	protected List<Player> villans;
	protected int bigBlind;
	protected int dealer;
	protected int pot;
	protected int buyIn;
	protected String playerName;
	protected UoAHand myHole, communityHand, hand;
	
	// TODO: delete
	protected Properties simulationParams = new Properties(); 

	/** poker street. preFlop=0, Flop=1 ... */
	protected int street = 0;

	/** keep track the current match cost. the cumulative cost of all actions */
	protected int matchCost = 0;

	/** stored simulation parameters for this bot */
	protected SimulatorClient simulatorClient;

	private int prevCash;
	
	protected String bankRollDescription;
	
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
		this.player = players.stream().filter(p -> playerName.equals(p.getName())).findFirst().get();
		villans.remove(player);
		this.bigBlind = bigBlind;
		this.buyIn = player.getCash();
		this.myHole = new UoAHand();
		this.prevCash = buyIn;
	}

	@Override
	public void messageReceived(String message) {
		/**
		 * invoqued directly after class constructor and set the player name. <b>Hero</b> is allawys Hero and the
		 * villans haven other names.
		 */
		if (message.startsWith("PlayerName=")) {
			this.playerName = message.split("[=]")[1];
			simulatorClient = SimulatorClient.findFirst("playerName = ?", playerName);
		}

		if (message.contains("wins ")) {
			String[] tmp = message.split("[ ]");
			String name = tmp[0];

			// when the winner is this player
			if (playerName.equals(name)) {

			}
			// update Stats for all players
//			saveObservations();
			simulationParams.clear();
			prevCash = player.getCash();
		}

		if (message.contains("is the dealer.")) {
			matchCost = 0;
			handsCounter++;
		}

		if (message.contains("Flop.") || message.contains("Turn.") || message.contains("River.")) {
			street++;
		}

		if (message.contains("Restartting the hole table.")) {
			backrollSnapSchot();
			handsCounter = 1;
			street = 0;
			prevCash = buyIn;
		}

	}

	/** internal hands counter */
	private int handsCounter;

	@Override
	public void playerActed(Player player) {
		if (playerName.equals(player.getName())) {
			matchCost = prevCash - player.getCash();
			// System.out.println(playerName + " " + matchCost);
		}
	}

	@Override
	public void playerUpdated(Player player) {
		if (playerName.equals(player.getName()) && player.getHand().size() == NO_OF_HOLE_CARDS) {
			this.myHole = player.getHand();
		}
	}

	/**
	 * invoqued after contruction. set the trooper and the asociated simulator for this boot
	 * 
	 * @param pokerSimulator - intance of {@link PokerSimulator}
	 * @param trooper - Instace of {@link Trooper}
	 */
	public void setPokerSimulator(PokerSimulator pokerSimulator, Trooper trooper) {
		this.pokerSimulator = pokerSimulator;
		this.trooper = trooper;
	}

	/**
	 * 
	 */
	private void backrollSnapSchot() {
		Alesia.getInstance().openDB("hero");
		String nam = "Bankroll";
		String sessionID = "" + System.currentTimeMillis();
		LazyList<SimulatorStatistic> last = SimulatorStatistic
				.where("name = ? AND player = ? AND value < ?", nam, playerName, sessionID).orderBy("name, value DESC")
				.limit(1);
		SimulatorStatistic statistic;
		if (last.size() == 0) {
			statistic = SimulatorStatistic.create("name", nam, "value", sessionID);
		} else {
			statistic = last.get(0);
			statistic.set("value", sessionID);
		}
		statistic.set("player", playerName);
		int hands = handsCounter + (statistic.getInteger("hands") == null ? 0 : statistic.getInteger("hands"));
		statistic.set("hands", hands);
		double wins = statistic.getDouble("wins") == null ? 0 : statistic.getDouble("wins");
		wins = wins + player.getCash() - buyIn;
		statistic.set("wins", wins);
		double bb = wins / (bigBlind * 1.0);
		statistic.set("ratio", bb / (hands * 1.0));
		statistic.setString("aditionalValue", bankRollDescription);
		statistic.insert();
	}

	/**
	 * save the the values of global variables {@link #wins} and {@link Hand}
	 */
	private void saveObservations() {
		// save observations only when the event apperar
		if (simulationParams.size() == 0)
			return;

		Alesia.getInstance().openDB("hero");
		SimulatorStatistic statistic = SimulatorStatistic.findOrInit("name", simulationParams.get("name"), "value",
				simulationParams.get("value"));

		// change the name to *name when many boot update the same name, value pair.
		// String pname = statistic.getString("player") == null ? playerName : statistic.getString("player");
		// if (!playerName.equals(pname))
		// pname = "*" + playerName;

		statistic.set("player", playerName);
		int hands = statistic.getInteger("hands") == null ? 0 : statistic.getInteger("hands");
		statistic.set("hands", ++hands);
		double wins = statistic.getDouble("wins") == null ? 0 : statistic.getDouble("wins");
		wins = wins + player.getCash() - prevCash;
		statistic.set("wins", wins);
		double bb = wins / (bigBlind * 1.0);
		statistic.set("ratio", bb / (hands * 1.0));
		statistic.setString("aditionalValue", simulationParams.get("aditionalValue"));
		statistic.save();
	}
}
