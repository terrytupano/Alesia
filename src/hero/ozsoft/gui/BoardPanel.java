// This file is part of the 'texasholdem' project, an open source
// Texas Hold'em poker application written in Java.
//
// Copyright 2009 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package hero.ozsoft.gui;

import java.awt.*;

import javax.swing.*;

import com.alee.extended.layout.*;

import core.*;
import hero.*;
import hero.UoAHandEval.*;

/**
 * Board panel with the community cards and general information.
 * 
 * @author Oscar Stigter
 */
public class BoardPanel extends JPanel {

	/** The serial version UID. */
	private static final long serialVersionUID = 8530615901667282755L;

	/** The maximum number of community cards. */
	private static final int NO_OF_CARDS = 5;

	/** Label with the bet. */
	private final JLabel betLabel;

	/** Label with the pot. */
	private final JLabel potLabel;

	/** Labels with the community cards. */
	private final JLabel[] cardLabels;

	/**
	 * Constructor.
	 * 
	 * @param controlPanel The control panel.
	 */
	public BoardPanel() {

		setBorder(UIConstants.PANEL_BORDER);
		setOpaque(false);
		setLayout(new VerticalFlowLayout(5));

		betLabel = new JLabel("Bets: ");
		betLabel.setHorizontalAlignment(JLabel.CENTER);
		potLabel = new JLabel("Pot: ");
		potLabel.setHorizontalAlignment(JLabel.CENTER);

		// Font font = potLabel.getFont();
		// font = font.deriveFont(font.getSize() + 3);
		// betLabel.setFont(font);
		potLabel.setFont(potLabel.getFont().deriveFont(14f));

		JPanel labels = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		labels.setOpaque(false);
		labels.add(potLabel);
		// labels.add(betLabel);

		// The five card positions.
		JPanel cards = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
		cards.setOpaque(false);
		cardLabels = new JLabel[NO_OF_CARDS];
		for (int i = 0; i < 5; i++) {
			cardLabels[i] = new JLabel(Constants.CARD_PLACEHOLDER_ICON);
			cards.add(cardLabels[i]);
		}
		add(labels);
		add(Box.createVerticalStrut(20));
		add(cards);
		setPreferredSize(new Dimension(400, 150));

		update(null, 0, 0);
	}

	/**
	 * Updates the current hand status.
	 * 
	 * @param bet The bet.
	 * @param pot The pot.
	 */
	public void update(UoAHand hand, int bet, int pot) {
		if (bet == 0) {
			betLabel.setText("Bets: ");
		} else {
			betLabel.setText("Bets: " + bet);
		}
		if (pot == 0) {
			potLabel.setText("Pot: ");
		} else {
			potLabel.setText("Pot: " + pot);
		}
		int noOfCards = (hand == null) ? 0 : hand.size();
		for (int i = 0; i < NO_OF_CARDS; i++) {
			if (i < noOfCards) {
				cardLabels[i].setIcon(TResources.getIcon(Constants.PLAY_CARDS + hand.getCard(i + 1)+".png"));
			} else {
				cardLabels[i].setIcon(Constants.CARD_PLACEHOLDER_ICON);
			}
		}
	}
}
