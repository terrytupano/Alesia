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
import hero.UoAHandEval.*;
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
		// if ("Hero".equals(trooperName))
		// System.out.println("table: " + table.getTableId() + " Hero: tau = " + tau +
		// ", " + "alpha: " + alpha);

		// apply tau parameter
		// if (!preflopCardsModel.containsHand(myHole)) {
		// 	return PlayerAction.FOLD;
		// }

		double cash = (double) player.getCash();
		List<TrooperAction> actions = Bot.loadActions(minBet, currentBet, cash, allowedActions);

		activeteSensors(minBet, currentBet, allowedActions);
		pokerSimulator.runSimulation();
		System.out.println(player.getName() + " actions: " + pokerSimulator.ruleBook.result);

		if (allowedActions.contains(PlayerAction.CHECK)) {
			return PlayerAction.CHECK;
		} else {
			return PlayerAction.CALL;
		}

		// return getPlayerAction(subOptimalAction.action, allowedActions);
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		super.boardUpdated(hand, bet, pot);
		if (hand.getCard(1) != null)
			pokerSimulator.cardsBuffer.put("flop1", hand.getCard(1).toString());
		if (hand.getCard(2) != null)
			pokerSimulator.cardsBuffer.put("flop2", hand.getCard(2).toString());
		if (hand.getCard(3) != null)
			pokerSimulator.cardsBuffer.put("flop3", hand.getCard(3).toString());
		if (hand.getCard(4) != null)
			pokerSimulator.cardsBuffer.put("turn", hand.getCard(4).toString());
		if (hand.getCard(5) != null)
			pokerSimulator.cardsBuffer.put("river", hand.getCard(5).toString());
	}

	@Override
	public void handStarted(Player dealer) {
		this.dealer = villains.indexOf(dealer) + 1;
		pokerSimulator.bigBlind = bigBlind;
		pokerSimulator.smallBlind = bigBlind / 2;
		pokerSimulator.buyIn = buyIn;
		pokerSimulator.newHand();
	}

	@Override
	public void playerUpdated(Player player) {
		super.playerUpdated(player);
		if (myHole.size() > 0) {
			pokerSimulator.cardsBuffer.put("hero.card1", myHole.getCard(1).toString());
			pokerSimulator.cardsBuffer.put("hero.card2", myHole.getCard(2).toString());
		}
	}
}
