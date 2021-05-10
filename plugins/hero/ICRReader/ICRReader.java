package plugins.hero.ICRReader;

import java.io.*;
import java.util.*;

import com.alee.utils.*;

import plugins.hero.UoALoky.*;

/**
 * @author Zack Tillotson
 *
 */

public class ICRReader {
	private final List<Game> games = new ArrayList<Game>();
	private String table;
	private Hashtable<String, String> actionsDic;

	public ICRReader(String table) {
		this.table = table;
		this.actionsDic = new Hashtable<>();
		actionsDic.put("-", "no action; player is no longer contesting pot");
		actionsDic.put("B", "blind bet");
		actionsDic.put("f", "fold");
		actionsDic.put("k", "check");
		actionsDic.put("b", "bet");
		actionsDic.put("c", "call");
		actionsDic.put("r", "raise");
		actionsDic.put("A", "all-in");
		actionsDic.put("Q", "quits game");
		actionsDic.put("K", "kicked from game");
	}

	/**
	 * retrun a list of {@link Player} that in some point, haven the equal cards
	 * 
	 * @param holeCards - Hole cards in string format (As Th)
	 * @param boardCards
	 * @return
	 */
	public List<Player> getPlayers(String holeCards, String boardCards, int distance) {
		List<Player> rtnl = new ArrayList();
		ICRHand hand = new ICRHand(holeCards + " " + boardCards);
		int numC = hand.getSize() - 2;
		for (Game game : games) {
			for (Player player : game.players) {
				if (player.handCards.size() == 2 && game.boardCards.size() >= numC) {
					ArrayList<ICRCard> list2 = new ArrayList<>(player.handCards);
					List<ICRCard> tl = game.boardCards.subList(0, numC);
					player.boardCards = ICRHand.getStringOf(tl);
					list2.addAll(tl);

					ICRHand phand = new ICRHand(list2);
					//
					int delt = phand.getHandRank()-hand.getHandRank();
					if (Math.abs(delt) <= distance) {
						player.distance = delt;
						player.handName = phand.handName();
						rtnl.add(player);
					}
				}
			}
		}
		return rtnl;
	}

	/**
	 * perform the read operacion from the IRC database. this method update the gloval variable {@link #games}
	 */
	public void performRead() {
		List<File> dirs = FileUtils.findFilesRecursively("plugins/hero/resources/IRCData/" + table,
				fn -> fn.getParent().endsWith(table));
		for (File dir : dirs) {
			Flattenizer f = new Flattenizer(dir.getAbsolutePath());
			games.addAll(f.getGames());
		}
	}

	/**
	 * TODO: incomplete
	 * 
	 * @param game
	 */
	public void printGameEvent(Game game) {
		String boardc = game.boardCards.toString();
		System.out.println("New Hand: " + game.id);

		boolean hadAction;

		// preflop

		// Flop actions
		System.out.println(
				"Flop: " + game.boardCards.get(0) + " " + game.boardCards.get(1) + " " + game.boardCards.get(2));
		hadAction = true;
		for (int round = 1; round == 1 || hadAction; round++) {
			hadAction = false;
			for (Player player : game.players) {
				if (player.flopActions.length() >= round) {
					String action = player.flopActions.substring(round - 1, round);
					System.out.println(player.handCards.toString() + " " + player.name + " " + actionsDic.get(action));
					hadAction = true;
				}
			}
		}

		// Turn actions
		System.out.println("Turn: " + game.boardCards.get(0) + " " + game.boardCards.get(1) + " "
				+ game.boardCards.get(2) + " " + game.boardCards.get(3));
		hadAction = true;
		for (int round = 1; round == 1 || hadAction; round++) {
			hadAction = false;
			for (Player player : game.players) {
				if (player.turnActions.length() >= round) {
					String action = player.turnActions.substring(round - 1, round);
					if (!action.equals("-"))
						System.out.println(
								player.handCards.toString() + " " + player.name + " " + actionsDic.get(action));
					hadAction = true;
				}
			}
		}

		// River actions
		System.out.println("River: " + game.boardCards.get(0) + " " + game.boardCards.get(1) + " "
				+ game.boardCards.get(2) + " " + game.boardCards.get(3) + " " + game.boardCards.get(4));
		hadAction = true;
		for (int round = 1; round == 1 || hadAction; round++) {
			hadAction = false;
			for (Player player : game.players) {
				if (player.riverActions.length() >= round) {
					String action = player.riverActions.substring(round - 1, round);
					if (!action.equals("-"))
						System.out.println(
								player.handCards.toString() + " " + player.name + " " + actionsDic.get(action));
					hadAction = true;
				}
			}
		}
	}

	/**
	 * Print the game argument in standar poker text format
	 * 
	 * @param game - the game to print
	 */
	public void tabularFormat(Game game) {
		System.out.println("Game information");
		StringBuffer board = new StringBuffer();
		game.boardCards.forEach(c -> board.append(c.toString() + " "));
		String patt = "%9s %5s %10s %10s %10s %10s %-15s";
		System.out.println(String.format(patt, "timestamp", "play#", "preF", "flop", "turn", "river", "board"));
		System.out.println(String.format(patt, game.id, game.players.size(), game.potFlop, game.potTurn, game.potRiver,
				game.potShowdown, board));

		System.out.println();

		patt = "%-10s %3s %-5s %-5s %-5s %-5s %7s %-5s";
		System.out.println("Player information");
		System.out.println(String.format(patt, "Player", "Pos", "preF", "flop", "turn", "river", "chips", "Cards"));
		for (Player player : game.players) {
			StringBuffer cards = new StringBuffer();
			player.handCards.forEach(c -> cards.append(c.toString() + " "));
			System.out.println(String.format(patt, player.name, player.position, player.flopActions, player.turnActions,
					player.riverActions, player.showdownActions, player.chipsCount, cards));
		}
	}

	public static void main(String[] args) throws Exception {
		ICRReader reader = new ICRReader("holdemTest");
		reader.performRead();

		Game sample = reader.games.get(1);
		reader.tabularFormat(sample);

		String holec = "Kd 7s";
		String boardc = "2c 3d Ah Ks 6h";
		int dis = 1;
		long mills = System.currentTimeMillis();
		System.out.println();
		System.out.println("Find " + holec + " " + boardc + " distane=" + dis);

		List<Player> plist = reader.getPlayers(holec, boardc, dis);
		String patt = "%-10s %3s %-5s %-5s %-5s %-5s %7s %7s %5s %-21s %-20s";
		System.out.println(String.format(patt, "Player", "Pos", "preF", "flop", "turn", "river", "chips", "Win", "Dis",
				"Cards", "Hand"));
		for (Player player : plist) {
			String cards = ICRHand.getStringOf(player.handCards) + " " + player.boardCards;
			System.out.println(String.format(patt, player.name, player.position, player.flopActions, player.turnActions,
					player.riverActions, player.showdownActions, player.chipsCount, player.chipsWins, player.distance,
					cards, player.handName));
		}
		System.out.println("Time = " + (System.currentTimeMillis() - mills) + "Ms");
	}
}
