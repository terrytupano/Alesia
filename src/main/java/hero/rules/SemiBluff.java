package hero.rules;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.*;

public class SemiBluff extends BasicRule {

    private RuleBook ruleBook;

    SemiBluff(RuleBook ruleBook) {
        super("Semi Bluff",
                "A semi-bluff occurs when you bet with a drawing hand.");
        this.ruleBook = ruleBook;
    }

    @Override
    public boolean evaluate(Facts facts) {
        // only flop and futher
        return ruleBook.pokerSimulator.street >= PokerSimulator.FLOP_CARDS_DEALT;
    }

    @Override
    public void execute(Facts facts) throws Exception {
        int outs = (Integer) ruleBook.pokerSimulator.evaluation.getOrDefault("outs", 0);
        boolean isTheNut = (Boolean) ruleBook.pokerSimulator.evaluation.getOrDefault("isTheNut", false);

        // Spots to Go All-in as a Semi-Bluff. 020 Essential Poker Math_ Fundamental
        // No-Limit Hold’em Mathematics You Need to Know p243
        if (outs > 8 || isTheNut) {
            List<TrooperAction> actions = PokerSimulator.loadActions(ruleBook.pokerSimulator, null);
            Collections.sort(actions, (o1, o2) -> Double.compare(o1.amount, o2.amount));

            // arbitrary select allin one more 2
            ruleBook.addAction(this, actions);

            // TODO: code coied for checkOportuty method in trooper. NOT TESTED
            // --------------------------------------
            // to this point, if available actions are empty, means hero is responding a
            // extreme high raise. that mean maybe hero is weak. at this point raise mean
            // all in. (call actions is not considerer because is not opportunity)

            // if (actions.size() == 0 && ruleBook.pokerSimulator.raiseValue >= 0)
            // actions.add(new TrooperAction("raise", ruleBook.pokerSimulator.raiseValue));

            // --------------------------------------
        }
    }
}
