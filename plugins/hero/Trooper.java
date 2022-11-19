package plugins.hero;

import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.descriptive.*;
import org.jdesktop.application.*;

import com.javaflair.pokerprophesier.api.adapter.*;

import core.*;
import core.datasource.model.*;
import plugins.hero.ozsoft.*;
import plugins.hero.ozsoft.bots.*;
import plugins.hero.utils.*;

/**
 * this class represent the core of al hero plugins. As a core class, this class dependes of anothers classes in order
 * to build a useful player agent. the followin is a list of class from where the information is retrived, and the
 * actions are performed.
 * <ul>
 * <li>{@link PokerSimulator} - get the numerical values for decition making
 * <li>{@link RobotActuator} - permorm the action sended by this class.
 * <li>{@link SensorsArray} - perform visual operation of the Environment
 * </ul>
 * <p>
 * <b>RULE 1: hero is here to win money. So in order to do that, hero need to fight and stay in the table. </b>
 * <p>
 * to be agree with this rule, this implementation:
 * <ul>
 * <li>Invest his chips only in calculated 0 or positive EV. When the EV for pot odd return an empty list, for example,
 * pot=0 (initial bet and hero is the dealer) the EV function will return negative espectative even with AAs. in this
 * case, the {@link #setPreflopActions()} is called as a last resource.
 * <li>Table Position: In this implementation, the tableposition is irrelevant because the normal odd action take the
 * values of the the pot odd actions are imp the table position are implied in the normal odd actions. this mean, the
 * convination of odd actions and preflophand evaluation has the hero table position already implied.
 * <li>Number of villans: The number of villans is also irrelevant in this implementation because that information is
 * already present in {@link PokerProphesierAdapter}.
 * </ul>
 * 
 * @author terry
 *
 */
public class Trooper extends Task {

	private static DecimalFormat fourDigitFormat = new DecimalFormat("#0.0000");
	private static DecimalFormat twoDigitFormat = new DecimalFormat("#0.00");
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	public static String EXPLANATION = "aa.Troper Explanation";
	public static String STATUS = "aa.Troper Status";

	private PokerSimulator pokerSimulator;
	private SensorsArray sensorsArray;
	private RobotActuator robotActuator;
	private List<TrooperAction> availableActions;
	private int countdown = 5;
	private int handsCounter = 0;
	private long time1;
	private DescriptiveStatistics outGameStats;
	private DescriptiveStatistics trooperPerformance;

	private boolean paused = false;
	private int reactToOportunity;
	private String prevReactionMessage;
	// This variable is ONLY used and cleaned by ensuregametable method
	private String lastHoleCards = "";
	private double maxRekonAmmo;
	private double currentHandCost;
	private double playUntil;
	private long playTime;
	private String subObtimalDist;
	private TrooperParameter trooperParameter;;
	private int numOfVillans;
	private GameRecorder gameRecorder;
	private PreflopCardsModel preflopCardsModel = new PreflopCardsModel("pokerStar");
	private Table simulationTable;

	private long startDate;

	public Trooper() {
		super(Alesia.getInstance());
		this.availableActions = new ArrayList<>();
		this.pokerSimulator = new PokerSimulator();
		this.sensorsArray = new SensorsArray(pokerSimulator);
		this.robotActuator = new RobotActuator(this);
		this.numOfVillans = sensorsArray.getVillans();
		this.gameRecorder = new GameRecorder(this, numOfVillans);
		this.outGameStats = new DescriptiveStatistics(10);
		this.trooperPerformance = new DescriptiveStatistics(10);
		this.handsCounter = 0;
		this.playUntil = 0;
		this.trooperParameter = null;
		this.subObtimalDist = "Triangular";

		// load all preflop ranges
		// this.preFlopCardsDist = new Hashtable<>();
		// TEntry<String, String>[] tarr = PreflopCardsModel.getPreflopList();
		// for (TEntry<String, String> tEntry : tarr) {
		// String rName = tEntry.getKey();
		// preFlopCardsDist.put(rName, new PreflopCardsModel(rName));
		// }
	}

