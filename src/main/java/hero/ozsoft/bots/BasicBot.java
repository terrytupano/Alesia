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
 * 
 */
public class BasicBot extends Bot {

	public BasicBot() {

	}

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		// No choice, must check.
		if (allowedActions.size() == 1)
			return PlayerAction.CHECK;

		// System.out.println("--- ");
		activeteSensors(minBet, currentBet, allowedActions);
		pokerSimulator.bettingSequence = table.bettingSequence;
		pokerSimulator.runSimulation();
		TrooperAction action = pokerSimulator.ruleBook.getAction();
		PlayerAction action2 = getPlayerAction(action, allowedActions);

		// System.out.println("currentBet " + currentBet);
		// System.out.println("minBet " + minBet);
		// System.out.println("player.getCash() " + player.getCash());
		// System.out.println("TrooperAction " + action);
		// System.out.println("PlayerAction " + action2);
		double amount = action.amount;
		if (action.name.equals("raise") && amount < minBet && amount < player.getCash()) {
			activeteSensors(minBet, currentBet, allowedActions);
			pokerSimulator.runSimulation();
			action = pokerSimulator.ruleBook.getAction();
			action2 = getPlayerAction(action, allowedActions);
		}

		return action2;
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
	public void playerUpdated(Player player) {
		super.playerUpdated(player);
		if (myHole.size() > 0) {
			pokerSimulator.cardsBuffer.put("hero.card1", myHole.getCard(1).toString());
			pokerSimulator.cardsBuffer.put("hero.card2", myHole.getCard(2).toString());
		}
	}
}
