package burlap.tictactoe;

import burlap.mdp.core.*;
import burlap.mdp.core.state.*;
import burlap.mdp.singleagent.model.*;

public class TicTacToe {

	/**
	 * direction matrix is used to increase/decrease {x,y} coordenates
	 */
	private int[][] directionMatrix = {{-1, 1}, {0, 1}, {1, 1}, {-1, 0}, {1, 0}, // {0,0 direcction is not valid}
			{-1, -1}, {0, -1}, {1, -1}};

	private static boolean checkPlayerWon() {
	    for (let square of squares) {
	        // only process taken elements by the current player
	        if (!(square.classList.contains("taken") && square.classList.contains(currentPlayer)))
	            continue;

	        for (let coord of directionMatrix) {
	            var numMatches = 1; // the current position count
	            var row = Number.parseInt(square.getAttribute("row"));
	            var col = Number.parseInt(square.getAttribute("column"));
	            var nextSquare = null;
	            for (var i = 0; i < WIN_IN_A_ROW; i++) {
	                col += coord[0];
	                row += coord[1];
	                nextSquare = document.getElementById(row + "-" + col);
	                if (nextSquare != null && nextSquare.classList.contains("taken") && nextSquare.classList.contains(currentPlayer)) {
	                    numMatches += 1;
	                }
	            }
	            // If our count reaches 4, the player has won the game
	            if (numMatches == WIN_IN_A_ROW) {
	                return true;
	            }
	        }
	    }
	    // If we reach this statement: they have NOT won the game
	    return false;
	}

	public TerminalFunction geTerminalFunction() {
		TerminalFunction tf = new TerminalFunction() {

			@Override
			public boolean isTerminal(State s) {
				// TODO Auto-generated method stub
				return false;
			}
		};
	}

	public RewardFunction getRewardFunction() {

	}
}
