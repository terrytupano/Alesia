package plugins.hero;

import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.descriptive.*;
import org.apache.poi.hsmf.parsers.*;
import org.jdesktop.application.*;

import com.javaflair.pokerprophesier.api.adapter.*;
import com.javaflair.pokerprophesier.api.card.*;

import core.*;

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

	private static Trooper instance;
	private static DecimalFormat fourDigitFormat = new DecimalFormat("#0.0000");
	private static DecimalFormat twoDigitFormat = new DecimalFormat("#0.00");
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	public static String EXPLANATION = "aa.Troper Explanation";
	public static String STATUS = "aa.Troper Status";

	private PokerSimulator pokerSimulator;
	private SensorsArray sensorsArray;
	private RobotActuator robotActuator;
	private Hashtable<String, Double> availableActions;
	private Hashtable<String, Double> asociatedCost;
	private int countdown = 5;
	private long time1;
	private DescriptiveStatistics outGameStats;
	private boolean paused = false;
	private Hashtable<String, Object> parameters;
	private TreeMap<String, String> preflopHands;
	private Vector<String> bluffHands;
	private Vector<Hashtable<Integer, Double>> bluffEVValues;
	long stepMillis;
	// This variable is ONLY used and cleaned by ensuregametable method
	private String lastHoleCards = "";
	private double maxRekonAmmo;
	boolean oportinity = false;
	private double currentHandCost;
	private double playUntil;
	private long playTime;

	public Trooper() {
		super(Alesia.getInstance());
		this.robotActuator = new RobotActuator();
		this.availableActions = new Hashtable();
		this.asociatedCost = new Hashtable();
		this.sensorsArray = Hero.sensorsArray;
		this.outGameStats = new DescriptiveStatistics(10);
		this.pokerSimulator = sensorsArray.getPokerSimulator();
		instance = this;
		this.preflopHands = TStringUtils.getProperties("preflop.card");
		// build bluff hand
		this.bluffHands = new Vector();
		this.bluffEVValues = new Vector();
		TreeMap<String, String> tmp = TStringUtils.getProperties("bluff.card");
		ArrayList<String> l = new ArrayList<>(tmp.keySet());
		for (String key : l) {
			String[] row = tmp.get(key).split("[\t]");
			Hashtable positions = new Hashtable<Integer, Double>();
			String hand = row[0];
			for (int i = 2; i < row.length; i++)
				positions.put(i - 1, new Double(row[i]));
			bluffHands.add(hand);
			bluffEVValues.add(positions);
		}
	}

	public static Trooper getInstance() {
		return instance;
	}
	public void cancelTrooper(boolean interrupt) {
		setVariableAndLog(STATUS, "Trooper Canceled.");
		super.cancel(interrupt);
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
	 * @see PokerSimulator#isOportunity()
	 * @see #setBluffActions()
	 */
	private boolean checkOportunities() {
		// check form oportunity
		String txt = pokerSimulator.isOportunity();
		if (txt != null) {
			setBluffActions();
			Hero.heroLogger.info("Oportunity detected ----------");
			Hero.heroLogger.info(txt);
			return true;
		}
		return false;
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
		boolean bluff = false;
		double bluffParm = Double.parseDouble(parameters.get("bluff").toString());
		double chips = pokerSimulator.getHeroChips();
		double buyIn = pokerSimulator.getBuyIn();
		double ev = getPreflopEV();
		// if chips and bluff
		bluffParm = (bluffParm / 100.0 * buyIn);
		// playGoal = (chips / playGoal) * 100;

		if (chips > 0 && chips < bluffParm) {
			// TEMP: only bluff whit super duper hands !!!
			if (ev >= 1) {
				setVariableAndLog(EXPLANATION, "Hero is able to bluff. EV = " + twoDigitFormat.format(ev));
				setBluffActions();
				bluff = true;
			}
		}
		return bluff;
	}
	/**
	 * clear the enviorement for a new round.
	 * 
	 */
	private void clearEnviorement() {
		sensorsArray.clearEnviorement();
		maxRekonAmmo = -1;
		currentHandCost = 0;
		oportinity = false;
		// at first time execution, a standar time of 10 second is used
		long tt = time1 == 0 ? 10000 : System.currentTimeMillis() - time1;
		outGameStats.addValue(tt);
		time1 = System.currentTimeMillis();
		// read troper variables again
		this.parameters = Hero.heroPanel.getTrooperPanel().getValues();
		Hero.heroLogger.fine("Game play time average=" + TStringUtils.formatSpeed((long) outGameStats.getMean()));
	}

	/**
	 * decide de action(s) to perform. This method is called when the {@link Trooper} detect that is my turn to play. At
	 * this point, the game enviorement is waiting for an accion.
	 * 
	 */
	private void decide() {
		setVariableAndLog(STATUS, "Deciding ...");
		// read first the numbers to update the dashboard whit the numerical values. this allow me some time to inspect.
		// only for visula purporse
		sensorsArray.read(SensorsArray.TYPE_NUMBERS);
		sensorsArray.read(SensorsArray.TYPE_CARDS);
		availableActions.clear();

		// chek the status of the simulator in case of error. if an error is detected, fold
		if (pokerSimulator.getVariables().get(PokerSimulator.STATUS).equals(PokerSimulator.STATUS_ERROR)) {
			availableActions.put("fold", 1.0);
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
			if (!checkOportunities())
				performDecisionMethod();

		}

		// if the list of available actions are empty, i habe no option but fold/check
		// in concordance whit rule1: if i can keep checking until i get a luck card. i will do. this behabior also
		// allow for example getpreflop action continue because some times, the enviorement is too fast and the trooper
		// is unable to retribe all information
		if (availableActions.size() == 0 && sensorsArray.getSensor("call").isEnabled()
				&& pokerSimulator.getCallValue() <= 0) {
			setVariableAndLog(STATUS, "Empty list. Checking");
			availableActions.put("call", 1.0);
			asociatedCost.put("call", 0.0);
		}

		// if the list of available actions are empty, the only posible action todo now is fold
		if (availableActions.size() == 0) {
			setVariableAndLog(STATUS, "Empty list. Folding");
			availableActions.put("fold", 1.0);
			asociatedCost.put("fold", 0.0);
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
		double handStreng = pokerSimulator.getCurrentHandStreng(true);
		int handPotential = pokerSimulator.getHandPotential();
		int handOuts = pokerSimulator.getHandPotentialOuts();
		int oppHand = pokerSimulator.getOppMostProbHand();
		double bBlind = pokerSimulator.getBigBlind();

		// the current fraction of the pot ammount that until now, is already mine
		double myPot = pot * handStreng;

		// ammount of ammo that is worth to invest according to future outcome
		double outAmmo = handOuts * bBlind;
		double fhp = (handPotential * 1.0) / (oppHand * 1.0);
		double hsdiff = (pot - myPot) * fhp;
		double invest = 0.0;
		String investMsg = "";
		if (outAmmo > hsdiff) {
			invest = outAmmo;
			investMsg = handOuts + " BB";
		} else {
			invest = hsdiff;
			investMsg = twoDigitFormat.format((pot - myPot)) + " * " + twoDigitFormat.format(fhp);
		}

		double ammunitions = myPot + invest;
		String myPotMsg = twoDigitFormat.format(pot) + " * " + twoDigitFormat.format(handStreng);

		// TEST: fail safe: when topOpphand and mostProbHand are equals, is because this is a real danger. set the pot =
		// 0 to force fold
		boolean danger = pokerSimulator.getOppMostProbHand() == pokerSimulator.getOppTopHand();
		if (danger && pokerSimulator.getCurrentHandRank() < pokerSimulator.getOppTopHand()) {
			myPotMsg = "WARNING!!";
			ammunitions = 0;
		}

		// double actvlim = (pot - pokerSimulator.getPrevPotValue()) / sensorsArray.getActiveVillans();
		// actvlim = actvlim/bBlind;
		// if (actvlim > 20) {
		// Card[] heroc = pokerSimulator.getMyHoleCards().getCards();
		// if (heroc[0].getRank() < Card.NINE || heroc[1].getRank() < Card.NINE) {
		// handStreng = 0;
		// myPotMsg = "WARNING!!";
		// }
		// }
		///////////////

		String txt1 = "(" + myPotMsg + ") + (" + investMsg + ") = " + twoDigitFormat.format(ammunitions);
		setVariableAndLog(EXPLANATION, txt1);
		return ammunitions;
	}

	private double getImpliedOdd() {
		double call = pokerSimulator.getCallValue();
		double raise = pokerSimulator.getRaiseValue();
		double pot = pokerSimulator.getPotValue();

		if (call >= 0)
			availableActions.put("call", call);

		// if (raise >= 0)
		// availableActions.put("raise", raise);

		return 0.0;
	}

	/**
	 * return the EV for the preflop card accourding to the blufflist. this method return <code>-1</code> if no EV was
	 * found
	 * 
	 * @return ev or -1
	 */
	private double getPreflopEV() {
		double ev = -1;
		HoleCards hc = pokerSimulator.getMyHoleCards();
		String s = hc.isSuited() ? "s" : "";
		String c1 = hc.getFirstCard().toString().substring(0, 1) + hc.getSecondCard().toString().substring(0, 1);
		String c2 = hc.getSecondCard().toString().substring(0, 1) + hc.getFirstCard().toString().substring(0, 1);
		int idx1 = bluffHands.indexOf(c1 + s);
		int idx2 = bluffHands.indexOf(c2 + s);
		Hashtable<Integer, Double> evvalues = null;
		int index = -1;
		if (idx1 > -1) {
			evvalues = bluffEVValues.elementAt(idx1);
			index = idx1;
		}
		if (idx2 > -1) {
			evvalues = bluffEVValues.elementAt(idx2);
			index = idx2;
		}
		int tablep = pokerSimulator.getTablePosition();
		if (evvalues != null && tablep > -1)
			ev = evvalues.get(tablep);
		return ev;
	}

	private String getSubOptimalAction() {
		Vector<TEntry<String, Double>> actProb = new Vector<>();
		availableActions.forEach((key, val) -> actProb.add(new TEntry(key, val)));
		Collections.sort(actProb, Collections.reverseOrder());

		int elements = availableActions.size();
		double hs = pokerSimulator.getCurrentHandStreng() * elements;
		double mode = (hs > 1) ? elements : hs * elements;
		TriangularDistribution tdist = new TriangularDistribution(0, mode, elements);
		int[] singletos = new int[elements];
		double[] probabilities = new double[elements];
		for (int i = 0; i < elements; i++) {
			singletos[i] = i;
			TEntry<String, Double> te = actProb.elementAt(i);
			probabilities[i] = tdist.probability(i, i + 1);
			te.setValue(probabilities[i]);
		}

		// action selecction range acording tDistributionRange parameter
		int actran = Integer.parseInt(parameters.get("tDistributionRange").toString());
		int ele = 99;
		while ((ele < mode - actran) || (ele > mode + actran)) {
			ele = (int) tdist.sample();
		}
		String selact = actProb.elementAt(ele).getKey();
		pokerSimulator.setActionsData(selact, actProb);
		return selact;
	}

	/**
	 * Check acordig to the <code>preflopStrategy</code> parameter, if this hand is a good preflop hand. if this metod
	 * return <code>null</code>, it is because this hand is not a good preflopa hand
	 * 
	 * TODO: Check Loky from UoA. there is a table with a complete hand distribution for preflop
	 * 
	 * return a String text for explanation. <code>null</code> for not good preflop hand
	 */
	private String isGoodPreflopHand() {
		String txt = null;
		String strategy = (String) parameters.get("preflopStrategy");

		// play all cards
		if (strategy.equals("ALL")) {
			txt = "strategy = ALL";
			return txt;
		}
		// upper half (2345678 | 9TJQKA)
		if (strategy.equals("UPPER")) {
			Card[] heroc = pokerSimulator.getMyHoleCards().getCards();
			// pocket pair
			if (pokerSimulator.getMyHandHelper().isPocketPair()) {
				txt = "A pocket pair";
			}
			if (heroc[0].getRank() >= Card.NINE && heroc[1].getRank() >= Card.NINE) {
				txt = "Upper half";
			}
			// A or K
			if ((heroc[0].getRank() > Card.QUEEN || heroc[1].getRank() > Card.QUEEN))
				txt = "A or K";

			// TEMP: suited Q
			if ((heroc[0].getRank() == Card.QUEEN || heroc[1].getRank() == Card.QUEEN)
					&& pokerSimulator.getMyHoleCards().isSuited())
				txt = "Suited Queen";

			return txt;
		}

		// +EV list
		if (strategy.equals("PRELIST")) {
			HoleCards hc = pokerSimulator.getMyHoleCards();
			String s = hc.isSuited() ? "s" : "";
			String c1 = hc.getFirstCard().toString().substring(0, 1) + hc.getSecondCard().toString().substring(0, 1);
			String c2 = hc.getSecondCard().toString().substring(0, 1) + hc.getFirstCard().toString().substring(0, 1);
			boolean inList = preflopHands.containsValue(c1 + s) || preflopHands.containsValue(c2 + s);
			txt = inList ? "Preflop hand it is in positive EV list" : null;
			return txt;
		}

		// naive prflop selection
		if (strategy.equals("NAIVE")) {
			// pocket pair
			if (pokerSimulator.getMyHandHelper().isPocketPair()) {
				txt = "A pocket pair";
			}

			// suited
			if (pokerSimulator.getMyHoleCards().isSuited())
				txt = "preflop hand is suited";

			// connected: cernters cards separated only by 1 or 2 cards provides de best probabilities (>6%)
			double sp = pokerSimulator.getMyHandStatsHelper().getStraightProb();
			if (sp >= 0.060)
				txt = "Posible straight";

			// J or higher
			Card[] heroc = pokerSimulator.getMyHoleCards().getCards();
			if (heroc[0].getRank() > Card.TEN && heroc[1].getRank() > Card.TEN)
				txt = "J or higher";

			return txt;
		}
		return null;
	}

	private boolean isMyTurnToPlay() {
		return sensorsArray.isSensorEnabled("fold") || sensorsArray.isSensorEnabled("call")
				|| sensorsArray.isSensorEnabled("raise");
	}

	/**
	 * thie method build a list of all actions available for the troper to perform. this mean, all action at first are
	 * consider alls posible. After the list ist build, this list is procesed acording to the selected method. those
	 * method will analize all entryes and select the actions acordinly. the result is stored in the gobal valiabel
	 * {@link #availableActions}
	 * 
	 * @param sourceName - the name of the source
	 * @param sourceValue - the value
	 */
	private void loadPostFlopActions(double maximum) {
		availableActions.clear();
		double call = pokerSimulator.getCallValue();
		double raise = pokerSimulator.getRaiseValue();
		double chips = pokerSimulator.getHeroChips();
		double pot = pokerSimulator.getPotValue();

		double imax = maximum == chips ? chips : pot;

		if (call >= 0)
			availableActions.put("call", call);

		if (raise >= 0)
			availableActions.put("raise", raise);

		if (pot >= 0 && sensorsArray.getSensor("raise.pot").isEnabled())
			availableActions.put("raise.pot;raise", pot);

		if (chips >= 0 && sensorsArray.getSensor("raise.allin").isEnabled())
			availableActions.put("raise.allin;raise", chips);

		double sb = pokerSimulator.getSmallBlind();
		double bb = pokerSimulator.getBigBlind();
		if (raise > 0 && sensorsArray.getSensor("raise.slider").isEnabled()) {
			// check for int or double values for blinds
			boolean isInt = (new Double(bb)).intValue() == bb && (new Double(sb)).intValue() == sb;
			double tick = raise;
			int step = 5;
			double ammoinc = imax / (step * 1.0);
			// if the differencie bettween raise and imax is less than 10bb is because the reise value extremly
			// hight or hero hat low imax. in this case, ignore the reise text actions because are irrelevant. the
			// relevant actions here are button actions (pot, call, reise, max)
			// if (ammoinc > bb * 2) {
			for (int c = 0; (c < step && (tick + ammoinc) < imax); c++) {
				tick += ammoinc;
				// round value to look natural (dont write 12345. write 12340 or 12350)
				if (isInt)
					tick = ((int) (tick / 10)) * 10;

				String txt = isInt ? "" + (int) tick : twoDigitFormat.format(tick);
				availableActions.put("raise.text,dc;raise.text,k=" + txt + ";raise", tick);
			}
			// }
		}

		// ////////////////////////
		// if (maximum == pot)
		// availableActions.values().removeIf(dv -> dv > maximum);

	}

	private void loadPostFlopActionsOrg() {
		availableActions.clear();

		double call = pokerSimulator.getCallValue();
		double raise = pokerSimulator.getRaiseValue();
		double chips = pokerSimulator.getHeroChips();
		double pot = pokerSimulator.getPotValue();

		if (call >= 0)
			availableActions.put("call", call);

		if (raise >= 0)
			availableActions.put("raise", raise);

		if (pot >= 0 && sensorsArray.getSensor("raise.pot").isEnabled())
			availableActions.put("raise.pot;raise", pot);

		if (chips >= 0 && sensorsArray.getSensor("raise.allin").isEnabled())
			availableActions.put("raise.allin;raise", chips);

		double sb = pokerSimulator.getSmallBlind();
		double bb = pokerSimulator.getBigBlind();
		if (raise > 0 && sensorsArray.getSensor("raise.slider").isEnabled()) {
			// check for int or double values for blinds
			boolean isInt = (new Double(bb)).intValue() == bb && (new Double(sb)).intValue() == sb;
			double tick = raise;
			double ammoinc = chips / 10;
			// if the differencie bettween raise and chips is less than 10bb is because the reise value extremly
			// hight or hero hat low chips. in this case, ignore the reise text actions because are irrelevant. the
			// relevant actions here are button actions (pot, call, reise, max)
			// if (ammoinc > bb * 2) {
			for (int c = 0; (c < 10 && (tick + ammoinc) < chips); c++) {
				tick += ammoinc;
				// round value to look natural (dont write 12345. write 12340 or 12350)
				if (isInt)
					tick = ((int) (tick / 10)) * 10;

				String txt = isInt ? "" + (int) tick : twoDigitFormat.format(tick);
				availableActions.put("raise.text,dc;raise.text,k=" + txt + ";raise", tick);
			}
			// }
		}
	}

	/**
	 * clear the global variable {@link #availableActions} and set only <code>raise.pot</code> and/or
	 * <code>raise.allin</code> actions whit equal probability. all actions or only one can be present depend of the
	 * enviorement
	 */
	private void setBluffActions() {
		availableActions.clear();
		double chips = pokerSimulator.getHeroChips();
		double pot = pokerSimulator.getPotValue();

		if (pot >= 0 && sensorsArray.getSensor("raise.pot").isEnabled())
			availableActions.put("raise.pot;raise", 0.5);

		if (chips >= 0 && sensorsArray.getSensor("raise.allin").isEnabled())
			availableActions.put("raise.allin;raise", 0.5);
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
		String prehand = isGoodPreflopHand();
		if (prehand == null) {
			setVariableAndLog(EXPLANATION, "Preflop hand not good.");
			return;
		}
		double pfBase = ((Number) parameters.get("preflopRekonAmmo.base")).doubleValue();
		double chips = pokerSimulator.getHeroChips();
		double pfHStreng = ((Number) parameters.get("preflopRekonAmmo.hand")).doubleValue();
		double base = pokerSimulator.getBigBlind() * pfBase;
		double ammo = pokerSimulator.getBigBlind() * pfHStreng;
		double streng = pokerSimulator.getPreFlopHandStreng();

		// standar preflop streng.
		double preflopStr = base + (ammo * streng);

		// assign values from +EV list. this allow hero don.t throw good preflop so easily. preflop hand like AK cann
		// suport more call form pot stillers
		double pfev = getPreflopEV();
		double pfsev = chips * pfev;
		if (pfev > 0 && preflopStr < pfsev)
			preflopStr = pfsev;

		if (maxRekonAmmo == -1)
			maxRekonAmmo = preflopStr;

		if (currentHandCost >= maxRekonAmmo) {
			setVariableAndLog(EXPLANATION, prehand + " but no more ammunition available.");
			return;
		}

		double call = pokerSimulator.getCallValue();
		double raise = pokerSimulator.getRaiseValue();
		// can i check ??
		if (call == 0) {
			availableActions.put("call", 0.0);
		} else {
			// can i call ?
			if (call > 0 && (call + currentHandCost) < maxRekonAmmo) {
				availableActions.put("call", call);
			} else {
				// the raise is mariginal ??
				if (raise != -1 && (raise + currentHandCost) < maxRekonAmmo) {
					availableActions.put("raise", raise);
				}
			}
		}
		updateAsociatedCost();

		String txt1 = prehand + " " + twoDigitFormat.format(base) + " + (" + twoDigitFormat.format(ammo) + " * "
				+ twoDigitFormat.format(streng) + ") = " + twoDigitFormat.format(maxRekonAmmo);
		setVariableAndLog(EXPLANATION, txt1);
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
				setVariableAndLog(EXPLANATION, "new round ----------");

				// play time or play sae parameter parameters. when the play time is reach, the action sit.out is
				// clicked
				// and hero return
				// play time.
				double ptd = Double.parseDouble(parameters.get("play.time").toString());
				long playtimeParm = (long) (ptd * 3600 * 1000);
				playTime = System.currentTimeMillis() - Hero.getStartDate().getTime();

				// play until parameter
				double playUntilParm = Double.parseDouble(parameters.get("play.until").toString());
				double chips = pokerSimulator.getHeroChips();
				// if chips are not available, show the last computed play safe value
				if (chips > 0)
					playUntil = pokerSimulator.getHeroChipsMax() - (playUntilParm * pokerSimulator.getBuyIn());

				if ((playtimeParm > 0 && playTime > playtimeParm)
						|| (chips > 0 && playUntilParm > 0 && chips <= playUntil)
								&& sensorsArray.isSensorEnabled("sit.out")) {
					robotActuator.perform("sit.out");
					robotActuator.perform("fold");
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
	protected void act() {
		setVariableAndLog(STATUS, "Acting ...");
		String ha = getSubOptimalAction();
		// normaly the cost is know. but sometimes(like in oportunities) not
		Double cost = asociatedCost.get(ha);
		if (cost != null)
			currentHandCost += cost;
		String key = "trooper.Action performed";
		pokerSimulator.setVariable(key, ha + " current cost " + twoDigitFormat.format(currentHandCost));
		// robot actuator perform the log
		robotActuator.perform(ha);
	}

	@Override
	protected Object doInBackground() throws Exception {

		// ensure db connection on the current thread.
		try {
			Alesia.openDB();
			Alesia.openDB("hero");
		} catch (Exception e) {
			// just a warning log because reiterated pause/stop/play can generate error re opening the connection
			Hero.heroLogger.warning(e.getMessage());
		}

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
			decide();
			act();
		}
		return null;
	}

	private void handStrengFilter() {
		// fail safe.
		if (availableActions.isEmpty()) {
			Hero.heroLogger.info("handStrengFilter() has no actions to filter");
			return;
		}
		int orgActNum = availableActions.size();
		double handS = pokerSimulator.getCurrentHandStreng();
		double max = availableActions.values().stream().mapToDouble(val -> val).max().getAsDouble();
		double upperB = max * handS;
		availableActions.values().removeIf(val -> val > upperB);
		updateAsociatedCost();
		// invert sing so, suboptimal mehtod can order in the right way
		availableActions.values().forEach(val -> val = val * -1.0);
		String fila = availableActions.size() == 0 ? "all" : "" + (orgActNum - availableActions.size());
		setVariableAndLog(EXPLANATION,
				"Hero hand streng ratio = " + twoDigitFormat.format(handS) + " " + fila + " actions removed");
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
		updateAsociatedCost();
		for (String act : availableActions.keySet()) {
			double cost = availableActions.get(act);
			double ev = (prob * ammunitions) - cost;
			availableActions.put(act, ev);
		}
		// remove all negative values
		availableActions.values().removeIf(dv -> dv < 0);

		// 191228: Hero win his first game against TH app !!!!!!!!!!!!!!!! :D
		String val = availableActions.keySet().stream().map(k -> k + "=" + twoDigitFormat.format(asociatedCost.get(k)))
				.collect(Collectors.joining(", "));
		val = val.trim().isEmpty() ? "No positive EV" : val;
		// TODO: log availa actions??
	}
	protected void performDecisionMethod() {
		String decisionM = parameters.get("decisionMethod").toString();
		if ("potOdd".equals(decisionM)) {
			loadPostFlopActions(pokerSimulator.getHeroChips());
			potOdd();
		} else {
			loadPostFlopActions(pokerSimulator.getPotValue());
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
	/**
	 * update the internal table with de cost of each action.
	 * <p>
	 * NOTE: call this metod befor {@link #decisionMethod(double)} because this method change the cost by EV.
	 */
	protected void updateAsociatedCost() {
		asociatedCost.clear();
		for (String act : availableActions.keySet()) {
			double cost = availableActions.get(act);
			asociatedCost.put(act, cost);
		}
	}
}
