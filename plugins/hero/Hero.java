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
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.Action;

import org.jdesktop.application.*;

import com.alee.laf.*;

import core.*;
import gui.console.*;
import net.sourceforge.tess4j.*;

public class Hero extends TPlugin {

	// protected static Tesseract iTesseract;
	protected static ActionMap actionMap;
	protected static ConsolePanel consolePanel;
	protected static HeroPanel heroPanel;
	protected static Logger logger;
	protected static File tableFile;
	protected static boolean isTestMode;
	protected static SensorsArray sensorsArray;
	protected static ShapeAreas shapeAreas;
	protected static Hashtable<String, Object> trooperParameters;
	protected static String CARDS_FOLDER = "plugins/hero/cards/";
	protected static TreeMap<String, BufferedImage> preparedCards;
	private static DateFormat dateFormat;
	/**
	 * update every time the action {@link #runTrooper(ActionEvent)} is performed
	 */
	private static Date startDate = null;

	protected static String getSesionID() {
		return dateFormat.format(startDate);
	}
	public Hero() {
		// iTesseract.setLanguage("pok");
		dateFormat = DateFormat.getDateTimeInstance();
		actionMap = Alesia.getInstance().getContext().getActionMap(this);
		logger = Logger.getLogger("Hero");
		consolePanel = new ConsolePanel(logger);
		// preparedCards = TCVUtils.loadCards(CARDS_FOLDER);
		TActionsFactory.insertActions(actionMap);
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

	@Override
	public ArrayList<javax.swing.Action> getUI(String type) {
		ArrayList<Action> alist = new ArrayList<>();
		alist.add(actionMap.get("heroPanel"));
		return alist;
	}

	@org.jdesktop.application.Action
	public void heroPanel(ActionEvent event) {
		heroPanel = new HeroPanel();
		Alesia.getMainPanel().setContentPanel(heroPanel);
		// temp: change the main frame using this coordenates: 0,40 547,735
		// temporal: must be loaded from troperPanel
		tableFile = new File("plugins/hero/resources/ps-main table.ppt");
		initGlovalVars();
		Alesia.mainFrame.setBounds(0, 40, 547, 735);
	}

	@org.jdesktop.application.Action
	public void pauseTrooper(ActionEvent event) {
		Trooper t = Trooper.getInstance();
		if (t != null) {
			boolean pause = !t.isPaused();
			AbstractButton ab = (AbstractButton) event.getSource();
			ab.setSelectedIcon(TResources.getSmallIcon("plugins/hero/resources/ResumeTrooper"));
			ab.setSelected(pause);
			t.pause(pause);
		}
	}

	@org.jdesktop.application.Action
	public Task runTrooper(ActionEvent event) {
		// retrive info from the porker window to resize
		ArrayList<TEntry<String, String>> winds = TResources.getActiveWindows("*terry1013*");
		// TODO: temporal: set manualy the correct win pos and size
		if (winds.isEmpty()) {
			JOptionPane.showMessageDialog(Alesia.mainFrame, "No active window found", "Error",
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		TResources.performCMDOWCommand(winds.get(0).getKey(), "/siz 840 600 /mov 532 41");

		isTestMode = false;
		return start();
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
	public void testCards(ActionEvent event) {
		sensorsArray.testCards();
	}

	@org.jdesktop.application.Action
	public Task testTrooper(ActionEvent event) {
		isTestMode = true;
		return start();
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
