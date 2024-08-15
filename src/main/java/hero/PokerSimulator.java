package hero;

import java.text.*;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.*;

import core.*;
import datasource.*;
import hero.UoAHandEval.*;
import hero.rules.*;

/**
 * 
 * Link between hero and PokerProthsis library. This Class perform the
 * simulation and store all result for further use. this is for <I>Me vs
 * Opponents</I> simulator.
 * <p>
 * this its the class that contain all necessary information for decision making
 * and is populated by the class {@link SensorsArray}
 * 
 */
public class PokerSimulator {

	// same values as PokerProphesierAdapter + no_card_deal status
	public static final int NO_CARDS_DEALT = 0;
	public static final int HOLE_CARDS_DEALT = 2;
	public static final int FLOP_CARDS_DEALT = 5;
	public static final int TURN_CARD_DEALT = 6;
	public static final int RIVER_CARD_DEALT = 7;
	public static String STATUS = "aa.Simulator Status";
	public static String STATUS_OK = "Ok";
	public static String STATUS_ERROR = "Error";
	private static NumberFormat percentageFormat = TResources.percentageFormat;
	private static DecimalFormat twoDigitFormat = TResources.twoDigitFormat;
	RuleBook ruleBook;

	/**
	 * temporal storage for the incoming cards (simulator)
	 */
	public final Hashtable<String, String> cardsBuffer;

	/**
	 * enable/disable status for read sensors.
	 */
	public final Hashtable<String, Boolean> sensorStatus;
	private TreeMap<String, Object> variableList;
	/**
	 * ............
	 */
	// TODO: all prevvalue chips related muss be tracked by the correct instance of
	// {@link GameRecorder}
	// TODO: all heromax chips related muss be tracked by the correct instance of
	// {@link GameRecorder}
	public double heroChips;
	public double callValue, raiseValue, potValue;
	public int opponents;
	public int street = NO_CARDS_DEALT;
	public double buyIn, smallBlind, bigBlind;
	public final UoAHand communityCards = new UoAHand();
	public final UoAHand holeCards = new UoAHand();
	public final UoAHand currentHand = new UoAHand();
	public int tablePosition;
	public DescriptiveStatistics performaceStatistic;
	public PokerSimulatorTraker pokerSimulatorTraker;

	public int stimatedVillanTau;

	public final Map<String, Object> evaluation = new Hashtable<>();

	// private long lastStepMillis;
	private Hashtable<Integer, String> streetNames = new Hashtable<>();

	// Global variables updated by getHandPotential method
	// public double Ppot, Npot, HS_n, winProb_n;

	private boolean isLive;

	public PokerSimulator() {
		streetNames.put(NO_CARDS_DEALT, "No cards dealt");
		streetNames.put(HOLE_CARDS_DEALT, "Hole cards dealt");
		streetNames.put(FLOP_CARDS_DEALT, "Flop");
		streetNames.put(TURN_CARD_DEALT, "Turn");
		streetNames.put(RIVER_CARD_DEALT, "River");

		// Retrieve tables parameters direct from database
		TrooperParameter hero = TrooperParameter.getHero();
		this.isLive = true;
		this.buyIn = hero.getDouble("buyIn");
		this.bigBlind = hero.getDouble("bigBlind");
		this.smallBlind = hero.getDouble("smallBlind");

		this.cardsBuffer = new Hashtable<String, String>();
		this.sensorStatus = new Hashtable<>();
		this.variableList = new TreeMap<>();
		this.performaceStatistic = new DescriptiveStatistics(10);
		this.pokerSimulatorTraker = new PokerSimulatorTraker();
		this.ruleBook = new RuleBook();
	}

	void setLive(boolean isLive) {
		this.isLive = isLive;
	}

