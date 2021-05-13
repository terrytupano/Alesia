package plugins.hero.pokerEnlighter.gui;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import plugins.hero.pokerEnlighter.*;

/**
 *
 * @author Radu Murzea
 */
public class MenuBar
{
    private JFrame parent;
    
    private JMenuBar menuBar;
    
    private JMenu fileMenu, helpMenu;
    
    private JMenuItem exitAction, aboutAction, prefsAction, updateAction, newSimulationAction;
    
    private PEDictionary dictionary;
    
    public MenuBar(JFrame parent, PEDictionary dictionary)
    {
        this.parent = parent;
        
        this.dictionary = dictionary;
        
        menuBar = new JMenuBar();

        fileMenu = new JMenu(dictionary.getValue("menubar.file"));
        helpMenu = new JMenu(dictionary.getValue("menubar.help"));
        
        fileMenu.setMnemonic(KeyEvent.VK_F);
        helpMenu.setMnemonic(KeyEvent.VK_H);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        exitAction = new JMenuItem(dictionary.getValue("menubar.file.exit"));
        prefsAction = new JMenuItem(dictionary.getValue("menubar.file.prefs"));
        aboutAction = new JMenuItem(dictionary.getValue("menubar.help.about"));
        updateAction = new JMenuItem(dictionary.getValue("menubar.help.checkupdate"));
        newSimulationAction = new JMenuItem(dictionary.getValue("menubar.file.newsim"));

        //fyi: order is important (user interface standards)
        fileMenu.add(newSimulationAction);
        fileMenu.add(prefsAction);
        fileMenu.add(exitAction);
        helpMenu.add(updateAction);
        helpMenu.add(aboutAction);

        aboutAction.addActionListener(new AboutListener());
        prefsAction.addActionListener(new PreferencesListener());
        exitAction.addActionListener(new ExitListener());
        updateAction.addActionListener(new UpdateListener());
        newSimulationAction.addActionListener(new NewSimulationListener());
    }
    
    public JMenuBar getMenuBar()
    {
        return menuBar;
    }

    private class AboutListener implements ActionListener
    {
        @Override
        public void actionPerformed (ActionEvent e)
        {
            AboutDialog ad = new AboutDialog(parent);
            ad.setLocationRelativeTo(parent);
            ad.setVisible(true);
        }
    }
    
    private class PreferencesListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            new PreferencesDialog(parent, dictionary).display();
        }
    }
    
    private class ExitListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            parent.dispose();
        }
    }

    private class UpdateListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            new UpdateChecker().execute();
        }
    }
    
    private class NewSimulationListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            GUI.getGUI().newSimulation();
        }
    }
    
    private class UpdateChecker extends SwingWorker<Void, Void>
    {
        @Override
        public Void doInBackground()
        {
            String url = "http://pokerenlighter.javafling.org/update.check.php?build=" + PokerEnlighter.BUILD_NUMBER;
            
            InternetConnection conn = null;
            try {
                conn = InternetConnectionFactory.createDirectConnection(url);
            } catch (IOException ex) {
                GUIUtilities.showErrorDialog(parent, "There was an error while checking for update", "Update Check");
                return null;
            }
            
            String content = conn.getContent();
            
            if (content == null || content.equals("ERROR")) {
                GUIUtilities.showErrorDialog(parent, "There was an error while checking for update", "Update Check");
            } else if (content.startsWith("YES")) {
                String[] elements = content.split("\\|");
            
                GUIUtilities.showOKDialog(parent, "An update is available: " + elements[1], "Update Check");
            } else if (content.startsWith("NO")) {
                GUIUtilities.showOKDialog(parent, "You have the latest version", "Update Check");
            }
            
            return null;
        }
    }
}

