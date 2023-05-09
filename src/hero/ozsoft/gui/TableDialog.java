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

package hero.ozsoft.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.alee.api.resource.*;
import com.alee.extended.layout.*;
import com.alee.laf.button.*;
import com.alee.utils.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.FormLayout;

import core.*;
import gui.*;
import hero.*;
import hero.UoAHandEval.*;
import hero.ozsoft.*;
import hero.ozsoft.actions.*;

/**
 * The game's main frame.
 * 
 * This is the core class of the Swing UI client application.
 * 
 * @author Oscar Stigter
 */
public class TableDialog extends JDialog implements Client {

	private Table table;

	/** The players at the table. */
	private final List<Player> players;

	/** The board panel. */
	private final BoardPanel boardPanel;

	/** The player panels. */
	private final Map<String, PlayerPanel> playerPanels;

	/** The current dealer's name. */
	private String dealerName;

	/** The current actor's name. */
	private String actorName;

	private Client proxyClient;
	private final TConsoleTextArea outConsole;
	private Player heroPlayer;

	/**
	 * Constructor.
	 */
	public TableDialog(Table table) {
		super(Alesia.getInstance().mainFrame);
		this.table = table;
		// cache all cards
		List<File> files = FileUtils.findFilesRecursively(TResources.USER_DIR+ Constants.PLAY_CARDS, f -> true);
		for (File file : files) {
			FileResource resource = new FileResource(file);
			Constants.CARDS_BUFFER.put(file.getName().substring(0, file.getName().length() - 4),
					ImageUtils.loadImageIcon(resource));
		}
		boardPanel = new BoardPanel();
		this.players = table.getPlayers();
		playerPanels = new HashMap<String, PlayerPanel>();

		// players positions in table
		Hashtable<Integer, PlayerPanel> chairs = new Hashtable<>();

		for (Player player : players) {
			PlayerPanel panel = new PlayerPanel();
			playerPanels.put(player.getName(), panel);
			chairs.put(player.getChair(), panel);
		}

		// the player "Hero" allays the test subject. this element is handled by this class but the
		// Decision is dispacht to proxiClient
		heroPlayer = players.stream().filter(p -> p.getName().equals("Hero")).findFirst().get();
		this.proxyClient = heroPlayer.getClient();
		heroPlayer.setClient(this);

		WindowAdapter wa = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				table.cancel(true);

			};
		};

		addWindowListener(wa);
		// ActionListener al = new ActionListener() {
		// public void actionPerformed(ActionEvent e) {
		// WebButton btn = (WebButton) e.getSource();
		//
		// }
		// };

		// control panel
		outConsole = new TConsoleTextArea();
		WebButton endGame = new WebButton(ap -> dispose());
		endGame.setIcon(TUIUtils.getSmallFontIcon('\ue047'));

		WebButton stepButton = new WebButton();
		stepButton.setIcon(TUIUtils.getSmallFontIcon('\ue0e4'));

		JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
		controlPanel.setOpaque(false);
		// controlPanel.add(table.getControlPanel(), BorderLayout.NORTH);
		controlPanel.add(TUIUtils.getSmartScroller(outConsole), BorderLayout.CENTER);

		JPanel jp = new JPanel(new VerticalFlowLayout());
		jp.setOpaque(false);
