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

		// No choice, must check.
		if (allowedActions.size() == 1)
			return PlayerAction.CHECK;

		// Bad hole cards; play tight. SEE tau parameter
		if (!preflopCardsModel.containsHand(myHole)) {
			return PlayerAction.FOLD;
		}

		/**
		 * in order to try to unify rank and EV en preflop (not expresed in 0,1 range else as a factor of wins) the
		 * upper bound of the base calculation will now be a factor from the top. the idea is after certain point, the
		 * rank will be > 1. in original preflop distribution 39 of 168 (23,21%) preflopgrups haven ev>0
		 */
		// a Ace High Straight Flush
		// double baseRank = 2970356d;

		// 23,21% of a Ace High Straight Flush
		double baseRank = 2280937d;

		// low base rank to allow simulation: Three of a Kind, Aces
		// double baseRank = 1116018d;
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

		double cash = (double) player.getCash();
		double q = (Double) Hero.getUoAEvaluation(myHole.toString(), communityHand.toString())
				.getOrDefault("2BetterThanMinePercent", 0.0);
		q = q / 100;
		double p = 1 - q;
		double cashInDanger = cash * q;

		/**
		 * pp implementation: this constant muss expres th number of BB allow to pull or pusch implementation. (maxx
		 * recon ammunitions). this implemntation read th EV value from preflopdistribution and compute the inverse of
		 * danger (positive EV, hat -danger value). diferenty of normal danger after preflop. preflop danger is
		 * incremental. meaning every previous call/bet increaase the chanse of Fold
		 */

		/**
		 * ppMax represent the max number of ammo allow to expend to see the flop- when the preflop is good, this allos
		 * multiples call/reise
		 */
		int ppMax = 20 * bigBlind;

		/**
		 * ppBase is a minimun allow to see the preflop. whe the ppMax * ev value are negative but not so far, ppBase
		 * allow Hero to call a number of bb calls in order to se the flop. when another villa, try to steal the pot,
		 * the the value of the equation is so negative thtat hero Fold
		 */
		int ppBase = 0;
		if (hand.size() == 2) {
			ppBase = 5 * bigBlind;
			evHand = preflopCardsModel.getEV(myHole);
			// cash = ppBase + ppMax * evHand;
			cash = ppMax;
			cashInDanger = matchCost;
			// cashInDanger = matchCost;
			q = 0.75;
			p = 1 - q;
		}

		double ammo = ppBase + ((cash - cashInDanger) * evHand);

		double K = ammo/2;
		if( matchCost > 0) {
			double den = (pot / matchCost);
			K = p - q / den;			
		}

		// if amunition control equation is less that minimun Fold
		if (minBet > ammo) {
			return PlayerAction.FOLD;
		}

		double b = ammo + alpha * ammo; // agresive: b > ammo
		b = b == 0 ? 1 : b; // avoid error when alpha is extreme low
		double c = alpha < 0 ? b : ammo; // K sugestions allways as upperbound
		TriangularDistribution td = new TriangularDistribution(0, c, b);
		int amount = (int) td.sample();

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
		return action;
	}

	@Override
	public void actorRotated(Player actor) {
		// Not implemented.
	}
}
