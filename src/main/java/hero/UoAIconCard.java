package hero;

import java.awt.*;

import javax.swing.*;

import core.*;
import hero.UoAHandEval.*;

/**
 * simple UI component wraper for a {@link UoACard}.
 * 
 * @author terry
 *
 */
public class UoAIconCard extends JLabel {
	private UoACard uoACard;

	public UoAIconCard(UoACard card) {
		// small card size
		setPreferredSize(new Dimension(31, 38));
		setUoACard(card);
	}

	public UoACard getUoACard() {
		return uoACard;
	}

	/**
	 * set a new {@link UoACard} for this ui element.
	 * 
	 * @param card the new card for this UI or null for no card
	 */
	public void setUoACard(UoACard card) {
		this.uoACard = card;
		if (card == null)
			this.uoACard = new UoACard();
		if (uoACard.getIndex() > -1) {
			setOpaque(false);
			setIcon(TResources.getIcon(Constants.UOA_ICON_CARS + card.toString() + ".gif"));
		} else {
			setIcon(null);
			setOpaque(true);
			setBackground(Color.gray);

		}
	}
}
