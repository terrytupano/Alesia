// This file is part of the 'texasholdem' project, an open source
// Texas Hold'em poker application written in Java.
//
// Copyright 2009 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// This file is part of the 'texasholdem' project, an open source
// Texas Hold'em poker application written in Java.
//
// Copyright 2009 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package hero.ozsoft;

import java.math.*;
import java.text.*;
import java.time.*;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.*;
import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.alee.utils.*;

import core.*;
import datasource.*;
import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.actions.*;
import hero.ozsoft.bots.*;

/**
 * Limit Texas Hold'em poker table. <br />
 * <br />
 * 
 * This class forms the heart of the poker engine. It controls the game flow for a single poker table.
 * 
 * @author Oscar Stigter
 */
public class PokerTable extends Task<Void, Void> {

	/** In fixed-limit games, the maximum number of raises per betting round. */
	private static final int MAX_RAISES = 3;

	/** Whether players will always call the showdown, or fold when no chance. */
	private static final boolean ALWAYS_CALL_SHOWDOWN = false;

	/**
	 * if the table has fewer players than allowed (field {@link #MIN_PLAYERS}), the simulation is restarted
	 */
	private static final String RESTAR = "RESTAR";

	/** current capacity of the table */
	public static final int MAX_CAPACITY = 9;

	/**
	 * indicate the number of tables muss be simulated before partial result request
	 */
	public static final int PARTIAL_RESULT_LIMIT = 10;

	public static final String PAUSE_TASK = "PAUSE_TASK";
	public static final String PAUSE_HERO = "PAUSE_HERO";
	public static final String PAUSE_PLAYER = "PAUSE_PLAYER";
	public static final String RESUME_ALL = "RESUME_ALL";

	/** Table type (poker variant). */
	private final TableType tableType;

	/** The players at the table. */
	private final List<Player> players;

	/** The active players in the current hand. */
	private final List<Player> activePlayers;

	/** The deck of cards. */
	private final UoADeck deck;

	/** The community cards on the board. */
	private final UoAHand board;

	private boolean holeCardsDealed;

	/** The current dealer position. */
	private int dealerPosition;

	/** counter to perform summarization */
	private int tableCounter;

	/** the current players with status active id DB */
	private int activeStrategies;

	/** The current dealer. */
	private Player dealer;

	/** The position of the acting player. */
	private int actorPosition;

	/** The acting player. */
	private Player actor;
	/** The minimum bet in the current hand. */
	private int minBet;
	/** The current bet in the current hand. */
	private int bet;
	/** All pots in the current hand (main pot and any side pots). */
	private final List<Pot> pots;
	/** The player who bet or raised last (aggressor). */
	private Player lastBettor;
	/** Number of raises in the current betting round. */
	private int raises;
	/** num of current played hands */
	private int numOfHand;

	public int buyIn, bigBlind, initialBigBlind;
	public BettingSequence bettingSequence;
	public int handsToSimulate;

	private boolean isTournament;

	private boolean pauseTask, pauseHero, pausePlayer;

	private String whenPlayerLose;

	private SimulationParameters simulationParameters;

	/** in simulation, identify this table with one unique id */
	private int tableId;

	/** compute hands x seg. */
	private DescriptiveStatistics statistics;

	private int lastBigBlindIncrement;

	private DB db;

	public PokerTable(int tableId, SimulationParameters parameters) {
		super(Alesia.getInstance());
		this.tableId = tableId;
		this.simulationParameters = parameters;
		this.tableType = TableType.NO_LIMIT;
		this.statistics = new DescriptiveStatistics(100);
		players = new ArrayList<Player>(MAX_CAPACITY);
		activePlayers = new ArrayList<Player>(MAX_CAPACITY);
		deck = new UoADeck();
		pots = new ArrayList<Pot>();
		board = new UoAHand();
		whenPlayerLose = RESTAR;

		// set task strings
		setTitle("Table simulation");
		setDescription("Description fo the simulation");
		setMessage("Simulation initialization ...");

		this.handsToSimulate = simulationParameters.getInteger("handsToSimulate");
		this.bigBlind = simulationParameters.getInteger("bigBlind");
		this.initialBigBlind = bigBlind;
		this.buyIn = simulationParameters.getInteger("buyIn");
		this.isTournament = simulationParameters.getBoolean("isTournament");
	}

	public int getTableId() {
		return tableId;
	}

