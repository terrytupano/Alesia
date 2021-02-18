package plugins.hero;

import java.util.*;

import org.apache.commons.math3.stat.descriptive.*;

/**
 * encapsulate all player information. this class collect the necesary information to make a wild guess over the
 * villans.
 * <p>
 * A beginner villan must play around 1-2 hours dayly. a profesional poker player around 2-8 . this class only take the
 * last 100 hands (must be around 1 1/2 hours)
 * 
 * TODO. 100 is an estimated to avoid pass behaviors interfir whit the new villans moods and skills. chick this value. a
 * better aproach cound be keep track of long data and remove the old ones by date.
 * 
 * @author terry
 *
 */
public class GamePlayer {
	private String name;
	private String oldName = "";
	private DescriptiveStatistics bettingPattern;
	private int playerId;
	private double prevValue;
	private boolean isActive;

	public GamePlayer(int playerId) {
		this.playerId = playerId;
		this.prevValue = -1;
		this.bettingPattern = new DescriptiveStatistics(100);
	}

	public String getDesignation() {
		return playerId == 0 ? "Hero" : "Villan "+playerId;
	}
	public int getId() {
		return playerId;
	}

	public boolean isActive() {
		return isActive;
	}
	/**
	 * return the name of the villan in this position. can return <code>null</code> if no complete information is
	 * available at this moment
	 * 
	 * @return villan name or <code>null</code>
	 */
	public String getName() {
		return name;
	}
	// public String getPreviousStats() {
	// return getMean(previousBettingPattern) + " (" + previousBettingPattern.getN() + ")";
	//
	// }

	/**
	 * Return a visual representation of the internal statistic.
	 * 
	 * @return String with statistic
	 */
	public String getStats() {
		return "N=" + bettingPattern.getN() + " M=" + getMean() + " Sd="+ getStandardDeviation();
	}

	/**
	 * Read the sensor form the {@link SensorsArray} asociated whit this recorder
	 * 
	 */
	public void readSensors() {
		isActive = false;
		String sensors = (playerId == 0) ? "hero" : "villan" + playerId;
		List<ScreenSensor> list = Hero.sensorsArray.getSensors(sensors);
		Hero.sensorsArray.readSensors(true, list);

		if (!Hero.sensorsArray.isActive(playerId))
			return;

		// Active variable here to avoid blick in assesment. if in this moment can retrive all info, present the
		// assesment any way
		isActive = true;

		// amunitions
		double chips = 0.0;
		if (playerId == 0) {
			chips = Hero.sensorsArray.getSensor("hero.chips").getNumericOCR();
		} else {
			chips = Hero.sensorsArray.getSensor("villan" + playerId + ".chips").getNumericOCR();
		}

		// name
		if (playerId == 0)
			name = "Hero";
		else
			name = Hero.sensorsArray.getSensor("villan" + playerId + ".name").getOCR();

		// both values must be available to continue the process
		if (chips == -1 || name == null)
			return;

		// new player ??
		if (!oldName.equals(name)) {
			bettingPattern.clear();
			prevValue = -1;
		}

		// an the beginning of the record process, set the buyIN as basic anc compute the average winnigs/lose
		if (prevValue == -1) {
			double buyIn = Hero.sensorsArray.getPokerSimulator().getBuyIn();
			double bb = Hero.sensorsArray.getPokerSimulator().getBigBlind();

			// +/-10 BB mean the player is a new player
			double win = bb * 10;
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

		// at this point, all is set to start/continue the record process

		// name = name == null ? prefix : name;
		// if (!(name.equals(prefix) || name.equals(oldName))) {
		// if (!oldName.equals(name)) {
		// oldName = name;
		// bettingPattern.clear();
		// prevValue = -1;
		// Game gh = Game.findFirst("NAME = ? AND TABLEPARAMS = ?", name,
		// Hero.sensorsArray.getPokerSimulator().getTableParameters());
		// if (gh != null) {
		// bettingPattern = (DescriptiveStatistics) TResources
		// .getObjectFromByteArray(gh.getBytes("BEATTIN_PATTERN"));
		// }
		// }

		// negative for betting, positive for winnigs (don.t record 0 value because affect statistical values)
		if (chips - prevValue != 0)
			bettingPattern.addValue(chips - prevValue);
		prevValue = chips;
	}

	public void updateDB() {
		// if (!name.equals(prefix)) {
		// Game gh = Game.findOrInit("tableparams", Hero.sensorsArray.getPokerSimulator().getTableParameters(), "name",
		// name);
		// gh.set("ASSESMENT", getStats());
		// gh.set("BEATTIN_PATTERN", TResources.getByteArrayFromObject(bettingPattern));
		// gh.save();
		// }
	}

	/**
	 * return the mean but expresed in BB
	 * 
	 * @return means in BB format
	 */
	public double getMean() {
		return getMean(bettingPattern);
	}
	
	public double getStandardDeviation() {
		double var = bettingPattern.getStandardDeviation();
		var = var / Hero.sensorsArray.getPokerSimulator().getBigBlind();
		var = ((int) (var * 100)) / 100.0;
		return var;
	}

	/**
	 * return the mean but expresed in BB
	 * 
	 * @param stats - {@link DescriptiveStatistics}
	 * @return means in BB format
	 */
	private double getMean(DescriptiveStatistics stats) {
		double mean = stats.getMean();
		mean = mean / Hero.sensorsArray.getPokerSimulator().getBigBlind();
		mean = ((int) (mean * 100)) / 100.0;
		return mean;
	}
}