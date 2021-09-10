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
 * wraper between Poker simulator and Hero trooper
 * 
 */
public class HeroBot extends Bot {

	private Trooper trooper;
	
	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		for (PlayerAction act : allowedActions) {
			String sensor = act.getName().toLowerCase();
			if (act instanceof AllInAction)
				sensor = "raise.allin";

			pokerSimulator.sensorStatus.put(sensor, true);
		}
		// pokerSimulator.setHeroChips(players.in);
		pokerSimulator.setCallValue(currentBet);
		if (allowedActions.contains(PlayerAction.CHECK))
			pokerSimulator.setCallValue(0);

		pokerSimulator.setPotValue(pot);
		pokerSimulator.setHeroChips(player.getCash());
		// FIXME: temporal
		pokerSimulator.setRaiseValue(minBet * 2);
		int actV = (int) villans.stream().filter(p -> p.hasCards()).count();
		// int actV = villans.size() - ((int) foldV);
		pokerSimulator.setNunOfPlayers(actV + 1);
		pokerSimulator.setTablePosition(dealer, actV);

		pokerSimulator.runSimulation();
		TrooperAction act = trooper.getSimulationAction();

		PlayerAction action = null;
		if (act.equals(TrooperAction.FOLD))
			action = PlayerAction.FOLD;
		if (act.equals(TrooperAction.CHECK))
			action = PlayerAction.CHECK;
		if (act.name.equals("call") && act.amount > 0) {
			if (allowedActions.contains(PlayerAction.CALL))
				action = new CallAction((int) act.amount);
			if (allowedActions.contains(PlayerAction.BET))
				action = new BetAction((int) act.amount);
		}
		if (act.name.equals("raise") || act.name.equals("pot") || act.name.equals("allIn")) {
			if (allowedActions.contains(PlayerAction.RAISE))
				action = new RaiseAction((int) act.amount);
			if (allowedActions.contains(PlayerAction.BET))
				action = new BetAction((int) act.amount);
		}
		if (action == null)
			throw new IllegalArgumentException("Hero bot has no correct action selected. Trooper action was" + act);
		return action;
	}
	
	@Override
	public void actorRotated(Player actor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
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
		this.pot = pot + bet;
	}

	@Override
	public void handStarted(Player dealer) {
		this.dealer = villans.indexOf(dealer) + 1;
		pokerSimulator.bigBlind = bigBlind;
		pokerSimulator.smallBlind = bigBlind / 2;
		pokerSimulator.buyIn = buyIn;
		pokerSimulator.clearEnviorement();
	}

	@Override
	public void messageReceived(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void playerUpdated(Player player) {
		if (player.getHand().size() == NO_OF_HOLE_CARDS) {
			UoAHand hand = player.getHand();
			pokerSimulator.cardsBuffer.put("hero.card1", hand.getCard(1).toString());
			pokerSimulator.cardsBuffer.put("hero.card2", hand.getCard(2).toString());
		}
	}
	@Override
	public void setPokerSimulator(PokerSimulator pokerSimulator) {
		super.setPokerSimulator(pokerSimulator);
		this.trooper = new Trooper(null, pokerSimulator);
	}
}
