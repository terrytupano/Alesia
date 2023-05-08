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
package hero;

import com.alee.laf.button.*;
import com.alee.laf.grouping.*;
import com.alee.laf.scroll.*;
import com.alee.laf.tabbedpane.*;
import com.alee.managers.settings.*;

import core.*;
import datasource.*;
import gui.*;
import gui.console.*;

public class HeroPanel extends TUIFormPanel {

	private TrooperPanel trooperPanel;
	private SensorArrayPanel sensorArrayPanel;
	private PokerSimulatorPanel pockerSimulatorPanel;
	private WebTabbedPane webTabbedPane;

	public HeroPanel() {
		this.sensorArrayPanel = new SensorArrayPanel();

		TrooperParameter model = TrooperParameter.findFirst("trooper = ?", "Hero");
		this.trooperPanel = new TrooperPanel(model);
		WebScrollPane trooperscrollPane = TUIUtils.getWebScrollPane(trooperPanel);

		this.pockerSimulatorPanel = new PokerSimulatorPanel();
		webTabbedPane = new WebTabbedPane();
		webTabbedPane.add(trooperscrollPane, "Trooper parameters");
		webTabbedPane.add(sensorArrayPanel, "Sensor Array");
		webTabbedPane.add(pockerSimulatorPanel, "Pocker Simulator");
		webTabbedPane.add(new ConsolePanel(Hero.heroLogger), "Log console");
		webTabbedPane.registerSettings(new Configuration<TabbedPaneState>("HeroPanel.tabbedPanel"));

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
		setBodyComponent(webTabbedPane);
	}

	/**
	 * shortcut that enable update all the UI components from (possible) new
	 * {@link SensorsArray} and/or {@link PokerSimulator}
	 * 
	 * @param trooper - the new trooper
	 */
	public void setTrooper(Trooper trooper) {
		this.pockerSimulatorPanel.setTrooper(trooper);
		this.sensorArrayPanel.setTrooper(trooper);
	}
}
