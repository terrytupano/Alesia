package hero.rules;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.*;
import hero.UoAHandEval.*;

public class RuleBook {

    private HashMap<String, Object> result;

    private final String HOLECARDS = "holeCards";
    private final String COMMUNITY_CARDS = "communityCards";

    private final String CHEN_FORMULA = "chenFormula";

    public RuleBook() {
        this.result = new HashMap<>();
        loadPreFlopRules();
    }

    private void loadPreFlopRules() {
        Rules rules = new Rules();

        Rule rule = new RuleBuilder().name("Chen Formula")
                .description("Ther Chen formula for preflop card.s selection.")
                .when(facts  -> isPreflop(facts)).then(facts -> getChenScore(facts)).build();
        rules.register(rule);

    }

    public boolean isPreflop(Facts facts) {
        UoAHand hand = (UoAHand) facts.get(HOLECARDS);
        return hand.size() >= 2;
    }

    public void getChenScore(Facts facts) throws Exception {
        UoAHand hand = (UoAHand) facts.get(HOLECARDS);
        double value = PokerSimulator.getChenScore(hand);
        result.put(CHEN_FORMULA, value);
    }

}
