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

package plugins.hero.ozsoft.bots;

import java.util.*;

import core.*;
import core.datasource.model.*;
import plugins.hero.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.*;
import plugins.hero.utils.*;

/**
 * Base class for all Texas Hold'em poker bot implementations. this base implementation contain all required variables
 * to allow subclasses record the history of this particular implementation.
 * 
 */
public abstract class Bot2 implements Client {

	/** the observation method for the entire simulation. stored in Hero client table */
	protected static String observationMethod;
	/** Number of hole cards. */
	protected static final int NO_OF_HOLE_CARDS = 2;

	protected PokerSimulator pokerSimulator;
	protected Player player;
	protected List<Player> villans;
	protected int bigBlind;
	protected int dealer;
	protected int pot;
	protected int buyIn;
	protected String playerName;
	protected UoAHand myHole, communityHand, hand;

	/** poker street. preFlop=0, Flop=1 ... */
	protected int street = 0;

	/** keep track the current match cost. the cumulative cost of all actions */
	protected int matchCost = 0;

	/** the PreflopCardsModel ussed by this client */
	protected PreflopCardsModel preflopCardsModel;

	/** easy access to client parameters */
	protected double alpha;
	protected int tau;

	/** only for stadistical analisis */
	private SimulatorStatistic statistic;
	private int prevCash; //
	private int wins; // easy access to wins field
	private int numOfMatch = 0; // # of mathc
	private int value; // easy access to value field
	protected String session; // Session id for statistical record

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		this.communityHand = hand;
		this.hand = new UoAHand(myHole.toString() + " " + hand.toString());
		this.pot = pot + bet;
	}

	@Override
	public void handStarted(Player dealer) {
		// not implemented
	}

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		this.villans = new ArrayList(players);
		this.player = players.stream().filter(p -> playerName.equals(p.getName())).findFirst().get();
		villans.remove(player);
		this.bigBlind = bigBlind;
		this.buyIn = player.getCash();
		this.myHole = new UoAHand();
		this.prevCash = buyIn;
	}

	@Override
	public void messageReceived(String message) {
		/**
		 * invoqued directly after class constructor and set the player name. <b>Hero</b> is allawys Hero and the
		 * villans haven other names.
		 */
		if (message.startsWith("PlayerName=")) {
			this.playerName = message.split("[=]")[1];
			this.preflopCardsModel = new PreflopCardsModel("pokerStar");
			session = "" + System.currentTimeMillis();
		}

		if (message.contains("wins ")) {
			String[] tmp = message.split("[ ]");
			String name = tmp[0];
			if (playerName.equals(name)) {
				// playerWins = Integer.parseInt(tmp[2].substring(0, tmp[2].length() - 1));
			}
		}

		if (message.startsWith("New match,")) {
			// playerWins = (player.getCash() - prevCash);

			// performObservation();

			prevCash = player.getCash();
			matchCost = 0;
			// playerWins = 0;
			numOfMatch++;
		}

		if (message.contains("Flop.") || message.contains("Turn.") || message.contains("River.")) {
			street++;
		}

		if (message.equals(Table.RESTAR)) {
			saveObservations();
			session = "" + System.currentTimeMillis();
			numOfMatch = 0;
			street = 0;
			prevCash = buyIn;
			initObservationParameters();
		}

	}
	@Override
	public void playerActed(Player player) {
		if (playerName.equals(player.getName())) {
			matchCost = prevCash - player.getCash();
			// System.out.println(playerName + " " + matchCost);
		}
	}

	@Override
	public void playerUpdated(Player player) {
		if (playerName.equals(player.getName()) && player.getHand().size() == NO_OF_HOLE_CARDS) {
			this.myHole = player.getHand();
		}
	}

	/**
	 * set the observation method. the method name is stored in Hero client table record and is setted only one and
	 * accesible for all instance of this class.
	 * 
	 * @param observationMethod - the observation method
	 */
	public void setObservationMethod(String observationMethod) {
		if ("Hero".equals(playerName))
			Bot2.observationMethod = observationMethod == null ? "*null" : observationMethod;
		SimulatorClient client = SimulatorClient.findFirst("playerName = ?", playerName);
		this.tau = client.getInteger("tau") == null ? 0 : client.getInteger("tau");
		this.alpha = client.getDouble("alpha") == null ? -1 : client.getInteger("alpha");
		initObservationParameters();
	}

	/**
	 * invoqued after contruction. set the simulator for this boot
	 * 
	 * @param pokerSimulator - Instace of poker simulator
	 */
	public void setPokerSimulator(PokerSimulator pokerSimulator) {
		this.pokerSimulator = pokerSimulator;
	}

	private void initObservationParameters() {
		// DON.T MOVE DB CONNECTION FROM THIS METHOD: THIS METHOD IS CALLED FROM MUTIPLE THREAD
		if (playerName.equals("Hero"))
			Alesia.getInstance().openDB("hero");

		this.wins = 0;

		// initial estimation of parameter occur in setObservationMethdo() random values for villas and secuencial for
		// hero
		if ("tauVariation".equals(observationMethod)) {
			this.tau = "Hero".equals(playerName) ? (tau == 100 ? 5 : tau + 5) : (int) (Math.random() * 100d);
			this.value = tau;
		}
		if ("alphaVariation".equals(observationMethod)) {
			this.alpha = "Hero".equals(playerName) ? (alpha >= 1 ? -1.0 : alpha + 0.1) : (Math.random() * 2d) - 1d;
			this.value = (int) (alpha * 100);
		}

		// retrive last values from statistical table
		if ("Hero".equals(playerName)) {
			statistic = SimulatorStatistic.findFirst("measureName = ? AND value = ?", observationMethod, value);
			if (statistic == null) {
				statistic = SimulatorStatistic.create("session", session, "measureName", observationMethod);
			} else {
				wins = statistic.getInteger("wins");
				numOfMatch = statistic.getInteger("hands");
			}
		}
		// for all players
		preflopCardsModel.setPercentage(tau);
	}

	private void saveObservations() {
		if (!"Hero".equals(playerName))
			return;

		wins = wins + player.getCash() - buyIn;
		Alesia.getInstance().openDB("hero");
		statistic.set("hands", numOfMatch);
		statistic.set("wins", wins);
		statistic.set("value", value);
		// int r = (int) ((double) wins / numOfMatch * 100);
		// statistic.set("ratio", r / 100d);
		double bb = wins / (bigBlind * 1.0);
		statistic.set("ratio", bb / (numOfMatch * 1.0));
		statistic.save();
	}
}
