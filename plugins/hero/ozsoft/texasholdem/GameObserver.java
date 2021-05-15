/*
 * Copyright (c) Chris 'MD87' Smith, 2007-2008. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.ozsoft.texasholdem;

import core.*;
import plugins.hero.UoAHandEval.*;

/**
 * The main interface for objects that wish to observe game events as they happen.
 * 
 * @author Chris
 */
public interface GameObserver {

	/**
	 * main metho for game change notification. the state value are:
	 * <li>newGame. Called at the start of a game. Value null
	 * <li>cardDealt: Called when a card is dealt to the specified player. Value= {@link TEntry}<{@link Player},
	 * {@link UoACard}>
	 * <li>endGame: Called at the end of a game. Value null
	 * <li>playerTurn: Notifies all observers that it is the specified player's turn.
	 * <li>winnerinner: Notifies all observers that the specified player is a winner.
	 * <li>communityCardsUpdated: Called when the community cards have been updated.
	 * <li>action_<action>: represent the action that the player has performed
	 * 
	 * @param state
	 * @param value
	 */
	void gameStateChanged(final String state, Player player, Object value);
}
