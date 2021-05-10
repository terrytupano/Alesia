package plugins.hero.ICRReader;

import java.util.*;

public class ICRCard implements Comparable<ICRCard> {

	public final String rank;
	public final String suit;
	public final String card;
	public ICRCard(String val) {
		this.rank = val.substring(0, 1);
		this.suit = val.substring(1, 2);
		this.card = rank + suit;
	}

	public static List<ICRCard> parseCards(String cards) {
		return parseCards(cards, -1);
	}
	/**
	 * parse the string representation and return a list of cards. the num argument control the numer of card to return.
	 * -1 retunr a list with all cards. 0 return empty list, 1 return a list only with the first cart and so on
	 * 
	 * @param cards - String of cards. <code>null</code> return a empty list
	 * @param num - num of card to consider.
	 * @return
	 */
	public static List<ICRCard> parseCards(String cards, int num) {
		ArrayList<ICRCard> rtnl = new ArrayList<>();
		if (cards == null)
			return rtnl;
		String[] cas = cards.split("[ ]");
		int num2 = num == -1 ? cas.length : num;
		for (int i = 0; i < num2; i++)
			rtnl.add(new ICRCard(cas[i]));
		return rtnl;
	}

	@Override
	public boolean equals(Object obj) {
		boolean eq = false;
		if (obj instanceof ICRCard) {
			ICRCard card = (ICRCard) obj;
			eq = rank.equals(card.rank) && suit.equals(card.suit);
		}
		return eq;
	}
	@Override
	public String toString() {
		return card;
	}

	@Override
	public int compareTo(ICRCard o) {
		return card.compareTo(o.card);
	}

}
