package burlap.part3;

import java.util.*;

import burlap.mdp.core.*;
import burlap.mdp.core.action.*;
import burlap.mdp.core.state.*;
import burlap.mdp.singleagent.model.statemodel.*;

public class GridWorldStateModel implements FullStateModel {

	private double[][] transitionMatrix;

	public GridWorldStateModel() {
		this.transitionMatrix = new double[4][4];
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 4; j++) {
				double p = i == j ? .8 : .2 / 3;
				transitionMatrix[i][j] = p;
			}
		}
	}

	/**
	 * return the <code>int</code> value (direction) simple translating the action name
	 * 
	 * @param action - the aciton
	 * @return int for navigation purpose
	 */
	public int actionDir(Action action) {
		int dir = -1;
		dir = ExampleGridWorld.ACTION_NORTH.equals(action.actionName()) ? 0 : dir;
		dir = ExampleGridWorld.ACTION_SOUTH.equals(action.actionName()) ? 1 : dir;
		dir = ExampleGridWorld.ACTION_EAST.equals(action.actionName()) ? 2 : dir;
		dir = ExampleGridWorld.ACTION_WEST.equals(action.actionName()) ? 3 : dir;
		return dir;
	}

	@Override
	public State sample(State s, Action a) {
		// return a new state form the incoming state appöying the action a (jprob)
		s = s.copy();
		EXGridState gs = (EXGridState) s;
		int curX = gs.x;
		int curY = gs.y;
		int aDir = actionDir(a);

		// sample a random direction (ist this method correct O.o !???!?!?!?)
		double r = Math.random();
		double sumProb = 0;
		int dir = 0;
		for (int i = 0; i < 4; i++) {
			sumProb += transitionMatrix[aDir][i];
			// found a direction
			if (r < sumProb) {
				dir = i;
				break;
			}
		}
		// get and set the new position
		int[] newPos = moveResult(curX, curY, dir);
		gs.x = newPos[0];
		gs.x = newPos[1];
		return gs;
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {

		// get agent current position
		EXGridState gs = (EXGridState) s;

		int curX = gs.x;
		int curY = gs.y;

		int adir = actionDir(a);

		List<StateTransitionProb> tps = new ArrayList<StateTransitionProb>(4);
		StateTransitionProb noChange = null;
		for (int i = 0; i < 4; i++) {

			int[] newPos = this.moveResult(curX, curY, i);
			if (newPos[0] != curX || newPos[1] != curY) {
				// new possible outcome
				EXGridState ns = (EXGridState) gs.copy();
				ns.x = newPos[0];
				ns.y = newPos[1];

				// create transition probability object and add to our list of outcomes
				tps.add(new StateTransitionProb(ns, this.transitionMatrix[adir][i]));
			} else {
				// this direction didn't lead anywhere new
				// if there are existing possible directions
				// that wouldn't lead anywhere, aggregate with them
				if (noChange != null) {
					noChange.p += this.transitionMatrix[adir][i];
				} else {
					// otherwise create this new state and transition
					noChange = new StateTransitionProb(s.copy(), this.transitionMatrix[adir][i]);
					tps.add(noChange);
				}
			}

		}

		return tps;
	}

	/**
	 * return the outcome of a single movement. This method will return a 2 element int array, where the first component
	 * is the new x position of the agent and the second component is the new y position.
	 * 
	 * @param curX - current x position
	 * @param curY - current y position
	 * @param direction - direction of the movement.
	 * @return the new x y coordinates or the same when the future position is a wall or outside the world
	 * 
	 * @see #actionDir(Action)
	 */
	protected int[] moveResult(int curX, int curY, int direction) {
		int xdelta = 0;
		int ydelta = 0;
		if (direction == 0) {
			ydelta = 1;
		} else if (direction == 1) {
			ydelta = -1;
		} else if (direction == 2) {
			xdelta = 1;
		} else {
			xdelta = -1;
		}

		int nx = curX + xdelta;
		int ny = curY + ydelta;

		int width = ExampleGridWorld.map.length;
		int height = ExampleGridWorld.map[0].length;

		// make sure new position is valid (not a wall or off bounds)
		if (nx < 0 || nx >= width || ny < 0 || ny >= height || ExampleGridWorld.map[nx][ny] == 1) {
			nx = curX;
			ny = curY;
		}

		return new int[]{nx, ny};
	}

}
