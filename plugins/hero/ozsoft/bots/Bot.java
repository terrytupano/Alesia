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

import java.text.*;
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
 * @author Oscar Stigter
 */
public abstract class Bot implements Client {

	/** Number of hole cards. */
	protected static final int NO_OF_HOLE_CARDS = 2;
	private static DateFormat dateFormat = DateFormat.getDateTimeInstance();
	protected PokerSimulator pokerSimulator;
	protected Player player;
	protected List<Player> villans;
	protected int bigBlind;
	protected int dealer;
	protected int pot;
	protected int buyIn;
	protected String playerName;
	protected String observationMethod;

	protected UoAHand myHole, communityHand, hand;

	/** cash that the player had when the hand start */
	protected int prevCash;

	private int wins = 0;

	/** track the number of simulated hands */
	protected int numOfMatch = 0;

	/** Sesiton id for statistical record */
	protected String session;

	/** keep track the current match cost. the cumulative cost of all actions */
	protected int matchCost = 0;

	/** the PreflopCardsModel ussed by this client */
	protected PreflopCardsModel preflopCardsModel;
	protected SimulatorStatistic statistic;
	protected int playerWins;
	protected int tau;
	protected double alpha;

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		this.communityHand = hand;
		this.hand = new UoAHand(myHole.toString() + " " + hand.toString());
		this.pot = pot + bet;
	}

	@Override
	public void handStarted(Player dealer) {

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
			// this.session = dateFormat.format(new Date());
			session = "" + System.currentTimeMillis();
			if (playerName.equals("Hero"))
				Alesia.getInstance().openDB("hero");
			initParameterVariation();
		}

		if (message.contains("wins ")) {
			String[] tmp = message.split("[ ]");
			String name = tmp[0];
			if (playerName.equals(name))
				playerWins = Integer.parseInt(tmp[2].substring(0, tmp[2].length() - 1));
		}

		if (message.startsWith("New match,")) {
			performObservation();
			prevCash = player.getCash();
			matchCost = 0;
			playerWins = 0;
			numOfMatch++;
		}

		if (message.startsWith(Table.RESTAR)) {
			// session = dateFormat.format(new Date());
			session = "" + System.currentTimeMillis();
			numOfMatch = 0;
			initParameterVariation();
		}

	}
	@Override
	public void playerActed(Player player) {
		if (playerName.equals(player.getName())) {
			matchCost = prevCash - player.getCash();
			// System.out.println(playerName + " " + matchCost);
		}
		// Not implemented.
	}

	@Override
	public void playerUpdated(Player player) {
		if (playerName.equals(player.getName()) && player.getHand().size() == NO_OF_HOLE_CARDS) {
			this.myHole = player.getHand();
		}
	}

	/**
	 * set the observation method for this Bot.
	 * 
	 * @param observationMethod - the observation method
	 */
	public void setObservationMethod(String observationMethod) {
		this.observationMethod = observationMethod;
	}

	public void setPokerSimulator(PokerSimulator pokerSimulator) {
		this.pokerSimulator = pokerSimulator;
	}

	private void initParameterVariation() {
		// Random values for all players
		this.tau = "Hero".equals(playerName) ? tau : (int) (Math.random() * 100d);

		this.wins = 0;
		if ("Hero".equals(playerName)) {
			tau = tau == 100 ? 5 : tau + 5;
			statistic = SimulatorStatistic.findFirst("value = ?", tau);
			if (statistic == null) {
				statistic = SimulatorStatistic.create("session", session, "measureName", "tau Estimation");
			} else {
				tau = statistic.getInteger("value") == null ? 0 : statistic.getInteger("value");
				wins = statistic.getInteger("wins");
				numOfMatch = statistic.getInteger("hands");
			}
		}

		// for all players
		this.preflopCardsModel = new PreflopCardsModel("original");
		preflopCardsModel.setPercentage(tau);
		// FIX: alpha range: [-1,1] temporal alpha = 0 (Hero do exactly what ammunition control say)
		alpha = -0.35;

	}

	private void initPreFlopConvergency() {
		// fail save: preFlopConvergency allow only for hero (temporal maybe)
		if ("preFlopConvergency".equals(observationMethod) && "Hero".equals(playerName)) {
			this.preflopCardsModel = new PreflopCardsModel("preFlopConvergency");
		}
	}

	private void performObservation() {
		// TODO: all observacion method implement only for hero
		if (!"Hero".equals(playerName))
			return;

		if ("parameterVariation".equals(observationMethod)) {
			wins = wins + playerWins;
			if (numOfMatch % 100 == 0 && numOfMatch > 0) {
				Alesia.getInstance().openDB("hero");
				statistic.set("hands", numOfMatch);
				statistic.set("wins", wins);
				statistic.set("value", tau);
				int r = (int) ((double) wins / numOfMatch * 100);
				statistic.set("ratio", r / 100d);
				statistic.save();
			}
		}

		if ("preFlopConvergency".equals(observationMethod) && myHole.size() != 0) {
			// TODO:
			// preflopCardsModel.updateCoordenates(myHole.getCard(1), myHole.getCard(2), delta);
		}
	}
}
