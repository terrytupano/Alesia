package hero;

import java.util.*;

import org.javalite.activejdbc.*;

import core.*;
import datasource.*;
import hero.ozsoft.*;
import weka.core.*;
import weka.core.converters.ConverterUtils.*;
import weka.core.neighboursearch.*;

public class TWekaUtils {

    public static void buildKdTreeInstances() {
        try {
            String[] streets = new String[] { "flopRank", "turnRank", "riverRank"};
            for (String street : streets) {
                Instances instances = TWekaUtils.buildKdTreeInstances(street);
                // save to file
                DataSink dataSink = new DataSink(street + "_instances.arff");
                dataSink.write(instances);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
                // KDTree kdTree = null;

                // kdTree = new KDTree();
                // kdTree.setInstances(instances);
    }

    /**
     * build the KDTree for the specific street argument.
     * - the instances only games with playerLeft <= Table.CAPACITY
     * - the label for this tree is the id value
     * 
     * 
     * @param street - the rank db filed: flopRank, turnRank or riverRank
     * @return the KDTree
     */
    private static Instances buildKdTreeInstances(String street) {
        Attribute playersLeft = new Attribute("playersLeft", 0);
        Attribute position = new Attribute("position", 1);
        Attribute rank = new Attribute(street, 2);
        Attribute id = new Attribute("id", 3);

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(playersLeft);
        attributes.add(position);
        attributes.add(rank);
        attributes.add(id);
        Instances instances = new Instances(street + " Instances list", attributes, 10000);
        instances.setClass(id);
        List<ICRPlayer> players = ICRPlayer.find("selected = ?", true);
        for (int i = 0; i < players.size(); i++) {
            ICRPlayer icrPlayer = players.get(i);
            Paginator<ICRGameDetail> paginator = new Paginator<>(ICRGameDetail.class, 100, "name = ?",
                    icrPlayer.get("name"));
            int totalPages = (int) paginator.pageCount();
            for (int j = 1; j <= totalPages; j++) {
                List<ICRGameDetail> details = paginator.getPage(j);
                for (ICRGameDetail detail : details) {
                    Integer pl = detail.getInteger("playersLeft");
                    // only games with players <= Table.CAPACITY
                    if (pl <= Table.CAPACITY) {
                        DenseInstance instance = new DenseInstance(attributes.size());
                        instance.setValue(playersLeft, pl);
                        instance.setValue(position, detail.getInteger("position"));
                        Integer rank2 = detail.getInteger(street);
                        instance.setValue(rank, rank2 == null ? -1 : rank2);
                        instance.setValue(id, detail.getInteger("id"));
                        instances.add(instance);
                    }
                }
            }
            printProgress("Building kdTree for " + icrPlayer.get("name"), players.size(), i);
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
     * hands played winCount ... etc)
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
