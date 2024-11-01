package hero.ozsoft;

import java.util.*;
import java.util.stream.*;

import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.actions.*;

public class BettingSequence {

    private Map<Integer, List<PlayerAction>> streets;
    private List<String> sequence = new ArrayList<>();
    private List<PlayerSumary> playerSumaries = new ArrayList<>();

    private int street;
    public static final String SEPARATOR = "------ ";

    public BettingSequence() {
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

        PlayerSumary playerSumary = new PlayerSumary(player.getName(), street, player.getChair(), player.getCash());
        playerSumaries.add(playerSumary);
    }

    public double getHeroMaxChips() {
        List<PlayerSumary> list = new ArrayList<>(playerSumaries);
        list.removeIf(s -> "Hero".equals(s.names));
        double max = list.stream().mapToDouble(s -> s.chips).max().orElse(0);
        return max;
    }

    /**
     * update the sumaries readed from the screensensor in live action hero battle
     * 
     * not implemented
     * 
     * @param playerSumaries the sumasies
     */
    public void update(PlayerSumary... playerSumaries) {
        this.playerSumaries.clear();
        this.playerSumaries.addAll(Arrays.asList(playerSumaries));
    }

    /**
     * return the smallest stack from {@link #playerSumaries}
     * 
     * @return the stack
     */
    public double getSmallestStack() {
        // if a player hat chips=0, the player go allin. remove
        List<PlayerSumary> list = new ArrayList<>(playerSumaries);
        list.removeIf(s -> s.chips == 0);

        // due the instance of this betting sequence is a new instance for every hand, all the elements in the list
        // allways haven less chips every time. simple get the minimum
        double min = list.stream().mapToDouble(s -> s.chips).min().orElse(0);
        return min;
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

    /**
     * bridge bettwen simulation table information and live tropper action information retrived by ScreenSensor
     * 
     */
    public static class PlayerSumary {
        public long currentTimeMillis;
        public int street;
        public int position;
        public String names;
        public double chips;

        public PlayerSumary(String name, int street, int position, double chips) {
            this.chips = chips;
            this.currentTimeMillis = System.currentTimeMillis();
            this.names = name;
            this.position = position;
            this.street = street;
        }
    }
}