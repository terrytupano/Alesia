/*
 * Copyright (c) Chris 'MD87' Smith, 2007-2008. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.cardgame.ui;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.laf.button.*;
import com.alee.utils.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import plugins.hero.UoAHandEval.*;
import plugins.hero.cardgame.*;
import plugins.hero.cardgame.controllers.*;
import plugins.hero.cardgame.interfaces.*;

public class GameWindow extends JDialog implements GameObserver {

	public class PlayerSit extends JPanel {

		private JLabel card1, card2;
		private JLabel name, chips;
		private ImageIcon dealerIcon;

		public PlayerSit(Player player) {
			super();
			this.card1 = new JLabel();
			card1.setPreferredSize(new Dimension(cardWidth, cardHeight));
			this.card2 = new JLabel();
			card2.setPreferredSize(new Dimension(cardWidth, cardHeight));
			this.name = new JLabel(player.getName());
			setName(player.getName());
			this.chips = new JLabel("" + player.getCash());
			this.dealerIcon = TResources.getIcon("dealer");

			FormLayout layout = new FormLayout("pref, 4dlu, pref", "");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout, this).border(Borders.DLU2);
			builder.append(card1, card2);
			builder.append(name, 3);
			builder.append(chips, 3);
			builder.build();
			setOpaque(false);
		}

		public void clearCards() {
			card1.setIcon(null);
			card1.setName(null);
			card2.setIcon(null);
			card2.setName(null);
		}
		public void addCard(Card card) {
			if (card1.getIcon() == null) {
				card1.setIcon(cards.get("cardback"));
				card1.setName(card.toString());
			} else {
				card2.setIcon(cards.get("cardback"));
				card2.setName(card.toString());
			}
		}
		public void setChips(int chips) {
			this.chips.setText("" + chips);
		}
		public void setDealer(boolean dealer) {
			name.setIcon(dealer ? dealerIcon : null);
		}
	}
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

	/** The game we're playing. */
	private final Game game;
	/** The player whose turn it is. */
	private Player turn;

	/** The player who has an outstanding message. */
	private Player messagePlayer;
	/** A list of known winners. */
	private final List<Player> winners = new ArrayList<Player>();

	/** A position where the next card should be dealt to for each player. */
	private final Map<Player, Point> nextCardPos = new HashMap<Player, Point>();

	/** The current player message. */
	private String message;
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
	private Map<String, ImageIcon> cards = new HashMap<String, ImageIcon>();
	private Dimension preferedDimension = new Dimension(800, 600);
	private JPanel playersPanel, comunityPanel;
	private UoAHand comunityCards;

	WebButton fastButton, normalButton, slowButton, continueButton, checkButton, openButton, foldButton, discardButton;

	/**
	 * Creates a new game window for the specified game.
	 *
	 * @param game The game that this window should display
	 * @param frontStyle The folder name to use for the front of card images
	 * @param backStyle The file name to use for the back of card images
	 * @param backgroundColour The background colour to use
	 */
	public GameWindow(final Game game) {
		super(Alesia.getInstance().mainFrame);
		this.game = game;
		game.registerObserver(this);
		setSize(preferedDimension);
		loadCards();
		configureActions();
		setContentPane(configureEnviorement());
		// setDefaultCloseOperation(GameWindow.EXIT_ON_CLOSE);
	}
	@Override
	public void call(final Player player) {
		messagePlayer = player;
		message = "Calls";
		repaintAndSleep();
	}

	@Override
	public void cardDealt(final Player player, final Card card) {
		for (int i = 0; i < playersPanel.getComponentCount(); i++) {
			PlayerSit ps = (PlayerSit) playersPanel.getComponent(i);
			if (ps.getName().equals(player.getName()))
				ps.addCard(card);
		}
		repaintAndSleep();
	}
	
	@Override
	public void check(final Player player) {
		messagePlayer = player;
		message = "Checks";
		repaintAndSleep();
	}

	/** {@inheritDoc} */
	@Override
	public void communityCardsUpdated() {
		repaintAndSleep();
	}

	@Override
	public void discards(Player player, int number) {
		// TODO Auto-generated method stub
	}

	// ----------------------- OBSERVER METHODS -------------------------------

	@Override
	public void endGame() {
		repaintAndSleep();
	}

	@Override
	public void fold(final Player player) {
		messagePlayer = player;
		message = "Folds";
		if (!game.hasActiveHuman()) {
			flipAllCards();
		}
		repaintAndSleep();
	}

	@Override
	public void newGame() {
		inShowdown = false;
		winners.clear();

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

	@Override
	public void newPlayer(final Player player) {
		repaint();
	}

	@Override
	public void open(final Player player, final int amount) {
		messagePlayer = player;
		message = "Opens at " + amount;
		repaintAndSleep();
	}

	@Override
	public void placeBlind(final Player player, final int blind, final String name) {
		winners.clear();
		messagePlayer = player;
		message = "Pays " + name;
		repaintAndSleep();
	}

	/** {@inheritDoc} */
	@Override
	public void playerCardsUpdated() {
		inShowdown = false;

		repaint();

		try {
			Thread.sleep(speed / 4);
		} catch (InterruptedException ex) {
			// Do nothing
		}
	}

	@Override
	public void playersTurn(final Player player) {
		if (!player.hasFolded() && !player.isOut() && !player.isAllIn()) {
			turn = player;
			messagePlayer = null;
			repaintAndSleep();
		}
	}

	@Override
	public void raise(final Player player, final int amount) {
		messagePlayer = player;
		message = "Raises " + amount;
		repaintAndSleep();
	}

	@Override
	public void setDealer(final Player player) {
		repaintAndSleep();
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

	@Override
	public void showdown() {
		inShowdown = true;
		flipAllCards();
		repaintAndSleep();
	}

	@Override
	public void winner(final Player player) {
		winners.add(player);
		repaintAndSleep();
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
			PlayerSit ps = new PlayerSit(player);
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
		final List<Card> unflipped = new ArrayList<Card>();

		for (Player curPlayer : game.getPlayers()) {
			if (!curPlayer.isOut() && !curPlayer.hasFolded() && !curPlayer.shouldShowCards()) {
				for (Card card : curPlayer.getCards()) {
					if (!card.isPublic()) {
						unflipped.add(card);
					}
				}
			}
		}

		flipCard(false, unflipped.toArray(new Card[0]));
	}

	/**
	 * Flips the specified cards over.
	 * 
	 * @param startsVisible Whether or not the card starts visible
	 * @param cards The cards to be flipped
	 */
	private void flipCard(final boolean startsVisible, final Card... cards) {

	}

	private void loadCards() {
		ImageIcon card = TResources.getIcon("playCards/As");
		cardWidth = card.getIconWidth();
		cardHeight = card.getIconHeight();
		List<File> files = FileUtils.findFilesRecursively("/plungins/hero/card", f -> true);
		for (File file : files)
			cards.put(file.getName(), ImageUtils.getImageIcon(file));
	}

	/**
	 * Paints the turn token at the specified position.
	 * 
	 * @param g The graphics object to render the token to.
	 * @param x The x co-ordinate of the token
	 * @param y The y co-ordinate of the token
	 */
	private void paintTurnToken(final Graphics g, final int x, final int y) {
		g.setColor(Color.WHITE);
		g.fillOval(x - 12, y - 10, 10, 10);
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
