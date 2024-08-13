package hero.rules;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.*;
import hero.UoAHandEval.*;

public class ChenScoreRule extends BasicRule {

    private HashMap<String, Object> result;

    ChenScoreRule(HashMap<String, Object> result) {
        this.result = result;
    }

    @Override
    public String getName() {
        return "the Chen Formula ";
    }

    @Override
    public String getDescription() {
        return "Ther Chen formula for preflop card.s selection.";
    }

    @Override
    public boolean evaluate(Facts facts) {
        UoAHand hand = (UoAHand) facts.get("hand");
        return hand.size() >= 2;
    }

    @Override
    public void execute(Facts facts) throws Exception {
        UoAHand hand = (UoAHand) facts.get("hand");
        double value = PokerSimulator.getChenScore(hand);
        result.put(this.getClass().getSimpleName(), value);
    }

}
