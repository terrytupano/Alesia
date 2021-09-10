package plugins.hero.utils;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.alee.extended.layout.*;
import com.alee.laf.combobox.*;
import com.alee.laf.panel.*;
import com.alee.managers.settings.*;

import core.*;
import gui.*;

public class PreFlopCardsPanel extends TUIPanel {
	private static final Color SELECTED_COLOR = Color.CYAN;
	private static final Color SELECTED_BORDER = Color.BLUE;
	private static final Color UNSELECTED_COLOR = Color.WHITE;
	private static final Color UNSELECTED_BORDER = Color.GRAY;

	private JLabel[][] cardButtons;
	private JSlider slider;
	private PreflopCardsModel preflopCardsModel;
	private WebComboBox preflopsComboBox;

	public PreFlopCardsPanel() {
		super();
		this.preflopsComboBox = new WebComboBox(PreflopCardsModel.getPreflopList());
		TUIUtils.setDimensionForTextComponent(preflopsComboBox, 40);
		preflopsComboBox.addActionListener(evt -> loadFromDB());
//		ResourceMap r = Alesia.getInstance().getContext().getResourceManager().getResourceMap();
//		r.injectComponent(this);
		preflopCardsModel = new PreflopCardsModel();
		WebPanel panel = new WebPanel(new VerticalFlowLayout());
		panel.add(createRangePanel());
		preflopsComboBox.registerSettings(new Configuration<ComboBoxState>(getClass().getName() + ".rangeComboBox"));
		addToolBarActions("savePreflopRange");
		getToolBarPanel().add(preflopsComboBox);
		setBodyComponent(panel);
	}

	/**
	 * return the {@link PreflopCardsModel} used by this panel
	 * 
	 * @return the preflop range
	 */
	public PreflopCardsModel getPreflopCardsRange() {
		return preflopCardsModel;
	}

	/**
	 * return the current selected name and description for the preflop range
	 * 
	 * @return {@link TEntry} with name and description
	 */
	public TEntry<String, String> getSelectedRange() {
		return (TEntry) preflopsComboBox.getSelectedItem();
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
				preflopCardsModel.flipValue(src.getText());
				updateCardsButtons();
			}
		};
		for (int i = 0; i < 13; i++) {
			for (int j = 0; j < 13; j++) {
				String card = preflopCardsModel.getCardAt(i, j);
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

		slider = new JSlider(JSlider.HORIZONTAL, 0, 100, preflopCardsModel.getPercentage());
		slider.setMinorTickSpacing(5);
		slider.setMajorTickSpacing(20);
		slider.setPaintLabels(true);
		slider.setPaintTicks(true);
		slider.getModel().setValueIsAdjusting(true);

		ChangeListener sliderL = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!slider.getValueIsAdjusting()) {
					preflopCardsModel.setPercentage(slider.getValue());
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
				String card = preflopCardsModel.getCardAt(i, j);
				boolean isSel = preflopCardsModel.isSelected(card);
				cardButtons[i][j].setBorder(new LineBorder(isSel ? SELECTED_BORDER : UNSELECTED_BORDER));
				cardButtons[i][j].setBackground(isSel ? SELECTED_COLOR : UNSELECTED_COLOR);
				cardButtons[i][j].setToolTipText("" + preflopCardsModel.getEV(card));
			}
		}
	}

	/**
	 * load the selected item in combobos and update the gloval variable {@link #preflopCardsModel}
	 * 
	 */
	private void loadFromDB() {
		TEntry<String, String> te = (TEntry<String, String>) preflopsComboBox.getSelectedItem();
		preflopCardsModel = new PreflopCardsModel(te.getKey());
		if (preflopCardsModel == null) {
			preflopCardsModel = new PreflopCardsModel();
		}

		// setvalueisadjusting = true avoid actionperformed invocation. this is becaus during 
//		slider.setValueIsAdjusting(true);
		slider.setValue(preflopCardsModel.getPercentage());
//		slider.setValueIsAdjusting(false);
		// slider.setLabelTable(labels);
		updateCardsButtons();
	}
}
