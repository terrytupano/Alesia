/*
 * Copyright (c) Chris 'MD87' Smith, 2007-2008. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.ozsoft.texasholdem;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.laf.button.*;
import com.alee.utils.*;

import core.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.cardgame.controllers.*;

public class GameWindow extends JDialog implements Client {

	/** The delay between events for the "fast" speed. */
	public final static int SPEED_FAST = 200;
	/** The delay between events for the "normal" speed. */
	public final static int SPEED_NORMAL = 1000;

	/** The delay between events for the "slow" speed. */
	public final static int SPEED_SLOW = 5000;
	private int cardWidth;
	private int cardHeight;

	private Color backgroundColour = new Color(0, 100, 0);

	/** The current speed. */
	private int speed = SPEED_NORMAL;

	/** The player whose turn it is. */
	private Player turn;

	/** The human player who we're waiting for to chose his move. */
	private HumanPlayer player = null;
	/** The human player who we're waiting for to continue. */
	private HumanPlayer waitPlayer = null;
	/** The human player who we're waiting for to discard. */
	private HumanPlayer discardPlayer = null;
	/** Whether the current player can fold. */
	private boolean canFold = false;
	/** Whether the current player can raise. */
	private boolean canRaise = true;
	/** Whether or not we're in showdown. */
	private boolean inShowdown = false;
	/** A cache of card images. */
	private Map<String, ImageIcon> cardsBuffer = new HashMap<String, ImageIcon>();
	private Dimension preferedDimension = new Dimension(800, 600);
	private JPanel playersPanel, comunityPanel;

	WebButton fastButton, normalButton, slowButton, continueButton, checkButton, openButton, foldButton, discardButton;

	/**
	 * Creates a new game window for the specified game.
	 *
	 * @param game The game that this window should display
	 * @param frontStyle The folder name to use for the front of card images
	 * @param backStyle The file name to use for the back of card images
	 * @param backgroundColour The background colour to use
	 */
	public GameWindow() {
		super(Alesia.getInstance().mainFrame);
		setSize(preferedDimension);
		loadCards();
		configureActions();
		setContentPane(configureEnviorement());
		// setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	@Override
	public void gameStateChanged(String state, Player player, Object value) {

		if ("showdown".equals(state)) {
			flipAllCards();
			repaintAndSleep();
		}

		// show the community cards
		if ("communityCardsUpdated".equals(state)) {
			UoAHand hand = game.getCommunityCards();
			if (hand.size() > 2) {
				JLabel flop1 = (JLabel) comunityPanel.getComponent(0);
				flop1.setIcon(cardsBuffer.get(hand.getCard(1).toString()));
				flop1.setName(hand.getCard(1).toString());
				JLabel flop2 = (JLabel) comunityPanel.getComponent(1);
				flop2.setIcon(cardsBuffer.get(hand.getCard(2).toString()));
				flop2.setName(hand.getCard(2).toString());
				JLabel flop3 = (JLabel) comunityPanel.getComponent(2);
				flop3.setIcon(cardsBuffer.get(hand.getCard(3).toString()));
				flop3.setName(hand.getCard(3).toString());
			}
			if (hand.size() > 3) {
				JLabel turn = (JLabel) comunityPanel.getComponent(3);
				turn.setIcon(cardsBuffer.get(hand.getCard(4).toString()));
				turn.setName(hand.getCard(4).toString());
			}
			if (hand.size() > 4) {
				JLabel river = (JLabel) comunityPanel.getComponent(4);
				river.setIcon(cardsBuffer.get(hand.getCard(5).toString()));
				river.setName(hand.getCard(5).toString());
			}
			repaintAndSleep();
		}
		//
		if ("winners".equals(state)) {
			// TODO: update ui with the resutl
		}

		// set all elements for a new game
		if ("newGame".equals(state)) {
			inShowdown = false;

			// clear players
			for (int i = 0; i < playersPanel.getComponentCount(); i++) {
				PlayerSit ps = (PlayerSit) playersPanel.getComponent(i);
				ps.clearCards();
			}

			// clear comunity
			for (int i = 0; i < comunityPanel.getComponentCount(); i++) {
				JLabel ps = (JLabel) comunityPanel.getComponent(i);
				ps.setIcon(null);
				ps.setName(null);
			}
			repaintAndSleep();
		}

		// hihgt the player
		if (state.startsWith("action_")) {
			PlayerSit curPs = null;
			for (int i = 0; i < playersPanel.getComponentCount(); i++) {
				PlayerSit ps = (PlayerSit) playersPanel.getComponent(i);
				ps.setEnabled(player.getName().equals(ps.getName()));
				if (player.getName().equals(ps.getName()))
					curPs = ps;
			}
		}

		// end the game
		if ("endGame".equals(state)) {
			repaintAndSleep();
		}

		// for all action where the player argument is not null, update the playerUI
		if (player != null) {
			findPlayerSit(player).updateSit();
			// no wait time for this action
			if (!"cardDeal".equals(state))
				repaintAndSleep();
		}
	}

	/**
	 * locate the sit on the table of a player
	 * 
	 * @param player - the player
	 * 
	 * @return the sit
	 */
	private PlayerSit findPlayerSit(Player player) {
		PlayerSit sit = null;
		for (int i = 0; i < playersPanel.getComponentCount(); i++) {
			PlayerSit ps = (PlayerSit) playersPanel.getComponent(i);
			if (player.getName().equals(ps.getName()))
				sit = ps;
		}
		return sit;
	}

	/**
	 * Indicates that a human player needs to make a decision as to whether to call/fold/raise.
	 *
	 * @param player The player who has to make the decision
	 * @param canFold Whether or not the player can fold
	 * @param canRaise Whether or not the player can raise
	 */
	public void setHumanPlayer(final HumanPlayer player, final boolean canFold, final boolean canRaise) {
		this.player = player;
		this.canFold = canFold;
		this.canRaise = canRaise;
		repaint();
	}

	/**
	 * Indicates that a human player controller is waiting for the player to indicate that it's OK to continue.
	 *
	 * @param player The player who has to make the indication
	 */
	public void setWaitPlayer(final HumanPlayer player) {
		this.waitPlayer = player;
		repaint();
	}

	private void configureActions() {
		fastButton = new WebButton("Fast");
		fastButton.addActionListener(ap -> speed = SPEED_FAST);
		normalButton = new WebButton("Normal");
		normalButton.addActionListener(ap -> speed = SPEED_NORMAL);
		slowButton = new WebButton("Slow");
		slowButton.addActionListener(ap -> speed = SPEED_SLOW);
		continueButton = new WebButton("Continue");
		slowButton.addActionListener(ap -> waitPlayer.notifyAll());
		checkButton = new WebButton("Check");
		slowButton.addActionListener(ap -> {
			player.move = 0;
			player.notifyAll();
		});
		openButton = new WebButton("Open");
		openButton.addActionListener(ap -> {
			player.move = 1;
			player.notifyAll();
		});
		foldButton = new WebButton("Fold");
		foldButton.addActionListener(ap -> {
			player.move = 2;
			player.notifyAll();
		});

		discardButton = new WebButton("discard");
	}

	// ------------------ HUMAN PLAYER INTERFACE ------------------------------

	private JPanel configureEnviorement() {

		// players
		this.playersPanel = new JPanel(new CircleLayout());
		playersPanel.setOpaque(false);
		playersPanel.setBounds(0, 0, preferedDimension.width, preferedDimension.height);
		for (Player player : game.getPlayers()) {
			PlayerSit ps = new PlayerSit(player, cardsBuffer);
			ps.setBorder(new LineBorder(Color.BLACK));
			playersPanel.add(ps);
		}

		// comunity cards
		this.comunityPanel = new JPanel(new GridLayout(0, 5, 5, 5));
		comunityPanel.setOpaque(false);
		for (int i = 0; i < 5; i++) {
			JLabel jl = new JLabel();
			jl.setPreferredSize(new Dimension(cardWidth, cardHeight));
			jl.setBorder(new LineBorder(Color.BLACK));
			comunityPanel.add(jl);
		}
		int cx = preferedDimension.width / 2 - comunityPanel.getPreferredSize().width / 2;
		int cy = preferedDimension.height / 2 - comunityPanel.getPreferredSize().height / 2;
		comunityPanel.setBounds(cx, cy, comunityPanel.getPreferredSize().width,
				comunityPanel.getPreferredSize().height);

		// JPanel jp1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 4));
		// jp1.setOpaque(false);
		// jp1.setBounds(preferedDimension.width / 2, preferedDimension.height / 2, preferedDimension.width,
		// preferedDimension.height);
		// jp1.add(comunityPanel);

		// cardpanel with comunity and players panel
		JPanel main = new JPanel(null);
		main.setBounds(0, 0, preferedDimension.width, preferedDimension.height);
		main.setBackground(backgroundColour);
		// hight number, paint firs
		main.setComponentZOrder(comunityPanel, 0);
		main.setComponentZOrder(playersPanel, 1);

		return main;
	}

	/**
	 * Flips all cards over.
	 */
	private void flipAllCards() {
		for (Component cmp : playersPanel.getComponents()) {
			((PlayerSit) cmp).flipCards();
		}
	}

	private void loadCards() {
		ImageIcon card = TResources.getIcon("playCards/cardback");
		cardWidth = card.getIconWidth();
		cardHeight = card.getIconHeight();
		List<File> files = FileUtils.findFilesRecursively("plugins/hero/resources/playCards", f -> true);
		for (File file : files) {
			cardsBuffer.put(file.getName().substring(0, file.getName().length() - 4), ImageUtils.getImageIcon(file));
		}
	}

	/**
	 * Requests a repaint, and sleeps for a period of time based on the speed of the game.
	 */
	private void repaintAndSleep() {
		repaint();
		try {
			Thread.sleep(speed);
		} catch (InterruptedException ex) {
			// Do nothing
		}
	}
}
