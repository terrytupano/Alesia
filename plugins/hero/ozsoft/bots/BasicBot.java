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

import org.javalite.activejdbc.*;

import core.datasource.model.*;
import plugins.hero.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.*;
import plugins.hero.ozsoft.actions.*;

/**
 * Auto mutable Alpha and Tau paremeters bot.
 * 
 * Current implementation acts purely on the bot's hole cards, based on <code>Tau</code> parameter of Original preflop
 * dsitribution. and <code>alpha</code> mutable parameter selection. Only <b>hero</b> ist allow to mutate every 100
 * hands. All other players will be randomly created with random parameters.
 * 
 * <li>combined with a configurable level of tightness (when to play or fold a hand ) and aggression (how much to bet or
 * raise in case of good cards or when bluffing). <br />
 * <br />
 * </ul>
 * 
 * TODO:
 * <ul>
 * <li>measurement of bad-luck biorhitmus</li>
 * <li>measurement of bad-luck biorhitmus</li>
 * 
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

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		PlayerAction action = null;
		if (allowedActions.size() == 1) {
			// No choice, must check.
			action = PlayerAction.CHECK;
		} else {
			// check hole card. NOT in tau range
			if (!preflopCardsModel.containsHand(myHole)) {
				if (allowedActions.contains(PlayerAction.CHECK)) {
					// Always check for free if possible.
					action = PlayerAction.CHECK;
				} else {
					// Bad hole cards; play tight.
					action = PlayerAction.FOLD;
				}
			} else {
				// range in tau. Bet or raise!
				if (alpha == 0) {
					// Never bet.
					if (allowedActions.contains(PlayerAction.CALL)) {
						action = PlayerAction.CALL;
					} else {
						action = PlayerAction.CHECK;
					}
				} else if (alpha == 100) {
					// Always go all-in!
					int amount = 100 * minBet;
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
					// ------------------
					// baseRange to comparation: a Ace High Straight Flush
					double baseRank = 2970356d;
					// baseRange to comparation: Three of a Kind, Eights
					// int baseRank = 1115012;
					double rank = (double) UoAHandEvaluator.rankHand(hand);
					double preAlpha = rank / baseRank;

					// TODO: check Poker Expected Value (EV) Formula: EV = (%W * $W) – (%L * $L)
					// https://www.splitsuit.com/simple-poker-expected-value-formula

					/**
					 * danger implementation allow Hero to leave the battle based on the risck of the current hand. this
					 * implementation is linked whit agresion. meaning: more agresion, less care of posible danger.
					 */
					cash = player.getCash();
					double danger = (Double) Hero.getUoAEvaluation(myHole.toString(), communityHand.toString())
							.getOrDefault("2BetterThanMinePercent", 0.0);
					danger = danger / 100;
					double cashToDanger = cash * danger;

					/**
					 * pp implementation: this constant muss expres th number of BB allow to pull or pusch
					 * implementation. (maxx recon ammunitions). this implemntation read th EV value from
					 * preflopdistribution and compute the inverse of danger (positive EV, hat -danger value). diferenty
					 * of normal danger after preflop. preflop danger is incremental. meaning every previous call/bet
					 * increaase the chanse of Fold
					 */
					int pp = 10;
					if (hand.size() == 2) {
						double ev = preflopCardsModel.getEV(myHole);
						double base = (pp * bigBlind) * ev;
						cashToDanger = matchCost - base;
					}

					double a = ((alpha / 50.0) * preAlpha * cash) - cashToDanger;

					// simulation of triangular distribution: random Value from minBet to max allow for hand rank.
					int amount = (int) (Math.random() * a);

					// street check. if str >= FLOP and the minBet represent more that ranck factor, Fold
					if (hand.size() > 2 && minBet > a) {
						return PlayerAction.FOLD;
					}

					amount = amount < minBet ? minBet : amount;
					// ------------------

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
			}
		}
		return action;
	}

	@Override
	public void actorRotated(Player actor) {
		// Not implemented.
	}
}
