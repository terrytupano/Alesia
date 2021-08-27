package burlap;

import burlap.domain.singleagent.gridworld.*;
import burlap.domain.singleagent.gridworld.state.*;
import burlap.mdp.core.state.*;
import burlap.mdp.singleagent.*;
import burlap.shell.visual.*;
import burlap.visualizer.*;

public class HelloGridWorld {

	public static void main(String[] args) {

		GridWorldDomain gw = new GridWorldDomain(11, 11);
		gw.setMapToFourRooms();
		gw.setProbSucceedTransitionDynamics(0.8);
		SADomain domain = gw.generateDomain();
		
		State s = new GridWorldState(new GridAgent(), new GridLocation(10, 10, "loc1"));
		
		Visualizer v = GridWorldVisualizer.getVisualizer(gw.getMap());
		VisualExplorer exp = new VisualExplorer(domain, v, s);
		exp.addKeyAction("w", GridWorldDomain.ACTION_NORTH, "");
		exp.addKeyAction("s", GridWorldDomain.ACTION_SOUTH, "");
		exp.addKeyAction("a", GridWorldDomain.ACTION_WEST, "");
		exp.addKeyAction("d", GridWorldDomain.ACTION_EAST, "");

		exp.initGUI();

	}

}