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

package hero;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.jdesktop.application.*;
import org.jdesktop.application.Task.*;

import com.alee.extended.layout.*;
import com.alee.laf.button.*;
import com.alee.laf.checkbox.*;
import com.alee.laf.combobox.*;
import com.alee.laf.grouping.*;
import com.alee.managers.settings.*;
import com.alee.utils.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.FormLayout;

import core.*;
import gui.*;
import hero.UoAHandEval.*;
import hero.ozsoft.*;
import hero.ozsoft.actions.*;
import hero.ozsoft.gui.*;

/**
 * The game's main frame.
 * 
 * This is the core class of the Swing UI client application.
 * 
 * @author Oscar Stigter
 */
public class TableDialog extends InputBlocker implements Client {

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

	/** list of messages to remove if {@link #detailLog} is false */
	private List<String> removeList = Arrays.asList("blind", "deals", "calls", "checks", "bets", "folds", "raises");

	private Client proxyClient;
	private final TConsoleTextArea outConsole;
	private Player heroPlayer;
	private JDialog dialog;
	private JPanel contentPane;
	private BusyPanel busyPanel;
	private Component oldGlassPanel;
	private long speed;
	private boolean detailLog;

	/**
	 * Constructor.
	 */
	public TableDialog(Table table) {
		super(table, Task.BlockingScope.WINDOW, Alesia.getMainPanel());
		TActionsFactory.insertActions(this);
		this.busyPanel = new BusyPanel();
		this.table = table;
		this.boardPanel = new BoardPanel();
		this.players = table.getPlayers();
		playerPanels = new HashMap<String, PlayerPanel>();

		// players positions in table
		Hashtable<Integer, PlayerPanel> chairs = new Hashtable<>();

		for (Player player : players) {
			PlayerPanel panel = new PlayerPanel();
			playerPanels.put(player.getName(), panel);
			chairs.put(player.getChair(), panel);
		}

		// the player "Hero" allays the test subject. this element is handled by this
		// class but the
		// Decision is dispatched to proxiClient
		heroPlayer = players.stream().filter(p -> p.getName().equals("Hero")).findFirst().get();
		this.proxyClient = heroPlayer.getClient();
		heroPlayer.setClient(this);

		// control panel
		outConsole = new TConsoleTextArea();
		// WebButton endGame = new WebButton(ap -> dispose());
		// endGame.setIcon(TUIUtils.getToolBarFontIcon('\ue047'));

		List<WebToggleButton> buttons = TUIUtils.getToggleButtons("pauseInteractiveSimulation",
				"pauseInteractiveSimulationOnHero", "pauseInteractiveSimulationOnPlayer",
				"resumeInteractiveSimulation");
		GroupPane groupPane = new GroupPane(2, 2, buttons.toArray(new WebToggleButton[0]));

		JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
		controlPanel.setOpaque(false);
		controlPanel.add(TUIUtils.getSmartScroller(outConsole), BorderLayout.CENTER);

		WebComboBox comboBox = TUIUtils.getComboBox("speed", "simulation.speed");
		comboBox.addActionListener(e -> this.speed = Long.valueOf(((TSEntry) comboBox.getSelectedItem()).getKey()));
		comboBox.registerSettings(new Configuration<ComboBoxState>("simulation.speed"));

		WebCheckBox checkBox = TUIUtils.getCheckBox("Detail log", false);
		checkBox.addActionListener(e -> this.detailLog = checkBox.isSelected());
		checkBox.registerSettings(new Configuration<ButtonState>("simulation.Log"));

		JPanel jp = new JPanel(new VerticalFlowLayout());
		jp.setOpaque(false);
		jp.add(comboBox);
		jp.add(groupPane);
		jp.add(checkBox);
		controlPanel.add(jp, BorderLayout.EAST);

		// set the table components
		FormLayout layout = new FormLayout("center:90dlu, center:90dlu, center:90dlu, center:90dlu, center:90dlu",
				"center:pref, 3dlu, center:pref, 3dlu, center:pref, 3dlu, center:pref, 3dlu, fill:80dlu");
		// PanelBuilder builder = new PanelBuilder(layout, new FormDebugPanel());
		PanelBuilder builder = new PanelBuilder(layout);

		// ArrayList<PlayerPanel> playerP = new ArrayList<>(playerPanels.values());
		builder.nextColumn();
		builder.add(chairs.get(4));
		builder.nextColumn();
		builder.add(chairs.get(5));
		builder.nextColumn();
		builder.add(chairs.get(6));
		builder.nextLine(2);

		builder.add(chairs.get(3));
		builder.nextColumn(2);
		builder.add(boardPanel, CC.rchw(3, 2, 3, 3));
		builder.nextColumn(2);
		builder.add(chairs.get(7));
		builder.nextLine(2);

		builder.add(chairs.get(2));
		builder.nextColumn(4);
		builder.add(chairs.get(8));
		builder.nextLine(2);

		builder.nextColumn();
		builder.add(chairs.get(1));
		builder.nextColumn();
		builder.add(new JLabel());
		builder.nextColumn();
		builder.add(chairs.get(9));
		builder.nextLine(2);
		builder.add(controlPanel, CC.rcw(9, 1, 5));

		contentPane = builder.build();
		contentPane.setOpaque(true);
		contentPane.setBackground(UIConstants.TABLE_COLOR);
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

		if (detailLog) {
			outConsole.append(msg + "\n");
		} else {
			Optional<String> optional = removeList.stream().filter(i -> message.contains(i)).findFirst();
			if (!optional.isPresent())
				outConsole.append(msg + "\n");
		}

		proxyClient.messageReceived(message);

		// wait
		ThreadUtils.sleepSafely(speed);
	}

