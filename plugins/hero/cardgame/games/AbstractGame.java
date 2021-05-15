/*
 * Copyright (c) Chris 'MD87' Smith, 2007-2008. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.cardgame.games;

import java.util.*;

import org.jdesktop.application.*;

import core.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.cardgame.*;
import plugins.hero.cardgame.interfaces.*;

/**
 * Implements some basic functions that may be used by a game.
 *
 * @author Chris
 */
public abstract class AbstractGame extends Task implements Game {

	protected int numplayers = 0;
	protected int bigblind = 0;
	protected int ante = 0;
	protected int raises = 4;
	protected int raisesLeft;
	protected int dealer = 0;
	protected int player = 0;
	protected final List<Player> players = new ArrayList<Player>();
	protected UoADeck deck = new UoADeck();
	private final List<GameObserver> observers = new ArrayList<GameObserver>();

	public AbstractGame(final int numplayers, final int bigblind, final int ante, final int raises) {
		super(Alesia.getInstance());
		this.numplayers = numplayers;
		this.bigblind = bigblind;
		this.ante = ante;
		this.raises = raises;
		dealer = (int) Math.round(Math.random() * (numplayers - 1));
	}

	public void addPlayer(final String name, final int cash, final PlayerController controller) {
		if (numplayers <= players.size()) {
			return;
		}
		final Player myPlayer = new Player(name, cash, controller);
		addPlayer(myPlayer);
	}

	public void addPlayer(final Player player) {
		players.add(player);

		notifyNewPlayer(player);
	}

	public Player getDealer() {
		if (players.size() > dealer) {
			return players.get(dealer);
		} else {
			return null;
		}
	}

	public int getBigBlind() {
		return bigblind;
	}

	public int getCurrentPot() {
		int pot = 0;
		for (Player myPlayer : players) {
			pot += myPlayer.getBet();
		}
		return pot;
	}

	public int getMaxBet() {
		int max = 0;
		for (Player player : players) {
			if (player.getBet() > max) {
				max = player.getBet();
			}
		}
		return max;
	}

	public int getNumPlayers() {
		return numplayers;
	}

	/**
	 * Clears and rebuilds the deck used for this game.
	 */
	protected void shuffle() {
		deck = new UoADeck();
		deck.shuffle();
	}

	/**
	 * Clears all player cards.
	 */
	protected void discardCards() {
		for (Player player : players) {
			player.discardCards();
		}
	}

	/**
	 * Deals a single card to each active player, starting with the specified player, and working clockwise.
	 *
	 * @param start The player to start dealing with
	 * @param isPublic Whether the card is a public card or not
	 */
	protected void dealCard(final Player start, final boolean isPublic) {
		int i = players.indexOf(start);
		for (int x = 0; x < numplayers; x++) {
			final Player player = players.get((i + x) % numplayers);

			if (!player.isOut() && !player.isFolded()) {
				final UoACard card = deck.deal();
				player.dealCard(card);
				notifyGameStateChanged("cardDeal", player, card);
			}
		}
	}

	/**
	 * Counts the number of players with the specified properties.
	 *
	 * @param mustBeIn Whether or not the players must be in
	 * @param mustNotFolded Whether or not the players must not have folded to be in
	 * @param mustNotAllIn Whether or not the players must not be all in
	 * @return The number of players matching the properties specified
	 */
	public int countPlayers(final boolean mustBeIn, final boolean mustNotFolded, final boolean mustNotAllIn) {
		int count = 0;
		for (Player player : players) {
			if ((!mustBeIn || !player.isOut()) && (!mustNotFolded || !player.isFolded())
					&& (!mustNotAllIn || !player.isAllIn())) {
				count++;
			}
		}
		return count;
	}

	public List<Player> getPlayers() {
		return players;
	}

	protected void doAntes() {
		if (ante > 0) {
			for (Player player : players) {
				if (!player.isOut()) {
					player.forceBet(ante);
					notifyGameStateChanged("action_Ante", player, ante);
				}
			}
		}
	}

