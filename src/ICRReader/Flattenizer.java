package ICRReader;

import java.io.*;
import java.util.*;

public class Flattenizer {

	private final List<Game> games = new ArrayList<Game>();
	// private final Map<String, PlayerHistory> playerHistory = new HashMap<String,
	// PlayerHistory>();
	private final String dir;
	private int gameCounter;
	private int playersCounter;
	private int actionsCounter;
	private int deletedGames;

	public Flattenizer(String dir) {
		this.dir = dir;
	}

	private void loadGameHistory(String directory) throws FileNotFoundException {
		File file = new File(directory, "hdb");
		Scanner in = new Scanner(file);
		while (in.hasNextLine()) {
			// errors in this section, dont add the game to games list
			String line = in.nextLine();
			Scanner inLine = new Scanner(line);
			try {
				String id = inLine.next();
				String dealer = inLine.next();
				String handCount = inLine.next();
				String playerCount = inLine.next();

				String pp[] = inLine.next().split("/"); ///
				String playersPreFlop = pp[0];
				String potPreFlop = pp[1];

				pp = inLine.next().split("/");
				String playersFlop = pp[0];
				String potFlop = pp[1]; ///

				pp = inLine.next().split("/");
				String playersTurn = pp[0];
				String potTurn = pp[1];

				pp = inLine.next().split("/");
				String playersRiver = pp[0];
				String potRiver = pp[1];

				List<ICRCard> boardCards = new ArrayList<ICRCard>();
				while (inLine.hasNext()) {
					boardCards.add(new ICRCard(inLine.next()));
				}
				inLine.close();
				gameCounter++;
				games.add(new Game(id, playersPreFlop, potPreFlop, playersFlop, potFlop, playersTurn, potTurn,
						playersRiver,
						potRiver, boardCards));
			} catch (Exception e) {
				inLine.close();
				// do nothing
			}
		}
		in.close();
	}

	/**
	 * update the files {@link Game#playersNameList} with the infor read from
	 * hroster file
	 * 
	 * @param directory
	 * @throws Exception
	 */
	private void loadRosterInformation(String directory) throws Exception {

		Scanner in = new Scanner(new File(directory, "hroster"));

		Iterator<Game> iter = games.iterator();

		while (in.hasNextLine()) {
			String line = in.nextLine();
			Scanner inLine = new Scanner(line);

			String id = inLine.next();
			String playerCount = inLine.next();
			List<String> players = new ArrayList<String>();
			while (inLine.hasNext()) {
				players.add(inLine.next());
			}
			inLine.close();

			try {
				Game nextHand = iter.next();
				if (!id.equals(nextHand.id)) {
					// terry: dont discard the hole database, discard only all the games where this
					// plager are present
					nextHand.delete = true;
					continue;
					// throw new Exception("Missing hand information");
				}

				nextHand.playersNameList.addAll(players);
			} catch (Exception e) {
				// do nothing
			}
		}
		in.close();

	}

	private void loadPlayerActions(String directory) throws Exception {

		// Player Name -> Id -> Action map
		Map<String, Map<String, Player>> playerToHandMap = new HashMap<String, Map<String, Player>>();

		String pdbDir = directory + "/pdb/";
		File pDir = new File(pdbDir);
		for (String pFile : pDir.list()) {
			playersCounter++;
			Map<String, Player> handToActionMap = new HashMap<String, Player>();
			String playerName = null;

			// files with access denied, continue the player will be not added to
			// handToActionMap
			Scanner in = null;
			try {
				in = new Scanner(new File(pdbDir, pFile));
			} catch (Exception e) {
				// continue
				continue;
			}
			while (in.hasNext()) {
				String line = in.nextLine();
				Scanner inLine = new Scanner(line);
				// error in this secction dont add the player to handToActionMap. so in the next
				// secction, the game will be marked as deleted
				try {
					playerName = inLine.next();
					String id = inLine.next();
					String playerNum = inLine.next();
					String position = inLine.next();
					String flop = inLine.next();
					String turn = inLine.next();
					String river = inLine.next();
					String showdown = inLine.next();
					String bankRoll = inLine.next();
					String betAmount = inLine.next();
					String winAmount = inLine.next();

					List<ICRCard> cards = new ArrayList<ICRCard>();
					while (inLine.hasNext()) {
						cards.add(new ICRCard(inLine.next()));
					}
					inLine.close();
					actionsCounter++;
					handToActionMap.put(id, new Player(playerName, position, playerNum, bankRoll, flop, turn, river,
							showdown, betAmount, winAmount, cards));
				} catch (Exception e) {
					inLine.close();
					playerName = null; // mark to no add in playerToHandMap
				}
			}
			in.close();
			if (playerName != null)
				playerToHandMap.put(playerName, handToActionMap);
		}

		for (Game ph : games) {
			for (String playerName : ph.playersNameList) {
				Map<String, Player> map = playerToHandMap.get(playerName);
				if (map == null) {
					// terry: dont discard the hole database, discard only all the games where this
					// plager are present
					ph.delete = true;
					continue;
					// throw new Exception("No data found for player " + playerName);
				}
				Player action = map.get(ph.id);
				if (action != null) {
					ph.players.add(action);
					// Collections.sort(ph.players, (o1,o2) -> o1.position.compareTo(o2.position));
					Collections.sort(ph.players, new Comparator<Player>() {
						@Override
						public int compare(Player o1, Player o2) {
							return o1.position.compareTo(o2.position);
						}
					});
				}
			}
		}
	}

	public List<Game> getGames() {
		long mills = System.currentTimeMillis();
		try {
			System.out.println("Processing dir " + dir);
			loadGameHistory(dir);
			loadRosterInformation(dir);
			loadPlayerActions(dir);

			// remove maked as delete games
			List<Game> games2 = new ArrayList<Game>();
			games.forEach(g -> {
				if (g.delete)
					deletedGames++;
				else
					games2.add(g);
			});
			System.out.println(gameCounter + " Games, (deleted " + deletedGames + ") " + playersCounter
					+ " players and " + actionsCounter
					+ " actions Processed in "
					+ ((System.currentTimeMillis() - mills) / 1000) + " Seg");

			return games2;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<Game>();
		}
	}
}
