package burlap.part4;

import java.util.*;

import javax.swing.*;

import burlap.behavior.policy.*;
import burlap.behavior.singleagent.*;
import burlap.behavior.singleagent.auxiliary.*;
import burlap.behavior.singleagent.auxiliary.performance.*;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.*;
import burlap.behavior.singleagent.learning.*;
import burlap.behavior.singleagent.learning.tdmethods.*;
import burlap.behavior.singleagent.planning.*;
import burlap.behavior.singleagent.planning.deterministic.*;
import burlap.behavior.singleagent.planning.deterministic.informed.*;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.*;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.*;
import burlap.behavior.singleagent.planning.deterministic.uninformed.dfs.*;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.*;
import burlap.behavior.valuefunction.*;
import burlap.domain.singleagent.gridworld.*;
import burlap.domain.singleagent.gridworld.state.*;
import burlap.mdp.auxiliary.stateconditiontest.*;
import burlap.mdp.core.*;
import burlap.mdp.core.state.*;
import burlap.mdp.singleagent.common.*;
import burlap.mdp.singleagent.environment.*;
import burlap.mdp.singleagent.model.*;
import burlap.mdp.singleagent.oo.*;
import burlap.statehashing.*;
import burlap.statehashing.simple.*;
import burlap.visualizer.*;

public class BasicBehavior {

	private static final String outputPath = "burlap/part4/";
	GridWorldDomain gwdg;
	OOSADomain domain;
	RewardFunction rf;
	TerminalFunction tf;
	StateConditionTest goalCondition;
	State initialState;
	HashableStateFactory hashingFactory;

	SimulatedEnvironment env;

	public BasicBehavior() {
		gwdg = new GridWorldDomain(11, 11);
		gwdg.setMapToFourRooms();
		tf = new GridWorldTerminalFunction(10, 10);
		gwdg.setTf(tf);
		goalCondition = new TFGoalCondition(tf);
		domain = gwdg.generateDomain();

		initialState = new GridWorldState(new GridAgent(0, 0), new GridLocation(10, 10, "loc0"));
		hashingFactory = new SimpleHashableStateFactory();

		env = new SimulatedEnvironment(domain, initialState);

		// interactive vizualizer
		// VisualActionObserver observer = new VisualActionObserver(domain,
		// GridWorldVisualizer.getVisualizer(gwdg.getMap()));
		// observer.initGUI();
		// env.addObservers(observer);
	}

	/**
	 * Value Function and Policy Visualization
	 * 
	 * @param valueFunction
	 * @param p
	 */
	public void simpleValueFunctionVis(ValueFunction valueFunction, Policy p) {
		List<State> allStates = StateReachability.getReachableStates(initialState, domain, hashingFactory);
		ValueFunctionVisualizerGUI gui = GridWorldDomain.getGridWorldValueFunctionVisualization(allStates, 11, 11,
				valueFunction, p);
		gui.initGUI();
	}

	public static void main(String[] args) {
		BasicBehavior example = new BasicBehavior();

		// run examples
		// example.BFSExample();
		// example.DFSExample();
		// example.AStarExample();
		// example.valueIterationExample();
		// example.QLearningExample();
		// example.SarsaLearningExample();
		example.experimentAndPlotter();

		// run the visualizer
		// example.visualize(outputPath);
	}