	/**
	 * perform small and big blind operations
	 * 
	 */
	protected void doBlinds() {
		if (countPlayers(true, false, false) > 2) {
			if (!players.get((dealer + 1) % numplayers).isOut()) {
				doSmallBlind(players.get((dealer + 1) % numplayers));
			}

			doBigBlind(players.get((dealer + 2) % numplayers));

			player = (dealer + 2) % numplayers;
		} else {
			Player big = null;
			Player small = null;

			for (Player player : players) {
				if (!player.isOut()) {
					if (player == players.get(dealer)) {
						small = player;
					} else {
						big = player;
					}
				}
			}

			doSmallBlind(small);
			doBigBlind(big);

			player = players.indexOf(big);
		}
	}

	protected void doSmallBlind(final Player player) {
		int value = bigblind / 2;
		player.forceBet(value);
		notifyGameStateChanged("action_SB", player, value);
	}

	protected void doBigBlind(final Player player) {
		player.forceBet(bigblind);
		notifyGameStateChanged("action_BB", player, bigblind);
	}

	protected void waitForBets() {
		int maxbet = getMaxBet();
		int endPlayer = player;
		boolean reachedEnd = false;
		raisesLeft = raises;
		Player myPlayer;

		do {
			player = (player + 1) % numplayers;
			myPlayer = players.get(player);

			if (!myPlayer.isFolded() && !myPlayer.isAllIn() && !myPlayer.isOut()) {
				notifyGameStateChanged("playerTurn", myPlayer, null);

				if (playersHaveBet(maxbet)) {
					// He can check or open
					switch (myPlayer.doOpenCheck()) {
						case CHECK :
							// Do nothing
							notifyGameStateChanged("action_Check", myPlayer, null);
							break;
						case OPEN :
							int raiseAmount = myPlayer.getRaiseAmount(bigblind);
							myPlayer.forceBet(raiseAmount);
							maxbet += raiseAmount;
							notifyGameStateChanged("action_Open", myPlayer, raiseAmount);
							break;
					}
				} else {
					final boolean canRaise = raisesLeft != 0 && countPlayers(true, true, true) > 1;

					// He can call, raise or fold
					switch (myPlayer.doCallRaiseFold(maxbet - myPlayer.getBet(), canRaise)) {
						case RAISE :
							if (canRaise) {
								myPlayer.forceBet(maxbet - myPlayer.getBet());
								int raiseAmount = myPlayer.getRaiseAmount(bigblind);
								myPlayer.forceBet(raiseAmount);
								maxbet += raiseAmount;
								notifyGameStateChanged("action_Raise", myPlayer, raiseAmount);
								raisesLeft--;
								break;
							} // Fall through: call instead
						case CALL :
							int value = maxbet - myPlayer.getBet();
							myPlayer.forceBet(value);
							notifyGameStateChanged("action_Call", myPlayer, value);
							break;
						case FOLD :
							notifyGameStateChanged("action_fold", myPlayer, null);
							break;
					}
				}
			}

			if (player == endPlayer) {
				reachedEnd = true;
			}

		} while (!playersHaveBet(maxbet) || (!reachedEnd && countPlayers(true, true, true) > 1));
	}

	/**
	 * move the dealer button to the next position
	 * 
	 */
	protected void advanceDealerButton() {
		if (countPlayers(true, false, false) > 2) {
			do {
				dealer = (dealer + 1) % numplayers;
			} while (players.get((dealer + 2) % numplayers).isOut());
		} else if (countPlayers(true, false, false) == 2) {
			do {
				dealer = (dealer + 1) % numplayers;
			} while (players.get(dealer).isOut());
		}
	}

	protected void doBettingRound() {
		notifyGameStateChanged("communityCardsUpdated", null, null);
		player = dealer;
		waitForBets();
	}

	protected boolean playersHaveBet(final int bet) {
		for (Player player : players) {
			if (!player.isOut() && !player.isFolded() && !player.isAllIn()) {
				if (player.getBet() < bet) {
					return false;
				}
			}
		}

		return true;
	}

	protected void doShowDown() {
		for (Player player : players) {
			if (!player.isOut()) {
				// TODO: ???????????????
				// player.calculateBestDeck();
			}
		}
		notifyShowdown();
		doWinner();
		for (Player player : players) {
			player.resetBet();
		}
	}

