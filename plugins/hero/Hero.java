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

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.alee.laf.*;
import com.alee.laf.button.*;
import com.alee.utils.*;
import com.javaflair.pokerprophesier.api.card.*;

import core.*;
import core.datasource.model.*;
import net.sourceforge.tess4j.*;
import plugins.hero.UoAHandEval.*;
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
	private HeroPanel heroPanel;
	private static Trooper activeTrooper;
	private GameSimulatorPanel simulatorPanel;
	private SensorsArray sensorsArray;

	public Hero() {
		dateFormat = DateFormat.getDateTimeInstance();
		heroLogger = Logger.getLogger("Hero");
		TActionsFactory.insertActions(this);

		Alesia.getInstance().openDB("hero");

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

	/**
	 * return a {@link Properties} object fill whit all data obtains using methods in {@link UoAHandEvaluator}. 
	 * <ul>
	 * <li>2BetterThanMinePercent: parameter contain the porcent of PreflopCards that are better that my Holecards
	 * </ul>
	 * 
	 * @param holeCards - Hole Cards
	 * @param comunityCards - Comunity cards
	 * @return properties
	 */
	public static Properties getUoAEvaluation(String holeCards, String comunityCards) {
		UoAHandEvaluator evaluator = new UoAHandEvaluator();
		int handRank = UoAHandEvaluator.rankHand(new UoAHand(holeCards + " " + comunityCards));
		UoAHand allCards = new UoAHand(holeCards + " " + comunityCards);

		Properties prp = new Properties();
		
		// my hand evaluation
		if (!holeCards.equals("")) {
			prp.put("rank", handRank);
			prp.put("name", UoAHandEvaluator.nameHand(allCards));
			prp.put("bestOf5Cards", evaluator.getBest5CardHand(allCards));
		}

		// board evaluation
		if (!comunityCards.equals("")) {
			int count = 0;
			int total = 0;
			ArrayList<UoAHand> list = new ArrayList<>();
			int[][] rowcol = evaluator.getRanks(new UoAHand(comunityCards));
			for (int i = 0; i < 52; i++) {
				for (int j = 0; j < 52; j++) {
					if (rowcol[i][j] > 0)
						total++;
					if (handRank < rowcol[i][j]) {
						count++;
						// FIXME: this list don.t take into account my hole cards. incorporate cards elimination from
						// the list where i already habe those cards
						list.add(new UoAHand((new UoACard(i)).toString() + " " + (new UoACard(j)).toString()));
					}
				}
			}
			double per = ((int) ((count / (total * 1.0)) * 10000)) / 100.0;
			prp.put("2BetterThanMineOf", total);
			prp.put("2BetterThanMineCount", count);
			prp.put("2BetterThanMinePercent", per);
			prp.put("2BetterThanMinelist", list);
		}
		return prp;
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
	public static void parseTableParameters(Map<String, Object> values) {
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
	public void preFlopCardsRange(ActionEvent event) {
		// this method is called by #savePreflopRange
		PreFlopCardsPanel panel = new PreFlopCardsPanel();
		Alesia.getInstance().getMainPanel().setContentPanel(panel);
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
			if (SimulatorClient.count("isActive = ?", true) != 4) {
				Alesia.showNotification("hero.msg02", "4");
				return null;
			}

			Map<String, Object> values = trooperPanel.getValues();
			LazyList<SimulatorClient> clients = SimulatorClient.findAll();

			int buyIn = ((Double) values.get("table.buyIn")).intValue();
			int bb = ((Double) values.get("table.bigBlid")).intValue();
			PokerSimulator simulator = new PokerSimulator();
			simulatorPanel.updatePokerSimulator(simulator);
			Table table = new Table(TableType.NO_LIMIT, buyIn, bb);
			for (SimulatorClient client : clients) {
				if (client.getBoolean("isActive")) {
					String name = client.getString("playerName");
					String bCls = client.getString("client");
					Class cls = Class.forName("plugins.hero.ozsoft.bots." + bCls);
					// Constructor cons = cls.getConstructor(String.class);
					// Bot bot = (Bot) cons.newInstance(name);
					Bot bot = (Bot) cls.newInstance();
					bot.setPlayerName(name);
					bot.setObservationMethod(client.getString("observationMethod"));
					bot.setPokerSimulator(simulator);
					Player p = new Player(name, buyIn, bot);
					table.addPlayer(p);
				}
			}
			table.setSpeed(0);
			table.setSimulationsHand(100000);
			table.whenPlayerLose(true, Table.REFILL);
			table.whenPlayerLose(false, Table.REFILL);

			TableDialog dialog = new TableDialog(table);

			// WARNING: this method is overrided!!!
			dialog.setVisible(true);

			return table;
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

	/**
	 * Returns the value of the hole cards based on the Chen formula.
	 * 
	 * @param cards The hole cards.
	 * 
	 * @return The score based on the Chen formula.
	 */
	public static double getChenScore(HoleCards holeCards) {
		return getChenScore(
				new UoAHand(holeCards.getFirstCard().toString() + " " + holeCards.getSecondCard().toString()));
	}

	/**
	 * Returns the value of the hole cards based on the Chen formula.
	 * 
	 * @param cards The hole cards.
	 * 
	 * @return The score based on the Chen formula.
	 */
	public static double getChenScore(UoAHand hand) {
		if (hand.size() != 2) {
			throw new IllegalArgumentException("Invalid number of cards: " + hand.size());
		}

		// Analyze hole cards.
		int rank1 = hand.getCard(1).getRank();
		int suit1 = hand.getCard(2).getSuit();
		int rank2 = hand.getCard(1).getRank();
		int suit2 = hand.getCard(2).getSuit();
		int highRank = Math.max(rank1, rank2);
		int lowRank = Math.min(rank1, rank2);
		int rankDiff = highRank - lowRank;
		int gap = (rankDiff > 1) ? rankDiff - 1 : 0;
		boolean isPair = (rank1 == rank2);
		boolean isSuited = (suit1 == suit2);

		double score = 0.0;

		// 1. Base score highest rank only
		if (highRank == UoACard.ACE) {
			score = 10.0;
		} else if (highRank == UoACard.KING) {
			score = 8.0;
		} else if (highRank == UoACard.QUEEN) {
			score = 7.0;
		} else if (highRank == UoACard.JACK) {
			score = 6.0;
		} else {
			score = (highRank + 2) / 2.0;
		}

		// 2. If pair, double score, with minimum score of 5.
		if (isPair) {
			score *= 2.0;
			if (score < 5.0) {
				score = 5.0;
			}
		}

		// 3. If suited, add 2 points.
		if (isSuited) {
			score += 2.0;
		}

		// 4. Subtract points for gap.
		if (gap == 1) {
			score -= 1.0;
		} else if (gap == 2) {
			score -= 2.0;
		} else if (gap == 3) {
			score -= 4.0;
		} else if (gap > 3) {
			score -= 5.0;
		}

		// 5. Add 1 point for a 0 or 1 gap and both cards lower than a Queen.
		if (!isPair && gap < 2 && rank1 < UoACard.QUEEN && rank2 < UoACard.QUEEN) {
			score += 1.0;
		}

		// Minimum score is 0.
		if (score < 0.0) {
			score = 0.0;
		}

		// 6. Round half point scores up.
		return Math.round(score);
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
}
