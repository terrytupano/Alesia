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

import java.awt.event.*;
import java.util.*;

import com.alee.laf.text.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.*;
import plugins.hero.*;
import plugins.hero.UoAHandEval.*;

public class UoAPanel extends TUIFormPanel implements ActionListener {

	private CardsPanel cardsPanel;
	private WebTextArea console;

	public UoAPanel() {
		this.console = TUIUtils.getConsoleTextArea();

		cardsPanel = new CardsPanel();
		cardsPanel.setActionListener(this);
		// JPanel cardsPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		// cardsPanel2.add(cardsPanel);

		FormLayout layout = new FormLayout("pref:grow, 3dlu, pref:grow", "p, 3dlu, fill:pref:grow");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DLU2);

//		builder.append(TUIUtils.getTitleLabel("Single Hand simulation",
//				"Select the hole card an the comunity cards. press <b>Evaluate hand</b> when ready"), 3);
//		builder.nextLine(2);
		builder.append(cardsPanel);
//		builder.nextLine(2);
//		builder.append(TUIUtils.getTitleLabel("Evaluation Result", ""), 3);
		builder.nextLine(2);
		builder.append(TUIUtils.getSmartScroller(console), 3);

		setBodyComponent(builder.getPanel());
		// registreSettings();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Hashtable<String, Object> parms = cardsPanel.getGameCards();
		console.setText("");

		UoAHand myHole = (UoAHand) parms.get("myHole");
		// check hole hand
		if (myHole.size() == 1) {
			console.append(
					"ERROR: Hole hand must contain 0 cards (for board evaluation) OR 2 cards for normal card evaluation.\n");
			return;
		}

		UoAHand comunity = (UoAHand) parms.get("comunityCards");
		// check comunity cards
		if (comunity.size() < 3) {
			console.append("ERROR: Comunity card must contain 3, 4 or 5 cards.\n");
			return;
		}

		console.append("Hole cards: " + myHole + " Comunity cards: " + comunity + "\n");
		int tau = (Integer) parms.get("tau");

		long t = System.currentTimeMillis();
		Properties properties = PokerSimulator.getEvaluation(myHole, comunity, 1, tau, 10000, 1000);
		// Add Chen score
		properties.put("Chen Score", PokerSimulator.getChenScore(myHole));

		// to sort the list
		TreeMap<Object, Object> tm = new TreeMap<>(properties);
//		DecimalFormat probFormat = new DecimalFormat("#0.000");

		// all elements instance of List are array of cards. override this property
		Set keys = tm.keySet();
		for (Object key : keys) {
			if (tm.get(key) instanceof List) {
				List l = (List) tm.get(key);
				String examp = PokerSimulator.parseHands(l.subList(0, Math.min(l.size(), 10)));
				tm.put(key, examp);
			}
		}
		// console.append("\nEvaluation\n");
		String patt = "%-15s %-50s";
		// console.append("\n" + String.format(patt, "Property", "Value"));
		tm.forEach((key, value) -> console.append("\n" + String.format(patt, key, value)));

		console.append("\nExcetution time: " + (System.currentTimeMillis() - t));
	}
}
