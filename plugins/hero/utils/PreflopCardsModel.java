package plugins.hero.utils;

import java.util.*;

import org.javalite.activejdbc.*;

import com.javaflair.pokerprophesier.api.card.*;

import core.*;
import core.datasource.model.*;
import plugins.hero.UoAHandEval.*;

/**
 * Represents a range of hands that a player can have. Its purpose is to work with Texas Hold'em games only. A range is
 * represented by the percentage of hand types that it contains. There are 169 hand types in Texas Hold'em. You can
 * include any one of those 169 hand types in the range, even if the range wouldn't normally support it. Setting a
 * specific percentage to a range will include a set of predefined hand types in it and eliminate all previously
 * included types. Because of this, it's recommended to set the range to a specific percentage and then, if needed, make
 * tiny adjustments to it. The methods within this class will document this behaviour in more detail.
 * 
 * @author Radu Murzea
 */
public class PreflopCardsModel {
	/**
	 * Contains the names of all hand types. They are split among 13 lines and 13 columns. In order to work with them
	 * easier, there are some patterns to their arrangements. For example: the pocket pairs run along the main diagonal.
	 * Also, all suited types are above this diagonal while the offsuit types are found below.
	 */
	private static final String[][] rangeNames = {
			{"AA", "AKs", "AQs", "AJs", "ATs", "A9s", "A8s", "A7s", "A6s", "A5s", "A4s", "A3s", "A2s"},
			{"AKo", "KK", "KQs", "KJs", "KTs", "K9s", "K8s", "K7s", "K6s", "K5s", "K4s", "K3s", "K2s"},
			{"AQo", "AKo", "QQ", "QJs", "QTs", "Q9s", "Q8s", "Q7s", "Q6s", "Q5s", "Q4s", "Q3s", "Q2s"},
			{"AJo", "KJo", "QJo", "JJ", "JTs", "J9s", "J8s", "J7s", "J6s", "J5s", "J4s", "J3s", "J2s"},
			{"ATo", "KTo", "QTo", "JTo", "TT", "T9s", "T8s", "T7s", "T6s", "T5s", "T4s", "T3s", "T2s"},
			{"A9o", "K9o", "Q9o", "J9o", "T9o", "99", "98s", "97s", "96s", "95s", "94s", "93s", "92s"},
			{"A8o", "K8o", "Q8o", "J8o", "T8o", "98o", "88", "87s", "86s", "85s", "84s", "83s", "82s"},
			{"A7o", "K7o", "Q7o", "J7o", "T7o", "97o", "87o", "77", "76s", "75s", "74s", "73s", "72s"},
			{"A6o", "K6o", "Q6o", "J6o", "T6o", "96o", "86o", "76o", "66", "65s", "64s", "63s", "62s"},
			{"A5o", "K5o", "Q5o", "J5o", "T5o", "95o", "85o", "75o", "65o", "55", "54s", "53s", "52s"},
			{"A4o", "K4o", "Q4o", "J4o", "T4o", "94o", "84o", "74o", "64o", "54o", "44", "43s", "42s"},
			{"A3o", "K3o", "Q3o", "J3o", "T3o", "93o", "83o", "73o", "63o", "53o", "43o", "33", "32s"},
			{"A2o", "K2o", "Q2o", "J2o", "T2o", "92o", "82o", "72o", "62o", "52o", "42o", "32o", "22"}};

	private int percentage;
	private String rangeName;
	private LazyList<PreflopCards> preflopCards;

	/**
	 * new instance loaded with the values setted in <b>original<b> range name
	 */
	public PreflopCardsModel() {
		this("pokerStar");
	}

	/**
	 * new instace. the argumetn rangeName is name of a new oer existent preflop rage. if the DB dont contain such
	 * preflop range, this instance will create a fresh copy of preflopt list stored in <b>original</b>
	 * 
	 * @param rangeName - the name to load or create
	 */
	public PreflopCardsModel(String rangeName) {
		this.rangeName = rangeName;
		this.preflopCards = PreflopCards.where("rangeName = '" + rangeName + "' ORDER BY ev DESC");
		if (preflopCards.size() == 0) {
			this.preflopCards = PreflopCards.where("rangeName = 'original' ORDER BY ev DESC");
			preflopCards.forEach(card -> card.set("rangeName", rangeName, "description", rangeName, "percentage", 0,
					"selected", false, "wins", 0, "hands", 0, "ev", 0.0));
		}
		this.percentage = preflopCards.get(0).getInteger("percentage");
	}

