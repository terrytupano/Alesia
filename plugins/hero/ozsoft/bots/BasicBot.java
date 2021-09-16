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

import org.apache.commons.math3.distribution.*;

import plugins.hero.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.*;
import plugins.hero.ozsoft.actions.*;
import plugins.hero.ozsoft.gui.*;

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

					/**
					 * in order to try to unify rank and EV en preflop (not expresed in 0,1 range else as a factor of
					 * wins) the upper bound of the base calculation will now be a factor from the top. the idea is
					 * after certain point, the rank will be > 1. in original preflop distribution 39 of 168 (23,21%)
					 * preflopgrups haven ev>0
					 */
					// a Ace High Straight Flush
					// double baseRank = 2970356d;

					// 23,21% of a Ace High Straight Flush
					double baseRank = 2280937d;

					// a Full House, Twos over Threes
					// double baseRank = 2227759d;

					// baseRange to comparation: Three of a Kind, Eights
					// int baseRank = 1115012;
					double r = (double) UoAHandEvaluator.rankHand(hand);
					double evHand = r / baseRank;
					int S = -1;
					S = hand.size() == 2 ? 2 : S; // pre-flop
					S = hand.size() == 5 ? 3 : S; // flop
					S = hand.size() == 6 ? 3 : S; // turn
					S = hand.size() == 7 ? 1 : S; // river

					// TODO: check Poker Expected Value (EV) Formula: EV = (%W * $W) – (%L * $L)
					// https://www.splitsuit.com/simple-poker-expected-value-formula

					/**
					 * danger implementation allow Hero to leave the battle based on the risck of the current hand. this
					 * implementation is linked whit agresion. meaning: more agresion, less care of posible danger.
					 */
					double cash = (double) player.getCash();
					double q = (Double) Hero.getUoAEvaluation(myHole.toString(), communityHand.toString())
							.getOrDefault("2BetterThanMinePercent", 0.0);
					q = q / 100;
					double p = 1 - q;
					double cashInDanger = cash * q;

					/**
					 * pp implementation: this constant muss expres th number of BB allow to pull or pusch
					 * implementation. (maxx recon ammunitions). this implemntation read th EV value from
					 * preflopdistribution and compute the inverse of danger (positive EV, hat -danger value). diferenty
					 * of normal danger after preflop. preflop danger is incremental. meaning every previous call/bet
					 * increaase the chanse of Fold
					 */

					/**
					 * ppMax represent the max number of ammo allow to expend to see the flop- when the preflop is good,
					 * this allos multiples call/reise
					 */
					int ppMax = 10 * bigBlind;

					/**
					 * ppBase is a minimun allow to see the preflop. whe the ppMax * ev value are negative but not so
					 * far, ppBase allow Hero to call a number of bb calls in order to se the flop. when another villa,
					 * try to steal the pot, the the value of the equation is so negative thtat hero Fold
					 */
					int ppBase = 1 * bigBlind;
					if (hand.size() == 2) {
						evHand = preflopCardsModel.getEV(myHole);
						to implemetn Kelly remove preflop restrictions 
>>>>						cash = ppBase + ppMax * evHand;
						cashInDanger = matchCost - cash;
						q = 0.75;
						p = 1 - q;
					}

					double ammo = (alpha / 50.0) * cash * (p - q / (pot + cash - cashInDanger));

					// if amunition control equation is less that minimun bet, Fold
					// if (hand.size() > 2 && minBet > a) {

//					double c = evHand * ammo - minBet;
//					if (minBet > ammo || c < 0) {
					if (minBet > ammo) {
						return PlayerAction.FOLD;
					}

//					TriangularDistribution td = new TriangularDistribution((double) minBet, c, ammo);
//					int amount = (int) td.sample();
					int amount = (int) (Math.random() * ammo);

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
