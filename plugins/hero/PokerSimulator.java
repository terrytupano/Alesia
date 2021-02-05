package plugins.hero;

import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;

import javax.swing.*;

import com.alee.laf.combobox.*;
import com.alee.managers.settings.*;
import com.javaflair.pokerprophesier.api.adapter.*;
import com.javaflair.pokerprophesier.api.card.*;
import com.javaflair.pokerprophesier.api.exception.*;
import com.javaflair.pokerprophesier.api.helper.*;
import com.jgoodies.common.base.*;

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
	public static final int HOLE_CARDS_DEALT = 1;
	public static final int FLOP_CARDS_DEALT = 2;
	public static final int TURN_CARD_DEALT = 3;
	public static final int RIVER_CARD_DEALT = 4;
	public static int SMALL_BLIND = 1;
	public static int BIG_BLIND = 2;
	public static int MIDLE = 3;
	public static int DEALER = 10;
	public static String STATUS = "aa.Simulator Status";
	public static String STATUS_OK = "Ok";
	public static String STATUS_ERROR = "Error";
	private static DecimalFormat fourDigitFormat = new DecimalFormat("#0.0000");
	private static DecimalFormat twoDigitFormat = new DecimalFormat("#0.00");
	// pair of 22
	// private static int minHandRank = 371293;
	// top hole card (no pair) (AK)
	private static int topHoleRank = 167;
	// villans top hand
	private int oppTopHand;
	private int handPotential;
	private int handPotentialOuts;
	// Number of simulations, total players
	private int numSimulations = 100000;
	// temporal storage for incoming cards
	private Hashtable<String, String> cardsBuffer;
	// number of players
	private int numSimPlayers;
	private TreeMap<String, Object> variableList;
	private PokerProphesierAdapter adapter;
	private double callValue, raiseValue, potValue;
	private CommunityCards communityCards;
	private JLabel reportJLabel;
	private TUIPanel reportPanel;
	private int currentRound;
	private HoleCards holeCards;
	private int tablePosition;
	private WebComboBox helperFilterComboBox;
	private MyHandHelper myHandHelper;
	private MyHandStatsHelper myHandStatsHelper;
	private MyOutsHelper myOutsHelper;
	private OppHandStatsHelper oppHandStatsHelper;
	private MyGameStatsHelper myGameStatsHelper;
	private Hashtable<Integer, Double> upperProbability;
	private double heroChips;
	private double buyIn;
	private double smallBlind;
	private double bigBlind;
	private ActionsBarChart actionsBarChart;
	// private long lastStepMillis;
	private double winPlusTieProbability;
	private UoAHand uoAHand;
	private Hashtable<Integer, String> handNames = new Hashtable<>();
	private Vector<Integer> handStrengSamples = new Vector<>();
	private boolean takeOportunity = false;
	private Hashtable<String, Object> parameters;

	public PokerSimulator() {
		handStrengSamples.add(2970352); // straight_flush
		handStrengSamples.add(2599136); // four_of_a_kind
		handStrengSamples.add(2227843); // full_house
		handStrengSamples.add(2101414); // flush
		handStrengSamples.add(1485180); // straight
		handStrengSamples.add(1114893); // three_of_a_kind
		handStrengSamples.add(743847); // two_pairs
		handStrengSamples.add(384475); // pair
		handStrengSamples.add(0); // hight card

		handNames.put(0, "straight_flush");
		handNames.put(1, "four_of_a_kind");
		handNames.put(2, "full_house");
		handNames.put(3, "flush");
		handNames.put(4, "straight");
		handNames.put(5, "three_of_a_kind");
		handNames.put(6, "two_pairs");
		handNames.put(7, "pair");
		handNames.put(8, "high_card");

		this.cardsBuffer = new Hashtable<String, String>();
		this.uoAHand = new UoAHand();
		// Create an adapter to communicate with the simulator
		this.adapter = new PokerProphesierAdapter();
		adapter.setNumSimulations(numSimulations);
		variableList = new TreeMap<>();

		// information components
		helperFilterComboBox = new WebComboBox();
		helperFilterComboBox.addItem(new TEntry("MyHandHelper", "My hand"));
		helperFilterComboBox.addItem(new TEntry("MyHandStatsHelper", "My hand statistics"));
		helperFilterComboBox.addItem(new TEntry("MyOutsHelper", "My outs"));
		helperFilterComboBox.addItem(new TEntry("OppHandStatsHelper", "Oponent Hand"));
		helperFilterComboBox.addItem(new TEntry("MyGameStatsHelper", "Game statistic"));
		helperFilterComboBox.addItem(new TEntry("trooperVariables", "Trooper variables"));
		// helperFilterComboBox.addActionListener(evt -> filterSensors());
		helperFilterComboBox.registerSettings(new Configuration<ComboBoxState>("PokerSimulator.HelperFilter"));

		this.reportPanel = new TUIPanel();
		reportPanel.showAditionalInformation(false);
		reportPanel.getToolBarPanel().add(helperFilterComboBox);
		this.reportJLabel = new JLabel();
		reportJLabel.setVerticalAlignment(JLabel.TOP);
		reportJLabel.setFont(new Font("courier new", Font.PLAIN, 12));
		this.actionsBarChart = new ActionsBarChart();
		JPanel jp = new JPanel(new BorderLayout());
		jp.add(reportJLabel, BorderLayout.CENTER);
		jp.add(actionsBarChart.getChartPanel(), BorderLayout.SOUTH);
		// reportPanel.setBodyComponent(new JScrollPane(reportJLabel));
		reportPanel.setBodyComponent(jp);

		// test: uper probabilitiy by street.
		this.upperProbability = new Hashtable<>();
		// pair of AA
		upperProbability.put(PokerSimulator.HOLE_CARDS_DEALT, 0.49);
		// middle card and 2 more cards in the comunity card: tree of a kind: 7.0.
		upperProbability.put(PokerSimulator.FLOP_CARDS_DEALT, 0.7);
		upperProbability.put(PokerSimulator.TURN_CARD_DEALT, 0.8);
		upperProbability.put(PokerSimulator.RIVER_CARD_DEALT, 0.9);

		// clearEnviorement();
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
		this.currentRound = NO_CARDS_DEALT;
		this.numSimPlayers = -1;
		holeCards = null;
		communityCards = null;
		// variableList.clear();
		// 190831: ya el sistema se esta moviendo. por lo menos hace fold !!!! :D estoy en el salon de clases del campo
		// de refujiados en dresden !!!! ya van 2 meses
		cardsBuffer.clear();
		potValue = -1;
		tablePosition = -1;
		callValue = -1;
		raiseValue = -1;
		heroChips = -1;
		oppTopHand = Hand.STRAIGHT_FLUSH;
		// parameters from the panel
		this.parameters = Hero.heroPanel.getTrooperPanel().getValues();
		this.buyIn = ((Number) parameters.get("table.buyIn")).doubleValue();
		this.smallBlind = ((Number) parameters.get("table.smallBlid")).doubleValue();
		this.bigBlind = ((Number) parameters.get("table.bigBlid")).doubleValue();
		this.takeOportunity = ((Boolean) parameters.get("takeOportunity"));
	}

	/**
	 * this mathod act like a buffer betwen {@link SensorsArray} and this class to set the cards based on the name/value
	 * of the {@link ScreenSensor} component while the cards arrive at the game table. For example durin a reading
	 * operation the card are retrived without order. this method store the incomming cards and the run simulation
	 * method will create the correct game status based on the card stored
	 * 
	 * @param sName - name of the {@link ScreenSensor}
	 * @param card - ocr retrived from the sensor. public void addCard(String sName, String card) {
	 *        cardsBuffer.put(sName, card); // System.out.println(sName + " " + card); }
	 */
	public PokerProphesierAdapter getAdapter() {
		return adapter;
	}
	public double getBigBlind() {
		return bigBlind;
	}
	public double getBuyIn() {
		return buyIn;
	}

	public double getCallValue() {
		return callValue;
	}

	public Hashtable<String, String> getCardsBuffer() {
		return cardsBuffer;
	}

	public CommunityCards getCommunityCards() {
		return communityCards;
	}

	/**
	 * this method compute and return the relation between gloval variable {@link #oppTopHand} and the current hero
	 * hand. this range is: [0.11,3]
	 */
	public double getCurrentHandStreng() {
		double rtnval = 0.0;
		double rank = myHandHelper.getHandRank();
		rtnval = rank / oppTopHand;
		return rtnval;
	}

	public String getCurrentHandStrengName() {
		return UoAHandEvaluator.nameHand(uoAHand);
	}

	public int getCurrentRound() {
		return currentRound;
	}
	public int getHandPotential() {
		return handPotential;
	}

	public int getHandPotentialOuts() {
		return handPotentialOuts;
	}

	/**
	 * this method return a factor 0 to (possible max value) 2 that represent the relation between the the best hand
	 * that hero can posible have and the best hand that any villan can hold.
	 * <p>
	 * E.g. Hero: Ac As, flop Ad 7d 4h. at this point, Hero hat the Nut, {@link #oppTopHand} = Hand.THREE_OF_A_KIND. the
	 * best hand that hero can have will be Hand.FOUR_OF_A_KIND. this metod will return 2
	 * 
	 * @see #updateHandValues()
	 * 
	 * @return the hand potential
	 */
	// public Pair<Double, Integer> getHandPotential() {
	// double handPotential = 0.0;
	// String hs = "";
	// int outs = 0;
	//
	// if (myOutsHelper != null) {
	// Card cards[][] = myOutsHelper.getAllOuts();
	// for (int i = 0; i < Hand.STRAIGHT_FLUSH; i++) {
	// int currOut = cards[i].length;
	// if (currOut > 0 && currOut > outs) {
	// outs = currOut;
	// handPotential = (Hand.STRAIGHT_FLUSH - i);
	// // 210117: with this modification, hero supportet 3 hour of continuous battle in a table without
	// // oportunity and hold steady without loosing his chips. :D
	// hs += handNames.get(i) + " " + outs + " ";
	// }
	// }
	// }
	// // if (handPotential > 0)
	// // handPotential = handPotential / oppTopHand;
	// hs += "= " + twoDigitFormat.format(handPotential);
	// setVariable("simulator.Hand potential", hs);
	// return new Pair<>(handPotential, outs);
	// }

	public double getHeroChips() {
		return heroChips;
	}
	public MyGameStatsHelper getMyGameStatsHelper() {
		return myGameStatsHelper;
	}

	public MyHandHelper getMyHandHelper() {
		return myHandHelper;
	}
	public MyHandStatsHelper getMyHandStatsHelper() {
		return myHandStatsHelper;
	}
	public HoleCards getMyHoleCards() {
		return holeCards;
	}

	public int getNumSimPlayers() {
		return numSimPlayers;
	}

	public OppHandStatsHelper getOppHandStatsHelper() {
		return oppHandStatsHelper;
	}
	public int getOppTopHand() {
		return oppTopHand;
	}
	public double getPotValue() {
		return potValue;
	}

	public double getPreFlopHandStreng() {
		double rank = UoAHandEvaluator.rankHand(uoAHand);
		// if is already a poket pair, assig 1
		rank = myHandHelper.isPocketPair() ? 1.0 : rank / topHoleRank;
		return rank;
	}

	/**
	 * return the probability of win plus tie
	 * 
	 * @return the probability of win + prob of tie
	 */
	public double getProbability() {
		return winPlusTieProbability;
	}
	public double getRaiseValue() {
		return raiseValue;
	}
	/**
	 * Return the information component whit all values computesd form simulations and game status
	 * 
	 * @return information component
	 */
	public TUIPanel getReportPanel() {
		return reportPanel;
	}

	public double getSmallBlind() {
		return smallBlind;
	}
	/**
	 * Return the strin representation of the parameters of the table
	 * 
	 * @return table parms
	 */
	public String getTableParameters() {
		return getBuyIn() + "," + getBigBlind() + "," + getSmallBlind();
	}

	public int getTablePosition() {
		return tablePosition;
	}

	public TreeMap<String, Object> getVariables() {
		return variableList;
	}

	public String isdraw() {
		String drw = null;
		drw = myHandHelper.isFlushDraw() ? "Flush draw" : drw;
		drw = myHandHelper.isStraightDraw() ? "Strainht draw" : drw;
		drw = myHandHelper.isStraightFlushDraw() ? "Straight Flush draw" : drw;
		return drw;
	}
	/**
	 * check whether is an oportunity. An oportunity is present when the current street is {@link #FLOP_CARDS_DEALT} or
	 * futher and the following conditions is found:
	 * <li>Heros hand muss be >= to the hand setted in {@link #oppTopHand} gobal variable
	 * <li>The hand is a set (both card in heros.s hands participate in the action)
	 * <li>OR the hand is the nut
	 * 
	 * @return a text explain what oportunity is detected or <code>null</code> if no oportunity are present
	 */
	public String isOportunity() {
		String txt = null;
		// Hero must check for oportunity
		if (!takeOportunity)
			return txt;
		// the word oportunity means the event present in flop or turn streat. in river is not a oportunity any more
		if (currentRound < FLOP_CARDS_DEALT)
			return txt;

		// is the nut
		if (getMyHandHelper().isTheNuts())
			return "Is the Nuts";

		// villan.s top hands is stronger as hero.s hand
		// TEMP oppTopHand+1
		if (oppTopHand + 1 > myHandHelper.getHandRank())
			return txt;

		// String sts = getSignificantCards();
		// // set hand but > pair
		// if (myHandHelper.getHandRank() > Hand.PAIR && sts.length() == 5) {
		// String nh = UoAHandEvaluator.nameHand(uoAHand);
		// txt = "Troper has " + nh + " (set)";
		// }

		String nh = UoAHandEvaluator.nameHand(uoAHand);
		txt = "Troper has " + nh;

		return txt;
	}
	/**
	 * perform the PokerProphesier simulation. Call this method when all the cards on the table has been setted using
	 * {@link #addCard(String, String)} this method will create the {@link HoleCards} and the {@link CommunityCards} (if
	 * is available). After the simulation, the adapters are updated and can be consulted and the report are up to date
	 * 
	 */
	public void runSimulation() {
		try {
			variableList.put(STATUS, "Runing ...");
			updateReport();
			// Set the simulator parameters

			// TODO: check this parameters. maybe is better set off or change it during the game play because not all
			// the
			// time are true. for example, in a 6 villans pre flop game, i can.t assume set opp hole card realiytic is
			// false, but in th turn, if a villan still in the battle, is set to true because maybe he got something
			//
			// or use this info comparing with the gameplayer history !!!!!!!!!!!!!
			adapter.setMyOutsHoleCardSensitive(true);
			adapter.setOppHoleCardsRealistic(true);
			adapter.setOppProbMyHandSensitive(true);

			// String c1 = cardsBuffer.get("hero.card1");
			// String c2 = cardsBuffer.get("hero.card2");
			// if (c1 == null || c2 == null) {
			// JOptionPane.showMessageDialog(Alesia.mainFrame, "error");
			// Trooper.getInstance().cancel(true);
			// }
			createHoleCards();
			createComunityCards();

			String h = cardsBuffer.values().stream().collect(Collectors.joining(" "));
			this.uoAHand = new UoAHand(h);

			adapter.runMySimulations(holeCards, communityCards, numSimPlayers, currentRound);
			myGameStatsHelper = adapter.getMyGameStatsHelper();
			myHandStatsHelper = adapter.getMyHandStatsHelper();
			oppHandStatsHelper = adapter.getOppHandStatsHelper();
			myOutsHelper = adapter.getMyOutsHelper();
			myHandHelper = adapter.getMyHandHelper();
			updateHandValues();
			updateSimulationResults();
			variableList.put(STATUS, STATUS_OK);
			String oportunity = isOportunity();
			if (oportunity != null)
				variableList.put(STATUS, oportunity);
			updateReport();
		} catch (SimulatorException e) {
			setVariable(STATUS, STATUS_ERROR);
			// setVariable(STATUS, e.getClass().getSimpleName() + e.getMessage());
			Hero.heroLogger.warning(e.getMessage() + "\n\tCurrent round: " + currentRound + "\n\tHole cards: "
					+ holeCards + "\n\tComunity cards: " + communityCards);
		} catch (Exception e) {
			setVariable(STATUS, STATUS_ERROR);
			// setVariable(STATUS, e.getClass().getSimpleName() + " " + e.getMessage());
			Hero.heroLogger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
	public void setActionsData(String aperformed, Vector<TEntry<String, Double>> actions) {
		actionsBarChart.setCategoryMarker(aperformed);
		actionsBarChart.setDataSet(actions);
		updateReport();
	}

	public void setCallValue(double callValue) {
		this.callValue = callValue;
	}

	public void setHeroChips(double heroChips) {
		this.heroChips = heroChips;
	}
	public void setNunOfPlayers(int p) {
		this.numSimPlayers = p;
	}

	public void setPotValue(double potValue) {
		this.potValue = potValue;
	}
	public void setRaiseValue(double raiseValue) {
		this.raiseValue = raiseValue;
	}
	public void setTablePosition(int tp) {
		this.tablePosition = tp;
	}
	public void setVariable(String key, Object value) {
		// format double values
		Object value1 = value;
		if (value instanceof Double)
			value1 = fourDigitFormat.format(((Double) value).doubleValue());
		variableList.put(key, value1);
		if (Trooper.STATUS.equals(key)) {
			// variableList.put("trooper.Performance Step time", (System.currentTimeMillis() - lastStepMillis));
			// lastStepMillis = System.currentTimeMillis();
		}
		// mandatori. i nedd to see what is happening
		updateReport();
	}

	public void updateReport() {
		Predicate<String> valgt0 = new Predicate<String>() {
			@Override
			public boolean test(String t) {
				double d = new Double(t);
				return d > 0;
			}
		};
		// long t1 = System.currentTimeMillis();
		String selectedHelper = ((TEntry) helperFilterComboBox.getSelectedItem()).getKey().toString();
		String text = "<html>";
		if (myHandHelper != null && selectedHelper.equals("MyHandHelper")) {
			text += getFormateTable(myHandHelper.toString());
		}
		if (myHandStatsHelper != null && selectedHelper.equals("MyHandStatsHelper")) {
			String tmp = myHandStatsHelper.toString();
			tmp = tmp.replaceFirst("[=]", ":");
			text += getFormateTable(tmp, valgt0);
		}
		MyOutsHelper myOutsHelper = adapter.getMyOutsHelper();
		if (myOutsHelper != null && selectedHelper.equals("MyOutsHelper")) {
			text += getFormateTable(myOutsHelper.toString(), str -> !Strings.isBlank(str));
		}
		OppHandStatsHelper oppHandStatsHelper = adapter.getOppHandStatsHelper();
		if (oppHandStatsHelper != null && selectedHelper.equals("OppHandStatsHelper")) {
			text += getFormateTable(oppHandStatsHelper.toString(), valgt0);
		}
		if (myGameStatsHelper != null && selectedHelper.equals("MyGameStatsHelper")) {
			text += getFormateTable(myGameStatsHelper.toString());
		}
		if (selectedHelper.equals("trooperVariables")) {
			String tmp = variableList.keySet().stream().map(key -> key + ": " + variableList.get(key))
					.collect(Collectors.joining("\n"));

			// remove the group heather. just for visual purporse
			tmp = tmp.replace("sensorArray.", "");
			tmp = tmp.replace("simulator.ammount.", "");
			tmp = tmp.replace("simulator.", "");
			tmp = tmp.replace("trooper.", "");
			tmp = tmp.replace("aa.", "");
			text += getFormateTable(tmp);
		}

		text += "</html>";
		reportJLabel.setText(text);
		reportJLabel.repaint();
		// Hero.heroLogger.severe("updateMyOutsHelperInfo(): " + (System.currentTimeMillis() - t1));
	}

	/**
	 * Create and return an {@link UoACard} based on the string representation. this method return <code>null</code> if
	 * the string representation is not correct.
	 * 
	 * @param scard - Standar string representation of a card
	 * @return Card
	 */
	private Card createCardFromString(String card) {
		Card car = null;
		int suit = -1;
		int rank = -1;
		String scard = new String(card);

		String srank = scard.substring(0, 1).toUpperCase();
		rank = srank.equals("A") ? Card.ACE : rank;
		rank = srank.equals("K") ? Card.KING : rank;
		rank = srank.equals("Q") ? Card.QUEEN : rank;
		rank = srank.equals("J") ? Card.JACK : rank;
		rank = srank.equals("T") ? Card.TEN : rank;
		if (scard.startsWith("10")) {
			rank = Card.TEN;
			scard = scard.substring(1);
		}
		rank = scard.startsWith("9") ? Card.NINE : rank;
		rank = scard.startsWith("8") ? Card.EIGHT : rank;
		rank = scard.startsWith("7") ? Card.SEVEN : rank;
		rank = scard.startsWith("6") ? Card.SIX : rank;
		rank = scard.startsWith("5") ? Card.FIVE : rank;
		rank = scard.startsWith("4") ? Card.FOUR : rank;
		rank = scard.startsWith("3") ? Card.THREE : rank;
		rank = scard.startsWith("2") ? Card.TWO : rank;

		// remove rank
		scard = scard.substring(1).toLowerCase();

		suit = scard.startsWith("s") ? Card.SPADES : suit;
		suit = scard.startsWith("c") ? Card.CLUBS : suit;
		suit = scard.startsWith("d") ? Card.DIAMONDS : suit;
		suit = scard.startsWith("h") ? Card.HEARTS : suit;

		if (rank > 0 && suit > 0)
			car = new Card(rank, suit);
		else
			Hero.heroLogger.warning("String " + card + " for card representation incorrect. Card not created");

		return car;
	}

	/**
	 * create the comunity cards. This method also set the currnet round of the game based on length of the
	 * <code>cards</code> parameter.
	 * <ul>
	 * <li>3 for {@link PokerSimulator#FLOP_CARDS_DEALT}
	 * <li>4 for {@link PokerSimulator#TURN}
	 * <li>5 for {@link PokerSimulator#RIVER}
	 * </ul>
	 */
	private void createComunityCards() {
		ArrayList<String> list = new ArrayList<>();
		if (cardsBuffer.containsKey("flop1"))
			list.add(cardsBuffer.get("flop1"));
		if (cardsBuffer.containsKey("flop2"))
			list.add(cardsBuffer.get("flop2"));
		if (cardsBuffer.containsKey("flop3"))
			list.add(cardsBuffer.get("flop3"));
		if (cardsBuffer.containsKey("turn"))
			list.add(cardsBuffer.get("turn"));
		if (cardsBuffer.containsKey("river"))
			list.add(cardsBuffer.get("river"));

		Card[] ccars = new Card[list.size()];
		for (int i = 0; i < ccars.length; i++) {
			ccars[i] = createCardFromString(list.get(i));
		}
		communityCards = new CommunityCards(ccars);
		// set current round
		currentRound = ccars.length == 3 ? FLOP_CARDS_DEALT : currentRound;
		currentRound = ccars.length == 4 ? TURN_CARD_DEALT : currentRound;
		currentRound = ccars.length == 5 ? RIVER_CARD_DEALT : currentRound;
	}
	/**
	 * Create my cards
	 * 
	 * @param c1 - String representation of card 1
	 * @param c2 - String representation of card 2
	 */
	private void createHoleCards() {
		String c1 = cardsBuffer.get("hero.card1");
		String c2 = cardsBuffer.get("hero.card2");
		Card ca1 = createCardFromString(c1);
		Card ca2 = createCardFromString(c2);
		holeCards = new HoleCards(ca1, ca2);
		currentRound = HOLE_CARDS_DEALT;
	}
	private String getFormateTable(String helperString) {
		return getFormateTable(helperString, s -> true);
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
	private String getFormateTable(String helperString, Predicate<String> valueFilter) {
		String[] hslines = helperString.split("\n");
		String res = "";
		for (String lin : hslines) {
			final String[] k_v = lin.split("[:]");
			if (valueFilter.test(k_v[1])) {
				lin = "<tr><td>" + lin + "</td></tr>";
				res += lin.replaceAll(": ", "</td><td>");
			}
		}
		return "<table>" + res + "</table>";
	}
	/**
	 * return a string representing the card in the troopers hand that participe in the hand Example:
	 * <li>hero: 2s Ah, comunity: 2s 4h 4c this method return only 2s
	 * <li>hero: 2s Ah, comunity: 2s Ah 4c this method return 2s Ah
	 * 
	 * @return the hole cards that participe in the hand
	 */
	private String getSignificantCards() {
		String stimate = "";
		Card[] cards = myHandHelper.getSignificantCards();
		for (Card card : cards) {
			stimate += card.isHoleCard() ? card.toString().substring(0, 2) + " " : "";
		}
		return stimate.trim();
	}

	/**
	 * this method update the gloval variable {@link #oppTopHand} whit the hihest hand that any villan can hold
	 * <p>
	 * The probabilities aren't future predictions of what hand one or many opponents may achieve by the river, rather
	 * they reflect the current hand that any single opponent may have already achieved.
	 * <p>
	 * this method allways set a minimum value for the gloval variable. when hero hat the nuts, set this variable as
	 * <code>Hand.THREE_OF_A_KIND</code>
	 */
	private void updateHandValues() {
		// TEMP: for opptop hand and handstreng select the most probable
		/////////////////////////////////////////////
		// the initialization of global variable happen in clearenviorement method

		// oppTopHand = -1;
		String msg = "";
		handPotential = Hand.HIGH_CARD;
		handPotentialOuts = 0;

		// select the most probable hand (avoiding the hight card and pair)
		double lastVal = 0.0;
		if (oppHandStatsHelper != null) {
			float[] list = oppHandStatsHelper.getAllProbs();
			for (int i = 0; i < Hand.STRAIGHT_FLUSH - 2; i++) {
				if (list[i] > 0 && list[i] > lastVal) {
					lastVal = list[i];
					oppTopHand = Hand.STRAIGHT_FLUSH - i;
					msg = handNames.get(i) + " " + fourDigitFormat.format(list[i]);
				}
			}
		}

		// no detection of oppTopHand can be from 2 way. hero is in preflop or the current hand favor hero (is the
		// nut). manual set to the standar Hand.THREE_OF_A_KIND
		if (oppTopHand == -1 || currentRound < FLOP_CARDS_DEALT) {
			oppTopHand = Hand.THREE_OF_A_KIND;
			msg = "Manual setted to Hand.THREE_OF_A_KIND.";
		}
		setVariable("simulator.Hand streng villans", msg);

		// TEMP. select as potential, the hand whit more out.
		msg = "";
		if (myOutsHelper != null) {
			Card cards[][] = myOutsHelper.getAllOuts();
			for (int i = 0; i < Hand.STRAIGHT_FLUSH; i++) {
				int currOut = cards[i].length;
				if (currOut > 0 && currOut > handPotentialOuts) {
					handPotentialOuts = currOut;
					handPotential = (Hand.STRAIGHT_FLUSH - i);
					// 210117: with this modification, hero supportet 3 hour of continuous battle in a table without
					// oportunity and hold steady without loosing his chips. :D
				}
			}
		}
		// if (handPotential > 0)
		// handPotential = handPotential / oppTopHand;

		msg += handNames.get(Hand.STRAIGHT_FLUSH - handPotential) + " with " + handPotentialOuts + " outs";
		setVariable("simulator.Hand potential", msg);
	}

	/**
	 * update the simulation result to the console
	 */
	private void updateSimulationResults() {
		winPlusTieProbability = myGameStatsHelper == null
				? 0
				: myGameStatsHelper.getWinProb() + myGameStatsHelper.getTieProb();

		variableList.put("simulator.Troper probability", fourDigitFormat.format(winPlusTieProbability));
		variableList.put("simulator.Trooper Current hand", getMyHandHelper().getHand().toString());
		variableList.put("simulator.Table cards", getMyHoleCards().getFirstCard() + ", "
				+ getMyHoleCards().getSecondCard() + ", " + getCommunityCards().toString());
		String txt = "Amunitions " + getHeroChips() + " Pot " + getPotValue() + " Call " + getCallValue() + " Raise "
				+ getRaiseValue() + " Position " + getTablePosition();
		variableList.put("simulator.Table values", txt);
		variableList.put("simulator.Simulator values", "Round " + getCurrentRound() + " Players " + getNumSimPlayers());

		Hero.heroLogger.info("Table parameters: " + getTableParameters());
		Hero.heroLogger.info("Table values: " + variableList.get("simulator.Table values"));
		Hero.heroLogger.info("Troper probability: " + variableList.get("simulator.Troper probability"));
		Hero.heroLogger.info("Trooper Current hand: " + variableList.get("simulator.Trooper Current hand"));
		Hero.heroLogger.info("Simulator values: " + variableList.get("simulator.Simulator values"));
		Hero.heroLogger.info("Hand: " + variableList.get("simulator.Hand potential") + " OppTopHand "
				+ handNames.get(Hand.STRAIGHT_FLUSH - oppTopHand));

	}
}
