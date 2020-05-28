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

import com.alee.laf.panel.*;
import com.alee.laf.tabbedpane.*;
import com.alee.managers.settings.*;

public class HeroPanel extends WebPanel {

	private TrooperPanel trooperPanel;
	private SensorArrayPanel sensorArrayPanel;
	private WebPanel pockerSimulatorPanel;
	private WebTabbedPane wtp;

	public HeroPanel() {
		super(new BorderLayout());
		this.sensorArrayPanel = new SensorArrayPanel();
		this.trooperPanel = new TrooperPanel();
		this.pockerSimulatorPanel = new WebPanel(new BorderLayout());
		// pockerSimulatorPanel.setMessage("hero.msg01");
		wtp = new WebTabbedPane();
		wtp.add(trooperPanel, "Trooper parameters");
		wtp.add(sensorArrayPanel, "Sensor Array");
		wtp.add(pockerSimulatorPanel, "Pocker Simulator");
		wtp.add(Hero.consolePanel, "Log console");
//		wtp.add(TCVUtils.createImagesPanel(Hero.preparedCards), "Cards");

		wtp.registerSettings(new Configuration<TabbedPaneState>("HeroPanel.tabbedPanel"));

		add(wtp, BorderLayout.CENTER);
	}

	public TrooperPanel getTrooperPanel() {
		return trooperPanel;
	}

	public void updateGlovalParameters() {
		setVisible(false);
		pockerSimulatorPanel.removeAll();
		pockerSimulatorPanel.add(Hero.sensorsArray.getPokerSimulator().getReportPanel(), BorderLayout.CENTER);
		sensorArrayPanel.updateArray();
		setVisible(true);
	}
}
