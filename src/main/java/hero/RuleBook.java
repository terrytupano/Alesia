package hero;

import java.util.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import hero.UoAHandEval.*;
import hero.ozsoft.*;

public class RuleBook {

    public static final String HOLECARDS = "holeCards";
    public static final String COMMUNITY_CARDS = "communityCards";

    private HashMap<String, TrooperAction> rulesDesitions;
    private Facts facts;
    private Rules rules;
    private List<TrooperAction> availableActions;
    private RulesEngine rulesEngine;
    private PreflopCardsModel preflopCardsModel;
    private PokerSimulator pokerSimulator;

    public RuleBook(PokerSimulator simulator) {
        this.pokerSimulator = simulator;
        this.rules = new Rules();
        this.rulesDesitions = new HashMap<>();
        this.facts = new Facts();
        this.rulesEngine = new DefaultRulesEngine();
        this.preflopCardsModel = new PreflopCardsModel();

        /**
         * preflop
         */
        // Rule chenScore = new RuleBuilder().name("Chen Formula")
        // .description("The Chen formula for preflop card.s selection.")
        // .when(facts -> isPreFlop()).then(facts -> evaluateChenScore()).build();
        // rules.register(chenScore);

        Rule tauPreflop = new RuleBuilder().name("Tau preflop")
                .description("Preflop card.s selection based on the tau, alpha and tableposition parameters.")
                .when(facts -> isPreFlop()).then(facts -> evaluateTauPreflop()).build();
        rules.register(tauPreflop);

        /**
         * posflop
         */
        Rule impliedOdds = new RuleBuilder().name("Implied Odds")
                .description("The amount of money that you expect to win on later streets if you hit one of your outs.")
                .when(facts -> isPosFlop()).then(facts -> evaluateImpliedOdds()).build();
        rules.register(impliedOdds);

        Rule potOdds = new RuleBuilder().name("Pot odds")
                .description("the ratio of the current size of the pot to the cost of a contemplated call.")
                .when(facts -> isPosFlop()).then(facts -> evaluatePotOdds()).build();
        rules.register(potOdds);

        Rule valueBetting = new RuleBuilder().name("Value Betting")
                .description("to extract as much money as possible from your opponents when you have a winning hand.")
                .when(facts -> isPosFlop()).then(facts -> evaluateValueBetting()).build();
        rules.register(valueBetting);

        Rule semiBluff = new RuleBuilder().name("Semi Bluff")
                .description("A semi-bluff occurs when you bet with a drawing hand.")
                .when(facts -> isPosFlop()).then(facts -> evaluateSemiBluff()).build();
        rules.register(semiBluff);
    }

    private void evaluateTauPreflop() {
        UoAHand holeCards = pokerSimulator.holeCards;
        double callValue = pokerSimulator.callValue;
        double raiseValue = pokerSimulator.raiseValue;
        double tablePosition = pokerSimulator.getTablePosition();

        // 25 is the max % for preflopCardsModel where all cards hat +EV
        double upB = 25d;
        double step = upB / (double) Table.MAX_CAPACITY;
        // use the table position to compute the distance tp=1 tight tp=9 loose in
        // reference to raise/re-raise
        int tau2 = (int) Math.round(step * tablePosition);
        preflopCardsModel.setPercentage(tau2);

        if (preflopCardsModel.containsHand(holeCards)) {
            // to allow call use the table position to probabilistic, select the call action
            double value = raiseValue;
            double r = Math.random();
            if (r < tablePosition / 10d)
                value = callValue;

            TrooperAction action = getCloseTo(availableActions, value);
            rulesDesitions.put("tauPreflop", action);
        }
    }

    private void evaluateChenScore() {
        UoAHand holeCards = pokerSimulator.holeCards;
        double score = PokerSimulator.getChenScore(holeCards);
        double callValue = pokerSimulator.callValue;
        int tablePosition = pokerSimulator.getTablePosition();

        int raise = 12;
        int call = 10;
        int raiseFactor = 2;
        // int raise = pokerSimulator.trooperParameter.getInteger("chenRaise");
        // int call = pokerSimulator.trooperParameter.getInteger("chenCall");
        // int raiseFactor =
        // pokerSimulator.trooperParameter.getInteger("chenRaiseFactor");

        // Always raise or reraise with x points or more.
        if (score >= raise) {
            TrooperAction action = getCloseTo(availableActions, callValue * raiseFactor);
            rulesDesitions.put("chenScore", action);
            return;
        }

        // Only ever consider calling a raise with x points or more.
        if (score >= call) {
            TrooperAction action = getCloseTo(availableActions, callValue);
            rulesDesitions.put("chenScore", action);
            return;
        }
    }

