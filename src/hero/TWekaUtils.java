package hero;

import java.util.*;

import org.javalite.activejdbc.*;

import datasource.*;
import weka.core.*;
import weka.core.converters.ConverterUtils.*;
import weka.core.neighboursearch.*;

public class TWekaUtils {

    public static List<String> streets = Arrays.asList(new String[] { "PreFlop", "Flop", "Turn", "River" });
    private static Map<String, KDTree> treesMap = new HashMap<>();

    public static void buildKdTreeInstances() {
        try {
            // NOT PREPLOP !!
            for (int i = 1; i < streets.size(); i++) {
                String street = streets.get(i);
                for (int round = 0; round < ICRGame.ACTIONS_MATRIX_DEEP; round++) {
                    Instances instances = TWekaUtils.buildKdTreeInstances(street, round);
                    if (!instances.isEmpty()) {
                        // save to file
                        DataSink dataSink = new DataSink(street + " Round " + round + "_instances.arff");
                        dataSink.write(instances);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readKdTree() {
        try {
            for (String street : streets) {
                DataSource dataSource = new DataSource(street + "_instances.arff");
                Instances instances = dataSource.getDataSet();
                System.out.println(instances.toSummaryString());
                KDTree kdTree = new KDTree();
                kdTree.setInstances(instances);
                treesMap.put(street, kdTree);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * build the KDTree for the specific street/round argument. the result is the
     * tree for all selected players actions at the specific stree and round with
     * all know table.s status information
     * 
     * - the instances only games with playerLeft <= Table.CAPACITY
     * - the label for this tree is the action:value pair
     * 
     * 
     * @param street - the sufix for the rank db filed: Flop (rankFlop), Turn
     *               (rankTurn) or River (rankRiver)
     * @param round  - 0 - ICRGame.ACTIONS_MATRIX_DEEP
     * @return the KDTree
     */
    private static Instances buildKdTreeInstances(String street, int round) {
        Attribute playersLeft = new Attribute("playersLeft");
        Attribute position = new Attribute("position");
        Attribute rank = new Attribute("rank");
        Attribute playersStreet = new Attribute("playersStreet");
        Attribute pot = new Attribute("pot");
        Attribute chips = new Attribute("chips");
        Attribute bets = new Attribute("chipsBets");
        Attribute action = new Attribute("action", true); // r:20

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(playersLeft);
        attributes.add(position);
        attributes.add(rank);
        attributes.add(playersStreet);
        attributes.add(pot);
        attributes.add(chips);
        attributes.add(bets);
        attributes.add(action);
        Instances instances = new Instances(street + " Round " + round, attributes, 10000);
        instances.setClassIndex(attributes.size() - 1);
        List<ICRPlayer> players2 = ICRPlayer.find("selected = ?", true);
        for (int i = 0; i < players2.size(); i++) {
            ICRPlayer icrPlayer = players2.get(i);
            String name = icrPlayer.getString("name");
            Paginator<ICRGameDetail> paginator = new Paginator<>(ICRGameDetail.class, 100, "name = ?", name);
            int totalPages = (int) paginator.pageCount();
            for (int j = 1; j <= totalPages; j++) {
                List<ICRGameDetail> details = paginator.getPage(j);
                for (ICRGameDetail detail : details) {
                    ICRGame game = ICRGame.findFirst("gameId = ?", detail.get("gameId"));

                    printProgress("Game: " + game.getInteger("gameId") + " Street: " + street
                            + " Round: " + round + " Player: " + name, players2.size(), i);

                    String actionValue = ICRGame.getActionValue(game, streets.indexOf(street), round, name);
                    Integer rankStreet = detail.getInteger("rank" + street);
                    // only ad instances to the tree iff the player was present on the action. -:0
                    // means the player folded befor this street/round action
                    // System.out.println("actionvalue: " + actionValue + " rank: " + rankStreet);
                    if (!"-:0".equals(actionValue) && rankStreet != null) {
                        DenseInstance instance = new DenseInstance(attributes.size());
                        instance.setValue(playersLeft, detail.getInteger("playersLeft"));
                        instance.setValue(position, detail.getInteger("position"));
                        instance.setValue(rank, rankStreet);
                        instance.setValue(playersStreet, game.getInteger("players" + street));
                        instance.setValue(pot, game.getInteger("pot" + street));
                        instance.setValue(chips, detail.getInteger("chipsCount"));
                        instance.setValue(bets, detail.getInteger("chipsBet"));
                        instance.setValue(action, actionValue);
                        instances.add(instance);
                    }
                }
            }
        }

        return instances;
    }

    public static void printProgress(String title, int total, int currentElement) {
        char incomplete = '=';
        char complete = ' ';
        StringBuilder builder = new StringBuilder();
        int taskProcent = (int) currentElement * 100 / total;
        int progress = taskProcent * 50 / 100;
        for (int i = 0; i < 50; i++) {
            builder.append(i <= progress ? incomplete : complete);
        }
        System.out.print(
                title + " [" + builder + "] " + currentElement + "/" + total + " (" + taskProcent + ") Completed.\r");
    }

    /**
     * from the ICRGameDetail file, retrive the players basic statistic. (name,
     * hands played winCount ... etc) and build the ICRPlayer table to determinate,
     * the bes players
     */
    public static void updatePlayersSts() {
        // ICRPlayer.deleteAll();
        Paginator<ICRGameDetail> paginator = new Paginator<>(ICRGameDetail.class, 1000, "id > ?", -1);
        int totalPages = (int) paginator.pageCount();
        for (int i = 1; i <= totalPages; i++) {
            List<ICRGameDetail> list = paginator.getPage(i);
            for (ICRGameDetail detail : list) {
                String name = detail.getString("name");
                ICRPlayer player = ICRPlayer.findFirst("name = ?", name);
                if (player == null)
                    player = ICRPlayer.create("name", name, "hands", 0, "winCount", 0, "winRatio", 0, "chipsWins", 0,
                            "avROI", 0);

                int hands = player.getInteger("hands") + 1;
                player.setInteger("hands", hands);

                if (detail.getInteger("chipsWins") > 0) {
                    int winCount = player.getInteger("winCount") + 1;
                    player.setInteger("winCount", winCount);
                }
                double hands2 = player.getInteger("hands");
                double winCount = player.getInteger("winCount");
                double winRatio = winCount / hands2;
                player.setDouble("ratio", winRatio);

                int diff = detail.getInteger("chipsWins") - detail.getInteger("chipsBet");
                int chipsWins = player.getInteger("chipsWins");
                player.setInteger("chipsWins", chipsWins + diff);

                double hands3 = player.getInteger("hands");
                double chipsWins3 = player.getInteger("chipsWins");
                double avROI = chipsWins3 / hands3;
                player.setDouble("avROI", avROI);

                player.save();
            }
            printProgress("Updating Players", totalPages, i);
        }
    }
}
