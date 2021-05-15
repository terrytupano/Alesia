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
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.table.*;

import org.jdesktop.application.*;

import com.alee.laf.*;

import core.*;
import gui.console.*;
import net.sourceforge.tess4j.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.cardgame.games.*;
import plugins.hero.cardgame.interfaces.*;
import plugins.hero.ozsoft.texasholdem.*;
import plugins.hero.ozsoft.texasholdem.bots.*;

public class Hero extends TPlugin {

	// protected static Tesseract iTesseract;
	protected static ActionMap actionMap;
	protected static Logger heroLogger;
	protected static ConsolePanel consolePanel;
	protected static HeroPanel heroPanel;
	protected static File tableFile;
	protected static boolean isTestMode;
	protected static SensorsArray sensorsArray;
	protected static ShapeAreas shapeAreas;
	protected static Hashtable<String, Object> trooperParameters;
	protected static String CARDS_FOLDER = "plugins/hero/cards/";
	private static DateFormat dateFormat;
	/**
	 * update every time the action {@link #runTrooper(ActionEvent)} is performed
	 */
	private static Date startDate = null;

	private GameSimulatorPanel simulatorPanel;

	public Hero() {
		// iTesseract.setLanguage("pok");
		dateFormat = DateFormat.getDateTimeInstance();
		actionMap = Alesia.getInstance().getContext().getActionMap(this);
		heroLogger = Logger.getLogger("Hero");
		consolePanel = new ConsolePanel(heroLogger);
		TActionsFactory.insertActions(actionMap);
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
	 * return the string representation of a list of cards
	 * 
	 * @param cards cards
	 * 
	 * @return
	 */
	public static String parseCards(List<UoACard> cards) {
		StringBuffer sb = new StringBuffer();
		cards.forEach(c -> sb.append(c.toString() + " "));
		return sb.toString().trim();
	}
	public static String parseHands(List<UoAHand> hands) {
		String hs = hands.toString();
		hs = hs.replaceAll("[ ]", "");
		hs = hs.replace(',', ' ');
		return hs.substring(1, hs.length() - 1);
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
	public static void parseTableParameters(Hashtable<String, Object> values) {
		String[] tparms = values.get("table.parameters").toString().split("[,]");
		values.put("table.buyIn", new Double(tparms[0]));
		values.put("table.bigBlid", new Double(tparms[1]));
		values.put("table.smallBlid", new Double(tparms[2]));
		// simbol if its present of "" if not
		values.put("table.currency", tparms.length > 3 ? tparms[3] : "");
	}

	public static String parseToUnicode(String hand) {
		String uni = new String(hand);
		uni = uni.replace('s', '\u2660');// u2660
		uni = uni.replace('c', '\u2663');// u2663
		uni = uni.replace('h', '\u2665');// u2665
		uni = uni.replace('d', '\u2666');// u2666
		return uni;
	}

	private static void initGlovalVars() {
		// dont put isTestMode = false; HERE !!!!!!!!!!!!!!!!!
		startDate = new Date();
		shapeAreas = new ShapeAreas(Hero.tableFile);
		shapeAreas.read();
		sensorsArray = new SensorsArray();
		sensorsArray.setShapeAreas(shapeAreas);
		heroPanel.updateGlovalParameters();
	}

	protected static String getSesionID() {
		return dateFormat.format(startDate);
	}

	@org.jdesktop.application.Action
	public void gameSimulator(ActionEvent event) {
		this.simulatorPanel = new GameSimulatorPanel();
		Alesia.getInstance().getMainPanel().setContentPanel(simulatorPanel);
	}

	@Override
	public ArrayList<javax.swing.Action> getUI(String type) {
		ArrayList<Action> alist = new ArrayList<>();
		alist.add(actionMap.get("heroPanel"));
		alist.add(actionMap.get("uoAEvaluator"));
		alist.add(actionMap.get("gameSimulator"));
		return alist;
	}

	@org.jdesktop.application.Action
	public void heroPanel(ActionEvent event) {
		heroPanel = new HeroPanel();
		Alesia.getInstance().getMainPanel().setContentPanel(heroPanel);
		// temp: change the main frame using this coordenates: 0,40 547,735
		// temporal: must be loaded from troperPanel
		// tableFile = new File("plugins/hero/resources/ps-main table.ppt");
		tableFile = new File("plugins/hero/resources/ps-10.ppt");
		initGlovalVars();
		Alesia.getInstance().getMainFrame().setBounds(0, 40, 547, 735);
	}

	@org.jdesktop.application.Action
	public void pauseTrooper(ActionEvent event) {
		Trooper t = Trooper.getInstance();
		if (t != null) {
			boolean pause = !t.isPaused();
			AbstractButton ab = (AbstractButton) event.getSource();
			// ab.setSelectedIcon(TResources.getSmallIcon("plugins/hero/resources/ResumeTrooper"));
			ab.setSelectedIcon(TUIUtils.getSmallFontIcon('\uf01d'));
			ab.setSelected(pause);
			t.pause(pause);
		}
	}

	@org.jdesktop.application.Action
	public Task runTrooper(ActionEvent event) {
		// retrive info from the porker window to resize
		ArrayList<TEntry<String, String>> winds = TResources.getActiveWindows("terry1013");
		// TODO: temporal: set manualy the correct win pos and size
		if (winds.isEmpty()) {
			JOptionPane.showMessageDialog(Alesia.getInstance().getMainFrame(), "No active window found", "Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		TResources.performCMDOWCommand(winds.get(0).getKey(), "/siz 840 600 /mov 532 41");

		isTestMode = false;
		return start();
	}

	@org.jdesktop.application.Action
	public Task startSimulation(ActionEvent event) {
		try {
			Hashtable<String, Object> values = simulatorPanel.getValues();
			TableModel model = simulatorPanel.getPlayersTable().getModel();

			int buy = ((Double) values.get("table.buyIn")).intValue();
			int bb = ((Double) values.get("table.bigBlid")).intValue();
			int sb = ((Double) values.get("table.smallBlid")).intValue();
			int maxR = ((Long) values.get("play.maxRaise")).intValue();
//			TODO: add the iption for limit and no limit
			Table game = new Table(TableType.NO_LIMIT, bb);

			for (int i = 0; i < model.getRowCount(); i++) {
				if ((Boolean) model.getValueAt(i, 2)) {
					String name = model.getValueAt(i, 0).toString();
					String bCls = model.getValueAt(i, 1).toString();
					Class cls = Class.forName("plugins.hero.ozsoft.texasholdem." + bCls);
					Bot bot = (Bot) cls.newInstance();
					Player p = new Player(name, buy, bot);
					game.addPlayer(p);
				}
			}
			

			GameWindow window = new GameWindow();
			window.setTitle("HoldEm " + buy + " " + bb + "/" + sb);
			window.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					((JDialog) e.getSource()).dispose();
					game.cancel(true);
				}
			});
			window.setVisible(true);
			return game;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@org.jdesktop.application.Action
	public void stopTrooper(ActionEvent event) {
		actionMap.get("testTrooper").setEnabled(true);
		actionMap.get("runTrooper").setEnabled(true);
		actionMap.get("pauseTrooper").setEnabled(true);
		if (Trooper.getInstance() != null)
			Trooper.getInstance().cancelTrooper(true);
	}

	@org.jdesktop.application.Action
	public void testAreas(ActionEvent event) {
		sensorsArray.testAreas();
	}

	@org.jdesktop.application.Action
	public Task testTrooper(ActionEvent event) {
		isTestMode = true;
		return start();
	}

	@org.jdesktop.application.Action
	public void uoAEvaluator(ActionEvent event) {
		UoAPanel aPanel = new UoAPanel();
		Alesia.getInstance().getMainPanel().setContentPanel(aPanel);
	}

	private Task start() {
		WebLookAndFeel.setForceSingleEventsThread(false);
		initGlovalVars();
		Trooper t = new Trooper();
		PropertyChangeListener tl = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (Trooper.PROP_DONE.equals(evt.getPropertyName())) {
					// WebLookAndFeel.setForceSingleEventsThread(true);
				}
			}
		};
		// t.getPokerSimulator().setParameter();
		t.addPropertyChangeListener(tl);
		actionMap.get("testTrooper").setEnabled(false);
		actionMap.get("runTrooper").setEnabled(false);
		actionMap.get("pauseTrooper").setEnabled(true);
		return t;
	}
}