    private void evaluateValueBetting() {
        boolean isTheNut = (Boolean) pokerSimulator.evaluation.getOrDefault("isTheNut", false);
        int valueBetting = pokerSimulator.trooperParameter.getInteger("valueBetting");

        if (isTheNut) {
            putAction("valueBetting", valueBetting, availableActions);
        }
    }

    private void evaluateSemiBluff() {
        int outs = (Integer) pokerSimulator.evaluation.getOrDefault("outs", 0);
        int semiBluff = pokerSimulator.trooperParameter.getInteger("semiBluff");

        // Spots to Go All-in as a Semi-Bluff. 020 Essential Poker Math_ Fundamental
        // No-Limit Hold’em Mathematics You Need to Know p243
        if (outs > 7) {
            putAction("semiBluff", semiBluff, availableActions);

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

    private void evaluatePotOdds() {
        double winProb = pokerSimulator.winProb;
        int potOdds = pokerSimulator.trooperParameter.getInteger("potOdds");

        List<TrooperAction> list = new ArrayList<>(availableActions);
        list.removeIf(a -> a.potOdds > winProb);

        if (!list.isEmpty()) {
            putAction("potOdds", potOdds, list);
        }
    }

    private void evaluateImpliedOdds() {
        double outs2 = (Double) pokerSimulator.evaluation.getOrDefault("outs2", 0.0);
        double outs4 = (Double) pokerSimulator.evaluation.getOrDefault("outs4", 0.0);
        int impliedOdds = pokerSimulator.trooperParameter.getInteger("impliedOdds");

        final double equity = pokerSimulator.street == PokerSimulator.FLOP_CARDS_DEALT ? outs4 : outs2;
        List<TrooperAction> list = new ArrayList<>(availableActions);
        list.removeIf(a -> a.potOdds > equity);

        if (!list.isEmpty()) {
            putAction("impliedOdds", impliedOdds, list);
        }
    }

    private boolean isPosFlop() {
        return pokerSimulator.street >= PokerSimulator.FLOP_CARDS_DEALT;
    }

    private boolean isPreFlop() {
        return pokerSimulator.street == PokerSimulator.HOLE_CARDS_DEALT;
    }

    /**
     * reset all values and prepare for star a new hand.
     */
    public void newHand() {

    }

    /**
     * Fire all registered rules.
     */
    public void fire() {
        rulesDesitions.clear();
        this.availableActions = PokerSimulator.loadActions(pokerSimulator, null);
        // System.out.println(actions);
        rulesEngine.fire(rules, facts);
    }

    /**
     * return the {@link TrooperAction } decide by this RuleBook instance
     * 
     * @return the action
     */
    public TrooperAction getAction() {
        TrooperAction action = TrooperAction.FOLD;
        // log a report
        // Hero.heroLogger.info("RuleBook decitions: " + rulesDesitions.toString());

        if (!rulesDesitions.isEmpty()) {
            List<TrooperAction> list = new ArrayList<>();
            rulesDesitions.forEach((k, v) -> {
                // avoid add the same action selected by rules
                if (!list.contains(v))
                    list.add(v);
            });

            // sort the actions.s values in ascending order
            Collections.sort(list, (o1, o2) -> ((Double) o1.amount).compareTo((Double) o2.amount));
            // System.out.println("RuleBook.getAction() " + list);
            action = list.get(list.size() - 1);

            // Collections.shuffle(list);
            // action = list.get(0);
        }
        return action;
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

    /**
     * from the list of actions passed as argument, return the action whose ammount
     * is closest to the value argument
     * 
     * @param actions - the actions
     * @param value   - the value
     * @return the closest action
     */
    public static TrooperAction getCloseTo(List<TrooperAction> actions, double value) {
        TrooperAction action = TrooperAction.CHECK;
        for (TrooperAction trooperAction : actions) {
            action = Math.abs(trooperAction.amount - value) < Math.abs(action.amount - value) ? trooperAction : action;
        }
        return action;
    }

    private void putAction(String ruleName, int factor, List<TrooperAction> actions) {
        int alpha = pokerSimulator.trooperParameter.getInteger("alpha");

        // 10=1 bb, 20=2 bb ... 100/10bb
        double value = alpha * pokerSimulator.bigBlind / 10d;
        double r = Math.random();
        double factor2 = factor / 100d;
        if (r < factor2)
            value = pokerSimulator.callValue;

        TrooperAction action = getCloseTo(actions, value);
        rulesDesitions.put(ruleName, action);
    }

}
