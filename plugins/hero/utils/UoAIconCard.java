package plugins.hero.utils;

import java.awt.*;

import javax.swing.*;

import core.*;
import plugins.hero.UoAHandEval.*;

/**
 * simple UI component wraper for a {@link UoACard}.
 * 
 * @author terry
 *
 */
public class UoAIconCard extends JLabel {
	private UoACard uoACard;

	public UoAIconCard(UoACard card) {
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
