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

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.Action;

import org.jdesktop.application.*;

import com.alee.laf.button.*;
import com.alee.utils.*;

import core.*;
import datasource.*;
import net.sourceforge.tess4j.*;

public class Hero {

	private static Trooper activeTrooper;
	public static Logger heroLogger = Logger.getLogger("Hero");
	private static HeroPanel heroPanel;
//	private static Table simulationTable;
//	private GameSimulatorPanel simulatorPanel;

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
	 * This method is separated because maybe in the future we will need different robot for different graphics
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
		iTesseract.setDatapath(Constants.TESSDATA); // path to tessdata directory

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

	/**
	 * central parser from PokerStar located formated number ($12,12) to parseable string to double
	 * <p>
	 * NOTE this method is called from {@link ScreenSensor} instances in order to correct the OCR read
	 * 
	 * @param numer - locale number
	 * @param currencySymbol - current symbol "" if not currency symbol is present
	 * @return double string
	 */
	public static String parseNummer(String numer, String currencySymbol) {
		char decSep = DecimalFormatSymbols.getInstance().getDecimalSeparator();

		String srcocd = numer.replaceAll("[^" + currencySymbol + decSep + "1234567890]", "");

		// at this point the var mus contain the currency symbol as first caracter. in case of error, the first
		// caracter maybe is a number. as a fail safe, remove allways the first caracter.
		if (!"".equals(currencySymbol) && srcocd.length() > 1)
			srcocd = srcocd.substring(1).trim();

		// at this point tesserac may detected the correct decimal separato o maybe not.
		srcocd = srcocd.replace(decSep, '.');

		// correction. at this poit if tesserac make a mistake in the reading the variable contain a extreme large
		// amount. sucho amount is correct amount

		// // use currency symbol as marker. when the currency symbol is present, assume 2 decimal digits for all
		// // numbers
		// if (!"".equals(currencySymbol)) {
		// int len = srcocd.length();
		// srcocd = srcocd.substring(0, len - 2) + "." + srcocd.substring(len - 2);
		// }
		return srcocd;
	}

	public Hero() {
		TActionsFactory.insertActions(this);
		Alesia.openDB();
		Locale.setDefault(Locale.ENGLISH);
	}


	public ArrayList<javax.swing.Action> getUI() {
		ArrayList<Action> alist = new ArrayList<>();
		alist.add(TActionsFactory.getAction("showHeroPanel"));
		alist.add(TActionsFactory.getAction("showUoaEvaluatorPanel"));
		alist.add(TActionsFactory.getAction("showPreFlopCardsPanel"));
		alist.add(TActionsFactory.getAction("showGameSimulatorPanel"));
		return alist;
	}

	private void initTrooperEnvironment() {
		TrooperParameter parameter = TrooperParameter.getHero();
		activeTrooper = new Trooper(parameter);
		heroPanel.setTrooper(activeTrooper);
	}

	@org.jdesktop.application.Action
	public void pauseTrooper(ActionEvent event) {
		if (activeTrooper != null) {
			activeTrooper.pause(true);
		}
	}

	@org.jdesktop.application.Action
	public Task<Void, Map<String, Object>> runTrooper(ActionEvent event) {
		// Retrieve info from the porker window to resize
		ArrayList<TSEntry> windows = TResources.getActiveWindows("terry1013");

		// live trooper need the target table window
		if (windows.isEmpty()) {
			JOptionPane.showMessageDialog(Alesia.getMainFrame(), "No active window found", "Error",
					JOptionPane.ERROR_MESSAGE);
			WebToggleButton tb = (WebToggleButton) TActionsFactory.getAbstractButton(event);
			tb.setSelected(false);
			 return null;
		}

		// Override trooper table parameters
		String winTitle = windows.get(0).getValue();
		String[] words = winTitle.split("\\s");
		String cs = "";
		for (String word : words) {
			if (TStringUtils.wildCardMacher(word, "*/*")) {

				// is there a currency symbol?
				char ch = word.charAt(0);
				if (!Character.isDigit(ch))
					cs = String.valueOf(ch);

				// remove currency symbol and split
				String[] sb_bb = word.split("/");
				double sb = Double.parseDouble(Hero.parseNummer(sb_bb[0], cs));
				double bb = Double.parseDouble(Hero.parseNummer(sb_bb[1], cs));
				double bi = bb * Constants.BUYIN_FACTOR;
				TrooperParameter trooperParameter = TrooperParameter.findFirst("trooper = ?", "Hero");
				trooperParameter.setDouble("buyIn", bi);
				trooperParameter.setDouble("bigBlind", bb);
				trooperParameter.setDouble("smallBlind", sb);
				trooperParameter.set("currency", cs);
				trooperParameter.save();
			}
		}

		Alesia.getMainFrame().setBounds(10, 65, 620, 900);
		// TResources.performCMDOWCommand(winds.get(0).getKey(), "/siz 1200 1200 /mov " + monitorWith + 630 + " 65 ");
		TResources.performCMDOWCommand(windows.get(0).getKey(), "/siz 1200 1200 /mov 630 65 ");
		initTrooperEnvironment();
		return activeTrooper;
	}

