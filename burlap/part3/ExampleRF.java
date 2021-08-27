package burlap.part3;

import burlap.mdp.core.action.*;
import burlap.mdp.core.state.*;
import burlap.mdp.singleagent.model.*;

public class ExampleRF implements RewardFunction {

	int goalX;
	int goalY;

	public ExampleRF(int goalX, int goalY){
		this.goalX = goalX;
		this.goalY = goalY;
	}

	@Override
	public double reward(State s, Action a, State sprime) {

		int ax = (Integer)s.get(ExampleGridWorld.VAR_X);
		int ay = (Integer)s.get(ExampleGridWorld.VAR_Y);

		//are they at goal location?
		if(ax == this.goalX && ay == this.goalY){
			return 100.;
		}

		return -1;
	}


}