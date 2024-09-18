package hero;

import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.descriptive.*;
import org.jdesktop.application.*;

import core.*;
import datasource.*;
import hero.ozsoft.*;
import hero.ozsoft.bots.*;

/**
 * this class represent the core of hero. As a core class, this class depends of
 * another classes in order to build a useful player agent. the following is a
 * list of class from where the information is retrieved, and the actions are
 * performed.
 * <ul>
 * <li>{@link PokerSimulator} - get the numerical values for decision making
 * <li>{@link RobotActuator} - perform the action sended by this class.
 * <li>{@link SensorsArray} - perform visual operation of the Environment
 * </ul>
 * <p>
 * <b>RULE 1: hero is here to win money. So in order to do that, hero need to
 * fight and stay in the table. </b>
 * <p>
 * to be agree with this rule, this implementation:
 * <ul>
 * <li>Invest his chips only in calculated 0 or positive EV. When the EV for pot
 * odd return an empty list, for example, pot=0 (initial bet and hero is the
 * dealer) the EV function will return negative expectation even with AAs. in
 * this case, the {@link #setPreflopActions()} is called as a last resource.
 * <li>Table Position: In this implementation, the table position is irrelevant
 * because the normal odd action take the values of the the pot odd actions are
 * imp the table position are implied in the normal odd actions. this mean, the
 * combination of odd actions and preflop hand evaluation has the hero table
 * position already implied.
 * <li>Number of villains: The number of villains is also irrelevant in this
 * implementation because that information is already present in
 * {@link PokerProphesierAdapter}.
 * </ul>
 * 
 * @author terry
 *
 */
public class Trooper extends Task<Void, Map<String, Object>> {

	/**
	 * encapsulate the pot odd decision make by
	 * {@link Trooper#potOdd(double, List, Map)}
	 */
	public static class PotOdd {
		public double potValue;
		public List<TrooperAction> availableActions = new ArrayList<>();
		public String explanation;
	}

	/** fired when trooper need to notify an step */
	public static final String STEP = "STEP";
	public static final String EXPLANATION = "aa.Explanation";
	public static final String STATUS = "aa.Trooper Status";
	public static final String ACTION_PERFORMED = "trooper.Action performed";
	public static final String ACTIONS = "trooper.Actions";
	private static DecimalFormat twoDigitFormat = TResources.twoDigitFormat;
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

	/**
	 * Compute the pot odds based on the formula from <strong>MoP page 54</strong>.
	 * the returned instance {@link PotOdd} will contain a list with only the
	 * actions with +EV.
	 * 
	 * @param potValue         - the current pot
	 * @param availableActions - preselected actions
	 * @param uoAEvaluation    - the current situation evaluation
	 * @return the decision make by this method
	 */
	public static PotOdd potOdd(double potValue, List<TrooperAction> availableActions,
			Map<String, Object> uoAEvaluation) {
		PotOdd potOdd = new PotOdd();
		potOdd.potValue = potValue;
		potOdd.availableActions = new ArrayList<>(availableActions);
		double Ppot = (double) uoAEvaluation.get("PPot");
		double winProb = (double) uoAEvaluation.get("winProb");

		double rPot = winProb * potValue;
		double invPot = Ppot * (potValue - rPot);
		double ammunitions = rPot + invPot;
		potOdd.explanation = String.format("ammo = (%1.3f * %7.2f) + (%1.3f * %7.2f) = %7.2f", winProb, potOdd.potValue,
				Ppot, (potOdd.potValue - rPot), ammunitions);

		// no calculation for 0 values
		if (ammunitions <= 0 || winProb <= 0) {
			potOdd.availableActions.clear();
			potOdd.explanation = String.format("No posible decision for values prob = %1.3f or amunitions = %7.2f",
					winProb, ammunitions);
			return potOdd;
		}

		for (TrooperAction act : potOdd.availableActions) {
			double ev = winProb * ammunitions - act.amount;
			act.potOdds = ev;
		}
		// remove all negative values
		potOdd.availableActions.removeIf(ta -> ta.potOdds < 0);
		// 191228: Hero win his first game against TH app !!!!!!!!!!!!!!!! :D
		return potOdd;
	}

	private PokerSimulator pokerSimulator;
	private SensorsArray sensorsArray;
	private RobotActuator robotActuator;
	private List<TrooperAction> availableActions;
	private int countdown = 5;

