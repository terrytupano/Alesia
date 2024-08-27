package hero.rules;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.*;

public class Consolidate extends BasicRule {

    private RuleBook ruleBook;

    Consolidate(RuleBook ruleBook) {
        super("UnionRule", "Perform a union of the know rules.s reslut");
        this.ruleBook = ruleBook;
    }

    @Override
    public boolean evaluate(Facts facts) {
        return true;
    }

    @Override
    public void execute(Facts facts) throws Exception {
        // preflop
        if(ruleBook.pokerSimulator.street == PokerSimulator.HOLE_CARDS_DEALT) {
            // do nothing. preflop action are allwasy
        }

        double winProb = ruleBook.pokerSimulator.winProb;

        List<TrooperAction> actions = PokerSimulator.loadActions(ruleBook.pokerSimulator, winProb);
        if (!actions.isEmpty())
            ruleBook.addAction(this, actions);
    }
}
