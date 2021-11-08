package plugins.hero;

import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.swing.*;

import core.*;
import gui.*;
import plugins.hero.UoAHandEval.*;

/**
 * 
 * Link betwen hero and PokerProthsis library. This Class perform th simulation and store all result for futher use.
 * this is for <I>Me vs Opponents</I> simulator.
 * <p>
 * this its the class that contain all nesesary information for desicion making and is populated bby the class
 * {@link SensorsArray}
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
	private static NumberFormat percentageFormat = NumberFormat.getPercentInstance();
	private static DecimalFormat twoDigitFormat = new DecimalFormat("#0.00");

	/**
	 * temporal storage for the incoming cards (simulator)
	 */
	public final Hashtable<String, String> cardsBuffer;
	/**
	 * enable/disable status for readed sensors.
	 */
	public final Hashtable<String, Boolean> sensorStatus;

	private TreeMap<String, Object> variableList;
	private JLabel reportJLabel;
	private TUIPanel reportPanel;

	/**
	 * ............
	 */
	// TODO: all prevvalue chips related muss be tracked by the correct instance of {@link GameRecorder}
	// TODO: all heromax chips related muss be tracked by the correct instance of {@link GameRecorder}
	public double heroChips, heroChipsMax;
	public double callValue, raiseValue, potValue, prevPotValue;
	public int numSimPlayers;
	public int currentRound = NO_CARDS_DEALT;
	public double buyIn, smallBlind, bigBlind;
	public final UoAHand communityCards = new UoAHand();
	public final UoAHand holeCards = new UoAHand();
	public final UoAHand currentHand = new UoAHand();
	public int tablePosition;
	public final Properties uoAEvaluation = new Properties();

	private ActionsBarChart actionsBarChart;
	// private long lastStepMillis;
	private Hashtable<Integer, String> streetNames = new Hashtable<>();

	// gloval variables updated by getHandPotential method
	public double Ppot, Npot, HS_n, winProb_n;

	public PokerSimulator() {
		streetNames.put(NO_CARDS_DEALT, "No cards dealt");
		streetNames.put(HOLE_CARDS_DEALT, "Hole cards dealt");
		streetNames.put(FLOP_CARDS_DEALT, "Flop");
		streetNames.put(TURN_CARD_DEALT, "Turn");
		streetNames.put(RIVER_CARD_DEALT, "River");

		Map<String, Object> values = Hero.trooperPanel.getValues();
		this.buyIn = ((Double) values.get("table.buyIn")).intValue();
		this.bigBlind = ((Double) values.get("table.bigBlid")).intValue();
		this.smallBlind = ((Double) values.get("table.smallBlid")).intValue();

		this.heroChipsMax = -1;
		this.prevPotValue = -1;
		this.cardsBuffer = new Hashtable<String, String>();
		this.sensorStatus = new Hashtable<>();
		variableList = new TreeMap<>();

		this.reportPanel = new TUIPanel();
		reportPanel.showAditionalInformation(false);
		this.reportJLabel = new JLabel();
		reportJLabel.setVerticalAlignment(JLabel.TOP);
		reportJLabel.setFont(new Font("courier new", Font.PLAIN, 12));
		this.actionsBarChart = new ActionsBarChart();
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(reportJLabel, BorderLayout.CENTER);
		jp.add(actionsBarChart.getChartPanel(), BorderLayout.SOUTH);
		reportPanel.setBodyComponent(jp);

		// clearEnviorement();
	}

	/**
	 * Returns the value of the hole cards based on the Chen formula.
	 * 
	 * @param cards The hole cards.
	 * 
	 * @return The score based on the Chen formula.
	 */
	public static double getChenScore(UoAHand hand) {
		if (hand.size() != 2) {
			throw new IllegalArgumentException("Invalid number of cards: " + hand.size());
		}

		// Analyze hole cards.
		int rank1 = hand.getCard(1).getRank();
		int suit1 = hand.getCard(2).getSuit();
		int rank2 = hand.getCard(1).getRank();
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
	 * return a {@link Properties} object fill whit all data obtains using methods in {@link UoAHandEvaluator}.
	 * 
	 * 
	 * FIXME: This method dont remove the card in my hole cards, despite these fact, the probability converge to the
	 * real probability. any futher use of ahead, 0 or tied list, muss be filtered.
	 * 
	 * @param holeCards - Hole Cards
	 * @param communityCards - Comunity cards
	 * @param opponents - number of opponents
	 * @return properties
	 */
	public static Properties getUoAEvaluation(UoAHand holeCards, UoAHand communityCards) {
		UoAHandEvaluator evaluator = new UoAHandEvaluator();
		UoAHand allCards = new UoAHand(holeCards + " " + communityCards);
		int handRank = UoAHandEvaluator.rankHand(allCards);
		Properties prp = new Properties();

		// my hand evaluation
		if (holeCards.size() > 0) {
			prp.put("rank", handRank);
			prp.put("name", UoAHandEvaluator.nameHand(allCards));
			prp.put("bestOf5Cards", evaluator.getBest5CardHand(allCards));
		}

		// board evaluation
		if (communityCards.size() > 0) {
			int total = 0;
			ArrayList<UoAHand> ahead = new ArrayList<>();
			ArrayList<UoAHand> tied = new ArrayList<>();
			ArrayList<UoAHand> behind = new ArrayList<>();
			int[][] rowcol = evaluator.getRanks(new UoAHand(communityCards));
			for (int i = 0; i < 52; i++) {
				for (int j = 0; j < 52; j++) {
					UoAHand hand = new UoAHand((new UoACard(i)).toString() + " " + (new UoACard(j)).toString());
					// FIXME: to retrive the real prob and elements, muss only take into account the upper diagonal of
					// the matrix. locate rowcol[i][j] > 0 and check only riht elements of the same row
					if (rowcol[i][j] > 0) {
						total++;
						if (handRank > rowcol[i][j])
							ahead.add(hand);

						if (handRank == rowcol[i][j])
							tied.add(hand);

						if (handRank < rowcol[i][j])
							behind.add(hand);
					}
				}
			}
			prp.put("HStotal", total);

			prp.put("HSAhead", ahead.size());
			prp.put("HSAhead%", ((int) ((ahead.size() / (total * 1.0)) * 10000)) / 100.0);
			prp.put("HSAheadList", ahead);

			prp.put("HSTied", tied.size());
			prp.put("HSTied%", ((int) ((tied.size() / (total * 1.0)) * 10000)) / 100.0);
			prp.put("HSTiedList", tied);

			prp.put("HSBehind", behind.size());
			prp.put("HSBehind%", ((int) ((behind.size() / (total * 1.0)) * 10000)) / 100.0);
			prp.put("HSBehindList", behind);
		}

		// invoque getHandPotential and add gobal variable to the list
		Properties psp2 = PokerSimulator.getHandPotential(holeCards, communityCards, 1);
		psp2.forEach((key, val) -> prp.put(key, val));

		// aditional computation tested hand: Hole cards: Ac Ad Comunity cards: Qs 9d As

		// is the nuts: hero can loose
		prp.put("isTheNut", false);
		if (allCards.size() > 2 && (double) prp.get("HSBehind%") == 0)
			prp.put("isTheNut", true);

		// TODO: getSignificantCard()
		// upperbound opponent hand probability: this value refleck the fack that the vas mayority of case, the
		// computation dont need to compute using the full range of card. e.g it is improbable that an some point, a
		// villa hat a royal flush or a streich flush and the villan is allin and hero hat a flush. in order to avoid a
		// hight provability of fold, the computation take into accound that a villas hand with ranck > this upperbound
		// is not take into acount

		return prp;
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

	// /**
	// * check whether is an oportunity. An oportunity is present when the current street is {@link #FLOP_CARDS_DEALT}
	// or
	// * futher and the following conditions is found:
	// * <li>Heros hand muss be >= to the hand setted in {@link #oppMostProbHand} gobal variable
	// * <li>The hand is a set (both card in heros.s hands participate in the action)
	// * <li>OR the hand is the nut
	// *
	// * @return a text explain what oportunity is detected or <code>null</code> if no oportunity are present
	// */
	// public String isOportunity() {

	// TODO: reevaluate this method. compute the bluf probability. especialy istheNut()

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

	/**
	 * Hand Potential algorithm based on "Opponent Modeling in Poker", Darse Billings, Denis Papp, Jonathan Schaeffer,
	 * Duane SzafronPoker. page 7.
	 * <p>
	 * this method compute and return a the <code>PPot</code> and <code>NPot</code> in array format.
	 * 
	 * note: The hand strength calculation is with respect to one opponent but can be extrapolated to multiple opponents
	 * by raising it to the power of the number of active opponents.
	 * 
	 * @param ourcards - my current hole cards
	 * @param boardcards - the current flop. this method will throw an exception if the current street is not flop
	 * 
	 * @return new double[]{Ppot, Npot, winProb};
	 */
	private static Properties getHandPotential(UoAHand ourcards, UoAHand boardcards, int opponents) {
		UoAHand iBoard = new UoAHand();
		int iterations = 100000;
		int ahead = 0, tied = 0, behind = 0;

		// TODO: this method muss count most probable villans hand and top villan hands ?????????? or better work
		// arround whit oponent modelint implementation

		// Hand potential array, each index represents ahead, tied, and behind.
		int HP[][] = new int[3][3];
		int HPTotal[] = new int[3];
		UoAHand villan = new UoAHand(); // two cards for each villans
		UoAHandEvaluator evaluator = new UoAHandEvaluator();
		int ourrank = evaluator.rankHand(ourcards.getCard(1), ourcards.getCard(2), boardcards);

		// Consider all two card combinations of the remaining cards for the opponent.
		UoADeck deck = new UoADeck();
		int index = 0;
		for (int i = 0; i < iterations; i++) {
			deck.reset();
			deck.shuffle();
			deck.extractHand(ourcards);
			deck.extractHand(boardcards);

			villan.makeEmpty();
			villan.addCard(deck.deal().getIndex());
			villan.addCard(deck.deal().getIndex());
			int opprank = evaluator.rankHand(villan.getCard(1), villan.getCard(2), boardcards);
			if (ourrank > opprank)
				index = 0; // ahead
			else if (ourrank == opprank)
				index = 1;// tied
			else
				index = 2; // behind
			HPTotal[index] += 1;

			/* All possible board cards to come. */
			iBoard.makeEmpty();
			for (int b = 0; b < boardcards.size(); b++)
				iBoard.addCard(boardcards.getCard(b + 1).getIndex());
			// board in in flop state: deal turn
			if (boardcards.size() == 3)
				iBoard.addCard(deck.deal().getIndex());
			// board in in turn state: deal river
			if (boardcards.size() == 4)
				iBoard.addCard(deck.deal().getIndex());
			int myRank = evaluator.rankHand(ourcards.getCard(1), ourcards.getCard(2), iBoard);
			int villanRank = evaluator.rankHand(villan.getCard(1), villan.getCard(2), iBoard);
			if (myRank > villanRank) {
				HP[index][0] += 1;
				ahead++;
			} else if (myRank == villanRank) {
				HP[index][1] += 1;
				tied++;
			} else {
				HP[index][2] += 1;
				behind++;
			}

		}
		/* Ppot: were behind but moved ahead. */
		// double Ppot = (HP[behind][ahead] + HP[behind][tied] / 2 + HP[tied][ahead] / 2) / (HPTotal[behind] +
		// HPTotal[tied]);
		double Ppot = (HP[2][0] + HP[2][1] / 2d + HP[1][0] / 2d) / (double) (HPTotal[2] + HPTotal[1]);

		/* Npot: were ahead but fell behind. */
		double Npot = (HP[0][2] + HP[1][2] / 2d + HP[0][1] / 2d) / (double) (HPTotal[0] + HPTotal[1]);

		// HandStrength: chance that our hand is better than a random hand.
		double HS_n = Math.pow((ahead + tied / 2d) / (double) (ahead + tied + behind), opponents);

		// winning probabilities ?!?!?!?!?!?
		double winProb_n = Math.pow((ahead + tied) / (double) iterations, opponents);

		Properties prp = new Properties();
		prp.put("PPot", Ppot);
		prp.put("NPot", Npot);
		prp.put("winProb", winProb_n);
		prp.put("HS", HS_n);
		return prp;
	}
	public void cleanReport() {
		variableList.keySet().forEach(key -> variableList.put(key, ""));
		variableList.put(STATUS, STATUS_OK);
		actionsBarChart.setDataSet(null);
		updateReport();
	}

	/**
	 * clear the simulation eviorement. Use this metod to clear al component in case of error or start/stop event
	 * 
	 */
	public void clearEnviorement() {
		currentRound = NO_CARDS_DEALT;
		this.numSimPlayers = -1;
		holeCards.makeEmpty();
		communityCards.makeEmpty();
		// 190831: ya el sistema se esta moviendo. por lo menos hace fold !!!! :D estoy en el salon de clases del campo
		// de refujiados en dresden !!!! ya van 2 meses
		cardsBuffer.clear();
		sensorStatus.clear();
		potValue = -1;
		tablePosition = -1;
		callValue = -1;
		raiseValue = -1;
		heroChips = -1;
		// // parameters from the panel
		// this.parameters = Hero.heroPanel.getTrooperPanel().getValues();
		// this.buyIn = ((Number) parameters.get("table.buyIn")).doubleValue();
		// this.smallBlind = ((Number) parameters.get("table.smallBlid")).doubleValue();
		// this.bigBlind = ((Number) parameters.get("table.bigBlid")).doubleValue();
	}

	public String getCurrentHandStrengName() {
		return UoAHandEvaluator.nameHand(currentHand);
	}

	/**
	 * Return the information component whit all values computesd form simulations and game status
	 * 
	 * @return information component
	 */
	public TUIPanel getReportPanel() {
		return reportPanel;
	}
	/**
	 * Return the strin representation of the parameters of the table
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
	// // the word oportunity means the event present in flop or turn streat. in river is not a oportunity any more
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
	 * perform the PokerProphesier simulation. Call this method when all the cards on the table has been setted using
	 * {@link #addCard(String, String)} this method will create the {@link HoleCards} and the {@link CommunityCards} (if
	 * is available). After the simulation, the adapters are updated and can be consulted and the report are up to date
	 * 
	 */
	public void runSimulation() {

		variableList.put(STATUS, "Runing ...");
		updateReport();

		// Hero.heroLogger.warning("String " + card + " for card representation incorrect. Card not created");
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
		currentRound = currentHand.size();

		uoAEvaluation.clear();
		uoAEvaluation.putAll(getUoAEvaluation(holeCards, communityCards));
		Ppot = (double) uoAEvaluation.get("PPot");
		Npot = (double) uoAEvaluation.get("NPot");
		winProb_n = (double) uoAEvaluation.get("winProb");
		HS_n = (double) uoAEvaluation.get("HS");

		/**
		 * update the simulation result to the console
		 */
		variableList.put("simulator.Troper probability", percentageFormat.format(winProb_n));
		variableList.put("simulator.Trooper Current hand", uoAEvaluation.get("name"));
		variableList.put("simulator.Table cards", currentHand.toString());
		String txt = "Amunitions " + heroChips + " Pot " + potValue + " Call " + callValue + " Raise " + raiseValue
				+ " Position " + tablePosition;
		variableList.put("simulator.Table values", txt);
		variableList.put("simulator.Simulator values",
				"Round " + streetNames.get(currentRound) + " Players " + numSimPlayers);

		variableList.put(STATUS, STATUS_OK);

		updateReport();
	}

	/**
	 * set the action related information.
	 * 
	 * @param aperformed - the action performed by the {@link Trooper}
	 * @param actions list of {@link TEntry} where each key is an instancia of {@link TrooperAction} and the value is
	 *        the probability for this action to be selected
	 */
	public void setActionsData(TrooperAction aperformed, Vector<TEntry<TrooperAction, Double>> actions) {
		actionsBarChart.setCategoryMarker(aperformed);
		actionsBarChart.setDataSet(actions);
		updateReport();
	}

	public void setCallValue(double callValue) {
		this.callValue = callValue;
	}
	public void setHeroChips(double heroChips) {
		this.heroChips = heroChips;
		if (heroChips > heroChipsMax)
			heroChipsMax = heroChips;
	}
	public void setNunOfPlayers(int p) {
		this.numSimPlayers = p;
	}
	public void setPotValue(double potValue) {
		this.prevPotValue = this.potValue;
		this.potValue = potValue;
	}
	public void setRaiseValue(double raiseValue) {
		this.raiseValue = raiseValue;
	}
	/**
	 * Update the table position. the Hero�s table position is determinated detecting the dealer button and counting
	 * clockwise. For examples, in a 4 villans table:
	 * <li>If hero has the dealer button, this method return 5;
	 * <li>if villan4 is the dealer, this method return 1. Hero is small blind
	 * <li>if villan1 is the dealer, this method return 4. Hero is in middle table position.
	 * <p>
	 * this metod is called during the {@link SensorsArray#read(String)} operation.
	 */
	public void setTablePosition(int dealerPos, int villans) {
		// int tp = Math.abs(dbp - (getActiveSeats() + 1));
		this.tablePosition = Math.abs(dealerPos - (villans + 1));
	}
	public void setVariable(String key, Object value) {
		// format double values
		Object value1 = value;
		if (value instanceof Double)
			value1 = twoDigitFormat.format(((Double) value).doubleValue());
		variableList.put(key, value1);
		if (Trooper.STATUS.equals(key)) {
			// variableList.put("trooper.Performance Step time", (System.currentTimeMillis() - lastStepMillis));
			// lastStepMillis = System.currentTimeMillis();
		}
		// mandatori. i nedd to see what is happening
		updateReport();
	}
	public void updateReport() {
		if (!Hero.allowSimulationGUIUpdate())
			return;

		Hero.heroLogger.info("Table values: " + variableList.get("simulator.Table values"));
		Hero.heroLogger.info("Troper probability: " + variableList.get("simulator.Troper probability"));
		Hero.heroLogger.info("Trooper Current hand: " + variableList.get("simulator.Trooper Current hand"));
		Hero.heroLogger.info("Simulator values: " + variableList.get("simulator.Simulator values"));

		// long t1 = System.currentTimeMillis();
		String text = "<html>";
		String tmp = variableList.keySet().stream().map(key -> key + ": " + variableList.get(key))
				.collect(Collectors.joining("\n"));

		// remove the group heather. just for visual purporse
		tmp = tmp.replace("sensorArray.", "");
		tmp = tmp.replace("simulator.ammount.", "");
		tmp = tmp.replace("simulator.", "");
		tmp = tmp.replace("trooper.", "");
		tmp = tmp.replace("aa.", "");
		text += getFormateTable(tmp);

		text += "</html>";
		reportJLabel.setText(text);
		reportJLabel.repaint();
		// Hero.heroLogger.severe("updateMyOutsHelperInfo(): " + (System.currentTimeMillis() - t1));
	}

	/**
	 * return a HTML table based on the <code>helperString</code> argument. the <code>only</code> paratemeter indicate a
	 * filter of elemenst. If any line form helperstring argument star with a word form this list, the line is include
	 * in the result. an empty list for this parametr means all elemenst
	 * 
	 * @param helperString - string come form any {@link PokerProphesierAdapter} helper class
	 * @param only - list of filter words or empty list
	 * 
	 * @return HTML table
	 */
	private String getFormateTable(String helperString) {
		String[] hslines = helperString.split("\n");
		String res = "";
		for (String lin : hslines) {
			lin = "<tr><td>" + lin + "</td></tr>";
			res += lin.replaceAll(": ", "</td><td>");
		}
		return "<table border=\"0\", cellspacing=\"0\">" + res + "</table>";
	}

}