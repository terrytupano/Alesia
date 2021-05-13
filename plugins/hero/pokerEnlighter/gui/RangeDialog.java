package plugins.hero.pokerEnlighter.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.javafling.pokerenlighter.simulation.*;

/**
 *
 * @author Radu Murzea
 */
public class RangeDialog extends JDialog
{
    private static final int DEFAULT_PERCENTAGE = 15;
    
    private JLabel[][] cardButtons;
    
    private JSlider slider;

    private Range range;
    
    private boolean cancelled;
    
    public RangeDialog(JFrame parent, Range range)
    {
        super(parent, "Range Dialog", true);
        
        this.range = (range == null) ? new Range(DEFAULT_PERCENTAGE) : range;

        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        JPanel rangePanel = createRangePanel();
        JPanel buttonsPanel = createButtonsPanel();
        
        JPanel content = new JPanel(new BorderLayout());
        content.add(rangePanel, BorderLayout.CENTER);
        content.add(buttonsPanel, BorderLayout.SOUTH);
        
        cancelled = false;
        
        setContentPane(content);
        pack();
    }
    
    public Range getRange()
    {
        return range;
    }
    
    public boolean isCancelled()
    {
        return cancelled;
    }
    
    private JPanel createRangePanel()
    {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        
        GUIUtilities.setBorder(panel, "Range Selection", TitledBorder.LEFT);
        
        cardButtons = new JLabel[13][13];
        
        drawButtons();
        
        JPanel cardTypesPanel = new JPanel();

        cardTypesPanel.setLayout(new GridLayout(13, 13, 2, 2));
        
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                cardButtons[i][j].addMouseListener(new CardFlipperMouseListener(i, j));
                
                cardTypesPanel.add(cardButtons[i][j]);
            }
        }
        
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
    
    private void drawButtons()
    {
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 13; j++) {
                StringBuilder path = new StringBuilder();
                path.append("../resources/cardtypes/");
                path.append(range.getValue(i, j) ? "selected" : "notselected");
                path.append("/");
                path.append(Range.rangeNames[i][j]);
                path.append(".png");
                
                ImageIcon icon = new ImageIcon(getClass().getResource(path.toString()));
                
                if (cardButtons[i][j] == null) {
                    cardButtons[i][j] = new JLabel(icon);
                } else {
                    cardButtons[i][j].setIcon(icon);
                }
            }
        }
    }
    
    private JPanel createButtonsPanel()
    {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        
        cancelButton.addActionListener(new CancelButtonListener());
        okButton.addActionListener(new OKListener());
        
        panel.add(okButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    private class CardFlipperMouseListener extends MouseAdapter
    {
        private int row;
        private int column;
        
        public CardFlipperMouseListener(int row, int column)
        {
            this.row = row;
            this.column = column;
        }

        @Override
        public void mousePressed(MouseEvent e)
        {
            range.flipValue(row, column);
            
            StringBuilder path = new StringBuilder();
            
            path.append("../resources/cardtypes/");
            path.append(range.getValue(row, column) ? "" : "not");
            path.append("selected/");
            path.append(Range.rangeNames[row][column]);
            path.append(".png");

            cardButtons[row][column].setIcon(new ImageIcon(getClass().getResource(path.toString())));
        }
    }
    
    private class SliderChangeListener implements ChangeListener
    {
        @Override
        public void stateChanged(ChangeEvent e)
        {
            if (! slider.getValueIsAdjusting()) {
                range.setNewPercentage(slider.getValue());

                drawButtons();
            }
        }
    }
    
    private class OKListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            dispose();
        }
    }
    
    private class CancelButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            cancelled = true;
            
            dispose();
        }
    }
}
