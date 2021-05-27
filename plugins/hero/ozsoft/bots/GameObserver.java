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

import org.javalite.activejdbc.*;

import core.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.*;
import plugins.hero.ozsoft.actions.*;
import plugins.hero.utils.*;

/**
 * Dummy Texas Hold'em poker bot that always just checks or calls. <br />
 * <br />
 * 
 * This bot allowed for perfectly predictable behavior.
 * 
 * @author Oscar Stigter
 */
public class GameObserver extends Bot {

	private PreflopCardsRange cardsRange;
	private int prevChips;
	private UoAHand myHole;
	private DB db;
	public GameObserver() {

	}
	@Override
	public void messageReceived(String message) {

	}

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		super.joinedTable(type, bigBlind, players);
		this.db = Alesia.getInstance().openDB("hero", true);
		this.cardsRange = new PreflopCardsRange("simul_11");
		this.prevChips = heroPlayer.getCash();
		this.myHole = new UoAHand();
	}

	@Override
	public void handStarted(Player dealer) {
		if (db.getConnection() == null) {
			System.out.println("Opening connection...");
			table.firePropertyChange(Table.PROP_MESSAGE, "", "Opening connection...");
			db.open();
		}

		int delta = heroPlayer.getCash() - prevChips;
		// if (delta != 0) {
		// if (myHole.size() == 0) {
		// System.out.println(heroPlayer.getCash());
		// cardsRange.addCount(coord.y, coord.x, delta);
		delta = delta > 0 ? 1 : -1;
		if (myHole.size() != 0)
			cardsRange.updateCoordenates(myHole.getCard(1), myHole.getCard(2), delta);
		prevChips = heroPlayer.getCash();
		// System.out.println((++handNum) + " cards: " + myHole + " delta: " + delta);
		myHole.makeEmpty();
		// }
	}

	@Override
	public void actorRotated(Player actor) {
		// Not implemented.
	}

	@Override
	public void playerUpdated(Player player) {
		UoAHand hand = player.getHand();
		if (player.equals(heroPlayer) && hand.size() == 2) {
			this.myHole = new UoAHand(new String(player.getHand().toString()));
			// if (myHole.size() != 2 || board.size() < 5) {
			// return;
			// }
		}
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {

	}

	@Override
	public void playerActed(Player player) {
		// if(player.equals(heroPlayer))
		// System.out.println("DummyBot.playerActed()");

		// Not implemented.
	}

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		if (allowedActions.contains(PlayerAction.CHECK)) {
			return PlayerAction.CHECK;
		} else {
			return PlayerAction.CALL;
		}
	}
}