	/**
	 * Adds a player.
	 * 
	 * @param player The player.
	 */
	public void addPlayer(Player player) {
		players.add(player);
	}

	/**
	 * Contributes to the pot.
	 * 
	 * @param amount The amount to contribute.
	 */
	private void contributePot(int amount) {
		for (Pot pot : pots) {
			if (!pot.hasContributer(actor)) {
				int potBet = pot.getBet();
				if (amount >= potBet) {
					// Regular call, bet or raise.
					pot.addContributer(actor);
					amount -= pot.getBet();
				} else {
					// Partial call (all-in); redistribute pots.
					pots.add(pot.split(actor, amount));
					amount = 0;
				}
			}
			if (amount <= 0) {
				break;
			}
		}
		if (amount > 0) {
			Pot pot = new Pot(amount);
			pot.addContributer(actor);
			pots.add(pot);
		}
	}

	/**
	 * Deals a number of community cards.
	 * 
	 * @param phaseName The name of the phase.
	 * @param noOfCards The number of cards to deal.
	 */
	private void dealCommunityCards(String phaseName, int noOfCards) {
		for (int i = 0; i < noOfCards; i++) {
			board.addCard(deck.deal());
		}
		notifyPlayersUpdated(false);
		notifyMessage("%s deals the %s.", dealer, phaseName);
		int street = PokerSimulator.FLOP_CARDS_DEALT;
		street = phaseName.equals("Turn") ? PokerSimulator.TURN_CARD_DEALT : street;
		street = phaseName.equals("River") ? PokerSimulator.RIVER_CARD_DEALT : street;
		bettingSequence.addStreet(street, board);
	}

	/**
	 * Deals the Hole Cards.
	 */
	private void dealHoleCards() {
		for (Player player : activePlayers) {
			ArrayList<UoACard> cs = new ArrayList<>();
			cs.add(deck.deal());
			cs.add(deck.deal());
			player.setCards(cs);

			// update sumaries
			bettingSequence.addSummary(player);
		}
		holeCardsDealed = true;
		notifyPlayersUpdated(false);
		notifyMessage("%s deals the hole cards.", dealer);
		bettingSequence.addStreet(PokerSimulator.HOLE_CARDS_DEALT, null);
	}

