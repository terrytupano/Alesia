package plugins.hero.ICRReader;

import java.util.*;

import plugins.hero.UoAHandEval.*;

/**
 * represetn a valid hand. a valid hand can be only hole cards, hole cards + Flop, river o turn
 * @author terry
 *
 */
public class ICRHand {

	private int handRank;
	private String handName;
	private UoAHand uoAHand;
	
	public ICRHand(String cards) {
		this.uoAHand = new UoAHand(cards);
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
