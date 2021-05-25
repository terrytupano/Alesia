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

package plugins.hero.ozsoft.bots;

import java.util.*;

import plugins.hero.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.*;
import plugins.hero.ozsoft.actions.*;

/**
 * Basic Texas Hold'em poker bot. <br />
 * <br />
 * 
 * The current implementation acts purely on the bot's hole cards, based on the Chen formula, combined with a
 * configurable level of tightness (when to play or fold a hand ) and aggression (how much to bet or raise in case of
 * good cards or when bluffing). <br />
 * <br />
 * 
 * TODO:
 * <ul>
 * <li>Improve basic bot AI</li>
 * <li>bluffing</li>
 * <li>consider board cards</li>
 * <li>consider current bet</li>
 * <li>consider pot</li>
 * </ul>
 * 
 * @author Oscar Stigter
 */
public class BasicBot extends Bot {

	/** Tightness (0 = loose, 100 = tight). */
	// 8
	// 36
	private final int tightness;

	/** Betting aggression (0 = safe, 100 = aggressive). */
	private final int aggression;

	/** Table type. */
	private TableType tableType;

	/** The hole cards. */
	private UoAHand hand;

	public BasicBot() {
		this((int) (Math.random() * 100d), (int) (Math.random() * 100d));
	}
	/**
	 * Constructor.
	 * 
	 * @param tightness The bot's tightness (0 = loose, 100 = tight).
	 * @param aggression The bot's aggressiveness in betting (0 = careful, 100 = aggressive).
	 */
	public BasicBot(int tightness, int aggression) {
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
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		this.tableType = type;
	}

	@Override
	public void messageReceived(String message) {
		// Not implemented.
	}

	@Override
	public void handStarted(Player dealer) {
		hand = null;
	}

	@Override
	public void actorRotated(Player actor) {
		// Not implemented.
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerUpdated(Player player) {
		if (player.getHand().size() == NO_OF_HOLE_CARDS) {
			this.hand = player.getHand();
		}
	}

	@Override
	public void playerActed(Player player) {
		// Not implemented.
	}

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		PlayerAction action = null;
		if (allowedActions.size() == 1) {
			// No choice, must check.
			action = PlayerAction.CHECK;
		} else {
			double chenScore = Hero.getChenScore(hand);
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
}
