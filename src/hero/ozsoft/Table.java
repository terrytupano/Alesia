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
 * This class forms the heart of the poker engine. It controls the game flow for
 * a single poker table.
 * 
 * @author Oscar Stigter
 */
public class Table extends Task<Void, Void> {

	/** In fixed-limit games, the maximum number of raises per betting round. */
	private static final int MAX_RAISES = 3;

	/** Whether players will always call the showdown, or fold when no chance. */
	private static final boolean ALWAYS_CALL_SHOWDOWN = false;

	/** The simulation continue to the end. */
	private static final String DO_NOTHING = "DO_NOTHING";

	/**
	 * if the table has fewer players than allowed (field {@link #MIN_PLAYERS}), the
	 * simulation is restarted
	 */
	private static final String RESTAR = "RESTAR";

	/** current capacity of the table */
	public static final int CAPACITY = 8;

	/** Min. Num. of player for {@link #RESTAR} action. */
	private static int MIN_PLAYERS = 5;

	public static final String PAUSE_TASK = "PAUSE_TASK";

	public static final String PAUSE_HERO = "PAUSE_HERO";

	public static final String PAUSE_PLAYER = "PAUSE_PLAYER";

	public static final String RESUME_ALL = "RESUME_ALL";

	private static ThreadLocal<List<Integer>> threadLocal = new ThreadLocal<>();

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

	public int buyIn, bigBlind;

	public int simulationsHand;

	private boolean pauseTask, pauseHero, pausePlayer;

	private String whenPlayerLose;

	private SimulationParameters simulationParameters;

	private TTaskMonitor taskMonitor;

	/** compute hands x seg. */
	private DescriptiveStatistics statistics;

	public Table(SimulationParameters parameters) {
		super(Alesia.getInstance());
		this.simulationParameters = parameters;
		this.simulationsHand = simulationParameters.getInteger("simulationsHands");
		this.tableType = TableType.NO_LIMIT;
		this.statistics = new DescriptiveStatistics(100); // to include DB access time.
		players = new ArrayList<Player>(CAPACITY);
		activePlayers = new ArrayList<Player>(CAPACITY);
		deck = new UoADeck();
		pots = new ArrayList<Pot>();
		board = new UoAHand();
		whenPlayerLose = RESTAR;
		threadLocal.set(getShuffleList());

		// set task strings
		setTitle("Table simulation");
		setDescription("Description fo the simulation");
		setMessage("Simulation initialization ...");

		this.bigBlind = parameters.getInteger("bigBlind");
		this.buyIn = parameters.getInteger("buyIn");
		this.taskMonitor = new TTaskMonitor(this);
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
		}
		holeCardsDealed = true;
		notifyPlayersUpdated(false);
		notifyMessage("%s deals the hole cards.", dealer);
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
			if (pausePlayer) {
				ThreadUtils.sleepSafely(100);
				continue;
			}