	private int handsCounter = 0;
	private long time1;
	private DescriptiveStatistics outGameStats;
	private DescriptiveStatistics performaceStatistic;
	private boolean paused = false;
	// This variable is ONLY used and cleaned by ensuregametable method
	private String lastHoleCards = "";
	private double currentHandCost;;
	private double playUntil;
	private long playTime;
	private String gameAction;
	private TrooperParameter trooperParameter;
	private int numOfvillains;
	private GameRecorder gameRecorder;
	private PreflopCardsModel preflopCardsModel = new PreflopCardsModel("pokerStar");
	private Table simulationTable;
	private long startDate;

	public Trooper(TrooperParameter parameter) {
		super(Alesia.getInstance());
		this.availableActions = new ArrayList<>();
		this.pokerSimulator = new PokerSimulator(parameter);
		this.sensorsArray = new SensorsArray(pokerSimulator);
		this.robotActuator = new RobotActuator(this);
		this.numOfvillains = sensorsArray.getVillains();
		this.gameRecorder = new GameRecorder(this, numOfvillains);
		this.outGameStats = new DescriptiveStatistics(10);
		this.performaceStatistic = new DescriptiveStatistics(10);
		this.handsCounter = 0;
		this.playUntil = 0;
		this.trooperParameter = parameter;
		this.gameAction = SubOptimalAction.ACTION_NORMAL;
	}

	/**
	 * perform the action. At this point, the game table is waiting for the hero
	 * action.
	 * 
	 */
	protected TrooperAction act() {
		int alpha = trooperParameter.getInteger("alpha");
		SubOptimalAction subOptimalAction = Trooper.getAction(availableActions, gameAction, alpha);
		pokerSimulator.setVariable(ACTION_PERFORMED, subOptimalAction.action);
		pokerSimulator.setVariable(ACTIONS, subOptimalAction.sampledActions);

		// Normally the cost is know. but sometimes(like in opportunities) not
		currentHandCost += subOptimalAction.action.amount;
		setVariableAndLog("trooper.Acumulated cost", twoDigitFormat.format(currentHandCost));
		// robot actuator perform the log
		if (simulationTable == null)
			robotActuator.perform(subOptimalAction.action);
		return subOptimalAction.action;
	}

	public void cancelTrooper(boolean interrupt) {
		setVariableAndLog(STATUS, "Trooper Canceled.");
		super.cancel(interrupt);
	}

	/**
	 * clear the Environment for a new round.
	 * 
	 */
	private void clearEnvironment() {
		// in live Environment
		if (simulationTable == null) {
			pokerSimulator.newHand();
			sensorsArray.clearEnvironment();
			// read trooper variables again (her because i can on the fly update
			Alesia.openDB();
			trooperParameter = TrooperParameter.findFirst("trooper = ?", "Hero");
		}
		currentHandCost = 0;
		// subObtimalDist is clear with the loadactions method

		// at first time execution, a standard time of 10 second is used
		long tt = time1 == 0 ? 10000 : System.currentTimeMillis() - time1;
		outGameStats.addValue(tt);
		time1 = System.currentTimeMillis();
		Hero.heroLogger.fine("Game play time average=" + TStringUtils.formatSpeed((long) outGameStats.getMean()));
	}

	/**
	 * decide de action(s) to perform. This method is called when the
	 * {@link Trooper} detect that is my turn to play. At this point, the game
	 * Environment is waiting for an accion.
	 * 
	 */
	private void decide() {

		// Check the status of the simulator in case of error. if an error is detected,
		// fold
		if (pokerSimulator.getVariables().get(PokerSimulator.STATUS).equals(PokerSimulator.STATUS_ERROR)) {
			availableActions.add(new TrooperAction("fold", 0d));
			setVariableAndLog(EXPLANATION, "Error detected in simulator.");
			return;
		}

		// here come the action retribed from ruleBook
		// pokerSimulator.ruleBook.action;

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

		// if the list of available actions are empty, the only possible action todo now
		// is fold
		if (availableActions.size() == 0) {
			setVariableAndLog(STATUS, "Empty list. Folding");
			availableActions.add(TrooperAction.FOLD);
		}
	}

