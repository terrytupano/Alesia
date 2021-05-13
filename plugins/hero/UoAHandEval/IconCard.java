package plugins.hero.UoAHandEval;

import java.awt.*;

import javax.swing.*;

import core.*;

public class IconCard extends JLabel {
	private UoACard uoACard;

	public IconCard(UoACard card) {
		this.uoACard = card;
		setOpaque(true);
		setBackground(Color.gray);
		// small card size
		setPreferredSize(new Dimension(31, 38));
		setUoACard(card);
	}

	public UoACard getUoACard() {
		return uoACard;
	}
	public void setUoACard(UoACard card) {
		this.uoACard = card;
		if (card.getIndex() > -1) {
			setIcon(TResources.getIcon("cards/" + card.toString()));
		} else {
			setIcon(null);
		}
	}
}
