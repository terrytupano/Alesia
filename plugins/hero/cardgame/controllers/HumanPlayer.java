package plugins.hero.cardgame.controllers;

import plugins.hero.cardgame.*;
import plugins.hero.cardgame.Player.*;
import plugins.hero.cardgame.interfaces.*;
import plugins.hero.cardgame.ui.*;

/**
 *
 * @author Chris
 */
public class HumanPlayer implements PlayerController, GameObserver {

	public int move = -1;
	public int bet = -1;
	public Deck discards = null;

	private Game game;
	private Player player;

	private GameWindow window;

	public HumanPlayer(final GameWindow window) {
		this.window = window;
	}

	public CallRaiseFold doCallRaiseFold(int callAmount, boolean canRaise) {
		move = -1;

		window.setHumanPlayer(this, true, canRaise);

		synchronized (this) {
			while (move == -1) {
				try {
					wait();
				} catch (InterruptedException ex) {
					// Do nothing
				}
			}
		}

		if (move == 0) {
			return CallRaiseFold.CALL;
		} else if (move == 1) {
			return CallRaiseFold.RAISE;
		} else {
			return CallRaiseFold.FOLD;
		}
	}

	public OpenCheck doOpenCheck() {
		move = -1;

		window.setHumanPlayer(this, false, true);

		synchronized (this) {
			while (move == -1) {
				try {
					wait();
				} catch (InterruptedException ex) {
					// Do nothing
				}
			}
		}

		if (move == 0) {
			return OpenCheck.CHECK;
		} else {
			return OpenCheck.OPEN;
		}
	}

	public boolean shouldShowCards() {
		return true;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void setGame(Game game) {
		this.game = game;
		game.registerObserver(this);
	}

	public int getRaise(int minimum) {
		bet = -1;

		new RaiseWindow(this, minimum);

		synchronized (this) {
			while (bet == -1) {
				try {
					wait();
				} catch (InterruptedException ex) {
					// Do nothing
				}
			}
		}

		return bet;
	}

	public Player getPlayer() {
		return player;
	}

	public GameWindow getWindow() {
		return window;
	}

	public void communityCardsUpdated() {
		// Do nothing
	}

	public void playerCardsUpdated() {
		// Do nothing
	}

	public void playersTurn(Player player) {
		// Do nothing
	}

	public void newPlayer(Player player) {
		// Do nothing
	}

	public void newGame() {
		// Do nothing
	}

	public void endGame() {
		window.setWaitPlayer(this);

		boolean cont = false;

		synchronized (this) {
			do {
				cont = false;

				try {
					wait();
				} catch (InterruptedException ex) {
					cont = true;
				}
			} while (cont);
		}
	}

	public void setDealer(Player player) {
		// Do nothing
	}

	public void placeBlind(Player player, int blind, String name) {
		// Do nothing
	}

	public void raise(Player player, int amount) {
		// Do nothing
	}

	public void fold(Player player) {
		// Do nothing
	}

	public void call(Player player) {
		// Do nothing
	}

	public void check(Player player) {
		// Do nothing
	}

	public void open(Player player, int amount) {
		// Do nothing
	}

	public void winner(Player players) {
		// Do nothing
	}

	public void showdown() {
		// Do nothing
	}

	/** {@inheritDoc} */
	public boolean isLocalHuman() {
		return true;
	}

	public Deck discardCards(final int minimum, final int maximum) {
		return null;
	}

	public void discards(Player player, int number) {
		// Do nothing
	}

	public void cardDealt(Player player, Card card) {
		// Do nothing
	}

}
