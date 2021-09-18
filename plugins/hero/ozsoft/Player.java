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

package plugins.hero.ozsoft;

import java.util.*;

import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.actions.*;

/**
 * A Texas Hold'em player. <br />
 * <br />
 * 
 * The player's actions are delegated to a {@link Client}, which can be either human-controlled or AI-controlled (bot).
 * 
 * @author Oscar Stigter
 */
public class Player {

	/** Name. */
	private final String name;

	/** Client application responsible for the actual behavior. */
	private Client client;

	/** Hand of cards. */
	private final UoAHand hand;

	/** Current amount of cash. */
	private int cash;

	/** Whether the player has hole cards. */
	private boolean hasCards;

	/** Current bet. */
	private int bet;

	/** Last action performed. */
	private PlayerAction action;
	
	/**
	 * Constructor.
	 * 
	 * @param name The player's name.
	 * @param cash The player's starting amount of cash.
	 * @param client The client application.
	 */
	public Player(String name, int cash, Client client) {
		this.name = name;
		this.cash = cash;
		this.client = client;
		hand = new UoAHand();
		resetHand();		
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Player))
			return false;
		return getName().equals(((Player) obj).getName());
	}

	/**
	 * Returns the player's most recent action.
	 * 
	 * @return The action.
	 */
	public PlayerAction getAction() {
		return action;
	}

	/**
	 * Returns the player's current bet.
	 * 
	 * @return The current bet.
	 */
	public int getBet() {
		return bet;
	}

	/**
	 * Returns the player's current amount of cash.
	 * 
	 * @return The amount of cash.
	 */
	public int getCash() {
		return cash;
	}

	/**
	 * Returns the client.
	 * 
	 * @return The client.
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * Returns the player's hole cards.
	 * 
	 * @return The hole cards.
	 */
	public UoAHand getHand() {
		return hand;
	}

	/**
	 * Returns the player's name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns whether the player has his hole cards dealt.
	 * 
	 * @return True if the hole cards are dealt, otherwise false.
	 */
	public boolean hasCards() {
		return hasCards;
	}

	/**
	 * Indicates whether this player is all-in.
	 * 
	 * @return True if all-in, otherwise false.
	 */
	public boolean isAllIn() {
		return hasCards() && (cash == 0);
	}

	/**
	 * Pays an amount of cash.
	 * 
	 * @param amount The amount of cash to pay.
	 */
	public void payCash(int amount) {
		if (amount > cash) {
			throw new IllegalStateException("Player asked to pay more cash than he owns!");
		}
		cash -= amount;
	}
	
	public void setCash(int cash) {
		this.cash = cash;
	}

	/**
	 * Posts the big blinds.
	 * 
	 * @param blind The big blind.
	 */
	public void postBigBlind(int blind) {
		action = PlayerAction.BIG_BLIND;
		cash -= blind;
		bet += blind;
	}

	/**
	 * Posts the small blind.
	 * 
	 * @param blind The small blind.
	 */
	public void postSmallBlind(int blind) {
		action = PlayerAction.SMALL_BLIND;
		cash -= blind;
		bet += blind;
	}

	/**
	 * Returns a clone of this player with only public information.
	 * 
	 * @return The cloned player.
	 */
	public Player publicClone() {
		Player clone = new Player(name, cash, null);
		clone.hasCards = hasCards;
		clone.bet = bet;
		clone.action = action;
		return clone;
	}

	/**
	 * Resets the player's bet.
	 */
	public void resetBet() {
		bet = 0;
		action = (hasCards() && cash == 0) ? PlayerAction.ALL_IN : null;
	}

	/**
	 * Prepares the player for another hand.
	 */
	public void resetHand() {
		hasCards = false;
		hand.makeEmpty();
		resetBet();
	}

	/**
	 * Sets the player's most recent action.
	 * 
	 * @param action The action.
	 */
	public void setAction(PlayerAction action) {
		this.action = action;
	}

	/**
	 * Sets the player's current bet.
	 * 
	 * @param bet The current bet.
	 */
	public void setBet(int bet) {
		this.bet = bet;
	}

	/**
	 * Sets the hole cards. calling this metod with null argument remove the cards (fold)
	 * 
	 * @param cards - cards or <code>null</code>
	 */
	public void setCards(List<UoACard> cards) {
		hand.makeEmpty();
		hasCards = false;
		if (cards != null) {
			if (cards.size() == 2) {
				hand.addCard(cards.get(0));
				hand.addCard(cards.get(1));
				hasCards = true;
				// System.out.format("[CHEAT] %s's cards:\t%s\n", name, hand);
			} else {
				throw new IllegalArgumentException("Invalid number of cards");
			}
		}
	}

	/**
	 * set a client for this player
	 * 
	 * @param client - The client.
	 */
	public void setClient(Client client) {
		this.client = client;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Wins an amount of money.
	 * 
	 * @param amount The amount won.
	 */
	public void win(int amount) {
		cash += amount;
	}
}
