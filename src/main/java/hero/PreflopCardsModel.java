package hero;

import java.util.*;

import org.javalite.activejdbc.*;

import com.jgoodies.common.base.*;

import core.*;
import datasource.*;
import hero.UoAHandEval.*;

/**
 * Represents a range of hands that a player can have. Its purpose is to work
 * with Texas Hold'em games only. A range is represented by the percentage of
 * hand types that it contains. There are 169 hand types in Texas Hold'em. You
 * can include any one of those 169 hand types in the range, even if the range
 * wouldn't normally support it. Setting a specific percentage to a range will
 * include a set of predefined hand types in it and eliminate all previously
 * included types. Because of this, it's recommended to set the range to a
 * specific percentage and then, if needed, make tiny adjustments to it. The
 * methods within this class will document this behaviour in more detail.
 * 
 * @author Radu Murzea
 */
public class PreflopCardsModel {
	/**
	 * Contains the names of all hand types. They are split among 13 lines and 13
	 * columns. In order to work with them easier, there are some patterns to their
	 * arrangements. For example: the pocket pairs run along the main diagonal.
	 * Also, all suited types are above this diagonal while the offsuit types are
	 * found below.
	 */
	private static final String[][] rangeNames = {
			{ "AA", "AKs", "AQs", "AJs", "ATs", "A9s", "A8s", "A7s", "A6s", "A5s", "A4s", "A3s", "A2s" },
			{ "AKo", "KK", "KQs", "KJs", "KTs", "K9s", "K8s", "K7s", "K6s", "K5s", "K4s", "K3s", "K2s" },
			{ "AQo", "KQo", "QQ", "QJs", "QTs", "Q9s", "Q8s", "Q7s", "Q6s", "Q5s", "Q4s", "Q3s", "Q2s" },
			{ "AJo", "KJo", "QJo", "JJ", "JTs", "J9s", "J8s", "J7s", "J6s", "J5s", "J4s", "J3s", "J2s" },
			{ "ATo", "KTo", "QTo", "JTo", "TT", "T9s", "T8s", "T7s", "T6s", "T5s", "T4s", "T3s", "T2s" },
			{ "A9o", "K9o", "Q9o", "J9o", "T9o", "99", "98s", "97s", "96s", "95s", "94s", "93s", "92s" },
			{ "A8o", "K8o", "Q8o", "J8o", "T8o", "98o", "88", "87s", "86s", "85s", "84s", "83s", "82s" },
			{ "A7o", "K7o", "Q7o", "J7o", "T7o", "97o", "87o", "77", "76s", "75s", "74s", "73s", "72s" },
			{ "A6o", "K6o", "Q6o", "J6o", "T6o", "96o", "86o", "76o", "66", "65s", "64s", "63s", "62s" },
			{ "A5o", "K5o", "Q5o", "J5o", "T5o", "95o", "85o", "75o", "65o", "55", "54s", "53s", "52s" },
			{ "A4o", "K4o", "Q4o", "J4o", "T4o", "94o", "84o", "74o", "64o", "54o", "44", "43s", "42s" },
			{ "A3o", "K3o", "Q3o", "J3o", "T3o", "93o", "83o", "73o", "63o", "53o", "43o", "33", "32s" },
			{ "A2o", "K2o", "Q2o", "J2o", "T2o", "92o", "82o", "72o", "62o", "52o", "42o", "32o", "22" } };

	private int percentage;
	private String rangeName;
	private LazyList<PreflopCards> preflopCards;
	private double upperBound, delta;

	/**
	 * new instance loaded with the values settled in <b>pokerStar<b> range name
	 */
	public PreflopCardsModel() {
		this("pokerStar");
	}

	public PreflopCardsModel(String rangeName) {
		this(rangeName, false);
	}