	public void cancelTrooper(boolean interrupt) {
		setVariableAndLog(STATUS, "Trooper Canceled.");
		super.cancel(interrupt);
	}

	/**
	 * return the minimun Tau value of all active villans
	 * 
	 * @return min tau value
	 */
	public int getMinActiveTau() {
		List<GamePlayer> list = gameRecorder.getPlayers();
		int tau = list.stream().filter(p -> p.isActive()).mapToInt(p -> p.getTau()).min().orElse(100);
		return tau;
	}

	public PokerSimulator getPokerSimulator() {
		return pokerSimulator;
	}

	public SensorsArray getSensorsArray() {
		return sensorsArray;
	}

	/**
	 * link between {@link HeroBot} an this instance. this method perfom all the decitions and return the action that he
	 * want to execute in simulation eviorement.
	 * 
	 * @param tropperParameter - instace of {@link TrooperParameter} whit all simulation parameters
	 * 
	 * @return the action to perform
	 */
	public TrooperAction getSimulationAction(TrooperParameter tropperParameter) {
		this.trooperParameter = tropperParameter;
		playTime = System.currentTimeMillis() - startDate;
		clearEnvironment();

		// for simulation purpose, allays read the assesment
		for (int i = 0; i < Table.CAPACITY; i++)
			readPlayerStat();

		decide();
		return act();
	}

	public Table getSimulationTable() {
		return simulationTable;
	}

	public boolean isPaused() {
		return paused;
	}

	public void pause(boolean pause) {
		this.paused = pause;
		setVariableAndLog(STATUS, paused ? "Trooper paused" : "Trooper resumed");
	}

	public void setSimulationTable(Table simulationTable) {
		this.simulationTable = simulationTable;
		pokerSimulator.setLive(false);
		this.numOfVillans = Table.CAPACITY - 1;
		this.gameRecorder = new GameRecorder(this, numOfVillans);

	}

	/**
	 * this method check the oportunity parameter and act accordinly. when Hero is in range of the parameter
	 * <code>phi</code> and the hero cards are in range of the preflop card distribution named <b>oportunity</b> this
	 * method will return <code>true</code> and override the main variable {@link #availableActions} and set only raise
	 * all actions for hero to take the oportunity
	 * 
	 * @return <code>true</code> for oportunity, <code>false</code> oetherwise
	 */
	private boolean checkOpportunities() {
		String txt = null;
		String op = trooperParameter.getString("takeOpportunity");

		if (reactToOportunity == pokerSimulator.street) {
			txt = prevReactionMessage + " Reacting!!!";
		} else {

			// pre flop
			if (pokerSimulator.street == PokerSimulator.HOLE_CARDS_DEALT) {
				int phi = trooperParameter.getInteger("phi");
				preflopCardsModel.setPercentage(phi);
				if (preflopCardsModel.containsHand(pokerSimulator.holeCards)) {
					txt = "Current Hole cards in oportunity range.";
					reactToOportunity = pokerSimulator.street;
					if (phi > 0 && ("takeNoO".equals(op) || "takePosFlop".equals(op))) {
						prevReactionMessage = txt;
						txt = null;
					}
				}
			}
			// flop turn and river
			if (pokerSimulator.street > PokerSimulator.HOLE_CARDS_DEALT) {

				// river
				int phi4 = trooperParameter.getInteger("phi4");
				if (phi4 > 0 && pokerSimulator.street == PokerSimulator.RIVER_CARD_DEALT) {
					double rankBehind = ((double) pokerSimulator.uoAEvaluation.get("rankBehind%"));
					if (rankBehind <= phi4) {
						txt = "rankBehind <= " + phi4 + " %";
						reactToOportunity = pokerSimulator.street;
						if ("takeNoO".equals(op) || "takePreFlop".equals(op)) {
							prevReactionMessage = txt;
							txt = null;
						}
					}
				}
				// allways
				if ((boolean) pokerSimulator.uoAEvaluation.get("isTheNut") == true) {
					txt = "Is the Nuts.";
					reactToOportunity = pokerSimulator.street;
					if ("takeNoO".equals(op) || "takePreFlop".equals(op)) {
						prevReactionMessage = txt;
						txt = null;
					}
				}
			}
		}

		if (reactToOportunity > PokerSimulator.NO_CARDS_DEALT && txt == null) {
			setVariableAndLog(EXPLANATION, "--- REACTION PREPARED " + prevReactionMessage + " ---");
		}

		if (txt != null) {
			setVariableAndLog(EXPLANATION, "--- OPORTUNITY DETECTED " + txt + " ---");
			// subObtimalDist = "UniformReal";
			subObtimalDist = "Oportunity";
			loadActions(pokerSimulator.heroChips);

			// to this point, if availableactions are empty, means hero is responding a
			// extreme hihgt raise. that mean meybe hero is weak. at this point reise mean
			// all in. (call actions is not considerer because is not oportuniti)
			if (availableActions.size() == 0 && pokerSimulator.raiseValue >= 0)
				availableActions.add(new TrooperAction("raise", pokerSimulator.raiseValue));
		}

		return txt != null;
	}

