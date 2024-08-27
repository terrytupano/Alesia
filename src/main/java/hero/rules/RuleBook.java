package hero.rules;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.*;

public class RuleBook {

    public static final String HOLECARDS = "holeCards";
    public static final String COMMUNITY_CARDS = "communityCards";

    public HashMap<String, Object> rulesDesitions;

    private Facts facts;
    private Rules rules;

    public TrooperAction action;

    PokerSimulator pokerSimulator;

    public RuleBook(PokerSimulator simulator) {
        this.pokerSimulator = simulator;
        this.rules = new Rules();
        this.rulesDesitions = new HashMap<>();
        this.facts = new Facts();

        rules.register(new ChenScore(this));
        rules.register(new PotOdds(this));
        rules.register(new ImpliedOdds(this));

        // Rule rule = new RuleBuilder().name("Chen Formula")
        // .description("Ther Chen formula for preflop card.s selection.")
        // .when(facts -> isPreflop(facts)).then(facts -> getChenScore(facts)).build();
        // rules.register(rule);

    }

    public void addAction(BasicRule rule, Object action) {
        String key = rule.getClass().getSimpleName();
        rulesDesitions.put(key, action);
    }

    public void fire() {
        RulesEngine engine = new DefaultRulesEngine();
        engine.fire(rules, facts);
    }

    /**
     * compute from reward:risk notation to probability. prob = risk / (reward +
     * risk)
     * 
     * @param reward - the reward
     * @param risk   - the risk
     * @return - the probability
     */
    public static double rewardRiskToProb(double reward, double risk) {
        double odds = risk / (reward + risk);
        return odds;
    }
}
