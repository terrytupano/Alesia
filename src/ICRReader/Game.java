package ICRReader;

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
	public final int potPreFlop;
	public final int potFlop;
	public final int potTurn;
	public final int potRiver;

	public boolean delete;

	public final int playersPreFlop, playersFlop, playersTurn, playersRiver;

	public Game(String id, String playersPreFlop, String potPreFlop, String playersFlop, String potFlop, String playersTurn, String potTurn, String playersRiver,String potRiver,
			List<ICRCard> boardCards) {
		this.id = id;
		this.playersPreFlop = Integer.parseInt(playersPreFlop);
		this.potPreFlop = Integer.parseInt(potPreFlop);
		this.playersFlop = Integer.parseInt(playersFlop);
		this.potFlop = Integer.parseInt(potFlop);
		this.playersTurn = Integer.parseInt(playersTurn);
		this.potTurn = Integer.parseInt(potTurn);
		this.playersRiver = Integer.parseInt(playersRiver);
		this.potRiver = Integer.parseInt(potRiver);
		this.boardCards = boardCards;
		this.players = new ArrayList<Player>();
		this.playersNameList = new ArrayList<String>();
	}

}