	/**
	 * load from the DB the list of saved preflop cards selections
	 * 
	 * @return list whit the names
	 */
	public static TEntry<String, String>[] getPreflopList() {
		ArrayList<TEntry<String, String>> names = new ArrayList<>();
		LazyList<PreflopCards> ranges = PreflopCards.find("card = ?", "AA");
		for (PreflopCards range : ranges) {
			TEntry<String, String> te = new TEntry<>(range.getString("rangeName"), range.getString("description"));
			if (!names.contains(te))
				names.add(te);
		}
		return names.toArray(new TEntry[0]);
	}

	/**
	 * peform {@link #containsHand(UoACard, UoACard)} wrapping first, the incomint arguments
	 * 
	 * @param holeCards - the hero hole cards
	 * 
	 * @return true if the specified hand is selected in this range, false otherwise.
	 */
	public boolean containsHand(HoleCards holeCards) {
		String c1 = holeCards.getFirstCard().toString().replace("*", "");
		String c2 = holeCards.getSecondCard().toString().replace("*", "");
		return containsHand(new UoACard(c1), new UoACard(c2));
	}

	/**
	 * Tells if the hand composed of the two specified cards is selected in this range. The order in which you specify
	 * the cards is not relevant.
	 * 
	 * @param c1 the first card
	 * @param c2 the second card
	 * 
	 * @return true if the specified hand is selected in this range, false otherwise.
	 */
	public boolean containsHand(UoACard c1, UoACard c2) {
		String card = getStringCard(c1, c2);
		return isSelected(card);
	}

	/**
	 * perform {@link #containsHand(UoACard, UoACard)} whit the first 2 cards of this Hand. (in theory, the hole hand)
	 * 
	 * @param aHand - hand
	 * @return true if the specified hand is selected in this range, false otherwise.
	 */
	public boolean containsHand(UoAHand aHand) {
		return containsHand(aHand.getCard(1), aHand.getCard(2));
	}

	/**
	 * enable/disable the card passed as argument
	 * 
	 * @param card - card
	 */
	public void flipValue(String card) {
		// exeption if card dont exist
		PreflopCards element = preflopCards.stream().filter(pfr -> pfr.getString("card").equals(card)).findFirst()
				.get();
		element.setBoolean("selected", !element.getBoolean("selected"));
	}

	/**
	 * return the card name at this coorditates
	 * 
	 * @param row - x coordinates
	 * @param column - y coordinate
	 * 
	 * @return the string representation
	 */
	public String getCardAt(int row, int column) {
		return rangeNames[row][column];
	}

	/**
	 * Returns the percentage of this range.
	 * 
	 * @return the percentage of this range.
	 */
	public int getPercentage() {
		return this.percentage;
	}

	/**
	 * return the card representation in this context. for example, when the hand is <code>Ah Kh</code> , this method
	 * will return <code>AKs</code>
	 * 
	 * @param card1 - the first card
	 * @param card2 - the second cards
	 * 
	 * @return the strin representation
	 */
	public String getStringCard(UoACard card1, UoACard card2) {
		int rbig, rsmall;

		if (card2.getRank() > card1.getRank()) {
			rbig = card2.getRank();
			rsmall = card1.getRank();
		} else {
			rbig = card1.getRank();
			rsmall = card2.getRank();
		}

		int row, column;

		// pocket pair, always on the main diagonal
		if (rbig == rsmall) {
			row = column = 12 - rbig;
		} else {
			// above the main diagonal
			if (card1.getSuit() == card2.getSuit()) {
				row = 12 - rbig;
				column = 12 - rsmall;
				// below the main diagonal
			} else {
				row = 12 - rsmall;
				column = 12 - rbig;
			}
		}
		return rangeNames[row][column];
	}

