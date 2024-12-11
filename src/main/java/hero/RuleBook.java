package hero;

import java.util.*;

import org.apache.commons.math3.util.*;
import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import core.*;
import datasource.*;
import hero.UoAHandEval.*;
import hero.ozsoft.*;
import hero.ozsoft.BettingSequence.*;

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
    private double sprUpper = 15;
    private Random random;

    public RuleBook(PokerSimulator simulator) {
        this.pokerSimulator = simulator;
        this.rules = new Rules();
        this.rulesDesitions = new HashMap<>();
        this.facts = new Facts();
        this.rulesEngine = new DefaultRulesEngine();
        this.preflopCardsModel = new PreflopCardsModel();
        this.random = new Random();
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

        Rule setMining = new RuleBuilder().name("Set Mining").description(
                "when you call a pre-flop raise with the sole intention of flopping a set with a small pocket pair such as 22-55.")
                .when(facts -> isPreFlop()).then(facts -> evaluateSetMining()).build();
        rules.register(setMining);

        /**
         * preflop and posflop
         */
        Rule valueBetting = new RuleBuilder().name("Value Betting")
                .description("to extract as much money as possible from your opponents when you have a winning hand.")
                .then(facts -> evaluateValueBetting()).build();
        rules.register(valueBetting);

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

        Rule semiBluff = new RuleBuilder().name("Semi Bluff")
                .description("A semi-bluff occurs when you bet with a drawing hand.").when(facts -> isPosFlop())
                .then(facts -> evaluateSemiBluff()).build();
        rules.register(semiBluff);

        Rule bluff = new RuleBuilder().name("Bluff")
                .description("a bluff is a bet or raise made with a hand which is not thought to be the best hand.").when(facts -> isPosFlop())
                .then(facts -> evaluateBluff()).build();
        rules.register(bluff);
    }

    private double getSPRs() {
        double SPRs = pokerSimulator.heroChips / pokerSimulator.potValue;
        SPRs = SPRs > sprUpper ? sprUpper : SPRs;
        return SPRs;
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
        double SPRs = getSPRs();

        double rangeUpper = PokerSimulator.PREFLOP_RANGE;
        double step = rangeUpper / PokerTable.MAX_CAPACITY;
        double preflopRange = step * tablePosition;
        int tau = (int) Math.round(preflopRange);

        // adust by SPRs. the final tau value will be the min of both. this allow the standar tau value be ajusted by
        // the environtment e.g: if hero is in later position (preflopRange is high) and sprs is low. taking th min
        // value (sprs) will avoid wasting chips when hero is in a precarious situation
        // see "preflop correction using SPR" tab in Some statistics.xlsx
        double sprRange = SPRs * (PokerTable.MAX_CAPACITY + 2 - tablePosition);
        tau = (int) Math.min(tau, sprRange);
        tau = (int) Math.max(step, tau); // the absolute min is at least 1 step

        preflopCardsModel.setPercentage(tau);
        if (preflopCardsModel.containsHand(holeCards)) {
            // * SPRs to betting. at this point, hero has a good hand. Low SPRs tight agresive, put money on the pot.
            // high SPRs tight pasive call/check
            // see "preflop correction using SPR" tab in Some statistics.xlsx
            double value = callValue;
            double r = random.nextDouble();
            if (r < 1 - (SPRs / sprUpper))
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

    /**
     * A value bet in poker is a bet made with the intention of being called by an opponent with a weaker hand. The goal
     * is to extract as much money as possible from the opponent when you believe you have the best hand.
     */
    private void evaluateValueBetting() {
        // preflop: all in with 2% AA KK QQ
        preflopCardsModel.setPercentage(2);
        boolean allIn = preflopCardsModel.containsHand(pokerSimulator.holeCards);

        // posflop
        boolean isTheNut = (Boolean) pokerSimulator.evaluation.getOrDefault("isTheNut", false);
        if (isTheNut || allIn) {
            putAlphaAction("valueBetting", availableActions);
        }
    }

    private double get3Bet() {
        double bbb = pokerSimulator.bigBlind * 3;
        // if call value > 0 i was raised
        if (pokerSimulator.callValue > 0)
            bbb = pokerSimulator.callValue * 3;
        return bbb;
    }

    /**
     * Set-mining is when you call a pre-flop raise with the sole intention of flopping a set with a small pocket pair
     * such as 22-55. Essential Poker Math p206
     */
    private void evaluateSetMining() {
        boolean rank = pokerSimulator.holeCards.getCard(1).getRank() < 6;
        boolean pokerPair = UoAHandEvaluator.isPoketPair(pokerSimulator.holeCards);
        // this rulle apply to poket pair less that 66 (55-22) if not, return
        if (!(rank && pokerPair))
            return;

        // 15-to-1 Rule. Essential Poker Math p207
        double ess = pokerSimulator.bettingSequence.getEfectiveStackSize();
        if ((ess < pokerSimulator.bigBlind * 15))
            return;

        Map<Integer, PlayerType> map = pokerSimulator.bettingSequence.getPlayersType(pokerSimulator.bigBlind);
        for (Map.Entry<Integer, PlayerType> entry : map.entrySet()) {
            TrooperParameter parameter = TrooperParameter.findFirst("chair = ?", entry.getKey());
            System.out.println(parameter.getString("trooper") + " BigBlinds: "
                    + TResources.twoDigitFormat.format(entry.getValue().bigBlinds) + " Flops: "
                    + TResources.twoDigitFormat.format(entry.getValue().flops) + " " + entry.getValue().designation);
        }

        putAlphaAction("setMining", availableActions);

    }

    private void evaluateBlindSteal() {
        // only valid for BTN
        if (pokerSimulator.getTablePosition() != PokerTable.MAX_CAPACITY - 1)
            return;

        boolean rank = pokerSimulator.holeCards.getCard(1).getRank() < 6;
        boolean pokerPair = UoAHandEvaluator.isPoketPair(pokerSimulator.holeCards);
        // this rulle apply to poket pair less that 66 (55-22) if not, return
        if (!(rank && pokerPair))
            return;

        // 15-to-1 Rule. Essential Poker Math p207
        double ess = pokerSimulator.bettingSequence.getEfectiveStackSize();
        if ((ess < pokerSimulator.bigBlind * 15))
            return;

        // In general, you want to risk the least amount possible when stealing your opponents’ blinds.
        double value = pokerSimulator.bigBlind * 2;
        putAction("setMining", value, availableActions);

    }

    /**
     * In the card game of poker, a bluff is a bet or raise made with a hand which is not thought to be the best hand.
     * To bluff is to make such a bet. The objective of a bluff is to induce a fold by at least one opponent who holds a
     * better hand.
     */
    private void evaluateBluff() {
        // Optimal Bluffing Frequency work only on the river
        if (PokerSimulator.RIVER_CARD_DEALT != pokerSimulator.street)
            return;

        // in this method, i will try only 1 pot size
        // double obf = pokerSimulator.potValue /(2*pokerSimulator.potValue + 1);
        double obf = 1 / 3d;
        if (random.nextDouble() > obf)
            return;

        double value = pokerSimulator.potValue;
        putAction("bluff", value, availableActions);

    }

    private void evaluateSemiBluff() {
        int outs = (Integer) pokerSimulator.evaluation.getOrDefault("outs", 0);
        int street = pokerSimulator.street;

        // A♣ T♣ and the flop is 5♣ K♣ 8♥ Raising all-in as a semi-bluff improves our equity to 45%; while
        // simultaneously providing additional
        // benefits. If villain only has a pair of Kings, we can make him fold better hands by forcing him into a tough
        // all-in decision. By semi-bluff raising, we can now win the hand by either making our opponent fold, or making
        // the best hand on the river.
        // Essential Poker Math_ Fundamental p170: shoul we call? section
        // todo: this paragraph means is posible make a semibluff if the turn outs are -EV put the river outs are +EV

        double outs2 = (Double) pokerSimulator.evaluation.getOrDefault("outs2", 0.0);
        double outs4 = (Double) pokerSimulator.evaluation.getOrDefault("outs4", 0.0);

        // List<TrooperAction> list = new ArrayList<>(availableActions);
        // list.removeIf(a -> a.potOdds > equity);
        long evfor2 = availableActions.stream().filter(a -> outs2 > a.potOdds).count();
        long evfor4 = availableActions.stream().filter(a -> outs4 > a.potOdds).count();
        double sprs = getSPRs();

        if (evfor2 == 0 && evfor4 > 0 && sprs < 5)
            putAction("semiBluff", pokerSimulator.buyIn, availableActions);

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
        int darknessHand = (int) pokerSimulator.evaluation.getOrDefault("darknessHand", 0);

        // no made hand, return
        if (darknessHand == 0)
            return;

        List<TrooperAction> list = new ArrayList<>(availableActions);
        // Should we call? If we expect to win at least potOdds of the time, we should
        // call. Essential poker math p95
        list.removeIf(a -> a.potOdds > winProb);

        // double texture = ((double) pokerSimulator.evaluation.get("rankBehindTexture%")) / 100d;
        // System.out.println("texture " + texture);

        // double SPRs = getSPRs();

        if (!list.isEmpty()) {
            TrooperAction action = getCloseToAlpha(list);
            // use the darknes variable to decide call/raise
            double value = darknessHand == 2 ? action.amount : pokerSimulator.callValue;
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

        int darknessDraw = (Integer) pokerSimulator.evaluation.getOrDefault("darknessDraw", 0);
        // no draw, return
        if (darknessDraw == 0)
            return;

        double outs2 = (Double) pokerSimulator.evaluation.getOrDefault("outs2", 0.0);
        double outs4 = (Double) pokerSimulator.evaluation.getOrDefault("outs4", 0.0);

        final double equity = pokerSimulator.street == PokerSimulator.FLOP_CARDS_DEALT ? outs4 : outs2;
        List<TrooperAction> list = new ArrayList<>(availableActions);

        list.removeIf(a -> a.potOdds > equity);

        if (!list.isEmpty()) {
            TrooperAction action = getCloseToAlpha(list);
            // use the darkness to raise or call
            double value = darknessDraw == 2 ? action.amount : pokerSimulator.callValue;
            putAction("impliedOdds", value, list);
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

            // Give priority to some rules.
            TrooperAction action2 = rulesDesitions.get("valueBetting");
            if (action2 != null)
                return action2;

            // remove same action selected by multiples rules
            List<TrooperAction> list = new ArrayList<>();
            rulesDesitions.forEach((k, v) -> {
                if (!list.contains(v))
                    list.add(v);
            });

            // sort the actions.s values in ascending order
            // Collections.sort(list, (o1, o2) -> ((Double) o1.amount).compareTo((Double) o2.amount));
            // System.out.println("RuleBook.getAction() " + list);
            // action = list.get(list.size() - 1);

            Collections.shuffle(list);
            action = list.get(0);
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
        TrooperAction action = TrooperAction.FOLD;
        for (TrooperAction trooperAction : actions) {
            action = Math.abs(trooperAction.amount - value) < Math.abs(action.amount - value) ? trooperAction : action;
        }
        return action;
    }

    /**
     * From the list of actions passed as argument, return the action whose ammount is closet to the alpha from the
     * {@link TrooperParameter} asociated with this instance
     * 
     * @param actions - the +EV actions
     * @return the action
     */
    private TrooperAction getCloseToAlpha(List<TrooperAction> actions) {
        TrooperAction action = TrooperAction.FOLD;
        int alpha = pokerSimulator.trooperParameter.getInteger("alpha");

        // select the closest value from the all options
        int i = alpha * availableActions.size() / 100;
        action = availableActions.get(i);
        double value = action.amount;

        // select for the +ev list, select the closest to the value
        for (TrooperAction trooperAction : actions) {
            action = Math.abs(trooperAction.amount - value) < Math.abs(action.amount - value) ? trooperAction : action;
        }

        return action;
    }

    private void putAction(String ruleName, double value, List<TrooperAction> actions) {
        TrooperAction action = getCloseTo(actions, value);
        rulesDesitions.put(ruleName, action);
    }

    private void putAlphaAction(String ruleName, List<TrooperAction> actions) {
        TrooperAction action = getCloseToAlpha(actions);
        rulesDesitions.put(ruleName, action);
    }

}
