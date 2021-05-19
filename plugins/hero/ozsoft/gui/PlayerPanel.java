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

package plugins.hero.ozsoft.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.extended.layout.*;

import plugins.hero.UoAHandEval.*;
import plugins.hero.ozsoft.*;
import plugins.hero.ozsoft.actions.*;

/**
 * Panel representing a player at the table.
 * 
 * @author Oscar Stigter
 */
public class PlayerPanel extends JPanel {

	/** The serial version UID. */
	private static final long serialVersionUID = 5851738752943098606L;

	/** The border. */
	private static final Border BORDER = new EmptyBorder(10, 10, 10, 10);

	/** The label with the player's name. */
	private JLabel nameLabel;

	/** The label with the player's amount of cash. */
	private JLabel cashLabel;

	/** The label with the last action performed. */
	private JLabel actionLabel;

	/** The label for the first hole card. */
	private JLabel card1Label;

	/** The label for the second hole card. */
	private JLabel card2Label;

	/** The label for the dealer button image. */
	private JLabel dealerButton;

	private JLabel getJLable() {
		JLabel jl = new JLabel(" ");
		Font nf = jl.getFont();
		nf = nf.deriveFont(((float) nf.getSize()) + 2f);
		jl.setFont(nf);
		return jl;
	}
	/**
	 * Constructor.
	 */
	public PlayerPanel() {
//		setBorder(BORDER);
		setOpaque(false);
		nameLabel = getJLable();
		cashLabel = getJLable();
		actionLabel = getJLable();
		card1Label = new JLabel(TableDialog.CARD_PLACEHOLDER_ICON);
		card2Label = new JLabel(TableDialog.CARD_PLACEHOLDER_ICON);
		dealerButton = new JLabel(TableDialog.BUTTON_ABSENT_ICON);
		dealerButton = new JLabel();

		JPanel cards = new JPanel(new GridLayout(0, 2, 5, 5));
		cards.setOpaque(false);
		cards.add(card1Label);
		cards.add(card2Label);

		setLayout(new VerticalFlowLayout());
		add(dealerButton);
		add(nameLabel);
		add(cashLabel);
		add(actionLabel);
		add(cards);
		
		setInTurn(false);
		setDealer(false);
	}

	/**
	 * Updates the panel.
	 * 
	 * @param player The player.
	 */
	public void update(Player player) {
		nameLabel.setText(player.getName());
		cashLabel.setText("$ " + player.getCash());
		int bet = player.getBet();
		PlayerAction action = player.getAction();
		if (action != null) {
			actionLabel.setText(action.getName() + " " + (bet == 0 ? "" : bet));
		} else {
			actionLabel.setText(" ");
		}
		if (player.hasCards()) {
			UoAHand hand = player.getHand();
			if (hand.size() == 2) {
				// Visible cards.
				card1Label.setIcon(TableDialog.cardsBuffer.get(hand.getCard(1).toString()));
				card2Label.setIcon(TableDialog.cardsBuffer.get(hand.getCard(2).toString()));
			} else {
				// Hidden cards (face-down).
				card1Label.setIcon(TableDialog.CARD_BACK_ICON);
				card2Label.setIcon(TableDialog.CARD_BACK_ICON);
			}
		} else {
			// No cards.
			card1Label.setIcon(TableDialog.CARD_PLACEHOLDER_ICON);
			card2Label.setIcon(TableDialog.CARD_PLACEHOLDER_ICON);
		}
	}

	/**
	 * Sets whether the player is the dealer.
	 * 
	 * @param isDealer True if the dealer, otherwise false.
	 */
	public void setDealer(boolean isDealer) {
		if (isDealer) {
			dealerButton.setIcon(TableDialog.BUTTON_PRESENT_ICON);
		} else {
			dealerButton.setIcon(TableDialog.BUTTON_ABSENT_ICON);
		}
	}

	/**
	 * Sets whether it's this player's turn to act.
	 * 
	 * @param inTurn True if it's the player's turn, otherwise false.
	 */
	public void setInTurn(boolean inTurn) {
		Color color = inTurn ? Color.WHITE : Color.BLACK;
			nameLabel.setForeground(color);
			cashLabel.setForeground(color);
			actionLabel.setForeground(color);
	}
}
