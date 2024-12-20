// This file is part of the 'texasholdem' project, an open source
// Texas Hold'em poker application written in Java.
//
// Copyright 2009 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package hero.ozsoft.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import hero.ozsoft.actions.*;

/**
 * Panel for selecting the amount to bet or raise.
 * 
 * @author Oscar Stigter
 */
public class AmountPanel extends JPanel implements ChangeListener, ActionListener {
    
    /** Serial version UID. */
    private static final long serialVersionUID = 171860711156799253L;

    /** Number of increasing amounts to choose from (ticks on slider bar). */
    private static final int NO_OF_TICKS = 10;

    /** Slider with the amount to bet or raise. */
    private final JSlider amountSlider;

    /** Label with selected amount. */
    private final JLabel amountLabel;
    
    /** Bet/Raise button. */
    private final JButton betRaiseButton;
    
    /** Cancel button. */
    private final JButton cancelButton;
    
    /** Incremental bet amounts mapped to slider's index. */
    private final HashMap<Integer, Integer> sliderAmounts;
    
    /** Monitor while waiting for user input. */
    private final Object monitor = new Object();
    
    private PlayerAction defaultAction;
    
    /** The selected action. */
    private PlayerAction selectedAction;
    
    /**
     * Constructor.
     */
    public AmountPanel() {
        setBackground(UIConstants.TABLE_COLOR);
        
        sliderAmounts = new HashMap<Integer, Integer>();
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        amountSlider = new JSlider();
        amountSlider.setBackground(UIConstants.TABLE_COLOR);
        amountSlider.setMajorTickSpacing(1);
        amountSlider.setMinorTickSpacing(1);
        amountSlider.setPaintTicks(true);
        amountSlider.setSnapToTicks(true);
        amountSlider.addChangeListener(this);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 5);
        add(amountSlider, gbc);

        amountLabel = new JLabel(" ");
        amountLabel.setForeground(UIConstants.TEXT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 0, 5, 0);
        add(amountLabel, gbc);
        
        betRaiseButton = new JButton("Bet");
        betRaiseButton.addActionListener(this);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(betRaiseButton, gbc);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(cancelButton, gbc);
    }
    
    /**
     * Resets and shows the panel.
     * 
     * @param defaultAction
     *            The default action.
     * @param minBet
     *            The minimum bet.
     * @param maxBet
     *            The maximum bet.
     * 
     * @return The selected action.
     */
    public PlayerAction show(PlayerAction defaultAction, int minBet, int maxBet) {
        this.defaultAction = defaultAction;
        betRaiseButton.setText(defaultAction.getName());
        selectedAction = null;
        
        // Determine incremental amounts on slider bar.
        sliderAmounts.clear();
        int noOfValues = 0;
        int value = minBet;
        while (value < maxBet && noOfValues < (NO_OF_TICKS - 1)) {
            sliderAmounts.put(noOfValues, value);
            noOfValues++;
            value *= 2;
        }
        sliderAmounts.put(noOfValues, maxBet);
        amountSlider.setMinimum(0);
        amountSlider.setMaximum(noOfValues);
        amountSlider.setValue(0);
        
        // Wait for the user to select an amount or cancel.
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                // Ignore.
            }
        }
        
        return selectedAction;
    }
    
    /**
     * Returns the selected amount.
     * 
     * @return The selected amount.
     */
    public int getAmount() {
        int index = amountSlider.getValue();
        return sliderAmounts.get(index);
    }

    /** {@inheritDoc} */
    @Override
    public void stateChanged(ChangeEvent e) {
        int index = amountSlider.getValue();
        int amount = sliderAmounts.get(index);
        amountLabel.setText(String.format("$ %d", amount));
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == betRaiseButton) {
            selectedAction = defaultAction;
        } else if (e.getSource() == cancelButton) {
            selectedAction = null;
        }
        
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }
    
}
