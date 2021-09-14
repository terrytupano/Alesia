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
	protected UoAHand myHole, communityHand, hand;

	protected int prevCash, cash, expendedCash;

	// game observer elements
	private PreflopCardsModel cardsRange;

	@Override
	public void handStarted(Player dealer) {
		cash = player.getCash();
		int delta = cash - prevCash;
		expendedCash += delta;
		delta = delta > 0 ? 1 : -1;

		if ("preFlopConvergency".equals(observationMethod) && myHole.size() != 0)
			cardsRange.updateCoordenates(myHole.getCard(1), myHole.getCard(2), delta);

		prevCash = player.getCash();
		myHole.makeEmpty();
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		this.communityHand = hand;
		this.hand = new UoAHand(myHole.toString() + " " + hand.toString());
		this.pot = pot + bet;
	}

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		this.villans = new ArrayList(players);
		this.player = players.stream().filter(p -> playerName.equals(p.getName())).findFirst().get();
		villans.remove(player);
		this.bigBlind = bigBlind;
		this.buyIn = player.getCash();

		// game observer init
		this.prevCash = player.getCash();
		this.myHole = new UoAHand();

		// fail save: preFlopConvergency allow only for hero (temporal maybe)
		if ("preFlopConvergency".equals(observationMethod) && "Hero".equals(playerName)) {
			Alesia.getInstance().openDB("hero");
			this.cardsRange = new PreflopCardsModel("preFlopConvergency");
		}
	}

	@Override
	public void playerActed(Player player) {
		// if(player.equals(player))
		// System.out.println("DummyBot.playerActed()");

		// Not implemented.
	}

	@Override
	public void playerUpdated(Player player) {
		if (playerName.equals(player.getName()) && player.getHand().size() == NO_OF_HOLE_CARDS) {
			this.myHole = player.getHand();
		}
	}

	/**
	 * set the observation method for this Bot.
	 * 
	 * @param observationMethod - the observation method
	 */
	public void setObservationMethod(String observationMethod) {
		this.observationMethod = observationMethod;
	}

	/**
	 * this method is invoqued directly after class constructor and set the player name. <b>Hero</b> is allawis Hero and
	 * the villas haven other names
	 * 
	 * @param playerName - name of the player
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public void setPokerSimulator(PokerSimulator pokerSimulator) {
		this.pokerSimulator = pokerSimulator;
	}
}