	@org.jdesktop.application.Action
	public void savePreflopRange(ActionEvent event) {
		/**
		 * this method is no more util. only for educational purporse
		 */
		AbstractButton ab = (AbstractButton) event.getSource();
		PreFlopCardsPanel rangePanel = SwingUtils.getFirstParent(ab, PreFlopCardsPanel.class);
		TSEntry selR = rangePanel.getSelectedRange();
		String nameDesc = (String) JOptionPane.showInputDialog(Alesia.getMainFrame(),
				"Write the name and a shor description for this range. \nThe name an description muss be coma separated.",
				"Save", JOptionPane.PLAIN_MESSAGE, null, null, selR.getKey() + "," + selR.getValue());
		// save
		if (nameDesc == null)
			return;

		// correct format ?
		String[] nam_desc = nameDesc.split("[,]");
		if (nam_desc.length == 1) {
			JOptionPane.showMessageDialog(Alesia.getMainFrame(), "Format error", "Error",
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
		showPreFlopCardsPanel();
	}

	@org.jdesktop.application.Action
	public void showGameSimulatorPanel(ActionEvent event) {
		SimulationParametersPanel simulatorPanel = new SimulationParametersPanel();
		simulatorPanel.setTitleDescriptionFromAction("showGameSimulatorPanel");
		Alesia.getMainPanel().showPanel(simulatorPanel);
	}

	@org.jdesktop.application.Action
	public void showHeroPanel() {
		// if there a instance of trooper current active, return the same pane
		if (activeTrooper != null && activeTrooper.isStarted()) {
			Alesia.getMainPanel().showPanel(heroPanel);
		} else {
			heroPanel = new HeroPanel();
			Alesia.getMainPanel().showPanel(heroPanel);
			initTrooperEnvironment();
		}
	}


	@org.jdesktop.application.Action
	public void showPreFlopCardsPanel() {
		PreFlopCardsPanel panel = new PreFlopCardsPanel();
		panel.setTitleDescriptionFromAction("showPreFlopCardsPanel");
		Alesia.getMainPanel().showPanel(panel);
	}

	@org.jdesktop.application.Action
	public void showUoaEvaluatorPanel(ActionEvent event) {
		UoAPanel aPanel = new UoAPanel();
		aPanel.setTitleDescriptionFromAction("showUoaEvaluatorPanel");
		Alesia.getMainPanel().showPanel(aPanel);
	}

	@org.jdesktop.application.Action
	public void stopTrooper(ActionEvent event) {
		if (activeTrooper != null) {
			activeTrooper.cancelTrooper(true);
			initTrooperEnvironment();
		}
	}

	@org.jdesktop.application.Action
	public void testAreasPpt(ActionEvent event) {
		if (activeTrooper != null)
			activeTrooper.getSensorsArray().testConfigurationFileSensorsAreas();
	}

	@org.jdesktop.application.Action
	public void testAreasScreen(ActionEvent event) {
		if (activeTrooper != null) {
			if (activeTrooper.getSensorsArray().getReadSourceFile() == null) {
				JOptionPane.showMessageDialog(Alesia.getMainFrame(), "No screenshot file has been setted",
						"Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			activeTrooper.getSensorsArray().testScreenShotsSensorsAreas();
		}
	}

	@org.jdesktop.application.Action
	public Task testTrooper(ActionEvent event) {
		initTrooperEnvironment();
		activeTrooper.getSensorsArray().setReadSource(SensorsArray.FROM_FILE);
		return activeTrooper;
	}

}