	/**
	 * clear the Environment for a new round.
	 * 
	 */
	private void clearEnvironment() {
		// in live Environment
		if (simulationTable == null) {
			pokerSimulator.clearEnvironment();
			sensorsArray.clearEnvironment();
			// read troper variables again (her because i can on the fly update
			Alesia.getInstance().openDB("hero");
			trooperParameter = TrooperParameter.findFirst("trooper = ?", "Hero");
		}
		reactToOportunity = PokerSimulator.NO_CARDS_DEALT;
		prevReactionMessage = null;
		maxRekonAmmo = -1;
		currentHandCost = 0;
		// subObtimalDist is clear with the loadactions method

		// at first time execution, a standar time of 10 second is used
		long tt = time1 == 0 ? 10000 : System.currentTimeMillis() - time1;
		outGameStats.addValue(tt);
		time1 = System.currentTimeMillis();
		Hero.heroLogger.fine("Game play time average=" + TStringUtils.formatSpeed((long) outGameStats.getMean()));
	}

	/**
	 * decide de action(s) to perform. This method is called when the {@link Trooper} detect that is my turn to play. At
	 * this point, the game Environment is waiting for an accion.
	 * 
	 */
	private void decide() {

		// chek the status of the simulator in case of error. if an error is detected,
		// fold
		if (pokerSimulator.getVariables().get(PokerSimulator.STATUS).equals(PokerSimulator.STATUS_ERROR)) {
			availableActions.add(new TrooperAction("fold", 0d));
			setVariableAndLog(EXPLANATION, "Error detected in simulator.");
			return;
		}

		// PREFLOP
		if (pokerSimulator.street == PokerSimulator.HOLE_CARDS_DEALT) {
			if (!checkOpportunities())
				setPreflopActions();
		}

		// FLOP AND FUTHER
		if (pokerSimulator.street > PokerSimulator.HOLE_CARDS_DEALT) {
			if (!checkOpportunities()) {
				loadActions(pokerSimulator.heroChips);
				potOdd();
			}
		}

		// if the list of available actions are empty, i habe no option but fold/check
		// in concordance whit rule1: if i can keep checking until i get a luck card. i
		// will do. this behabior also
		// allow for example getpreflop action continue because some times, the
		// Environment is too fast and the trooper
		// is unable to retribe all information
		if (availableActions.size() == 0 && pokerSimulator.isSensorEnabled("call") && pokerSimulator.callValue <= 0) {
			setVariableAndLog(STATUS, "Empty list. Checking");
			availableActions.add(TrooperAction.CHECK);
		}

		// if the list of available actions are empty, the only posible action todo now
		// is fold
		if (availableActions.size() == 0) {
			setVariableAndLog(STATUS, "Empty list. Folding");
			availableActions.add(TrooperAction.FOLD);
		}
	}

