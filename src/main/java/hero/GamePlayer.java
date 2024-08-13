package hero;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import hero.ozsoft.*;

/**
 * encapsulate all player information. this class collect the necesary
 * information to make a wild guess over the villans.
 * <p>
 * A beginner villan must play around 1-2 hours dayly. a profesional poker
 * player around 2-8 . this class only take the last 100 hands (must be around 1
 * 1/2 hours)
 * 
 * TODO. 100 is an estimated to avoid pass behaviors interfir whit the new
 * villans moods and skills. chick this value. a better aproach cound be keep
 * track of long data and remove the old ones by date.
 * 
 * @author terry
 *
 */
public class GamePlayer {
	private String name = "";
	private String oldName = "";
	private DescriptiveStatistics bettingPattern;
	private int playerId;
	private double prevValue;
	private boolean isActive, flopMeasured;
	private int tauCounter, totalTauCounter;
	private double chips, bigBlind, buyIn;

	public GamePlayer(int playerId) {
		this.playerId = playerId;
		this.prevValue = -1;
		this.bettingPattern = new DescriptiveStatistics(100);
		this.chips = 0.0;
		// initial stimation for tau parameter: 50%
		this.tauCounter = 8;
		this.totalTauCounter = 16;
	}

	public String getDesignation() {
		return playerId == 0 ? "Hero" : "Villan " + playerId;
	}

	/**
	 * return the player recorder id or position. 0=Hero, 1=Villan 1 ...
	 * 
	 * @return {@link GamePlayer} id
	 */
	public int getId() {
		return playerId;
	}

	/**
	 * return <code>true</code> if this player is currently active.
	 * <p>
	 * this status is update during {@link #readSensors()} process
	 * 
	 * @return active player or not
	 */
	public boolean isActive() {
		return isActive;
	}

	/**
	 * return the name of the villan in this position. can return <code>null</code>
	 * if no complete information is available at this moment
	 * 
	 * @return villan name or <code>null</code>
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return a visual representation of the internal statistic.
	 * 
	 * @return String with statistic
	 */
	public String getStats() {
		return "N=" + bettingPattern.getN() + " M=" + getMean() + " Sd=" + getStandardDeviation();
	}

	/**
	 * Read the sensor form the {@link SensorsArray} asociated whit this recorder
	 * 
	 */
	public void readSensors(Trooper trooper) {
		isActive = false;
		String ssPrefix = (playerId == 0) ? "hero" : "villan" + playerId;

		// in simulation Environment, retrive data direct from Table instance inf Hero
		if (trooper.getSimulationTable() != null) {
			Player p = trooper.getSimulationTable().getPlayers().get(playerId);
			bigBlind = trooper.getSimulationTable().bigBlind;
			buyIn = trooper.getSimulationTable().buyIn;
			chips = p.getCash();
			name = p.getName();

			// active player or not
			isActive = p.hasCards();

			// ensure flop count only once
			if (trooper.getSimulationTable().getCurrentRound() != PokerSimulator.FLOP_CARDS_DEALT)
				flopMeasured = false;

			// only count flop
			if (!flopMeasured && trooper.getSimulationTable().getCurrentRound() == PokerSimulator.FLOP_CARDS_DEALT) {
				int cnt = p.hasCards() ? 1 : 0;
				totalTauCounter += 1;
				tauCounter += cnt;
				flopMeasured = true;
			}
			performMeasure();
			return;
		}

		bigBlind = trooper.getPokerSimulator().bigBlind;
		buyIn = trooper.getPokerSimulator().buyIn;

		// first step: read only card1 and card2 sensor.
		List<ScreenSensor> list = trooper.getSensorsArray().getSensors(ssPrefix + ".card");
		trooper.getSensorsArray().readSensors(false, list);

		// only count on preflop and flop
		if (trooper.getPokerSimulator().street == PokerSimulator.FLOP_CARDS_DEALT) {
			tauCounter += trooper.getSensorsArray().isActive(playerId) ? 1 : -1;
			tauCounter = tauCounter <= 10 ? 10 : tauCounter;
			tauCounter = tauCounter > 100 ? 100 : tauCounter;
		}

		// in real live battle, the player muss be active to continue
		if (!trooper.getSensorsArray().isActive(playerId))
			return;

		isActive = true;
		list = trooper.getSensorsArray().getSensors(ssPrefix);
		trooper.getSensorsArray().readSensors(true, list);

		// amunitions
		String sName = (playerId == 0) ? "hero.chips" : "villan" + playerId + ".chips";
		chips = trooper.getSensorsArray().getSensor(sName).getNumericOCR();

		// name
		if (playerId == 0)
			name = "Hero";
		else
			name = trooper.getSensorsArray().getSensor("villan" + playerId + ".name").getOCR();

		// both values must be available to continue the process
		if (chips == -1 || name == null)
			return;

		performMeasure();
	}

	private void performMeasure() {
		// new player ??
		if (!oldName.equals(name)) {
			int diff = (StringUtils.difference(oldName, name).length() / Math.max(oldName.length(), name.length()))
					* 100;
			// string difference must be more than 60%
			if (diff > 60) {
				bettingPattern.clear();
				prevValue = -1;
			}
		}

		// an the beginning of the record process, set the buyIN as basic anc compute
		// the average winnigs/lose
		if (prevValue == -1) {
			// +/-10 BB mean the player is a new player
			double win = bigBlind * 10;
			if ((chips < buyIn - win) || (chips > buyIn + win)) {
				double diff = chips - buyIn;
				bettingPattern.addValue(diff);
				// int wins = bettingPattern.getWindowSize() / 2;
				// double num = diff / ((double) wins);
				// for (int i = 0; i < wins; i++)
				// bettingPattern.addValue(num);
			}
			prevValue = chips;
			oldName = name;
			return;
		}

		// negative for betting, positive for winnigs (don.t record 0 value because
		// affect statistical values)
		if (chips - prevValue != 0)
			bettingPattern.addValue(chips - prevValue);
		prevValue = chips;
	}

	public void updateDB() {
		// if (!name.equals(prefix)) {
		// Game gh = Game.findOrInit("tableparams",
		// Hero.pokerSimulator.getTableParameters(), "name",
		// name);
		// gh.set("ASSESMENT", getStats());
		// gh.set("BEATTIN_PATTERN", TResources.getByteArrayFromObject(bettingPattern));
		// gh.save();
		// }
	}

	/**
	 * return the las amount of chips captured
	 * 
	 * @return chips
	 */
	public double getChips() {
		return chips;
	}

	/**
	 * return the mean but expresed in BB
	 * 
	 * @return means in BB format
	 */
	public double getMean() {
		double mean = bettingPattern.getMean();
		// mean = mean / bigBlind;
		// mean = ((int) (mean * 100)) / 100.0;
		mean = ((int) (mean * 100)) / 100.0;
		return mean;
	}

	/**
	 * return the measured <code>tau </code> value
	 * 
	 * @return tau [0,100] range (%)
	 */
	public int getTau() {
		double val = tauCounter / (double) totalTauCounter;
		return (int) (val * 100);
//		return tauCounter;
	}

	public long getN() {
		return bettingPattern.getN();
	}

	public double getVariance() {
		double var = bettingPattern.getVariance();
		// var = var / bigBlind;
		// var = ((int) (var * 100)) / 100.0;
		var = ((int) (var * 100)) / 100.0;
		return var;
	}

	public double getStandardDeviation() {
		double var = bettingPattern.getStandardDeviation();
		// var = var / bigBlind;
		var = ((int) (var * 100)) / 100.0;
		return var;
	}
}