	/**
	 * Returns the value of the hole cards based on the Chen formula.
	 * 
	 * @param cards The hole cards.
	 * 
	 * @return The score based on the Chen formula. [0,20] range
	 */
	public static double getChenScore(UoAHand hand) {
		if (hand.size() != 2) {
			throw new IllegalArgumentException("Invalid number of cards: " + hand.size());
		}

		// Analyze hole cards.
		int rank1 = hand.getCard(1).getRank();
		int suit1 = hand.getCard(1).getSuit();
		int rank2 = hand.getCard(2).getRank();
		int suit2 = hand.getCard(2).getSuit();
		int highRank = Math.max(rank1, rank2);
		int lowRank = Math.min(rank1, rank2);
		int rankDiff = highRank - lowRank;
		int gap = (rankDiff > 1) ? rankDiff - 1 : 0;
		boolean isPair = (rank1 == rank2);
		boolean isSuited = (suit1 == suit2);

		double score = 0.0;

		// 1. Base score highest rank only
		if (highRank == UoACard.ACE) {
			score = 10.0;
		} else if (highRank == UoACard.KING) {
			score = 8.0;
		} else if (highRank == UoACard.QUEEN) {
			score = 7.0;
		} else if (highRank == UoACard.JACK) {
			score = 6.0;
		} else {
			score = (highRank + 2) / 2.0;
		}

		// 2. If pair, double score, with minimum score of 5.
		if (isPair) {
			score *= 2.0;
			if (score < 5.0) {
				score = 5.0;
			}
		}

		// 3. If suited, add 2 points.
		if (isSuited) {
			score += 2.0;
		}

		// 4. Subtract points for gap.
		if (gap == 1) {
			score -= 1.0;
		} else if (gap == 2) {
			score -= 2.0;
		} else if (gap == 3) {
			score -= 4.0;
		} else if (gap > 3) {
			score -= 5.0;
		}

		// 5. Add 1 point for a 0 or 1 gap and both cards lower than a Queen.
		if (!isPair && gap < 2 && rank1 < UoACard.QUEEN && rank2 < UoACard.QUEEN) {
			score += 1.0;
		}

		// Minimum score is 0.
		if (score < 0.0) {
			score = 0.0;
		}

		// 6. Round half point scores up.
		return Math.round(score);
	}

	/**
	 * return the evaluation computed based on {@link UoAHandEvaluator}
	 * 
	 * @param holeCardsv     - my cards
	 * @param communityCards - table's cards
	 * 
	 * @return the evaluation
	 */
	public static Map<String, Object> getUoAEvaluation(UoAHand holeCards, UoAHand communityCards) {
		UoAHandEvaluator evaluator = new UoAHandEvaluator();
		UoAHand allCards = new UoAHand(holeCards + " " + communityCards);
		int handRank = UoAHandEvaluator.rankHand(allCards);
		int handType = UoAHandEvaluator.getType(allCards);
		Map<String, Object> result = new TreeMap<>();

		// my hand evaluation
		if (holeCards.size() > 0) {
			result.put("rank", handRank);
			result.put("name", UoAHandEvaluator.nameHand(allCards));
			result.put("bestOf5Cards", evaluator.getBest5CardHand(allCards));
		}

		// board evaluation
		if (communityCards.size() > 0) {
			int total = 0;
			ArrayList<UoAHand> aheadList = new ArrayList<>(52 * 52);
			ArrayList<UoAHand> tiedList = new ArrayList<>(52 * 52);
			ArrayList<UoAHand> behindList = new ArrayList<>(52 * 52);
			ArrayList<UoACard> outsTurn = new ArrayList<>(52 * 52);
			ArrayList<UoACard> outsRiver = new ArrayList<>(52 * 52);
			int[][] rowcol = evaluator.getRanks(new UoAHand(communityCards));
			for (int i = 0; i < 52; i++) {
				for (int j = i; j < 52; j++) { // <--- only triangular superior values
					UoAHand hand = new UoAHand((new UoACard(i)).toString() + " " + (new UoACard(j)).toString());
					// don.t include hands where hero hat one or both cards
					if (hand.getCardIndex(1) == holeCards.getCardIndex(1)
							|| hand.getCardIndex(1) == holeCards.getCardIndex(2)
							|| hand.getCardIndex(2) == holeCards.getCardIndex(1)
							|| hand.getCardIndex(2) == holeCards.getCardIndex(2)) {
						continue;
					}
					if (rowcol[i][j] > 0) {
						total++;
						if (handRank > rowcol[i][j])
							aheadList.add(hand);

						if (handRank == rowcol[i][j])
							tiedList.add(hand);

						if (handRank < rowcol[i][j])
							behindList.add(hand);

						// outs.
						UoAHand outsHand = new UoAHand(holeCards + " " + communityCards);

						// turn
						UoACard turn = new UoACard(i);
						outsHand.addCard(turn);
						int turnType = UoAHandEvaluator.getType(outsHand);
						if (turnType > handType && !outsTurn.contains(turn))
							outsTurn.add(turn);

						// river
						UoACard river = new UoACard(j);
						outsHand.addCard(river);
						int riverType = UoAHandEvaluator.getType(outsHand);
						if (riverType > handType && !outsRiver.contains(river))
							outsRiver.add(river);
					}

				}
			}
			result.put("rankTotal", total);

			result.put("isPoketPair", UoAHandEvaluator.isPoketPair(holeCards));
			result.put("isOvercard", UoAHandEvaluator.isOvercard(holeCards, communityCards));
			result.put("isIStraightDraw", UoAHandEvaluator.isInsideStraightDraw(holeCards, communityCards));
			result.put("darkness", UoAHandEvaluator.getDarkness(holeCards, communityCards));


			// result.put("outsTurn", outsTurn);
			// result.put("outsRiver", outsRiver);

			result.put("rankAhead", aheadList.size());
			result.put("rankAhead%", ((double) aheadList.size()) / ((double) total) * 100d);
			result.put("rankAheadList", aheadList);

			result.put("rankTied", tiedList.size());
			result.put("rankTied%", ((double) tiedList.size()) / ((double) total) * 100d);
			result.put("rankTiedList", tiedList);

			result.put("rankBehind", behindList.size());
			result.put("rankBehind%", ((double) behindList.size()) / ((double) total) * 100d);
			result.put("rankBehindList", behindList);

			// outs

		}

		// is the nuts (apply only in post flop): hero can't loose
		result.put("isTheNut", false);
		if (allCards.size() > 2 && (double) result.get("rankBehind%") == 0)
			result.put("isTheNut", true);

		return result;
	}

