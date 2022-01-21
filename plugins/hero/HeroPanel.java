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
import com.alee.laf.panel.*;
import com.alee.laf.tabbedpane.*;
import com.alee.managers.settings.*;

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
		SimulatorClient model = SimulatorClient.findFirst("playerName = ?", "Hero");
		this.trooperPanel = new TrooperPanel(model);
		TableParametersPanel tablePanel = new TableParametersPanel();
		WebPanel params = new WebPanel();
		params.setLayout(new VerticalFlowLayout());
		params.add(trooperPanel);
		params.add(tablePanel);
		this.pockerSimulatorPanel = new WebPanel(new BorderLayout());
		// pockerSimulatorPanel.setMessage("hero.msg01");
		wtp = new WebTabbedPane();
		wtp.add(params, "Trooper parameters");
		wtp.add(sensorArrayPanel, "Sensor Array");
		wtp.add(pockerSimulatorPanel, "Pocker Simulator");
		wtp.add(new ConsolePanel(Hero.heroLogger), "Log console");
		// wtp.add(TCVUtils.createImagesPanel(Hero.preparedCards), "Cards");

		wtp.registerSettings(new Configuration<TabbedPaneState>("HeroPanel.tabbedPanel"));
		addToolBarActions("runTrooper", "testTrooper", "stopTrooper", "pauseTrooper");
		setBodyComponent(wtp);		
	}

	public TrooperPanel getTrooperPanel() {
		return trooperPanel;
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
