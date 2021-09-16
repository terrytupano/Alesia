package gui.docking;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.alee.extended.layout.*;
import com.alee.laf.button.*;
import com.alee.laf.panel.*;

import core.*;
import gui.*;

public class TSettingsPanel extends WebPanel {

	private ActionMap myMap;
	public TSettingsPanel() {
		super();
//		setLayout(new GridLayout(1,1));
		setLayout(new VerticalFlowLayout());
		this.myMap = Alesia.getInstance().getContext().getActionMap(this);
		for (Object key : myMap.keys()) {
			Action act = myMap.get(key);
			TUIUtils.overRideIcons(32, Color.DARK_GRAY, act);
			WebButton btn = new WebButton(act);
			btn.setBorder(null);
			String html = TStringUtils.getTitleText(act.getValue(javax.swing.Action.NAME).toString(),
					act.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString());
			btn.setText(html);
			btn.setVerticalAlignment(SwingConstants.TOP);
			add(btn);
		}
	}

	// @Action
	// public void about(ActionEvent event) {
	// DockingContainer dc = SwingUtils.getFirstParent((JComponent) event.getSource(), DockingContainer.class);
	// dc.setContentPanel(new AboutPanel());
	// }

	@org.jdesktop.application.Action
	public void about(ActionEvent event) {
		Alesia.getInstance().getMainPanel().setContentPanel(new AboutPanel());
	}

	@org.jdesktop.application.Action
	public void uiManager(ActionEvent event) {
		UIManagerDefaults man = new UIManagerDefaults();
		Alesia.getInstance().getMainPanel().setContentPanel(man.getContentPane());
	}

}
