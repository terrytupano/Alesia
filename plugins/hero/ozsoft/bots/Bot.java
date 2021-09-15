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

import org.javalite.activejdbc.*;

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

	protected int match = 0;
	protected int wins = 0;

	/** track the number of simulated hands */
	protected int numOfMatch = 0;

	/** Sesiton id for statistical record */
	protected String session;

	/** keep track the current match cost. the cumulative cost of all actions */
	protected int matchCost = 0;

	/** the PreflopCardsModel ussed by this client */
	protected PreflopCardsModel preflopCardsModel;

	@Override
	public void handStarted(Player dealer) {
		matchCost = prevCash - player.getCash();
	}

	private void performObservation() {
		// TODO: all observacion method implement only for hero
		if (!"Hero".equals(playerName))
			return;

		if ("parameterVariation".equals(observationMethod) && myHole.size() != 0) {
			wins = wins + playerWins;
			if (numOfMatch % 100 == 0) {
				Alesia.getInstance().openDB("hero");
				SimulatorStatistic statistic = SimulatorStatistic.findOrCreateIt("session", session, "measureName",
						"tau Estimation");
				statistic.set("hands", numOfMatch);
				statistic.set("wins", wins);
				// statistic.set("tau", tau);
				statistic.save();
			}
		}

		if ("preFlopConvergency".equals(observationMethod) && myHole.size() != 0) {
			// TODO:
			// preflopCardsModel.updateCoordenates(myHole.getCard(1), myHole.getCard(2), delta);
		}

	}
	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		this.communityHand = hand;
		this.hand = new UoAHand(myHole.toString() + " " + hand.toString());
		this.pot = pot + bet;
	}
	private static DateFormat dateFormat = DateFormat.getDateTimeInstance();

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		this.villans = new ArrayList(players);
		this.player = players.stream().filter(p -> playerName.equals(p.getName())).findFirst().get();
		villans.remove(player);
		this.bigBlind = bigBlind;
		this.buyIn = player.getCash();
		this.session = dateFormat.format(new Date());
		this.myHole = new UoAHand();
		this.prevCash = buyIn;
	}

	protected int playerWins;

	@Override
	public void messageReceived(String message) {
		/**
		 * invoqued directly after class constructor and set the player name. <b>Hero</b> is allawys Hero and the
		 * villans haven other names.
		 */
		if (message.startsWith("PlayerName=")) {
			this.playerName = message.split("[=]")[1];
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
			numOfMatch++;
		}
	}

	protected int tau;
	protected int alpha;

	private void initPreFlopConvergency() {
		Alesia.getInstance().openDB("hero");
		// fail save: preFlopConvergency allow only for hero (temporal maybe)
		if ("preFlopConvergency".equals(observationMethod) && "Hero".equals(playerName)) {
			this.preflopCardsModel = new PreflopCardsModel("preFlopConvergency");
		}
	}

	private void initParameterVariation() {
		// Random values for villans
		// tightness The bot's tightness (0 = tight, 100 = loose).
		this.tau = (int) (Math.random() * 100d);
		// aggression The bot's aggressiveness in betting (0.0 = careful, 2.0 = aggressive).
		// this.alpha = (int) (Math.random() * 100d);

		if ("Hero".equals(playerName)) {
			LazyList<SimulatorStatistic> list = SimulatorStatistic.where("ORDER BY session DESC").limit(1);
			tau = 5;
			if (list.size() > 0) {
				SimulatorStatistic statistic = list.get(0);
				tau = statistic.getInteger("tau") == null ? 0 : statistic.getInteger("tau");
				tau = tau == 100 ? 5 : tau + 5;
			}
		}

		// for all players
		this.preflopCardsModel = new PreflopCardsModel("original");
		preflopCardsModel.setPercentage(tau);

		// FIX: temporal alpha = 25 ( 1/4 times less that equations say)
		alpha = 25;

	}

	@Override
	public void playerActed(Player player) {
		System.out.println("Bot.playerActed()");
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
}
