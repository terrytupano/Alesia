package plugins.hero;

import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.descriptive.*;
import org.jdesktop.application.*;

import com.javaflair.pokerprophesier.api.adapter.*;
import com.javaflair.pokerprophesier.api.card.*;

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
	private Hashtable<String, PreflopCardsModel> cardsRanges;

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
		this.cardsRanges = new Hashtable<>();
		TEntry<String, String>[] tarr = PreflopCardsModel.getPreflopList();
		for (TEntry<String, String> tEntry : tarr) {
			String rName = tEntry.getKey();
			cardsRanges.put(rName, new PreflopCardsModel(rName));
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
	 * this method override the global variable {@link #availableActions} when the tropper detect an oportunity.
	 * 
	 * @return <code>true</code> if hero has an oportunity
	 * @see #setBluffActions()
	 */
	private boolean checkPosflopBluff() {
		String txt = null;

		if (pokerSimulator.getMyHandHelper().isTheNuts())
			txt = "Is the Nuts";

		// trooper is set
		boolean set = pokerSimulator.isSet();

		// probability
		double prob = pokerSimulator.getProbability();

		// is a T o better card. T o better are the 40% of all card. this mean, hero hat 60% chance of win in a bluff
		if (pokerSimulator.getSignificantRank() > Card.NINE && set && prob > 0.66) {
			Card[] cards = pokerSimulator.getMyHandHelper().getSignificantCards();
			txt = cards.length + " Significant card with " + twoDigitFormat.format(prob);
		}

		if (txt != null) {
			setVariableAndLog(EXPLANATION, "--- Oportunity detected " + txt + " ---");
		}
		return txt != null;
	}

	/**
	 * this method check the bluff parameter and act accordinly. the bluff parameter is expresed in porcentage of when
	 * hero is allow to bluff. E.G: bluff=300, buyIn=10000, Nro. of elements in blufflist=10. With this parameteres,
	 * hero build a list of level 10000/10 = 10 levels. When an bluff oportunity is present, hero if only available to
	 * bluff when:
	 * <ul>
	 * <li>the EV ist > 0
	 * <li>the ammunitions is <= to the upper bound
	 * <li>the index of the currend hand (allow to bluf) >= ammunitions level.
	 * </ul>
	 * <p>
	 * the last option, allow hero to bluff with poor hand when hero is in hard situation. when hero recovery his chips,
	 * the index ist hi
	 * 
	 * @return <code>true</code> when this method clear the main variable {@link #availableActions} and set only reise
	 *         all actions for hero to bluff. false otherwise.
	 */
	private boolean checkPreflopBluff() {
		boolean ok = false;
		double bluffParm = Double.parseDouble(parameters.get("upperBoundBluff").toString());
		double chips = pokerSimulator.getHeroChips();
		double buyIn = pokerSimulator.buyIn;
		// if chips and bluff
		bluffParm = (bluffParm / 100.0 * buyIn);
		if (chips > 0 && chips < bluffParm) {
			PreflopCardsModel bluff = cardsRanges.get("bluff");
			HoleCards hc = pokerSimulator.getMyHoleCards();
			if (bluff.containsHand(hc)) {
				setVariableAndLog(EXPLANATION, "Hero is able to bluff.");
				setPreflopBluffActions();
				ok = true;
			}
		}
		return ok;
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
		if (pokerSimulator.getCurrentRound() == PokerSimulator.HOLE_CARDS_DEALT) {
			if (!checkPreflopBluff())
				setPreflopActions();
		}

		// FLOP AND FUTHER
		if (pokerSimulator.getCurrentRound() > PokerSimulator.HOLE_CARDS_DEALT) {
			performDecisionMethod();

		}

		// if the list of available actions are empty, i habe no option but fold/check
		// in concordance whit rule1: if i can keep checking until i get a luck card. i will do. this behabior also
		// allow for example getpreflop action continue because some times, the enviorement is too fast and the trooper
		// is unable to retribe all information
		if (availableActions.size() == 0 && pokerSimulator.isSensorEnabled("call")
				&& pokerSimulator.getCallValue() <= 0) {
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
	 * // double EHS = HS + (1 - HS) * pHS; // empirical top: 0.8131 prob with 20 ouds of improbe hand in the folowin
	 * example: Ts Qs Js Tc Ks // return hp / 0.8131;
	 * 
	 * TODO: control the number of amunitions per street. the trooper most dangeros disadvantege is when all the villas
	 * put small amount of chips during a lager period of time in a single street (generaly preflop) hero must avoid
	 * this situation because in subsecuent street, the pot will be so hight that hero will be available to go allin
	 * whit poor hands
	 **/
	private double getAmmunitions() {
		double pot = pokerSimulator.getPotValue();
		// int handPotential = pokerSimulator.getHandPotential();
		int handOuts = pokerSimulator.getHandPotentialOuts();
		double bBlind = pokerSimulator.bigBlind;

		String bluffMsg = "No bluff";
		double bluff = 0.0;
		if (checkPosflopBluff()) {
			double[] bv = getAvgBluffValue();
			bluff = bv[0];
			int vill = (int) bv[1];
			bluffMsg = twoDigitFormat.format(bluff) + " bluff form " + vill + " villans";
		}

		// PROBLEM: hero try to presuit hight hands. the relation handpotential / oppMostProbHand is to low, the result
		// is invest part of the pot * 2 or 3 times
		// solution: normailization of the result, or handpotential / oppMostProbHand
		// TEST: the future hand potential is in relation whit oppTopHand. this avoid hero to prusuit hand whit many
		// outs and hihgt
		double opt1 = pokerSimulator.getCurrentHandStreng(false);
		double opt2 = pokerSimulator.getSignificantRank() / (Card.ACE * 1.0);
		// when significat card is negative, haro has nothing. force selection to opt1
		opt2 = opt2 < 0 ? Double.MAX_VALUE : opt2;
		double handStreng = Math.min(opt1, opt2);
		String sufix = handStreng == opt1 ? "Hs" : "Sc";
		String myPotMsg = twoDigitFormat.format(pot) + " * " + twoDigitFormat.format(handStreng) + sufix;

		// int oppTopHand = pokerSimulator.getOppTopHand();

		// the current fraction of the pot ammount that until now, is already mine
		double myPot = pot * handStreng;

		// ammount of ammo that is worth to invest according to future outcome
		int factor = 2;
		double outAmmo = handOuts * bBlind * factor;
		// double fhp = (handPotential * 1.0) / (oppTopHand * 1.0);
		// double hsdiff = (pot - myPot) * fhp;
		double invest = 0.0;
		String investMsg = "";
		// TEST: assign nur the value computed in outshand. this allow hero only go for really good hand instead of try
		// whid hands that are por in outs
		// if (outAmmo > hsdiff) {
		invest = outAmmo;
		investMsg = (handOuts * factor) + " BB";
		// } else {
		// invest = hsdiff;
		// investMsg = twoDigitFormat.format((pot - myPot)) + " * " + twoDigitFormat.format(fhp);
		// }

		double ammunitions = myPot + invest + bluff;

		String txt1 = "(" + myPotMsg + ") + (" + investMsg + ") + (" + bluffMsg + ") = "
				+ twoDigitFormat.format(ammunitions);
		setVariableAndLog(EXPLANATION, txt1);
		return ammunitions;
	}

	/**
	 * return the EV for the preflop card accourding to the blufflist. this method return <code>-1</code> if no EV was
	 * found
	 * 
	 * @return ev or -1 private double getPreflopEV() { double ev = -1; HoleCards hc = pokerSimulator.getMyHoleCards();
	 *         String s = hc.isSuited() ? "s" : ""; String c1 = hc.getFirstCard().toString().substring(0, 1) +
	 *         hc.getSecondCard().toString().substring(0, 1); String c2 = hc.getSecondCard().toString().substring(0, 1)
	 *         + hc.getFirstCard().toString().substring(0, 1); ArrayList<String> list1 = bluffHands.stream().filter(lst
	 *         -> lst.get(0).equals(c1 + s)).findFirst().orElse(null); ArrayList<String> list2 =
	 *         bluffHands.stream().filter(lst -> lst.get(0).equals(c2 + s)).findFirst().orElse(null); ArrayList<String>
	 *         evvalues = list1 == null ? list2 : list1; int tablep = pokerSimulator.getTablePosition(); if (evvalues !=
	 *         null && tablep > -1) ev = Double.parseDouble(evvalues.get(tablep + 2)); return ev; }
	 */

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
			rval[0] = pokerSimulator.getHeroChips();
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

	private void handStrengFilter() {
		// fail safe.
		if (availableActions.isEmpty()) {
			Hero.heroLogger.info("handStrengFilter() has no actions to filter");
			return;
		}
		int orgActNum = availableActions.size();
		double handS = pokerSimulator.getCurrentHandStreng();
		double max = availableActions.stream().mapToDouble(act -> act.amount).max().getAsDouble();
		double upperB = max * handS;
		availableActions.removeIf(act -> act.amount > upperB);
		// invert sing so, suboptimal mehtod can order in the right way
		// availableActions.forEach((ta) -> ta.setAmount(ta.amount * -1.0));
		String fila = availableActions.size() == 0 ? "all" : "" + (orgActNum - availableActions.size());
		setVariableAndLog(EXPLANATION,
				"Hero hand streng ratio = " + twoDigitFormat.format(handS) + " " + fila + " actions removed");
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
		double call = pokerSimulator.getCallValue();
		double raise = pokerSimulator.getRaiseValue();
		double chips = pokerSimulator.getHeroChips();
		double pot = pokerSimulator.getPotValue();

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
		double prob = pokerSimulator.getProbability();
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
		// String val = availableActions.keySet().stream().map(k -> k + "=" +
		// twoDigitFormat.format(asociatedCost.get(k)))
		// .collect(Collectors.joining(", "));
		// val = val.trim().isEmpty() ? "No positive EV" : val;
		// TODO: log availa actions??
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
		HoleCards holeCards = pokerSimulator.getMyHoleCards();
		PreflopCardsModel cardsRange = cardsRanges.get(rName);
		boolean good = cardsRange.containsHand(holeCards);
		if (!good) {
			setVariableAndLog(EXPLANATION, "Preflop hand not good.");
			return;
		}
		double pfBase = ((Number) parameters.get("preflopRekonAmmo.base")).doubleValue();
		double bBlind = pokerSimulator.bigBlind;
		double base = bBlind * pfBase;

		// chen score is a renge [7,22] - 7 = [0, 15]
		double chenRank = Hero.getChenScore(holeCards);
		chenRank = (chenRank - 7d) / 15d;
		if (maxRekonAmmo == -1) {
			maxRekonAmmo = base + (bBlind * chenRank);
		}

		double call = pokerSimulator.getCallValue();
		double raise = pokerSimulator.getRaiseValue();
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

		String txt1 = String.format("Preflop hand in range %7.2f (%7.2f * %7.2f) = %7.2f", base, bBlind, chenRank,
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
		double raise = pokerSimulator.getRaiseValue();
		if (availableActions.size() == 0 && raise >= 0)
			availableActions.add(new TrooperAction("raise", raise));
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
					playUntil = pokerSimulator.getHeroChipsMax() - (playUntilParm * pokerSimulator.buyIn);

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
			loadActions(pokerSimulator.getHeroChips());
			potOdd();
		} else {
			loadActions(pokerSimulator.getHeroChips());
			// loadPostFlopActions(pokerSimulator.getPotValue());
			handStrengFilter();
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
