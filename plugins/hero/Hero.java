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
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.Action;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.alee.laf.*;
import com.alee.laf.button.*;
import com.alee.utils.*;

import core.*;
import core.datasource.model.*;
import gui.*;
import net.sourceforge.tess4j.*;
import plugins.hero.ozsoft.*;
import plugins.hero.ozsoft.bots.*;
import plugins.hero.ozsoft.gui.*;
import plugins.hero.utils.*;

public class Hero extends TPlugin {

	// protected static Tesseract iTesseract;
	protected static Logger heroLogger;
	protected static File tableFile;
	protected static boolean isTestMode;
	protected static ShapeAreas shapeAreas;
	// protected static Hashtable<String, Object> trooperParameters;
	private static DateFormat dateFormat;
	/**
	 * update every time the action {@link #runTrooper(ActionEvent)} is performed
	 */
	private static Date startDate = null;
	public static TrooperPanel trooperPanel;
	private static Trooper activeTrooper;
	/** in simualtion envioremet, this is the current instance of Table. */
	public static Table simulationTable;
	private HeroPanel heroPanel;
	private GameSimulatorPanel simulatorPanel;

	private SensorsArray sensorsArray;

	public Hero() {
		dateFormat = DateFormat.getDateTimeInstance();
		heroLogger = Logger.getLogger("Hero");
		TActionsFactory.insertActions(this);
		Alesia.getInstance().openDB("hero");
	}

	/**
	 * in simulation enviorement, this method return <code>true</code> if the current enviorement is NOT a simulation or
	 * when the current player is Hero and the speed is correct
	 * 
	 * @return
	 */
	public static boolean allowSimulationGUIUpdate() {
		// enviorement is NOT in simulation?
		if (Hero.simulationTable == null)
			return true;

		// never log in simulationSpeed <= 10;
		if (Hero.simulationTable.getSpeed() == Table.RUN_BACKGROUND)
			return false;

		// in simulation eviorement, update panel only for hero when the speed is not 0
		if (Hero.simulationTable.getSpeed() < Table.RUN_INTERACTIVE_LOG
				|| !"Hero".equals(Hero.simulationTable.getActor().getName()))
			return false;

		// the speed is correct and the current player is Hero
		return true;
	}