	/**
	 * Performs a betting round.
	 */
	private void doBettingRound() {
		// Determine the number of active players.
		int playersToAct = activePlayers.size();
		// Determine the initial player and bet size.
		if (board.size() == 0) {
			// PreFlop; player left of big blind starts, bet is the big blind.
			bet = bigBlind;
		} else {
			// Otherwise, player left of dealer starts, no initial bet.
			actorPosition = dealerPosition;
			bet = 0;
		}

		if (playersToAct == 2) {
			// Heads Up mode; player who is not the dealer starts.
			actorPosition = dealerPosition;
		}

		lastBettor = null;
		raises = 0;
		notifyBoardUpdated();

		while (playersToAct > 0) {
			// this pause allow me to see what is going on inside a hand for every player
			// TODO: (old implementation from table.s control buttons)
			if (pausePlayer && !isCancelled()) {
				ThreadUtils.sleepSafely(100);
				continue;
			}

			rotateActor();
			PlayerAction action = null;
			if (actor.isAllIn()) {
				// Player is all-in, so must check.
				action = PlayerAction.CHECK;
				bettingSequence.addAction(actor, action);
				playersToAct--;
			} else {
				// // Otherwise allow client to act.
				Set<PlayerAction> allowedActions = getAllowedActions(actor);
				action = actor.getClient().act(minBet, bet, allowedActions);

				// this pause allow me to see what is going on inside a hand only when hero is
				// about to act TODO: (old implementation from table.s control buttons)
				if ("Hero".equals(actor.getName()))
					while (pauseHero && !isCancelled()) {
						ThreadUtils.sleepSafely(100);
					}

				// // Verify chosen action to guard against broken clients (accidental or on
				// purpose).
				// if (!allowedActions.contains(action)) {
				// if (action instanceof BetAction &&
				// !allowedActions.contains(PlayerAction.BET)) {
				// throw new IllegalStateException(
				// String.format("Player '%s' acted with illegal Bet action", actor));
				// } else if (action instanceof RaiseAction &&
				// !allowedActions.contains(PlayerAction.RAISE)) {
				// throw new IllegalStateException(
				// String.format("Player '%s' acted with illegal Raise action", actor));
				// }
				// }

				playersToAct--;
				if (action instanceof CheckAction) {
					// Do nothing.
				} else if (action instanceof CallAction) {
					int betIncrement = bet - actor.getBet();
					if (betIncrement > actor.getCash()) {
						betIncrement = actor.getCash();
					}
					actor.payCash(betIncrement);
					actor.setBet(actor.getBet() + betIncrement);
					contributePot(betIncrement);
					bettingSequence.addAction(actor, action);
				} else if (action instanceof BetAction) {
					int amount = (tableType == TableType.FIXED_LIMIT) ? minBet : action.getAmount();
					if (amount < minBet && amount < actor.getCash()) {
						throw new IllegalStateException("Illegal client action: bet less than minimum bet!");
					}
					actor.setBet(amount);
					actor.payCash(amount);
					contributePot(amount);
					bet = amount;
					minBet = amount;
					lastBettor = actor;
					playersToAct = activePlayers.size();
					bettingSequence.addAction(actor, action);
				} else if (action instanceof RaiseAction) {
					int amount = (tableType == TableType.FIXED_LIMIT) ? minBet : action.getAmount();
					if (amount < minBet && amount < actor.getCash()) {
						throw new IllegalStateException("Illegal client action: raise less than minimum bet!");
					}
					bet += amount;
					minBet = amount;
					int betIncrement = bet - actor.getBet();
					if (betIncrement > actor.getCash()) {
						betIncrement = actor.getCash();
					}
					actor.setBet(bet);
					actor.payCash(betIncrement);
					contributePot(betIncrement);
					lastBettor = actor;
					raises++;
					if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
						// All players get another turn.
						playersToAct = activePlayers.size();
					} else {
						// Max. number of raises reached; other players get one more turn.
						playersToAct = activePlayers.size() - 1;
					}
					bettingSequence.addAction(actor, action);
				} else if (action instanceof FoldAction) {
					actor.setCards(null);
					activePlayers.remove(actor);
					actorPosition--;
					if (activePlayers.size() == 1) {
						// Only one player left, so he wins the entire pot.
						// <<<<<<<<<<<
						actor.setAction(action);
						notifyBoardUpdated();
						notifyPlayerActed();
						Player winner = activePlayers.get(0);
						int amount = getTotalPot();
						winner.win(amount);
						notifyBoardUpdated();
						notifyMessage("%s wins %d.", winner, amount);
						bettingSequence.addMessage("%s wins %d with %s", winner, amount, winner.getHand());
						playersToAct = 0;
					}
					bettingSequence.addAction(actor, action);
				} else {
					// Programming error, should never happen.
					throw new IllegalStateException("Invalid action: " + action);
				}
			}
			actor.setAction(action);
			if (playersToAct > 0) {
				notifyBoardUpdated();
				notifyPlayerActed();
			}
		}

