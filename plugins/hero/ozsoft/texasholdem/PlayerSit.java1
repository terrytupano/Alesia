package plugins.hero.ozsoft.texasholdem;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.cardgame.*;

public class PlayerSit extends JPanel {

	private JLabel card1, card2;
	private JLabel name, chips, bets;
	private ImageIcon dealerIcon;
	private Map<String, ImageIcon> cardBuffer;

	private Player player;

	public PlayerSit(Player player, Map<String, ImageIcon> cards) {
		super();
		this.player = player;
		this.chips = new JLabel("");
		this.bets = new JLabel("");
		this.cardBuffer = cards;
		this.dealerIcon = TResources.getIcon("dealer");
		ImageIcon ii = cardBuffer.get("cardback");
		this.card1 = new JLabel();
		card1.setPreferredSize(new Dimension(ii.getIconWidth(), ii.getIconHeight()));
		this.card2 = new JLabel();
		card2.setPreferredSize(new Dimension(ii.getIconWidth(), ii.getIconHeight()));
		this.name = new JLabel(player.getName());
		updateSit();

		FormLayout layout = new FormLayout("pref, 4dlu, pref", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout, this).border(Borders.DLU2);
		builder.append(card1, card2);
		builder.append(name, 3);
		builder.append(chips, 3);
		builder.append(bets, 3);
		builder.build();
		setOpaque(false);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		name.setForeground(enabled ? Color.white:Color.black);
		chips.setForeground(enabled ? Color.white:Color.black);
		bets.setForeground(enabled ? Color.white:Color.black);
	}
	
	public void clearCards() {
		card1.setIcon(null);
		card1.setName(null);
		card2.setIcon(null);
		card2.setName(null);
	}

	public void flipCards() {
		card1.setIcon(cardBuffer.get(card1.getName()));
		card2.setIcon(cardBuffer.get(card2.getName()));
	}

	public void setPlayer(Player player) {
		this.player = player;
		updateSit();
	}

	public void updateSit() {
		setName(player.getName());
		this.chips.setText("" + player.getCash());
		this.bets.setText("" + player.getBet());
		if (player.isOut()) {
			clearCards();
		}
		UoAHand hand = player.getCards();
		if (hand.getCard(1) != null) {
			card1.setIcon(cardBuffer.get("cardback"));
			card1.setName(hand.getCard(1).toString());
		}
		if (hand.getCard(2) != null) {
			card2.setIcon(cardBuffer.get("cardback"));
			card2.setName(hand.getCard(2).toString());
		}
		if (player.getMessage() == null)
			name.setText(getName());
		else
			name.setText(player.getMessage());

		this.bets.setText(player.getBet() == 0 ? "" : ""+player.getBet());
		this.chips.setText("" + player.getCash());
		
		if(player.isFolded())
			clearCards();
		// TODO: paint dealr button
		// name.setIcon(player.isd ? dealerIcon : null);
	}
}
