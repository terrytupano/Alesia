
package plugins.hero.cardgame.controllers;

import plugins.hero.cardgame.*;
import plugins.hero.cardgame.Player.*;
import plugins.hero.cardgame.interfaces.*;

/**
 * A computer-controlled player that choses what to do randomly.
 * 
 * @author Chris
 */
public class RandomPlayer implements PlayerController {

	/** The player that we're controlling. */
	protected Player player;

	/** The game that we're playing. */
	protected Game game;

	/** {@inheritDoc} */
	@Override
	public CallRaiseFold doCallRaiseFold(final int callAmount, final boolean canRaise) {
		if (Math.random() < 0.3) {
			if (player.getCash() - callAmount < game.getBigBlind() || !canRaise) {
				return CallRaiseFold.FOLD;
			} else {
				return CallRaiseFold.RAISE;
			}
		} else {
			return CallRaiseFold.CALL;
		}
	}

	/** {@inheritDoc} */
	@Override
	public OpenCheck doOpenCheck() {
		if (Math.random() < 0.5 || player.getCash() < game.getBigBlind()) {
			return OpenCheck.CHECK;
		} else {
			return OpenCheck.OPEN;
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean shouldShowCards() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void setPlayer(final Player player) {
		this.player = player;
	}

	/** {@inheritDoc} */
	@Override
	public void setGame(final Game game) {
		this.game = game;
	}
	
	public RandomPlayer() {

	}

	/** {@inheritDoc} */
	@Override
	public int getRaise(final int minimum) {
		return minimum;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isLocalHuman() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Deck discardCards(final int minimum, final int maximum) {
		return new Deck(player.getCards().subList(0, minimum));
	}

}
