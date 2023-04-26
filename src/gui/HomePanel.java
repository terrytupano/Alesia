package gui;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;

import com.alee.extended.layout.*;
import com.alee.extended.magnifier.*;
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
	private MagnifierGlass magnifier;
	
	public HomePanel() {
		super(new BorderLayout(TUIUtils.STANDAR_GAP,TUIUtils.STANDAR_GAP));
		magnifier = new MagnifierGlass();
		centerPanel = new WebPanel();
		TUIUtils.setEmptyBorder(this);
		centerPanel.setLayout(new VerticalFlowLayout(TUIUtils.STANDAR_GAP,TUIUtils.STANDAR_GAP,true,  false));

		// splash component
		JComponent splash = Alesia.getInstance().mainFrame.getSplash();
		
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
		Alesia.getInstance().getMainPanel().showPanel(new AboutPanel());
	}

	@org.jdesktop.application.Action
	public void uiManager(ActionEvent event) {
		UIManagerDefaults man = new UIManagerDefaults();
		Alesia.getInstance().getMainPanel().showPanel(man.getContentPane());
	}

	@org.jdesktop.application.Action
	public void magnifier(ActionEvent event) {
		magnifier.displayOrDispose(Alesia.getInstance().mainFrame);
	}
}
