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
        super("the Chen Formula", "Ther Chen formula for preflop card.s selection.");
        this.ruleBook = ruleBook;
        this.properties = new Properties();
        this.raisePositions = new HashMap<>();
        this.callPositions = new HashMap<>();
        this.raiseFactors = new HashMap<>();

        RuleBook.loadStrategy(this, properties);

        // raise/call/fold chenscores idexed by position
        for (int position = 0; position < Table.CAPACITY; position++) {
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
        return RuleBook.isPreflop(facts);
    }

    @Override
    public void execute(Facts facts) throws Exception {
        String key = this.getClass().getSimpleName();
        UoAHand hand = (UoAHand) facts.get(RuleBook.HOLECARDS);
        double score = PokerSimulator.getChenScore(hand);

        int callValue = (Integer) facts.get("callValue");
        int tablePosition = (Integer) facts.get("tablePosition");

        int raise = raisePositions.get(tablePosition);
        int call = callPositions.get(tablePosition);
        double raiseFactor = raiseFactors.get(tablePosition);

        // Always raise or reraise with x points or more.
        if (score >= raise) {
            ruleBook.result.put(key, new TrooperAction("raise", callValue * raiseFactor));
            return;
        }

        // Only ever consider calling a raise with x points or more.
        if (score >= call) {
            ruleBook.result.put(key, new TrooperAction("call", callValue));
            return;
        }

        ruleBook.result.put(key, TrooperAction.FOLD);
    }
}
