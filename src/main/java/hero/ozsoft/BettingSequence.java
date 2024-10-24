package hero.ozsoft;

import java.util.*;
import java.util.stream.*;

import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.actions.*;

public class BettingSequence {

    private Map<Integer, List<PlayerAction>> streets;
    private List<String> sequence;
    private int street;
    public static final String SEPARATOR = "------ ";

    public BettingSequence() {
        this.sequence = new ArrayList<>();
        this.street = PokerSimulator.NO_CARDS_DEALT;
        this.streets = new TreeMap<>();
        streets.put(street, new ArrayList<>());
    }

    public void addAction(Player player, PlayerAction action) {
        List<PlayerAction> list = streets.get(street);
        list.add(action);

        String name = "";
        String action2 = action.toString();
        if (player != null)
            name = player.getName();

        if (action instanceof FoldAction || action instanceof CheckAction) {
            action2 = action.getName();
        }

        addMessage(name + " - " + action2);
    }

    public void addStreet(int street, UoAHand board) {
        this.street = street;
        streets.put(street, new ArrayList<>());
        String board2 = board == null ? "" : ": " + board;

        addMessage(SEPARATOR + PokerSimulator.streetNames.get(street) + board2);
    }

    public void addMessage(String message, Object... args) {
        String message2 = String.format(message, args);
        sequence.add(message2);
    }

    public String getSequence() {
        String result = sequence.stream().collect(Collectors.joining("\n"));
        return result;
    }

    private void printStreets() {
        for (Map.Entry<Integer, List<PlayerAction>> street2 : streets.entrySet()) {
            System.out.println(PokerSimulator.streetNames.get(street2.getKey()));
            for (PlayerAction action : street2.getValue()) {
                System.out.println("  " + action);
            }
        }
    }
}