	/**
	 * this method retrive the ammount of chips of all currentliy active villans. the 0 position is the amount of chips
	 * computed and the index 1 is the number of active villans
	 * 
	 * TODO: maybe ammocontrol shoud control loadaction(ammo) instead loadaction(herrochips) make a comment !!!
	 * 
	 * NOTE: Die Liste von m�glichen Aktions ist nach Wert der Aktion geordnet. das Verh�ltnis ist 1-1 doch ist es
	 * wichtig die Werte gucken. das gibt mir ein Idee von zuk�nftige Aggression.
	 */

	private TrooperAction getSubOptimalAction() {

		// double b = ammo + alpha * ammo; // agresive: b > ammo
		// b = b == 0 ? 1 : b; // avoid error when alpha is extreme low
		// double c = alpha < 0 ? b : ammo; // K sugestions allways as upperbound
		// TriangularDistribution td = new TriangularDistribution(0, c, b);
		// int amount = (int) td.sample();

		Vector<TEntry<TrooperAction, Double>> actProb = new Vector<>();
		availableActions.forEach((ta) -> actProb.add(new TEntry<TrooperAction, Double>(ta, ta.amount)));
		Collections.sort(actProb, Collections.reverseOrder());

		int elements = availableActions.size();
		double winProb = (double) pokerSimulator.uoAEvaluation.getOrDefault("winProb", 0.0);
		double hs = winProb * elements;
		// double mode = (hs > 1) ? elements : hs * elements;
		double mode = (hs > 1) ? 0 : hs * elements;
		AbstractRealDistribution tdist = new TriangularDistribution(0, mode, elements);
		if (subObtimalDist.equals("UniformReal"))
			tdist = new UniformRealDistribution(0, elements);
		if (subObtimalDist.equals("Oportunity"))
			tdist = new TriangularDistribution(0, 0, elements);
		int[] singletos = new int[elements];
		double[] probabilities = new double[elements];
		for (int i = 0; i < elements; i++) {
			singletos[i] = i;
			TEntry<TrooperAction, Double> te = actProb.elementAt(i);
			probabilities[i] = tdist.probability(i, i + 1);
			te.setValue(probabilities[i]);
		}

		// action selecction range acording tau parameter
		// int actran = Integer.parseInt(parameters.get("tau").toString());
		// int ele = 99;
		// while ((ele < mode - actran) || (ele > mode + actran)) {
		// ele = (int) tdist.sample();
		// }

		int ele = (int) tdist.sample();
		TrooperAction selact = actProb.elementAt(ele).getKey();
		pokerSimulator.setActionsData(selact, actProb);
		return selact;
	}

	private boolean isMyTurnToPlay() {
		return sensorsArray.isSensorEnabled("fold") || sensorsArray.isSensorEnabled("call")
				|| sensorsArray.isSensorEnabled("raise");
	}

	/**
	 * 
	 * this method fill the global variable {@link #availableActions} whit all available actions according to the
	 * parameter <code>maximum</code>. the expected actions are
	 * <li>Check/Call
	 * <li>Raise
	 * <li>Pot
	 * <li>All-in
	 * <li>5 more actions that range from raise to the value close to All-in.
	 * <p>
	 * for a total of 9 posible actions
	 * 
	 * @param maximum - the upper bound to consider
	 */
	private void loadActions(double maximum) {
		availableActions.clear();
		subObtimalDist = "Triangular";

		double call = pokerSimulator.callValue;
		double raise = pokerSimulator.raiseValue;
		double chips = pokerSimulator.heroChips;
		double pot = pokerSimulator.potValue;

		// fail safe: the maximun can.t be greater as chips.
		double imax = maximum > chips ? chips : maximum;

		if (call >= 0 && call <= imax)
			availableActions.add(new TrooperAction("call", call));

		if (raise >= 0 && raise <= imax)
			availableActions.add(new TrooperAction("raise", raise));

		if (pot >= 0 && pot <= imax && pokerSimulator.isSensorEnabled("raise.pot"))
			availableActions.add(new TrooperAction("pot", "raise.pot;raise", pot));

		if (chips >= 0 && chips <= imax && pokerSimulator.isSensorEnabled("raise.allin"))
			availableActions.add(new TrooperAction("allIn", "raise.allin;raise", chips));

		double sb = pokerSimulator.smallBlind;
		double bb = pokerSimulator.bigBlind;
		if (raise > 0 && pokerSimulator.isSensorEnabled("raise.slider")) {
			// check for int or double values for blinds
			boolean isInt = (Double.valueOf(bb)).intValue() == bb && (Double.valueOf(bb)).intValue() == sb;
			double tick = raise;
			int step = 5;
			double ammoinc = imax / (step * 1.0);
			// TODO:
			// when tha call to this method, the parameter maximum = chips is valid val <
			// meximus because tha all in
			// acction will take kare of maximum chip. but when maximum is another value,
			// the comparation mus be <=
			// TEMPORAL: try to incorporate the las element
			double max2 = (imax == chips) ? imax : imax + 0.01;

			for (int c = 0; (c < step && (tick + ammoinc) < max2); c++) {
				tick += ammoinc;
				// round value to look natural (dont write 12345. write 12340 or 12350)
				if (isInt)
					tick = ((int) (tick / 10)) * 10;
				String txt = isInt ? "" + (int) tick : twoDigitFormat.format(tick);
				availableActions.add(new TrooperAction("raise", "raise.text,dc;raise.text,k=" + txt + ";raise", tick));
			}
		}
	}

