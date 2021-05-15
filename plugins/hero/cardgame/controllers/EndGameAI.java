/*
 * Copyright (c) Chris 'MD87' Smith, 2007. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.cardgame.controllers;

import plugins.hero.UoAHandEval.*;
import plugins.hero.cardgame.Player.*;
import plugins.hero.cardgame.interfaces.*;

/**
 *
 * @author Chris
 */
public class EndGameAI extends ConservativeOpener {

	private boolean shouldBluff;
	private boolean shouldFold;
	private boolean endGame = false;
	private double myLevel = 0;
	// private final Map<String, Integer> handRanks = new HashMap<String, Integer>();

	public EndGameAI() {

	}

	public void communityCardsUpdated() {
		UoAHand hand = game.getCommunityCards();
		if (hand.size() == 7) {
			endGame = true;
			shouldFold = false;

			// TODO: this player must implemet up and down from handran: bet wen up, fold wen down
			// similar hero filter operation

			// final int myFreq = handRanks.get(bestHand.getBestRank());
			double betterFreq = 0;
			double totalFreq = 0;

			// for (StandardHand.Ranking ranking : StandardHand.Ranking.values()) {
			// if (ranking.ordinal() < bestHand.getBestRank().ordinal()) {
			// betterFreq += handRanks.get(ranking);
			// }
			// totalFreq += handRanks.get(ranking);
			// }

			// 0 <= score < 1
			// Lower the better
			double score = betterFreq / totalFreq;

			if (score > myLevel) {
				shouldFold = true;
			}
		}
	}

	@Override
	public CallRaiseFold doCallRaiseFold(int callAmount, boolean canRaise) {
		if (endGame) {
			if (canRaise && (shouldBluff || (!shouldFold && Math.random() > 0.5))) {
				return CallRaiseFold.RAISE;
			} else if (shouldFold) {
				return CallRaiseFold.FOLD;
			} else {
				return CallRaiseFold.CALL;
			}
		} else {
			return super.doCallRaiseFold(callAmount, canRaise);
		}
	}

	@Override
	public OpenCheck doOpenCheck() {
		return super.doOpenCheck();
	}

	@Override
	public int getRaise(int minimum) {
		if (endGame) {
			if (shouldBluff) {
				return minimum + (int) (Math.random() * (player.getCash() - minimum) / 2);
			} else {
				return minimum + (int) (Math.random() * (player.getCash() - minimum));
			}
		} else {
			return super.getRaise(minimum);
		}
	}

	public void newGame() {
		shouldBluff = Math.random() > 0.7;
		myLevel = Math.random();
		endGame = false;
	}

	@Override
	public void setGame(Game game) {
		// super.setGame(game);
		// game.registerObserver(this);

		// handRanks.put(StandardHand.Ranking.FLUSH, 4047644);
		// handRanks.put(StandardHand.Ranking.FOUR_OF_A_KIND, 224848);
		// handRanks.put(StandardHand.Ranking.FULL_HOUSE, 3473184);
		// handRanks.put(StandardHand.Ranking.NO_PAIR, 23294460);
		// handRanks.put(StandardHand.Ranking.ONE_PAIR, 58627800);
		// handRanks.put(StandardHand.Ranking.STRAIGHT, 6180020);
		// handRanks.put(StandardHand.Ranking.STRAIGHT_FLUSH, 41584);
		// handRanks.put(StandardHand.Ranking.THREE_OF_A_KIND, 6461620);
		// handRanks.put(StandardHand.Ranking.TWO_PAIR, 31433400);
	}
}
