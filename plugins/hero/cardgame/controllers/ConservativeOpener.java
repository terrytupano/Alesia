/*
 * Copyright (c) Chris 'MD87' Smith, 2007. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.cardgame.controllers;

import plugins.hero.cardgame.Player.*;

public class ConservativeOpener extends RandomPlayer {

	@Override
	public CallRaiseFold doCallRaiseFold(int callAmount, boolean canRaise) {

		// TODO: this player mis implement the preflop table in pokerentleiter

		// if (c1.getSuit() == c2.getSuit() || Math.abs(c1.getRank().compareTo(c2.getRank())) < 2) {
		// return super.doCallRaiseFold(callAmount, canRaise);
		// } else {
		return CallRaiseFold.FOLD;
		// }
	}
}
