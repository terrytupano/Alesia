// This file is part of the 'texasholdem' project, an open source
// Texas Hold'em poker application written in Java.
//
// Copyright 2009 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package hero.ozsoft.bots;

import java.util.*;

import hero.*;
import hero.ozsoft.*;
import hero.ozsoft.actions.*;

/**
 * Basic Texas Hold'em poker bot. <br />
 * <br />
 * 
 * The current implementation acts purely on the bot's hole cards, based on the Chen formula, combined with a
 * configurable level of tightness (when to play or fold a hand ) and aggression (how much to bet or raise in case of
 * good cards or when bluffing). <br />
 * <br />
 * 
 * @author Oscar Stigter
 */
public class ChenBasicBot extends Bot {

	/** Tightness (0 = loose, 100 = tight). */
	private final int tightness;

	/** Betting aggression (0 = safe, 100 = aggressive). */
	private final int aggression;

	/** Table type. */
	private TableType tableType;

	public ChenBasicBot() {
		this.tightness = Table.getShuffleVariable();
		this.aggression = Table.getShuffleVariable();
	}
	
	/**
	 * Constructor.
	 * 
	 * @param tightness The bot's tightness (0 = loose, 100 = tight).
	 * @param aggression The bot's aggressiveness in betting (0 = careful, 100 = aggressive).
	 */
	public ChenBasicBot(int tightness, int aggression) {
		if (tightness < 0 || tightness > 100) {
			throw new IllegalArgumentException("Invalid tightness setting");
		}
		if (aggression < 0 || aggression > 100) {
			throw new IllegalArgumentException("Invalid aggression setting");
		}

		this.tightness = tightness; // 8
		this.aggression = aggression; // 36
	}

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		PlayerAction action = null;
		if (allowedActions.size() == 1) {
			// No choice, must check.
			action = PlayerAction.CHECK;
		} else {
			double chenScore = PokerSimulator.getChenScore(myHole);
			double chenScoreToPlay = tightness * 0.2;
			if ((chenScore < chenScoreToPlay)) {
				if (allowedActions.contains(PlayerAction.CHECK)) {
					// Always check for free if possible.
					action = PlayerAction.CHECK;
				} else {
					// Bad hole cards; play tight.
					action = PlayerAction.FOLD;
				}
			} else {
				// Good enough hole cards, play hand.
				if ((chenScore - chenScoreToPlay) >= ((20.0 - chenScoreToPlay) / 2.0)) {
					// Very good hole cards; bet or raise!
					if (aggression == 0) {
						// Never bet.
						if (allowedActions.contains(PlayerAction.CALL)) {
							action = PlayerAction.CALL;
						} else {
							action = PlayerAction.CHECK;
						}
					} else if (aggression == 100) {
						// Always go all-in!
						// FIXME: Check and bet/raise player's remaining cash.
						int amount = (tableType == TableType.FIXED_LIMIT) ? minBet : 100 * minBet;
						if (allowedActions.contains(PlayerAction.BET)) {
							action = new BetAction(amount);
						} else if (allowedActions.contains(PlayerAction.RAISE)) {
							action = new RaiseAction(amount);
						} else if (allowedActions.contains(PlayerAction.CALL)) {
							action = PlayerAction.CALL;
						} else {
							action = PlayerAction.CHECK;
						}
					} else {
						int amount = minBet;
						if (tableType == TableType.NO_LIMIT) {
							int betLevel = aggression / 20;
							for (int i = 0; i < betLevel; i++) {
								amount *= 2;
							}
						}
						if (currentBet < amount) {
							if (allowedActions.contains(PlayerAction.BET)) {
								action = new BetAction(amount);
							} else if (allowedActions.contains(PlayerAction.RAISE)) {
								action = new RaiseAction(amount);
							} else if (allowedActions.contains(PlayerAction.CALL)) {
								action = PlayerAction.CALL;
							} else {
								action = PlayerAction.CHECK;
							}
						} else {
							if (allowedActions.contains(PlayerAction.CALL)) {
								action = PlayerAction.CALL;
							} else {
								action = PlayerAction.CHECK;
							}
						}
					}
				} else {
					// Decent hole cards; check or call.
					if (allowedActions.contains(PlayerAction.CHECK)) {
						action = PlayerAction.CHECK;
					} else {
						action = PlayerAction.CALL;
					}
				}
			}
		}
		return action;
	}

	@Override
	public void handStarted(Player dealer) {
		// TODO Auto-generated method stub
		
	}
}
