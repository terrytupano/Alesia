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
package plugins.hero.UoAHandEval;

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

public class UoAPanel extends TUIFormPanel implements ActionListener {

	// private List<UoACard> cards;
	private ArrayList<IconCard> selectedICards;
	private WebButton evalHandButton;
	private WebTextArea console;

	public UoAPanel() {
		this.console = new WebTextArea();
		Font f = new Font("courier new", Font.PLAIN, 12);
		console.setFont(f);
		console.setLineWrap(false);
		this.selectedICards = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			selectedICards.add(new IconCard(new UoACard()));
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
		// DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append(TUIUtils.getJEditorPane(
				"<h3>UoA Hand evaluator</h3> <p>Select the hole card an the correct convination of comuniticard and press <b>evaluate hand</b>",
				null), 3);
		builder.nextLine(2);
		builder.append(cardsPanel2, 3);
		builder.nextLine(2);
		builder.append(getSelectedCardsPanel(selectedICards));
		builder.append(evalHandButton);
		builder.nextLine(2);
		builder.append(new WebLabel("<html><p style='font-family: Verdana; font-size: 15;'>" + "Result" + "</p>"
				+ "<p style='font-family: Tahoma; font-size: 11;'>Chechk de resutl"), 3);
		builder.nextLine(2);
		builder.append(new JScrollPane(console), 3);

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
		if (src instanceof IconCard) {
			UoACard uoacard = ((IconCard) src).getUoACard();
			// command: add or remove
			boolean found = false;
			for (int i = 0; i < 7 && !found; i++) {
				IconCard select = selectedICards.get(i);
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

	/**
	 * return the string representation of a list of cards
	 * 
	 * @param cards cards
	 * 
	 * @return
	 */
	public static String getStringOf(List<UoACard> cards) {
		StringBuffer sb = new StringBuffer();
		cards.forEach(c -> sb.append(c.toString() + " "));
		return sb.toString().trim();
	}

	private void evaluateHand() {
		ArrayList<UoACard> selectedCopy = new ArrayList<>();
		selectedICards.forEach(ic -> selectedCopy.add(ic.getUoACard()));
		console.setText("");

		// check hole hand
		if (selectedCopy.get(0).getIndex() * selectedCopy.get(1).getIndex() < 0) {
			console.append(
					"ERROR: Hole hand must contain 0 cards (for getRanks() method OR 2 cards for normal card evaluation.\n");
			return;
		}
		String holeCards = selectedCopy.remove(0) + " " + selectedCopy.remove(0);
		selectedCopy.removeIf(c -> c.getIndex() < 0);

		// check comunity cards
		if (selectedCopy.size() == 1 || selectedCopy.size() == 2) {
			console.append("ERROR: Comunity card must contain 3, 4 or 5 cards.\n");
			return;
		}
		StringBuffer sb = new StringBuffer();
		selectedCopy.forEach(ca -> sb.append(ca.toString() + " "));
		String comunityCards = sb.toString().trim();

		UoAHand hand = new UoAHand(holeCards + " " + comunityCards);
		UoAHandEvaluator evaluator = new UoAHandEvaluator();
		int handRank = UoAHandEvaluator.rankHand(hand);
		// numbers of hand better that my hand
		int count = 0;
		int[][] rowcol = evaluator.getRanks(hand);
		for (int i = 0; i < 52; i++) {
			for (int j = 0; j < 52; j++) {
				if (handRank < rowcol[i][j])
					count++;
			}
		}
		int total = 52 * 50;
		console.append("Hole cards: " + holeCards + " Comunity cards: " + comunityCards + "\n");
		console.append("\nHand evaluation resut\n");
		console.append("Rank: " + handRank + "\n");
		console.append("Name: " + UoAHandEvaluator.nameHand(hand) + "\n");
		console.append("# of posible 2 cards conbinations better that my Hole cards: " + count + " of " + total + "\n");
		double per = (count / (total * 1.0)) * 100;
		console.append("% of posible 2 cards conbinations better that my Hole cards: " + per + "\n");
	}

	private JPanel getSelectedCardsPanel(List<IconCard> cards) {
		FormLayout layout = new FormLayout(
				"pref, 3dlu, pref, 20dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);

		builder.append(new WebLabel("My Hole cards"), 3);
		builder.append(new WebLabel("Comunity cards"), 9);
		builder.nextLine();
		for (IconCard icon : cards) {
			builder.append(icon);
		}
		return builder.build();
	}
}