	private void logInfo(String message) {
		if (simulationTable != null)
			return;
		Hero.heroLogger.info(message);
	}

	/**
	 * Compute the EV for all actions inside of the global variable {@link #availableActions}. after this method, the
	 * list contain only the actions with +EV. *
	 * <p>
	 * to comply with rule 1, this method retrive his probability from {@link PokerSimulator#getProbability()
	 * 
	 * <h5>MoP page 54</h5>
	 * 
	 */
	private void potOdd() {
		double Ppot = (double) pokerSimulator.uoAEvaluation.get("PPot");
		// double NPot = (double) pokerSimulator.uoAEvaluation.get("NPot");
		// double RPot = (double) pokerSimulator.uoAEvaluation.get("RPot");
		// double HS_n = (double) pokerSimulator.uoAEvaluation.get("HS_n");
		double winProb = (double) pokerSimulator.uoAEvaluation.get("winProb");

		// double alpha = trooperParameter.getInteger("alpha") / 100.0;
		// double uppB = 3.0;
		// double zeta = trooperParameter.getInteger("zeta") / 100.0;

		// System.out.println(String.format("%1.3f + %1.3f + %1.3f = %1.3f", Ppot, NPot, RPot, Ppot + NPot + RPot));

		double rPot = winProb * pokerSimulator.potValue;// * alpha;
		double invPot = Ppot * (pokerSimulator.potValue - rPot);// * zeta;
		// double future = Ppot * pokerSimulator.buyIn * (uppB - alpha);
		double ammo = rPot + invPot;

		// no calculation for 0 values
		if (ammo == 0 || winProb == 0) {
			availableActions.clear();
			logInfo(String.format("No posible decision for values prob = %1.3f or amunitions = %7.2f", winProb, ammo));
			return;
		}

		for (TrooperAction act : availableActions) {
			double ev = (winProb + Ppot) * ammo - act.amount;
			act.expectedValue = ev;
		}
		// remove all negative values
		double beta = trooperParameter.getDouble("beta");
		availableActions.removeIf(ta -> ta.expectedValue < beta);
		// 191228: Hero win his first game against TH app !!!!!!!!!!!!!!!! :D

		String txt1 = String.format("rPot %1.3f * %7.2f = %7.2f invPot %1.3f * %7.2f = %7.2f ammo = %7.2f", winProb,
				pokerSimulator.potValue, rPot, Ppot, (pokerSimulator.potValue - rPot), invPot, ammo);
		setVariableAndLog(EXPLANATION, txt1);

	}

