package gui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

import com.alee.extended.layout.*;
import com.alee.laf.panel.*;
import com.alee.utils.*;

import core.*;
import gui.jgoodies.*;

/**
 * main panel of Alesia. contain all available actions + tools and about links
 * 
 * @author Terry
 *
 */
public class HomePanel extends WebPanel {

	private ActionMap myMap;
	private WebPanel centerPanel, southPanel;
	
	public HomePanel() {
		super(new BorderLayout(TUIUtils.STANDAR_GAP,TUIUtils.STANDAR_GAP));
		centerPanel = new WebPanel();
		TUIUtils.setEmptyBorder(this);
		centerPanel.setLayout(new VerticalFlowLayout(TUIUtils.STANDAR_GAP,TUIUtils.STANDAR_GAP,true,  false));

		// splash component
		JComponent splash = Alesia.getMainFrame().getSplash();
		
		// footer actions
		southPanel = new WebPanel();
		southPanel.setLayout(new VerticalFlowLayout(TUIUtils.STANDAR_GAP,TUIUtils.STANDAR_GAP,true,  false));
		this.myMap = Alesia.getInstance().getContext().getActionMap(this);
		for (Object key : myMap.keys()) {
			Action action = myMap.get(key);
			ListItem item = new ListItem();
			item.setSmallAction(action);
			southPanel.add(item);
		}

		add(splash, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(southPanel, BorderLayout.SOUTH);
	}

	/**
	 * create a main component whit all actions passes as argument.
	 * 
	 * @param actions
	 */
	public void setActions(List<Action> actions) {
		centerPanel.removeAll();
		for (Action action : actions) {
			ListItem item = new ListItem();
			item.setAction(action);
			centerPanel.add(item);
//			centerPanel.add(TUIUtils.getMosaicWebButton(action));
		}
		SwingUtils.equalizeComponentsSize(centerPanel.getComponents());
	}

	@org.jdesktop.application.Action
	public void about(ActionEvent event) {
		Alesia.getMainPanel().showPanel(new AboutPanel());
	}

	@org.jdesktop.application.Action
	public void uiManager(ActionEvent event) {
		UIManagerDefaults man = new UIManagerDefaults();
		Alesia.getMainPanel().showPanel(man.getContentPane());
	}

}
