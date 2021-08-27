package ticTacToe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A Value Iteration Agent, only very partially implemented. The methods to implement are: (1)
 * {@link ValueIterationAgent#iterate} (2) {@link ValueIterationAgent#extractPolicy}
 * 
 * You may also want/need to edit {@link ValueIterationAgent#train} - feel free to do this, but you probably won't need
 * to.
 * 
 * @author ae187
 *
 */
public class ValueIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states
	 */
	Map<Game, Double> valueFunction = new HashMap<Game, Double>();

	/**
	 * the discount factor
	 */
	double discount = 0.9;

	/**
	 * the MDP model
	 */
	TTTMDP mdp = new TTTMDP();

	/**
	 * the number of iterations to perform - feel free to change this/try out different numbers of iterations
	 */
	int k = 50;

	/**
	 * This constructor trains the agent offline first and sets its policy
	 */
	public ValueIterationAgent() {
		super();
		mdp = new TTTMDP();
		this.discount = 0.9;
		initValues();
		train();
	}

	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * 
	 * @param p
	 */
	public ValueIterationAgent(Policy p) {
		super(p);

	}

	public ValueIterationAgent(double discountFactor) {

		this.discount = discountFactor;
		mdp = new TTTMDP();
		initValues();
		train();
	}

	/**
	 * Initialises the {@link ValueIterationAgent#valueFunction} map, and sets the initial value of all states to 0 (V0
	 * from the lectures). Uses {@link Game#inverseHash} and {@link Game#generateAllValidGames(char)} to do this.
	 * 
	 */
	public void initValues() {

		List<Game> allGames = Game.generateAllValidGames('X');// all valid games where it is X's turn, or it's terminal.
		for (Game g : allGames)
			this.valueFunction.put(g, 0.0);

	}

	public ValueIterationAgent(double discountFactor, double winReward, double loseReward, double livingReward,
			double drawReward) {
		this.discount = discountFactor;
		mdp = new TTTMDP(winReward, loseReward, livingReward, drawReward);
	}

	/**
	 * 
	 * 
	 * /* Performs {@link #k} value iteration steps. After running this method, the
	 * {@link ValueIterationAgent#valueFunction} map should contain the (current) values of each reachable state. You
	 * should use the {@link TTTMDP} provided to do this.
	 * 
	 *
	 */
	public void iterate() {
		for (int i = 0; i < k; i++) {

			Set<Entry<Game, Double>> entrySet = valueFunction.entrySet(); // Fetch the current entrySet
			Iterator<Entry<Game, Double>> setIterator = entrySet.iterator();// Create iterator over the entry sets

			while (setIterator.hasNext()) { // iterate over entrySet

				Map.Entry<Game, Double> currentEntry = setIterator.next();
				Game key = currentEntry.getKey(); // returns the game key of the current set
				List<Move> moves = key.getPossibleMoves(); // fetches the possible moves in the current set
				Iterator<Move> moveIterator = moves.iterator(); // Create iterator over all moves
				List<Double> a = new ArrayList<Double>(); // List to store values of a
				double maxA = 0.0; // Variable to store the maximum value in the list a

				while (moveIterator.hasNext()) { // iterate over possible moves
					Move potentialMove = moveIterator.next();
					// Generate the transition probabilities from the mdp
					List<TransitionProb> transitionProbability = mdp.generateTransitions(key, potentialMove); 
					Iterator<TransitionProb> transitionIterator = transitionProbability.iterator(); // Iterate over
																									// transitions
					double vk1s = 0.0; // Variable to store value of Vk+1(s)

					// Loop through transition list
					while (transitionIterator.hasNext()) { 
						TransitionProb currentTransitionProbability = transitionIterator.next();
						double t = currentTransitionProbability.prob; // Return the probability
						double r = currentTransitionProbability.outcome.localReward; // Return the reward
						// Return the value of Vk(s')
						double vksPrime = valueFunction.get(currentTransitionProbability.outcome.sPrime); 
						vk1s += t * (r + (discount * vksPrime)); // Value iterator equation
					}

					a.add(vk1s); // add the current value of vks to the list a
					maxA = Collections.max(a); // maxA stores the current maximum of the list a
				}
				a.clear(); // list a is cleared in preparation for the next move
				valueFunction.put(key, maxA); // the value function is updated with the game key and the max value of a
			}
		}
	}

	/**
	 * This method should be run AFTER the train method to extract a policy according to
	 * {@link ValueIterationAgent#valueFunction} You will need to do a single step of expectimax from each game (state)
	 * key in {@link ValueIterationAgent#valueFunction} to extract a policy.
	 * 
	 * @return the policy according to {@link ValueIterationAgent#valueFunction}
	 */

	public Policy extractPolicy() {
		Policy p = new Policy(); // create new policy

		Set<Entry<Game, Double>> entrySet = valueFunction.entrySet(); // Fetch the current entrySet
		Iterator<Entry<Game, Double>> setIterator = entrySet.iterator();// Create iterator over the entry sets

		while (setIterator.hasNext()) { // iterate over entrySet

			Map.Entry<Game, Double> currentEntry = setIterator.next();
			Game key = currentEntry.getKey(); // returns the game key of the current set
			List<Move> moves = key.getPossibleMoves(); // fetches the possible moves in the current set
			Iterator<Move> moveIterator = moves.iterator(); // Create iterator over all moves
			List<Double> a = new ArrayList<Double>(); // List to store values of a
			double maxA = 0.0; // Variable to store the maximum value in the list a

			while (moveIterator.hasNext()) { // iterate over possible moves

				Move potentialMove = moveIterator.next();
				List<TransitionProb> transitionProbability = mdp.generateTransitions(key, potentialMove); // Generate
																											// the
																											// transition
																											// probabilities
																											// from the
																											// mdp
				Iterator<TransitionProb> transitionIterator = transitionProbability.iterator(); // Iterate over
																								// transitions
				double vk1s = 0.0; // Variable to store value of Vk+1(s)

				while (transitionIterator.hasNext()) { // Loop through transition list

					TransitionProb currentTransitionProbability = transitionIterator.next();
					double t = currentTransitionProbability.prob; // Return the probability
					double r = currentTransitionProbability.outcome.localReward; // Return the reward
					double vksPrime = valueFunction.get(currentTransitionProbability.outcome.sPrime); // Return the
																										// value of
																										// Vk(s')
					vk1s += t * (r + (discount * vksPrime)); // Value iterator equation
				}

				a.add(vk1s); // add the current value of vks to the list a
				maxA = Collections.max(a); // maxA stores the current maximum of the list

				if (vk1s == maxA) { // if the current value is the max of a, then add move as the policy
					p.policy.put(key, potentialMove); // add move as the policy for the current entry
				}
			}
			a.clear(); // clear list a in preparation for the next iteration
		}
		return p; // return the policy
	}

	/**
	 * This method solves the mdp using your implementation of {@link ValueIterationAgent#extractPolicy} and
	 * {@link ValueIterationAgent#iterate}.
	 */
	public void train() {
		/**
		 * First run value iteration
		 */
		this.iterate();
		/**
		 * now extract policy from the values in {@link ValueIterationAgent#valueFunction} and set the agent's policy
		 * 
		 */

		super.policy = extractPolicy();

		if (this.policy == null) {
			System.out.println("Unimplemented methods! First implement the iterate() & extractPolicy() methods");
			// System.exit(1);
		}

	}

	public static void main(String a[]) throws IllegalMoveException {
		// Test method to play the agent against a human agent.
		ValueIterationAgent agent = new ValueIterationAgent();
		// Number of games against each agent
		int games = 50;

		// games against the aggressive agent, tallying up the scores
		int aggWins = 0;
		int aggDraws = 0;
		int aggLosses = 0;
		for (int i = 0; i < games; i++) {
			AggressiveAgent agg = new AggressiveAgent();
			Game g = new Game(agent, agg, agent);
			g.playOut();
			if (g.evaluateGameState() == 1) {
				aggWins++;
			} else if (g.evaluateGameState() == 2) {
				aggLosses++;
			} else if (g.evaluateGameState() == 3) {
				aggDraws++;
			}
		}

		// games against the defensive agent, tallying up the scores
		int defWins = 0;
		int defDraws = 0;
		int defLosses = 0;
		for (int i = 0; i < games; i++) {
			DefensiveAgent def = new DefensiveAgent();
			Game g = new Game(agent, def, agent);
			g.playOut();
			if (g.evaluateGameState() == 1) {
				defWins++;
			} else if (g.evaluateGameState() == 2) {
				defLosses++;
			} else if (g.evaluateGameState() == 3) {
				defDraws++;
			}
		}

		// games against the random agent, tallying up the scores
		int randomWins = 0;
		int randomDraws = 0;
		int randomLosses = 0;
		for (int i = 0; i < games; i++) {
			RandomAgent random = new RandomAgent();
			Game g = new Game(agent, random, agent);
			g.playOut();
			if (g.evaluateGameState() == 1) {
				randomWins++;
			} else if (g.evaluateGameState() == 2) {
				randomLosses++;
			} else if (g.evaluateGameState() == 3) {
				randomDraws++;
			}
		}

		// games against the aggressive agent, tallying up the scores
		int aggWins2 = 0;
		int aggDraws2 = 0;
		int aggLosses2 = 0;
		for (int i = 0; i < games; i++) {
			AggressiveAgent agg = new AggressiveAgent();
			Game g = new Game(agent, agg, agg);
			g.playOut();
			if (g.evaluateGameState() == 1) {
				aggWins2++;
			} else if (g.evaluateGameState() == 2) {
				aggLosses2++;
			} else if (g.evaluateGameState() == 3) {
				aggDraws2++;
			}
		}

		// games against the defensive agent, tallying up the scores
		int defWins2 = 0;
		int defDraws2 = 0;
		int defLosses2 = 0;
		for (int i = 0; i < games; i++) {
			DefensiveAgent def = new DefensiveAgent();
			Game g = new Game(agent, def, def);
			g.playOut();
			if (g.evaluateGameState() == 1) {
				defWins2++;
			} else if (g.evaluateGameState() == 2) {
				defLosses2++;
			} else if (g.evaluateGameState() == 3) {
				defDraws2++;
			}
		}

		// games against the random agent, tallying up the scores
		int randomWins2 = 0;
		int randomDraws2 = 0;
		int randomLosses2 = 0;
		for (int i = 0; i < games; i++) {
			RandomAgent random = new RandomAgent();
			Game g = new Game(agent, random, random);
			g.playOut();
			if (g.evaluateGameState() == 1) {
				randomWins2++;
			} else if (g.evaluateGameState() == 2) {
				randomLosses2++;
			} else if (g.evaluateGameState() == 3) {
				randomDraws2++;
			}
		}

		// Print results
		System.out.println("\n" + games + " Games Against Aggressive Agent:\n");
		System.out.println("Vi plays first              Agg plays first");
		if (aggWins > 9)
			System.out.println("Wins:   " + aggWins + "                  Wins:   " + aggWins2);
		else
			System.out.println("Wins:   " + aggWins + "                 Wins:   " + aggWins2);
		if (defWins > 9)
			System.out.println("Draws:  " + aggDraws + "                   Draws:  " + aggDraws2);
		else
			System.out.println("Draws:  " + aggDraws + "                  Draws:  " + aggDraws2);
		System.out.println("Losses: " + aggLosses + "                   Losses: " + aggLosses2 + "\n");

		System.out.println("\n" + games + " Games Against Defensive Agent:\n");
		System.out.println("Vi plays first              Def plays first");
		if (aggWins > 9)
			System.out.println("Wins:   " + defWins + "                  Wins:   " + defWins2);
		else
			System.out.println("Wins:   " + defWins + "                 Wins:   " + defWins2);
		if (defWins > 9)
			System.out.println("Draws:  " + defDraws + "                   Draws:  " + defDraws2);
		else
			System.out.println("Draws:  " + defDraws + "                  Draws:  " + defDraws2);
		System.out.println("Losses: " + defLosses + "                   Losses: " + defLosses2 + "\n");

		System.out.println("\n" + games + " Games Against Random Agent:\n");
		System.out.println("Vi plays first              Random plays first");
		if (aggWins > 9)
			System.out.println("Wins:   " + randomWins + "                  Wins:   " + randomWins2);
		else
			System.out.println("Wins:   " + randomWins + "                 Wins:   " + randomWins2);
		if (defWins > 9)
			System.out.println("Draws:  " + randomDraws + "                   Draws:  " + randomDraws2);
		else
			System.out.println("Draws:  " + randomDraws + "                 Draws:  " + randomDraws2);
		System.out.println("Losses: " + randomLosses + "                   Losses: " + randomLosses2 + "\n");
	}
}