	/**
	 * read one unit of information. This method is intented to retrive information from the Environment in small amount
	 * to avoid exces of time comsumption.
	 * 
	 */
	private void readPlayerStat() {
		gameRecorder.takeSample();
		String asse = "<html><table border=\"0\", cellspacing=\"0\"><assesment></table></html>";
		String tmp = "<tr><td>Name</td><td>Chips</td><td>Tau</td><td>Mean</td><td>SD</td></tr>";

		List<GamePlayer> list = gameRecorder.getPlayers();
		if (list.size() > 0) {
			for (GamePlayer gp : list) {
				String rowsty = gp.isActive() ? "" : "style=\"color:#808080\"";
				tmp += "<tr " + rowsty + "><td>" + gp.getId() + " " + gp.getName() + "</td><td>" + gp.getChips()
						+ "</td><td>" + gp.getTau() + "</td><td>" + gp.getMean() + "</td><td>"
						+ gp.getStandardDeviation() + "</td></tr>";
			}
			asse = asse.replace("<assesment>", tmp);
		} else
			asse = "Unknow";
		pokerSimulator.setVariable("trooper.Assesment", asse);

	}

	/**
	 * Set the action based on the starting hand distribution. This method set the global variable {@link #maxRekonAmmo}
	 * this method allwais select the less cost action. The general idea here is try to put the trooper in folp, so the
	 * normal odds operation has chance to decide, at lower posible cost
	 */
	private void setPreflopActions() {
		availableActions.clear();
		// 220902 reconnBand = 30
		double base = pokerSimulator.bigBlind * trooperParameter.getDouble("reconnBase");
		double band = pokerSimulator.bigBlind * trooperParameter.getDouble("reconnBand");

		// 220302: CURRENT SIMULATION TAU PARAMETER VARIATION (STRICK PREPLOP, NO
		// OPORTUNITY)
		// 211205: the first real simulation, analisis and result: 30%
		int tau = trooperParameter.getInteger("tau");
		preflopCardsModel.setPercentage(tau);
		String txt = "No strict Preflop.";
		
		boolean strictPreflop = trooperParameter.getBoolean("strictPreflop");
		if (strictPreflop) {
			txt = "Preflop in Range";
			if (!preflopCardsModel.containsHand(pokerSimulator.holeCards)) {
				setVariableAndLog(EXPLANATION, "Preflop not in range.");
				return;
			}
		}

		double ev = preflopCardsModel.getEV(pokerSimulator.holeCards);
		if (maxRekonAmmo == -1) {
			maxRekonAmmo = base + (band * ev);
		}

		double call = pokerSimulator.callValue;
		double raise = pokerSimulator.raiseValue;
		// can i check ??
		if (call == 0) {
			availableActions.add(TrooperAction.CHECK);
			txt += " Checking.";
		} else {
			// can i call ?
			if (call > 0 && (call + currentHandCost) < maxRekonAmmo) {
				availableActions.add(new TrooperAction("call", call));
				txt += " Calling.";
			} else {
				// the raise is mariginal ??
				if (raise != -1 && (raise + currentHandCost) < maxRekonAmmo) {
					availableActions.add(new TrooperAction("raise", raise));
					txt += " Raising.";
				}
			}
		}
		if (availableActions.size() == 0) {
			txt = String.format(txt + " No more ammunition available. (%7.2f)", maxRekonAmmo);
			setVariableAndLog(EXPLANATION, txt);
			return;
		}

		String txt1 = String.format(txt + " %7.2f = %7.2f + %7.2f * %1.3f", maxRekonAmmo, base, band, ev);
		setVariableAndLog(EXPLANATION, txt1);
	}

	private void setVariableAndLog(String key, String value) {
		// append the playtime to the status (visual purpose only)
		if (STATUS.equals(key)) {
			value = "Hand " + handsCounter + " Play time "
					+ timeFormat.format(new Date(playTime - TimeUnit.HOURS.toMillis(1))) + " loss limit "
					+ twoDigitFormat.format(playUntil) + " " + value.toString();
		}
		pokerSimulator.setVariable(key, value);
		// don.t log the status, only the explanation
		if (!STATUS.equals(key)) {
			String key1 = key.replace(EXPLANATION, "");
			// 200210: Hero play his first 2 hours with REAL +EV. Convert 10000 chips in
			// 64000
			logInfo(key1 + value);
		}
	}

