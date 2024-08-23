package hero;

import java.util.*;

import com.google.common.collect.Comparators;

public class PokerSimulatorTraker {
    public List<Integer> streets = new ArrayList<>();
    public List<Integer> opponents = new ArrayList<>();
    public List<Double> heroChips = new ArrayList<>();
    public List<Double> callValues = new ArrayList<>();
    public List<Double> raiseValues = new ArrayList<>();
    public List<Double> potValues = new ArrayList<>();

    public List<List<?>> lists = new ArrayList<>();

    public PokerSimulatorTraker() {
        //
    }

    /**
     * return the # of bet round of the last street. eg. if the list contains
     * HOLE_CARDS_DEALT, FLOP_CARDS_DEALT, TURN_CARD_DEALT, TURN_CARD_DEALT this
     * method return 2
     * 
     * @return
     */
    public int getStreetRound() {
        if (streets.isEmpty())
            return -1;

        int lastRound = streets.get(streets.size() - 1);
        int rounds = (int) streets.stream().filter(s -> s == lastRound).count();
        return rounds;
    }

    public double getHeroMaxChips() {
        double max = heroChips.stream().max(Comparator.naturalOrder()).orElse(-1.0);
        return max;
    }

    public void update(PokerSimulator simulator) {
        streets.add(simulator.street);
        opponents.add(simulator.villans);
        heroChips.add(simulator.heroChips);
        callValues.add(simulator.callValue);
        raiseValues.add(simulator.raiseValue);
        potValues.add(simulator.potValue);
    }

    public void newHand() {
        streets.clear();
        opponents.clear();
        heroChips.clear();
        callValues.clear();
        raiseValues.clear();
        potValues.clear();
    }
}