	/**
	 * Hand Potential algorithm based on "Opponent Modeling in Poker", Darse
	 * Billings, Denis Papp, Jonathan Schaeffer, Duane SzafronPoker. page 7.
	 * <p>
	 * this method compute and return a the <code>PPot</code> and <code>NPot</code>
	 * in array format.
	 * 
	 * note: The hand strength calculation is with respect to one opponent but can
	 * be extrapolated to multiple opponents by raising it to the power of the
	 * number of active opponents.
	 */
	public static Map<String, Object> getHandPotential(UoAHand holeCards, UoAHand communityCards, int opponents,
			int sVillanTau) {
		/**
		 * reweight a list of posibles villans hole cards. the new weight is based on
		 * the normalized EV obtain form preprolpacardsmodel. if a card is outside
		 * range, weight = 0 Hashtable<UoAHand, Double> rWeight = new Hashtable<>();
		 * preflopCardsModel.setPercentage(tau); for (UoAHand hand : behindList) { if
		 * (preflopCardsModel.containsHand(hand)) rWeight.put(hand,
		 * preflopCardsModel.getNormalizedEV(hand)); } result.put("reWeightList",
		 * rWeight);
		 */

		// NOT IMPLEMENTED set the tau value to compute re weight
		// preflopCardsModel.setPercentage(sVillanTau);
		Map<String, Object> result = new TreeMap<>();

		// terry: this method is util with comunity cards. TODO: check whit paper
		if (communityCards.size() == 0) {
			return result;
		}

		UoAHand iBoard = new UoAHand();
		int iterations = 100_000;

		result.put("iterations", iterations);
		double ahead = 0, tied = 0, behind = 0;

		// Hand potential array, each index represents ahead, tied, and behind.
		double HP[][] = new double[3][3];
		int HPTotal[] = new int[3];
		UoAHand villan = new UoAHand(); // two cards for each villans0
		UoAHandEvaluator evaluator = new UoAHandEvaluator();
		int ourrank = evaluator.rankHand(holeCards.getCard(1), holeCards.getCard(2), communityCards);

		// Consider all two card combinations of the remaining cards for the opponent.
		UoADeck deck = new UoADeck();
		int index = 0;
		for (int i = 0; i < iterations; i++) {
			deck.reset();
			deck.shuffle();
			deck.extractHand(holeCards);
			deck.extractHand(communityCards);

			villan.makeEmpty();
			villan.addCard(deck.deal().getIndex());
			villan.addCard(deck.deal().getIndex());
			int opprank = evaluator.rankHand(villan.getCard(1), villan.getCard(2), communityCards);
			if (ourrank > opprank)
				index = 0; // ahead
			else if (ourrank == opprank)
				index = 1;// tied
			else
				index = 2; // behind
			HPTotal[index] += 1;

			/* All possible board cards to come. */
			iBoard.makeEmpty();
			for (int b = 0; b < communityCards.size(); b++)
				iBoard.addCard(communityCards.getCard(b + 1).getIndex());
			// board in in flop state: deal turn
			if (iBoard.size() == 3)
				iBoard.addCard(deck.deal().getIndex());
			// board in in turn state: deal river
			if (iBoard.size() == 4)
				iBoard.addCard(deck.deal().getIndex());
			int myRank = evaluator.rankHand(holeCards.getCard(1), holeCards.getCard(2), iBoard);
			int villanRank = evaluator.rankHand(villan.getCard(1), villan.getCard(2), iBoard);

			// following the opponent modeling paper: 1 for in card selection range , 0.01
			// when not
			// *** test purpose ***
			double weight = 1;
			// double weight = preflopCardsModel.containsHand(villan) ? 1.0 : 0.01;

			// double weight = rWeight.getOrDefault(villan, PokerSimulator.lowerBound);
			if (myRank > villanRank) {
				HP[index][0] += weight;
				ahead += weight;
			} else if (myRank == villanRank) {
				HP[index][1] += weight;
				tied += weight;
			} else {
				HP[index][2] += weight;
				behind += weight;
			}
		}

		// HPTotal[tied]);

		/* Ppot: were behind but moved ahead. */
		// Ppot = (HP[behind][ahead] + HP[behind][tied] / 2 + HP[tied][ahead]/2) /
		// (HPTotal[behind] + HPTotal[tied])
		double Ppot = (HP[2][0] + HP[2][1] / 2d + HP[1][0] / 2d) / (double) (HPTotal[2] + HPTotal[1]);
		if (Double.isNaN(Ppot))
			Ppot = 1.0;

		/* Npot: were ahead but fell behind. */
		// Npot = (HP[ahead][behind] + HP[tied][behind] / 2 + HP[ahead][tied] / 2) /
		// (HPTotal[ahead] +
		// HPTotal[tied])
		double Npot = (HP[0][2] + HP[1][2] / 2d + HP[0][1] / 2d) / (double) (HPTotal[0] + HPTotal[1]);

		// HandStrength: chance that our hand is better than a random hand.
		// double HS_n = Math.pow((ahead + tied / 2d) / (double) (ahead + tied +
		// behind), opponents);
		double HS_n = Math.pow(ahead / (double) (ahead + tied + behind), opponents);
		double HT_n = Math.pow(tied / (double) (ahead + tied + behind), opponents);
		double HB_n = Math.pow(behind / (double) (ahead + tied + behind), opponents);

		result.put("PPot", Ppot);
		result.put("NPot", Npot);
		result.put("RPot", 1 - (Ppot + Npot));
		result.put("HS_n", HS_n);
		result.put("HT_n", HT_n);
		result.put("HB_n", HB_n);

		result.put("winProb", HS_n + HT_n);

		return result;
	}

