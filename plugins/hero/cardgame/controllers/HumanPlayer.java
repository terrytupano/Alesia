package plugins.hero.cardgame.controllers;

import plugins.hero.cardgame.*;
import plugins.hero.cardgame.Player.*;
import plugins.hero.cardgame.interfaces.*;
import plugins.hero.cardgame.ui.*;

/**
 *
 * @author Chris
 */
public class HumanPlayer implements PlayerController {

	public int move = -1;
	public int bet = -1;
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
		//		game.registerObserver(this);
	}

	public int getRaise(int minimum) {
		return minimum * 2;
	}

	@Override
	public boolean isLocalHuman() {
		// TODO Auto-generated method stub
		return false;
	}
}
