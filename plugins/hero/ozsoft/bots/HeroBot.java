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

import com.jgoodies.common.base.*;

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
	private PokerSimulator pokerSimulator;;
	private TableType tableType;
	private List<Player> villans;
	private Player heroPlayer;
	private int bigBlind;
	private int dealer;
	private int pot;

	public HeroBot() {
		this.pokerSimulator = new PokerSimulator();
		this.trooper = new Trooper(null, pokerSimulator);
	}

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
		pokerSimulator.setHeroChips(heroPlayer.getCash());
		// FIXME: temporal
		pokerSimulator.setRaiseValue(minBet * 2);
		// long foldV = villans.stream().filter(p -> p.getAction().getName().equals("Fold")).count();
		int foldV = 0;
		for (Player p : villans) {
			if (p.getHand().size() == 0)
				foldV++;
		}
		int actV = villans.size() - ((int) foldV);
		pokerSimulator.setNunOfPlayers(actV + 1);
		pokerSimulator.setTablePosition(dealer, actV);

		pokerSimulator.runSimulation();
		TrooperAction act = trooper.getSimulationAction();

		if (act.equals(TrooperAction.CHECK))
			return PlayerAction.CHECK;
		if (act.name.equals("call"))
			return new BetAction((int) act.amount);
		if (act.name.equals("raise"))
			return new BetAction((int) act.amount);
		return PlayerAction.FOLD;
	}

	@Override
	public void actorRotated(Player actor) {
		// Not implemented.
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
	private int buyIn;
	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		this.tableType = type;
		this.villans = new ArrayList(players);
		this.heroPlayer = players.stream().filter(p -> p.getName().equals("Hero")).findFirst().get();
		villans.remove(heroPlayer);
		this.bigBlind = bigBlind;
		this.buyIn = heroPlayer.getCash();
	}

	@Override
	public void messageReceived(String message) {
		// Not implemented.
	}

	@Override
	public void playerActed(Player player) {

	}

	@Override
	public void playerUpdated(Player player) {
		if (player.getHand().size() == NO_OF_HOLE_CARDS) {
			UoAHand hand = player.getHand();
			pokerSimulator.cardsBuffer.put("hero.card1", hand.getCard(1).toString());
			pokerSimulator.cardsBuffer.put("hero.card2", hand.getCard(2).toString());
		}
	}
}
