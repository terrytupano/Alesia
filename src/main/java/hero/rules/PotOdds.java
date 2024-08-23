package hero.rules;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.*;

public class PotOdds extends BasicRule {

    private RuleBook ruleBook;

    PotOdds(RuleBook ruleBook) {
        super("Pot odds", "the ratio of the current size of the pot to the cost of a contemplated call.");
        this.ruleBook = ruleBook;
    }

    @Override
    public boolean evaluate(Facts facts) {
        // only posflop
        return ruleBook.pokerSimulator.street > PokerSimulator.HOLE_CARDS_DEALT;
    }

    @Override
    public void execute(Facts facts) throws Exception {
        double winProb = ruleBook.pokerSimulator.winProb;

        List<TrooperAction> actions = PokerSimulator.loadActions(ruleBook.pokerSimulator, winProb);
        if (!actions.isEmpty())
            ruleBook.addAction(this, actions);
    }
}
