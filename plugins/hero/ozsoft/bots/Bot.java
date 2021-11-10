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

import com.javaflair.pokerprophesier.api.card.*;

import core.*;
import core.datasource.model.*;
import plugins.hero.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.*;

/**
 * Base class for all Texas Hold'em poker bot implementations. this base implementation contain all required variables
 * to allow subclasses record the history of this particular implementation.
 * 
 */
public abstract class Bot implements Client {

	/** Number of hole cards. */
	protected static final int NO_OF_HOLE_CARDS = 2;
	protected static Hashtable<String, Object> parm1 = new Hashtable<>();
	protected PokerSimulator pokerSimulator;
	protected Trooper trooper;
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

	/** easy access to client parameters */
	protected int reconnBase, reconnBand, oppLowerBound;
	/** stored parameters for this bot */
	protected SimulatorClient client;

	/** only for stadistical analisis */
	private SimulatorStatistic statistic;
	private int prevCash; //
	private int wins; // easy access to wins field
	private int hands = 0; // # of hands

	@Override
	public void actorRotated(Player actor) {
		// TODO Auto-generated method stub
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		this.communityHand = hand;
		this.hand = new UoAHand(myHole.toString() + " " + hand.toString());
		this.pot = pot + bet;
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
			// this.preflopCardsModel = new PreflopCardsModel("original");
			// session = "" + System.currentTimeMillis();
		}

		if (message.contains("wins ")) {
			String[] tmp = message.split("[ ]");
			String name = tmp[0];
			if (playerName.equals(name)) {
				// playerWins = Integer.parseInt(tmp[2].substring(0, tmp[2].length() - 1));
			}
		}

		if (message.contains("is the dealer.")) {
			// playerWins = (player.getCash() - prevCash);

			// performObservation();
			matchCost = 0;
			// playerWins = 0;
			hands++;
		}

		if (message.contains("Flop.") || message.contains("Turn.") || message.contains("River.")) {
			street++;
		}

		if (message.contains("Restartting the hole table.")) {
			saveObservations();
			// session = "" + System.currentTimeMillis();
			hands = 0;
			street = 0;
			prevCash = buyIn;
			initStatisticsParms();
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
	 * invoqued after contruction. set the trooper and the asociated simulator for this boot
	 * 
	 * @param pokerSimulator - intance of {@link PokerSimulator}
	 * @param trooper - Instace of {@link Trooper}
	 */
	public void setPokerSimulator(PokerSimulator pokerSimulator, Trooper trooper) {
		this.pokerSimulator = pokerSimulator;
		this.trooper = trooper;
		initStatisticsParms();
	}

	/**
	 * init the statisticas parameters
	 * 
	 */
	private void initStatisticsParms() {
		// DON.T MOVE DB CONNECTION FROM THIS METHOD: THIS METHOD IS CALLED FROM MUTIPLE THREAD
		Alesia.getInstance().openDB("hero");

		// clear collision table values for this bot. this implementation leav the old values for all others players.
		// that mean the new simulation force all bot take anoter random values (good for uniformitiy)
		parm1.remove(playerName);

		client = SimulatorClient.findFirst("playerName = ?", playerName);
		this.wins = 0;
		// range of random variations for all know variables
		if (client.getInteger("reconnBase") == null || client.getInteger("reconnBase") == -1) {
			this.reconnBase = (int) (Math.random() * 20d);
			client.set("reconnBase", reconnBase);
		} else {
			this.reconnBase = client.getInteger("reconnBase");
		}

		if (client.getInteger("reconnBand") == null || client.getInteger("reconnBand") == -1) {
			this.reconnBand = (int) (Math.random() * 20d);
			client.set("reconnBand", reconnBand);
		} else {
			this.reconnBand = client.getInteger("reconnBand");
		}

		if (client.getInteger("oppLowerBound") == null || client.getInteger("oppLowerBound") == -1) {
			this.oppLowerBound = -1;
			boolean con = true;
			while (con) {
				this.oppLowerBound = (int) (Math.random() * 100d);
				// Starting at 50% and only modulus 5
				if (oppLowerBound % 5 == 0 && oppLowerBound >= 50 && !parm1.containsValue(oppLowerBound))
					con = false;
			}
//			System.out.println(playerName + " " + oppLowerBound);
			client.set("oppLowerBound", oppLowerBound);
			parm1.put(playerName, oppLowerBound);
		} else {
			this.oppLowerBound = client.getInteger("oppLowerBound");
		}

		// retrive last values from statistical table
		statistic = SimulatorStatistic.findFirst("reconnBase = ? AND reconnBand = ? AND oppLowerBound = ?", reconnBase,
				reconnBand, oppLowerBound);
		if (statistic == null) {
			statistic = SimulatorStatistic.create("reconnBase", reconnBase, "reconnBand", reconnBand, "oppLowerBound",
					oppLowerBound);
		} else {
			wins = statistic.getInteger("wins");
			hands = statistic.getInteger("hands");
		}
	} 
	
	/**
	 * save the the values of global variables {@link #wins} and {@link Hand}
	 */
	private void saveObservations() {
		wins = wins + player.getCash() - buyIn;
		Alesia.getInstance().openDB("hero");
		statistic.set("hands", hands);
		statistic.set("wins", wins);
		statistic.set("takeOpportunity", true);
		// int r = (int) ((double) wins / numOfMatch * 100);
		// statistic.set("ratio", r / 100d);
		double bb = wins / (bigBlind * 1.0);
		statistic.set("ratio", bb / (hands * 1.0));
		statistic.save();
	}
}
