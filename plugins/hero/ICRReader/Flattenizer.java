package plugins.hero.ICRReader;

import java.io.*;
import java.util.*;

public class Flattenizer {

	private final List<Game> games = new ArrayList<Game>();
	// private final Map<String, PlayerHistory> playerHistory = new HashMap<String, PlayerHistory>();
	private final String dir;

	public Flattenizer(String dir) {
		this.dir = dir;
	}

	private void loadGameHistory(String directory) throws FileNotFoundException {
		Scanner in = new Scanner(new File(directory, "hdb"));
		int counter = 0;
		long mills = System.currentTimeMillis();
		while (in.hasNextLine()) {
			counter++;
			String line = in.nextLine();
			Scanner inLine = new Scanner(line);
			String id = inLine.next();
			String dealer = inLine.next();
			String handCount = inLine.next();
			String playerCount = inLine.next();
			String potFlop = inLine.next().split("/")[1];
			String potTurn = inLine.next().split("/")[1];
			String potRiver = inLine.next().split("/")[1];
			String potShowdown = inLine.next().split("/")[1];
			List<ICRCard> boardCards = new ArrayList<ICRCard>();
			while (inLine.hasNext()) {
				boardCards.add(new ICRCard(inLine.next()));
			}
			inLine.close();

			games.add(new Game(id, potFlop, potTurn, potRiver, potShowdown, boardCards));

		}
		in.close();
		System.out
				.println(counter + " processed HDB lines in " + ((System.currentTimeMillis() - mills) / 1000) + " Seg");
	}

	/**
	 * update the files {@link Game#playersNameList} with the infor read from hroster file
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

			Game nextHand = iter.next();

			if (!id.equals(nextHand.id)) {
				throw new Exception("Missing hand information");
			}

			nextHand.playersNameList.addAll(players);

		}
		in.close();

	}

	private void loadPlayerActions(String directory) throws Exception {

		// Player Name -> Id -> Action map
		Map<String, Map<String, Player>> playerToHandMap = new HashMap<String, Map<String, Player>>();

		String pdbDir = directory + "/pdb/";
		File pDir = new File(pdbDir);
		int counter = 0;
		long mills = System.currentTimeMillis();
		for (String pFile : pDir.list()) {
			counter++;
			Map<String, Player> handToActionMap = new HashMap<String, Player>();
			String playerName = null;

			Scanner in = new Scanner(new File(pdbDir, pFile));
			while (in.hasNext()) {

				String line = in.nextLine();
				Scanner inLine = new Scanner(line);

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

				handToActionMap.put(id, new Player(playerName, position, playerNum, bankRoll, flop, turn, river,
						showdown, betAmount, winAmount, cards));

			}
			in.close();
			playerToHandMap.put(playerName, handToActionMap);
		}

		for (Game ph : games) {
			for (String playerName : ph.playersNameList) {
				Player action = playerToHandMap.get(playerName).get(ph.id);
				if (action != null) {
					ph.players.add(action);
//					Collections.sort(ph.players, (o1,o2) -> o1.position.compareTo(o2.position));
					Collections.sort(ph.players, new Comparator<Player>() {
						@Override
						public int compare(Player o1, Player o2) {
							return o1.position.compareTo(o2.position);
						}
					});
				}
			}
		}
		System.out.println(counter + " Processed users in " + ((System.currentTimeMillis() - mills) / 1000) + " Seg");
	}

	public List<Game> getGames() {
		try {
			loadGameHistory(dir);
			loadRosterInformation(dir);
			loadPlayerActions(dir);
			return games;
		} catch (Exception e) {
			System.err.println("ERROR in directory: " + dir + " " + e.getMessage());
			return new ArrayList<Game>();
		}
	}
}
