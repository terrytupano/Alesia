package ticTacToe;

import java.util.*;
import java.util.Map.*;
/**
 * A policy iteration agent. You should implement the following methods: (1)
 * {@link PolicyIterationAgent#evaluatePolicy}: this is the policy evaluation step from your lectures (2)
 * {@link PolicyIterationAgent#improvePolicy}: this is the policy improvement step from your lectures (3)
 * {@link PolicyIterationAgent#train}: this is a method that should runs/alternate (1) and (2) until convergence.
 * 
 * NOTE: there are two types of convergence involved in Policy Iteration: Convergence of the Values of the current
 * policy, and Convergence of the current policy to the optimal policy. The former happens when the values of the
 * current policy no longer improve by much (i.e. the maximum improvement is less than some small delta). The latter
 * happens when the policy improvement step no longer updates the policy, i.e. the current policy is already optimal.
 * The algorithm should stop when this happens.
 * 
 * @author ae187
 *
 */
public class PolicyIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states according to the current policy (policy evaluation).
	 */
	HashMap<Game, Double> policyValues = new HashMap<Game, Double>();

	/**
	 * This stores the current policy as a map from {@link Game}s to {@link Move}.
	 */
	HashMap<Game, Move> curPolicy = new HashMap<Game, Move>();

	double discount = 0.9;

	/**
	 * The mdp model used, see {@link TTTMDP}
	 */
	TTTMDP mdp;

	/**
	 * loads the policy from file if one exists. Policies should be stored in .pol files directly under the project
	 * folder.
	 */
	public PolicyIterationAgent() {
		super();
		this.mdp = new TTTMDP();
		initValues();
		initRandomPolicy();
		train();

	}

	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * 
	 * @param p
	 */
	public PolicyIterationAgent(Policy p) {
		super(p);

	}

	/**
	 * Use this constructor to initialise a learning agent with default MDP paramters (rewards, transitions, etc) as
	 * specified in {@link TTTMDP}
	 * 
	 * @param discountFactor
	 */
	public PolicyIterationAgent(double discountFactor) {

		this.discount = discountFactor;
		this.mdp = new TTTMDP();
		initValues();
		initRandomPolicy();
		train();
	}
	/**
	 * Use this constructor to set the various parameters of the Tic-Tac-Toe MDP
	 * 
	 * @param discountFactor
	 * @param winningReward
	 * @param losingReward
	 * @param livingReward
	 * @param drawReward
	 */
	public PolicyIterationAgent(double discountFactor, double winningReward, double losingReward, double livingReward,
			double drawReward) {
		this.discount = discountFactor;
		this.mdp = new TTTMDP(winningReward, losingReward, livingReward, drawReward);
		initValues();
		initRandomPolicy();
		train();
	}
	/**
	 * Initialises the {@link #policyValues} map, and sets the initial value of all states to 0 (V0 under some policy pi
	 * ({@link #curPolicy} from the lectures). Uses {@link Game#inverseHash} and
	 * {@link Game#generateAllValidGames(char)} to do this.
	 * 
	 */
	public void initValues() {
		List<Game> allGames = Game.generateAllValidGames('X');// all valid games where it is X's turn, or it's terminal.
		for (Game g : allGames)
			this.policyValues.put(g, 0.0);

	}

	/**
	 * You should implement this method to initially generate a random policy, i.e. fill the {@link #curPolicy} for
	 * every state. Take care that the moves you choose for each state ARE VALID. You can use the
	 * {@link Game#getPossibleMoves()} method to get a list of valid moves and choose randomly between them.
	 */
	public void initRandomPolicy() {
		Random r = new Random();
		Set<Entry<Game, Double>> entrySet = policyValues.entrySet();
		Iterator<Entry<Game, Double>> setIterator = entrySet.iterator();

		while (setIterator.hasNext()) {
			Entry<Game, Double> currentEntry = setIterator.next();
			Game g = currentEntry.getKey();

			List<Move> moves = g.getPossibleMoves();
			if (moves.size() != 0) {

				// Modified from the RandomPolicy class
				List<IndexPair> pairs = new ArrayList<IndexPair>();

				for (int i = 0; i < 3; i++)
					for (int j = 0; j < 3; j++) {
						if (g.getBoard()[i][j] == ' ')
							pairs.add(new IndexPair(i, j));
					}

				if (pairs.size() > 0) { // ensure positive random value
					IndexPair random = pairs.get(r.nextInt(pairs.size()));
					Move randomMove = new Move(g.whoseTurn, random.x, random.y);

					while (!g.isLegal(randomMove)) { // ensure the move is legal
						random = pairs.get(r.nextInt(pairs.size()));
						randomMove = new Move(g.whoseTurn, random.x, random.y);
					}

					curPolicy.put(g, randomMove);
				}
			}
		}
	}

	/**
	 * Performs policy evaluation steps until the maximum change in values is less than {@param delta}, in other words
	 * until the values under the current policy converge. After running this method, the
	 * {@link PolicyIterationAgent#policyValues} map should contain the values of each reachable state under the current
	 * policy. You should use the {@link TTTMDP} {@link PolicyIterationAgent#mdp} provided to do this.
	 *
	 * @param delta
	 */
	protected void evaluatePolicy(double delta) {
		double maxChange = 0.0;
		do {
			Set<Entry<Game, Move>> entrySet = curPolicy.entrySet(); // Fetch the current entrySet
			maxChange = 0.0;
			Iterator<Entry<Game, Move>> setIterator = entrySet.iterator();// Create iterator over the entry sets

			while (setIterator.hasNext()) { // iterate over entrySet

				Entry<Game, Move> currentEntry = setIterator.next();
				Game key = currentEntry.getKey(); // returns the game key of the current set
				List<Move> moves = key.getPossibleMoves(); // fetches the possible moves in the current set
				Iterator<Move> moveIterator = moves.iterator(); // Create iterator over all moves
				double currentValue = policyValues.get(key);

				if (currentEntry.getKey().isTerminal()) {
					this.policyValues.put(currentEntry.getKey(), 0.0);
				} else {
					while (moveIterator.hasNext()) { // iterate over possible moves

						Move potentialMove = moveIterator.next();
						List<TransitionProb> transitionProbability = mdp.generateTransitions(key, curPolicy.get(key)); // Generate
																														// the
																														// transition
																														// probabilities
																														// from
																														// the
																														// mdp
						Iterator<TransitionProb> transitionIterator = transitionProbability.iterator(); // Iterate over
																										// transitions
						double vk1s = 0.0; // Variable to store value of Vk+1(s)
						while (transitionIterator.hasNext()) { // Loop through transition list
							TransitionProb currentTransitionProbability = transitionIterator.next();
							double t = currentTransitionProbability.prob; // Return the probability
							double r = currentTransitionProbability.outcome.localReward; // Return the reward
							double vksPrime = policyValues.get(currentTransitionProbability.outcome.sPrime); // Return
																												// the
																												// value
																												// of
																												// Vk(s')
							vk1s += t * (r + (discount * vksPrime)); // Value iterator equation
						}
						double change = Math.abs(currentValue - vk1s);
						policyValues.replace(key, vk1s);
						if (change > maxChange) {
							maxChange = change;
						}
					}
				}
			}
		} while (maxChange >= delta);
	}
	/**
	 * This method should be run AFTER the {@link PolicyIterationAgent#evaluatePolicy} train method to improve the
	 * current policy according to {@link PolicyIterationAgent#valueFuncion}. You will need to do a single step of
	 * expectimax from each game (state) key in {@link PolicyIterationAgent#curPolicy} to look for a move/action that
	 * potentially improves the current policy.
	 * 
	 * @return true if the policy improved. Returns false if there was no improvement, i.e. the policy already returned
	 *         the optimal actions.
	 */
	protected boolean improvePolicy() {
		Set<Entry<Game, Move>> entrySet = curPolicy.entrySet(); // Fetch the current entrySet
		boolean optimalPolicy = false;
		Iterator<Entry<Game, Move>> setIterator = entrySet.iterator();// Create iterator over the entry sets

		while (setIterator.hasNext()) { // iterate over entrySet
			Entry<Game, Move> currentEntry = setIterator.next();
			Game key = currentEntry.getKey(); // returns the game key of the current set
			List<Move> moves = key.getPossibleMoves(); // fetches the possible moves in the current set
			Iterator<Move> moveIterator = moves.iterator(); // Create iterator over all moves
			List<Double> a = new ArrayList<Double>(); // List to store values of a
			double maxA = 0.0;

			Move currentMove = curPolicy.get(key);
			Move bestMove = null;

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
					double vksPrime = policyValues.get(currentTransitionProbability.outcome.sPrime); // Return the value
																										// of Vk(s')
					vk1s += t * (r + (discount * vksPrime)); // Value iterator equation
				}

				a.add(vk1s); // add the current value of vks to the list a
				maxA = Collections.max(a); // maxA stores the current maximum of the list

				if (vk1s == maxA) { // if the current value is the max of a, then add move as the policy
					bestMove = potentialMove;
				}
			}

			if (!(bestMove.equals(currentMove))) {
				curPolicy.put(key, bestMove);
				optimalPolicy = true;
			}
		}
		return optimalPolicy;
	}

	/**
	 * The (convergence) delta
	 */
	double delta = 0.1;

	/**
	 * This method should perform policy evaluation and policy improvement steps until convergence (i.e. until the
	 * policy no longer changes), and so uses your {@link PolicyIterationAgent#evaluatePolicy} and
	 * {@link PolicyIterationAgent#improvePolicy} methods.
	 */
	public void train() {
		do {
			this.evaluatePolicy(delta);
		} while (this.improvePolicy() == true);

		super.policy = new Policy(curPolicy);
	}

	public static void main(String[] args) throws IllegalMoveException {
		// Test method to play the agent against a human agent.
		PolicyIterationAgent agent = new PolicyIterationAgent();
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
		System.out.println("Pi plays first              Agg plays first");
		if (aggWins > 9)
			System.out.println("Wins:   " + aggWins + "                  Wins:   " + aggWins2);
		if (aggWins <= 9)
			System.out.println("Wins:   " + aggWins + "                   Wins:   " + aggWins2);
		if (aggDraws > 9)
			System.out.println("Draws:  " + aggDraws + "                  Draws:  " + aggDraws2);
		if (aggDraws <= 9)
			System.out.println("Draws:  " + aggDraws + "                   Draws:  " + aggDraws2);
		System.out.println("Losses: " + aggLosses + "                   Losses: " + aggLosses2 + "\n");

		System.out.println("\n" + games + " Games Against Defensive Agent:\n");
		System.out.println("Pi plays first              Def plays first");
		if (defWins > 9)
			System.out.println("Wins:   " + defWins + "                  Wins:   " + defWins2);
		if (defWins <= 9)
			System.out.println("Wins:   " + defWins + "                   Wins:   " + defWins2);
		if (defDraws > 9)
			System.out.println("Draws:  " + defDraws + "                  Draws:  " + defDraws2);
		if (defDraws <= 9)
			System.out.println("Draws:  " + defDraws + "                   Draws:  " + defDraws2);
		System.out.println("Losses: " + defLosses + "                   Losses: " + defLosses2 + "\n");

		System.out.println("\n" + games + " Games Against Random Agent:\n");
		System.out.println("Pi plays first              Random plays first");
		if (randomWins > 9)
			System.out.println("Wins:   " + randomWins + "                  Wins:   " + randomWins2);
		if (randomWins <= 9)
			System.out.println("Wins:   " + randomWins + "                   Wins:   " + randomWins2);
		if (randomDraws > 9)
			System.out.println("Draws:  " + randomDraws + "                  Draws:  " + randomDraws2);
		if (randomDraws <= 9)
			System.out.println("Draws:  " + randomDraws + "                   Draws:  " + randomDraws2);
		System.out.println("Losses: " + randomLosses + "                   Losses: " + randomLosses2 + "\n");

	}

}