	@Override
	protected Void doInBackground() throws Exception {
		clearEnvironment();
		startDate = System.currentTimeMillis();
		while (!isCancelled()) {
			if (paused) {
				Thread.sleep(100);
				continue;
			}
			// count down before start
			if (countdown > 0) {
				countdown--;
				setVariableAndLog(STATUS, "start in " + countdown);
				Thread.sleep(1000);
				continue;
			}

			boolean ingt = watchEnvironment();

			// if watchEnvironment() method return false, dismiss the trooper.
			if (!ingt) {
				setVariableAndLog(EXPLANATION, "Tropper dismiss.");
				return null;
			}
			long t1 = System.currentTimeMillis();

			// TODO: used for reweight. not implemented
			// pokerSimulator.stimatedVillanTau = getMinActiveTau();
			pokerSimulator.stimatedVillanTau = 50;

			// at this point i must decide and act.
			// MANDATORY ORDEN FIRST NUMBER AND THEN CARDS
			setVariableAndLog(STATUS, "Reading NUMBERS ...");
			sensorsArray.read(SensorsArray.TYPE_NUMBERS);
			setVariableAndLog(STATUS, "Reading CARDS ...");
			sensorsArray.read(SensorsArray.TYPE_CARDS);
			setVariableAndLog(STATUS, "Deciding ...");
			availableActions.clear();
			decide();
			setVariableAndLog(STATUS, "Acting ...");
			act();

			performaceStatistic.addValue(System.currentTimeMillis() - t1);
			String troperS = twoDigitFormat.format(performaceStatistic.getMean() / 1000D);
			String sensorS = twoDigitFormat.format(sensorsArray.performaceStatistic.getMean() / 1000D);
			String simulatorS = twoDigitFormat.format(pokerSimulator.performaceStatistic.getMean() / 1000D);
			pokerSimulator.setVariable("aa.Performance",
					"Tesseract " + sensorS + "s. Trooper " + troperS + "s. Simulator " + simulatorS + "s.");

		}
		return null;
	}

	/**
	 * return the minimum Tau value of all active villains
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
	 * link between {@link HeroBot} an this instance. this method perform all the
	 * decisions and return the action that he want to execute in simulation
	 * environment.
	 * 
	 * @param tropperParameter - instance of {@link TrooperParameter} whit all
	 *                         simulation parameters
	 * 
	 * @return the action to perform
	 */
	public TrooperAction getSimulationAction() {
		playTime = System.currentTimeMillis() - startDate;
		clearEnvironment();

		// for simulation purpose, allays read the assessment
		// for (int i = 0; i < Table.CAPACITY; i++)
		// readPlayerStat();

		decide();
		TrooperAction action = act();
		return action;
	}

	public Table getSimulationTable() {
		return simulationTable;
	}

	public static class SubOptimalAction {
		public static String ACTION_NORMAL = "ACTION_NORMAL";
		public static String ACTION_OPORTUNITY = "ACTION_OPORTUNITY";
		public TrooperAction action;
		public List<TEntry<TrooperAction, Double>> sampledActions;
	}

	public static SubOptimalAction getAction(List<TrooperAction> availableActions, String gameAction, int alpha) {
		List<TEntry<TrooperAction, Double>> sampledActions = new ArrayList<>();
		for (TrooperAction ta : availableActions) {
			sampledActions.add(new TEntry<TrooperAction, Double>(ta, ta.amount));
		}
		Collections.sort(sampledActions);

		int bandc = (int) Math.round(sampledActions.size() * alpha / 100d);
		bandc = bandc == 0 ? 1 : bandc; // avoid error when alpha is extreme low
		// in oportunity, ignore alpha and use the complete list of actions. in normal
		// play, use alpha
		if (SubOptimalAction.ACTION_OPORTUNITY.equals(gameAction))
			bandc = sampledActions.size();

		sampledActions = sampledActions.subList(0, bandc);

		// if (bandc > 2)
		// System.out.println("Trooper.getAction() " + bandc);

		AbstractRealDistribution distribution = new TriangularDistribution(0, bandc, bandc);
		for (int i = 0; i < bandc; i++) {
			TEntry<TrooperAction, Double> te = sampledActions.get(i);
			double prob = distribution.probability(i, i + 1);
			te.setValue(prob);
		}

		int ele = (int) distribution.sample();
		SubOptimalAction subOptimalAction = new SubOptimalAction();
		subOptimalAction.action = sampledActions.get(ele).getKey();
		subOptimalAction.sampledActions = sampledActions;
		return subOptimalAction;
	}

	private boolean isMyTurnToPlay() {
		return sensorsArray.isSensorEnabled("fold") || sensorsArray.isSensorEnabled("call")
				|| sensorsArray.isSensorEnabled("raise");
	}

	public boolean isPaused() {
		return paused;
	}

	private void logInfo(String message) {
		if (simulationTable != null)
			return;
		Hero.heroLogger.info(message);
	}

	public void pause(boolean pause) {
		this.paused = pause;
		setVariableAndLog(STATUS, paused ? "Trooper paused" : "Trooper resumed");
	}


