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

import java.awt.BorderLayout;

import com.alee.extended.layout.VerticalFlowLayout;
import com.alee.laf.button.WebToggleButton;
import com.alee.laf.grouping.GroupPane;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.tabbedpane.TabbedPaneState;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.managers.settings.Configuration;

import core.TActionsFactory;
import core.TUIUtils;
import core.datasource.model.TrooperParameter;
import gui.TUIFormPanel;
import gui.console.ConsolePanel;

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
		WebToggleButton test = TUIUtils.getWebToggleButton(TActionsFactory.getAction("testTrooper"));
		WebToggleButton stop = TUIUtils.getWebToggleButton(TActionsFactory.getAction("stopTrooper"));
		WebToggleButton pause = TUIUtils.getWebToggleButton(TActionsFactory.getAction("pauseTrooper"));
		GroupPane pane = new GroupPane(play, test, stop, pause);

		stop.setSelected(true);
		play.addActionListener(e -> test.setEnabled(false));
		test.addActionListener(e -> play.setEnabled(false));
		stop.addActionListener(e -> {
			play.setEnabled(true);
			test.setEnabled(true);
		});

		getToolBar().add(pane);
		setBodyComponent(wtp);
	}

	/**
	 * shortcut that enable update all the UI components from (posible) new
	 * {@link SensorsArray} and/or {@link PokerSimulator}
	 * 
	 * @param sensorsArray - the array to update
	 */
	public void updateSensorsArray(Trooper trooper) {
		setVisible(false);
		pockerSimulatorPanel.removeAll();
		pockerSimulatorPanel.add(trooper.getPokerSimulator().getReportPanel(), BorderLayout.CENTER);
		sensorArrayPanel.updateArray(trooper.getSensorsArray());
		setVisible(true);
	}
}
