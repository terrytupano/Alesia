package hero;

import java.text.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.*;
import org.apache.commons.math3.stat.descriptive.*;

import com.jgoodies.common.base.*;

import core.*;
import datasource.*;
import hero.UoAHandEval.*;
import hero.ozsoft.*;

public class PokerSimulator {

	// standar prefloo range 42% until poket 22
	// 42% is ultil 22 poket pair
	// 45% is to make every step 5%
	// 36% is to make every step 4%
	// 26 is the max % for preflopCardsModel where all cards hat 0 <= EV
	public static final int PREFLOP_RANGE = 45;
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
	private static PreflopCardsModel preflopCardsModel = new PreflopCardsModel();
	private int tablePosition;
	private TreeMap<String, Object> variableList;

	/** temporal storage for the incoming cards (simulator) */
	public final Hashtable<String, String> cardsBuffer;

	/** enable/disable status for read sensors. */
	public final Hashtable<String, Boolean> sensorStatus;

	public double callValue, raiseValue, potValue;
	public double buyIn, smallBlind, bigBlind;
	public double heroChips;
	public double winProb, SPRs;
	public int activeVillans;
	public int street = NO_CARDS_DEALT;
	public final UoAHand communityCards = new UoAHand();
	public final UoAHand holeCards = new UoAHand();
	public final UoAHand currentHand = new UoAHand();
	public DescriptiveStatistics performaceStatistic;
	public RuleBook ruleBook;
	public TrooperParameter trooperParameter;
	public boolean isLive;
	public BettingSequence bettingSequence;

	public final Map<String, Object> evaluation = new Hashtable<>();

	public static final Hashtable<Integer, String> streetNames = new Hashtable<>();

	/** the number of step to divide the raise values */
	public static int STEPS = 6;

	public PokerSimulator(TrooperParameter trooperParameter) {
		this.trooperParameter = trooperParameter;
		streetNames.put(NO_CARDS_DEALT, "No cards dealt");
		streetNames.put(HOLE_CARDS_DEALT, "Hole cards");
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
		this.ruleBook = new RuleBook(this);
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
	 * @param holeCards      - my cards
	 * @param communityCards - table's cards
	 * 
	 * @return the evaluation
	 */
	public static Map<String, Object> getUoAEvaluation(UoAHand holeCards, UoAHand communityCards) {
		UoAHandEvaluator evaluator = new UoAHandEvaluator();
		UoAHand allCards = new UoAHand(holeCards + " " + communityCards);
		int handRank = UoAHandEvaluator.rankHand(allCards);
		Map<String, Object> result = new TreeMap<>();

		// my hand evaluation
		if (holeCards.size() > 0) {
			result.put("rank", handRank);
			result.put("handName", UoAHandEvaluator.nameHand(allCards));
			result.put("cardsDealed", holeCards + " " + communityCards);
			// result.put("bestOf5Cards", evaluator.getBest5CardHand(allCards).toString().trim());
		}

		// board evaluation
		if (communityCards.size() > 0) {
			int total = 0;
			ArrayList<UoAHand> aheadList = new ArrayList<>(52 * 52);
			ArrayList<UoAHand> tiedList = new ArrayList<>(52 * 52);
			ArrayList<UoAHand> behindList = new ArrayList<>(52 * 52);
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
					}
				}
			}
			result.put("rankTotal", total);

			double ahead = ((double) aheadList.size()) / ((double) total);
			result.put("rankAhead", aheadList.size());
			result.put("rankAhead%", ahead * 100d);
			result.put("rankAheadList", aheadList);

			double tied = ((double) tiedList.size()) / ((double) total);
			result.put("rankTied", tiedList.size());
			result.put("rankTied%", tied * 100d);
			result.put("rankTiedList", tiedList);

