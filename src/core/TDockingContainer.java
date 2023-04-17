package core;
/*******************************************************************************
 * Copyright (C) 2017 terry.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     terry - initial API and implementation
 ******************************************************************************/


import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;

import org.jdesktop.application.*;

import com.alee.extended.statusbar.*;
import com.alee.extended.transition.*;
import com.alee.extended.transition.effects.*;
import com.alee.extended.transition.effects.slide.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.laf.toolbar.*;
import com.alee.managers.style.*;
import com.alee.utils.*;
import com.jgoodies.common.base.*;

import gui.*;

public class TDockingContainer extends WebPanel {

	private ComponentTransition transitionPanel;
	private WebToolBar toolBar;
	private SlideTransitionEffect effect;
	private HomePanel homePanel;
	private ApplicationAction home, previous;
	private ActionMap myMap;
	private int cmpCounter;
	private List<JComponent> components;

	public TDockingContainer() {
		super(new BorderLayout());
		this.myMap = Alesia.getInstance().getContext().getActionMap(this);

		// toolbar
		this.previous = (ApplicationAction) myMap.get("previous");
		this.home = (ApplicationAction) myMap.get("home");
		this.toolBar = TUIUtils.getWebToolBar(previous, home);

		// staus bar
		WebStatusBar statusBar = new WebStatusBar();
		WebLabel pd = new WebLabel(TStringUtils.getAboutAppShort(), TResources.getSmallIcon("alpha.png"));
		statusBar.add(pd);
		statusBar.addSpacing();
		statusBar.addToEnd(Alesia.getInstance().taskManager.getProgressBar());

		transitionPanel = new ComponentTransition();
		transitionPanel.setContent(Alesia.getInstance().getMainFrame().getContentPane());

		// Transition effect
		effect = new SlideTransitionEffect();
		// effect.setDirection(Direction.down);
		effect.setType(SlideType.moveBoth);
		effect.setSpeed(60);

		// CurtainTransitionEffect effect = new CurtainTransitionEffect();
		// effect.setDirection(com.alee.extended.transition.effects.Direction.down);
		// effect.setType(CurtainType.fade);
		// effect.setSpeed(9);

		transitionPanel.setTransitionEffect(effect);

		this.components = new ArrayList<>();
		this.cmpCounter = -1;
		this.homePanel = new HomePanel();

		add(toolBar, BorderLayout.NORTH);
		add(transitionPanel, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
		showPanel(homePanel, Direction.down);
		syncNavActions();
	}

	public static ArrayList<WebButton> createNavButtons(Color toColor, String style, Font font, Action... actions) {
		int size = 20;
		ArrayList<WebButton> list = new ArrayList<>();
		TUIUtils.overRideIcons(size, toColor, actions);
		for (Action action : actions) {
			WebButton wb = new WebButton(StyleId.of(style), action);
			if (font != null) {
				wb.setFont(font);
			}
			// TODO: incorporate security
			list.add(wb);
		}
		return list;
	}

	/**
	 * create and return a especial instace of {@link WebButton}
	 * 
	 * @param action - action
	 * 
	 * @return especial webbuton
	 */
	public static WebButton getMosaicWebButton(Action action) {
		TUIUtils.overRideIcons(32, TUIUtils.ACCENT_COLOR, action);
		WebButton btn = new WebButton(StyleId.buttonHover, action);
		// btn.onMouseEnter(me -> btn.setBorder(new LineBorder(Color.BLUE));
		String html = TStringUtils.getTitleText(action.getValue(javax.swing.Action.NAME).toString(),
				action.getValue(javax.swing.Action.SHORT_DESCRIPTION).toString());
		btn.setText(html);
		btn.setIconTextGap(8);
		btn.setVerticalAlignment(SwingConstants.TOP);
		btn.setHorizontalAlignment(SwingConstants.LEFT);
		// btn.setVerticalTextPosition(SwingConstants.TOP);
		return btn;
	}

	/**
	 * add the listener all containers instances of <code>soruceClazz</code> inside
	 * of this component content panel.
	 * 
	 * @param sourceClazz  - the class source of the property
	 * @param propertyName - the name
	 * @param listener     - the listener interested in recive notification
	 */
	public void addChangeListener(String propertyName, PropertyChangeListener listener) {
		List<Container> cnts = SwingUtils.collectAllContainers(this);
		// avoid to add listerner to the "listener" itselft and other class disticg to
		// TUIListPanel
		cnts.removeIf(cnt -> cnt == listener || !(cnt instanceof TUIListPanel));
		for (Container source : cnts) {
			// avoid mutiple propertyChange invocation on listener
			source.removePropertyChangeListener(propertyName, listener);
			source.addPropertyChangeListener(propertyName, listener);
		}

		// for (String cn : vs) {
		// // check not addPropertyChangeListener to myself
		// if (!dview.getName().equals(cn)) {
		// addPropertyChangeListener(cn, TConstants.RECORD_SELECTED, lst);
		// addPropertyChangeListener(cn, TConstants.FIND_TEXT, lst);
		// addPropertyChangeListener(cn, TConstants.LOG_MESSAGE, lst);
		// }
		// }
		// }

	}

	public HomePanel getHomePanel() {
		return homePanel;
	}

	@org.jdesktop.application.Action
	public void home(ActionEvent event) {
		components.clear();
		showPanel(homePanel, Direction.right);
	}

	@org.jdesktop.application.Action
	public void previous(ActionEvent event) {
		components.remove(cmpCounter);
		JComponent cmp = components.get(--cmpCounter);
		components.remove(cmp);
		showPanel(cmp, Direction.right);
	}

	/**
	 * show the component in this
	 * 
	 * @param newComponent
	 */
	public void showPanel(JComponent newComponent) {
		showPanel(newComponent, Direction.left);
	}

	private void showPanel(JComponent newComponent, Direction direction) {
		effect.setDirection(direction);
		components.add(newComponent);
		cmpCounter = components.size() - 1;
		syncNavActions();

		// auto select listener for model select property
		List<Container> cnts = SwingUtils.collectAllContainers(newComponent);
		for (Container cnt : cnts) {
			if (cnt instanceof TUIListPanel)
				((TUIListPanel) cnt).init();

			if (cnt instanceof PropertyChangeListener && cnt instanceof TUIListPanel) {
				PropertyChangeListener pcl = (PropertyChangeListener) cnt;
				addChangeListener(TUIListPanel.MODEL_SELECTED, pcl);
			}
		}
		transitionPanel.performTransition(newComponent);
	}

	/**
	 * Utility method to perform {@link UIListPanel#freshen()} in an active instance
	 * of the class pass as argument.
	 * <p>
	 * Use this class for example when an arbitrary action alter the internal
	 * content of another class and you want
	 * those class uptade their internal content data .
	 * 
	 * @param clsn - active instance to refresh
	 */
	public void signalFreshgen(Class<? extends Component> clazz) {
		SwingUtilities.invokeLater(() -> {
			Object cnt = SwingUtils.getFirst(components.get(cmpCounter), clazz);
			Preconditions.checkArgument(cnt instanceof TUIListPanel, "the Class %s must be instance of TUIListPanel",
					clazz.getName(), TUIListPanel.class.getName());
			TUIListPanel tuilp = (TUIListPanel) cnt;
			tuilp.freshen();
		});
	}

	private void syncNavActions() {
		previous.setEnabled(components.size() > 1);
		home.setEnabled(components.size() > 1);
	}
}
