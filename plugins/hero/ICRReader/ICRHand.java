package plugins.hero.ICRReader;

import java.util.*;

import plugins.hero.UoAHandEval.*;

/**
 * represetn a valid hand. a valid hand can be only hole cards, hole cards + Flop, river o turn
 * 
 * @author terry
 *
 */
public class ICRHand {

	private int handRank;
	private String handName;
	private UoAHand uoAHand;

	public ICRHand(String cards) {
		this.uoAHand = new UoAHand(cards);

		// todo: improve performan movien this 2 out of the constructor
		handRank = UoAHandEvaluator.rankHand(uoAHand);
		handName = UoAHandEvaluator.nameHand(uoAHand);
	}
	public ICRHand(List<ICRCard> cards) {
		this(getStringOf(cards));
	}

	public String handName() {
		return handName;
	}
	public int getSize() {
		return uoAHand.size();
	}

	public int getHandRank() {
		return handRank;
	}

	/**
	 * test if one or two of the cards in hole hand are co
	 * @param holeHand
	 * @param board
	 * @return
	 */
	public static boolean isPresent(String holeCards, List<ICRCard> list) {
		String[] hole  = holeCards.split("[ ]");
		String cards = list.toString();
		
//		todo: && temporal for test purpose: 
		return cards.contains(hole[0]) && cards.contains(hole[1]);
	}

	public static String getStringOf(List<ICRCard> cards) {
		StringBuffer sb = new StringBuffer();
		cards.forEach(c -> sb.append(c.toString() + " "));
		return sb.toString().trim();
	}

	@Override
	public String toString() {
		return uoAHand.toString();
	}
}
