
package hero.ozsoft.bots;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.javalite.activejdbc.LazyList;

import core.*;
import datasource.*;

import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.*;

/**
 * Base class for all Texas Hold'em poker bot implementations. this base implementation contain all required variables
 * to allow subclasses record the history of this particular implementation.
 * 
 */
public abstract class Bot implements Client {

	public class SimulationVariable {
		public String name;
		public int upperBound;

		SimulationVariable(String name, int upperBound) {
			this.name = name;
			this.upperBound = upperBound;
		}
	}
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
	private String aditionalValue;;

	private List<SimulationVariable> simulationVariables;

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
	
	/**
	 * return a {@link Trooper} instance configured to run as Bot inside the simulation table.
	 * 
	 * @param simulationTable - the environment in witch the trooper run
	 * @param trooperP - the parameters that the trooper must follow.
	 * 
	 * @return the trooper
	 */

	public Trooper getSimulationTrooper(Table simulationTable, TrooperParameter trooperP) {
		this.trooper = new Trooper();
		this.trooper.setSimulationTable(simulationTable);
		this.pokerSimulator = trooper.getPokerSimulator();
		this.trooperParameter = trooperP;
		this.trooperName = trooperParameter.getString("trooper");

		// simulation name 
		this.simulationName = "Oportunities - phi2 flop turn und river";

		// for single variable simulation
		this.aditionalValue = "phi2";

		// for mutivariable simulation
//		 this.simulationVariables = new ArrayList<>();
//		 simulationVariables.add(new SimulationVariable("phi", 16));
//		 simulationVariables.add(new SimulationVariable("phi2", 16));
//		 simulationVariables.add(new SimulationVariable("phi3", 16));
//		 simulationVariables.add(new SimulationVariable("phi4", 16));

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

	private void backrollSnapSchot() {
		if (simulationVariables == null)
			singleVariableBackrollSnapSchot();
		else
			multiVariableBackrollSnapSchot();
	}

	private void multiVariableBackrollSnapSchot() {
		Alesia.getInstance().openDB("hero");

		long cnt = SimulationResult.count("name = ? ", simulationName);

		// if no element is present, add standard 0 elements
		if (cnt == 0) {
			SimulationResult sts = SimulationResult.create("name", simulationName, "trooper", "Hero");
			sts.set("hands", 0);
			sts.set("wins", 0);
			sts.set("ratio", 0);
			sts.insert();
		}

		// build multiAditionalValues field based rotation of simulation fields
		Map<String, Object> map = new TreeMap<>();
		for (SimulationVariable sVar : simulationVariables)
			map.put(sVar.name, trooperParameter.get(sVar.name));
		String addVal = map.toString();

		SimulationResult statistic = SimulationResult.findFirst("name = ? AND multiAditionalValues = ?", simulationName,
				addVal);
		if (statistic == null) {
			statistic = SimulationResult.create("name", simulationName, "trooper", "", "multiAditionalValues", addVal);
		}

		int hands = handsT + (statistic.getInteger("hands") == null ? 0 : statistic.getInteger("hands"));
		statistic.set("hands", hands);
		double wins = statistic.getDouble("wins") == null ? 0 : statistic.getDouble("wins");
		wins = wins + player.getCash() - buyIn;
		statistic.set("wins", wins);
		double bb = wins / (bigBlind * 1.0);
		statistic.set("ratio", bb / (hands * 1.0));
		statistic.save();

		// only hero rotate the list of variables. the rotate values are static stored. so the other bot can pic the
		// rest of variables. this avoid collisions between bots
		// WARNIG: this code fragment take into account that hero always is the first in the rotation. (because is sit
		// in chair 0)
		if ("Hero".equals(trooperName)) {
			varAndList.clear();
			int count = 8;
			for (SimulationVariable sVar : simulationVariables) {
				List<Integer> list = new ArrayList<>();
				int rb = trooperParameter.getInteger(sVar.name);
				int inc = sVar.upperBound / count;
				for (int i = 0; i < count; i++) {
					rb = rb + inc;
					if (rb > sVar.upperBound)
						rb = inc;
					list.add(rb);
				}
				Collections.shuffle(list);
				varAndList.put(sVar.name, list);
			}
			// System.out.println("shuffled by Hero: " + varAndList);
		}

		for (SimulationVariable sVar : simulationVariables) {
			List<Integer> sList = varAndList.get(sVar.name);
			Integer val = sList.remove(0);
			trooperParameter.set(sVar.name, val);
		}
		// System.out.println("after " + trooperName +" selection "+ varAndList);
		trooperParameter.save();
	}

	// private static List<Integer> shuffleList = new ArrayList<>();
	private static Map<String, List<Integer>> varAndList = new Hashtable<>();

	private void singleVariableBackrollSnapSchot() {
		Alesia.getInstance().openDB("hero");
		LazyList<SimulationResult> last = SimulationResult
				.where("name = ? AND trooper = ?", simulationName, trooperName).orderBy("id DESC").limit(1);
		SimulationResult statistic = SimulationResult.create("name", simulationName, "trooper", trooperName);

		if (last.size() > 0) {
			statistic.copyFrom(last.get(0));
		} else {
			// insert 0 element for a correct graphics and posterior list selection
			statistic.set("hands", 0);
			statistic.set("wins", 0);
			statistic.set("ratio", 0);
			statistic.setString("aditionalValue", trooperParameter.get(aditionalValue));
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
		statistic.setString("aditionalValue", trooperParameter.get(aditionalValue));
		statistic.insert();
	}
}
