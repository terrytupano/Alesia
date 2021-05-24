package plugins.hero.utils;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.jdesktop.application.*;

import com.alee.extended.layout.*;
import com.alee.laf.combobox.*;
import com.alee.laf.panel.*;
import com.alee.managers.settings.*;

import core.*;
import gui.*;

public class PreFlopCardsRangePanel extends TUIPanel {
	private static final int DEFAULT_PERCENTAGE = 15;
	private JLabel[][] cardButtons;
	private JSlider slider;
	private PreflopCardsRange preflopCardsRange;
	private WebComboBox rangeComboBox;

	public PreflopCardsRange getPreflopCardsRange() {
		return preflopCardsRange;
	}

	public PreFlopCardsRangePanel() {
		super();
		this.rangeComboBox = new WebComboBox(PreflopCardsRange.getSavedCardsRanges());
		TUIUtils.setDimensionForTextComponent(rangeComboBox, 40);
		rangeComboBox.addActionListener(evt -> updateRange());
		ResourceMap r = Alesia.getInstance().getContext().getResourceManager().getResourceMap();
		r.injectComponent(this);
		preflopCardsRange = new PreflopCardsRange(DEFAULT_PERCENTAGE);
		WebPanel panel = new WebPanel(new VerticalFlowLayout());
		panel.add(createRangePanel());
		rangeComboBox.registerSettings(new Configuration<ComboBoxState>(getClass().getName() + ".rangeComboBox"));
		addToolBarActions("savePreflopRange");
		getToolBarPanel().add(rangeComboBox);
		setBodyComponent(panel);
	}

	private void updateRange() {
		TEntry<String, String> te = (TEntry<String, String>) rangeComboBox.getSelectedItem();
		preflopCardsRange = PreflopCardsRange.loadFromDB(te.getKey());
		if (preflopCardsRange == null) {
			preflopCardsRange = new PreflopCardsRange(DEFAULT_PERCENTAGE);
		}
		slider.setValue(preflopCardsRange.getPercentage());
		updateCardsButtons();
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
				preflopCardsRange.flipValue(coord.x, coord.y);
				updateCardsButtons();
			}
		};
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				cardButtons[i][j] = new JLabel(preflopCardsRange.getCardAt(i, j));
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

		slider = new JSlider(JSlider.HORIZONTAL, 0, 100, preflopCardsRange.getPercentage());
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
				cardButtons[i][j].setBorder(
						new LineBorder(preflopCardsRange.getValue(i, j) ? SELECTED_BORDER : UNSELECTED_BORDER));
				cardButtons[i][j].setBackground(preflopCardsRange.getValue(i, j) ? SELECTED_COLOR : UNSELECTED_COLOR);
			}
		}
	}

	private class SliderChangeListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent e) {
			if (!slider.getValueIsAdjusting()) {
				preflopCardsRange.setNewPercentage(slider.getValue());
				slider.setToolTipText("" + slider.getValue());
				updateCardsButtons();
			}
		}
	}
}
