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
		while (action == null) {
			int raiseValue = minBet * 2;
			boolean callBol = player.getCash() >= minBet;
			boolean raiseBol = callBol && player.getCash() >= raiseValue;
			boolean potBol = callBol && player.getCash() >= pot;
			boolean allinBol = callBol;
			pokerSimulator.sensorStatus.put("call", callBol);
			pokerSimulator.sensorStatus.put("raise", raiseBol);
			pokerSimulator.sensorStatus.put("raise.pot", potBol);
			pokerSimulator.sensorStatus.put("raise.allin", allinBol);
			pokerSimulator.sensorStatus.put("raise.slider", allinBol);

			pokerSimulator.setCallValue(minBet);
			if (allowedActions.contains(PlayerAction.CHECK))
				pokerSimulator.setCallValue(0);

			pokerSimulator.setPotValue(pot);
			pokerSimulator.setHeroChips(player.getCash());
			pokerSimulator.setRaiseValue(raiseValue);
			int actV = (int) villains.stream().filter(p -> p.hasCards()).count();
			pokerSimulator.setNunOfOpponets(actV);
			pokerSimulator.setTablePosition(dealer, actV);

			pokerSimulator.runSimulation();
			trooperParameter.fromMap(simulationVariables);
			TrooperAction act = trooper.getSimulationAction(trooperParameter);

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
			if (!(PlayerAction.CHECK.equals(action) || PlayerAction.FOLD.equals(action)) && act.amount < minBet
					&& act.amount < player.getCash())
				action = null;

		}
		return action;

	}

	@Override
	public void messageReceived(String message) {
		super.messageReceived(message);
		// if (message.equals(Table.RESTAR)) {
		// System.out.println("Avg descition method: "+statistics.getMean());
		// }
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
	public void handStarted(Player dealer) {
		this.dealer = villains.indexOf(dealer) + 1;
		pokerSimulator.bigBlind = bigBlind;
		pokerSimulator.smallBlind = bigBlind / 2;
		pokerSimulator.buyIn = buyIn;
		pokerSimulator.clearEnvironment();
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
