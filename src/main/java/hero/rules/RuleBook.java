package hero.rules;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.*;

public class RuleBook {

    public static final String HOLECARDS = "holeCards";
    public static final String COMMUNITY_CARDS = "communityCards";

    public HashMap<String, Object> result;

    private Facts facts;
    private Rules rules;

    PokerSimulator pokerSimulator;

    public RuleBook(PokerSimulator simulator) {
        this.pokerSimulator = simulator;
        this.rules = new Rules();
        this.result = new HashMap<>();
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
        result.put(key, action);
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

        // if for some reason, i hero need check/call (risk = 0) odds are 1
        if (risk == 0)
            return 1.0;

        if (risk > reward)
            odds *= -1.0;

        return odds;
    }
}
