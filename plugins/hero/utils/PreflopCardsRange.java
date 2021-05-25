package plugins.hero.utils;

import java.awt.*;
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
public class PreflopCardsRange {
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

	// this field defines how the card types of the range progress as the range percentage grows and shrinks
	private static final Point[][] rangeProgressions = {{}, {new Point(0, 0), new Point(1, 1), new Point(2, 2)},
			{new Point(3, 3), new Point(4, 4)}, {new Point(0, 1), new Point(5, 5)}, {new Point(1, 0), new Point(0, 2)},
			{new Point(1, 2), new Point(0, 3), new Point(6, 6)}, {new Point(2, 0), new Point(0, 4)},
			{new Point(1, 3), new Point(1, 4)}, {new Point(3, 0), new Point(2, 3)}, {new Point(2, 1)},
			{new Point(0, 5), new Point(2, 4), new Point(7, 7)}, {new Point(4, 0)}, {new Point(3, 1), new Point(3, 4)},
			{new Point(0, 6), new Point(1, 5), new Point(3, 2)}, {new Point(0, 7)}, {new Point(4, 1)},
			{new Point(0, 9), new Point(2, 5), new Point(8, 8)}, {new Point(4, 2), new Point(0, 8)},
			{new Point(5, 0), new Point(3, 5)}, {new Point(0, 10), new Point(4, 5)}, {new Point(4, 3), new Point(1, 6)},
			{new Point(1, 7), new Point(6, 0)}, {new Point(2, 6), new Point(0, 11)}, {new Point(5, 1)},
			{new Point(0, 12), new Point(1, 8), new Point(3, 6), new Point(4, 6)}, {new Point(7, 0)},
			{new Point(5, 2), new Point(9, 9)}, {new Point(1, 9), new Point(5, 6)}, {new Point(2, 7), new Point(5, 3)},
			{new Point(9, 0)}, {new Point(5, 4)}, {new Point(8, 0)}, {new Point(6, 1), new Point(1, 10)},
			{new Point(2, 8), new Point(3, 7), new Point(4, 7)}, {new Point(10, 0)},
			{new Point(1, 11), new Point(2, 9), new Point(5, 7), new Point(6, 7)}, {new Point(7, 1)},
			{new Point(6, 2), new Point(10, 10)}, {new Point(11, 0)}, {new Point(1, 12), new Point(6, 3)},
			{new Point(2, 10), new Point(6, 4)}, {new Point(3, 8)}, {new Point(8, 1)},
			{new Point(4, 8), new Point(12, 0)}, {new Point(6, 5)}, {new Point(5, 8), new Point(6, 8), new Point(7, 8)},
			{new Point(2, 11), new Point(3, 9), new Point(9, 1)}, {new Point(7, 2)},
			{new Point(2, 12), new Point(3, 10)}, {new Point(8, 9), new Point(7, 3), new Point(11, 11)},
			{new Point(7, 4)}, {new Point(10, 1)}, {new Point(4, 9), new Point(7, 9)}, {new Point(8, 2)},
			{new Point(3, 11), new Point(5, 9), new Point(7, 6)}, {new Point(6, 9), new Point(7, 5)},
			{new Point(4, 10)}, {new Point(11, 1)}, {new Point(3, 12), new Point(9, 2), new Point(9, 10)},
			{new Point(4, 11), new Point(8, 10)}, {new Point(12, 1), new Point(12, 12)},
			{new Point(7, 10), new Point(8, 7)}, {new Point(4, 12), new Point(10, 2)}, {new Point(8, 3)},
			{new Point(6, 10)}, {new Point(5, 10), new Point(8, 6)}, {new Point(8, 4)}, {new Point(8, 5)},
			{new Point(5, 11), new Point(9, 11), new Point(11, 2)}, {new Point(9, 3)},
			{new Point(8, 11), new Point(10, 11)}, {new Point(5, 12), new Point(7, 11), new Point(9, 8)},
			{new Point(12, 2)}, {new Point(10, 3)}, {new Point(6, 11), new Point(9, 7)},
			{new Point(9, 12), new Point(9, 6)}, {new Point(6, 12)}, {new Point(9, 4)},
			{new Point(9, 5), new Point(11, 3)}, {new Point(8, 12)}, {new Point(10, 9)},
			{new Point(10, 4), new Point(10, 12)}, {new Point(12, 3)}, {new Point(7, 12), new Point(10, 8)},
			{new Point(11, 4)}, {new Point(11, 12), new Point(10, 7)}, {new Point(10, 6)}, {new Point(12, 4)},
			{new Point(10, 5)}, {new Point(11, 9)}, {new Point(11, 5)}, {new Point(11, 8), new Point(11, 10)},
			{new Point(12, 5)}, {new Point(11, 7)}, {new Point(11, 6)}, {new Point(12, 9)}, {new Point(12, 6)},
			{new Point(12, 10)}, {new Point(12, 8)}, {new Point(12, 7)}, {new Point(12, 11)}};

	private int percentage;
	private int[][] winnCounts = new int[13][13];

