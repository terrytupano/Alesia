// This file is part of the 'texasholdem' project, an open source
// Texas Hold'em poker application written in Java.
//
// Copyright 2009 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package plugins.hero.ozsoft.gui;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.alee.utils.*;

import core.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.*;
import plugins.hero.ozsoft.actions.*;

/**
 * The game's main frame.
 * 
 * This is the core class of the Swing UI client application.
 * 
 * @author Oscar Stigter
 */
public class TableDialog2 extends JDialog implements Client {

	/** Table type (betting structure). */
	private static final TableType TABLE_TYPE = TableType.NO_LIMIT;

	/** The players at the table. */
	private final List<Player> players;

	/** The GridBagConstraints. */
	private final GridBagConstraints gc;

	/** The board panel. */
	private final BoardPanel boardPanel;

	/** The control panel. */
	private final ControlPanel controlPanel;

	/** The player panels. */
	private final Map<String, PlayerPanel> playerPanels;

	/** The current dealer's name. */
	private String dealerName;

	/** The current actor's name. */
	private String actorName;

	private Client proxyClient;

	/** table Related images. */
	public static final Icon BUTTON_PRESENT_ICON = TResources.getIcon("dealer_button");
	public static final Icon BUTTON_ABSENT_ICON = TResources.getIcon("dealer_placeholder");
	public static final Icon CARD_PLACEHOLDER_ICON = TResources.getIcon("playCards/placeholder");
	public static final Icon CARD_BACK_ICON = TResources.getIcon("playCards/cardback");
	public static final Map<String, ImageIcon> cardsBuffer = new HashMap<String, ImageIcon>();
	public static final int CARD_WIDTH = CARD_BACK_ICON.getIconWidth();
	public static final int CARD_HEIGHT = CARD_BACK_ICON.getIconHeight();
	public static final Dimension CARD_DIMENSION = new Dimension(CARD_WIDTH, CARD_HEIGHT);

	/**
	 * Constructor.
	 */
	// public TableDialog(Map<String, Player> players) {
	public TableDialog2(Table game) {
		super(Alesia.getInstance().mainFrame);
		// cache all cards
		List<File> files = FileUtils.findFilesRecursively("plugins/hero/resources/playCards", f -> true);
		for (File file : files) {
			cardsBuffer.put(file.getName().substring(0, file.getName().length() - 4), ImageUtils.getImageIcon(file));
		}

		getContentPane().setBackground(UIConstants.TABLE_COLOR);
		setLayout(new GridBagLayout());

		gc = new GridBagConstraints();

		controlPanel = new ControlPanel(TABLE_TYPE);

		boardPanel = new BoardPanel();
		addComponent(boardPanel, 1, 1, 1, 1);

		this.players = game.getPlayers();
		playerPanels = new HashMap<String, PlayerPanel>();
		int i = 0;
		for (Player player : players) {
			PlayerPanel panel = new PlayerPanel();
			playerPanels.put(player.getName(), panel);
			switch (i++) {
				case 0 :
					// North position.
					addComponent(panel, 1, 0, 1, 1);
					break;
				case 1 :
					// East position.
					addComponent(panel, 2, 1, 1, 1);
					break;
				case 2 :
					// South position.
					addComponent(panel, 1, 2, 1, 1);
					break;
				case 3 :
					// West position.
					addComponent(panel, 0, 1, 1, 1);
					break;
				default :
					// Do nothing.
			}
		}

		// the player in the 3 position is allwais the test subject. this element is handled by this class but the
		// desition is dispacht to proxiClient
		Player test = players.get(2);
		this.proxyClient = test.getClient();
		test.setClient(this);
		// Show the frame.
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	}

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		for (Player player : players) {
			PlayerPanel playerPanel = playerPanels.get(player.getName());
			if (playerPanel != null) {
				playerPanel.update(player);
			}
		}
		proxyClient.joinedTable(type, bigBlind, players);
	}

	@Override
	public void messageReceived(String message) {
//		boardPanel.setMessage(message);
		// boardPanel.waitForUserInput();
		proxyClient.messageReceived(message);
	}

	@Override
	public void handStarted(Player dealer) {
		setDealer(false);
		dealerName = dealer.getName();
		setDealer(true);
		proxyClient.handStarted(dealer);
	}

	@Override
	public void actorRotated(Player actor) {
		setActorInTurn(false);
		actorName = actor.getName();
		setActorInTurn(true);
		proxyClient.actorRotated(actor);
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		boardPanel.update(hand, bet, pot);
		proxyClient.boardUpdated(hand, bet, pot);
	}

	@Override
	public void playerUpdated(Player player) {
		PlayerPanel playerPanel = playerPanels.get(player.getName());
		if (playerPanel != null) {
			playerPanel.update(player);
		}
		proxyClient.playerUpdated(player);
	}

	@Override
	public void playerActed(Player player) {
		String name = player.getName();
		PlayerPanel playerPanel = playerPanels.get(name);
		if (playerPanel != null) {
			playerPanel.update(player);
			PlayerAction action = player.getAction();
			if (action != null) {
//				boardPanel.setMessage(String.format("%s %s.", name, action.getVerb()));
				if (player.getClient() != this) {
					proxyClient.playerActed(player);
					// boardPanel.waitForUserInput();
				}
			}
		} else {
			throw new IllegalStateException(String.format("No PlayerPanel found for player '%s'", name));
		}
	}

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		// boardPanel.setMessage("Please select an action:");
		// return controlPanel.getUserInput(minBet, humanPlayer.getCash(), allowedActions);
		return proxyClient.act(minBet, currentBet, allowedActions);
	}

	/**
	 * Adds an UI component.
	 * 
	 * @param component The component.
	 * @param x The column.
	 * @param y The row.
	 * @param width The number of columns to span.
	 * @param height The number of rows to span.
	 */
	private void addComponent(Component component, int x, int y, int width, int height) {
		gc.gridx = x;
		gc.gridy = y;
		gc.gridwidth = width;
		gc.gridheight = height;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.NONE;
		gc.weightx = 0.0;
		gc.weighty = 0.0;
		getContentPane().add(component, gc);
	}

	/**
	 * Sets whether the actor is in turn.
	 * 
	 * @param isInTurn Whether the actor is in turn.
	 */
	private void setActorInTurn(boolean isInTurn) {
		if (actorName != null) {
			PlayerPanel playerPanel = playerPanels.get(actorName);
			if (playerPanel != null) {
				playerPanel.setInTurn(isInTurn);
			}
		}
	}

	/**
	 * Sets the dealer.
	 * 
	 * @param isDealer Whether the player is the dealer.
	 */
	private void setDealer(boolean isDealer) {
		if (dealerName != null) {
			PlayerPanel playerPanel = playerPanels.get(dealerName);
			if (playerPanel != null) {
				playerPanel.setDealer(isDealer);
			}
		}
	}

	@Override
	public void setTable(Table table) {
		// TODO Auto-generated method stub
		
	}

}
