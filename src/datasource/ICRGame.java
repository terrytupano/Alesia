package datasource;

import java.util.*;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
@Table("icr_holdem2_game")
public class ICRGame extends Model {

    private static int bigBlind = 20;
    private static int smallBlind = 10;


    /**
     * return the matrix representation of the fields *actions (determined by street argument) from the *player table.
     * 
     * @param game - the game
     * @param street - the street to retrive the acction form
     * @return the matrix form the actions field
     */
    private static String[][] getActionsMatrix(ICRGame game, int street) {
        List<ICRPlayer> players = ICRPlayer.find("gameId = ?", game.getInteger("gameId"));
        String[][] matrix2 = new String[players.size()][10];
        for (int i = 0; i < players.size(); i++) {
            ICRPlayer player = players.get(i);
            String action = "";
            action = street == 0 ? player.getString("preFlopActions") : action;
            action = street == 1 ? player.getString("flopActions") : action;
            action = street == 2 ? player.getString("turnActions") : action;
            action = street == 3 ? player.getString("riverActions") : action;

            for (int j = 0; j < 10; j++) {
                String action2 = action.length() <= j ? "-" : action.substring(j, j + 1);
                matrix2[i][j] = action2;
            }
        }

        return matrix2;
    }

    /**
     * translate the player.s actions from db tabe actions to the actual estimated value of the action.
     * 
     * @param game - the game to translate
     */
    public static void checkGame(ICRGame game) {
        int prevPot = 0;
        for (int street = 0; street < 4; street++) {
            String[][] actions = getActionsMatrix(game, street);
            printMatrix(actions);
            int pot = 0;
            pot = street == 0 ? game.getInteger("potPreFlop") : pot;
            pot = street == 1 ? game.getInteger("potFlop") : pot;
            pot = street == 2 ? game.getInteger("potTurn") : pot;
            pot = street == 3 ? game.getInteger("potRiver") : pot;
            int dif = pot - prevPot;
            int[][] matrixValues = getValueMatrix(actions, dif);
            System.out.println("Target pot: " + pot);
            printMatrix(matrixValues);
            prevPot = pot;
        }
    }

    public static void printMatrix(int[][] matrix) {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                System.out.printf("%6d", matrix[row][col]);
            }
            System.out.println();
        }
    }

    public static void printMatrix(String[][] matrix) {
        for (int row = 0; row < matrix.length; row++) {
            for (int col = 0; col < matrix[row].length; col++) {
                System.out.printf("%2s", matrix[row][col]);
            }
            System.out.println();
        }
    }

    /**
     * compute and return the value sequence correspondint to the action matrix passes as argument.
     * 
     * @param matrix - the actions matrix @see {@link #getActionsMatrix(ICRGame, int)}
     * @param target - the pot value who mss be computed thro this actions. the sume of the acctions sequence muss be similar to this value)
     * @return the matrix
     */
    private static int[][] getValueMatrix(String[][] matrix, int target) {
        int pot = 0;
        int call = 0;
        int[][] matrixValues = new int[matrix.length][matrix[0].length];
        while (pot < target) {
            for (int i = 0; i < matrix[0].length; i++) {
                for (int j = 0; j < matrix.length; j++) {
                    String action = matrix[j][i];
                    if ("B".equals(action) && j == 0) {
                        pot += smallBlind;
                        call = smallBlind;
                        matrixValues[j][i] += smallBlind;
                    }
                    if ("B".equals(action) && j == 1) {
                        pot += bigBlind;
                        matrixValues[j][i] += bigBlind;
                        call = smallBlind;
                    }
                    if ("c".equals(action)) {
                        pot += call;
                        matrixValues[j][i] += call;
                        call = 0;
                    }
                    if ("r".equals(action)) {
                        pot += call + bigBlind;
                        matrixValues[j][i] += call + bigBlind;
                        call = bigBlind;
                    }
                    if ("b".equals(action)) {
                        pot += bigBlind;
                        matrixValues[j][i] += bigBlind;
                        call = bigBlind;
                    }
                    if ("A".equals(action)) {
                        System.out.println("ICRReader.getPot()");
                    }
                }
            }
        }
        return matrixValues;
    }
}