	/**
	 * return a {@link Map} object filled whit all values obtained from diverse
	 * evaluations algorithms.
	 * 
	 * @param holeCards      - Hole Cards
	 * @param communityCards - Community cards
	 * @param opponents      - number of players ONLY VILLANS
	 * @param bigBlinds      - # of big blinds to compute ammoControl
	 * 
	 * @return properties
	 */
	public static Map<String, Object> getEvaluation(UoAHand holeCards, UoAHand communityCards, int opponents,
			double bigBlinds) {
		long t1 = System.currentTimeMillis();

		Map<String, Object> result = getUoAEvaluation(holeCards, communityCards);
		result.putAll(getHandPotential(holeCards, communityCards, opponents, 0));
		result.put("chenScore", PokerSimulator.getChenScore(holeCards));

		// with x to put an the end :)
		result.put("xExecution time", (System.currentTimeMillis() - t1));
		return result;

	}

	/**
	 * return the string representation of a list of cards
	 * 
	 * @param cards cards
	 * 
	 * @return
	 */
	public static String parseCards(List<UoACard> cards) {
		StringBuffer sb = new StringBuffer();
		cards.forEach(c -> sb.append(c.toString() + " "));
		return sb.toString().trim();
	}

	/**
	 * String representation of a list of {@link UoACard}
	 * 
	 * @param hands - List of Hands
	 * @return formated string
	 */
	public static String parseHands(List<UoAHand> hands) {
		String hs = hands.toString();
		hs = hs.replaceAll("[ ]", "");
		hs = hs.replace(',', ' ');
		return hs.substring(1, hs.length() - 1);
	}

