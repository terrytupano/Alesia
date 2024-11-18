package hero.ozsoft;

import java.util.*;
import java.util.stream.*;

import org.apache.commons.math3.util.*;

import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.actions.*;

public class BettingSequence {

    private static Map<Long, List<PlayerSummary>> bettingHistory = new TreeMap<>(Comparator.reverseOrder());

    private Map<Integer, List<PlayerAction>> streets;
    private List<String> sequence = new ArrayList<>();
    private List<PlayerSummary> playerSumaries = new ArrayList<>();

    // street in simulation eviroment. in live environment, the information is readed form the bettingHistory
    private int street;

    // id of the currnet hand or instance of this bettingSequence
    private long handId;

    public static final String SEPARATOR = "------ ";

    public BettingSequence() {
        this.street = PokerSimulator.NO_CARDS_DEALT;
        this.streets = new TreeMap<>();
        streets.put(street, new ArrayList<>());
        this.handId = System.currentTimeMillis();
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

        // remove summary for folded player
        // error i cant alter the record process
        // if (action instanceof FoldAction) {
        // playerSumaries.removeIf(s -> s.name.equals(player.getName()));
        // return;
        // }

        addSummary(player);
    }

    public double getHeroMaxChips() {
        List<PlayerSummary> list = new ArrayList<>(playerSumaries);
        list.removeIf(s -> "Hero".equals(s.name));
        double max = list.stream().mapToDouble(s -> s.chips).max().orElse(0);
        return max;
    }

    public void addSummary(Player player) {
        PlayerSummary summary = new PlayerSummary(player.getName(), street, player.getChair(), player.getCash(),
                player.hasCards());
        addSummary(summary);
    }

    /**
     * collect and store for futher analisis, the villan infomation readed from the screensensor in live action hero
     * battle.
     * 
     * @param playerSummary - the player info
     */
    public void addSummary(PlayerSummary playerSummary) {
        this.playerSumaries.add(playerSummary);

        // update the history of the current hand
        List<PlayerSummary> list = bettingHistory.get(handId);
        if (list == null) {
            list = new ArrayList<>();
            bettingHistory.put(handId, list);
        }
        list.add(playerSummary);
    }

    /**
     * not completed method. delete
     * @param bb
     * @return
     */
    public double wasIRaised(double bb) {
        // i raise is at least 3 bigblind
        double bbb = bb * 3;

        List<PlayerSummary> currentHand = bettingHistory.get(handId);
        if (currentHand == null || currentHand.isEmpty())
            return -1;

        // select only active players & the current street
        List<PlayerSummary> currentStreet = new ArrayList<>(currentHand);
        currentStreet.removeIf(ps -> ps.isAlive && ps.street == street);

        if (currentStreet.isEmpty())
            return -1;

        return 0;
    }

    /**
     * compute and return a characterization of the player sit on the char passed as argument. The first element of the
     * returned {@link Pair} is the flop frequency and the second is the betting amount. both values are normalized.
     * meaning frequency=1 the villan saw all preflop
     * 
     * 20241109: this methos was tested and dont work. using the chips to try to calculate the avg of betting yield a
     * false result. in the chip information are included the winnins/loses of a particula player.
     * 
     * 
     * @param chair - the chair
     * @return the player type
     */
    public Map<Integer, Pair<Double, Double>> getPlayersType(double buyIn, double bb) {
        int window = 30;
        int counter = 0;
        Map<Integer, List<Double>> chipsByChair = new HashMap<>();
        Map<Integer, Integer> preFlopByChair = new HashMap<>();

        for (int i = 1; i <= Table.MAX_CAPACITY; i++) {
            chipsByChair.put(i, new ArrayList<>());
            preFlopByChair.put(i, 0);
        }

        for (List<PlayerSummary> hand : bettingHistory.values()) {
            if (counter > window)
                break;

            // for every chair, collect statistic
            for (int i = 1; i <= Table.MAX_CAPACITY; i++) {

                // select only preflops & chair with index "i"
                final int ch = i;
                List<PlayerSummary> preflop = new ArrayList<>(hand);
                preflop.removeIf(ps -> ps.chair != ch || ps.street != PokerSimulator.HOLE_CARDS_DEALT);

                List<Double> chipByChair = chipsByChair.get(i);
                for (PlayerSummary playerSummary : preflop) {
                    chipByChair.add(playerSummary.chips);
                }

                // is there preflop info, count
                if (chipByChair.size() > 0) {
                    preFlopByChair.put(i, preFlopByChair.get(i) + 1);
                }
            }
            counter++;
        }

        // foreach chipsByChair list, compute the diference to obtain the betting ammounts
        for (List<Double> chipByChair : chipsByChair.values()) {

            // only 1 element in this list meaning error o maybe check??
            if (chipByChair.size() == 1) {
                chipByChair.clear();
                continue;
            }

            List<Double> doubles = new ArrayList<>(chipByChair);
            chipByChair.clear();
            for (int i = 0; i < doubles.size() - 1; i++) {
                chipByChair.add(doubles.get(i) - doubles.get(i + 1));
            }
        }

        // remove 0s. 0s mean chechs or multiple measurement with the same value
        for (List<Double> chipByChair : chipsByChair.values()) {
            chipByChair.removeIf(d -> d == 0);
        }

        // 20241109: this methos was tested and dont work. using the chips to try to calculate the avg of betting yield
        // asfalse result. in the chip information are included the winnins/loses of a particula player.

        // for each colected statistic, make a player characteritation
        Map<Integer, Pair<Double, Double>> pairs = new HashMap<>();
        for (int i = 1; i <= Table.MAX_CAPACITY; i++) {
            OptionalDouble optionalDouble = chipsByChair.get(i).stream().mapToDouble(Double::doubleValue).average();
            if (optionalDouble.isPresent()) {
                int flop = preFlopByChair.get(i);
                double avg = optionalDouble.getAsDouble();
                Pair<Double, Double> pair = new Pair<>((double) flop, avg);
                pairs.put(i, pair);
            }
        }
        return pairs;
    }

    /**
     * Effective stack sizes are nothing more than the size of the smallest stack between two different players in a
     * hand. This indicates the highest amount of money you can either win or lose in a hand against any one particular
     * opponent.
     * 
     * <p>
     * this method return the smallest stack from {@link #playerSumaries}
     * 
     * @return the smallerst stack
     */
    public double getEfectiveStackSize() {
        List<PlayerSummary> list = new ArrayList<>(playerSumaries);
        // if a player hat chips=0, the player go allin. remove
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
    public static class PlayerSummary {
        public long currentTimeMillis;
        public int street;
        public int chair;
        public String name;
        public double chips;
        public boolean isAlive;

        public PlayerSummary(String name, int street, int position, double chips, boolean isAlive) {
            this.chips = chips;
            this.currentTimeMillis = System.currentTimeMillis();
            this.name = name;
            this.chair = position;
            this.street = street;
            this.isAlive = isAlive;
        }
    }
}