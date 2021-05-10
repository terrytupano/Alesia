package plugins.hero.ICRReader;

import java.util.ArrayList;
import java.util.List;

/**
 * All of the information associated with a poker game played 
 *
 */
public class Game {

	public final String id;
	public final List<ICRCard> boardCards;
	public final List<Player> players;
	public final List<String> playersNameList;
	public final int potFlop;
	public final int potTurn;
	public final int potRiver;
	public final int potShowdown;

	public Game(String id, String potFlop, String potTurn, String potRiver, String potShowdown,
			List<ICRCard> boardCards) {
		this.id = id;
		this.potFlop = Integer.parseInt(potFlop);
		this.potTurn = Integer.parseInt(potTurn);
		this.potRiver = Integer.parseInt(potRiver);
		this.potShowdown = Integer.parseInt(potShowdown);
		this.boardCards = boardCards;
		this.players = new ArrayList<Player>();
		this.playersNameList = new ArrayList<String>();
	}

}
