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
import hero.Trooper.*;
import hero.ozsoft.*;
import hero.ozsoft.actions.*;

/**
 * Auto mutable Alpha and Tau parameters bot.
 * 
 * Current implementation acts purely on the bot's hole cards, based on
 * <code>Tau</code> parameter of Original preflop Distribution. and
 * <code>alpha</code> mutable parameter selection. Only <b>hero</b> ist allow to
 * mutate every 100 hands. All other players will be randomly created with
 * random parameters.
 * 
 * <li>combined with a configurable level of tightness (when to play or fold a
 * hand ) and aggression (how much to bet or raise in case of good cards or when
 * bluffing). <br />
 * <br />
 * </ul>
 * 
 */
public class BasicBot extends Bot {

	private PreflopCardsModel preflopCardsModel = new PreflopCardsModel();

	/** Tightness (0 = loose, 100 = tight). */
	private int tau;

	/** Betting aggression (0 = safe, 100 = aggressive). */
	private int alpha;

	private Random random = new Random();


	public BasicBot() {

	}

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {

		// No choice, must check.
		if (allowedActions.size() == 1)
			return PlayerAction.CHECK;

		this.tau = simulationVariables.get("tau");
		this.alpha = simulationVariables.get("alpha");
		preflopCardsModel.setPercentage(tau);
//		if ("Hero".equals(trooperName))
//			System.out.println("table: " + table.getTableId() + " Hero: tau = " + tau + ", " + "alpha: " + alpha);

		// apply tau parameter
		if (!preflopCardsModel.containsHand(myHole)) {
			return PlayerAction.FOLD;
		}

		//TODO: hasCard method don't say if the villain folded his cards -------------------------------------------------- !!!!!!!!!
		int activeVillains = (int) villains.stream().filter(p -> p.hasCards()).count();

		double cash = (double) player.getCash();
		List<TrooperAction> actions = Bot.loadActions(minBet, currentBet, cash, allowedActions);
		Map<String, Object> evaluation = PokerSimulator.getEvaluation(myHole, communityHand, activeVillains, cash / bigBlind);
		PotOdd potOdd = Trooper.potOdd(pot, actions, evaluation);
		SubOptimalAction subOptimalAction = Trooper.getAction(potOdd.availableActions,
				SubOptimalAction.TRIANGULAR, alpha);

		return getPlayerAction(subOptimalAction.action, allowedActions);

		// q = q / 100;
//		 double p = 1 - q;
//		 double cashInDanger = cash * q;
		//
		// /**
		// * pp implementation: this constant muss expres th number of BB allow to pull
		// or pusch implementation. (maxx
		// * recon ammunitions). this implemntation read th EV value from
		// preflopdistribution and compute the inverse of
		// * danger (positive EV, hat -danger value). diferenty of normal danger after
		// preflop. preflop danger is
		// * incremental. meaning every previous call/bet increaase the chanse of Fold
		// */
		//
		// /**
		// * ppMax represent the max number of ammo allow to expend to see the flop-
		// when the preflop is good, this
		// allos
		// * multiples call/reise
		// */
		// int ppMax = 20 * bigBlind;
		//
		// /**
		// * ppBase is a minimun allow to see the preflop. whe the ppMax * ev value are
		// negative but not so far, ppBase
		// * allow Hero to call a number of bb calls in order to se the flop. when
		// another villa, try to steal the pot,
		// * the the value of the equation is so negative thtat hero Fold
		// */
		// int ppBase = 0;
		// if (hand.size() == 2) {
		// ppBase = 5 * bigBlind;
		// evHand = preflopCardsModel.getEV(myHole);
		// // cash = ppBase + ppMax * evHand;
		// cash = ppMax;
		// cashInDanger = matchCost;
		// // cashInDanger = matchCost;
		// q = 0.75;
		// p = 1 - q;
		// }
		//
		// double ammo = ppBase + ((cash - cashInDanger) * evHand);
		//
		// double K = ammo / 2;
		// if (matchCost > 0) {
		// double risk = (pot / matchCost);
		// K = p - q / risk;
		// }
		//
		// // if amunition control equation is less that minimun Fold
		// if (minBet > ammo) {
		// return PlayerAction.FOLD;
		// }
		//
		// double b = ammo + alha * ammo; // agresive: b > ammo
		// b = b == 0 ? 1 : b; // avoid error when alpha is extreme low
		// double c = alpha < 0 ? b : ammo; // K sugestions allways as upperbound
		// TriangularDistribution td = new TriangularDistribution(0, c, b);
		// int amount = (int) td.sample();
		//
		// amount = amount < minBet ? minBet : amount;
		// // ------------------
		//

//		return null;
	}

	@Override
	public void actorRotated(Player actor) {
		// Not implemented.
	}

	@Override
	public void handStarted(Player dealer) {
		// Not implemented.
	}
}
