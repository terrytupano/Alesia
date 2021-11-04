package plugins.hero;

import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.descriptive.*;
import org.jdesktop.application.*;

import core.*;
import plugins.hero.ozsoft.bots.*;
import plugins.hero.utils.*;

/**
 * this class represent the core of al hero plugins. As a core class, this class dependes of anothers classes in order
 * to build a useful player agent. the followin is a list of class from where the information is retrived, and the
 * actions are performed.
 * <ul>
 * <li>{@link PokerSimulator} - get the numerical values for decition making
 * <li>{@link RobotActuator} - permorm the action sended by this class.
 * <li>{@link SensorsArray} - perform visual operation of the enviorement
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
	private int roundCounter = 0;
	private long time1;
	private DescriptiveStatistics outGameStats;
	private boolean paused = false;
	private Map<String, Object> parameters;
	long stepMillis;
	// This variable is ONLY used and cleaned by ensuregametable method
	private String lastHoleCards = "";
	private double maxRekonAmmo;
	boolean oportinity = false;
	private double currentHandCost;
	private double playUntil;
	private long playTime;
	private Hashtable<String, PreflopCardsModel> preFlopCardsDist;

	private String subObtimalDist;

	public Trooper(SensorsArray array, PokerSimulator pokerSimulator) {
		super(Alesia.getInstance());
		this.availableActions = new ArrayList<>();
		this.pokerSimulator = pokerSimulator;
		if (array != null) {
			this.sensorsArray = array;
			this.robotActuator = new RobotActuator();
			this.pokerSimulator = sensorsArray.getPokerSimulator();
		}
		this.outGameStats = new DescriptiveStatistics(10);
		// this.pokerSimulator = sensorsArray.getPokerSimulator();
		this.roundCounter = 0;
		this.playUntil = 0;

		// load all preflop ranges
		this.preFlopCardsDist = new Hashtable<>();
		TEntry<String, String>[] tarr = PreflopCardsModel.getPreflopList();
		for (TEntry<String, String> tEntry : tarr) {
			String rName = tEntry.getKey();
			preFlopCardsDist.put(rName, new PreflopCardsModel(rName));
		}
	}

	public void cancelTrooper(boolean interrupt) {
		setVariableAndLog(STATUS, "Trooper Canceled.");
		super.cancel(interrupt);
	}

	/**
	 * link between {@link HeroBot} an this instance. this method perfom all the decitions and return the action that he
	 * want to execute
	 * 
	 * @return the action to execute
	 */
	public TrooperAction getSimulationAction() {
		playTime = System.currentTimeMillis() - Hero.getStartDate().getTime();
		clearEnviorement();
		decide();
		return act();
	}

	public boolean isPaused() {
		return paused;
	}

	public void pause(boolean pause) {
		this.paused = pause;
		setVariableAndLog(STATUS, paused ? "Trooper paused" : "Trooper resumed");
	}

	/**
	 * this method check the bluff parameter and act accordinly. when Hero is in range of the parameter
	 * <code>bluffUpperBound</code> and the hero cards are in range of the preflop card distribution named <b>bluff</b>
	 * this method will return <code>true</code> and override the main variable {@link #availableActions} and set only
	 * reise all actions for hero to bluff
	 * 
	 * @return <code>true</code> for bluff, <code>false</code> oetherwise
	 */
	private boolean checkOpportunities() {
		if ((boolean) parameters.get("takeOpportunity") == false)
			return false;

		String txt = null;
		double buyIn = pokerSimulator.buyIn;
		int bluffUpperB = Integer.parseInt(parameters.get("bluffUpperBound").toString());
		double chips = pokerSimulator.heroChips;
		double BUPB = (bluffUpperB / 100.0 * buyIn);

		// TEMP: implementation of bluffUpperBound is suspended. The direct selection of the preflop distribution take
		// control of the preflop frecuence (5% card selection ist direct correlate with 5% bluff frecuence).
		// TODO: this decition is BIAS and maybe muss be controled randomly
		// if (chips > 0 && chips < BUPB && bluffP <= p) {

		// Preflop
		if (pokerSimulator.currentRound == PokerSimulator.HOLE_CARDS_DEALT) {
			PreflopCardsModel bluff = preFlopCardsDist.get("bluff");
			if (bluff.containsHand(pokerSimulator.holeCards)) {
				txt = "Current Hole cards in bluff range.";
				setPreflopBluffActions();
			}
		}

		// posflop
		if (pokerSimulator.currentRound == PokerSimulator.FLOP_CARDS_DEALT) {
			if ((boolean) pokerSimulator.uoAEvaluation.get("isTheNut") == true)
				txt = "Is the Nuts.";

			double minWin = Integer.parseInt(parameters.get("oppLowerBound").toString());
			if (pokerSimulator.winProb_n >= minWin) {
				txt = String.format("Current win probability (%1.3f) >= (%1.3f) oppLowerBound",
						pokerSimulator.winProb_n , minWin);
			}
		}

		if (txt != null)
			setVariableAndLog(EXPLANATION, ">> OPPORTUNITY DETECTED: " + txt + " <<");

		return txt == null;
	}
	/**
	 * clear the enviorement for a new round.
	 * 
	 */
	private void clearEnviorement() {
		if (sensorsArray != null)
			sensorsArray.clearEnviorement();
		maxRekonAmmo = -1;
		currentHandCost = 0;
		oportinity = false;
		subObtimalDist = "Triangular";
		// at first time execution, a standar time of 10 second is used
		long tt = time1 == 0 ? 10000 : System.currentTimeMillis() - time1;
		outGameStats.addValue(tt);
		time1 = System.currentTimeMillis();
		// read troper variables again
		this.parameters = Hero.trooperPanel.getValues();
		Hero.heroLogger.fine("Game play time average=" + TStringUtils.formatSpeed((long) outGameStats.getMean()));
	}

	/**
	 * decide de action(s) to perform. This method is called when the {@link Trooper} detect that is my turn to play. At
	 * this point, the game enviorement is waiting for an accion.
	 * 
	 */
	private void decide() {

		// chek the status of the simulator in case of error. if an error is detected, fold
		if (pokerSimulator.getVariables().get(PokerSimulator.STATUS).equals(PokerSimulator.STATUS_ERROR)) {
			availableActions.add(new TrooperAction("fold", 0d));
			setVariableAndLog(EXPLANATION, "Error detected in simulator.");
			return;
		}

		// PREFLOP
		if (pokerSimulator.currentRound == PokerSimulator.HOLE_CARDS_DEALT) {
			if (!checkOpportunities())
				setPreflopActions();
		}

		// FLOP AND FUTHER
		if (pokerSimulator.currentRound > PokerSimulator.HOLE_CARDS_DEALT) {
			performDecisionMethod();

		}

		// if the list of available actions are empty, i habe no option but fold/check
		// in concordance whit rule1: if i can keep checking until i get a luck card. i will do. this behabior also
		// allow for example getpreflop action continue because some times, the enviorement is too fast and the trooper
		// is unable to retribe all information
		if (availableActions.size() == 0 && pokerSimulator.isSensorEnabled("call") && pokerSimulator.callValue <= 0) {
			setVariableAndLog(STATUS, "Empty list. Checking");
			availableActions.add(TrooperAction.CHECK);
		}

		// if the list of available actions are empty, the only posible action todo now is fold
		if (availableActions.size() == 0) {
			setVariableAndLog(STATUS, "Empty list. Folding");
			availableActions.add(TrooperAction.FOLD);
		}
	}

	/**
	 * compute and return the amount of chips available for actions. The number of amount are directe related to the
	 * currnet hand rank. More the rank, more chips to invest. This allow the troper invest ammunitons acording to a
	 * real chance of winning. The previos estimation based on probabilities send the trooper to invest a lot on
	 * amunitions in low value hands an is easy anbush by villans.
	 * 
	 **/
	private double getAmmunitions() {
		// EHS = HSn + (1 - HSn) x Ppot
		double EHS = PokerSimulator.HS_n + (1 - PokerSimulator.HS_n) * PokerSimulator.Ppot;
		double ammo = EHS * pokerSimulator.heroChips;
		String txt1 = String.format("%0.3f = %0.3f + (1 - %0.3f) Ammo = %7.2f", EHS, PokerSimulator.HS_n,
				PokerSimulator.HS_n, PokerSimulator.Ppot);
		setVariableAndLog(EXPLANATION, txt1);
		return ammo;
	}

	/**
	 * this method retrive the ammount of chips of all currentliy active villans. the 0 position is the amount of chips
	 * computed and the index 1 is the number of active villans
	 * 
	 * @return - [total chips, num of villans]
	 */
	private double[] getAvgBluffValue() {
		double[] rval = new double[2];
		// FIXME: for simulation purpose, return my chips
		if (sensorsArray == null) {
			rval[0] = pokerSimulator.heroChips;
			rval[1] = 3;
			return rval;
		}
		GameRecorder gameRecorder = sensorsArray.getGameRecorder();
		ArrayList<GamePlayer> list = gameRecorder.getAssesment();
		for (GamePlayer gp : list) {
			if (gp.isActive() && gp.getId() > 0) {
				rval[0] += gp.getChips();
				rval[1]++;
			}
		}
		rval[0] = rval[0] / rval[1];
		return rval;
	}

	private TrooperAction getSubOptimalAction() {
		Vector<TEntry<TrooperAction, Double>> actProb = new Vector<>();
		availableActions.forEach((ta) -> actProb.add(new TEntry(ta, ta.amount)));
		Collections.sort(actProb, Collections.reverseOrder());

		int elements = availableActions.size();
		double hs = pokerSimulator.getCurrentHandStreng() * elements;
		double mode = (hs > 1) ? elements : hs * elements;
		AbstractRealDistribution tdist = new TriangularDistribution(0, mode, elements);
		if (subObtimalDist.equals("UniformReal"))
			tdist = new UniformRealDistribution(0, elements);
		int[] singletos = new int[elements];
		double[] probabilities = new double[elements];
		for (int i = 0; i < elements; i++) {
			singletos[i] = i;
			TEntry<TrooperAction, Double> te = actProb.elementAt(i);
			probabilities[i] = tdist.probability(i, i + 1);
			te.setValue(probabilities[i]);
		}

		// action selecction range acording tDistributionRange parameter
		int actran = Integer.parseInt(parameters.get("tDistributionRange").toString());
		int ele = 99;
		while ((ele < mode - actran) || (ele > mode + actran)) {
			ele = (int) tdist.sample();
		}
		TrooperAction selact = actProb.elementAt(ele).getKey();
		pokerSimulator.setActionsData(selact, actProb);
		return selact;
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
			boolean isInt = (new Double(bb)).intValue() == bb && (new Double(sb)).intValue() == sb;
			double tick = raise;
			int step = 5;
			double ammoinc = imax / (step * 1.0);
			// TODO:
			// when tha call to this method, the parameter maximum = chips is valid val < meximus because tha all in
			// acction will take kare of maximum chip. but when maximum is another value, the comparation mus be <=
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
		double prob = PokerSimulator.winProb_n;
		double ammunitions = getAmmunitions();

		// no calculation for 0 values
		if (ammunitions == 0 || prob == 0) {
			availableActions.clear();
			Hero.heroLogger.info("No posible decision for values prob = " + twoDigitFormat.format(prob)
					+ " or ammuntion = " + twoDigitFormat.format(ammunitions));
			return;
		}
		for (TrooperAction act : availableActions) {
			double ev = (prob * ammunitions) - act.amount;
			act.expectedValue = ev;
		}
		// remove all negative values
		availableActions.removeIf(ta -> ta.expectedValue < 0);
		// 191228: Hero win his first game against TH app !!!!!!!!!!!!!!!! :D
	}

	/**
	 * Set the action based on the starting hand distribution. If the starting hand is inside on the predefined hands
	 * distribution, this method evaluate if a predefinde max amount of chips has ben reached (due to an allin or
	 * repeated villans.s rise/call) . if this is the case, the metodh clear the action list and the standar final
	 * action will be selected (check/fold).
	 * <p>
	 * the general idea here is try to put the trooper in folp, so the normal odds operation has chance to decide, at
	 * lower posible cost
	 */
	private void setPreflopActions() {
		availableActions.clear();
		String rName = (String) parameters.get("preflopCards");
		PreflopCardsModel pfcm = preFlopCardsDist.get(rName);
		boolean good = pfcm.containsHand(pokerSimulator.holeCards);
		if (!good) {
			setVariableAndLog(EXPLANATION, "Preflop hand not good.");
			return;
		}

		double pfBase = ((Number) parameters.get("preflopRekonAmmo.base")).doubleValue();
		double pfband = ((Number) parameters.get("preflopRekonAmmo.hand")).doubleValue();
		double base = pokerSimulator.bigBlind * pfBase;
		double ev = pfcm.getEV(pokerSimulator.holeCards);

		// maxreconammo = base + (inversion * ev)
		if (maxRekonAmmo == -1) {
			maxRekonAmmo = base + (pfband * ev);
		}

		double call = pokerSimulator.callValue;
		double raise = pokerSimulator.raiseValue;
		// can i check ??
		if (call == 0) {
			availableActions.add(TrooperAction.CHECK);
		} else {
			// can i call ?
			if (call > 0 && (call + currentHandCost) < maxRekonAmmo) {
				availableActions.add(new TrooperAction("call", call));
			} else {
				// the raise is mariginal ??
				if (raise != -1 && (raise + currentHandCost) < maxRekonAmmo) {
					availableActions.add(new TrooperAction("raise", raise));
				}
			}
		}
		if (availableActions.size() == 0) {
			setVariableAndLog(EXPLANATION, "Preflop hand in range but no more ammunition available.");
			return;
		}

		String txt1 = String.format("Preflop hand in range %7.2f + (%7.2f * %7.2f) = %7.2f", base, pfband, ev,
				maxRekonAmmo);
		setVariableAndLog(EXPLANATION, txt1);
	}

	/**
	 * clear the global variable {@link #availableActions} and set only <code>raise.pot</code> and
	 * <code>raise.allin</code> actions whit equal probability.
	 * <p>
	 * This method signal {@link #getSubOptimalAction()} to use UniformRealDistribution. this allow true random
	 * selection of all posible bluff actions
	 */
	private void setPreflopBluffActions() {
		availableActions.clear();
		subObtimalDist = "UniformReal";
		double[] bv = getAvgBluffValue();
		double bluff = bv[0];
		loadActions(bluff);

		// to this point, if availableactions are empty, means hero is responding a extreme hihgt raise. that mean meybe
		// hero is weak. at this point reise mean all in. (call actions is not considerer because is not bluff)
		if (availableActions.size() == 0 && pokerSimulator.raiseValue >= 0)
			availableActions.add(new TrooperAction("raise", pokerSimulator.raiseValue));
	}

	private void setVariableAndLog(String key, Object value) {
		String value1 = value.toString();
		if (value instanceof Double)
			value1 = fourDigitFormat.format(((Double) value).doubleValue());
		// append the playtime to the status (visual purpose only)
		if (STATUS.equals(key)) {
			value = "Play time " + timeFormat.format(new Date(playTime - TimeUnit.HOURS.toMillis(1))) + " loss limit "
					+ twoDigitFormat.format(playUntil) + " " + value.toString();
		}
		pokerSimulator.setVariable(key, value);
		// don.t log the status, only the explanatio
		if (!STATUS.equals(key)) {
			String key1 = key.replace(EXPLANATION, "");
			// 200210: Hero play his first 2 hours with REAL +EV. Convert 10000 chips in 64000
			Hero.heroLogger.info(key1 + value1);
		}
	}

	private boolean isMyTurnToPlay() {
		return sensorsArray.isSensorEnabled("fold") || sensorsArray.isSensorEnabled("call")
				|| sensorsArray.isSensorEnabled("raise");
	}

	/**
	 * This metod check all the sensor areas and perform the corrections to get the troper into the fight. The
	 * conbination of enabled/disabled status of the sensor determine the action to perform. If the enviorement request
	 * the trooper to play, this method return <code>true</code>,
	 * <p>
	 * This method return <code>false</code> when:
	 * <ol>
	 * <li>try to reach the gametable until an fix amount of time is reached. In that case, this method return
	 * <code>false</code>.
	 * <li>the buy-in window is displayed, in this case, cancel option is selected
	 * 
	 * @return <code>true</code> if the enviorement is waiting for the troper to {@link #decide()} and {@link #act()}.
	 */
	private boolean watchEnviorement() throws Exception {
		setVariableAndLog(STATUS, "Looking the table ...");
		// try during x seg. Some round on PS long like foreeeeveeerr
		long tottime = 300 * 1000;
		long t1 = System.currentTimeMillis();
		while (System.currentTimeMillis() - t1 < tottime) {
			// pause ?
			if (paused) {
				Thread.sleep(100);
				// update t1 var while is out. this avoid troper dismist because large pause is interpreted as a faule
				// in enviorement and trooper can.t reach the main table
				t1 = System.currentTimeMillis();
				continue;
			}
			// canceled ?
			if (isCancelled())
				return false;
			sensorsArray.readPlayerStat();
			sensorsArray.read(SensorsArray.TYPE_ACTIONS);

			// NEW ROUND: if the hero current hand is diferent to the last measure, clear the enviorement.
			String hc1 = sensorsArray.getSensor("hero.card1").getOCR();
			String hc2 = sensorsArray.getSensor("hero.card2").getOCR();
			String hch = hc1 == null ? "" : hc1;
			hch += hc2 == null ? "" : hc2;
			if (!("".equals(hch) || lastHoleCards.equals(hch))) {
				lastHoleCards = hch;
				setVariableAndLog(EXPLANATION,
						"--- Round " + (++roundCounter) + " " + pokerSimulator.getTableParameters() + " ---");

				// play time or play sae parameter parameters. when the play time is reach, the action sit.out is
				// clicked and hero return

				// play time
				double ptd = Double.parseDouble(parameters.get("play.time").toString());
				long playtimeParm = (long) (ptd * 3600 * 1000);
				playTime = System.currentTimeMillis() - Hero.getStartDate().getTime();

				// play until parameter
				double playUntilParm = Double.parseDouble(parameters.get("play.until").toString());
				// read hero chips. this avoid false tropper dismist after all in or bluff (hero chips was very low at
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

				clearEnviorement();
				setVariableAndLog(STATUS, "Looking the table ...");
				continue;
			}

			// enviorement is in the gametable
			if (isMyTurnToPlay()) {
				// repeat the look of the sensors. this is because some times the capture is during a animation
				// transition. to avoid error reading sensors, perform the lecture once more time. after the second
				// lecutre, this return return normaly
				sensorsArray.read(SensorsArray.TYPE_ACTIONS);
				return true;
			}

			// if buy-in window is displayed, hero lost the battle
			if (sensorsArray.isSensorEnabled("buyIn.cancel") && sensorsArray.isSensorEnabled("buyIn.ok")
					&& sensorsArray.isSensorEnabled("buyIn.point3")) {
				robotActuator.perform("buyIn.cancel");
				setVariableAndLog(EXPLANATION, "Trooper lost the battle !!!!");
				return false;
			}

			// the i.m back button is active (at this point, the enviorement must only being showing the i.m back
			// button)
			if (sensorsArray.isSensorEnabled("imBack")) {
				// robotActuator.perform("imBack");
				continue;
			}

			// if any of this are active, do nothig. raise.text in this case, is wachit a chackbok for check
			if (sensorsArray.isSensorEnabled("raise.text") || sensorsArray.isSensorEnabled("sensor1")) {
				continue;
			}
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
		if (robotActuator != null)
			robotActuator.perform(act);
		return act;
	}

	@Override
	protected Object doInBackground() throws Exception {

		// ensure db connection on the current thread.
		// try {
		// Alesia.getInstance().openDB();
		// Alesia.getInstance().openDB("hero");
		// } catch (Exception e) {
		// // just a warning log because reiterated pause/stop/play can generate error re opening the connection
		// Hero.heroLogger.warning(e.getMessage());
		// }

		clearEnviorement();

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

			boolean ingt = watchEnviorement();

			// if watchEnviorement() methdo return false, dismiss the troper.
			if (!ingt) {
				setVariableAndLog(EXPLANATION, "Tropper dismiss.");
				return null;
			}

			// at this point i must decide and act
			setVariableAndLog(STATUS, "Reading NUMBERS ...");
			// read first the numbers to update the dashboard whit the numerical values. this allow me some time to
			// inspect.
			// only for visula purporse
			sensorsArray.read(SensorsArray.TYPE_NUMBERS);
			setVariableAndLog(STATUS, "Reading CARDS ...");
			sensorsArray.read(SensorsArray.TYPE_CARDS);
			availableActions.clear();
			setVariableAndLog(STATUS, "Deciding ...");

			decide();
			act();
		}
		return null;
	}

	protected void performDecisionMethod() {
		String decisionM = parameters.get("decisionMethod").toString();
		if ("potOdd".equals(decisionM)) {
			loadActions(pokerSimulator.heroChips);
			potOdd();
		} else {
			// loadActions(pokerSimulator.heroChips);
			// TODO: future decition method or delete this method
		}
	}

	/**
	 * This method is invoked during the idle phase (after {@link #act()} and before {@link #decide()}. use this method
	 * to perform large computations.
	 */
	protected void think() {
		// setVariableAndLog(STATUS, "Reading villans ...");
		// sensorsArray.readVillan();
		// 191020: ayer ya la implementacion por omision jugo una partida completa y estuvo a punto de vencer a la
		// chatarra de Texas poker - poker holdem. A punto de vencer porque jugaba tan lento que me aburri del sueno :D
	}
}