	private static List<UoAHand> reweight(double mean, double variance, Hashtable<UoAHand, Double> hands) {
		ArrayList<UoAHand> dangerHands = new ArrayList<>();
		/* interpolate in the range mean +- variance. */
		double low_wt = 0.01;
		double high_wt = 1.0;
		Set<UoAHand> hands2 = hands.keySet();
		for (UoAHand hand : hands2) {
			double EHS = hands.get(hand);
			// EHS = HS_n + (1 - HS_n) x Ppot
			double reweight = (EHS - mean + variance) / (2 * variance);
			/* Assign low weights below (mean-variance). */
			// if(reweight<low_wt)
			// reweight = low_wt;
			/* Assign high weights above (mean+variance). */
			if (reweight > high_wt)
				dangerHands.add(hand);
			// reweight = high_wt;
			// weight[subcase] = weight[subcase]*reweight
		}
		return dangerHands;
	}

	public void cleanReport() {
		variableList.keySet().forEach(key -> variableList.put(key, ""));
		variableList.put(STATUS, STATUS_OK);
	}

	/**
	 * reset all values and prepare for star a new hand.
	 */
	public void newHand() {
		street = NO_CARDS_DEALT;
		this.opponents = -1;
		holeCards.makeEmpty();
		communityCards.makeEmpty();
		// 190831: ya el sistema se esta moviendo. por lo menos hace fold !!!! :D estoy
		// en el salon de clases del campo
		// de refujiados en dresden !!!! ya van 2 meses
		cardsBuffer.clear();
		sensorStatus.clear();
		pokerSimulatorTraker.newHand();
		potValue = -1;
		tablePosition = -1;
		callValue = -1;
		raiseValue = -1;
		heroChips = -1;
	}

	public String getCurrentHandStrengName() {
		return UoAHandEvaluator.nameHand(currentHand);
	}

	/**
	 * Return the string representation of the parameters of the table
	 * 
	 * @return table parms
	 */
	public String getTableParameters() {
		return buyIn + " " + bigBlind + "," + smallBlind;
	}

	public TreeMap<String, Object> getVariables() {
		// TODO: delete ???
		return variableList;
	}

	// String txt = null;
	// // Hero must check for oportunity
	// if (!takeOpportunity)
	// return txt;
	// // the word oportunity means the event present in flop or turn streat. in
	// river is not a oportunity any more
	// if (currentRound < FLOP_CARDS_DEALT)
	// return txt;
	//
	// // is the nut
	// if (getMyHandHelper().isTheNuts())
	// return "Is the Nuts";
	//
	// // villan.s most probable hands is stronger as hero.s hand
	// // if (oppTopHand > myHandHelper.getHandRank())
	// // return txt;
	//
	// // String sts = getSignificantCards();
	// // // set hand but > pair
	// // if (myHandHelper.getHandRank() > Hand.PAIR && sts.length() == 5) {
	// // String nh = UoAHandEvaluator.nameHand(uoAHand);
	// // txt = "Troper has " + nh + " (set)";
	// // }
	//
	// // String nh = UoAHandEvaluator.nameHand(uoAHand);
	// // txt = "Troper has " + nh;
	//
	// return txt;
	// }

	/**
	 * return <code>true</code> if the sensor argument is enable. false otherwise
	 * 
	 * @param sensor - sensor name
	 * @return <code>true</code> if enabled
	 */
	public boolean isSensorEnabled(String sensor) {
		return sensorStatus.getOrDefault(sensor, false);
	}

