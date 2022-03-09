/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/
package plugins.hero;

import java.awt.*;

import com.alee.extended.layout.*;
import com.alee.laf.button.*;
import com.alee.laf.grouping.*;
import com.alee.laf.panel.*;
import com.alee.laf.tabbedpane.*;
import com.alee.managers.settings.*;

import core.*;
import core.datasource.model.*;
import gui.*;
import gui.console.*;

public class HeroPanel extends TUIFormPanel {

	private TrooperPanel trooperPanel;
	private SensorArrayPanel sensorArrayPanel;
	private WebPanel pockerSimulatorPanel;
	private WebTabbedPane wtp;

	public HeroPanel() {
		this.sensorArrayPanel = new SensorArrayPanel();
		TrooperParameter model = TrooperParameter.findFirst("trooper = ?", "Hero");
		this.trooperPanel = new TrooperPanel(model);

		WebPanel params = new WebPanel();
		params.setLayout(new VerticalFlowLayout(true, false));
		params.add(trooperPanel);
		this.pockerSimulatorPanel = new WebPanel(new BorderLayout());
		wtp = new WebTabbedPane();
		wtp.add(params, "Trooper parameters");
		wtp.add(sensorArrayPanel, "Sensor Array");
		wtp.add(pockerSimulatorPanel, "Pocker Simulator");
		wtp.add(new ConsolePanel(Hero.heroLogger), "Log console");

		wtp.registerSettings(new Configuration<TabbedPaneState>("HeroPanel.tabbedPanel"));
		WebToggleButton play = TUIUtils.getWebToggleButton(TActionsFactory.getAction("runTrooper"));
		WebToggleButton stop = TUIUtils.getWebToggleButton(TActionsFactory.getAction("stopTrooper"));
		stop.setSelected(true);
		WebToggleButton pause = TUIUtils.getWebToggleButton(TActionsFactory.getAction("pauseTrooper"));
		GroupPane pane = new GroupPane(play, stop, pause);
		getToolBar().add(pane);
		addToolBarActions("testTrooper");
		setBodyComponent(wtp);
	}

	/**
	 * shortcut that enable update all the UI components from (posible) new {@link SensorsArray} and/or
	 * {@link PokerSimulator}
	 * 
	 * @param sensorsArray - the array to update
	 */
	public void updateSensorsArray(SensorsArray sensorsArray) {
		setVisible(false);
		pockerSimulatorPanel.removeAll();
		pockerSimulatorPanel.add(sensorsArray.getPokerSimulator().getReportPanel(), BorderLayout.CENTER);
		sensorArrayPanel.updateArray(sensorsArray);
		setVisible(true);
	}
}
