package hero.rules;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.*;

public class ImpliedOdds extends BasicRule {

    private RuleBook ruleBook;

    ImpliedOdds(RuleBook ruleBook) {
        super("Implied Odds",
                "The amount of money that you expect to win on later streets if you hit one of your outs.");
        this.ruleBook = ruleBook;
    }

    @Override
    public boolean evaluate(Facts facts) {
        // only flop and futher 
        return ruleBook.pokerSimulator.street >= PokerSimulator.FLOP_CARDS_DEALT;
    }

    @Override
    public void execute(Facts facts) throws Exception {
        double outs2Perent = (Double) ruleBook.pokerSimulator.evaluation.getOrDefault("outs2%", 0.0);
        double outs4Perent = (Double) ruleBook.pokerSimulator.evaluation.getOrDefault("outs4%", 0.0);

        double equity = outs2Perent / 100.0;

        List<TrooperAction> actions = PokerSimulator.loadActions(ruleBook.pokerSimulator, equity);
        if (!actions.isEmpty())
            ruleBook.addAction(this, actions);
    }
}
