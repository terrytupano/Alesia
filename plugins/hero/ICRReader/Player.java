package plugins.hero.ICRReader;

import java.util.*;

/**
 * This class represent the especific actions performed by a player in a especific game.
 * 
 * @author terry
 *
 */
public class Player {

	public final String name;
	public final String position;
	public final int playersLeft;
	public final int chipsCount, chipsBet, chipsWins;
	public final String flopActions;
	public final String turnActions;
	public final String riverActions;
	public final String showdownActions;
	public final List<ICRCard> handCards;
	
	public String boardCards;
	public int distance;
	public String handName;

	public Player(String name, String position, String playersLeft, String chips, String flopActions,
			String turnActions, String riverActions, String showdownActions, String bets, String wins,
			List<ICRCard> handCards) {
		this.name = name;
		this.position = position;
		this.playersLeft = Integer.parseInt(playersLeft);
		this.chipsCount = Integer.parseInt(chips);
		this.chipsBet = Integer.parseInt(bets);
		this.chipsWins = Integer.parseInt(wins);
		this.flopActions = flopActions;
		this.turnActions = turnActions;
		this.riverActions = riverActions;
		this.showdownActions = showdownActions;
		this.handCards = handCards;
	}

}
