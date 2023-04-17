package hero;

import java.util.ArrayList;
import java.util.List;

/**
 * This class record the game secuence and store the result in the games db
 * file. instance of this class are dispached when is the trooper turns to
 * fight. all sensor information are stored inside of this class and this class
 * silent perform the record operation retrivin all necesari information.
 * <p>
 * the information retrived for this game recorded will be available at the next
 * game session
 * <p>
 * TODO: maybe exted the funcionality to supply imediate info. a villans in this
 * game sesion may alter his behabior and the curren hero status is not aware of
 * this.
 * 
 * @author terry
 *
 */
public class GameRecorder {

	private ArrayList<GamePlayer> players;
	private Trooper trooper;
	private int beacon;

	public GameRecorder(Trooper trooper, int vills) {
		this.trooper = trooper;
		// 0 index is the troper
		this.players = new ArrayList<>();
		for (int i = 0; i <= vills; i++) {
			players.add(new GamePlayer(i));
		}
	}

	public List<GamePlayer> getPlayers() {
		return players;
	}

	public void takeSample() {
		getGamePlayer(beacon).readSensors(trooper);
		beacon++;
		if (beacon == players.size()) {
			beacon = 0;
		}

	}

	/**
	 * Store the result in DB. This method is called afther the troper perform the
	 * action. at this moment, is enough time to update the database and perform an
	 * assesment over the villans
	 * 
	 */
	public void updateDB() {
		players.forEach(gr -> gr.updateDB());
	}

	public GamePlayer getGamePlayer(int index) {
		return players.get(index);
	}

	/**
	 * Take a snapshot of the game status. At this point all elements are available
	 * for be processed because this method is called one step before the tropper
	 * perform his action.
	 * 
	 */
	public void takeSnapShot() {
		players.stream().forEach(v -> v.readSensors(trooper));
	}
}
