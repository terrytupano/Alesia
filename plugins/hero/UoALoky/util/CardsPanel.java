package plugins.hero.UoALoky.util;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import plugins.hero.UoAHandEval.*;
import plugins.hero.utils.*;

/**
 * panel with cart to select.
 * 
 */
public class CardsPanel extends JPanel {
	private ArrayList<UoAIconCard> iconCards;
	private ActionListener actionListener;

	public CardsPanel() {
		super(new GridLayout(4, 13, 5, 5));
		iconCards = new ArrayList<>();
		for (int j = 0; j < 52; j++) {
			UoAIconCard card = new UoAIconCard(new UoACard(j));
			card.addMouseListener(new CardMouseListener());
			add(card);
			iconCards.add(card);
		}
	}

	public void setActionListener(ActionListener actionListener) {
		this.actionListener = actionListener;
	}

	/**
	 * create a {@link JLabel} whit the visual represetation of a {@link UoACard}. the name of this componente is the
	 * string represtation of a card
	 * 
	 * @param aCard
	 * @return
	 */

	/**
	 * restore the card. if the card was selected, remark as available.
	 * 
	 * @param uoACard - card public void restoreCard(String card) { for (JLabel jLabel : cards) { if (jLabel.getName()
	 *        != null & jLabel.getName().equals(card)) jLabel.setEnabled(true); } }
	 */

	private class CardMouseListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			UoAIconCard card = (UoAIconCard) e.getSource();
			boolean isEnable = card.isEnabled();
			card.setEnabled(true);
			// if action is selection and all 7 card has ben selected, do nothng
			long selc = iconCards.stream().filter(jl -> !jl.isEnabled()).count();
			if (isEnable && selc < 7) {
				actionListener.actionPerformed(new ActionEvent(card, 1, "add"));
				card.setEnabled(false);
			}
			if (!isEnable) {
				actionListener.actionPerformed(new ActionEvent(card, 1, "remove"));
				card.setEnabled(true);
			}
		}
	}
}
