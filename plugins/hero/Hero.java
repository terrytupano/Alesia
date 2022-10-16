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
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.Action;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;
import org.jdesktop.application.Task.*;

import com.alee.laf.button.*;
import com.alee.utils.*;

import core.*;
import core.datasource.model.*;
import net.sourceforge.tess4j.*;
import plugins.hero.ozsoft.*;
import plugins.hero.ozsoft.bots.*;
import plugins.hero.ozsoft.gui.*;
import plugins.hero.utils.*;

public class Hero extends TPlugin {

	private static Trooper activeTrooper;
	protected static Logger heroLogger = Logger.getLogger("Hero");
	private static HeroPanel heroPanel;
	private static Table simulationTable;
	private GameSimulatorPanel simulatorPanel;

	public static Action getLoadAction() {
		Action load = TActionsFactory.getAction("fileChooserOpen");
		load.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals(TActionsFactory.DATA_LOADED)) {
				// tableFile = (File) load.getValue(TActionsFactory.DATA_LOADED);
			}
		});
		return load;
	}

	/**
	 * This metod is separated because maybe in the future we will need diferents robot for diferent graphics
	 * configurations
	 * 
	 * @return
	 */
	public static Robot getNewRobot() {
		Robot r = null;
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		return r;
	}

	public static Tesseract getTesseract() {
		// TODO: no visible performance improve by setting every sensor with his own
		// teseract instance
		Tesseract iTesseract = new Tesseract(); // JNA Interface Mapping
		iTesseract.setDatapath("plugins/hero/tessdata"); // path to tessdata directory

		// DON:T SET THE PAGEMODE VAR: THIS DESTROY THE ACURACY OF THE OCR OPERATION. i
		// don.t know why but it is
		// iTesseract.setPageSegMode(3); //

		iTesseract.setTessVariable("classify_enable_learning", "0");
		iTesseract.setTessVariable("OMP_THREAD_LIMIT", "1");
		// TODO: recheck performanece. no visible performance improve setting this
		// variable
		// iTesseract.setOcrEngineMode(0); // Run Tesseract only - fastest
		return iTesseract;
	}

	public Hero() {
		TActionsFactory.insertActions(this);
		Alesia.getInstance().openDB("hero");
	}

	@org.jdesktop.application.Action
	public void backrollHistory(ActionEvent event) {
		LazyList<SimulationResult> results = SimulationResult.find("trooper = ? AND hands = ?", "Hero", 0);
		ArrayList<String> names = new ArrayList<>();
		results.forEach(sr -> names.add(sr.getString("name")));
		String[] possibleValues = names.toArray(new String[0]);
		Object selectedValue = JOptionPane.showInputDialog(Alesia.getInstance().mainFrame, "Choose one", "Input",
				JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
		if (selectedValue != null) {
			String resultName = selectedValue.toString();

			// retrive the first element of the statistical series to detect, the type of graph
			Alesia.getInstance().openDB("hero");
			SimulationResult sample = SimulationResult.findFirst("name = ? AND trooper = ?", resultName, "Hero");
			JDialog chart;
			if (sample.get("aditionalValue") != null) {
				chart = new SingeVariableSimulationLineChart(resultName);
			} else {
				chart = new MultiVariableSimulationBarChar(resultName);
			}

			chart.pack();
			chart.setLocationRelativeTo(null);
			chart.setVisible(true);
		}
	}

	@org.jdesktop.application.Action
	public void gameSimulator(ActionEvent event) {
		this.simulatorPanel = new GameSimulatorPanel();
		// trooperPanel = simulatorPanel.getTrooperPanel();
		Alesia.getInstance().getMainPanel().showPanel(simulatorPanel);
	}

	@Override
	public ArrayList<javax.swing.Action> getUI(String type) {
		ArrayList<Action> alist = new ArrayList<>();
		alist.add(TActionsFactory.getAction("heroPanel"));
		alist.add(TActionsFactory.getAction("uoAEvaluator"));
		alist.add(TActionsFactory.getAction("preFlopCardsRange"));
		alist.add(TActionsFactory.getAction("gameSimulator"));
		return alist;
	}

	@org.jdesktop.application.Action
	public void heroPanel(ActionEvent event) {
		heroPanel = new HeroPanel();
		Alesia.getInstance().getMainPanel().showPanel(heroPanel);
		initTrooperEnvironment();
		Alesia.getInstance().getMainFrame().setBounds(10, 65, 620, 900);
	}

	private void initTrooperEnvironment() {
		simulationTable = null;
		activeTrooper = new Trooper();
		heroPanel.updateSensorsArray(activeTrooper);
	}

	@org.jdesktop.application.Action
	public void pauseTrooper(ActionEvent event) {
		if (activeTrooper != null) {
			activeTrooper.pause(true);
		}
	}

	@org.jdesktop.application.Action
	public void preFlopCardsRange(ActionEvent event) {
		// this method is called by #savePreflopRange
		PreFlopCardsPanel panel = new PreFlopCardsPanel();
		Alesia.getInstance().getMainPanel().showPanel(panel);
	}

	@org.jdesktop.application.Action
	public Task runTrooper(ActionEvent event) {
		// retrive info from the porker window to resize
		ArrayList<TEntry<String, String>> winds = TResources.getActiveWindows("terry1013");
		// TODO: temporal: set manualy the correct win pos and size
		if (winds.isEmpty()) {
			JOptionPane.showMessageDialog(Alesia.getInstance().getMainFrame(), "No active window found", "Error",
					JOptionPane.ERROR_MESSAGE);
			WebToggleButton tb = (WebToggleButton) TActionsFactory.getAbstractButton(event);
			tb.setSelected(false);
			return null;

		}
		// TResources.performCMDOWCommand(winds.get(0).getKey(), "/siz 1200 1200 /mov " + monitorWith + 630 + " 65 ");
		TResources.performCMDOWCommand(winds.get(0).getKey(), "/siz 1200 1200 /mov 630 65 ");
		initTrooperEnvironment();
		return activeTrooper;
	}

	@org.jdesktop.application.Action
	public void savePreflopRange(ActionEvent event) {
		AbstractButton ab = (AbstractButton) event.getSource();
		PreFlopCardsPanel rangePanel = SwingUtils.getFirstParent(ab, PreFlopCardsPanel.class);
		TEntry<String, String> selR = rangePanel.getSelectedRange();
		String nameDesc = (String) JOptionPane.showInputDialog(Alesia.getInstance().getMainFrame(),
				"Write the name and a shor description for this range. \nThe name an description muss be coma separated.",
				"Save", JOptionPane.PLAIN_MESSAGE, null, null, selR.getKey() + "," + selR.getValue());
		// save
		if (nameDesc == null)
			return;

		// correct format ?
		String[] nam_desc = nameDesc.split("[,]");
		if (nam_desc.length == 1) {
			JOptionPane.showMessageDialog(Alesia.getInstance().getMainFrame(), "Format error", "Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		// override
		PreflopCards tmp = PreflopCards.findFirst("rangeName = ? ", nam_desc[0]);
		if (tmp != null) {
			Object[] options = {"OK", "CANCEL"};
			int opt = JOptionPane.showOptionDialog(rangePanel, "the preflop range " + nam_desc[0] + " exist. Override?",
					"Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			if (opt != 0)
				return;
		}

		rangePanel.getPreflopCardsRange().saveInDB(nam_desc[0], nam_desc[1]);
		preFlopCardsRange(null);
	}

	@org.jdesktop.application.Action(block = BlockingScope.ACTION)
	// @org.jdesktop.application.Action(block = BlockingScope.WINDOW)
	// @org.jdesktop.application.Action
	public Task startSimulation(ActionEvent event) {
		try {
			// check max task
			if (!Alesia.getInstance().taskManager.suporMoreTask()) {
				Alesia.showNotification("hero.msg03", "");
				return null;
			}
			// check for hero client
			if (TrooperParameter.find("name = ?", "Hero") == null) {
				Alesia.showNotification("hero.msg01", "");
				return null;
			}
			// check min num of players
			if (TrooperParameter.count("isActive = ?", true) < 2) {
				Alesia.showNotification("hero.msg02");
				return null;
			}

			// WARNING: order by chair is importat. this is take into akonut in simulation
			LazyList<TrooperParameter> tparms = TrooperParameter.findAll().orderBy("chair");

			TrooperParameter hero = TrooperParameter.findFirst("trooper = ?", "Hero");
			int buy = hero.getDouble("buyIn").intValue();
			int bb = hero.getDouble("bigBlind").intValue();
			simulationTable = new Table(TableType.NO_LIMIT, buy, bb);
			for (TrooperParameter tparm : tparms) {
				if (tparm.getBoolean("isActive")) {
					String tName = tparm.getString("trooper");
					String bCls = tparm.getString("client");
					Class<?> cls = Class.forName("plugins.hero.ozsoft.bots." + bCls);
					// Constructor cons = cls.getConstructor(String.class);
					// Bot bot = (Bot) cons.newInstance(name);
					@SuppressWarnings("deprecation")
					Bot bot = (Bot) cls.newInstance();
					Trooper t = bot.getSimulationTrooper(simulationTable, tparm);
					Player p = new Player(tName, buy, bot, tparm.getInteger("chair"));
					simulationTable.addPlayer(p);
					if ("Hero".equals(tName))
						simulatorPanel.updatePokerSimulator(t.getPokerSimulator());
				}
			}

			TableDialog dialog = new TableDialog(simulationTable);
			dialog.setVisible(true);

			return simulationTable;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@org.jdesktop.application.Action
	public void stopTrooper(ActionEvent event) {
		if (activeTrooper != null) {
			activeTrooper.cancelTrooper(true);
			activeTrooper = null;
			initTrooperEnvironment();
		}
	}

	@org.jdesktop.application.Action
	public void testAreas(ActionEvent event) {
		if (activeTrooper != null)
			activeTrooper.getSensorsArray().testSensorsAreas();
	}

	@org.jdesktop.application.Action
	public Task testTrooper(ActionEvent event) {
		initTrooperEnvironment();
		activeTrooper.getSensorsArray().setLive(false);
		return activeTrooper;
	}

	@org.jdesktop.application.Action
	public void uoAEvaluator(ActionEvent event) {
		UoAPanel aPanel = new UoAPanel();
		Alesia.getInstance().getMainPanel().showPanel(aPanel);
	}

}
