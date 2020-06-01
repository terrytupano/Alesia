package plugins.hero;

import java.util.*;

import org.apache.commons.math3.stat.descriptive.*;

import core.*;
import core.datasource.model.*;

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
	private DescriptiveStatistics previousBettingPattern;
	private SensorsArray array;
	private int playerId;
	private String prefix;
	private double prevValue;

	public GamePlayer(int playerId) {
		this.playerId = playerId;
		this.prefix = "villan" + playerId;
		this.name = prefix;
		this.prevValue = -1;
		this.array = Hero.sensorsArray;
		this.bettingPattern = new DescriptiveStatistics(100);
		this.previousBettingPattern = new DescriptiveStatistics(100);
	}

	public int getId() {
		return playerId;
	}
	public String getName() {
		return name;
	}
	public String getPreviousStats() {
		return getMean(previousBettingPattern) + " (" + previousBettingPattern.getN() + ")";

	}

	public String getStats() {
		return getMean() + " (" + bettingPattern.getN() + ")";
	}

	/**
	 * Read the sensor form the {@link SensorsArray} asociated whit this recorder
	 * 
	 */
	public void readSensors() {
		// record only if the player is active
		if (!array.isActive(playerId))
			return;

		List<ScreenSensor> list = new ArrayList<>();
		if (playerId == 0) {
			list.add(array.getSensor("hero.chips"));
		} else {
			list = array.getSensors("villan" + playerId);
		}
		array.readSensors(true, list);

		// amunitions
		double chips = 0.0;
		if (playerId == 0) {
			chips = array.getPokerSimulator().getHeroChips();
		} else {
			chips = array.getSensor("villan" + playerId + ".chips").getNumericOCR();
		}

		// chips = -1 because this seat is inactive or the player fold
		if (chips == -1)
			return;

		// at the beginnin of the record process, i just set the initial values. after that, i start the record process.
		if (prevValue == -1) {
			prevValue = chips;
			return;
		}

		// at this point, all is set to start the record process
		// name
		if (playerId == 0)
			name = "Hero";
		else
			name = array.getSensor(prefix + ".name").getOCR();

		name = name == null ? prefix : name;
		if (!(name.equals(prefix) || name.equals(oldName))) {
			oldName = name;
			Game gh = Game.findFirst("NAME = ? AND TABLEPARAMS = ? ORDER BY SESSION DESC", name,
					array.getPokerSimulator().getTableParameters());
			if (gh != null) {
				previousBettingPattern = (DescriptiveStatistics) TResources
						.getObjectFromByteArray(gh.getBytes("BEATTIN_PATTERN"));
			}
		}

		// negative for betting, positive for winnigs (don.t record 0 value because affect statistical values)
		if (chips - prevValue != 0)
			bettingPattern.addValue(chips - prevValue);
		prevValue = chips;
	}

	public void updateDB() {
		if (!name.equals(prefix)) {
			Game gh = Game.findOrInit("SESSION", Hero.getSesionID(), "tableparams",
					array.getPokerSimulator().getTableParameters(), "name", name);
			gh.set("ASSESMENT", getStats());
			gh.set("BEATTIN_PATTERN", TResources.getByteArrayFromObject(bettingPattern));
			gh.save();
		}
	}

	/**
	 * return the mean but expresed in BB
	 * 
	 * @return means in BB format
	 */
	public double getMean() {
		return getMean(bettingPattern);
	}

	/**
	 * return the mean but expresed in BB
	 * 
	 * @param stats - {@link DescriptiveStatistics}
	 * @return means in BB format
	 */
	private double getMean(DescriptiveStatistics stats) {
		double mean = stats.getMean();
		mean = mean / array.getPokerSimulator().getBigBlind();
		mean = ((int) (mean * 100)) / 100.0;
		return mean;
	}
}