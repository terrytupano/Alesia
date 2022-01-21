/*
 * Copyright (c) Chris 'MD87' Smith, 2007-2008. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.ozsoft;

import java.awt.*;
import java.beans.*;

import com.alee.laf.panel.*;
import com.alee.laf.tabbedpane.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.model.*;
import gui.*;
import plugins.hero.*;
import plugins.hero.utils.*;

public class GameSimulatorPanel extends TUIFormPanel implements PropertyChangeListener {

	private SimulatorClientList clientList;
	private TrooperPanel trooperPanel;
	private WebPanel pockerSimulatorPanel;

	public GameSimulatorPanel() {
		trooperPanel = new TrooperPanel(new SimulatorClient());
		clientList = new SimulatorClientList();

		clientList.getWebTable().addPropertyChangeListener(this);

		this.pockerSimulatorPanel = new WebPanel(new BorderLayout());

		// trooper panel + list of Clients
		FormLayout layout = new FormLayout("pref, 3dlu, pref, 3dlu, pref:grow",
				"p, 3dlu, p, 3dlu, p, 3dlu, fill:10dlu:grow");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DLU2);
		builder.append(TUIUtils.getTitleLabel("Trooper panel", "trooper configuration parameters"), 5);
		builder.nextLine(2);
		builder.append(trooperPanel, 5);
		builder.nextLine(2);
		builder.append(TUIUtils.getTitleLabel("Players", "Select the number and type of players"), 5);
		builder.nextLine(2);
		builder.append(clientList, 5);

		WebTabbedPane wtp = new WebTabbedPane();
		wtp.add(builder.getPanel(), "Simulation parameters");
		wtp.add(pockerSimulatorPanel, "PockerSimulator");

		setBodyComponent(wtp);
		setFooterActions("backrollHistory", "startSimulation");
		registreSettings();
	}

	public TrooperPanel getTrooperPanel() {
		// TODO: implement a property cahge listener to recive all changes from input components. and remove this shitty
		// method and related
		return trooperPanel;
	}

	public void updatePokerSimulator(PokerSimulator simulator) {
		setVisible(false);
		pockerSimulatorPanel.removeAll();
		pockerSimulatorPanel.add(simulator.getReportPanel(), BorderLayout.CENTER);
		setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (TUIListPanel.MODEL_SELECTED.equals(evt.getPropertyName())) {

		}
	}
}