		// Reset player's bets.
		for (Player player : activePlayers) {
			player.resetBet();
		}
		notifyBoardUpdated();
		notifyPlayersUpdated(false);
	}

	@Override
	protected Void doInBackground() throws Exception {
		for (Player player : players) {
			player.getClient().joinedTable(tableType, bigBlind, players);
		}
		int totalTables = 0;
		dealerPosition = -1;
		actorPosition = -1;
		numOfHand = 0;
		tableCounter = 0;
		activeStrategies = Integer.MAX_VALUE;
		db = Alesia.openDB();
		updateBigBlind();

		for (numOfHand = 1; (numOfHand < handsToSimulate && !isCancelled())
				&& ((!isTournament) || (isTournament && MAX_CAPACITY <= activeStrategies)); numOfHand++) {
			// pause ?
			if (pauseTask && !isCancelled()) {
				ThreadUtils.sleepSafely(250);
				continue;
			}

			long time1 = System.currentTimeMillis();

			// Counts active players
			int activePlayers = 0;
			for (Player player : players) {
				if (player.getCash() >= bigBlind) {
					activePlayers++;
				}
			}

			if (RESTAR.equals(whenPlayerLose) && activePlayers <= simulationParameters.getInteger("minPlayers")) {
				String msg = "Hand: " + numOfHand
						+ ", The table has less players that allow. Restartting the hole table.";
				tableCounter++;

				// fire partial result reques and wait until the summarization process is finish
				if (tableCounter >= PARTIAL_RESULT_LIMIT) {

					// for the taskGroup
					firePropertyChange(TaskGroup.PARTIAL_RESULT_REQUEST, false, true);

					// for the TTaskmonitor
					totalTables += tableCounter;
					firePropertyChange(PROP_MESSAGE, null, "Tables: " + totalTables + " Waiting for summarization ...");

					// spetial case: interactive simulation: handle partial result directly
					if (1 == simulationParameters.getInteger("numOfTasks")) {
						TaskGroup.processPartialResult();
						resetTableCounter();
					}
				}
				while (tableCounter >= PARTIAL_RESULT_LIMIT && !isCancelled()) {
					ThreadUtils.sleepSafely(250);
				}

				resetAndUpdatePlayers();
				updateBigBlind();
				updateActiveStrategies();

				// (don.t move) notify the bot. bot us this msg to init internal status
				notifyMessage(msg);
				bettingSequence.addMessage(msg);
			}

			// DONT MOVE. actions alter player.s cash
			int noOfActivePlayers = 0;
			for (Player player : players) {
				if (player.getCash() >= bigBlind) {
					noOfActivePlayers++;
				}
			}

			if (noOfActivePlayers > 1) {
				playHand();
				// System.out.println(getGlobalState());
			} else {
				// end the simulation when there is no more active players. if the flow reach
				// this point, is probably because whenPlayerLose = DO_NOTHING
				break;
			}

			statistics.addValue((System.currentTimeMillis() - time1) / 1000d);
			String speed = TResources.twoDigitFormat.format(statistics.getMean());
			int sb = (int) bigBlind / 2;
			firePropertyChange(PROP_MESSAGE, null,
					bigBlind + "-" + sb + " Hands: " + numOfHand + " Speed: " + speed + " Sec/Hand");
			setProgress(numOfHand, 0, handsToSimulate);
		}

		// Game over.
		board.makeEmpty();
		pots.clear();
		bet = 0;
		notifyBoardUpdated();
		for (Player player : players) {
			player.resetHand();
		}
		notifyPlayersUpdated(false);
		Alesia.showNotification("hero.msg04", numOfHand);
		notifyMessage("Game over.");
		db.close();
		return null;
	}

	/**
	 * increment the bb in tournament mode. this method Compute the AVG(tables) and if every strategy has played (avg)
	 * more that 10 tables, the bb is incremented 10% of the initial bb, 20% the second time and so on. the buyIn is
	 * also incremented
	 * 
	 * This method should be executed in taskGroup but i made here to allow me to test interactive. also, unilateral
	 * increment don.t affect the simulation (i think) eventualy all tables will be reach the same conclution
	 * 
	 * 
	 */
	private void updateBigBlind() {
		if (!isTournament)
			return;

		BigDecimal decimal = (BigDecimal) db.firstCell(
				"SELECT AVG(tables) FROM simulation_results WHERE tableId = ? AND status = ?", -1,
				SimulationResult.ACTIVE);
		int bbIncrement = decimal == null ? 0 : decimal.intValue() / 5;
		if (bbIncrement > lastBigBlindIncrement) {
			bigBlind = initialBigBlind + (initialBigBlind * bbIncrement / 100);
			buyIn = bigBlind * 100;
			for (Player player : players) {
				player.setCash(buyIn);
				player.getClient().joinedTable(tableType, bigBlind, players);
			}
			lastBigBlindIncrement = bbIncrement;
		}
	}

	public static int getTotalStrategies(SimulationParameters parameters) {
		int vars = parameters.getVariablesToSimulate();
		int grain = parameters.getInteger("grain");
		double totalStrategies = Math.pow((grain - 1), vars);
		return (int) totalStrategies;
	}

	private boolean areAllPresent() {
		long simulatedStrategies = getSimulatedStrategies();
		int totalStrategies = getTotalStrategies(simulationParameters);
		return totalStrategies == simulatedStrategies;
	}

	private long getSimulatedStrategies() {
		Long simulatedStrategies = (Long) db.firstCell("SELECT COUNT(*) FROM simulation_results WHERE tableId = ?", -1);
		return simulatedStrategies;
	}

	/**
	 * this method: - reset the player values - update the simulation result table - update the strategies for the next
	 * round
	 */
	private void resetAndUpdatePlayers() {
		List<Map> activeList = null;
		boolean allPresent = areAllPresent();
		for (Player player2 : players) {
			Client client = player2.getClient();
			// interactive environment Hero ist an instance of tableDialog
			Bot bot = null;
			if (client instanceof TableDialog)
				bot = (Bot) ((TableDialog) client).getProxyClient();
			else
				bot = (Bot) player2.getClient();

			SimulationResult result = bot.getBankrollSnapSchot();
			player2.resetHand();
			player2.setCash(buyIn);
			player2.getClient().joinedTable(tableType, bigBlind, players);
			simulationParameters.add(result);

			// normal
			if (!isTournament) {
				shuffle(bot.simulationVariables);
				continue;
			}

			// tournament: this level organization is to prevent the same strategy be used
			// by 2 or more troppers in the same table
			if (!allPresent) {
				// first level: random generation of strategies until there are all strategies
				// present.
				boolean retry = true;
				while (retry) {
					shuffle(bot.simulationVariables);
					String vars2 = bot.simulationVariables.toString();
					SimulationResult result2 = SimulationResult.findFirst("trooper = ? AND variables = ?", "*", vars2);
					retry = result2 != null && SimulationResult.RETIRED.equals(result2.getString("status"));
				}
			} else {
				// second level: if all strategies are present in the db file, shuffle a sublist
				if (activeList == null) {
					activeList = db.all("SELECT variables FROM simulation_results WHERE tableId = ? AND status = ?", -1,
							SimulationResult.ACTIVE);
					Collections.shuffle(activeList);
				}
				// if there no more active strategies, retire the player (happen at end of the
				// simulation where sumarization may retire strategies)
				if (activeList.isEmpty()) {
					player2.setCash(0);
				} else {
					Map<Object, Object> map = activeList.remove(0);
					String vars3 = map.get("variables").toString();
					String[] keysVals = vars3.split(",");
					for (String kv : keysVals) {
						String[] kv2 = kv.split("=");
						bot.simulationVariables.put(kv2[0].trim(), Integer.parseInt(kv2[1]));
					}

				}

			}
		}
	}

	private void shuffle(TreeMap<String, Integer> variables) {
		for (String key : variables.keySet()) {
			Integer newValue = getShuffleVariable();
			variables.put(key, newValue);
		}
	}

	/**
	 * this method update the global variable {@link #activeStrategies}
	 */
	private void updateActiveStrategies() {
		if (!isTournament)
			return;
		// wait until all strategies are present
		if (!areAllPresent())
			return;

		Long actives = (Long) db.firstCell("SELECT COUNT(*) FROM simulation_results WHERE tableId = ? AND status = ?",
				-1, SimulationResult.ACTIVE);
		activeStrategies = actives.intValue();
	}

	/**
	 * reset the simulated tables. use this method to allow this table continua after sumnarsatopm task
	 */
	public void resetTableCounter() {
		this.tableCounter = 0;
	}

	/**
	 * Performs the showdown.
	 */
	private void doShowdown() {
		// System.out.println("\n[DEBUG] Pots:");
		// for (Pot pot : pots) {
		// System.out.format(" %s\n", pot);
		// }
		// System.out.format("[DEBUG] Total: %d\n", getTotalPot());

		// Determine show order; start with all-in players...
		List<Player> showingPlayers = new ArrayList<Player>();
		for (Pot pot : pots) {
			for (Player contributor : pot.getContributors()) {
				if (!showingPlayers.contains(contributor) && contributor.isAllIn()) {
					showingPlayers.add(contributor);
				}
			}
		}
		// ...then last player to bet or raise (aggressor)...
		if (lastBettor != null) {
			if (!showingPlayers.contains(lastBettor)) {
				showingPlayers.add(lastBettor);
			}
		}
		// ...and finally the remaining players, starting left of the button.
		int pos = (dealerPosition + 1) % activePlayers.size();
		while (showingPlayers.size() < activePlayers.size()) {
			Player player = activePlayers.get(pos);
			if (!showingPlayers.contains(player)) {
				showingPlayers.add(player);
			}
			pos = (pos + 1) % activePlayers.size();
		}

		// Players automatically show or fold in order.
		boolean firstToShow = true;
		int bestHandValue = -1;
		bettingSequence.addMessage(BettingSequence.SEPARATOR + "Showdown");
		for (Player playerToShow : showingPlayers) {
			UoAHand hand = new UoAHand(board + " " + playerToShow.getHand());
			boolean doShow = ALWAYS_CALL_SHOWDOWN;
			if (!doShow) {
				if (playerToShow.isAllIn()) {
					// All-in players must always show.
					doShow = true;
					firstToShow = false;
				} else if (firstToShow) {
					// First player must always show.
					doShow = true;
					bestHandValue = UoAHandEvaluator.rankHand(hand);
					;
					firstToShow = false;
				} else {
					// Remaining players only show when having a chance to win.
					if (UoAHandEvaluator.rankHand(hand) >= bestHandValue) {
						doShow = true;
						bestHandValue = UoAHandEvaluator.rankHand(hand);
					}
				}
			}
			if (doShow) {
				// Show hand.
				for (Player player : players) {
					player.getClient().playerUpdated(playerToShow);
				}
				String hand2 = UoAHandEvaluator.nameHand(hand);
				notifyMessage("%s has %s.", playerToShow, hand2);
				bettingSequence.addMessage("%s has %s.", playerToShow, hand2);
			} else {
				// Fold.
				playerToShow.setCards(null);
				activePlayers.remove(playerToShow);
				for (Player player : players) {
					if (player.equals(playerToShow)) {
						player.getClient().playerUpdated(playerToShow);
					} else {
						// Hide secret information to other players.
						player.getClient().playerUpdated(playerToShow.publicClone());
					}
				}
				notifyMessage("%s muck.", playerToShow);
				bettingSequence.addMessage("%s muck.", playerToShow);
			}
		}

		// Sort players by hand value (highest to lowest).
		Map<Integer, List<Player>> rankedPlayers = new TreeMap<Integer, List<Player>>(Comparator.reverseOrder());
		for (Player player : activePlayers) {
			// Create a hand with the community cards and the player's hole cards.
			UoAHand hand = new UoAHand(board + " " + player.getHand());
			// Store the player together with other players with the same hand value.
			// HandValue handValue = new HandValue(hand);
			// System.out.format("[DEBUG] %s: %s\n", player, handValue);
			int handValue = UoAHandEvaluator.rankHand(hand);
			List<Player> playerList = rankedPlayers.get(handValue);
			if (playerList == null) {
				playerList = new ArrayList<Player>();
			}
			playerList.add(player);
			rankedPlayers.put(handValue, playerList);
		}

		// Per rank (single or multiple winners), calculate pot distribution.
		int totalPot = getTotalPot();
		Map<Player, Integer> potDivision = new HashMap<Player, Integer>();
		for (Integer handValue : rankedPlayers.keySet()) {
			List<Player> winners = rankedPlayers.get(handValue);
			for (Pot pot : pots) {
				// Determine how many winners share this pot.
				int noOfWinnersInPot = 0;
				for (Player winner : winners) {
					if (pot.hasContributer(winner)) {
						noOfWinnersInPot++;
					}
				}
				if (noOfWinnersInPot > 0) {
					// Divide pot over winners.
					int potShare = pot.getValue() / noOfWinnersInPot;
					for (Player winner : winners) {
						if (pot.hasContributer(winner)) {
							Integer oldShare = potDivision.get(winner);
							if (oldShare != null) {
								potDivision.put(winner, oldShare + potShare);
							} else {
								potDivision.put(winner, potShare);
							}
						}
					}
					// Determine if we have any odd chips left in the pot.
					int oddChips = pot.getValue() % noOfWinnersInPot;
					if (oddChips > 0) {
						// Divide odd chips over winners, starting left of the dealer.
						pos = dealerPosition;
						while (oddChips > 0) {
							pos = (pos + 1) % activePlayers.size();
							Player winner = activePlayers.get(pos);
							Integer oldShare = potDivision.get(winner);
							if (oldShare != null) {
								potDivision.put(winner, oldShare + 1);
								// System.out.format("[DEBUG] %s receives an odd chip from the pot.\n", winner);
								oddChips--;
							}
						}
					}
					pot.clear();
				}
			}
		}

		// Divide winnings.
		StringBuilder winnerText = new StringBuilder();
		int totalWon = 0;
		for (Player winner : potDivision.keySet()) {
			int potShare = potDivision.get(winner);
			winner.win(potShare);
			totalWon += potShare;
			if (winnerText.length() > 0) {
				winnerText.append(", ");
			}
			winnerText.append(String.format("%s wins %d with %s", winner, potShare, winner.getHand()));

			notifyPlayersUpdated(true);
		}
		winnerText.append('.');
		notifyMessage(winnerText.toString());
		bettingSequence.addMessage(winnerText.toString());

		// Sanity check.
		if (totalWon != totalPot) {
			System.out.println(LocalDateTime.now() + " Incorrect pot division!");
			// terry commented to allow the simulation to continue
			// throw new IllegalStateException("Incorrect pot division!");
		}
	}

	/**
	 * return a copy of the current player (the player in turn)
	 * 
	 * @return the current player
	 */
	public Player getActor() {
		return actor.publicClone();
	}

	/**
	 * Returns the allowed actions of a specific player.
	 * 
	 * @param player The player.
	 * 
	 * @return The allowed actions.
	 */
	private Set<PlayerAction> getAllowedActions(Player player) {
		Set<PlayerAction> actions = new HashSet<PlayerAction>();
		if (player.isAllIn()) {
			actions.add(PlayerAction.CHECK);
		} else {
			int actorBet = actor.getBet();
			if (bet == 0) {
				actions.add(PlayerAction.CHECK);
				if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
					actions.add(PlayerAction.BET);
				}
			} else {
				if (actorBet < bet) {
					actions.add(PlayerAction.CALL);
					if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
						actions.add(PlayerAction.RAISE);
					}
				} else {
					actions.add(PlayerAction.CHECK);
					if (tableType == TableType.NO_LIMIT || raises < MAX_RAISES || activePlayers.size() == 2) {
						actions.add(PlayerAction.RAISE);
					}
				}
			}
			actions.add(PlayerAction.FOLD);
		}
		return actions;
	}

	/**
	 * return the current round expressed in cards numbers. 2 = preflop, 5 = Flop, 6 = Turn, 7 = River
	 * 
	 * @return # of dealt cards
	 */
	public int getCurrentRound() {
		return board.size() + (holeCardsDealed == true ? 2 : 0);
	}

	public List<Player> getPlayers() {
		// ArrayList<Player> tmp = new ArrayList<>();
		// players.forEach(p -> tmp.add(p.publicClone()));
		return players;
	}

	public TableDialog getTableDialog() {
		TableDialog dialog = new TableDialog(this);
		setInputBlocker(dialog);
		return dialog;
	}

	/**
	 * Returns the total pot size.
	 * 
	 * @return The total pot size.
	 */
	private int getTotalPot() {
		int totalPot = 0;
		for (Pot pot : pots) {
			totalPot += pot.getValue();
		}
		return totalPot;
	}

	public boolean isPaused() {
		return pauseTask || pauseHero || pausePlayer;
	}

	/**
	 * Notifies clients that the board has been updated.
	 */
	private void notifyBoardUpdated() {
		int pot = getTotalPot();
		for (Player player : players) {
			player.getClient().boardUpdated(board, bet, pot);
		}
	}

	/**
	 * Notifies listeners with a custom game message.
	 * 
	 * @param message The formatted message.
	 * @param args    Any arguments.
	 */
	private void notifyMessage(String message, Object... args) {
		message = String.format(message, args);
		for (Player player : players) {
			player.getClient().messageReceived(message);
		}
	}

	/**
	 * Notifies clients that a player has acted.
	 */
	private void notifyPlayerActed() {
		for (Player p : players) {
			Player playerInfo = p.equals(actor) ? actor : actor.publicClone();
			p.getClient().playerActed(playerInfo);
		}
	}

	/**
	 * Notifies clients that one or more players have been updated. <br />
	 * <br />
	 * 
	 * A player's secret information is only sent its own client; other clients see only a player's public information.
	 * 
	 * @param showdown Whether we are at the showdown phase.
	 */
	private void notifyPlayersUpdated(boolean showdown) {
		for (Player playerToNotify : players) {
			for (Player player : players) {
				if (!showdown && !player.equals(playerToNotify)) {
					// Hide secret information to other players.
					player = player.publicClone();
				}
				playerToNotify.getClient().playerUpdated(player);
			}
		}
	}

	public void pause(String pauseType) {
		pauseHero = false;
		pausePlayer = false;
		pauseTask = false;
		if (RESUME_ALL.equals(pauseType))
			return;

		pauseHero = PAUSE_HERO.equals(pauseType);
		pausePlayer = PAUSE_PLAYER.equals(pauseType);
		pauseTask = PAUSE_TASK.equals(pauseType);
	}

	/**
	 * Return the log recorded for this hand. This log string contain newline character, so can be read as standar
	 * multiline poker log
	 * 
	 * @return the log
	 */
	public String getGlobalState() {
		String result = "hand " + numOfHand;
		result += " Time: " + TStringUtils.getTime(System.currentTimeMillis());
		result += "\n" + bettingSequence.getSequence();
		return result;
	}

	/**
	 * Plays a single hand.
	 * 
	 * @throws InterruptedException
	 */
	private void playHand() throws InterruptedException {
		resetHand();
		// Small blind.
		if (activePlayers.size() > 2) {
			rotateActor();
		}
		postSmallBlind();

		// Big blind.
		rotateActor();
		postBigBlind();

		// Pre-Flop.
		dealHoleCards();
		doBettingRound();

		// Flop.
		if (activePlayers.size() > 1) {
			bet = 0;
			dealCommunityCards("Flop", 3);
			doBettingRound();

			// Turn.
			if (activePlayers.size() > 1) {
				bet = 0;
				dealCommunityCards("Turn", 1);
				minBet = 2 * bigBlind;
				doBettingRound();

				// River.
				if (activePlayers.size() > 1) {
					bet = 0;
					dealCommunityCards("River", 1);
					doBettingRound();

					// Showdown.
					if (activePlayers.size() > 1) {
						bet = 0;
						doShowdown();
					}
				}
			}
		}
	}

	/**
	 * Posts the big blind.
	 */
	private void postBigBlind() {
		actor.postBigBlind(bigBlind);
		contributePot(bigBlind);
		notifyBoardUpdated();
		notifyPlayerActed();
		BigBlindAction action = new BigBlindAction(bigBlind);
		bettingSequence.addAction(actor, action);
	}

	/**
	 * Posts the small blind.
	 */
	private void postSmallBlind() {
		final int smallBlind = bigBlind / 2;
		actor.postSmallBlind(smallBlind);
		contributePot(smallBlind);
		notifyBoardUpdated();
		notifyPlayerActed();
		SmallBlindAction action = new SmallBlindAction(smallBlind);
		bettingSequence.addAction(actor, action);
	}

	/**
	 * Resets the game for a new hand.
	 */
	private void resetHand() {
		bettingSequence = new BettingSequence();
		// Clear the board.
		holeCardsDealed = false;
		board.makeEmpty();
		pots.clear();
		notifyBoardUpdated();

		// Determine the active players.
		activePlayers.clear();
		for (Player player : players) {
			player.resetHand();
			// Player must be able to afford at least the big blind.
			if (player.getCash() >= bigBlind) {
				activePlayers.add(player);
			}
		}

		String result = "Players: ";
		for (Player player : activePlayers) {
			result += " " + player.getChair() + " " + player.getName();
		}
		bettingSequence.addMessage(result);

		// Rotate the dealer button.
		dealerPosition = (dealerPosition + 1) % activePlayers.size();
		dealer = activePlayers.get(dealerPosition);

		// terry: mark the dealer for BettingSequence usage
		activePlayers.forEach(p -> p.isDealer = false);
		dealer.isDealer = true;

		// Shuffle the deck.
		deck.shuffle();

		// Determine the first player to act.
		actorPosition = dealerPosition;
		actor = activePlayers.get(actorPosition);

		// Set the initial bet to the big blind.
		minBet = bigBlind;
		bet = minBet;

		// Notify all clients a new hand has started.
		for (Player player : players) {
			player.getClient().handStarted(dealer);
		}
		notifyPlayersUpdated(false);
		notifyMessage("Hand: %d, %s is the dealer.", numOfHand, dealer);
		bettingSequence.addMessage("%s is the dealer.", dealer);
	}

	/**
	 * Rotates the position of the player in turn (the actor).
	 */
	private void rotateActor() {
		actorPosition = (actorPosition + 1) % activePlayers.size();
		actor = activePlayers.get(actorPosition);
		for (Player player : players) {
			player.getClient().actorRotated(actor);
		}
	}

	/**
	 * return shuffle list of integers based on range [0, 100] / {@link #GRAIN} e.g: if GRAIN = 10 what i want is
	 * 10,20,...,90 because in simulation i want to test the ranges [0,10], [11,20], ... [91,100]
	 * 
	 * @return the shuffle list
	 */
	public static List<Integer> getShuffleList(int grain) {
		List<Integer> integers = new ArrayList<>();
		int step = 100 / grain;
		for (int i = 1; i < grain; i++) {
			integers.add(step * i);
		}
		Collections.shuffle(integers);
		return integers;
	}

	/**
	 * return the next assignable value from {@link PokerTable#getShuffleList()}
	 * 
	 * @return the new value
	 */
	public int getShuffleVariable() {
		int grain = simulationParameters.getInteger("grain");
		List<Integer> integers = getShuffleList(grain);
		int i = integers.get(0);
		return i;
	}
}
