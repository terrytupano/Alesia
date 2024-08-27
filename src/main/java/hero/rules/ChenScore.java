package hero.rules;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.*;

public class ChenScore extends BasicRule {

    private RuleBook ruleBook;
    private Properties properties;
    private Map<Integer, Integer> raisePositions;
    private Map<Integer, Integer> callPositions;
    private Map<Integer, Double> raiseFactors;

    ChenScore(RuleBook ruleBook) {
        super("Chen Formula", "The Chen formula for preflop card.s selection.");
        this.ruleBook = ruleBook;
        this.properties = new Properties();
        this.raisePositions = new HashMap<>();
        this.callPositions = new HashMap<>();
        this.raiseFactors = new HashMap<>();

        // raise/call/fold chenscores idexed by position
        for (int position = 0; position < Table.MAX_CAPACITY; position++) {
            int raise = (Integer) properties.getOrDefault("raisePosition." + position, 15);
            raisePositions.put(position, raise);
            int call = (Integer) properties.getOrDefault("callPosition." + position, 10);
            callPositions.put(position, call);
            double factor = (Double) properties.getOrDefault("raiseFactor." + position, 1.5);
            raiseFactors.put(position, factor);
        }
    }

    @Override
    public boolean evaluate(Facts facts) {
        // only preflop
        return ruleBook.pokerSimulator.street == PokerSimulator.HOLE_CARDS_DEALT;
    }

    @Override
    public void execute(Facts facts) throws Exception {
        UoAHand holeCards = ruleBook.pokerSimulator.holeCards;
        double score = PokerSimulator.getChenScore(holeCards);
        double callValue = ruleBook.pokerSimulator.callValue;

        int tablePosition = ruleBook.pokerSimulator.getTablePosition();

        int raise = raisePositions.get(tablePosition - 1);
        int call = callPositions.get(tablePosition - 1);
        double raiseFactor = raiseFactors.get(tablePosition - 1);

        // Always raise or reraise with x points or more.
        if (score >= raise) {
            TrooperAction action = new TrooperAction("raise", callValue * raiseFactor);
            ruleBook.addAction(this, action);
            Hero.heroLogger.info("chen score say: " + action);
            return;
        }

        // Only ever consider calling a raise with x points or more.
        if (score >= call) {
            TrooperAction action = new TrooperAction("call", callValue);
            ruleBook.addAction(this, action);
            Hero.heroLogger.info("chen score say: " + action);
            return;
        }

        Hero.heroLogger.info("Chen score found no action to perform");
    }
}
