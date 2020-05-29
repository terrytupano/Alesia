package plugins.hero;

import java.text.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.math3.distribution.*;
import org.apache.commons.math3.stat.descriptive.*;
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
 * case, the {@link #setPrefloopActions()} is called as a last resource.
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
	private Hashtable<String, String> preflopHands;
	long stepMillis;
	// This variable is ONLY used and cleaned by ensuregametable method
	private String lastHoleCards = "";
	private double maxRekonAmmo;
	boolean oportinity = false;

	private double currentHandCost;

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
		Hero.logger.fine("Game play time average=" + TStringUtils.formatSpeed((long) outGameStats.getMean()));
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
		// if normal pot odd action has no action todo, check the preflopcards.
		if (pokerSimulator.getCurrentRound() == PokerSimulator.HOLE_CARDS_DEALT) {
			setPrefloopActions();
		}

		// FLOP AND FUTHER
		if (pokerSimulator.getCurrentRound() > PokerSimulator.HOLE_CARDS_DEALT) {
			setPostFlopActions();
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

	private double getAmmunitions2() {
		double HS = pokerSimulator.getCurrentHandStreng();
		double pHS = pokerSimulator.getHandPotential();
		double pot = pokerSimulator.getPotValue();
		int villans = sensorsArray.getActiveVillans();

		double EHS = HS + (1 - HS) * pHS;
		// empirical base
		double base = pokerSimulator.getBigBlind() * villans;

		// double number = base + myPot + (invest * potential);
		double number = base + pot * EHS;

		String txt1 = twoDigitFormat.format(base) + " + (" + twoDigitFormat.format(pot) + " * "
				+ twoDigitFormat.format(HS) + ") + (1 - " + twoDigitFormat.format(HS) + ") * "
				+ twoDigitFormat.format(pHS) + " = " + twoDigitFormat.format(number);
		setVariableAndLog(EXPLANATION, txt1);

		// temporal for record stats in sensorarray
		pokerSimulator.setVariable("EHSValue", number);

		return number;
	}

	/**
	 * compute and return the amount of chips available for actions. The number of amount are directe related to the
	 * currnet hand rank. More the rank, more chips to invest. This allow the troper invest ammunitons acording to a
	 * real chance of winning. The previos estimation based on probabilities send the trooper to invest a lot on
	 * amunitions in low value hands an is easy anbush by villans.
	 * 
	 * TODO: control the number of amunitions per street. the trooper most dangeros disadvantege is when all the villas
	 * put small amount of chips during a lager period of time in a single street (generaly preflop) hero must avoid
	 * this situation because in subsecuent street, the pot will be so hight that hero will be available to go allin
	 * whit poor hands
	 * 
	 * @return amunitions
	 */
	private double getAmmunitions() {
		double handStreng = pokerSimulator.getCurrentHandStreng();
		double potential = pokerSimulator.getHandPotential();
		double chips = pokerSimulator.getHeroChips();
		double buyIn = pokerSimulator.getBuyIn();
		double pot = pokerSimulator.getPotValue();

		// empirical base
		double base = pokerSimulator.getBigBlind() * 5;

		// the source of invest can arrive from 2 sources: hero.s chips or buy in.: When hero is poor, play safe. when
		// is richt, play whit more room to invest
		double invest = Math.min(chips, buyIn);

		double myPot = (pot * handStreng);
		invest = pot - myPot;

		// double number = base + myPot + (invest * potential);
		double number = base + myPot + (invest * potential);

		String txt1 = twoDigitFormat.format(base) + " + (" + twoDigitFormat.format(pot) + " * "
				+ twoDigitFormat.format(handStreng) + ") + (" + twoDigitFormat.format(invest) + " * "
				+ twoDigitFormat.format(potential) + ") = " + twoDigitFormat.format(number);
		// String txt1 = twoDigitFormat.format(base) + " + (" + twoDigitFormat.format(pot) + " * "
		// + twoDigitFormat.format(handStreng) + ") + (" + twoDigitFormat.format(invest) + " * "
		// + twoDigitFormat.format(potential) + ") = " + twoDigitFormat.format(number);

		setVariableAndLog(EXPLANATION, txt1);
		return number;
	}

	/**
	 * perform a random selection of the available actions. this method build a probability distribution and select
	 * randmly a variate of that distribution. this is suboptimal because the objetive function already has the optimal
	 * action to perfomr. but this behavior make the trooper visible to the villans that can use this informatiion for
	 * trap or fool the troper.
	 * <p>
	 * the available actions and his values must be previous evaluated. The action value express the hihest expected
	 * return.
	 * 
	 */
	private String getSubOptimalAction() {
		int elements = availableActions.size();
		double denom = availableActions.values().stream().mapToDouble(dv -> dv.doubleValue()).sum();
		int[] singletos = new int[elements];
		Vector<Double> vals = new Vector<>(availableActions.values());
		double[] probabilities = new double[elements];
		for (int i = 0; i < elements; i++) {
			singletos[i] = i;
			double cost = vals.get(i);
			probabilities[i] = cost / denom;
		}
		EnumeratedIntegerDistribution dist = new EnumeratedIntegerDistribution(singletos, probabilities);
		Vector<TEntry<String, Double>> visv = new Vector<>();
		availableActions.forEach((key, val) -> visv.add(new TEntry(key, val)));
		String selact = visv.elementAt(dist.sample()).getKey();
		pokerSimulator.setActionsData(selact, visv);
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

		HoleCards hc = pokerSimulator.getMyHoleCards();

		// play only preflop list
		if (strategy.equals("PRELIST")) {
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
	 * 
	 * TODO: maybe implement some kind of threshold to alow 1 more action (bluff)
	 * <p>
	 * TODO: the extreme values: fold=-1 and allin=x must be agree whit mathematical poker model to allow bluff.
	 * 
	 * @param sourceName - the name of the source
	 * @param sourceValue - the value
	 */
	private void setPostFlopActions() {
		double ammunitions = getAmmunitions();
		availableActions.clear();
		// no calculation for 0 value
		if (ammunitions == 0) {
			Hero.logger.info("No posible positive EV for ammunitions " + ammunitions);
			return;
		}
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
			double tick = (chips - raise) / 10.0;
			// not 11 because 11 is equal to allin
			for (int c = 1; c < 10; c++) {
				double tickVal = raise + (tick * c);
				// round value to look natural (dont write 12345. write 12340 or 12350)
				if (isInt)
					tickVal = ((int) (tickVal / 10)) * 10;

				String txt = isInt ? "" + (int) tickVal : twoDigitFormat.format(tickVal);
				availableActions.put("raise.text,dc;raise.text,k=" + txt + ";raise", tickVal);
			}
		}
		// check form oportunity
		String txt = pokerSimulator.isOportunity();
		if (txt != null) {
			// at this point pot action must be enabled because the tropper has very hight probabilities. enway check
			// just in case
			if (availableActions.containsKey("raise.pot;raise"))
				availableActions.keySet().removeIf(key -> !key.equals("raise.pot;raise"));
			else
				availableActions.keySet().removeIf(key -> !key.equals("raise"));
			Hero.logger.info("Oportunity detected ----------");
			Hero.logger.info(txt);
			return;
		}

		// action filter *all spetial value allow all action to be consider
		ArrayList<String> ava = pokerSimulator.getAvailableActions();
		if (!ava.contains("*all"))
			availableActions.keySet().removeIf(key -> !ava.contains(key));

		String computationType = pokerSimulator.getOddCalculation();
		if ("ODDS_EV".equals(computationType)) {
			calculateOdds(ammunitions);
			Vector<TEntry<String, Double>> tmp = new Vector<>();
			availableActions.forEach((k, v) -> tmp.add(new TEntry<>(k, v)));
			Collections.sort(tmp, Collections.reverseOrder());
			availableActions.clear();
			tmp.forEach(te -> availableActions.put(te.getKey(), te.getValue()));
		}
		if ("ODDS_MREV".equals(computationType)) {
			calculateRegretMinOdds(ammunitions);
		}

		// 191228: Hero win his first game against TH app !!!!!!!!!!!!!!!! :D
		String val = availableActions.keySet().stream().map(k -> k + "=" + twoDigitFormat.format(asociatedCost.get(k)))
				.collect(Collectors.joining(", "));
		val = val.trim().isEmpty() ? "No positive EV" : val;
		Hero.logger.info(computationType + " " + val);
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
	private void setPrefloopActions() {
		availableActions.clear();
		String prehand = isGoodPreflopHand();
		if (prehand == null) {
			setVariableAndLog(EXPLANATION, "Preflop hand not good.");
			return;
		}

		// test: empirical result set the buy in for preflop to 0.05 of the hero chips. now, from the original 5%, i
		// take a factor dependin of the card dealed. the idea is try to survive the "pot stoler" that generaly
		// start the preflop with hight amounts and after the preflop raise all in to steal the pot. the constant 5% of
		// the chip, leave hero vulnerable to those buffons and the chips start to dropping out whiout control.
		double base = pokerSimulator.getBigBlind() * pokerSimulator.getPreflopBase();
		double ammo = pokerSimulator.getBigBlind() * pokerSimulator.getHandStrengBase();
		double streng = pokerSimulator.getPreFlopHandStreng();
		if (pokerSimulator.getCurrentRound() == PokerSimulator.HOLE_CARDS_DEALT && maxRekonAmmo == -1) {
			maxRekonAmmo = base + (ammo * streng);
		}

		if (currentHandCost >= maxRekonAmmo) {
			setVariableAndLog(EXPLANATION, prehand + " but no more ammunition available.");
			return;
		}

		double call = pokerSimulator.getCallValue();
		double raise = pokerSimulator.getRaiseValue();

		// can i check ??
		if (call == 0) {
			availableActions.put("call", 0.0);
		}
		// can i call ?
		if (call > 0 && (call + currentHandCost) < maxRekonAmmo) {
			availableActions.put("call", call);
		}
		// the raise is mariginal ??
		if (raise != -1 && (raise + currentHandCost) < maxRekonAmmo) {
			availableActions.put("raise", raise);
		}
		// use buy in just as reference
		calculateOdds(pokerSimulator.getBuyIn());
		setVariableAndLog(EXPLANATION, prehand + "Preflop hand not good.");
		String txt1 = prehand + " " + twoDigitFormat.format(base) + " + (" + twoDigitFormat.format(ammo) + " * "
				+ twoDigitFormat.format(streng) + ") = " + twoDigitFormat.format(maxRekonAmmo);
		setVariableAndLog(EXPLANATION, txt1);

	}

	private void setVariableAndLog(String key, Object value) {
		String value1 = value.toString();
		if (value instanceof Double)
			value1 = fourDigitFormat.format(((Double) value).doubleValue());
		pokerSimulator.setVariable(key, value);
		// don.t log the status, only the explanatio
		if (!STATUS.equals(key)) {
			String key1 = key.replace(EXPLANATION, "");
			// 200210: Hero play his first 2 hours with REAL +EV. Convert 10000 chips in 64000
			Hero.logger.info(key1 + value1);
		}
	}
	/**
	 * This metod check all the sensor areas and perform the corrections to get the troper into the fight. The
	 * conbination of enabled/disabled status of the sensor determine the action to perform. If the enviorement request
	 * the trooper to play, this method return <code>true</code>, else this method will try to reach the gametable until
	 * an fix amount of time is reached. In that case, this method return <code>false</code>
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

			// if any of this are active, do nothig. raise.text in this case, is wachit a chackbok for check
			if (sensorsArray.isSensorEnabled("raise.text") || sensorsArray.isSensorEnabled("sensor1")
					|| sensorsArray.isSensorEnabled("sensor2")) {
				continue;
			}

			// the i.m back button is active (at this point, the enviorement must only being showing the i.m back
			// button)
			if (sensorsArray.isSensorEnabled("imBack")) {
				// robotActuator.perform("imBack");
				continue;
			}
		}
		setVariableAndLog(EXPLANATION, "Can.t reach the main gametable. Trooper return.");
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
	/**
	 * Compute the EV for all actions inside of the <code>list</code> parameter. after this method, the list contain
	 * only the actions with +EV. in this case, for less cost, better EV. the {@link #getSubOptimalAction()} method
	 * reverse the order of the list to correctly select the option whit best EV
	 * <p>
	 * to comply with rule 1, this method retrive his probability from {@link PokerSimulator#getBestProbability()
	 * 
	 * <h5>MoP page 54</h5>
	 * 
	 * @param base - ammount of ammo calculated in {@link #getAmmunitions()}
	 * @param list - list of action (call/bet/raise/...) with his asociated cost
	 * 
	 */
	protected void calculateOdds(double base) {
		// Preconditions.checkArgument(base >= 0 && cost >= 0, "Odd function accept only 0 or positive values.");
		double prob = pokerSimulator.getBestProbability();
		if (prob == 0) {
			availableActions.clear();
			setVariableAndLog(EXPLANATION, "no posible +EV with probability = " + prob);
		}
		asociatedCost.clear();
		for (String act : availableActions.keySet()) {
			double cost = availableActions.get(act);
			// MoP page 54
			double ev = (prob * base) - cost;
			availableActions.put(act, ev);
			if (ev > 0)
				asociatedCost.put(act, cost);
		}
		// remove all negative values
		availableActions.values().removeIf(dv -> dv < 0);
	}

	protected void calculateRegretMinOdds(double base) {
		// deprecated method. this method need a rebuild. redirect to normal ev calculation
		calculateOdds(base);
		// double prob = pokerSimulator.getBestProbability();
		// if (prob == 0) {
		// list.clear();
		// setVariableAndLog(EXPLANATION, "no posible +EV with probability = " + prob);
		// }
		//
		// // step (by observation, 1/20 of the bb)
		// // double step = pokerSimulator.getBigBlind() / 20.0;
		// double step = 5.0;
		// // regret
		// double reg = (prob - PokerSimulator.probabilityThreshold) * -1 * step;
		// for (TEntry<String, Double> tEntry : list) {
		// double cost = tEntry.getValue();
		// // 1 calculate normal EV
		// double ev = (prob * base) - cost;
		// // 2 ONLY if normal EV is positive, calcula RMEV.
		// if (ev > 0)
		// ev = (prob * base) - (cost * reg);
		// tEntry.setValue(ev);
		// }
		// // 3 remove all negative values
		// list.removeIf(te -> te.getValue() < 0);
	}
	@Override
	protected Object doInBackground() throws Exception {

		// ensure db connection on the current thread.
		try {
			Alesia.openDB();
			Alesia.openDB("hero");
		} catch (Exception e) {
			// just a warning log because reiterated pause/stop/play can generate error re opening the connection
			Hero.logger.warning(e.getMessage());
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

			// if i can reach the gametable, dismiss the troper
			if (!ingt) {
				return null;
			}

			// at this point i must decide and act
			decide();
			act();
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
		// 191020: ayer ya la implementacion por omision jugo una partida completa y estuvo a punto de vencer a la
		// chatarra de Texas poker - poker holdem. A punto de vencer porque jugaba tan lento que me aburri del sueno :D
	}
}