	protected void doWinner() {
		doWinner(false);
	}

	protected void doWinner(final boolean doHalf) {
		if (countPlayers(true, true, false) == 1) {
			int pot = 0;
			Player winner = null;
			for (Player player : players) {
				if (!player.isOut()) {
					if (!player.isFolded()) {
						winner = player;
					}
					if (doHalf) {
						pot += player.getBet() / 2;
					} else {
						pot += player.getBet();
					}
				}
			}
			winner.addCash(pot);
			notifyGameStateChanged("winner", winner, null);
			return;
		}

		// tempPlayers is a list of everyone involved in the round
		List<Player> tempPlayers = new ArrayList<Player>();
		Map<Player, Integer> playerBets = new HashMap<Player, Integer>();
		int maxbet = 0;

		for (Player player : players) {
			if (!player.isOut()) {
				tempPlayers.add(player);

				final int bet = doHalf ? player.getBet() / 2 : player.getBet();

				playerBets.put(player, bet);
				maxbet = Math.max(bet, maxbet);
			}
		}

		while (maxbet > 0 && tempPlayers.size() > 0) {
			int minbet = maxbet;

			for (Player player : tempPlayers) {
				if (playerBets.get(player) < minbet) {
					minbet = playerBets.get(player);
				}
			}

			int potsize = minbet * tempPlayers.size();

			List<Player> minPlayers = new ArrayList<Player>();

			for (Player player : tempPlayers) {
				if (playerBets.get(player) == minbet) {
					minPlayers.add(player);
				}
				playerBets.put(player, playerBets.get(player) - minbet);
			}

			List<Player> possibleWinners = new ArrayList<Player>();

			for (Player player : tempPlayers) {
				if (!player.isFolded() && !player.isOut()) {
					possibleWinners.add(player);
				}
			}

			List<Player> theseWinners = getWinners(possibleWinners);

			potsize = potsize / theseWinners.size();

			if (potsize != 0) {

				for (Player player : theseWinners) {
					player.addCash(potsize);
					notifyGameStateChanged("winner", player, null);
				}
			}

			tempPlayers.removeAll(minPlayers);
			maxbet = 0;
			for (Integer bet : playerBets.values()) {
				if (bet > maxbet) {
					maxbet = bet;
				}
			}
		}
	}

	public boolean hasActiveHuman() {
		for (Player player : players) {
			if (player.isLocalHuman() && !player.isOut() && !player.isFolded()) {
				return true;
			}
		}
		return false;
	}

	public void notifyGameStateChanged(String state, Player player, Object value) {
		for (GameObserver observer : observers) {
			observer.gameStateChanged(state, player, value);
		}
	}

	/**
	 * return the hand name for the player
	 * 
	 */
	public String getHandText(final Player player) {
		String[] parts = null;// getHand(player.getBestDeck()).getFriendlyName().split(": ");
		String res = "";
		for (String part : parts) {
			res = res + "\n" + part;
		}
		return res.substring(1);
	}

	protected List<Player> getWinners(final List<Player> winners) {
		final List<Player> res = new ArrayList<Player>();

		Collections.sort(winners);

		res.add(winners.get(0));

		for (int i = 1; i < winners.size(); i++) {
			if (winners.get(i).compareTo(winners.get(0)) == 0) {
				res.add(winners.get(i));
			}
		}

		return res;
	}

	protected abstract boolean canDoBringIns();

	public void registerObserver(final GameObserver observer) {
		observers.add(observer);
	}

	public void unregisterObserver(final GameObserver observer) {
		observers.remove(observer);
	}

	/**
	 * Notifies all observers that a new player has joined.
	 *
	 * @param player The player who has joined
	 */
	protected void notifyNewPlayer(final Player player) {
		for (GameObserver observer : observers) {
			observer.newPlayer(player);
		}
	}

	protected void notifyDiscards(final Player player, final int amount) {
		for (GameObserver observer : observers) {
			observer.discards(player, amount);
		}
	}

	/**
	 * Notifies all observers that the showdown is taking place.
	 */
	protected void notifyShowdown() {
		for (GameObserver observer : observers) {
			observer.showdown();
		}
	}

}
