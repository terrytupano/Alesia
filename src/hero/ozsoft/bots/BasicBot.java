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

	// // TODO: check Poker Expected Value (EV) Formula: EV = (%W * $W) � (%L * $L)
	// // https://www.splitsuit.com/simple-poker-expected-value-formula
	public BasicBot() {
//		this.tau = Table.getShuffleVariable();
//		this.alpha = Table.getShuffleVariable();
	}

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {

		// No choice, must check.
		if (allowedActions.size() == 1)
			return PlayerAction.CHECK;

		this.tau = trooperParameter.getInteger("tau");
		this.alpha = trooperParameter.getInteger("aggression");
		preflopCardsModel.setPercentage(tau);

		// apply tau parameter
		if (!preflopCardsModel.containsHand(myHole)) {
			return PlayerAction.FOLD;
		}

		return getAction(minBet, currentBet, allowedActions);

//		int street = -1;
//		street = hand.size() == 2 ? 2 : street; // pre-flop
//		street = hand.size() == 5 ? 3 : street; // flop
//		street = hand.size() == 6 ? 3 : street; // turn
//		street = hand.size() == 7 ? 1 : street; // river
//
//		double cash = (double) player.getCash();
//		int actV = (int) villans.stream().filter(p -> p.hasCards()).count();
//
//		Map<String, Object> uoaEvaluation = PokerSimulator.getEvaluation(myHole, communityHand, actV, cash / bigBlind);
//		double q = (Double) uoaEvaluation.getOrDefault("rankBehind%", 0.0);

//		add a1 to a4
		// idea for future simulation:
		// - simulate the optimal upperboud of handsranks:
		// motiviation: better probability calculation when there is extremely rare to
		// find a hand like royal flush or straith flus
		//
		// - simulation to compute the preflop % based on num of villans (the result
		// were something like villans*10)
		//
//		read: artikle über Return on Investment (ROI)
//		lowe limit in roi formula: 10% lower limmit in a bussines is 30% (read literature about it) maybe is util to make a table with detail investments 
//		make a ROI formula: ROI = (Winnings - Investment) / Investment take into acount any moment in play when bet > lower limitn (roucht 10%)of roi , fold hero mus play until lower limit but once the limit is errreichen, dont gepardice any more also consider playtime 

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
		// TODO Auto-generated method stub

	}

	/**
	 * return a {@link PlayerAction} based on the current {@link #alpha} value. this
	 * method create a internal list of possibles check/call/bet/raise values based
	 * on {@link #alpha}. when alpha <- 0, this method will return lowers actions's
	 * values. if alpha -> 100 this method will select more often high values
	 * 
	 * @return
	 */
	public PlayerAction getAction(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		List<PlayerAction> actions = new ArrayList<>();

		int bet = Math.max(minBet, currentBet);
		int amount = Double.valueOf(player.getCash() * (alpha / 100d)).intValue();

//		int steps = 10;
//		List<Integer> integers = new ArrayList<>();
//		for (int i = 0; i < steps; i++) {
//			amount += cashToBet / steps;
//			integers.add(amount);
//		}

		// if someone aggression is more that i can bare, check if i can or fold
//		if (cashToBet < minBet) {
//			if (allowedActions.contains(PlayerAction.CHECK))
//				return PlayerAction.CHECK;
//			if (allowedActions.contains(PlayerAction.FOLD))
//				return PlayerAction.FOLD;
//		}

//		if (cashToBet < minBet) {
//			return PlayerAction.ALL_IN;
//		}

		if (allowedActions.contains(PlayerAction.CHECK))
			actions.add(PlayerAction.CHECK);
		if (allowedActions.contains(PlayerAction.CALL))
			actions.add(PlayerAction.CALL);
		if (bet < amount) {
			if (allowedActions.contains(PlayerAction.BET))
				actions.add(new BetAction(amount));
			if (allowedActions.contains(PlayerAction.RAISE))
				actions.add(new RaiseAction(amount));
		}

		// if there is no more option, fold
		if (actions.isEmpty())
			actions.add(PlayerAction.FOLD);

		PlayerAction action = actions.get(random.nextInt(actions.size()));
//		if (action instanceof BetAction || action instanceof RaiseAction)
//			System.out.println("BasicBot.getAction()");
		return action;
	}
}
