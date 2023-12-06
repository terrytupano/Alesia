package ICRReader;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.*;

import com.alee.utils.*;

import datasource.*;
import hero.*;
import hero.UoAHandEval.*;

/**
 * @author Zack Tillotson
 *
 */

public class ICRReader {
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
	 * read all de available data and build the game history. the golbal variable
	 * {@link #games} will contain a list of all table games performed.
	 * 
	 */
	public void loadGameHistory() {
		String userdir = System.getProperty("user.dir");
		File dirFile = new File(userdir + "/IRCData/" + table);
		File[] dirs = FileUtils.listFiles(dirFile);
		for (File dir : dirs) {
			Flattenizer f = new Flattenizer(dir.getAbsolutePath());
			List<Game> games2 = f.getGames();
			save(games2);
		}
	}

	private void save(List<Game> games) {
		long mills = System.currentTimeMillis();
		int gameCounter = 0;
		for (int i = 0; i < games.size(); i++) {
			Game game = games.get(i);
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
			List<ICRGameDetail> gameDetails = new ArrayList<>();
			for (Player player : game.players) {
				ICRGameDetail gameDetail = ICRGameDetail.create();
				gameDetail.setInteger("gameId", game.id);
				gameDetail.setString("name", player.name);
				gameDetail.setInteger("position", Integer.parseInt(player.position));
				gameDetail.setInteger("playersLeft", player.playersLeft);
				gameDetail.setInteger("chipsCount", player.chipsCount);
				gameDetail.setInteger("chipsBet", player.chipsBet);
				gameDetail.setInteger("chipsWins", player.chipsWins);
				gameDetail.setString("preFlopActions", player.preFlopActions);
				gameDetail.setString("flopActions", player.flopActions);
				gameDetail.setString("turnActions", player.turnActions);
				gameDetail.setString("riverActions", player.riverActions);
				gameDetail.setString("handCards",
						player.handCards.stream().map(c -> c.card).collect(Collectors.joining(" ")));
				String handCards = gameDetail.getString("handCards");
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
					gameDetail.setInteger("flopRank", flopRank);

					UoAHand turnHand = new UoAHand(
							cards2[0] + " " + cards2[1] + " " + cards2[2] + " " + cards2[3] + " " + cards2[4] + " "
									+ cards2[5]);
					int turnRank = UoAHandEvaluator.rankHand(turnHand);
					gameDetail.setInteger("turnRank", turnRank);

					UoAHand uoAHand = new UoAHand(cards);
					int riverRank = UoAHandEvaluator.rankHand(uoAHand);
					gameDetail.setInteger("riverRank", riverRank);
					saveGame = true;
				}
				gameDetails.add(gameDetail);
			}
			// save game iif at least 2 players saw the showdown
			if (saveGame) {
				gameCounter++;
				icrGame.save();
				gameDetails.forEach(p -> p.save());
			}
			TWekaUtils.printProgress("Saving", games.size(), i);
		}
		System.out.println(gameCounter + " Games saved. " + ((System.currentTimeMillis() - mills) / 1000) + " Seg");
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
		System.out
				.println(String.format(patt, game.id, game.players.size(), game.potPreFlop, game.potFlop, game.potTurn,
						game.potRiver, board));

		patt = "%-10s %3s %-5s %-5s %-5s %-5s %7s %7s %-5s";
		System.out.println("\nPlayer information");
		System.out.println("--------------------");
		System.out.println(
				String.format(patt, "Player", "Pos", "preF", "flop", "turn", "river", "chips", "Win", "Cards"));
		for (Player player : game.players) {
			StringBuffer cards = new StringBuffer();
			player.handCards.forEach(c -> cards.append(c.toString() + " "));
			System.out.println(
					String.format(patt, player.name, player.position, player.preFlopActions, player.flopActions,
							player.turnActions, player.riverActions, player.chipsCount, player.chipsWins, cards));
		}
	}

	public static void performImport() {
		ICRReader reader = new ICRReader("holdem2");
		reader.loadGameHistory();
	}
}