	/**
	 * read one unit of information. This method is intented to retrive information
	 * from the Environment in small amount to avoid exces of time comsumption.
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

	public void setSimulationTable(Table simulationTable) {
		this.simulationTable = simulationTable;
		pokerSimulator.setLive(false);
		this.numOfvillains = Table.MAX_CAPACITY - 1;
		this.gameRecorder = new GameRecorder(this, numOfvillains);

	}

	private void setVariableAndLog(String key, String value) {
		// append the play time to the status (visual purpose only)
		if (STATUS.equals(key)) {
			pokerSimulator.setVariable("aa.General",
					"Hands " + handsCounter + " Played time "
							+ timeFormat.format(new Date(playTime - TimeUnit.HOURS.toMillis(1))) + " Loss limit "
							+ twoDigitFormat.format(playUntil));
		}

		pokerSimulator.setVariable(key, value);

		// don.t log the status, only the explanation
		if (!STATUS.equals(key)) {
			String key1 = key.replace(EXPLANATION, "");
			// 200210: Hero play his first 2 hours with REAL +EV. Convert 10000 chips in
			// 64000
			logInfo(key1 + value);
		}

		firePropertyChange(Trooper.STEP, null, pokerSimulator.getVariables());
	}

	/**
	 * This method is invoked during the idle phase (after {@link #act()} and before
	 * {@link #decide()}. use this method to perform large computations.
	 */
	protected void think() {
		// setVariableAndLog(STATUS, "Reading villains ...");
		// sensorsArray.readVillan();
		// 191020: ayer ya la implementacion por omision jugo una partida completa y
		// estuvo a punto de vencer a la
		// chatarra de Texas poker - poker holdem. A punto de vencer porque jugaba tan
		// lento que me aburri del sueno :D
	}

	/**
	 * This method check all the sensor areas and perform the corrections to get the
	 * trooper into the fight. The Combination of enabled/disabled status of the
	 * sensor determine the action to perform. If the Environment request the
	 * trooper to play, this method return <code>true</code>,
	 * <p>
	 * This method return <code>false</code> when:
	 * <ol>
	 * <li>try to reach the game table until an fix amount of time is reached. In
	 * that case, this method return <code>false</code>.
	 * <li>the buy-in window is displayed, in this case, cancel option is selected
	 * 
	 * @return <code>true</code> if the Environment is waiting for the troper to
	 *         {@link #decide()} and {@link #act()}.
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
				// update t1 var while is out. this avoid trooper dismiss because large pause is
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

			// NEW ROUND: if the hero current hand is different to the last measure, clear
			// the Environment.
			String hc1 = sensorsArray.getSensor("hero.card1").getOCR();
			String hc2 = sensorsArray.getSensor("hero.card2").getOCR();
			String hch = hc1 == null ? "" : hc1;
			hch += hc2 == null ? "" : hc2;
			if (!("".equals(hch) || lastHoleCards.equals(hch))) {
				lastHoleCards = hch;
				setVariableAndLog(EXPLANATION,
						"--- Hand " + (++handsCounter) + " " + pokerSimulator.getTableParameters() + " ---");

				// play time or play until parameter parameters. when the play time is reach,
				// the
				// action sit.out is
				// clicked and hero return

				// play time
				double ptd = trooperParameter.getDouble("playTime");
				long playtimeParm = (long) (ptd * 3600 * 1000);
				playTime = System.currentTimeMillis() - startDate;

				// play until parameter
				double playUntilParm = trooperParameter.getDouble("playUntil");
				// read hero chips. this avoid false trooper dismiss after all in or bluff (hero
				// chips was very low at
				// that point)
				sensorsArray.readSensors(true, sensorsArray.getSensors("hero.chips"));
				double chips = sensorsArray.getSensor("hero.chips").getNumericOCR();
				// if chips are not available, show the last computed play safe value
				if (chips > 0) {
					double maxC = pokerSimulator.pokerSimulatorTraker.getHeroMaxChips();
					playUntil = maxC - (playUntilParm * pokerSimulator.buyIn);
				}

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

			// Environment is in the game table
			if (isMyTurnToPlay()) {
				// repeat the look of the sensors. this is because some times the capture is
				// during a animation
				// transition. to avoid error reading sensors, perform the lecture once more
				// time. after the second
				// Lecture, this return return normally
				// sensorsArray.read(SensorsArray.TYPE_ACTIONS);
				Thread.sleep(100);
				sensorsArray.read(SensorsArray.TYPE_ACTIONS);

				// if this sensor is active and with the corresponding text, hero is in a all in
				// situation. select
				// "continue hand" always (no cash out option) to maximize winnings
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
}