//		JToolBar tool = table.getControlPanel();
//		for (Component cmp : tool.getComponents()) {
//			JComponent jcmp = (JComponent) cmp;
//			jp.add(jcmp);
//		}

		// jp.add(TUIUtils.getStartPauseToggleButton(ap -> table.pause(!table.isPaused())));
		// jp.add(stepButton);
		controlPanel.add(jp, BorderLayout.EAST);

		// set ui components
		FormLayout layout = new FormLayout("center:90dlu, center:90dlu, center:90dlu, center:90dlu",
				"center:pref, 3dlu, center:pref, 3dlu, center:pref, 3dlu, center:pref, 3dlu, fill:80dlu");
		// PanelBuilder builder = new PanelBuilder(layout, new FormDebugPanel());
		PanelBuilder builder = new PanelBuilder(layout);

		// ArrayList<PlayerPanel> playerP = new ArrayList<>(playerPanels.values());
		builder.nextColumn();
		builder.add(chairs.get(3));
		builder.nextColumn();
		builder.add(chairs.get(4));
		builder.nextLine(2);

		builder.add(chairs.get(2));
		builder.nextColumn(2);
		builder.add(boardPanel, CC.rchw(3, 2, 3, 2));
		builder.nextColumn();
		builder.add(chairs.get(5));
		builder.nextLine(2);

		builder.add(chairs.get(1));
		builder.nextColumn(3);
		builder.add(chairs.get(6));
		builder.nextLine(2);

		builder.nextColumn();
		builder.add(chairs.get(0));
		builder.nextColumn();
		builder.add(chairs.get(7));

		builder.nextLine(2);
		builder.add(controlPanel, CC.rcw(9, 1, 4));

		JPanel contentPane = builder.build();
		contentPane.setOpaque(true);
		contentPane.setBackground(UIConstants.TABLE_COLOR);
		setContentPane(contentPane);

		// Show the frame.
		setResizable(false);
		pack();
		setLocation(660, 75);
		// setLocationRelativeTo(null);
		// setVisible(true);
	}

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		for (Player player : players) {
			PlayerPanel playerPanel = playerPanels.get(player.getName());
			if (playerPanel != null) {
				playerPanel.update(player);
			}
		}

		//
		proxyClient.joinedTable(type, bigBlind, players);
	}

	@Override
	public void messageReceived(String message) {
		String msg = message.startsWith("Hand: ") ? message : "    " + message;
		// boardPanel.waitForUserInput();

		// TODO: temporal. avoid log detail messages
		String[] dets = {"blind", "deals", "calls", "checks", "bets", "folds", "raises"};
		boolean log = true;
		for (String det : dets)
			log = message.contains(det) ? false : log;
		if (log)
			outConsole.append(msg + "\n");

		proxyClient.messageReceived(message);

		// wait
		try {
			Thread.sleep(table.getSpeed());
		} catch (Exception e) {
		}
	}

	@Override
	public void handStarted(Player dealer) {
		setDealer(false);
		dealerName = dealer.getName();
		setDealer(true);

		//
		proxyClient.handStarted(dealer);
	}

	@Override
	public void actorRotated(Player actor) {
		setActorInTurn(false);
		actorName = actor.getName();
		setActorInTurn(true);

		//
		proxyClient.actorRotated(actor);
	}

	@Override
	public void boardUpdated(UoAHand hand, int bet, int pot) {
		boardPanel.update(hand, bet, pot);

		//
		proxyClient.boardUpdated(hand, bet, pot);
	}

	@Override
	public void playerUpdated(Player player) {
		PlayerPanel playerPanel = playerPanels.get(player.getName());
		if (playerPanel != null) {
			playerPanel.update(player);
		}

		//
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
				// detail message for every action
				messageReceived(String.format("%s %s.", name, action.getVerb()));
				// boardPanel.setMessage(String.format("%s %s.", name, action.getVerb()));
				if (player.getClient() == this) {
					// boardPanel.waitForUserInput();
					proxyClient.playerActed(player);
				}
			}
		} else {
			throw new IllegalStateException(String.format("No PlayerPanel found for player '%s'", name));
		}
	}

	/*
	 * 
	 * @Override public void setVisible(boolean b) { // only show this dialo when the table speed value is >0. this
	 * allow doinbackground show the dialog if (table.getSpeed() > Table.RUN_BACKGROUND) super.setVisible(b); else {
	 * TTaskMonitor ttm = new TTaskMonitor(table, false); table.setInputBlocker(ttm); } }
	 * 
	 */

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		// boardPanel.setMessage("Please select an action:");
		// return controlPanel.getUserInput(minBet, humanPlayer.getCash(), allowedActions);

		//
		return proxyClient.act(minBet, currentBet, allowedActions);
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
}