	@Override
	public void handStarted(Player dealer) {
		setDealer(false);
		dealerName = dealer.getName();
		setDealer(true);

		//
		proxyClient.handStarted(dealer);
	}

	public Client getProxyClient() {
		return proxyClient;
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

	/**
	 * only show this dialog, when the table speed value is >0. this
	 * 
	 * @Override public void setVisible(boolean b) { if (table.getSpeed() > 0)
	 *           super.setVisible(b); else { TTaskMonitor ttm = new
	 *           TTaskMonitor(table); table.setInputBlocker(ttm); } }
	 */

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		// boardPanel.setMessage("Please select an action:");
		// return controlPanel.getUserInput(minBet, humanPlayer.getCash(),
		// allowedActions);

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

	@Override
	protected void block() {
		oldGlassPanel = Alesia.getMainFrame().getGlassPane();
		Alesia.getMainFrame().setGlassPane(busyPanel);
		this.dialog = TUIUtils.getDialog("Table visualizer", contentPane);
		// dialog.setMinimumSize(new Dimension(650, 580));

		// cancel the associated table on close
		WindowAdapter wa = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				unblock();
			};
		};
		dialog.addWindowListener(wa);

		busyPanel.setVisible(true);
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);

		// // Show the frame.
		// setResizable(false);
		// pack();
		// setLocation(660, 75);
		// // setLocationRelativeTo(null);
		// // setVisible(true);

	}

	@Override
	protected void unblock() {
		dialog.dispose();
		table.cancel(true);
		busyPanel.setVisible(false);
		Alesia.getMainFrame().setGlassPane(oldGlassPanel);
	}

	@org.jdesktop.application.Action
	public void pauseInteractiveSimulationOnHero() {
		table.pause(Table.PAUSE_HERO);
	}

	@org.jdesktop.application.Action
	public void pauseInteractiveSimulationOnPlayer() {
		table.pause(Table.PAUSE_PLAYER);
	}

	@org.jdesktop.application.Action
	public void pauseInteractiveSimulation() {
		table.pause(Table.PAUSE_TASK);
	}

	@org.jdesktop.application.Action
	public void resumeInteractiveSimulation() {
		table.pause(Table.RESUME_ALL);
	}

}
