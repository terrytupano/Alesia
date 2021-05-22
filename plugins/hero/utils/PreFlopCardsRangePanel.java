package plugins.hero.utils;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.alee.extended.layout.*;
import com.alee.laf.panel.*;
/**
 *
 * @author Radu Murzea
 */
public class PreFlopCardsRangePanel extends WebPanel {
	private static final int DEFAULT_PERCENTAGE = 15;
	private JLabel[][] cardButtons;
	private JSlider slider;
	private PreflopCardsRange range;

	public PreFlopCardsRangePanel(PreflopCardsRange range) {
		super(new VerticalFlowLayout());
		this.range = (range == null) ? new PreflopCardsRange(DEFAULT_PERCENTAGE) : range;
		add(createRangePanel());
	}

	public PreflopCardsRange getRange() {
		return range;
	}

	/**
	 * return the range panel with all the cards on it
	 * 
	 * @return the cards panel
	 */
	private JPanel createRangePanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 10));
		cardButtons = new JLabel[13][13];
		JPanel cardTypesPanel = new JPanel();
		cardTypesPanel.setLayout(new GridLayout(13, 13, 2, 2));

		MouseAdapter listener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				JLabel src = (JLabel) e.getSource();
				Point coord = (Point) src.getClientProperty("Coordinate");
				range.flipValue(coord.x, coord.y);
				updateCardsButtons();
			}
		};
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				cardButtons[i][j] = new JLabel(PreflopCardsRange.rangeNames[i][j]);
				cardButtons[i][j].setHorizontalAlignment(JLabel.CENTER);
				cardButtons[i][j].putClientProperty("Coordinate", new Point(i, j));
				cardButtons[i][j].setOpaque(true);
				cardButtons[i][j].setPreferredSize(new Dimension(30, 30));
				cardButtons[i][j].setBorder(new LineBorder(Color.BLACK));
				cardButtons[i][j].addMouseListener(listener);
				cardTypesPanel.add(cardButtons[i][j]);
			}
		}
		updateCardsButtons();
		panel.add(cardTypesPanel, BorderLayout.CENTER);

		slider = new JSlider(JSlider.HORIZONTAL, 0, 100, range.getPercentage());
		slider.setMinorTickSpacing(5);
		slider.setMajorTickSpacing(20);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.getModel().setValueIsAdjusting(true);
		slider.addChangeListener(new SliderChangeListener());

		panel.add(slider, BorderLayout.SOUTH);

		return panel;
	}

	private static final Color SELECTED_COLOR = Color.CYAN;
	private static final Color SELECTED_BORDER = Color.BLUE;
	private static final Color UNSELECTED_COLOR = Color.WHITE;
	private static final Color UNSELECTED_BORDER = Color.GRAY;

	/**
	 * update the UI status for all cardsButtons
	 * 
	 */
	private void updateCardsButtons() {
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				cardButtons[i][j].setBorder(new LineBorder(range.getValue(i, j) ? SELECTED_BORDER : UNSELECTED_BORDER));
				cardButtons[i][j].setBackground(range.getValue(i, j) ? SELECTED_COLOR : UNSELECTED_COLOR);
			}
		}
	}


	private class SliderChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (!slider.getValueIsAdjusting()) {
				range.setNewPercentage(slider.getValue());
				slider.setToolTipText(""+slider.getValue());
				updateCardsButtons();
			}
		}
	}
}
