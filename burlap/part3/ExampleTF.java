package burlap.part3;

import burlap.mdp.core.*;
import burlap.mdp.core.state.*;

public class ExampleTF implements TerminalFunction {

	int goalX;
	int goalY;

	public ExampleTF(int goalX, int goalY){
		this.goalX = goalX;
		this.goalY = goalY;
	}

	@Override
	public boolean isTerminal(State s) {

		//get location of agent in next state
		int ax = (Integer)s.get(ExampleGridWorld.VAR_X);
		int ay = (Integer)s.get(ExampleGridWorld.VAR_Y);

		//are they at goal location?
		if(ax == this.goalX && ay == this.goalY){
			return true;
		}

		return false;
	}

}