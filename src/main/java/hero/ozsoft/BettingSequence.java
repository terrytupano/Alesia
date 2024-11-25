package hero.ozsoft;

import static tech.tablesaw.aggregate.AggregateFunctions.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.apache.commons.math3.util.*;

import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.actions.*;
import tech.tablesaw.api.*;

public class BettingSequence {

    private static Table table = PlayerSummary.getTableTemplate();
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
        PlayerSummary summary = new PlayerSummary(player.getName(), handId, street, player.getChair(), player.getCash(),
                player.hasCards(), player.isDealer);
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
        PlayerSummary.addRow(table, playerSummary);
    }

    /**
     * compute and return a characterization of the player sit on the char passed as argument. The first element of the
     * returned {@link Pair} is the flop frequency and the second is the betting amount. both values are normalized.
     * meaning frequency=1 the villan saw all preflop
     * 
     * @param chair - the chair
     * @return the player type
     */
    public Map<Integer, Pair<Integer, Double>> getPlayersType(double buyIn, double bb) {
        int window = 30;
        Map<Integer, Pair<Integer, Double>> pairs = new HashMap<>();

        for (int i = 1; i <= PokerTable.MAX_CAPACITY; i++) {
            Table table2 = table.where(table.intColumn("chair").isEqualTo(i));
            System.out.println(table2);

            if (table2.rowCount() > window)
                table2 = table2.inRange(window);
            System.out.println(table2);

            // ((DoubleColumn) table2.column("chips")).mult summarize("chips", min, max).apply();
            // System.out.println(table2a);

            Table table3 = table2.summarize("chips", change).by("handId");
            System.out.println(table3);

            // LongColumn handIds = (LongColumn) table2.column("handId").unique();
            // System.out.println(handIds.asList());
            DoubleColumn changeChips = ((DoubleColumn) table3.column("Change [chips]"));
            OptionalDouble optionalDouble = changeChips.asList().stream().mapToDouble(Double::doubleValue).average();
            if (optionalDouble.isPresent()) {
                DoublePredicate predicate = (d) -> d > 0;
                int flops = changeChips.count(predicate);
                double avg = optionalDouble.getAsDouble();
                Pair<Integer, Double> pair = new Pair<>(flops, avg);
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
        public long handId;
        public long currentTimeMillis;
        public int street;
        public int chair;
        public String name;
        public double chips;
        public boolean isAlive;
        public boolean isDealer;

        public PlayerSummary(String name, long handId, int street, int chair, double chips, boolean isAlive,
                boolean isDealer) {
            this.name = name;
            this.handId = handId;
            this.street = street;
            this.chair = chair;
            this.chips = chips;
            this.isAlive = isAlive;
            this.isDealer = isDealer;
            this.currentTimeMillis = System.currentTimeMillis();
        }

        public static void addRow(Table table, PlayerSummary summary) {
            Row row = table.appendRow();
            row.setString("name", summary.name);
            row.setLong("handId", summary.handId);
            row.setInt("street", summary.street);
            row.setInt("chair", summary.chair);
            row.setDouble("chips", summary.chips);
            row.setBoolean("isAlive", summary.isAlive);
            row.setBoolean("isDealer", summary.isDealer);
            row.setLong("currentTimeMillis", summary.currentTimeMillis);
        }

        public static Table getTableTemplate() {
            StringColumn name = StringColumn.create("name");
            LongColumn handId = LongColumn.create("handId");
            IntColumn street = IntColumn.create("street");
            IntColumn chair = IntColumn.create("chair");
            DoubleColumn chips = DoubleColumn.create("chips");
            BooleanColumn isAlive = BooleanColumn.create("isAlive");
            BooleanColumn isDealer = BooleanColumn.create("isDealer");
            LongColumn currentTimeMillis = LongColumn.create("currentTimeMillis");

            Table table = Table.create("PlayerSumaries").addColumns(name, handId, street, chair, chips, isAlive,
                    isDealer, currentTimeMillis);

            return table;
        }
    }
}