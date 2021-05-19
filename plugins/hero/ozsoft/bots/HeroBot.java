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
import java.util.stream.*;

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

	/** Table type. */
	private TableType tableType;

	/** The hole cards. */
	private UoAHand hand;

	public HeroBot() {
		this((int) Math.random() * 100, (int) Math.random() * 100);
		this.pokerSimulator = new PokerSimulator();
		this.trooper = new Trooper();
	}
	/**
	 * Constructor.
	 * 
	 * @param tightness The bot's tightness (0 = loose, 100 = tight).
	 * @param aggression The bot's aggressiveness in betting (0 = careful, 100 = aggressive).
	 */
	public HeroBot(int tightness, int aggression) {
		if (tightness < 0 || tightness > 100) {
			throw new IllegalArgumentException("Invalid tightness setting");
		}
		if (aggression < 0 || aggression > 100) {
			throw new IllegalArgumentException("Invalid aggression setting");
		}
		this.tightness = tightness;
		this.aggression = aggression;
	}

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		this.tableType = type;
	}

	@Override
	public void messageReceived(String message) {
		// Not implemented.
	}

	@Override
	public void handStarted(Player dealer) {
		hand = null;
	}

	@Override
	public void actorRotated(Player actor) {
		// Not implemented.
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playerUpdated(Player player) {
		if (player.getHand().size() == NO_OF_HOLE_CARDS) {
			this.hand = player.getHand();
		}
	}

	@Override
	public void playerActed(Player player) {
		// Not implemented.
	}

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		PlayerAction action = null;

		pokerSimulator.setTablePosition(getDealerButtonPosition(), getActiveVillans());
		pokerSimulator.setPotValue(getSensor("pot").getNumericOCR());
		pokerSimulator.setCallValue(getSensor("hero.call").getNumericOCR());
		pokerSimulator.setHeroChips(getSensor("hero.chips").getNumericOCR());
		pokerSimulator.setRaiseValue(getSensor("hero.raise").getNumericOCR());

		pokerSimulator.setNunOfPlayers(getActiveVillans() + 1);
		pokerSimulator.runSimulation();

		return action;
	}
}
