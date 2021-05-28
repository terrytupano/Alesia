package plugins.hero.utils;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

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
	private static final Color SELECTED_COLOR = Color.CYAN;
	private static final Color SELECTED_BORDER = Color.BLUE;
	private static final Color UNSELECTED_COLOR = Color.WHITE;
	private static final Color UNSELECTED_BORDER = Color.GRAY;

	private JLabel[][] cardButtons;
	private JSlider slider;
	private PreflopCardsRange preflopCardsRange;
	private WebComboBox rangeComboBox;

	public PreFlopCardsRangePanel() {
		super();
		this.rangeComboBox = new WebComboBox(PreflopCardsRange.getSavedCardsRanges());
		TUIUtils.setDimensionForTextComponent(rangeComboBox, 40);
		rangeComboBox.addActionListener(evt -> loadFromDB());
		ResourceMap r = Alesia.getInstance().getContext().getResourceManager().getResourceMap();
		r.injectComponent(this);
		preflopCardsRange = new PreflopCardsRange();
		WebPanel panel = new WebPanel(new VerticalFlowLayout());
		panel.add(createRangePanel());
		rangeComboBox.registerSettings(new Configuration<ComboBoxState>(getClass().getName() + ".rangeComboBox"));
		addToolBarActions("savePreflopRange");
		getToolBarPanel().add(rangeComboBox);
		setBodyComponent(panel);
	}
	
	/**
	 * return the {@link PreflopCardsRange} used by this panel
	 * 
	 * @return the preflop range
	 */
	public PreflopCardsRange getPreflopCardsRange() {
		return preflopCardsRange;
	}
	
	/**
	 * return the current selected name and description for the preflop range
	 * 
	 * @return {@link TEntry} with name and description
	 */
	public TEntry<String, String> getSelectedRange() {
		return (TEntry) rangeComboBox.getSelectedItem();
	}

	private JPanel createRangePanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 10));
		cardButtons = new JLabel[13][13];
		JPanel cardTypesPanel = new JPanel();
		cardTypesPanel.setLayout(new GridLayout(13, 13, 2, 2));

		MouseAdapter listener = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				JLabel src = (JLabel) e.getSource();
				preflopCardsRange.flipValue(src.getText());
				updateCardsButtons();
			}
		};
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				String card = preflopCardsRange.getCardAt(i, j);
				cardButtons[i][j] = new JLabel(card);
				cardButtons[i][j].setHorizontalAlignment(JLabel.CENTER);
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

		ChangeListener sliderL = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!slider.getValueIsAdjusting()) {
					preflopCardsRange.setNewPercentage(slider.getValue());
					updateCardsButtons();
				}
			}
		};
		slider.addChangeListener(sliderL);

		panel.add(slider, BorderLayout.SOUTH);

		return panel;
	}

	/**
	 * update the UI status for all cardsButtons
	 * 
	 */
	private void updateCardsButtons() {
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				String card = preflopCardsRange.getCardAt(i, j);
				boolean isSel = preflopCardsRange.isSelected(card);
				cardButtons[i][j].setBorder(new LineBorder(isSel ? SELECTED_BORDER : UNSELECTED_BORDER));
				cardButtons[i][j].setBackground(isSel ? SELECTED_COLOR : UNSELECTED_COLOR);
				cardButtons[i][j].setToolTipText("" + preflopCardsRange.getEV(card));
			}
		}
	}

	/**
	 * load the selected item in combobos and update the gloval variable {@link #preflopCardsRange}
	 * 
	 */
	private void loadFromDB() {
		TEntry<String, String> te = (TEntry<String, String>) rangeComboBox.getSelectedItem();
		preflopCardsRange = new PreflopCardsRange(te.getKey());
		if (preflopCardsRange == null) {
			preflopCardsRange = new PreflopCardsRange();
		}
		
//		double steps = preflopCardsRange.evRange / 10;
//		Hashtable<Integer, JLabel> labels = new Hashtable<>();
//		for (int i = 1; i <= 10; i++) {
//			String val = String.format("%4.2f", (preflopCardsRange.lowerBound + (steps * i)));
//			labels.put(100 - (i * 10), new JLabel(val));
//		}

		slider.setValue(preflopCardsRange.getPercentage());
//		slider.setLabelTable(labels);
		updateCardsButtons();
	}
}

