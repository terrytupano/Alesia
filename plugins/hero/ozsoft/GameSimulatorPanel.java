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
	public WebTable getPlayersTable() {
	 return playersTable;
 }

	@Override
	public Hashtable<String, Object> getValues() {
		Hashtable vals = super.getValues();
		Hero.parseTableParameters(vals);
		return vals;
	}

	private WebTable createPlayersTable() {
		
		
		String[] columnNames = {"Name", "Style", "Active"};
		Object[][] data = {{"Kathy", "BasicBot", true}, {"John", "BasicBot", true},
				{"Hero", "BasicBot", true}, {"Rick", "BasicBot", true}};

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