			rotateActor();
			PlayerAction action = null;
			if (actor.isAllIn()) {
				// Player is all-in, so must check.
				action = PlayerAction.CHECK;
				playersToAct--;
			} else {
				// // Otherwise allow client to act.
				Set<PlayerAction> allowedActions = getAllowedActions(actor);
				action = actor.getClient().act(minBet, bet, allowedActions);

				// this pause allow me to see what is going on inside a hand only when hero is
				// about to act TODO: (old implementation from table.s control buttons)
				if ("Hero".equals(actor.getName()))
					while (pauseHero) {
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
						playersToAct = 0;
					}
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
		dealerPosition = -1;
		actorPosition = -1;
		boolean endedByHero = false;
		numOfHand = 0;

		// canceled or simulate a finite num of hands
		// while (!isCancelled() && (simulationsHand = 0)(simulationsHand > 0 &&
		// numOfHand < simulationsHand)) {
		for (numOfHand = 1; (numOfHand < simulationsHand && !isCancelled() && !endedByHero)
				|| (simulationsHand == 0 && !isCancelled() && !endedByHero); numOfHand++) {
			// pause ?
			if (pauseTask) {
				ThreadUtils.sleepSafely(100);
				continue;
			}

			long time1 = System.currentTimeMillis();

			// Counts active players
			int actp = 0;
			for (Player player : players) {
				if (player.getCash() >= bigBlind) {
					actp++;
				}
			}

			if (RESTAR.equals(whenPlayerLose) && actp < MIN_PLAYERS) {
				String msg = "Hand: " + numOfHand
						+ ", The table has less players that allow. Restartting the hole table.";
				threadLocal.set(getShuffleList());
				for (Player player2 : players) {
					Client client = player2.getClient();
					// interactive environment client can be an instance of tableDialog
					Bot bot = null;
					if (client instanceof TableDialog)
						bot = (Bot) ((TableDialog) client).getProxyClient();
					else
						bot = (Bot) player2.getClient();

					Alesia.openDB();
					insertZeroElement();
					simulationParameters.add(bot.getBackrollSnapSchot());

					player2.resetHand();
					player2.setCash(buyIn);
				}
				notifyMessage(msg);
			}

			// when a single player loose
			for (Player player : players) {
				if (player.getCash() < bigBlind) {

					// when is Hero, end the simulation??
					// if (player.getName().equals("Hero") && GAME_OVER.equals(whenPlayerLose)) {
					// endedByHero = true;
					// break;
					// }

					// if (REFILL.equals(whenPlayerLose)) {
					// String msg = "Hand # " + numOfHand + ": " + player.getName()
					// + " lost the battle. Refilling cash " + buyIn;
					// notifyMessage(msg);
					// notifyMessage(REFILL);
					// player.resetHand();
					// player.setCash(buyIn);
					// }

					// if (RESTAR.equals(whenPlayerLose)) {
					// String msg = "Hand: " + numOfHand + ": " + player.getName()
					// + " lost the battle. Restartting the hole table.";
					// notifyMessage(msg);
					// for (Player player2 : players) {
					// player2.resetHand();
					// player2.setCash(buyIn);
					// }
					// }
				}
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
			} else {
				// end the simulation when there is no more active players. if the flow reach
				// this point, is probably because whenPlayerLose = DO_NOTHING
				break;
			}

			statistics.addValue((System.currentTimeMillis() - time1) / 1000d);
			String speed = TResources.twoDigitFormat.format(statistics.getMean());
			firePropertyChange(PROP_MESSAGE, null, "Played Hands: " + numOfHand + " Speed: " + speed + " Sec/Hand");
			setProgress(numOfHand, 0, simulationsHand);
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
		return null;
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
				notifyMessage("%s has %s.", playerToShow, UoAHandEvaluator.nameHand(hand));
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
//								System.out.format("[DEBUG] %s receives an odd chip from the pot.\n", winner);
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
			winnerText.append(String.format("%s wins %d", winner, potShare));
			notifyPlayersUpdated(true);
		}
		winnerText.append('.');
		notifyMessage(winnerText.toString());

		// Sanity check.
		if (totalWon != totalPot) {
			System.err.println("WARNING: Incorrect pot division!");
			// TODO: commented to allow the simulation to continue
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
	 * return the current round expressed in cards numbers. 2 = preflop, 5 = Flop, 6
	 * = Turn, 7 = River
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

	public TTaskMonitor getTaskMonitor() {
		return taskMonitor;
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
	 * A player's secret information is only sent its own client; other clients see
	 * only a player's public information.
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
	}

	/**
	 * Resets the game for a new hand.
	 */
	private void resetHand() {
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

		// Rotate the dealer button.
		dealerPosition = (dealerPosition + 1) % activePlayers.size();
		dealer = activePlayers.get(dealerPosition);

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

	/** determine how fine the simulation variables will be */
	private static int GRAIN = 10;

	/**
	 * return shuffle list of integers based on range [0, 100] / {@link #GRAIN}
	 * 
	 * @return the shuffle list
	 */
	public static List<Integer> getShuffleList() {
		List<Integer> integers = new ArrayList<>();
		int step = 100 / GRAIN;
		for (int i = 0; i < GRAIN; i++) {
			integers.add(step + i * step);
		}
		Collections.shuffle(integers);
		return integers;
	}

	/**
	 * return the next assignable value from {@link Table#getShuffleList()} if the
	 * list is depleted, this method build a new list and return the next new value
	 * 
	 * @return the new value
	 */
	public static int getShuffleVariable() {
		if (threadLocal.get().isEmpty())
			threadLocal.set(getShuffleList());
		int value = threadLocal.get().remove(0);
		return value;
	}

	private void insertZeroElement() {
		if (zeroElementCreated)
			return;
		// check if previous simulation the element was added
		LazyList<SimulationResult> results = simulationParameters.get(SimulationResult.class,
				"trooper = ? and hands = ?", "Hero", 0);
		if (results.isEmpty()) {
			SimulationResult sts = SimulationResult.create("trooper", "Hero");
			sts.set("hands", 0);
			sts.set("wins", 0);
			sts.set("ratio", 0);
			simulationParameters.add(sts);
		}
		this.zeroElementCreated = true;
	}

	/*
	 * Mark if the zero element was already created inside or on a previous
	 * simulation
	 */
	private boolean zeroElementCreated;

}
