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

import core.*;
import plugins.hero.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.*;
import plugins.hero.utils.*;

/**
 * Base class for all Texas Hold'em poker bot implementations. this base implementation contain all required variables
 * to allow subclasses record the history of this particular implementation.
 * 
 * @author Oscar Stigter
 */
public abstract class Bot implements Client {

	/** Number of hole cards. */
	protected static final int NO_OF_HOLE_CARDS = 2;
	protected PokerSimulator pokerSimulator;
	protected Player player;
	protected List<Player> villans;
	protected int bigBlind;
	protected int dealer;
	protected int pot;
	protected int buyIn;
	protected String playerName;
	protected String observationMethod;

	// game observer elements
	private PreflopCardsModel cardsRange;
	private int prevChips;
	private UoAHand myHole;

	@Override
	public void handStarted(Player dealer) {
		int delta = player.getCash() - prevChips;
		delta = delta > 0 ? 1 : -1;

		if ("preFlopConvergency".equals(observationMethod) && myHole.size() != 0)
			cardsRange.updateCoordenates(myHole.getCard(1), myHole.getCard(2), delta);

		prevChips = player.getCash();
		myHole.makeEmpty();
	}

	public void setPokerSimulator(PokerSimulator pokerSimulator) {
		this.pokerSimulator = pokerSimulator;
	}

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		this.villans = new ArrayList(players);
		this.player = players.stream().filter(p -> p.getName().equals("Hero")).findFirst().get();
		villans.remove(player);
		this.bigBlind = bigBlind;
		this.buyIn = player.getCash();

		// game observer init
		this.prevChips = player.getCash();
		this.myHole = new UoAHand();

		// fail save: preFlopConvergency allow only for hero (temporal maybe)
		if ("preFlopConvergency".equals(observationMethod) && "Hero".equals(playerName))
			throw new IllegalArgumentException("preFlopConvergency observarion.s method is allow only for Hero.");
		
		if ("preFlopConvergency".equals(observationMethod)) {
			Alesia.getInstance().openDB("hero");
			this.cardsRange = new PreflopCardsModel("preFlopConvergency");
		}
	}

	public void setObservationMethod(String observationMethod) {
		this.observationMethod = observationMethod;
	}

	@Override
	public void playerActed(Player player) {
		// if(player.equals(player))
		// System.out.println("DummyBot.playerActed()");

		// Not implemented.
	}

	@Override
	public void playerUpdated(Player player) {
		UoAHand hand = player.getHand();
		if (player.equals(this.player) && hand.size() == 2) {
			this.myHole = new UoAHand(new String(player.getHand().toString()));
			// if (myHole.size() != 2 || board.size() < 5) {
			// return;
			// }
		}
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
}
