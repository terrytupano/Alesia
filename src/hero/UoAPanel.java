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

import com.alee.laf.button.*;
import com.alee.laf.grouping.*;
import com.alee.laf.panel.*;
import com.alee.laf.spinner.*;

import core.*;
import gui.*;
import hero.UoAHandEval.*;

public class UoAPanel extends TUIPanel {

	private CardsPanel cardsPanel;
	private TConsoleTextArea console;
	private WebSpinner tauSpinner;

	public UoAPanel() {
		super();
		this.tauSpinner = TUIUtils.getSpinner("tau", 15, 0, 100, 5);

//		setBorder(TUIUtils.STANDAR_EMPTY_BORDER);
		this.console = new TConsoleTextArea();
		this.cardsPanel = new CardsPanel();

		WebButton evaluateHandButton = TUIUtils.getButtonForToolBar(this, "evaluateHand");
		WebButton resetTableButton = TUIUtils.getButtonForToolBar(this, "resetTable");
		WebButton setRandomHandButton = TUIUtils.getButtonForToolBar(this, "setRandomHand");
		WebButton setExampleFromOMPaperButton = TUIUtils.getButtonForToolBar(this, "setExampleFromOMPaper");

		GroupPane pane = new GroupPane(tauSpinner, evaluateHandButton, setRandomHandButton, setExampleFromOMPaperButton, resetTableButton);

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
	public void setRandomHand(ActionEvent event) {
		cardsPanel.setRandomHand();
		evaluateHandImpl();
	}

	@org.jdesktop.application.Action
	public void setExampleFromOMPaper(ActionEvent event) {
		cardsPanel.setExampleFromOponetModelingPaper();
		evaluateHandImpl();
	}

	private void evaluateHandImpl() {
		Hashtable<String, Object> parms = cardsPanel.getGameCards();
		console.clear();

		UoAHand myHole = (UoAHand) parms.get("myHole");
		// check hole hand
		if (myHole.size() == 1) {
			console.append(
					"ERROR\nHole hand must contain 0 cards (for board evaluation)\nOR 2 cards for normal card evaluation.\n");
			return;
		}

		UoAHand comunity = (UoAHand) parms.get("comunityCards");
		// check community cards
		if (comunity.size() < 3) {
			console.append("ERROR\nComunity card must contain 3, 4 or 5 cards.\n");
			return;
		}

		console.append("Hole cards: " + myHole + " Comunity cards: " + comunity + "\n");
		int bigBlinds = (Integer) tauSpinner.getValue();

		Map<String, Object> evaluation = PokerSimulator.getEvaluation(myHole, comunity, 1, bigBlinds);
		// Add Chen score
		evaluation.put("Chen Score", PokerSimulator.getChenScore(myHole));

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