	/**
	 * perform the PokerProphesier simulation. Call this method when all the cards
	 * on the table has been set using {@link #addCard(String, String)} this method
	 * will create the {@link HoleCards} and the {@link CommunityCards} (if is
	 * available). After the simulation, the adapters are updated and can be
	 * consulted and the report are up to date
	 * 
	 */
	public void runSimulation() {
		long t1 = System.currentTimeMillis();
		variableList.put(STATUS, "Runing ...");

		// Hero.heroLogger.warning("String " + card + " for card representation
		// incorrect. Card not created");
		UoACard c1 = new UoACard(cardsBuffer.get("hero.card1"));
		UoACard c2 = new UoACard(cardsBuffer.get("hero.card2"));
		holeCards.makeEmpty();
		holeCards.addCard(c1);
		holeCards.addCard(c2);

		communityCards.makeEmpty();
		if (cardsBuffer.containsKey("flop1"))
			communityCards.addCard(new UoACard(cardsBuffer.get("flop1")));
		if (cardsBuffer.containsKey("flop2"))
			communityCards.addCard(new UoACard(cardsBuffer.get("flop2")));
		if (cardsBuffer.containsKey("flop3"))
			communityCards.addCard(new UoACard(cardsBuffer.get("flop3")));
		if (cardsBuffer.containsKey("turn"))
			communityCards.addCard(new UoACard(cardsBuffer.get("turn")));
		if (cardsBuffer.containsKey("river"))
			communityCards.addCard(new UoACard(cardsBuffer.get("river")));

		// String h = cardsBuffer.values().stream().collect(Collectors.joining(" "));
		currentHand.makeEmpty();
		cardsBuffer.forEach((key, val) -> currentHand.addCard(new UoACard(val)));
		street = currentHand.size();

		evaluation.clear();
		evaluation.putAll(getEvaluation(holeCards, communityCards, opponents, heroChips / bigBlind));
		ruleBook.updateFacts(this);
		pokerSimulatorTraker.update(this);

		// WARNING: theses values ARE NOT available in preflop
		double Ppot = (double) evaluation.getOrDefault("PPot", 0.0);
		// Npot = (double) uoAEvaluation.getOrDefault("NPot", 0.0);
		double winProb_n = (double) evaluation.getOrDefault("winProb", 0.0);
		// HS_n = (double) uoAEvaluation.getOrDefault("HS_n", 0.0);
		double rankA = (double) evaluation.getOrDefault("rankAhead%", 0.0);
		double rankB = (double) evaluation.getOrDefault("rankBehind%", 0.0);

		// update the simulation result to the console
		String text = "rankAhead " + twoDigitFormat.format(rankA) + " rankBehind " + twoDigitFormat.format(rankB);
		variableList.put("simulator.Hand Ranks", text);

		text = "winProb " + percentageFormat.format(winProb_n) + " Ppot " + percentageFormat.format(Ppot) + " "
				+ evaluation.get("name");
		variableList.put("simulator.Evaluation", text);

		text = "Hole " + holeCards.toString() + " Comunity " + communityCards + " Current " + currentHand.toString();
		variableList.put("simulator.Table cards", text);

		text = "Chips " + heroChips + " Pot " + potValue + " Call " + callValue + " Raise " + raiseValue;
		variableList.put("simulator.Table values", text);

		text = "Round " + streetNames.get(street) + " Opponents " + opponents + " Position " + tablePosition;
		variableList.put("simulator.Simulator values", text);

		if (isLive) {
			Hero.heroLogger.info("Table values: " + variableList.get("simulator.Table values"));
			Hero.heroLogger.info("Troper probability: " + variableList.get("simulator.Troper probability"));
			Hero.heroLogger.info("Trooper Current hand: " + variableList.get("simulator.Trooper Current hand"));
			Hero.heroLogger.info("Simulator values: " + variableList.get("simulator.Simulator values"));
		}

		variableList.put(STATUS, STATUS_OK);
		performaceStatistic.addValue(System.currentTimeMillis() - t1);
	}

	public void setCallValue(double callValue) {
		this.callValue = callValue;
	}

	public void setHeroChips(double heroChips) {
		this.heroChips = heroChips;
	}

	public void setNunOfOpponets(int opp) {
		this.opponents = opp;
	}

	public void setPotValue(double potValue) {
		this.potValue = potValue;
	}

	public void setRaiseValue(double raiseValue) {
		this.raiseValue = raiseValue;
	}

	/**
	 * Update the table position. the Heros table position is determinated detecting
	 * the dealer button and counting clockwise. For examples, in a 4 villans table:
	 * <li>If hero has the dealer button, this method return 5;
	 * <li>if villan4 is the dealer, this method return 1. Hero is small blind
	 * <li>if villan1 is the dealer, this method return 4. Hero is in middle table
	 * position.
	 * <p>
	 * this metod is called during the {@link SensorsArray#read(String)} operation.
	 */
	public void setTablePosition(int dealerPos, int villans) {
		// int tp = Math.abs(dbp - (getActiveSeats() + 1));
		this.tablePosition = Math.abs(dealerPos - (villans + 1));
	}

	public void setVariable(String key, Object value) {
		variableList.put(key, value);
	}

}