/*
 * Copyright (c) Chris 'MD87' Smith, 2007. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.cardgame.games;

import plugins.hero.UoAHandEval.*;
import plugins.hero.cardgame.*;

/**
 * Implements a standard (local) Texas Hold'em game.
 *
 * @author Chris
 */
public class TexasHoldEm extends AbstractGame {

	protected UoAHand community = new UoAHand();
	protected boolean doneFlop = false;
	protected boolean doneTurn = false;
	protected boolean doneRiver = false;

	public TexasHoldEm(final int numplayers, final int bigblind, final int ante, final int raises) {
		super(numplayers, bigblind, ante, raises);
		setTitle("TexasHoldEm");
		setDescription("Texas Holdem game simulation");
	}

	protected void dealPlayerCards() {
		dealCard(players.get((dealer + 1) % numplayers), false);
		dealCard(players.get((dealer + 1) % numplayers), false);
	}

	public UoAHand getCommunityCards() {
		return community;
	}

	public int holeCardCount() {
		return 2;
	}

	protected boolean canDoBringIns() {
		return true;
	}

	@Override
	protected Object doInBackground() throws Exception {
		while (countPlayers(true, false, false) > 1 && !isCancelled()) {
			notifyGameStateChanged("newGame", null, null);
			discardCards();
			shuffle();
			community = new UoAHand();
			doAntes();
			doBlinds();
			doneFlop = false;
			doneTurn = false;
			doneRiver = false;
			dealPlayerCards();
			waitForBets();

			if (countPlayers(true, true, false) > 1) {
				community.addCard(deck.deal());
				community.addCard(deck.deal());
				community.addCard(deck.deal());
				doBettingRound();
				doneFlop = true;
			}

			if (countPlayers(true, true, false) > 1) {
				community.addCard(deck.deal());
				doneTurn = true;
				doBettingRound();
			}

			if (countPlayers(true, true, false) > 1) {
				community.addCard(deck.deal());
				doneRiver = true;
				doBettingRound();
			}

			if (countPlayers(true, true, false) > 1) {
				doShowDown();
			} else {
				doWinner();
			}

			for (Player player : players) {
				if (player.getCash() <= 0) {
					player.setOut();
				}
			}

			notifyGameStateChanged("endGame", null, null);
			advanceDealerButton();
		}
		return null;
	}
}
