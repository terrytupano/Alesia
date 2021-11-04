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
package plugins.hero.utils;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.text.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.*;
import plugins.hero.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.UoALoky.util.*;

/**
 * panel with UI components to test {@link UoAHandEvaluator} capabilities
 * 
 * @author terry
 *
 */
public class UoAPanel extends TUIFormPanel implements ActionListener {

	// private List<UoACard> cards;
	private ArrayList<UoAIconCard> selectedICards;
	private WebButton evalHandButton;
	private WebTextArea console;

	public UoAPanel() {
		this.console = TUIUtils.getConsoleTextArea();
		this.selectedICards = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			selectedICards.add(new UoAIconCard(new UoACard()));
		}

		CardsPanel cardsPanel = new CardsPanel();
		cardsPanel.setActionListener(this);
		JPanel cardsPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		cardsPanel2.add(cardsPanel);

		evalHandButton = new WebButton("Evaluate hand");
		evalHandButton.addActionListener(this);

		FormLayout layout = new FormLayout("pref:grow, 3dlu, pref:grow",
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, fill:pref:grow");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DLU2);

		builder.append(TUIUtils.getJEditorPane(
				"<h3>UoA Hand evaluator</h3> <p>Select the hole card an the correct convination of comuniticard and press <b>evaluate hand</b>",
				null), 3);
		builder.nextLine(2);
		builder.append(cardsPanel2, 3);
		builder.nextLine(2);
		builder.append(getSelectedCardsPanel(selectedICards));
		builder.append(evalHandButton);
		builder.nextLine(2);
		builder.append(TUIUtils.getTitleLabel("titletest", "message text"), 3);
		builder.nextLine(2);
		builder.append(TUIUtils.getSmartScroller(console), 3);

		setBodyComponent(builder.getPanel());
		// registreSettings();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		// perform hand evaluation
		Object src = e.getSource();
		if (src == evalHandButton) {
			evaluateHand();
		}

		// card selected form cardsPanel
		if (src instanceof UoAIconCard) {
			UoACard uoacard = ((UoAIconCard) src).getUoACard();
			// command: add or remove
			boolean found = false;
			for (int i = 0; i < 7 && !found; i++) {
				UoAIconCard select = selectedICards.get(i);
				// add
				if (e.getActionCommand().equals("add") && select.getUoACard().getIndex() < 0) {
					found = true;
					select.setUoACard(uoacard);
				}
				// remove
				UoACard ca = select.getUoACard();
				if (e.getActionCommand().equals("remove") && uoacard.getIndex() == ca.getIndex()) {
					found = true;
					select.setUoACard(new UoACard());
				}
			}
		}
	}

	private void evaluateHand() {
		ArrayList<UoACard> selectedCopy = new ArrayList<>();
		selectedICards.forEach(ic -> selectedCopy.add(ic.getUoACard()));
		console.setText("");

		// check hole hand
		if (selectedCopy.get(0).getIndex() * selectedCopy.get(1).getIndex() < 0) {
			console.append(
					"ERROR: Hole hand must contain 0 cards (for board evaluation) OR/AND 2 cards for normal card evaluation.\n");
			return;
		}
		String holeCards = selectedCopy.remove(0) + " " + selectedCopy.remove(0);
		holeCards = holeCards.replace("1c", "").trim();
		selectedCopy.removeIf(c -> c.getIndex() < 0);

		// check comunity cards
		if (selectedCopy.size() == 1 || selectedCopy.size() == 2) {
			console.append("ERROR: Comunity card must contain 3, 4 or 5 cards.\n");
			return;
		}
		String comunityCards = PokerSimulator.parseCards(selectedCopy);
		comunityCards = comunityCards.replace("1c", "").trim();

		console.append("Hole cards: " + holeCards + " Comunity cards: " + comunityCards + "\n");

		long t = System.currentTimeMillis();
		Properties properties = PokerSimulator.getUoAEvaluation(new UoAHand(holeCards), new UoAHand(holeCards));

		// my hand evaluation
		if (!holeCards.equals("")) {
			console.append("\nMy hand evaluation:\n");
			console.append("Rank: " + properties.get("rank") + "\n");
			console.append("Name: " + properties.get("name") + "\n");
			console.append("Best: " + properties.get("bestOf5Cards") + "\n");
		}

		// Chen score
		if (!holeCards.equals("")) {
			console.append("\nChen score: " + PokerSimulator.getChenScore(new UoAHand(holeCards)) + "\n");
		}

		// board evaluation
		if (!comunityCards.equals("")) {
			console.append("\nHand Strength:\n");
			ArrayList<UoAHand> list = (ArrayList<UoAHand>) properties.get("HSAheadList");
			// String examp = Hero.parseHands(list.subList(0, Math.min(list.size(), 20)));
			String examp = PokerSimulator.parseHands(list);
			console.append("Ahead: \t" + properties.get("HSAhead") + "\t (" + properties.get("HSAhead%") + "%)\t"
					+ examp + "\n");

			list = (ArrayList<UoAHand>) properties.get("HSTiedList");
			// String examp = Hero.parseHands(list.subList(0, Math.min(list.size(), 20)));
			examp = PokerSimulator.parseHands(list);
			console.append(
					"Tied: \t" + properties.get("HSTied") + "\t (" + properties.get("HSTied%") + "%)\t" + examp + "\n");

			list = (ArrayList<UoAHand>) properties.get("HSBehindList");
			// String examp = Hero.parseHands(list.subList(0, Math.min(list.size(), 20)));
			examp = PokerSimulator.parseHands(list);
			console.append("Behind: " + properties.get("HSBehind") + "\t (" + properties.get("HSBehind%") + "%)\t"
					+ examp + "\n");

			// hand Strehent evaluator			
			console.append("PPot: \t" + properties.get("PPot") + "\n");
			console.append("NPot: \t" + properties.get("NPot") + "\n");
			console.append("winProb: \t" + properties.get("winProb") + "\n");
			console.append("isTheNut: \t" + properties.get("isTheNut") + "\n");
			
			console.append("Excetution time: " + (System.currentTimeMillis() - t) + "\n");
		}
	}

	private JPanel getSelectedCardsPanel(List<UoAIconCard> cards) {
		FormLayout layout = new FormLayout(
				"pref, 3dlu, pref, 20dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);

		builder.append(new WebLabel("My Hole cards"), 3);
		builder.append(new WebLabel("Comunity cards"), 9);
		builder.nextLine();
		for (UoAIconCard icon : cards) {
			builder.append(icon);
		}
		return builder.build();
	}
}
