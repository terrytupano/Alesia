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
import java.util.*;
import java.util.List;

import org.apache.commons.math3.stat.*;
import org.apache.commons.math3.stat.descriptive.*;

import com.alee.laf.button.*;
import com.alee.laf.grouping.*;
import com.alee.laf.panel.*;
import com.alee.laf.spinner.*;

import core.*;
import datasource.*;
import gui.*;
import hero.UoAHandEval.*;

public class UoAPanel extends TUIPanel {

	private CardsPanel cardsPanel;
	private TConsoleTextArea console;
	private WebSpinner rangeSpinner;

	public UoAPanel() {
		super();
		this.rangeSpinner = TUIUtils.getSpinner("rangeSpinner", 25, 1, 100, 5);

		// setBorder(TUIUtils.STANDAR_EMPTY_BORDER);
		this.console = new TConsoleTextArea();
		this.cardsPanel = new CardsPanel();

		WebButton evaluateHandButton = TUIUtils.getButtonForToolBar(this, "evaluateHand");
		WebButton resetTableButton = TUIUtils.getButtonForToolBar(this, "resetTable");
		WebButton setRandomHandButton = TUIUtils.getButtonForToolBar(this, "setRandomHand");
		WebButton setExampleFromOMPaperButton = TUIUtils.getButtonForToolBar(this, "setExampleFromOMPaper");
		WebButton saveCurrentHandButton = TUIUtils.getButtonForToolBar(this, "saveCurrentHand");
		WebButton loadSavedHandButton = TUIUtils.getButtonForToolBar(this, "loadSavedHand");
		WebButton testVariableButton = TUIUtils.getButtonForToolBar(this, "testVariable");

		GroupPane pane = new GroupPane(rangeSpinner, evaluateHandButton, setRandomHandButton,
				setExampleFromOMPaperButton, resetTableButton, saveCurrentHandButton, loadSavedHandButton,
				testVariableButton);

		getToolBar().add(pane);
		WebPanel webPanel = new WebPanel(new BorderLayout());
		webPanel.add(cardsPanel, BorderLayout.NORTH);
		webPanel.add(TUIUtils.getSmartScroller(console), BorderLayout.CENTER);

		setBodyComponent(webPanel);
	}

	@org.jdesktop.application.Action
	public void evaluateHand(ActionEvent event) {
		evaluateHandImpl();
	}

	@org.jdesktop.application.Action
	public void resetTable(ActionEvent event) {
		cardsPanel.resetTable();
		console.clear();
	}

	@org.jdesktop.application.Action
	public void testVariable(ActionEvent event) {
		Frequency frequency = new Frequency();
		DescriptiveStatistics statistics = new DescriptiveStatistics();
		console.clear();
		UoAHand holeCards = new UoAHand();
		UoAHand comunityCards = new UoAHand();
		UoADeck deck = new UoADeck();
		for (int i = 0; i < 1_000; i++) {
			deck.reset();
			deck.shuffle();
			holeCards.makeEmpty();
			comunityCards.makeEmpty();
			holeCards.addCard(deck.deal().getIndex());
			holeCards.addCard(deck.deal().getIndex());
			comunityCards.addCard(deck.deal().getIndex());
			comunityCards.addCard(deck.deal().getIndex());
			comunityCards.addCard(deck.deal().getIndex());

			// which snip will be executed
			String snip = "handTexture";

			if ("handTexture".equals(snip)) {
				Map<String, Object> result = PokerSimulator.getEvaluation(holeCards, comunityCards);
				int texture = (int) result.get("handTexture");
				statistics.addValue(texture);
				if (texture > 0)
					frequency.addValue(texture);
				System.out.println(holeCards + " " + comunityCards + "\t\t" + texture);
			}

			if ("outs".equals(snip)) {
				// simple code to evaluate how many outs are out there
				Map<String, Object> evaluation = PokerSimulator.getOuts(holeCards, comunityCards);
				int outs = (int) evaluation.get("outs");
				statistics.addValue(outs);
				frequency.addValue(outs);
				if (outs > 20) {
					System.out.println("Outs: " + outs + " cardsDealed " + holeCards + " " + comunityCards + " "
							+ evaluation.get("outsExplanation"));
				}
			}

		}
		for (int i = 0; i < 30; i++) {
			// System.out.printf("%-3s\t%-5s\n", i, frequency.getCount(i));
		}

		Map<String, Object> map = new HashMap<>();
		map.put("min", statistics.getMin());
		map.put("mean", statistics.getMean());
		map.put("max", statistics.getMax());
		map.put("frequency", frequency.getMode());

		console.print(map);
	}

	@org.jdesktop.application.Action
	public void setRandomHand(ActionEvent event) {
		cardsPanel.setRandomHand();
		evaluateHandImpl();
	}

	@org.jdesktop.application.Action
	public void setExampleFromOMPaper(ActionEvent event) {
		cardsPanel.setExampleFromOponetModelingPaper();
		evaluateHandImpl();
	}

	@org.jdesktop.application.Action
	public void saveCurrentHand(ActionEvent event) {
		UoAHand hand = cardsPanel.getHand();
		if (hand == null) {
			Alesia.showNotification("hero.msg09");
			return;
		}
		Property property = Property.first("tkey = ?", "UoAPanel.savedCards");
		if (property == null)
			property = new Property();

		property.set("tkey", "UoAPanel.savedCards", "tvalue", hand.toString());
		property.save();
		Alesia.showNotification("hero.msg08");
	}

	@org.jdesktop.application.Action
	public void loadSavedHand(ActionEvent event) {
		Property property = Property.first("tkey = ?", "UoAPanel.savedCards");
		if (property != null) {
			UoAHand hand = new UoAHand(property.getString("tvalue"));
			cardsPanel.setHand(hand);
		}
	}

	private void evaluateHandImpl() {
		UoAHand hand = cardsPanel.getHand();
		console.clear();
		if (hand == null) {
			Alesia.showNotification("hero.msg09");
			return;
		}

		String community = hand.getCard(3) + " " + hand.getCard(4) + " " + hand.getCard(5) + " " + hand.getCard(6) + " "
				+ hand.getCard(7);
		community = community.replace("1c", "");
		community = community.trim();

		UoAHand holeCards = new UoAHand(hand.getCard(1) + " " + hand.getCard(2));
		UoAHand comunityCards = new UoAHand(community);

		console.append("Hole cards: " + holeCards + " Comunity cards: " + comunityCards + "\n");
		int preflopRange = (Integer) rangeSpinner.getValue();

		Map<String, Object> evaluation = PokerSimulator.getEvaluation(holeCards, comunityCards);

		// all elements instance of List are array of uoAHand. override this property
		// and show only a sublist
		Set<String> keys = evaluation.keySet();
		for (String key : keys) {
			if (evaluation.get(key) instanceof List) {
				@SuppressWarnings("unchecked")
				List<UoAHand> l = (List<UoAHand>) evaluation.get(key);
				String examp = PokerSimulator.parseHands(l.subList(0, Math.min(l.size(), 10)));
				evaluation.put(key, examp);
			}
		}

		console.print(evaluation);
	}
}
