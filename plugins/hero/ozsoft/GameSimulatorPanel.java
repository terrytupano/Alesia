/*
 * Copyright (c) Chris 'MD87' Smith, 2007-2008. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.ozsoft;

import java.util.*;

import com.alee.laf.scroll.*;
import com.alee.laf.table.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.*;
import plugins.hero.*;

public class GameSimulatorPanel extends TUIFormPanel {

	private WebTable playersTable;
	private TrooperPanel trooperPanel;

	public GameSimulatorPanel() {
		trooperPanel = new TrooperPanel(false);
		playersTable = createPlayersTable();

		FormLayout layout = new FormLayout("pref, 3dlu, pref, 3dlu, pref:grow",
				"p, 3dlu, p, 3dlu, p, 3dlu, fill:pref:grow");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DLU2);

		builder.append(TUIUtils.getTitleLabel("Trooper panel", "trooper configuration parameters"), 5);
		builder.nextLine(2);
		builder.append(trooperPanel, 5);
		builder.nextLine(2);
		builder.append(TUIUtils.getTitleLabel("Players", "Select the number and type of players"), 5);
		builder.nextLine(2);
		builder.append(new WebScrollPane(playersTable), 5);

		setBodyComponent(builder.getPanel());
		setFooterActions("startSimulation");
		registreSettings();
	}
	
	public WebTable getPlayersTable() {
		return playersTable;
	}

	public TrooperPanel getTrooperPanel() {
		return trooperPanel;
	}
	
	private WebTable createPlayersTable() {

		String[] columnNames = {"Name", "Style", "Active"};
		Object[][] data = {{"Kathy", "BasicBot", true}, {"John", "BasicBot", true}, {"Hero", "HeroBot", true},
				{"Rick", "BasicBot", true}};

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
