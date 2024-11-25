package ICRReader;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import javax.swing.*;

import org.apache.commons.lang3.*;

import com.alee.utils.*;

import core.*;
import datasource.*;
import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.*;

/**
 * @author Zack Tillotson
 *
 */

public class ICRReader {
	private String table;
	private Hashtable<String, String> actionsToText = new Hashtable<>();

	public ICRReader(String table) {
		this.table = table;
		actionsToText.put("-", "no action; player is no longer contesting pot");
		actionsToText.put("B", "blind bet");
		actionsToText.put("f", "fold");
		actionsToText.put("k", "check");
		actionsToText.put("b", "bet");
		actionsToText.put("c", "call");
		actionsToText.put("r", "raise");
		actionsToText.put("A", "all-in");
		actionsToText.put("Q", "quits game");
		actionsToText.put("K", "kicked from game");
	}

	/**
	 * read all ICR files in the folder ICRData and parse it. the result are stored
	 * in ICRGame and ICRGameDetail tables.
	 * 
	 */
	private void loadGameHistory() {
		String userdir = System.getProperty("user.dir");
		File dirFile = new File(userdir + "/IRCData/" + table);
		File[] dirs = FileUtils.listFiles(dirFile);
		for (File dir : dirs) {
			Flattenizer f = new Flattenizer(dir.getAbsolutePath());
			List<Game> games2 = f.getGames();
			save(games2);
		}
	}

	/**
	 * save the list of parsed Game into the corresponding db tables. this method
	 * will store only:
	 * - games with playerLeft <= Table.CAPACITY
	 * - games with at leas flop card present.
	 * 
	 * @param games the parsed games
	 */
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
				gameDetail.setString("actionsPreFlop", player.preFlopActions);
				gameDetail.setString("actionsFlop", player.flopActions);
				gameDetail.setString("actionsTurn", player.turnActions);
				gameDetail.setString("actionsRiver", player.riverActions);
				gameDetail.setString("handCards",
						player.handCards.stream().map(c -> c.card).collect(Collectors.joining(" ")));
				String handCards = gameDetail.getString("handCards");
				String boardCards = icrGame.getString("boardCards");
				if (!StringUtils.isBlank(boardCards) && !StringUtils.isBlank(handCards)) {
					String cards = handCards + " " + boardCards;
					String[] cards2 = cards.split(" ");
					// at leas floop and the right # of players
					if (player.playersLeft > PokerTable.MAX_CAPACITY || StringUtils.isBlank(boardCards)) {
						// don nothing the game will be not save it
						continue;
					}

					UoAHand flopHand = new UoAHand(
							cards2[0] + " " + cards2[1] + " " + cards2[2] + " " + cards2[3] + " " + cards2[4]);
					int rankFlop = UoAHandEvaluator.rankHand(flopHand);
					gameDetail.setInteger("rankFlop", rankFlop);

					if (cards2.length >= 6) {
						UoAHand turnHand = new UoAHand(
								cards2[0] + " " + cards2[1] + " " + cards2[2] + " " + cards2[3] + " " + cards2[4] + " "
										+ cards2[5]);
						int rankTurn = UoAHandEvaluator.rankHand(turnHand);
						gameDetail.setInteger("rankTurn", rankTurn);
					}

					if (cards2.length == 7) {
						UoAHand uoAHand = new UoAHand(cards);
						int rankRiver = UoAHandEvaluator.rankHand(uoAHand);
						gameDetail.setInteger("rankRiver", rankRiver);
					}

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
		Object[] options = { "OK", "CANCEL" };
		int opt = JOptionPane.showOptionDialog(Alesia.getMainFrame(), "Delete files?", "Warning", JOptionPane.DEFAULT_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		if (opt != 0) {
			System.out.println("Aborted");
			return;
		}
		ICRGame.deleteAll();
		ICRGameDetail.deleteAll();
		ICRReader reader = new ICRReader("holdem2");
		reader.loadGameHistory();
	}
}
