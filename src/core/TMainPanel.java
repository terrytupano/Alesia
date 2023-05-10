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

import org.jdesktop.application.*;

import com.alee.extended.statusbar.*;
import com.alee.extended.transition.*;
import com.alee.extended.transition.effects.*;
import com.alee.extended.transition.effects.slide.*;
import com.alee.laf.grouping.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.laf.scroll.*;
import com.alee.laf.toolbar.*;
import com.alee.utils.*;
import com.jgoodies.common.base.*;

import gui.*;

/**
 * Contain the Toolbar, and the center panel where all component are displayed.
 * this panel control the transitions between pages and control the navigations.
 * 
 * @author terry
 *
 */
public class TMainPanel extends WebPanel {

	private ComponentTransition transitionPanel;
	private WebToolBar toolBar;
	private GroupPane leftAndHomeGroup;
	private SlideTransitionEffect effect;
	private HomePanel homePanel;
	private ApplicationAction home, previous;
	private int cmpCounter;
	private List<JComponent> components;

	public TMainPanel() {
		super(new BorderLayout());
		ActionMap actionMap = Alesia.getInstance().getContext().getActionMap(this);

		// toolbar
		this.previous = (ApplicationAction) actionMap.get("previous");
		this.home = (ApplicationAction) actionMap.get("home");
		this.toolBar = TUIUtils.getWebToolBar();
		this.leftAndHomeGroup = TUIUtils.getGroupPane(previous, home);
		toolBar.add(leftAndHomeGroup);

		// Status bar
		WebStatusBar statusBar = new WebStatusBar();
		WebLabel pd = new WebLabel(TStringUtils.getAboutAppShort(), TResources.getSmallIcon("alpha.png"));
		statusBar.add(pd);
		statusBar.addSpacing();
		statusBar.addToEnd(Alesia.getTaskManager().getProgressBar());

		transitionPanel = new ComponentTransition();
		// add the initial content of the frame splash
		transitionPanel.setContent(Alesia.getMainFrame().getContentPane());

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
	 * show the component in this container as a new component in the sequence.
	 * 
	 * @param newComponent - the component to show
	 * @param inScroll     - if the component must be wrapped in a
	 *                     {@link WebScrollPane}
	 */
	public void showInScrollPanel(JComponent newComponent) {
		WebScrollPane pane = TUIUtils.getWebScrollPane(newComponent);
		showPanel(pane, Direction.left);
	}

	public void showPanel(JComponent newComponent) {
		showPanel(newComponent, Direction.left);
	}

	private void showPanel(JComponent newComponent, Direction direction) {
		effect.setDirection(direction);
		components.add(newComponent);
		cmpCounter = components.size() - 1;
		syncNavActions();

		// TODO: temporal impl find only the first one
		Component toobarCmp = newComponent;
		if (newComponent instanceof JScrollPane) {
			JScrollPane pane = (JScrollPane) newComponent;
			for (Component c : pane.getComponents()) {
				if (c instanceof TUIPanel) {
					toobarCmp = pane.getViewport().getView();
					break;
				}
			}
		}

		// remove the toolbar from the source and add to this component
		if (toobarCmp instanceof TUIPanel) {
			TUIPanel tuiPanel = (TUIPanel) newComponent;
			if (tuiPanel.getToolBar().getComponentCount() > 0)
				toolBar.addSeparator();
			Component[] components = tuiPanel.getToolBar().getComponents();
			for (Component component : components) {
				toolBar.add(component);
			}
			// remove decorations
			tuiPanel.getToolBar().setVisible(false);
		}

		transitionPanel.performTransition(newComponent);
	}

	/**
	 * Utility method to perform {@link UIListPanel#freshen()} in an active instance
	 * of the class pass as argument.
	 * <p>
	 * Use this class for example when an arbitrary action alter the internal
	 * content of another class and you want those class uptade their internal
	 * content data .
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
		toolBar.removeAll();
		toolBar.add(leftAndHomeGroup);
		toolBar.repaint();
		previous.setEnabled(components.size() > 1);
		home.setEnabled(components.size() > 1);
	}
}
