package hero.rules;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.*;
import hero.UoAHandEval.*;

public class AlphaBet extends BasicRule {

    private RuleBook ruleBook;
    private PreflopCardsModel preflopCardsModel = new PreflopCardsModel();

    AlphaBet(RuleBook ruleBook) {
        super("Alpha", "Betting strategui based on alpha parameter");
        this.ruleBook = ruleBook;
    }

    @Override
    public boolean evaluate(Facts facts) {
        // only preflop
        return ruleBook.pokerSimulator.street == PokerSimulator.HOLE_CARDS_DEALT;
    }

    @Override
    public void execute(Facts facts) throws Exception {
        int alpha = ruleBook.pokerSimulator.trooperParameter.getInteger("alpha");

        UoAHand holeCards = ruleBook.pokerSimulator.holeCards;

        preflopCardsModel.setPercentage(alpha);

    }
}
