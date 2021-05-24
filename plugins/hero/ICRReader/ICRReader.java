package plugins.hero.ICRReader;

import java.io.*;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.*;

import com.alee.utils.*;

import core.*;

/**
 * @author Zack Tillotson
 *
 */

public class ICRReader {
	private final List<Game> games = new ArrayList<Game>();
	private String table;
	private Hashtable<String, String> actionsDic;

	public ICRReader(String table, int sb, int bb) {
		this.table = table;
		this.smallB = sb;
		this.bigB = bb;
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
				// player muss habe a visible card,
				// board is in the correct stage
				// player cards muss be equals to holeCards
				if (player.handCards.size() == 2 && game.boardCards.size() >= numC
						&& ICRHand.isPresent(holeCards, player.handCards)) {
					ArrayList<ICRCard> list2 = new ArrayList<>(player.handCards);
					List<ICRCard> tl = game.boardCards.subList(0, numC);
					player.boardCards = ICRHand.getStringOf(tl);
					list2.addAll(tl);

					ICRHand phand = new ICRHand(list2);
					//
					int delt = phand.getHandRank() - hand.getHandRank();
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
	 * read all de available data and build the game history. the golbal variable {@link #games} will contain a list of
	 * all table games performed.
	 * 
	 */
	public void loadGameHistory() {
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
		System.out.println("\nGame information");
		System.out.println("------------------");
		StringBuffer board = new StringBuffer();
		game.boardCards.forEach(c -> board.append(c.toString() + " "));
		String patt = "%9s %5s %10s %10s %10s %10s %-15s";
		System.out.println(String.format(patt, "timestamp", "play#", "preF", "flop", "turn", "river", "board"));
		System.out.println(String.format(patt, game.id, game.players.size(), game.potFlop, game.potTurn, game.potRiver,
				game.potShowdown, board));

		patt = "%-10s %3s %-5s %-5s %-5s %-5s %7s %7s %-5s";
		System.out.println("\nPlayer information");
		System.out.println("--------------------");
		System.out.println(
				String.format(patt, "Player", "Pos", "preF", "flop", "turn", "river", "chips", "Win", "Cards"));
		for (Player player : game.players) {
			StringBuffer cards = new StringBuffer();
			player.handCards.forEach(c -> cards.append(c.toString() + " "));
			System.out.println(String.format(patt, player.name, player.position, player.flopActions, player.turnActions,
					player.riverActions, player.showdownActions, player.chipsCount, player.chipsWins, cards));
		}
	}

	private int smallB, bigB;

	public List<TEntry<String, Double>> getTopPlayers(int firstN) {
		Hashtable<String, DescriptiveStatistics> statistics = new Hashtable<>();
		for (Game game : games) {
			for (Player player : game.players) {
				DescriptiveStatistics sts = statistics.getOrDefault(player.name, new DescriptiveStatistics(1000));
				sts.addValue(player.chipsCount);
				statistics.put(player.name, sts);
			}
		}

		ArrayList<TEntry<String, Double>> topPly = new ArrayList<>();
		for (String key : statistics.keySet()) {
			double exp = statistics.get(key).getMean() / (1000.0 * (double) bigB);
			topPly.add(new TEntry<>(key, exp));
		}
		Collections.sort(topPly, Collections.reverseOrder());
		return topPly.subList(0, firstN);
	}
	
	public static void main(String[] args) throws Exception {
		ICRReader reader = new ICRReader("holdemTest", 10, 20);

		reader.loadGameHistory();
		int firstN = 100;
		List<TEntry<String, Double>> top100 = reader.getTopPlayers(firstN);
		String patt3 = "%-10s %7.4f";
		System.out.println("\nTop "+firstN+" Players ");
		System.out.println("-------------------------");
		for (TEntry<String, Double> tEntry : top100) {
			System.out.println(String.format(patt3, tEntry.getKey(), tEntry.getValue()));
		}
		
		Game sample = reader.games.get(1);
		reader.tabularFormat(sample);
		
		String holec = "Td Ac";
		String boardc = "5c Tc Ah 5s";
		int dis = 50;
		long mills = System.currentTimeMillis();
		System.out.println("\nFind " + holec + " " + boardc + " distance = " + dis);
		System.out.println("-----------------------------------------------------");

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
