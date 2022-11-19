package gui.docking;

import java.awt.*;
import java.awt.event.*;
import java.util.List;

import javax.swing.*;

import com.alee.extended.layout.*;
import com.alee.extended.magnifier.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.managers.style.*;
import com.alee.utils.*;

import core.*;
import gui.*;

/**
 * this home panel is divide en 2 main grups.
 * 
 * Center - cotain all plugin detected in plugin folder richt - contain adtional links
 * 
 * @author terry
 *
 */
public class HomePanel extends WebPanel {

	private ActionMap myMap;
	private WebPanel centerPanel, eastPanel;
	private MagnifierGlass magnifier;

	public HomePanel() {
		super(new BorderLayout());
		magnifier = new MagnifierGlass();
		centerPanel = new WebPanel();
		centerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 4));

		// importants links
		eastPanel = new WebPanel(new VerticalFlowLayout());

		WebLabel ol = new WebLabel();
		ol.setBoldFont();
		eastPanel.add(ol);
		this.myMap = Alesia.getInstance().getContext().getActionMap(this);
		for (Object key : myMap.keys()) {
			Action act = myMap.get(key);
			// TUIUtils.overRideIcons(12, Color.DARK_GRAY, act);
			WebButton btn = new WebButton(StyleId.buttonHover, act);
			btn.setHorizontalAlignment(WebButton.LEFT);
			eastPanel.add(btn);
		}

		add(centerPanel, BorderLayout.CENTER);
		add(eastPanel, BorderLayout.EAST);
	}

	/**
	 * create a main component whit all actions passes as argument.
	 * 
	 * @param actions
	 */
	public void setActions(List<Action> actions) {
		centerPanel.removeAll();
		for (Action action : actions) {
			centerPanel.add(TDockingContainer.getMosaicWebButton(action));
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
