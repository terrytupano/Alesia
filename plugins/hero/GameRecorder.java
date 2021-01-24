package plugins.hero;

import java.util.*;

import core.*;

/**
 * This class record the game secuence and store the result in the games db file. instance of this class are dispached
 * when is the trooper turns to fight. all sensor information are stored inside of this class and this class silent
 * perform the record operation retrivin all necesari information.
 * <p>
 * the information retrived for this game recorded will be available at the next game session
 * <p>
 * TODO: maybe exted the funcionality to supply imediate info. a villans in this game sesion may alter his behabior and
 * the curren hero status is not aware of this.
 * 
 * @author terry
 *
 */
public class GameRecorder {

	private Vector<GamePlayer> players;

	public GameRecorder(int vills) {
		// 0 index is the troper
		this.players = new Vector<>(vills);
		for (int i = 0; i <= vills; i++) {
			players.add(new GamePlayer(i));
		}
	}
	public Vector<GamePlayer> getPlayers() {
		return players;
	}
	/**
	 * 
	 * @return
	 */
	public ArrayList<String> getAssesment() {
		tempList.clear();
		tempList2.clear();
		for (int i = 0; i < players.size(); i++) {
			GamePlayer gp = players.elementAt(i);
			// active player only
			if (gp.isActive())
				tempList.add(new TEntry<>(gp, gp.getMean()));
		}
		tempList.sort(Comparator.reverseOrder());
		if (tempList.size() > 0) {
			for (int i = 0; i < Math.min(4, tempList.size()); i++) {
				GamePlayer gp = tempList.get(i).getKey();
				tempList2.add(gp.getId() + " " + gp.getName() + " " + gp.getStats());
			}
		}
		return tempList2;
	}

	/**
	 * Store the result in DB. This method is called afther the troper perform the action. at this moment, is enough
	 * time to update the database and perform an assesment over the villans
	 * 
	 */
	public void updateDB() {
		players.forEach(gr -> gr.updateDB());
	}

	private ArrayList<TEntry<GamePlayer, Double>> tempList = new ArrayList<>();
	private ArrayList<String> tempList2 = new ArrayList<>();

	public GamePlayer getGamePlayer(int index) {
		return players.elementAt(index);
	}
	/**
	 * Take a snapshot of the game status. At this point all elements are available for be processed because this method
	 * is called one step before the tropper perform his action.
	 * 
	 */
	public void takeSnapShot() {
		players.stream().forEach(v -> v.readSensors());
	}
}
