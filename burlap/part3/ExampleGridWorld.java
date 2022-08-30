package burlap.part3;

import java.awt.*;
import java.awt.geom.*;

import burlap.mdp.auxiliary.*;
import burlap.mdp.core.*;
import burlap.mdp.core.action.*;
import burlap.mdp.core.state.*;
import burlap.mdp.singleagent.*;
import burlap.mdp.singleagent.environment.*;
import burlap.mdp.singleagent.model.*;
import burlap.shell.visual.*;
import burlap.visualizer.*;

public class ExampleGridWorld implements DomainGenerator {

	public class AgentPainter implements StatePainter {

		@Override
		public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {

			// agent will be filled in gray
			g2.setColor(Color.GRAY);

			// set up floats for the width and height of our domain
			float fWidth = ExampleGridWorld.map.length;
			float fHeight = ExampleGridWorld.map[0].length;

			// determine the width of a single cell on our canvas
			// such that the whole map can be painted
			float width = cWidth / fWidth;
			float height = cHeight / fHeight;

			int ax = (Integer) s.get(VAR_X);
			int ay = (Integer) s.get(VAR_Y);

			// left coordinate of cell on our canvas
			float rx = ax * width;

			// top coordinate of cell on our canvas
			// coordinate system adjustment because the java canvas
			// origin is in the top left instead of the bottom right
			float ry = cHeight - height - ay * height;

			// paint the rectangle
			g2.fill(new Ellipse2D.Float(rx, ry, width, height));

		}
	}
	public class WallPainter implements StatePainter {

		public void paint(Graphics2D g2, State s, float cWidth, float cHeight) {

			// walls will be filled in black
			g2.setColor(Color.BLACK);

			// set up floats for the width and height of our domain
			float fWidth = ExampleGridWorld.map.length;
			float fHeight = ExampleGridWorld.map[0].length;

			// determine the width of a single cell
			// on our canvas such that the whole map can be painted
			float width = cWidth / fWidth;
			float height = cHeight / fHeight;

			// pass through each cell of our map and if it's a wall, paint a black rectangle on our
			// cavas of dimension widthxheight
			for (int i = 0; i < ExampleGridWorld.map.length; i++) {
				for (int j = 0; j < ExampleGridWorld.map[0].length; j++) {

					// is there a wall here?
					if (ExampleGridWorld.map[i][j] == 1) {

						// left coordinate of cell on our canvas
						float rx = i * width;

						// top coordinate of cell on our canvas
						// coordinate system adjustment because the java canvas
						// origin is in the top left instead of the bottom right
						float ry = cHeight - height - j * height;

						// paint the rectangle
						g2.fill(new Rectangle2D.Float(rx, ry, width, height));

					}
				}
			}
		}
	}

	public static final String VAR_X = "x";
	public static final String VAR_Y = "y";

	protected static int[][] map = new int[][]{{0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0},
			{1, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1}, {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},};

	public static final String ACTION_NORTH = "north";
	public static final String ACTION_SOUTH = "south";
	public static final String ACTION_EAST = "east";
	public static final String ACTION_WEST = "west";

	protected int goalx = 10;
	
	protected int goaly = 10;

	public static void main(String [] args){

		ExampleGridWorld gen = new ExampleGridWorld();
		gen.setGoalLocation(10, 10);
		SADomain domain = (SADomain) gen.generateDomain();
		State initialState = new EXGridState(0, 0);
		SimulatedEnvironment env = new SimulatedEnvironment(domain, initialState);

		Visualizer v = gen.getVisualizer();
		VisualExplorer exp = new VisualExplorer(domain, env, v);

		exp.addKeyAction("w", ACTION_NORTH, "");
		exp.addKeyAction("s", ACTION_SOUTH, "");
		exp.addKeyAction("d", ACTION_EAST, "");
		exp.addKeyAction("a", ACTION_WEST, "");

		exp.initGUI();

	}

	@Override
	public Domain generateDomain() {
		SADomain domain = new SADomain();
		domain.addActionTypes(new UniversalActionType(ACTION_NORTH), new UniversalActionType(ACTION_SOUTH),
				new UniversalActionType(ACTION_EAST), new UniversalActionType(ACTION_WEST));

		GridWorldStateModel smodel = new GridWorldStateModel();
		RewardFunction rf = new ExampleRF(this.goalx, this.goaly);
		TerminalFunction tf = new ExampleTF(this.goalx, this.goaly);

		domain.setModel(new FactoredModel(smodel, rf, tf));

		return domain;
	}

	public StateRenderLayer getStateRenderLayer(){
		StateRenderLayer rl = new StateRenderLayer();
		rl.addStatePainter(new ExampleGridWorld.WallPainter());
		rl.addStatePainter(new ExampleGridWorld.AgentPainter());

		return rl;
	}

	public Visualizer getVisualizer(){
		return new Visualizer(this.getStateRenderLayer());
	}
	
	/**
	 * set the location for the rewardfunction
	 * 
	 * @param goalx - x coord
	 * @param goaly - y coord
	 */
	public void setGoalLocation(int goalx, int goaly) {
		this.goalx = goalx;
		this.goaly = goaly;
	}
}