	// contains the set of cards selected for this range.
	// initially, none are selected (0 % range)
	private final boolean[][] rangeSelections = {
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},
			{false, false, false, false, false, false, false, false, false, false, false, false, false},};

	/**
	 * Constructs a range of 0 % hands.
	 */
	public PreflopCardsRange() {
		this(0);
	}

	public void addCount(int row, int column, int count) {
		winnCounts[row][column] = winnCounts[row][column] + count;
	}
	/**
	 * Constructs a range with the specified percentage.
	 * 
	 * @param percentage the percentage of the range.
	 * @throws IllegalArgumentException if the percentage is below 0 or over 100
	 */
	public PreflopCardsRange(int percentage) {
		setNewPercentage(percentage);
	}

	/**
	 * Changes the specified card type (found at the row and column specified) to the specified value.
	 * 
	 * @param row the row where the card type is found
	 * @param column the column where the card type is found
	 * @param newValue the new value of the card type (true for enabled, false for disabled).
	 */
	public void changeValue(int row, int column, boolean newValue) {
		this.rangeSelections[row][column] = newValue;
	}

	/**
	 * Reverts the value of the specified card type: if it's true, then it will become false and vice-versa.
	 * 
	 * @param row the row where the card type is found
	 * @param column the column where the card type is found
	 */
	public void flipValue(int row, int column) {
		this.rangeSelections[row][column] ^= true;
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
	 * Tells you if a card type is selected in this range or not
	 * 
	 * @param row the row where the card type is found
	 * @param column the column where the card type is found
	 * @return true if the card type is selected, false otherwise.
	 */
	public boolean getValue(int row, int column) {
		return this.rangeSelections[row][column];
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
	 * Sets a new percentage for this range. This will overwrite all the card type selections.
	 * 
	 * @param newPercentage the new percentage.
	 * @throws IllegalArgumentException if the new percentage is below 0 or over 100
	 */
	public void setNewPercentage(int newPercentage) {
		if (newPercentage < 0 || newPercentage > 100) {
			throw new IllegalArgumentException("percentage value must be between 0 and 100 inclusively");
		}

		for (int i = 1; i <= newPercentage; i++) {
			for (Point c : PreflopCardsRange.rangeProgressions[i]) {
				this.rangeSelections[c.x][c.y] = true;
			}
		}

		for (int i = newPercentage + 1; i < PreflopCardsRange.rangeProgressions.length; i++) {
			for (Point c : PreflopCardsRange.rangeProgressions[i]) {
				this.rangeSelections[c.x][c.y] = false;
			}
		}

		this.percentage = newPercentage;
	}

	/**
	 * create and return an instance with all data stored in the database.
	 * 
	 * @param rangeName - name of the stored range
	 * 
	 * @return a ready to use {@link PreflopCardsRange}
	 */
	public static PreflopCardsRange loadFromDB(String rangeName) {
		PreflopCardsRange cardsRange = new PreflopCardsRange();
		LazyList<PreflopRange> ranges = PreflopRange.find("rangeName = ?", rangeName);
		for (PreflopRange range : ranges) {
			// set the percentage only once
			if (cardsRange.getPercentage() == 0)
				cardsRange.setNewPercentage(range.getInteger("percentage"));
			cardsRange.rangeSelections[range.getInteger("y")][range.getInteger("x")] = range.getBoolean("selected");
			cardsRange.winnCounts[range.getInteger("y")][range.getInteger("x")] = range.getInteger("winCount");
		}
		return cardsRange;
	}

	/**
	 * load from the DB the list of saved preflop cards selections
	 * 
	 * @return list whit the names
	 */
	public static TEntry<String, String>[] getSavedCardsRanges() {
		ArrayList<TEntry<String, String>> names = new ArrayList<>();
		LazyList<PreflopRange> ranges = PreflopRange.findAll();
		for (PreflopRange range : ranges) {
			TEntry<String, String> te = new TEntry<>(range.getString("rangeName"), range.getString("description"));
			if (!names.contains(te))
				names.add(te);
		}
		return names.toArray(new TEntry[0]);
	}
	/**
	 * save the current preflop card selection in the database
	 * 
	 * @param rangeName - the name for this range
	 */
	public void saveInDB(String rangeName, String description) {
		PreflopRange.delete("rangename = ?", rangeName);
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				// PreflopRange range = PreflopRange.create("rangename", rangeName);
				PreflopRange range = new PreflopRange();
				range.set("rangeName", rangeName);
				range.set("description", description);
				range.set("percentage", getPercentage());
				range.set("card", getCardAt(i, j));
				range.set("x", j);
				range.set("y", i);
				range.set("selected", getValue(i, j));
				range.set("winCount", winnCounts[i][j]);
				range.insert();
			}
		}

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

	public Point getCoordenates(UoACard card1, UoACard card2) {
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
			row = column = 14 - rbig;
		} else {
			// above the main diagonal
			if (card1.getSuit() == card2.getSuit()) {
				row = 14 - rbig;
				column = 14 - rsmall;
				// below the main diagonal
			} else {
				row = 14 - rsmall;
				column = 14 - rbig;
			}
		}
		return new Point(column, row);
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
		Point coord = getCoordenates(c1, c2);
		return this.rangeSelections[coord.y][coord.x];
	}
}
