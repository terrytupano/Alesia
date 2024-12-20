package hero;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.border.*;

import com.alee.laf.panel.*;

import hero.UoAHandEval.*;

/**
 * panel with whit a matrix of all poker cards and area to set Hero Hole cards,
 * villain Hole cards and the community cards
 * 
 */
public class CardsPanel extends WebPanel {
	private ArrayList<UoAIconCard> boardCards;
	private ArrayList<UoAIconCard> deckCards;

	public CardsPanel() {
		super();
		deckCards = new ArrayList<>();
		boardCards = new ArrayList<>();
		CardMouseListener mouseListener = new CardMouseListener();

		// panel with all cards
		WebPanel cardsPanel = new WebPanel();
		cardsPanel.setLayout(new GridLayout(4, 13, 5, 5));
		cardsPanel.setBorder(new TitledBorder("Deck"));
		for (int j = 0; j < 52; j++) {
			UoAIconCard card = new UoAIconCard(new UoACard(j));
			card.addMouseListener(mouseListener);
			cardsPanel.add(card);
			deckCards.add(card);
		}

		// board cards
		for (int i = 0; i < 7; i++) {
			UoAIconCard card = new UoAIconCard(new UoACard());
			card.addMouseListener(mouseListener);
			boardCards.add(card);
		}

		WebPanel holP = new WebPanel(new GridLayout(1, 0, 5, 5));
		holP.add(boardCards.get(0));
		holP.add(boardCards.get(1));
		holP.setBorder(new TitledBorder("My Hole"));

		WebPanel comP = new WebPanel(new GridLayout(1, 0, 5, 5));
		comP.add(boardCards.get(2));
		comP.add(boardCards.get(3));
		comP.add(boardCards.get(4));
		comP.add(boardCards.get(5));
		comP.add(boardCards.get(6));
		comP.setBorder(new TitledBorder("Comunity cards"));

		WebPanel gamePanel = new WebPanel(new FlowLayout(FlowLayout.LEFT));
		gamePanel.add(holP);
		gamePanel.add(comP);

		setLayout(new BorderLayout(0, 5));
		add(cardsPanel, BorderLayout.CENTER);
		add(gamePanel, BorderLayout.SOUTH);
	}

	/**
	 * the the boar with the example cart form modeling opponent paper
	 * 
	 */
	public void setExampleFromOponetModelingPaper() {
		resetTable();
		UoAHand exam = new UoAHand("Ad Qc 3h 4c jh");
		setHand(exam);
	}

	/**
	 * set the hand in the table for simulation
	 * 
	 * @param hand
	 */
	public void setHand(UoAHand hand) {
		int idx[] = hand.getCardArray();
		int cnt = 0;
		for (int i = 1; i < idx.length - 2; i++) {
			for (UoAIconCard dcar : deckCards) {
				if (dcar.getUoACard().getIndex() == idx[i]) {
					boardCards.get(cnt++).setUoACard(dcar.getUoACard());
					dcar.setEnabled(false);
				}
			}
		}
	}

	/**
	 * clear the simulation board
	 */
	public void resetTable() {
		deckCards.forEach(jl -> jl.setEnabled(true));
		boardCards.forEach(jl -> jl.setUoACard(null));
	}

	/**
	 * set a random hand for simulation
	 */
	public void setRandomHand() {
		resetTable();
		ArrayList<UoAIconCard> list = new ArrayList<>(deckCards);
		Collections.shuffle(list);
		UoAHand hand = new UoAHand();
		hand.addCard(list.remove(0).getUoACard());
		hand.addCard(list.remove(0).getUoACard());

		hand.addCard(list.remove(0).getUoACard());
		hand.addCard(list.remove(0).getUoACard());
		hand.addCard(list.remove(0).getUoACard());
		setHand(hand);
	}

	/**
	 * return the current board status as a {@link UoAHand}. this method return null
	 * if the current board status is not valid for evaluation
	 * 
	 * @return the hand or null
	 */
	public UoAHand getHand() {
		String hole = boardCards.get(0).getUoACard() + " " + boardCards.get(1).getUoACard();
		hole = hole.replace("1c", "");
		hole = hole.trim();
		String community = boardCards.get(2).getUoACard() + " " + boardCards.get(3).getUoACard() + " "
				+ boardCards.get(4).getUoACard() + " " + boardCards.get(5).getUoACard() + " "
				+ boardCards.get(6).getUoACard();
		community = community.replace("1c", "");
		community = community.trim();

		if (hole.length() != 5 || community.length() < 8)
			return null;

		UoAHand hand = new UoAHand(hole + " " + community);
		return hand;
	}

	private class CardMouseListener extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			UoAIconCard selCard = (UoAIconCard) e.getSource();

			// deck -> board
			if (deckCards.contains(selCard)) {
				// add: find available space and insert
				if (selCard.isEnabled()) {
					UoAIconCard avaBoard = boardCards.stream().filter(uoai -> uoai.getUoACard().getIndex() == -1)
							.findFirst().orElseGet(null);
					if (avaBoard != null) {
						avaBoard.setUoACard(selCard.getUoACard());
						selCard.setEnabled(false);
					}
					// remove the previous selected cards
				} else {
					UoAIconCard psel = boardCards.stream()
							.filter(uoai -> uoai.getUoACard().getIndex() == selCard.getUoACard().getIndex()).findFirst()
							.orElseGet(null);
					if (psel != null) {
						psel.setUoACard(null);
						selCard.setEnabled(true);
					}
				}
			}

			// board -> deck
			if (boardCards.contains(selCard)) {
				if (selCard.getUoACard().getIndex() > -1) {
					// find in board the correspondent card and enabled
					UoAIconCard psel = deckCards.stream()
							.filter(uoai -> uoai.getUoACard().getIndex() == selCard.getUoACard().getIndex()).findFirst()
							.orElseGet(null);
					if (psel != null) {
						selCard.setUoACard(null);
						psel.setEnabled(true);
					}
				}
			}
		}
	}
}