	public static Action getLoadAction() {
		Action load = TActionsFactory.getAction("fileChooserOpen");
		load.addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals(TActionsFactory.DATA_LOADED)) {
				tableFile = (File) load.getValue(TActionsFactory.DATA_LOADED);
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

	public static Date getStartDate() {
		return startDate;
	}
	public static Tesseract getTesseract() {
		// TODO: no visible performance improve by setting every sensor with his own teseract instance
		Tesseract iTesseract = new Tesseract(); // JNA Interface Mapping
		iTesseract.setDatapath("plugins/hero/tessdata"); // path to tessdata directory

		// DON:T SET THE PAGEMODE VAR: THIS DESTROY THE ACURACY OF THE OCR OPERATION. i don.t know why but it is
		// iTesseract.setPageSegMode(3); //

		iTesseract.setTessVariable("classify_enable_learning", "0");
		iTesseract.setTessVariable("OMP_THREAD_LIMIT", "1");
		// TODO: recheck performanece. no visible performance improve setting this variable
		// iTesseract.setOcrEngineMode(0); // Run Tesseract only - fastest
		return iTesseract;
	}

	/**
	 * parse the variable <code>table.parameters</code> inserting in the <code>values</code> hastable:
	 * <li><code>table.buyIn</code>
	 * <li><code>table.bigBlid</code>
	 * <li><code>table.smallBlid</code>
	 * <li><code>table.currency</code> (simbol if its present of "" if not)
	 * 
	 * @param values - values
	 */
	public static void parseTableParameters(Map<String, Object> values) {
		String[] tparms = values.get("table.parameters").toString().split("[,]");
		values.put("table.buyIn", new Double(tparms[0]));
		values.put("table.bigBlid", new Double(tparms[1]));
		values.put("table.smallBlid", new Double(tparms[2]));
		// simbol if its present of "" if not
		values.put("table.currency", tparms.length > 3 ? tparms[3] : "");
	}

	@Deprecated
	public static String parseToUnicode(String hand) {
		String uni = new String(hand);
		uni = uni.replace('s', '\u2660');// u2660
		uni = uni.replace('c', '\u2663');// u2663
		uni = uni.replace('h', '\u2665');// u2665
		uni = uni.replace('d', '\u2666');// u2666
		return uni;
	}

	protected static String getSesionID() {
		return dateFormat.format(startDate);
	}

	@org.jdesktop.application.Action
	public void gameSimulator(ActionEvent event) {
		this.simulatorPanel = new GameSimulatorPanel();
		trooperPanel = simulatorPanel.getTrooperPanel();
		Alesia.getInstance().getMainPanel().setContentPanel(simulatorPanel);
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
		trooperPanel = heroPanel.getTrooperPanel();
		Alesia.getInstance().getMainPanel().setContentPanel(heroPanel);
		// temp: change the main frame using this coordenates: 0,40 547,735
		// temporal: must be loaded from troperPanel
		// tableFile = new File("plugins/hero/resources/ps-main table.ppt");
		tableFile = new File("plugins/hero/resources/ps-10.ppt");
		initTrooperEnviorement();
		Alesia.getInstance().getMainFrame().setBounds(0, 40, 547, 735);
	}

	@org.jdesktop.application.Action
	public void pauseTrooper(ActionEvent event) {
		if (activeTrooper != null) {
			boolean pause = !activeTrooper.isPaused();
			TActionsFactory.getAbstractButton(event).setIcon(TUIUtils.getSmallFontIcon(pause ? '\ue037' : '\ue034'));
			activeTrooper.pause(pause);
		}
	}

	@org.jdesktop.application.Action
	public void preFlopCardsRange(ActionEvent event) {
		// this method is called by #savePreflopRange
		PreFlopCardsPanel panel = new PreFlopCardsPanel();
		Alesia.getInstance().getMainPanel().setContentPanel(panel);
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
		TResources.performCMDOWCommand(winds.get(0).getKey(), "/siz 840 600 /mov 532 41");
		isTestMode = false;
		return start(event);
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

	// @org.jdesktop.application.Action(block = BlockingScope.COMPONENT)
	// @org.jdesktop.application.Action(block = BlockingScope.WINDOW)
	@org.jdesktop.application.Action
	public Task startSimulation(ActionEvent event) {
		try {
			// check max task
			if (!Alesia.getInstance().taskManager.suporMoreTask()) {
				Alesia.showNotification("hero.msg03", "");
				return null;
			}
			// check for hero client
			if (SimulatorClient.find("name = ?", "Hero") == null) {
				Alesia.showNotification("hero.msg01", "");
				return null;
			}
			// check min num of players
			if (SimulatorClient.count("isActive = ?", true) < 2) {
				Alesia.showNotification("hero.msg02");
				return null;
			}

			Map<String, Object> values = trooperPanel.getValues();
			LazyList<SimulatorClient> clients = SimulatorClient.findAll().orderBy("chair");

			int buyIn = ((Double) values.get("table.buyIn")).intValue();
			int bb = ((Double) values.get("table.bigBlid")).intValue();
			simulationTable = new Table(TableType.NO_LIMIT, buyIn, bb);
			for (SimulatorClient client : clients) {
				if (client.getBoolean("isActive")) {
					String name = client.getString("playerName");
					String bCls = client.getString("client");
					Class cls = Class.forName("plugins.hero.ozsoft.bots." + bCls);
					// Constructor cons = cls.getConstructor(String.class);
					// Bot bot = (Bot) cons.newInstance(name);
					Bot bot = (Bot) cls.newInstance();
					bot.messageReceived("PlayerName=" + name);
					PokerSimulator simulator = new PokerSimulator();
					Trooper trooper = new Trooper(null, simulator);
					bot.setPokerSimulator(simulator, trooper);
					Player p = new Player(name, buyIn, bot, client.getInteger("chair"));
					simulationTable.addPlayer(p);
					if ("Hero".equals(name))
						simulatorPanel.updatePokerSimulator(simulator);
				}
			}
			startDate = new Date();
			simulationTable.setSpeed(Table.RUN_INTERACTIVE_LOG);
			simulationTable.setSimulationsHand(100000);
			simulationTable.whenPlayerLose(Table.DO_NOTHING);

			TableDialog dialog = new TableDialog(simulationTable);

			// WARNING: this method is overrided!!!
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
			trooperPanel.setAllEnabledBut(true, new String[0]);
			// TActionsFactory.getAction("pauseTrooper").putValue(Action.SMALL_ICON,
			// TUIUtils.getSmallFontIcon('\ue037'));// :
			activeTrooper = null; // '\ue034'));
		}
	}

	@org.jdesktop.application.Action
	public void testAreas(ActionEvent event) {
		if (activeTrooper == null)
			sensorsArray.testAreas();
	}

	@org.jdesktop.application.Action
	public void backrollHistory(ActionEvent event) {
		LineChartDemo6 chart = new LineChartDemo6();
		chart.pack();
		chart.setLocationRelativeTo(null);
		chart.setVisible(true);
	}

	@org.jdesktop.application.Action
	public Task testTrooper(ActionEvent event) {
		isTestMode = true;
		return start(event);
	}

	@org.jdesktop.application.Action
	public void uoAEvaluator(ActionEvent event) {
		UoAPanel aPanel = new UoAPanel();
		Alesia.getInstance().getMainPanel().setContentPanel(aPanel);
	}

	private void initTrooperEnviorement() {
		// dont put isTestMode = false; HERE !!!!!!!!!!!!!!!!!
		simulationTable = null;
		startDate = new Date();
		shapeAreas = new ShapeAreas(Hero.tableFile);
		shapeAreas.read();
		sensorsArray = new SensorsArray();
		sensorsArray.setShapeAreas(shapeAreas);
		heroPanel.updateSensorsArray(sensorsArray);
		trooperPanel = heroPanel.getTrooperPanel();
	}

	private Task start(ActionEvent event) {
		WebLookAndFeel.setForceSingleEventsThread(false);
		initTrooperEnviorement();
		activeTrooper = new Trooper(sensorsArray, sensorsArray.getPokerSimulator());
		PropertyChangeListener tl = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (Trooper.PROP_DONE.equals(evt.getPropertyName())) {
					// WebLookAndFeel.setForceSingleEventsThread(true);
				}
			}
		};
		// t.getPokerSimulator().setParameter();
		activeTrooper.addPropertyChangeListener(tl);
		trooperPanel.setAllEnabledBut(false, "pauseTrooper", "stopTrooper");
		return activeTrooper;
	}
}