	/**
	 * new instance. the argument rangeName is name of a new or existent preflop
	 * rage.
	 * 
	 * @param rangeName - the name to load or create
	 * @param create    - <code>true</code> to create a new preflop card
	 *                  distribution based on pokerStar
	 */
	public PreflopCardsModel(String rangeName, boolean create) {
		this.rangeName = rangeName;
		this.preflopCards = PreflopCards.where("rangeName = '" + rangeName + "' ORDER BY ev DESC");
		Preconditions.checkArgument(!create && !preflopCards.isEmpty(), "Range name " + rangeName + " Not found.");

		if (create) {
			this.preflopCards = PreflopCards.where("rangeName = 'pokerStar' ORDER BY ev DESC");
			preflopCards.forEach(card -> card.set("rangeName", rangeName, "description", rangeName, "percentage", 0,
					"selected", false, "wins", 0, "hands", 0, "ev", 0.0));
		}

		this.percentage = preflopCards.get(0).getInteger("percentage");
		upperBound = preflopCards.stream().mapToDouble(pfc -> pfc.getDouble("ev")).max().getAsDouble();
		// delta with inverted sing
		delta = -1.0 * preflopCards.stream().mapToDouble(pfc -> pfc.getDouble("ev")).min().getAsDouble();
		// function displacement
		upperBound = upperBound + delta;
	}

	/**
	 * load from the DB the list of saved preflop cards selections
	 * 
	 * @return list whit the names
	 */
	public static List<TSEntry> getPreflopList() {
		ArrayList<TSEntry> names = new ArrayList<>();
		LazyList<PreflopCards> ranges = PreflopCards.find("card = ?", "AA");
		for (PreflopCards range : ranges) {
			TSEntry te = new TSEntry(range.getString("rangeName"), range.getString("description"));
			if (!names.contains(te))
				names.add(te);
		}
		return names;
	}

	/**
	 * Tells if the hand composed of the two specified cards is selected in this
	 * range. The order in which you specify the cards is not relevant.
	 * 
	 * @param hand 2 cards hand
	 * 
	 * @return true if the specified hand is selected in this range, false
	 *         otherwise.
	 */
	public boolean containsHand(UoAHand hand) {
		String card = getStringCard(hand);
		return isSelected(card);
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
	 * @param row    - x coordinates
	 * @param column - y coordinate
	 * 
	 * @return the string representation
	 */
	public String getCardAt(int row, int column) {
		return rangeNames[row][column];
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
		double ev = element.getDouble("ev");
		return ev;
	}

	public double getEV(UoAHand hand) {
		return getEV(getStringCard(hand));
	}

	/**
	 * perform {@link #getNormalizedEV(UoAHand)}
	 * 
	 * @see #getNormalizedEV(String)
	 * 
	 */
	public double getNormalizedEV(UoAHand hand) {
		return getNormalizedEV(getStringCard(hand));
	}

	/**
	 * this metod will return the Normalized EV.
	 * 
	 * @param cards - cards in {@link PreflopCardsModel} format
	 * 
	 * @return EV in [0,1] range
	 */
	public double getNormalizedEV(String cards) {
		// double nev = PokerSimulator.lowerBound;
		double ev = getEV(cards);
		double nev = (ev + delta) / upperBound;
		// System.out.println("PreflopCardsModel.getNormalizedEV()");
		return nev;
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
	 * return the card representation in this context. for example, when the hand is
	 * <code>Ah Kh</code> , this method will return <code>AKs</code>
	 * 
	 * @param card1 - the first card
	 * @param card2 - the second cards
	 * 
	 * @return the string representation
	 */
	public static String getStringCard(UoAHand hand) {
		int rbig, rsmall;
		UoACard card1 = hand.getCard(1);
		UoACard card2 = hand.getCard(2);
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
	 * @param row    the row where the card type is found
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
	 * @param rangeName   - Name of the card range
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
	 * Sets a new percentage for this range. this method select only the card whose
	 * EV is inside of the percentage area
	 * 
	 * @param newPercentage the new percentage.
	 * @throws IllegalArgumentException if the new percentage is below 0 or over 100
	 */
	public void setPercentage(int newPercentage) {
		Preconditions.checkArgument(newPercentage >= 0 && newPercentage <= 100,
				"percentage value must be between 0 and 100 inclusively");

		// clear all area.
		preflopCards.forEach(pfr -> pfr.setBoolean("selected", false));
		this.percentage = newPercentage;
		int set = (int) Math.round(preflopCards.size() * percentage / 100d);
		for (int i = 0; i < set; i++)
			preflopCards.get(i).setBoolean("selected", true);
	}

	/**
	 * used for statistics purpose. this method update the <code>winnings</code> and
	 * <code>ev</code> fields preflopscards file.
	 * 
	 * @param card1   - preflop card 1
	 * @param card2   - preflop card 2
	 * @param ammount - normaly +1 or -1 to update the count and ev
	 */
	public void updateCoordenates(UoAHand hand, int ammount) {
		String card = getStringCard(hand);
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
}
