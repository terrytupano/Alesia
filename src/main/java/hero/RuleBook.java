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
                .description("A semi-bluff occurs when you bet with a drawing hand.").when(facts -> isPosFlop())
                .then(facts -> evaluateSemiBluff()).build();
        rules.register(semiBluff);
    }

    private void evaluateTauPreflop() {
        // Low SPR (0-5): Favor Strong Hands and Aggressive Play
        // Medium SPR (5-15): Balanced Play with a Mix of Hand Strengths
        // High SPR (15+): Speculative Play for Big Pots
        double sprUpper = 15;
        UoAHand holeCards = pokerSimulator.holeCards;
        double callValue = pokerSimulator.callValue;
        double raiseValue = pokerSimulator.raiseValue;
        double tablePosition = pokerSimulator.getTablePosition();
        // double SPRs = pokerSimulator.SPRs > sprUpper ? sprUpper : pokerSimulator.SPRs;
        double SPRs = pokerSimulator.heroChips / pokerSimulator.potValue;
        SPRs = SPRs > sprUpper ? sprUpper : SPRs;

        // 42% is ultil 22 poket pair
        // 45% is to make every step 5%
        // 25 is the max % for preflopCardsModel where all cards hat +EV
        // double upperB = 25d;
        double rangeUpper = 45d;
        double step = rangeUpper / Table.MAX_CAPACITY;
        double preflopRange = step * tablePosition;
        int tau = (int) Math.round(preflopRange);

        // adust by SPRs. the final tau value will be the min of both. this allow the standar tau value be ajusted by
        // the environtment e.g: if hero is in later position (preflopRange is high) and sprs is low. taking th min
        // value (sprs) will avoid wasting chips when hero is in a precarious situation
        double sprRange = SPRs * (Table.MAX_CAPACITY + 1 - tablePosition);
        tau = (int) Math.min(tau, sprRange);
        tau = (int) Math.max(step, tau); // the absolute min is at least 1 step

        preflopCardsModel.setPercentage(tau);
        if (preflopCardsModel.containsHand(holeCards)) {
            // * SPRs to betting. at this point, hero has a good hand. Low SPRs tight agresive, put money on the pot.
            // high SPRs tight pasive call/check
            double value = callValue;
            double r = Math.random();
            if (r < SPRs / sprUpper)
                value = raiseValue;

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

        pokerSimulator.bettingSequence.getPlayersType(pokerSimulator.buyIn, pokerSimulator.bigBlind);
        if (isTheNut) {
            putAction("valueBetting", pokerSimulator.buyIn, availableActions);
        }
    }

    private void evaluateSemiBluff() {
        int outs = (Integer) pokerSimulator.evaluation.getOrDefault("outs", 0);
        int street = pokerSimulator.street;

        // Spots to Go All-in as a Semi-Bluff. 020 Essential Poker Math_ Fundamental
        // No-Limit Hold’em Mathematics You Need to Know p243
        if (PokerSimulator.FLOP_CARDS_DEALT == street && outs > 7) {
            putAction("semiBluff", pokerSimulator.buyIn, availableActions);
        }

        // with more that 9 out i have prob > 0.53
        if (PokerSimulator.TURN_CARD_DEALT == street && outs > 9) {
            putAction("semiBluff", pokerSimulator.buyIn, availableActions);
        }

        // TODO: code coied for checkOportuty method in trooper. NOT TESTED
        // --------------------------------------
        // to this point, if available actions are empty, means hero is responding a
        // extreme high raise. that mean maybe hero is weak. at this point raise mean
        // all in. (call actions is not considerer because is not opportunity)

        // if (actions.size() == 0 && ruleBook.pokerSimulator.raiseValue >= 0)
        // actions.add(new TrooperAction("raise", ruleBook.pokerSimulator.raiseValue));

        // --------------------------------------

    }

    /**
     * Pot odds are the IMMEDIATE odds we’re being offered when we call a bet in poker. The important aspect of this
     * definition is IMMEDIATE, because with pot odds it’s all about how much we stand to win IMMEDIATELY in relation to
     * what we are risking by calling a bet.
     * 
     * Essential poker math p95
     */
    private void evaluatePotOdds() {

        double winProb = pokerSimulator.winProb;
        int darkness = (int) pokerSimulator.evaluation.getOrDefault("darknessHand", 0);

        List<TrooperAction> list = new ArrayList<>(availableActions);
        // Should we call? If we expect to win at least potOdds of the time, we should
        // call. Essential poker math p95
        list.removeIf(a -> a.potOdds > winProb);

        // double texture = ((double) pokerSimulator.evaluation.get("rankBehindTexture%")) / 100d;
        // System.out.println("texture " + texture);

        // TODO: rethink this strategie. why darknes? and how much call/raise? should i take the winprob to select the
        // amunt? e.g: value = winprob * chips? or maybe i need to check: in order to take account of potodd i need to
        // check if i have a made hand. (using darkness to detect it)
        if (!list.isEmpty() && darkness > 0) {
            // use the darknes variable to decide call/raise
            double value = darkness == 2 ? pokerSimulator.potValue : pokerSimulator.raiseValue;
            putAction("potOdds", value, list);
        }
    }

    /**
     * You can think of implied odds as an extension of pot odds. While pot odds are considered our most direct and
     * immediate odds when calling a bet, implied odds are our indirect odds. Recapping on the previous chapter, with
     * pot odds, it’s all about how much we stand to win immediately in relationship to what we’re risking by calling a
     * bet. In contrast, implied odds consider how much we stand to win not only immediately, but also on later rounds
     * of betting after we make the best hand.
     * 
     * Essential poker math p113
     */
    private void evaluateImpliedOdds() {
        if (pokerSimulator.street == PokerSimulator.RIVER_CARD_DEALT) {
            // Hero.heroLogger.info("impliedOdds rule only on the flop & turn street");
            return;
        }

        double outs2 = (Double) pokerSimulator.evaluation.getOrDefault("outs2", 0.0);
        double outs4 = (Double) pokerSimulator.evaluation.getOrDefault("outs4", 0.0);

        final double equity = pokerSimulator.street == PokerSimulator.FLOP_CARDS_DEALT ? outs4 : outs2;
        List<TrooperAction> list = new ArrayList<>(availableActions);

        list.removeIf(a -> a.potOdds > equity);

        if (!list.isEmpty()) {
            putAction("impliedOdds", pokerSimulator.buyIn, list);
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
     * from the list of actions passed as argument, return the action whose ammount is closest to the value argument
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

    private void putAction(String ruleName, double value, List<TrooperAction> actions) {
        TrooperAction action = getCloseTo(actions, value);
        rulesDesitions.put(ruleName, action);
    }

}
