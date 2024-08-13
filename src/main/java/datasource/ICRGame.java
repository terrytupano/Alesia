package datasource;

import java.util.*;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

import hero.*;

@DbName("hero")
@Table("icr_holdem2_game")
public class ICRGame extends Model {

    private static final int BIG_BLIND = 20;
    private static final int SMALL_BLIND = 10;
    public static final int ACTIONS_MATRIX_DEEP = 50;

    /**
     * return the matrix representation of the fields actions* (determined by street
     * argument) from the gameDetails table.
     * 
     * @param details - the players on the current game
     * @param street  - the street to retrive the acction form
     * @return the matrix form the actions field
     */
    private static String[][] getActionsMatrix(List<ICRGameDetail> details, int street) {
        String[][] matrix2 = new String[details.size()][ACTIONS_MATRIX_DEEP];
        for (int i = 0; i < details.size(); i++) {
            ICRGameDetail detail = details.get(i);
            String action = detail.getString("actions" + TWekaUtils.streets.get(street));
            for (int j = 0; j < ACTIONS_MATRIX_DEEP; j++) {
                String action2 = action.length() <= j ? "-" : action.substring(j, j + 1);
                matrix2[i][j] = action2;
            }
        }

        return matrix2;
    }

    /**
     * return the action:value for the current game, street round and player name.
     * 
     * <p>
     * this method also will return "-:0" (no action) if in the action matrix the
     * All-in action is found. in holdem, all-in mean the rest of the chips, (the
     * player.s chips < current bet) assign the current call check if the action A
     * is present. if so, the complete game is ignored due the fack, that others
     * values like pot or player in game file are not recorded
     * 
     * 
     * @param game   - the game
     * @param street - the street
     * @param round  - the round
     * @param name   - player name
     * @return pair action:value of action as string
     */
    public static String getActionValue(ICRGame game, int street, int round, String name) {
        try {
            int prevPot = 0;
            String[] vector = null;
            List<ICRGameDetail> details = ICRGameDetail.find("gameId = ?", game.getInteger("gameId"));
            // System.out.println("GameId: " + game.getInteger("gameId"));
            // System.out.println("name: " + name);
            // System.out.println("street: " + TWekaUtils.streets.get(street));
            // System.out.println("round: " + round);
            // if (name.equals("mtew-boynk") && game.getInteger("gameId").equals(897360895))
            // {
            if (game.getInteger("gameId").equals(813256593) & round == 2) {
                // System.out.println("ICRGame.getActionValue()");
            }
            for (int street2 = 0; street2 < 4; street2++) {
                String[][] actions = getActionsMatrix(details, street2);
                int pot = game.getInteger("pot" + TWekaUtils.streets.get(street2));
                int target = pot - prevPot;
                int[][] values = getValueMatrix(actions, target);
                if (values == null)
                    return "-:0";
                prevPot = pot;
                if (street == street2) {
                    String[][] join = joinMatrix(actions, values);
                    // printMatrix(join);
                    int i = -1;
                    for (int i2 = 0; i2 < details.size(); i2++) {
                        // ignorecase: kibble - Kibble are the same player ?!?!?! i.dont know. i.m
                        // assuming are the same O.o
                        i = name.equalsIgnoreCase(details.get(i2).getString("name")) ? i2 : i;
                    }

                    vector = new String[join.length];
                    for (int j = 0; j < join.length; j++) {
                        vector[j] = join[i][j];
                    }

                    String act = "-:0";
                    if (vector.length > round)
                        act = vector[round];
                    return act;
                }
            }
        } catch (Exception e) {
            // this try is only to print a nl caracter befor the stacktrace. so i can see the progressbar
            System.out.println();
            e.printStackTrace();
        }
        return null;
    }

    private static String[][] joinMatrix(String[][] actions, int[][] values) {
        String[][] join = new String[actions.length][actions[0].length];
        for (int row = 0; row < actions.length; row++) {
            for (int col = 0; col < actions[row].length; col++) {
                join[row][col] = actions[row][col] + ":" + values[row][col];
            }
        }
        return join;
    }

    private static void printMatrix(int[][] matrix) {
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
                System.out.printf("%-6s", matrix[row][col]);
            }
            System.out.println();
        }
    }

    /**
     * compute and return the value sequence correspondint to the action matrix
     * passes as argument. this method will return null if the action A (all-in) is
     * found inside the action matrix see
     * {@link #getActionValue(ICRGame, int, int, String)}
     * 
     * @param actionMatrix - the actions matrix see
     *                     {@link #getActionsMatrix(List, int)}
     * @param target       - the pot value who muss be computed thro this actions.
     *                     the sume of the acctions sequence muss be similar to this
     *                     value)
     * @return the matrix
     */
    public static int[][] getValueMatrix(String[][] actionMatrix, int target) {
        int pot = 0;
        int call = 0;
        int[][] matrixValues = new int[actionMatrix.length][actionMatrix[0].length];
        // continue guesing until the aproximation is > 50%
        double procent = 0;
        while (procent <= 50) {
            for (int i = 0; i < actionMatrix[0].length; i++) {
                for (int j = 0; j < actionMatrix.length; j++) {
                    String action = actionMatrix[j][i];
                    if ("B".equals(action) && j == 0) {
                        pot += SMALL_BLIND;
                        call = SMALL_BLIND;
                        matrixValues[j][i] += SMALL_BLIND;
                    }
                    if ("B".equals(action) && j == 1) {
                        pot += BIG_BLIND;
                        matrixValues[j][i] += BIG_BLIND;
                        call = SMALL_BLIND;
                    }
                    if ("c".equals(action)) {
                        pot += call;
                        matrixValues[j][i] += call;
                        call = 0;
                    }
                    if ("r".equals(action)) {
                        pot += call + BIG_BLIND;
                        matrixValues[j][i] += call + BIG_BLIND;
                        call = BIG_BLIND;
                    }
                    if ("b".equals(action)) {
                        pot += BIG_BLIND;
                        matrixValues[j][i] += BIG_BLIND;
                        call = BIG_BLIND;
                    }
                    if ("A".equals(action)) {
                        return null;
                        // throw new IllegalArgumentException("All-in action found in action matrix.");
                    }
                }
            }
            procent = pot * 100d / target;
        }
        return matrixValues;
    }
}
