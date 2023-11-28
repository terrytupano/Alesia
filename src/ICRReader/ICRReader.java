package ICRReader;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.*;

import com.alee.utils.*;

import core.*;
import datasource.*;
import hero.UoAHandEval.*;

/**
 * @author Zack Tillotson
 *
 */

public class ICRReader {
	private final List<Game> games = new ArrayList<Game>();
	private String table;
	private Hashtable<String, String> actionsDic;
	private Hashtable<String, Integer> actionsValues;
	private static TreeMap<String, Integer> playersWins = new TreeMap<>();
	private static TreeMap<String, Integer> playersHands = new TreeMap<>();

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

		this.actionsValues = new Hashtable<>();
		actionsValues.put("b", 10);
		actionsValues.put("B", 20);
	}

	/**
	 * read all de available data and build the game history. the golbal variable
	 * {@link #games} will contain a list of all table games performed.
	 * 
	 */
	public void loadGameHistory() {
		String userdir = System.getProperty("user.dir");
		File dirFile = new File(userdir + "/IRCData/" + table);
		File[] dirs = FileUtils.listFiles(dirFile);
		for (File dir : dirs) {
			// int name = Integer.parseInt(dir.getName());
			// if (name < 199902)
			// continue;
			Flattenizer f = new Flattenizer(dir.getAbsolutePath());
			List<Game> games2 = f.getGames();
			// games.addAll(games2);
			// countHands(games2);
			// save(games2);
		}
	}

	private void save(List<Game> games) {
		long mills = System.currentTimeMillis();
		int gameCounter = 0;
		for (Game game : games) {
			if (ICRGame.first("gameId = ?", game.id) != null)
				continue;

			boolean saveGame = false;
			ICRGame icrGame = ICRGame.create();
			icrGame.setInteger("gameId", game.id);
			icrGame.setInteger("playersPreFlop", game.playersPreFlop);
			icrGame.setInteger("potPreFlop", game.potPreFlop);
			icrGame.setInteger("playersFlop", game.playersFlop);
			icrGame.setInteger("potFlop", game.potFlop);
			icrGame.setInteger("playersTurn", game.playersTurn);
			icrGame.setInteger("potTurn", game.potTurn);
			icrGame.setInteger("playersRiver", game.playersRiver);
			icrGame.setInteger("potRiver", game.potRiver);
			icrGame.setString("boardCards", game.boardCards.stream().map(c -> c.card).collect(Collectors.joining(" ")));
			List<ICRPlayer> icrPlayers = new ArrayList<>();
			for (Player player : game.players) {
				ICRPlayer icrPlayer = ICRPlayer.create();
				icrPlayer.setInteger("gameId", game.id);
				icrPlayer.setString("name", player.name);
				icrPlayer.setInteger("position", Integer.parseInt(player.position));
				icrPlayer.setInteger("playersLeft", player.playersLeft);
				icrPlayer.setInteger("chipsCount", player.chipsCount);
				icrPlayer.setInteger("chipsBet", player.chipsBet);
				icrPlayer.setInteger("chipsWins", player.chipsWins);
				icrPlayer.setString("preFlopActions", player.preFlopActions);
				icrPlayer.setString("flopActions", player.flopActions);
				icrPlayer.setString("turnActions", player.turnActions);
				icrPlayer.setString("riverActions", player.riverActions);
				icrPlayer.setString("handCards",
						player.handCards.stream().map(c -> c.card).collect(Collectors.joining(" ")));
				String handCards = icrPlayer.getString("handCards");
				String boardCards = icrGame.getString("boardCards");
				if (!StringUtils.isBlank(boardCards) && !StringUtils.isBlank(handCards)) {
					String cards = handCards + " " + boardCards;
					String[] cards2 = cards.split(" ");
					if (cards2.length != 7) {
						// don nothing the game will be not save it
						continue;
					}

					UoAHand flopHand = new UoAHand(
							cards2[0] + " " + cards2[1] + " " + cards2[2] + " " + cards2[3] + " " + cards2[4]);
					int flopRank = UoAHandEvaluator.rankHand(flopHand);
					icrPlayer.setInteger("flopRank", flopRank);

					UoAHand turnHand = new UoAHand(
							cards2[0] + " " + cards2[1] + " " + cards2[2] + " " + cards2[3] + " " + cards2[4] + " "
									+ cards2[5]);
					int turnRank = UoAHandEvaluator.rankHand(turnHand);
					icrPlayer.setInteger("turnRank", turnRank);

					UoAHand uoAHand = new UoAHand(cards);
					int riverRank = UoAHandEvaluator.rankHand(uoAHand);
					icrPlayer.setInteger("riverRank", riverRank);
					saveGame = true;
				}
				icrPlayers.add(icrPlayer);
			}
			// save game iif at least 2 players saw the showdown
			if (saveGame) {
				gameCounter++;
				icrGame.save();
				icrPlayers.forEach(p -> p.save());
			}
		}
		System.out.println(gameCounter + " Games saved. " + ((System.currentTimeMillis() - mills) / 1000) + " Seg");
	}


	private void countHands(List<Game> games) {
		for (Game game : games) {
			for (Player player : game.players) {
				int hands = 1;
				if (playersHands.containsKey(player.name)) {
					hands = playersHands.get(player.name) + 1;
				}
				playersHands.put(player.name, hands);

				if (player.chipsWins > 0) {
					int wins = 1;
					if (playersWins.containsKey(player.name)) {
						wins = playersWins.get(player.name) + 1;
					}
					playersWins.put(player.name, wins);
				}
			}
		}
		System.out.println("Total Hands: " + playersHands.size());
		System.out.println("Total Wins: " + playersWins.size());
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
				if (player.preFlopActions.length() >= round) {
					String action = player.preFlopActions.substring(round - 1, round);
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
				if (player.flopActions.length() >= round) {
					String action = player.flopActions.substring(round - 1, round);
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
				if (player.turnActions.length() >= round) {
					String action = player.turnActions.substring(round - 1, round);
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
		System.out.println(String.format(patt, game.id, game.players.size(), game.potPreFlop, game.potFlop, game.potTurn,
				game.potRiver, board));

		patt = "%-10s %3s %-5s %-5s %-5s %-5s %7s %7s %-5s";
		System.out.println("\nPlayer information");
		System.out.println("--------------------");
		System.out.println(
				String.format(patt, "Player", "Pos", "preF", "flop", "turn", "river", "chips", "Win", "Cards"));
		for (Player player : game.players) {
			StringBuffer cards = new StringBuffer();
			player.handCards.forEach(c -> cards.append(c.toString() + " "));
			System.out.println(String.format(patt, player.name, player.position, player.preFlopActions, player.flopActions,
					player.turnActions, player.riverActions, player.chipsCount, player.chipsWins, cards));
		}
	}

	public List<TEntry<String, Integer>> getTopPlayersByHands(int firstN) {
		ArrayList<TEntry<String, Integer>> topPly = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : playersHands.entrySet()) {
			topPly.add(new TEntry<>(entry.getKey(), entry.getValue()));
		}
		Collections.sort(topPly, Collections.reverseOrder());
		return topPly.subList(0, firstN);
	}

	public static void performImport() {
		ICRReader reader = new ICRReader("holdem2");
		reader.loadGameHistory();
		int firstN = 100;
		List<TEntry<String, Integer>> topHands = reader.getTopPlayersByHands(firstN);
		System.out.println("\nTop " + firstN + " Players ");
		System.out.printf("%-20s %10s %10s", "Name", "Hands", "wins");
		System.out.println("\n----------------------------------------");
		for (TEntry<String, Integer> tEntry : topHands) {
			System.out.println(
					String.format("%-20s %10d %10d", tEntry.getKey(), tEntry.getValue(),
							playersWins.get(tEntry.getKey())));
		}

		// Game sample = reader.games.get(1);
		// reader.tabularFormat(sample);

	}
}
