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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import hero.ozsoft.*;
import hero.ozsoft.actions.*;

/**
 * Panel with buttons to let a human player select an action.
 * 
 * @author Oscar Stigter
 */
public class ControlPanel extends JPanel implements ActionListener {
    
    /** Serial version UID. */
    private static final long serialVersionUID = 4059653681621749416L;
    
    /** The table type (betting structure). */
    private final TableType tableType;

    /** The Check button. */
    private final JButton checkButton;
    
    /** The Call button. */
    private final JButton callButton;
    
    /** The Bet button. */
    private final JButton betButton;
    
    /** The Raise button. */
    private final JButton raiseButton;
    
    /** The Fold button. */
    private final JButton foldButton;
    
    /** The Continue button. */
    private final JButton continueButton;
    
    /** The betting panel. */
    private final AmountPanel amountPanel;

    /** Monitor while waiting for user input. */
    private final Object monitor = new Object();
    
    /** The selected action. */
    private PlayerAction selectedAction;
    
    /**
     * Constructor.
     */
    public ControlPanel(TableType tableType) {
        this.tableType = tableType;
        setBackground(UIConstants.TABLE_COLOR);
        continueButton = createActionButton(PlayerAction.CONTINUE);
        checkButton = createActionButton(PlayerAction.CHECK);
        callButton = createActionButton(PlayerAction.CALL);
        betButton = createActionButton(PlayerAction.BET);
        raiseButton = createActionButton(PlayerAction.RAISE);
        foldButton = createActionButton(PlayerAction.FOLD);
        amountPanel = new AmountPanel();
    }
    
    /**
     * Waits for the user to click the Continue button.
     */
    public void waitForUserInput() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                removeAll();
                add(continueButton);
                repaint();
            }
        });
        Set<PlayerAction> allowedActions = new HashSet<PlayerAction>();
        allowedActions.add(PlayerAction.CONTINUE);
        getUserInput(0, 0, allowedActions);
    }
    
    /**
     * Waits for the user to click an action button and returns the selected
     * action.
     * 
     * @param minBet
     *            The minimum bet.
     * @param cash
     *            The player's remaining cash.
     * @param allowedActions
     *            The allowed actions.
     * 
     * @return The selected action.
     */
    public PlayerAction getUserInput(int minBet, int cash, final Set<PlayerAction> allowedActions) {
        selectedAction = null;
        while (selectedAction == null) {
            // Show the buttons for the allowed actions.
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    removeAll();
                    if (allowedActions.contains(PlayerAction.CONTINUE)) {
                        add(continueButton);
                    } else {
                        if (allowedActions.contains(PlayerAction.CHECK)) {
                            add(checkButton);
                        }
                        if (allowedActions.contains(PlayerAction.CALL)) {
                            add(callButton);
                        }
                        if (allowedActions.contains(PlayerAction.BET)) {
                            add(betButton);
                        }
                        if (allowedActions.contains(PlayerAction.RAISE)) {
                            add(raiseButton);
                        }
                        if (allowedActions.contains(PlayerAction.FOLD)) {
                            add(foldButton);
                        }
                    }
                    repaint();
                }
            });
            
            // Wait for the user to select an action.
            synchronized (monitor) {
                try {
                    monitor.wait();
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
            
            // In case of a bet or raise, show panel to select amount.
            if (tableType == TableType.NO_LIMIT && (selectedAction == PlayerAction.BET || selectedAction == PlayerAction.RAISE)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        removeAll();
                        add(amountPanel);
                        repaint();
                    }
                });
                selectedAction = amountPanel.show(selectedAction, minBet, cash);
                if (selectedAction == PlayerAction.BET) {
                    selectedAction = new BetAction(amountPanel.getAmount());
                } else if (selectedAction == PlayerAction.RAISE) {
                    selectedAction = new RaiseAction(amountPanel.getAmount());
                } else {
                    // User cancelled.
                    selectedAction = null;
                }
            }
        }
        
        return selectedAction;
    }
    
    /*
     * (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == continueButton) {
            selectedAction = PlayerAction.CONTINUE;
        } else if (source == checkButton) {
            selectedAction = PlayerAction.CHECK;
        } else if (source == callButton) {
            selectedAction = PlayerAction.CALL;
        } else if (source == betButton) {
            selectedAction = PlayerAction.BET;
        } else if (source == raiseButton) {
            selectedAction = PlayerAction.RAISE;
        } else {
            selectedAction = PlayerAction.FOLD;
        }
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }
    
    /**
     * Creates an action button.
     * 
     * @param action
     *            The action.
     * 
     * @return The button.
     */
    private JButton createActionButton(PlayerAction action) {
        String label = action.getName();
        JButton button = new JButton(label);
        button.setMnemonic(label.charAt(0));
        button.setSize(100, 30);
        button.addActionListener(this);
        return button;
    }

}
