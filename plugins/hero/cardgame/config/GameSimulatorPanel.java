/*
 * Copyright (c) Chris 'MD87' Smith, 2007-2008. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.cardgame.config;

import java.util.*;

import javax.swing.table.*;

import com.alee.laf.scroll.*;
import com.alee.laf.table.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.*;
import plugins.hero.*;
import plugins.hero.cardgame.games.*;
import plugins.hero.cardgame.interfaces.*;
import plugins.hero.cardgame.ui.*;

public class GameSimulatorPanel extends TUIFormPanel {

	private WebTable playersTable;
	public GameSimulatorPanel() {
		playersTable = createPlayersTable();

		addInputComponent(TUIUtils.getTWebComboBox("table.parameters", "table.parameters0"));
		addInputComponent(TUIUtils.getNumericTextField("play.time", null, 5, null), false, true);
		addInputComponent(TUIUtils.getNumericTextField("play.maxRaise", null, 4, null), false, true);

		FormLayout layout = new FormLayout("pref, 3dlu, pref, 3dlu, pref:grow",
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, fill:pref:grow");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DLU2);

		builder.append(TUIUtils.getTitleLabel("Table Parameters", "table parameters configuration"), 5);
		builder.nextLine(2);
		builder.append(getLabel("table.parameters"), getInputComponent("table.parameters"));
		builder.nextLine(2);

		builder.append(getLabel("play.maxRaise"), getInputComponent("play.maxRaise"));
		builder.nextLine(2);
		builder.append(getLabel("play.time"), getInputComponent("play.time"));
		builder.nextLine(2);
		builder.append(TUIUtils.getTitleLabel("Players", "Select the number and type of players"), 5);
		builder.nextLine(2);
		builder.append(new WebScrollPane(playersTable), 5);

		setBodyComponent(builder.getPanel());
		setFooterActions("startSimulation");
		registreSettings();
	}

	@Override
	public Hashtable<String, Object> getValues() {
		Hashtable vals = super.getValues();
		Hero.parseTableParameters(vals);
		return vals;
	}

	/**
	 * start a game simulation with the store parameters
	 * 
	 */
	public void startGame() {
		try {
			Hashtable<String, Object> values = getValues();
			TableModel model = playersTable.getModel();
			int num = 0;
			for (int i = 0; i < model.getRowCount(); i++) {
				if ((Boolean) model.getValueAt(i, 2))
					num++;
			}
			int buy = ((Double) values.get("table.buyIn")).intValue();
			int bb = ((Double) values.get("table.bigBlid")).intValue();
			int maxR = ((Long) values.get("play.maxRaise")).intValue();
			TexasHoldEm game = new TexasHoldEm(num, buy, bb, maxR);

			for (int i = 0; i < model.getRowCount(); i++) {
				if ((Boolean) model.getValueAt(i, 2)) {
					String name = model.getValueAt(i, 0).toString();
					String control = model.getValueAt(i, 1).toString();
					Class cls = Class.forName("plugins.hero.cardgame.controllers." + control);
					PlayerController ctrl = (PlayerController) cls.newInstance();
					ctrl.setGame(game);
					game.addPlayer(name, 1000, ctrl);
				}
			}

			GameWindow window = new GameWindow(game);
			window.setVisible(true);
			game.startTournament();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private WebTable createPlayersTable() {
		String[] columnNames = {"Name", "Style", "Active"};
		Object[][] data = {{"Kathy", "ConservativeOpener", true}, {"John", "EndGameAI", true},
				{"Sue", "RandomPlayer", true}, {"Rat Ass", "RatAss", true}};

		WebTable table = new WebTable(data, columnNames);

		// Custom column
		// TableColumn column = table.getColumnModel ().getColumn ( 1 );
		//
		// // Custom renderer
		// WebTableCellRenderer renderer = new WebTableCellRenderer ();
		// renderer.setToolTipText ( "Click for combo box" );
		// column.setCellRenderer ( renderer );
		//
		// // Custom editor
		// JComboBox comboBox = new JComboBox ();
		// comboBox.addItem ( "Snowboarding" );
		// comboBox.addItem ( "Rowing" );
		// comboBox.addItem ( "Knitting" );
		// comboBox.addItem ( "Speed reading" );
		// comboBox.addItem ( "Pool" );
		// comboBox.addItem ( "None of the above" );
		// column.setCellEditor ( new WebDefaultCellEditor ( comboBox ) );
		return table;
	}
}
