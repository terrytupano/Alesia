package hero.ozsoft;

import static tech.tablesaw.aggregate.AggregateFunctions.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.apache.commons.math3.stat.regression.*;
import org.apache.commons.math3.util.*;

import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.actions.*;
import tech.tablesaw.api.*;

public class BettingSequence {

    // constant to measurement of the player type flop max value.
    public static final int FRAME_WINDOW = 30;
    // the max average chips of a player type. empiricaly set to this value based on simulations
    public static final int AVGBB_WINDOW = 20;

    private static Table table = PlayerSummary.getTableTemplate();
    private Map<Integer, List<PlayerAction>> streets;
    private List<String> sequence;
    private List<PlayerSummary> playerSumaries;

    // street in simulation eviroment. in live environment, the information is readed form the bettingHistory
    private int street;

    // id of the currnet hand or instance of this bettingSequence
    private long handId;

    public static final String SEPARATOR = "------ ";

    public BettingSequence() {
        this.street = PokerSimulator.NO_CARDS_DEALT;
        this.streets = new TreeMap<>();
        streets.put(street, new ArrayList<>());
        this.sequence = new ArrayList<>();
        this.playerSumaries = new ArrayList<>();
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
     * compute and return a characterization of the player sit on the char passed as argument. On the returned returned
     * {@link Pair} :
     * <p>
     * The first element is the average chips on this period of time expresed in big blinds
     * <p>
     * the second is the flop frequency. how many flop the dude saw. the maximun is {@link #FRAME_WINDOW}
     *
     * @param bigBlind - the big blind
     * @return the evaluations
     */
    public Map<Integer, PlayerType> getPlayersType(double bigBlind) {
        Map<Integer, PlayerType> pairs = new HashMap<>();
        for (int i = 1; i <= PokerTable.MAX_CAPACITY; i++) {
            // select the current chair
            Table table2 = table.where(table.intColumn("chair").isEqualTo(i)
                    .and(table.intColumn("street").isEqualTo(PokerSimulator.FLOP_CARDS_DEALT))
                    .and(table.booleanColumn("isAlive").isTrue()));

            // remove 0 (is 0 and active allin??)
            table2 = table2.where(table2.doubleColumn("chips").isGreaterThan(0));

            // select only the last window elements
            if (table2.rowCount() > FRAME_WINDOW)
                table2 = table2.inRange(-FRAME_WINDOW);

            List<Double> doubles = table2.doubleColumn("chips").asList();
            double max = doubles.stream().mapToDouble(Double::doubleValue).max().orElse(0d);
            double min = doubles.stream().mapToDouble(Double::doubleValue).min().orElse(0d);

            // positive or negative diference
            SimpleRegression regression = new SimpleRegression();
            for (int j = 0; j < doubles.size(); j++) {
                regression.addData(j + 1, doubles.get(j));
            }
            double slope = regression.getSlope();

            double diff = max - min;
            int flops = table2.rowCount();
            double avgBigBlinds = 0;
            // double flops2 = 0;
            if (flops > 0) {
                avgBigBlinds = diff / flops / bigBlind;
                // avg = slope < 0 ? avg * -1.0 : avg;
                // flops2 = flops / (double) FRAME_WINDOW;
            }
            PlayerType type = new PlayerType(avgBigBlinds, flops);
            pairs.put(i, type);
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
        // only the current street. this avoid return previous recorded values of later positions when the invoquer fron
        // this method is in early positions.
        list.removeIf(s -> s.street != street);

        // remove no active & when the player hat chips=0, the player go allin.
        list.removeIf(s -> !s.isAlive || s.chips == 0);

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

    public static class PlayerType {
        public final static String NO_INFO = "NO_INFO";
        // prefers bet & raises to call, very aggresive, winning players
        public final static String TAGs = "TAG";
        // play too many nads. very agresive hight variance in win/loose
        public final static String LAGs = "LAG";
        // Call with goods hands. if raising, look out! not a winning conbination
        public final static String ROCK = "ROCK";
        // Clls with good hands, if raising, look out. generaly not a wining player
        public final static String CALLING_STATIONS = "CALLING_STATIONS";

        public double bigBlinds;
        public double flops;
        public String designation;

        public PlayerType(double avgBigBlinds, double flops) {
            this.bigBlinds = avgBigBlinds;
            this.flops = flops;
            designation = NO_INFO;
            designation = avgBigBlinds > BettingSequence.AVGBB_WINDOW / 2d && flops < BettingSequence.FRAME_WINDOW / 2d
                    ? TAGs
                    : designation;
            designation = avgBigBlinds > BettingSequence.AVGBB_WINDOW / 2d && flops > BettingSequence.FRAME_WINDOW / 2d
                    ? LAGs
                    : designation;
            designation = avgBigBlinds < BettingSequence.AVGBB_WINDOW / 2d && flops > BettingSequence.FRAME_WINDOW / 2d
                    ? CALLING_STATIONS
                    : designation;
            designation = avgBigBlinds < BettingSequence.AVGBB_WINDOW / 2d && flops < BettingSequence.FRAME_WINDOW / 2d
                    ? CALLING_STATIONS
                    : designation;
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