	/**
	 * This metod check all the sensor areas and perform the corrections to get the troper into the fight. The
	 * conbination of enabled/disabled status of the sensor determine the action to perform. If the Environment request
	 * the trooper to play, this method return <code>true</code>,
	 * <p>
	 * This method return <code>false</code> when:
	 * <ol>
	 * <li>try to reach the gametable until an fix amount of time is reached. In that case, this method return
	 * <code>false</code>.
	 * <li>the buy-in window is displayed, in this case, cancel option is selected
	 * 
	 * @return <code>true</code> if the Environment is waiting for the troper to {@link #decide()} and {@link #act()}.
	 */
	private boolean watchEnvironment() throws Exception {
		setVariableAndLog(STATUS, "Looking the table ...");
		// try during x seg. Some round on PS long like foreeeeveeerr
		long tottime = 300 * 1000;
		long t1 = System.currentTimeMillis();
		while (System.currentTimeMillis() - t1 < tottime) {
			// pause ?
			if (paused) {
				Thread.sleep(100);
				// update t1 var while is out. this avoid troper dismist because large pause is
				// interpreted as a faule
				// in Environment and trooper can.t reach the main table
				t1 = System.currentTimeMillis();
				continue;
			}
			// canceled ?
			if (isCancelled())
				return false;
			// readPlayerStat();
			sensorsArray.read(SensorsArray.TYPE_ACTIONS);

			// NEW ROUND: if the hero current hand is diferent to the last measure, clear
			// the Environment.
			String hc1 = sensorsArray.getSensor("hero.card1").getOCR();
			String hc2 = sensorsArray.getSensor("hero.card2").getOCR();
			String hch = hc1 == null ? "" : hc1;
			hch += hc2 == null ? "" : hc2;
			if (!("".equals(hch) || lastHoleCards.equals(hch))) {
				lastHoleCards = hch;
				setVariableAndLog(EXPLANATION,
						"--- Hand " + (++handsCounter) + " " + pokerSimulator.getTableParameters() + " ---");

				// play time or play until parameter parameters. when the play time is reach, the
				// action sit.out is
				// clicked and hero return

				// play time
				double ptd = trooperParameter.getDouble("playTime");
				long playtimeParm = (long) (ptd * 3600 * 1000);
				playTime = System.currentTimeMillis() - startDate;

				// play until parameter
				double playUntilParm = trooperParameter.getDouble("playUntil");
				// read hero chips. this avoid false tropper dismist after all in or bluff (hero
				// chips was very low at
				// that point)
				sensorsArray.readSensors(true, sensorsArray.getSensors("hero.chips"));
				double chips = sensorsArray.getSensor("hero.chips").getNumericOCR();
				// if chips are not available, show the last computed play safe value
				if (chips > 0)
					playUntil = pokerSimulator.heroChipsMax - (playUntilParm * pokerSimulator.buyIn);

				if ((playtimeParm > 0 && playTime > playtimeParm)
						|| (chips > 0 && playUntilParm > 0 && chips <= playUntil)
								&& sensorsArray.isSensorEnabled("sit.out")) {
					robotActuator.perform("sit.out");
					robotActuator.perform(TrooperAction.FOLD);
					setVariableAndLog(EXPLANATION, "Play time or loss fail safe reach. mission accomplisch.");
					return false;
				}

				clearEnvironment();
				setVariableAndLog(STATUS, "Looking the table ...");
				continue;
			}

			// Environment is in the gametable
			if (isMyTurnToPlay()) {
				// repeat the look of the sensors. this is because some times the capture is during a animation
				// transition. to avoid error reading sensors, perform the lecture once more time. after the second
				// lecutre, this return return normaly sensorsArray.read(SensorsArray.TYPE_ACTIONS);
				Thread.sleep(100);
				sensorsArray.read(SensorsArray.TYPE_ACTIONS);

				// if this sensor is active and wth the corresponding text, hero is in a all in situation. select
				// "continue hand" allways (no cash out option) to maximize winnings
				if (sensorsArray.isSensorEnabled("allin.name")) {
					// sensorsArray.readSensors(true, sensorsArray.getSensors("allin.name"));
					if (sensorsArray.getSensor("allin.name").getOCR().equals("fortgesetzt")) {
						robotActuator.perform("allin.name");
						setVariableAndLog(EXPLANATION, "Responding with Cotinue Hand in all-in.");
						continue;
					}
				}

				sensorsArray.saveSample(handsCounter, pokerSimulator.street);
				return true;
			}

			// if buy-in window is displayed, hero lost the battle
			if (sensorsArray.isSensorEnabled("buyIn.cancel") && sensorsArray.isSensorEnabled("buyIn.ok")
					&& sensorsArray.isSensorEnabled("buyIn.point3")) {
				robotActuator.perform("buyIn.cancel");
				setVariableAndLog(EXPLANATION, "Trooper lost the battle !!!!");
				return false;
			}

			// the i.m back button is active (at this point, the Environment must only being
			// showing the i.m back
			// button)
			// if (sensorsArray.isSensorEnabled("imBack")) {
			// robotActuator.perform("imBack");
			// continue;
			// }

		}
		setVariableAndLog(EXPLANATION, "Can.t reach the main gametable.");
		return false;
	}