			result.put("rankBehind", behindList.size());
			result.put("rankBehind%", ((double) behindList.size()) / ((double) total) * 100d);
			result.put("rankBehindList", behindList);

		}

		// is the nuts (apply only in post flop): hero can't loose
		result.put("isTheNut", false);
		if (allCards.size() > 2 && (double) result.get("rankBehind%") == 0)
			result.put("isTheNut", true);

		return result;
	}

	/**
	 * return the outs & related information
	 * 
	 * @param holeCards      - my cards
	 * @param communityCards - table's cards
	 * 
	 * @return the outs
	 */
	public static Map<String, Object> getOuts(UoAHand holeCards, UoAHand communityCards) {
		Map<String, Object> result = new TreeMap<>();

		UoAHand allCards = new UoAHand(holeCards + " " + communityCards);
		int outs = 0;
		String outsExplanation = "";

		// there is no more outs on the river
		if (communityCards.size() == 3 || communityCards.size() == 4) {

			result.put("isPoketPair", UoAHandEvaluator.isPoketPair(holeCards));
			if (UoAHandEvaluator.isPoketPair(holeCards)
					&& UoAHandEvaluator.getType(allCards) == UoAHandEvaluator.PAIR) {
				outs += 2;
				outsExplanation += "Pocket pair to set, ";
			}

			boolean isOvercard = UoAHandEvaluator.isOvercard(holeCards, communityCards);
			result.put("isOvercard", isOvercard);
			if (isOvercard && UoAHandEvaluator.getType(allCards) == UoAHandEvaluator.HIGH) {
				outs += 3;
				outsExplanation += "One overcard, ";
			}

			int inStraightDraw = UoAHandEvaluator.isInStraightDraw(holeCards, communityCards);
			result.put("isInStraightDraw", inStraightDraw);
			if (inStraightDraw > 0) {
				outs += 4;
				outsExplanation += "Inside straight draw, ";
			}

			if (UoAHandEvaluator.getType(allCards) == UoAHandEvaluator.TWOPAIR
					&& UoAHandEvaluator.getDarkness(holeCards, communityCards) == 2) {
				outs += 4;
				outsExplanation += "2 pairs to full house, ";
			}

			if (UoAHandEvaluator.getType(allCards) == UoAHandEvaluator.PAIR
					&& UoAHandEvaluator.getDarkness(holeCards, communityCards) == 1
					&& !UoAHandEvaluator.isPoketPair(holeCards)) {
				outs += 5;
				outsExplanation += "1 pair to 2 pairs or trip, ";
			}
			// No pair to pair
			// if (UoAHandEvaluator.getType(allCards) == UoAHandEvaluator.HIGH) {
			// outs += 6;
			// outsExplanation += "No pair to pair, ";
			// }

			boolean is2Overcards = UoAHandEvaluator.is2Overcards(holeCards, communityCards);
			result.put("is2Overcards", is2Overcards);
			if (is2Overcards && UoAHandEvaluator.getType(allCards) == UoAHandEvaluator.HIGH) {
				outs += 6;
				outsExplanation += "2 overcard to overpair, ";
			}

			if (UoAHandEvaluator.getType(allCards) == UoAHandEvaluator.THREEKIND
					&& UoAHandEvaluator.isPoketPair(holeCards)) {
				outs += 7;
				outsExplanation += "set to full house / four of a kind, ";
			}

			int oeStraightDraw = UoAHandEvaluator.isOEStraightDraw(holeCards, communityCards);
			result.put("isOEStraightDraw", oeStraightDraw);
			if (oeStraightDraw > 0) {
				outs += 8;
				outsExplanation += "Open ended straight draw, ";
			}

			int flushDraw = UoAHandEvaluator.isFlushDraw(holeCards, communityCards);
			result.put("isFlushDraw", flushDraw);
			if (flushDraw > 0) {
				outs += 9;
				outsExplanation += "Flush draw, ";
			}
			// inside straight draw and 2 overcards
			// inside straight and flush draw
			// open ended straight and flush draw

			result.put("handType", UoAHandEvaluator.getType(allCards));
			result.put("darknessHand", UoAHandEvaluator.getDarkness(holeCards, communityCards));
			// for multiples draws, take the maximun fo them
			result.put("darknessDraw", Collections.max(Arrays.asList(inStraightDraw, oeStraightDraw, flushDraw)));

		}

		result.put("outs", outs);
		result.put("outs2", outs * (2.13 / 100.0)); // Essential Poker Math p143
		result.put("outs4", outs * (4.3 / 100.0)); // Essential Poker Math p143
		result.put("outsExplanation", StringUtils.substringBeforeLast(outsExplanation, ", "));
		return result;
	}

	/**
	 * Hand Potential algorithm based on "Opponent Modeling in Poker", Darse Billings, Denis Papp, Jonathan Schaeffer,
	 * Duane SzafronPoker. page 7.
	 * <p>
	 * this method compute and return a the <code>PPot</code> and <code>NPot</code> in array format.
	 * 
	 * note: The hand strength calculation is with respect to one opponent but can be extrapolated to multiple opponents
	 * by raising it to the power of the number of active opponents.
	 */
	public static Map<String, Object> getHandPotential(UoAHand holeCards, UoAHand communityCards, int totalPlayers) {
		Map<String, Object> result = new TreeMap<>();

		// this method is valid only with comunity cards
		if (communityCards.size() == 0) {
			return result;
		}

		UoAHand iBoard = new UoAHand();
		int iterations = 10_000;

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
		int opponents = totalPlayers - 1;
		double HS_n = Math.pow(ahead / (double) (ahead + tied + behind), opponents);
		double HT_n = Math.pow(tied / (double) (ahead + tied + behind), opponents);
		double HB_n = Math.pow(behind / (double) (ahead + tied + behind), opponents);

		// result.put("PPot", Ppot);
		// result.put("NPot", Npot);
		// result.put("RPot", 1 - (Ppot + Npot));
		// result.put("HS_n", HS_n);
		// result.put("HT_n", HT_n);
		// result.put("HB_n", HB_n);

		result.put("winProb", HS_n + HT_n);

		return result;
	}

	/**
	 * Evaluation for simulation purposes. this method simulate 2 players
	 * 
	 * @param holeCards      - the hole cards
	 * @param communityCards - the comunity cards
	 * @return the evaluation
	 */
	public static Map<String, Object> getEvaluation(UoAHand holeCards, UoAHand communityCards) {
		return getEvaluation(holeCards, communityCards, 2, PREFLOP_RANGE);
	}

	/**
	 * return a {@link Map} object filled whit all values obtained from diverse evaluations algorithms.
	 * 
	 * @param holeCards      - Hole Cards
	 * @param communityCards - Community cards
	 * @param totalPlayers   - number of players HERO INCLUDED
	 * @param preflopRange   - % to set on PreflopCardsModel
	 * 
	 * @return properties
	 */
	private static Map<String, Object> getEvaluation(UoAHand holeCards, UoAHand communityCards, int totalPlayers,
			int preflopRange) {
		long t1 = System.currentTimeMillis();
		Map<String, Object> result = getUoAEvaluation(holeCards, communityCards);
		result.putAll(getOuts(holeCards, communityCards));
		result.putAll(getHandPotential(holeCards, communityCards, totalPlayers));

		result.put("chenScore", PokerSimulator.getChenScore(holeCards));

		/**
		 * of the rankBehindList property, check how many of those hands are inside of the preflopRange, if one hand
		 * pass the preflop distribution, count the group (not the outs) zB. if is there a set draw, mark as 1
		 * handTexture is simple the sum of the posibles outs 0=dry 1=wet 2=more wet 3=max wet
		 */
		if (communityCards.size() > 0) {
			int inside = 0;
			if (preflopRange > 0) {
				int setDraw = UoAHandEvaluator.isPoketPair(communityCards) ? 3 : 0;
				int inStraightDraw = UoAHandEvaluator.isInStraightDraw(communityCards) ? 4 : 0;
				int oEStraightDraw = UoAHandEvaluator.isOEStraightDraw(communityCards) ? 8 : 0;
				int flushDraw = UoAHandEvaluator.countSuits(communityCards) > 2 ? 9 : 0;

				result.put("handTexture", setDraw + inStraightDraw + oEStraightDraw + flushDraw);
				result.put("handInside", inside);
			}
		}

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
		this.activeVillans = -1;
		holeCards.makeEmpty();
		communityCards.makeEmpty();
		// 190831: ya el sistema se esta moviendo. por lo menos hace fold !!!! :D estoy
		// en el salon de clases del campo
		// de refujiados en dresden !!!! ya van 2 meses
		cardsBuffer.clear();
		sensorStatus.clear();
		potValue = -1;
		tablePosition = -1;
		callValue = -1;
		raiseValue = -1;
		heroChips = -1;
		winProb = -1;
		SPRs = -1;
		ruleBook.newHand();
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
	 * perform the PokerProphesier simulation. Call this method when all the cards on the table has been set using
	 * {@link #addCard(String, String)} this method will create the {@link HoleCards} and the {@link CommunityCards} (if
	 * is available). After the simulation, the adapters are updated and can be consulted and the report are up to date
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
		evaluation.putAll(getEvaluation(holeCards, communityCards, activeVillans + 1, PREFLOP_RANGE));

		// Stack-to-Pot Ratios (SPRs)
		// this value is only calculate on preflop and flop. ref: https://www.splitsuit.com/how-to-use-spr-poker-video
		if (street <= FLOP_CARDS_DEALT)
			SPRs = bettingSequence.getEfectiveStackSize() / potValue;

		winProb = (double) evaluation.getOrDefault("winProb", 0.0);
		ruleBook.fire();

		// WARNING: theses values ARE NOT available in preflop
		double Ppot = (double) evaluation.getOrDefault("PPot", 0.0);
		double rankA = (double) evaluation.getOrDefault("rankAhead%", 0.0);
		double rankB = (double) evaluation.getOrDefault("rankBehind%", 0.0);

		// update the simulation result to the console
		String text = "rankAhead " + twoDigitFormat.format(rankA) + " rankBehind " + twoDigitFormat.format(rankB);
		variableList.put("simulator.Hand Ranks", text);

		text = "winProb " + percentageFormat.format(winProb) + " Ppot " + percentageFormat.format(Ppot) + " "
				+ evaluation.get("name");
		variableList.put("simulator.Evaluation", text);

		text = "Hole " + holeCards.toString() + " Comunity " + communityCards + " Current " + currentHand.toString();
		variableList.put("simulator.Table cards", text);

		text = "Chips " + heroChips + " Pot " + potValue + " Call " + callValue + " Raise " + raiseValue;
		variableList.put("simulator.Table values", text);

		text = "Round " + streetNames.get(street) + " Opponents " + activeVillans + " Position " + tablePosition;
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

	/**
	 * set the {@link #tablePosition} and {@link #activeVillans} internal parameters. all incomming parameters are 1
	 * based
	 * 
	 * @param dealerChair   - the dealler position.
	 * @param myChair       - the hero chair (can vary in simulation)
	 * @param activeVillans - the current active villans
	 */
	public void setTablePosition(int dealerChair, int myChair, int activeVillans) {
		Preconditions.checkArgument(dealerChair > 0, "dealerPosition argument muss be > 0");
		Preconditions.checkArgument(myChair > 0, "myChair argument muss be > 0");

		this.activeVillans = activeVillans;
		// int tp = Math.abs(dbp - (getActiveSeats() + 1));
		this.tablePosition = myChair - dealerChair;

		if (dealerChair == myChair)
			this.tablePosition = PokerTable.MAX_CAPACITY;

		if (tablePosition < 0)
			this.tablePosition = tablePosition + PokerTable.MAX_CAPACITY;
		// this.tablePosition = Math.abs(dealerPos - (villans + 1));
	}

	public int getTablePosition() {
		return tablePosition;
	}

	public void setVariable(String key, Object value) {
		variableList.put(key, value);
	}

	/**
	 * return the raise steps list. All the steps values are from > value < to. this method assume that the "from" value
	 * is = raise and "to" value are all in. the # of returned elements inside the list is determined by the
	 * {@value STEPS}
	 * 
	 * @param from - raise value
	 * @param to   - all in value
	 * 
	 * @return the list
	 */
	public static List<Double> getRaiseSteps(double from, double to) {
		double amount = from;
		double inc = (to - from) / (STEPS + 1);
		List<Double> doubles = new ArrayList<>();
		for (int i = 0; i < STEPS; i++) {
			amount += inc;
			doubles.add(amount);
		}
		return doubles;
	}

	/**
	 * this method fill and return a List of {@link TrooperAction} that are available for Hero to select. the returned
	 * list look similar to the following
	 * <li>Check/Call
	 * <li>Raise
	 * <li>Pot
	 * <li>All-in
	 * <li>{@link #STEPS} more actions that range from raise to the value close to All-in.
	 * <p>
	 * for a total of 10 possible actions. this method will remove all actions if equity != null && potodds < equity
	 * 
	 * @param pokerSimulator - the simulator instace to read all the info
	 * @param equity         - the equity to consider if the action remains on the returned list. null for no delete
	 * @return the list
	 */
	public static List<TrooperAction> loadActions(PokerSimulator pokerSimulator, Double equity) {
		List<TrooperAction> availableActions = new ArrayList<>();

		double call = pokerSimulator.callValue;
		double raise = pokerSimulator.raiseValue;
		double chips = pokerSimulator.heroChips;
		double pot = pokerSimulator.potValue;

		if (call >= 0 && call <= chips)
			availableActions.add(new TrooperAction("call", call));

		if (raise >= 0 && raise <= chips)
			availableActions.add(new TrooperAction("raise", raise));

		if (pot >= 0 && pot <= chips && pokerSimulator.isSensorEnabled("raise.pot"))
			availableActions.add(new TrooperAction("pot", pot));

		if (chips >= 0 && chips <= chips && pokerSimulator.isSensorEnabled("raise.allin"))
			availableActions.add(new TrooperAction("allIn", chips));

		if (raise > 0 && pot <= chips && pokerSimulator.isSensorEnabled("raise.slider")) {
			// add standar bettings values
			double bbb = 3 * pokerSimulator.bigBlind;
			availableActions.add(new TrooperAction(TrooperAction.RAISE, bbb));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 0.25 * pot));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 0.3 * pot));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 0.5 * pot));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 0.66 * pot));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 0.75 * pot));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 1.5 * pot));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 2 * pot));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 2.5 * pot));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 3 * pot));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 3.5 * pot));
			availableActions.add(new TrooperAction(TrooperAction.RAISE, 4 * pot));

			// remove actions out of range.
			// in sumulation: if call = 0 (check) and raise != 0, i made the bet, the action is on me again and i habe
			// the chance of check or reraise from the minimun ammount stored in raise variable, not less.
			double lowB = call == 0 ? raise : call;
			availableActions.removeIf(a -> a.amount >= chips || a.amount < lowB);
		}

		// compute reward:risk ratio
		availableActions.forEach(a -> a.potOdds = rewardRiskToProb(pot, a.amount));

		// remove all actions when potOdds < equity
		// 240823: dont remove. the rules are responsible for that. some rules cann
		// decide zB to bluff and all actions are needed
		if (equity != null) {
			availableActions.removeIf(a -> a.potOdds > equity);
		}

		Collections.sort(availableActions, (a1, a2) -> Double.compare(a1.amount, a2.amount));

		// 191228: Hero win his first game against TH app !!!!!!!!!!!!!!!! :D
		return availableActions;
	}

	/**
	 * compute from reward:risk notation to probability. prob = risk / (reward + risk)
	 * 
	 * @param reward - the reward
	 * @param risk   - the risk
	 * @return - the probability
	 */
	public static double rewardRiskToProb(double reward, double risk) {
		double odds = risk / (reward + risk);
		return odds;
	}

}