	/**
	 * Planin algorithm: A*
	 * 
	 */
	public void AStarExample() {

		// Manhatten distance to 10, 10. We return the negative value, because BURLAP is based on rewards rather than
		// costs. However, negative rewards are equivalent to costs
		Heuristic mdistHeuristic = ((State s) -> {
			GridAgent a = ((GridWorldState) s).agent;
			double mdist = Math.abs(a.x - 10) + Math.abs(a.y - 10);
			return -mdist;
		});

		DeterministicPlanner planner = new AStar(domain, goalCondition, hashingFactory, mdistHeuristic);
		Policy p = planner.planFromState(initialState);
		PolicyUtils.rollout(p, initialState, domain.getModel()).write(outputPath + "astar");

	}
	/**
	 * Planin algorithm: Breadth-first search
	 * 
	 */
	public void BFSExample() {
		DeterministicPlanner planner = new BFS(domain, goalCondition, hashingFactory);
		Policy p = planner.planFromState(initialState);
		PolicyUtils.rollout(p, initialState, domain.getModel()).write(outputPath + "bfs");

	}
	/**
	 * Planin algorithm: depth-first search
	 * 
	 */
	public void DFSExample() {
		DeterministicPlanner planner = new DFS(domain, goalCondition, hashingFactory);
		Policy p = planner.planFromState(initialState);
		PolicyUtils.rollout(p, initialState, domain.getModel()).write(outputPath + "dfs");

	}

	/**
	 * learning algorithm: Q-learning
	 * 
	 */
	public void QLearningExample() {

		LearningAgent agent = new QLearning(domain, 0.99, hashingFactory, 0., 1.);

		// run learning for 50 episodes
		for (int i = 0; i < 50; i++) {
			Episode e = agent.runLearningEpisode(env);

			e.write(outputPath + "ql_" + i);
			System.out.println("Episode: " + i + " Steps: " + e.maxTimeStep());

			// reset environment for next learning episode
			env.resetEnvironment();
		}

	}
	/**
	 * learning algorithm: SARSA
	 * 
	 */
	public void SarsaLearningExample() {

		LearningAgent agent = new SarsaLam(domain, 0.99, hashingFactory, 0., 0.5, 0.3);

		// run learning for 50 episodes
		for (int i = 0; i < 50; i++) {
			Episode e = agent.runLearningEpisode(env);

			e.write(outputPath + "sarsa_" + i);
			System.out.println(i + ": " + e.maxTimeStep());

			// reset environment for next learning episode
			env.resetEnvironment();
		}

	}

	public void experimentAndPlotter() {

		// different reward function for more structured performance plots
		((FactoredModel) domain.getModel()).setRf(new GoalBasedRF(this.goalCondition, 5.0, -0.1));

		/**
		 * Create factories for Q-learning agent and SARSA agent to compare
		 */
		LearningAgentFactory qLearningFactory = new LearningAgentFactory() {

			public String getAgentName() {
				return "Q-Learning";
			}

			public LearningAgent generateAgent() {
				return new QLearning(domain, 0.99, hashingFactory, 0.3, 0.1);
			}
		};

		LearningAgentFactory sarsaLearningFactory = new LearningAgentFactory() {

			public String getAgentName() {
				return "SARSA";
			}

			public LearningAgent generateAgent() {
				return new SarsaLam(domain, 0.99, hashingFactory, 0.0, 0.1, 1.);
			}
		};

		LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter(env, 10, 100, qLearningFactory,
				sarsaLearningFactory);
		exp.setUpPlottingConfiguration(500, 250, 2, 1000, TrialMode.MOST_RECENT_AND_AVERAGE,
				PerformanceMetric.CUMULATIVE_STEPS_PER_EPISODE, PerformanceMetric.AVERAGE_EPISODE_REWARD);

		exp.startExperiment();
		exp.writeStepAndEpisodeDataToCSV("expData");
	}

	/**
	 * Planin algorithm: value iteration
	 * 
	 */
	public void valueIterationExample() {
		Planner planner = new ValueIteration(domain, 0.99, hashingFactory, 0.001, 100);
		Policy p = planner.planFromState(initialState);
		PolicyUtils.rollout(p, initialState, domain.getModel()).write(outputPath + "vi");

		simpleValueFunctionVis((ValueFunction) planner, p);
	}

	public void visualize(String outputPath) {
		Visualizer v = GridWorldVisualizer.getVisualizer(gwdg.getMap());
		EpisodeSequenceVisualizer esv = new EpisodeSequenceVisualizer(v, domain, outputPath);
		esv.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		esv.setBounds(35, 35, 800, 600);
	}
}