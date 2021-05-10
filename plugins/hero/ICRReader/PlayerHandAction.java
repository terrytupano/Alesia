package plugins.hero.ICRReader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PlayerHandAction {
	
	final List<ICRCard> handCards = new ArrayList<ICRCard>();
	final List<ICRCard> boardCards = new ArrayList<ICRCard>();
	final BigDecimal potSize;
	final BigDecimal avgChipCount;
	final BigDecimal chipCount;
	final String numPlayers;
	final String position;
	final String action;
	
	public PlayerHandAction(List<ICRCard> handCards, List<ICRCard> boardCards, BigDecimal potSize, String numPlayers, String position, BigDecimal avgChipCount, BigDecimal chipCount, String action) {
		this.potSize = potSize;
		this.handCards.addAll(handCards);
		this.boardCards.addAll(boardCards);
		this.numPlayers = numPlayers;
		this.position = position;
		this.action = action;
		this.avgChipCount = avgChipCount;
		this.chipCount = chipCount;
	}
	
}
