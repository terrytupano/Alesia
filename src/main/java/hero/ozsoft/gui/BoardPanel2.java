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

import core.*;
import hero.*;
import hero.UoAHandEval.*;

/**
 * Board panel with the community cards and general information.
 * 
 * @author Oscar Stigter
 */
public class BoardPanel2 extends JPanel {

	/** The serial version UID. */
	private static final long serialVersionUID = 8530615901667282755L;

	/** The maximum number of community cards. */
	private static final int NO_OF_CARDS = 5;

	/** The control panel. */
	private final ControlPanel controlPanel;

	/** Label with the bet. */
	private final JLabel betLabel;

	/** Label with the pot. */
	private final JLabel potLabel;

	/** Labels with the community cards. */
	private final JLabel[] cardLabels;

	/** Label with a custom message. */
	private final JLabel messageLabel;

	/**
	 * Constructor.
	 * 
	 * @param controlPanel The control panel.
	 */
	public BoardPanel2(ControlPanel controlPanel) {
		this.controlPanel = controlPanel;

		setBorder(UIConstants.PANEL_BORDER);
		setBackground(UIConstants.TABLE_COLOR);
		setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();

		JLabel label = new JLabel("Bet");
		label.setForeground(Color.GREEN);
		gc.gridx = 1;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.NONE;
		gc.weightx = 1.0;
		gc.weighty = 0.0;
		gc.insets = new Insets(0, 5, 0, 5);
		add(label, gc);

		label = new JLabel("Pot");
		label.setForeground(Color.GREEN);
		gc.gridx = 3;
		gc.gridy = 0;
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.NONE;
		gc.weightx = 1.0;
		gc.weighty = 0.0;
		gc.insets = new Insets(0, 5, 0, 5);
		add(label, gc);

		betLabel = new JLabel(" ");
		betLabel.setBorder(UIConstants.LABEL_BORDER);
		betLabel.setForeground(Color.GREEN);
		betLabel.setHorizontalAlignment(JLabel.CENTER);
		gc.gridx = 1;
		gc.gridy = 1;
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 1.0;
		gc.weighty = 0.0;
		gc.insets = new Insets(5, 5, 5, 5);
		add(betLabel, gc);

		potLabel = new JLabel(" ");
		potLabel.setBorder(UIConstants.LABEL_BORDER);
		potLabel.setForeground(Color.GREEN);
		potLabel.setHorizontalAlignment(JLabel.CENTER);
		gc.gridx = 3;
		gc.gridy = 1;
		gc.gridwidth = 1;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 1.0;
		gc.weighty = 0.0;
		gc.insets = new Insets(5, 5, 5, 5);
		add(potLabel, gc);

		// The five card positions.
		cardLabels = new JLabel[NO_OF_CARDS];
		for (int i = 0; i < 5; i++) {
			// cardLabels[i] = new JLabel(ResourceManager.getIcon("images/card_placeholder.png"));
			cardLabels[i] = new JLabel(Constants.CARD_PLACEHOLDER_ICON);
			gc.gridx = i;
			gc.gridy = 2;
			gc.gridwidth = 1;
			gc.gridheight = 1;
			gc.anchor = GridBagConstraints.CENTER;
			gc.fill = GridBagConstraints.NONE;
			gc.weightx = 0.0;
			gc.weighty = 0.0;
			gc.insets = new Insets(5, 1, 5, 1);
			add(cardLabels[i], gc);
		}

		// Message label.
		messageLabel = new JLabel();
		messageLabel.setForeground(Color.YELLOW);
		messageLabel.setHorizontalAlignment(JLabel.CENTER);
		gc.gridx = 0;
		gc.gridy = 3;
		gc.gridwidth = 5;
		gc.gridheight = 1;
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		gc.insets = new Insets(0, 0, 0, 0);
		add(messageLabel, gc);

		// Control panel.
		gc.gridx = 0;
		gc.gridy = 4;
		gc.gridwidth = 5;
		gc.gridheight = 1;
		gc.insets = new Insets(0, 0, 0, 0);
		gc.anchor = GridBagConstraints.CENTER;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 1.0;
		gc.weighty = 1.0;
		add(controlPanel, gc);

		setPreferredSize(new Dimension(400, 270));

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
			betLabel.setText(" ");
		} else {
			betLabel.setText("$ " + bet);
		}
		if (pot == 0) {
			potLabel.setText(" ");
		} else {
			potLabel.setText("$ " + pot);
		}
		int noOfCards = (hand == null) ? 0 : hand.size();
		for (int i = 0; i < NO_OF_CARDS; i++) {
			if (i < noOfCards) {
				cardLabels[i].setIcon(TResources.getIcon("playCards/" + hand.getCard(i + 1)));
			} else {
				cardLabels[i].setIcon(Constants.CARD_PLACEHOLDER_ICON);
			}
		}
	}

	/**
	 * Sets a custom message.
	 * 
	 * @param message The message.
	 */
	public void setMessage(String message) {
		if (message.length() == 0) {
			messageLabel.setText(" ");
		} else {
			messageLabel.setText(message);
			System.out.println(message);
		}
	}

	/**
	 * Waits for the user to continue.
	 */
	public void waitForUserInput() {
		controlPanel.waitForUserInput();
	}

}
