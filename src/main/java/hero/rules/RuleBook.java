package hero.rules;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import org.jeasy.rules.api.*;
import org.jeasy.rules.core.*;

import core.*;
import hero.*;
import hero.UoAHandEval.*;

public class RuleBook {

    public static final String HOLECARDS = "holeCards";
    public static final String COMMUNITY_CARDS = "communityCards";

    public HashMap<String, Object> result;
    private Facts facts;
    private Rules rules;

    public RuleBook() {
        this.rules = new Rules();
        this.result = new HashMap<>();
        this.facts = new Facts();
        loadPreFlopRules();
    }

    private void loadPreFlopRules() {
        rules.register(new ChenScore(this));

        // Rule rule = new RuleBuilder().name("Chen Formula")
        // .description("Ther Chen formula for preflop card.s selection.")
        // .when(facts -> isPreflop(facts)).then(facts -> getChenScore(facts)).build();
        // rules.register(rule);

    }

    /**
     * return true iff on the preflop
     * 
     * @param facts - the facts
     * @return true iff preflop
     */
    public static boolean isPreflop(Facts facts) {
        int street = (Integer) facts.get("street");
        return street == PokerSimulator.FLOP_CARDS_DEALT;
    }

    /**
     * called by {@link PokerSimulator} after the evaluation to update all the facts
     * 
     * @param simulator the simulator
     */
    public void updateFacts(PokerSimulator simulator) {
        facts.add(new Fact<Double>("heroChips", simulator.heroChips));
        facts.add(new Fact<Double>("callValue", simulator.callValue));
        facts.add(new Fact<Double>("raiseValue", simulator.raiseValue));
        facts.add(new Fact<Double>("potValue", simulator.potValue));
        facts.add(new Fact<Double>("buyIn", simulator.buyIn));
        facts.add(new Fact<Double>("smallBlind", simulator.smallBlind));
        facts.add(new Fact<Double>("bigBlind", simulator.bigBlind));

        facts.add(new Fact<UoAHand>("communityCards", simulator.communityCards));
        facts.add(new Fact<UoAHand>("holeCards", simulator.holeCards));
        facts.add(new Fact<UoAHand>("currentHand", simulator.currentHand));

        facts.add(new Fact<Integer>("opponents", simulator.opponents));
        facts.add(new Fact<Integer>("tablePosition", simulator.tablePosition));
        facts.add(new Fact<Integer>("street", simulator.street));
    }

    public static void loadStrategy(BasicRule rule, Properties properties) {
        try {
            String strategyFile = Constants.HERO_RESOURCES + "/" + rule.getClass().getSimpleName() + ".properties";
            File fp = new File(strategyFile);
            properties.load(new FileInputStream(fp));
        } catch (Exception e) {
            Alesia.logger.log(Level.SEVERE, "", e);
        }
    }

    public static void saveStrategy(BasicRule rule, Properties properties) {
        try {
            String strategyFile = Constants.HERO_RESOURCES + "/" + rule.getClass().getSimpleName() + ".properties";
            File fp = new File(strategyFile);
            properties.store(new FileOutputStream(fp), "");
        } catch (Exception e) {
            Alesia.logger.log(Level.SEVERE, "", e);
        }

    }

}
