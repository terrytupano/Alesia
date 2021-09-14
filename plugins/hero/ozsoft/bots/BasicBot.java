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

import java.text.*;
import java.util.*;

import org.javalite.activejdbc.*;

import com.javaflair.pokerprophesier.api.card.*;

import core.datasource.model.*;
import plugins.hero.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.*;
import plugins.hero.ozsoft.actions.*;
import plugins.hero.utils.*;

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

	private static DateFormat dateFormat = DateFormat.getDateTimeInstance();

	private int tau;
	private int alpha;
	private int hands = 0;
	private int wins = 0;

	// private SimulatorClient heroClient;
	private PreflopCardsModel cardsModel;

	private String session;

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		PlayerAction action = null;
		if (allowedActions.size() == 1) {
			// No choice, must check.
			action = PlayerAction.CHECK;
		} else {
			// check hole card. NOT in tau range
			if (!cardsModel.containsHand(myHole)) {
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

					// danger implementation allow Hero to leva the battle based on the risck of the current hand. this
					// implementation is linke whit agresion. meaning: more agresion, less care of posible danger.

					double danger = (Double) Hero.getUoAEvaluation(myHole.toString(), communityHand.toString())
							.get("2BetterThanMinePercent");

					// test danger implenetation. delete the danger from available ammunitions
					danger = danger / 100;
					double cashToDanger = cash - (cash *danger);
					
					double a = (alpha / 50.0) * preAlpha * cash;

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

	@Override
	public void handStarted(Player dealer) {

		//
		// TODO: move to basic bot implemetation and assing a observationMethod parameterVariation
		//
		int delta = player.getCash() - prevCash;
		super.handStarted(dealer);
		if (!"Hero".equals(playerName))
			return;

		// wins = heroClient.getInteger("wins") == null ? 0 : heroClient.getInteger("wins");
		wins = wins + delta;
		// heroClient.set("wins", wins);
		// hands = heroClient.getInteger("hands") == null ? 0 : heroClient.getInteger("hands");
		hands++;
		// heroClient.set("hands", hands);

		// update DB
		if (hands % 10 == 0) {
			SimulatorStatistic statistic = SimulatorStatistic.findOrCreateIt("session", session, "measureName",
					"tau Estimation");
			statistic.set("hands", hands);
			statistic.set("wins", wins);
			statistic.set("tau", tau);
			statistic.save();
		}
	}

	@Override
	public void messageReceived(String message) {
		// Not implemented.
	}
	@Override
	public void setPlayerName(String playerName) {
		super.setPlayerName(playerName);

		// Random values for villans
		// tightness The bot's tightness (0 = tight, 100 = loose).
		this.tau = (int) (Math.random() * 100d);
		// aggression The bot's aggressiveness in betting (0.0 = careful, 2.0 = aggressive).
		// this.alpha = (int) (Math.random() * 100d);

		if ("Hero".equals(playerName)) {
			LazyList<SimulatorStatistic> list = SimulatorStatistic.where("ORDER BY session DESC").limit(1);
			tau = 5;
			if (list.size() > 0) {
				SimulatorStatistic statistic = list.get(0);
				tau = statistic.getInteger("tau") == null ? 0 : statistic.getInteger("tau");
				tau = tau == 100 ? 5 : tau + 5;
				session = dateFormat.format(new Date());
			}
			// this.heroClient = SimulatorClient.first("playerName = ?", "Hero");
			// this.alpha = heroClient.getInteger("alpha") == null ? 0 : heroClient.getInteger("alpha");
			// this.tau = heroClient.getInteger("tau") == null ? 0 : heroClient.getInteger("tau");
		}

		// for all players
		this.cardsModel = new PreflopCardsModel();
		cardsModel.setPercentage(tau);

		// FIX: temporal alpha = 25 ( 1/4 times less that equations say)
		alpha = 25;

	}
}