	/**
	 * Tells you if a card type is selected in this range or not
	 * 
	 * @param row the row where the card type is found
	 * @param column the column where the card type is found
	 * @return true if the card type is selected, false otherwise.
	 */
	public boolean isSelected(String card) {
		PreflopCards element = preflopCards.stream().filter(pfr -> pfr.getString("card").equals(card)).findFirst()
				.get();
		return element.getBoolean("selected");
	}

	/**
	 * save (create or update) the current preflop card selection in the database
	 * 
	 * @param rangeName - Name of the card range
	 * @param description - description
	 */
	public void saveInDB(String rangeName, String description) {
		PreflopCards.delete("rangename = ?", rangeName);
		for (PreflopCards element : preflopCards) {
			PreflopCards range = new PreflopCards();
			range.set("rangeName", rangeName);
			range.set("card", element.get("card"));
			range.set("description", description);
			range.set("percentage", getPercentage());
			range.set("selected", element.get("selected"));
			range.set("ev", element.get("ev"));
			range.insert();
		}
	}
	/**
	 * Sets a new percentage for this range. This will overwrite only the cards inside of the selection. Cards outside
	 * of the specify procet, remaind equal.
	 * 
	 * @param newPercentage the new percentage.
	 * @throws IllegalArgumentException if the new percentage is below 0 or over 100
	 */
	public void setPercentage(int newPercentage) {
		if (newPercentage < 0 || newPercentage > 100) {
			throw new IllegalArgumentException("percentage value must be between 0 and 100 inclusively");
		}

		// preflopRanges.forEach(pfr -> pfr.setBoolean("selected", false));

		// clear only the card that are in the old percentage range
		int clear = (int) Math.round(168 * percentage / 100d);
		for (int i = 0; i < clear; i++)
			preflopCards.get(i).setBoolean("selected", false);

		this.percentage = newPercentage;
		int set = (int) Math.round(168 * percentage / 100d);
		for (int i = 0; i < set; i++)
			preflopCards.get(i).setBoolean("selected", true);
	}

	/**
	 * used for statistics purporse. this method update the <code>winnings</code> and <code>ev</code> fields
	 * preflopscards file.
	 * 
	 * @param card1 - preflop card 1
	 * @param card2 - preflop card 2
	 * @param ammount - normaly +1 or -1 to update the count and ev
	 */
	public void updateCoordenates(UoACard card1, UoACard card2, int ammount) {
		String card = getStringCard(card1, card2);
		PreflopCards range = PreflopCards.findOrCreateIt("rangeName", rangeName, "card", card);
		range.set("rangeName", rangeName);
		range.set("card", card);
		// range.set("description", description);
		range.set("percentage", getPercentage());
		range.set("selected", isSelected(card));
		int win = range.getInteger("wins") == null ? 0 : range.getInteger("wins");
		range.set("wins", win + ammount);
		int h = range.getInteger("hands") == null ? 0 : range.getInteger("hands");
		range.set("hands", ++h);
		double winD = range.getInteger("wins").doubleValue();
		double handsD = range.getInteger("hands").doubleValue();
		range.setDouble("ev", winD / handsD);

		range.save();
	}

	/**
	 * call {@link #getEV(String)} parsing the string representation from this hand
	 * 
	 * @param aHand - a hand contain hole cards
	 * 
	 * @return ev
	 */
	public double getEV(UoAHand aHand) {
		return getEV(getStringCard(aHand.getCard(1), aHand.getCard(2)));
	}

	/**
	 * return the stored EV from the cards pass as argument.
	 * 
	 * @param cards - cards in preflop format (AA, AKs, ...)
	 * 
	 * @return the expected value
	 */
	public double getEV(String cards) {
		PreflopCards element = preflopCards.stream().filter(pfr -> pfr.getString("card").equals(cards)).findFirst()
				.get();
		double ev = ((int) (element.getDouble("ev") * 10000)) / 10000d;
		return ev;
	}
}
