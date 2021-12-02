package burlap;

import burlap.domain.singleagent.gridworld.*;
import burlap.mdp.auxiliary.stateconditiontest.*;
import burlap.mdp.core.*;
import burlap.mdp.core.state.*;
import burlap.mdp.singleagent.environment.*;
import burlap.mdp.singleagent.model.*;
import burlap.mdp.singleagent.oo.*;
import burlap.statehashing.*;

public class BasicBehavior {

	GridWorldDomain gwdg;
	OOSADomain domain;
	RewardFunction rf;
	TerminalFunction tf;
	StateConditionTest goalConditions;
	State initialState;
	HashableStateFactory hashingFactory;
	SimulatedEnvironment env;

	public BasicBehavior() {
		this.gwdg = new GridWorldDomain(11, 11);
		gwdg.setMapToFourRooms();
		gwdg.setTf(new GridWorldTerminalFunction(10, 10));
	}
}
