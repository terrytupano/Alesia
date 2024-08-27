package hero.ozsoft.bots;

import java.util.*;

import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.*;
import hero.ozsoft.actions.*;

/**
 * Wrapper between Poker simulator and Hero trooper
 * 
 */
public class HeroBot extends Bot {

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		PlayerAction action = null;
		// while (action == null) {

		activeteSensors(minBet, currentBet, allowedActions);

		pokerSimulator.runSimulation();
		pokerSimulator.trooperParameter.fromMap(simulationVariables);
		TrooperAction act = trooper.getSimulationAction();

		if (act.equals(TrooperAction.FOLD))
			action = PlayerAction.FOLD;
		if (act.equals(TrooperAction.CHECK))
			action = PlayerAction.CHECK;
		if (act.name.equals("call") && act.amount > 0) {
			if (allowedActions.contains(PlayerAction.CALL))
				action = new CallAction((int) act.amount);
			if (allowedActions.contains(PlayerAction.BET))
				action = new BetAction((int) act.amount);
		}
		if (act.name.equals("raise") || act.name.equals("pot") || act.name.equals("allIn")) {
			if (allowedActions.contains(PlayerAction.RAISE))
				action = new RaiseAction((int) act.amount);
			if (allowedActions.contains(PlayerAction.BET))
				action = new BetAction((int) act.amount);
		}

		if (action == null)
			throw new IllegalArgumentException("Hero bot has no correct action selected. Trooper action was" + act);

		// avoid Player '" + name + "' asked to pay more cash than he owns!
		// if (!(PlayerAction.CHECK.equals(action) || PlayerAction.FOLD.equals(action))
		// && act.amount < minBet
		// && act.amount < player.getCash())
		// action = null;

		// avoid Illegal client action: raise less than minimum bet!
		// if (!(PlayerAction.CHECK.equals(action) || PlayerAction.FOLD.equals(action))
		// && act.amount < minBet
		// && act.amount < player.getCash())
		// action = null;

		// }
		return action;

	}

	@Override
	public void messageReceived(String message) {
		super.messageReceived(message);
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		super.boardUpdated(hand, bet, pot);
		if (hand.getCard(1) != null)
			pokerSimulator.cardsBuffer.put("flop1", hand.getCard(1).toString());
		if (hand.getCard(2) != null)
			pokerSimulator.cardsBuffer.put("flop2", hand.getCard(2).toString());
		if (hand.getCard(3) != null)
			pokerSimulator.cardsBuffer.put("flop3", hand.getCard(3).toString());
		if (hand.getCard(4) != null)
			pokerSimulator.cardsBuffer.put("turn", hand.getCard(4).toString());
		if (hand.getCard(5) != null)
			pokerSimulator.cardsBuffer.put("river", hand.getCard(5).toString());
	}

	@Override
	public void playerUpdated(Player player) {
		super.playerUpdated(player);
		if (myHole.size() > 0) {
			pokerSimulator.cardsBuffer.put("hero.card1", myHole.getCard(1).toString());
			pokerSimulator.cardsBuffer.put("hero.card2", myHole.getCard(2).toString());
		}
	}
}