	/**
	 * perform the action. At this point, the game table is waiting for the hero action.
	 * 
	 */
	protected TrooperAction act() {
		setVariableAndLog(STATUS, "Acting ...");
		TrooperAction act = getSubOptimalAction();
		// normaly the cost is know. but sometimes(like in oportunities) not
		currentHandCost += act.amount;
		String key = "trooper.Action performed";
		setVariableAndLog(key, " " + act + ". Current cost " + twoDigitFormat.format(currentHandCost));
		// robot actuator perform the log
		if (simulationTable == null)
			robotActuator.perform(act);
		return act;
	}

	@Override
	protected Object doInBackground() throws Exception {
		clearEnvironment();
		startDate = System.currentTimeMillis();
		while (!isCancelled()) {
			if (paused) {
				Thread.sleep(100);
				continue;
			}
			// countdown before start
			if (countdown > 0) {
				countdown--;
				setVariableAndLog(STATUS, "start in " + countdown);
				Thread.sleep(1000);
				continue;
			}

			boolean ingt = watchEnvironment();

			// if watchEnvironment() methdo return false, dismiss the troper.
			if (!ingt) {
				setVariableAndLog(EXPLANATION, "Tropper dismiss.");
				return null;
			}
			long t1 = System.currentTimeMillis();

			// TODO: used for reweight. not implemented
			// pokerSimulator.stimatedVillanTau = getMinActiveTau();
			pokerSimulator.stimatedVillanTau = 50;

			// at this point i must decide and act
			setVariableAndLog(STATUS, "Reading NUMBERS ...");
			// MANDATORY ORDEN FIRST NUMBER AND THEN CARDS
			sensorsArray.read(SensorsArray.TYPE_NUMBERS);
			setVariableAndLog(STATUS, "Reading CARDS ...");
			sensorsArray.read(SensorsArray.TYPE_CARDS);
			availableActions.clear();
			setVariableAndLog(STATUS, "Deciding ...");

			decide();
			act();

			trooperPerformance.addValue(System.currentTimeMillis() - t1);
			pokerSimulator.setVariable("sensorArray.Performance",
					"Tesseract " + twoDigitFormat.format(sensorsArray.tesseractTime.getMean() / 1000D) + "s. Trooper "
							+ twoDigitFormat.format(trooperPerformance.getMean() / 1000D) + "s.");

		}
		return null;
	}

	/**
	 * This method is invoked during the idle phase (after {@link #act()} and before {@link #decide()}. use this method
	 * to perform large computations.
	 */
	protected void think() {
		// setVariableAndLog(STATUS, "Reading villans ...");
		// sensorsArray.readVillan();
		// 191020: ayer ya la implementacion por omision jugo una partida completa y
		// estuvo a punto de vencer a la
		// chatarra de Texas poker - poker holdem. A punto de vencer porque jugaba tan
		// lento que me aburri del sueno :D
	}
}
