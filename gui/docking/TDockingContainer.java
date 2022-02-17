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
package gui.docking;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.border.*;

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

import core.*;
import gui.*;

public class TDockingContainer extends WebPanel {

	private ComponentTransition transitionPanel;
	private WebToolBar toolBar;
	private SlideTransitionEffect effect;
	private HomePanel homePanel;
	private ApplicationAction home, previous, next;
	private ActionMap myMap;
	private boolean navAction;
	private int cmpCounter;
	private Vector<JComponent> cmpList;

	public TDockingContainer() {
		super(new BorderLayout());
		this.myMap = Alesia.getInstance().getContext().getActionMap(this);

		// toolbar
		this.previous = (ApplicationAction) myMap.get("previous");
		this.home = (ApplicationAction) myMap.get("home");
		this.next = (ApplicationAction) myMap.get("next");
		this.toolBar = TUIUtils.getWebToolBar(previous, home, next);

		// staus bar
		WebStatusBar statusBar = new WebStatusBar();
		WebLabel pd = new WebLabel(TStringUtils.getAboutAppShort(), TResources.getSmallIcon("alpha"));
		statusBar.add(pd);
		statusBar.addSpacing();
		statusBar.addToEnd(Alesia.getInstance().taskManager.getProgressBar());

		transitionPanel = new ComponentTransition();
		transitionPanel.setContent(TUIUtils.getBackgroundPanel());

		// Transition effect
		effect = new SlideTransitionEffect();
		effect.setDirection(Direction.left);
		effect.setType(SlideType.moveBoth);
		effect.setSpeed(50);

		// CurtainTransitionEffect effect = new CurtainTransitionEffect();
		// effect.setDirection(com.alee.extended.transition.effects.Direction.down);
		// effect.setType(CurtainType.fade);
		// effect.setSpeed(9);

		transitionPanel.setTransitionEffect(effect);

		this.cmpList = new Vector();
		this.cmpCounter = -1;
		this.navAction = false;
		sincronizeNavigation();
		this.homePanel = new HomePanel();

		add(toolBar, BorderLayout.NORTH);
		add(transitionPanel, BorderLayout.CENTER);
		add(statusBar, BorderLayout.SOUTH);
		showPanel(homePanel);
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
	 * Return a {@link JLabel} formatted to present warning message about problems with autorization for user
	 * 
	 * @param txt - text to format message
	 * 
	 * @return {@link JLabel}
	 */
	private static JLabel getLockPanel(String txt) {
		// JPanel jp = new JPanel(new BorderLayout());
		String msg = MessageFormat.format(TStringUtils.getString("security.msg01"), txt);
		JLabel jl = new JLabel(msg, TResources.getIcon("lock-panel", 48), JLabel.CENTER);
		jl.setVerticalTextPosition(JLabel.BOTTOM);
		jl.setHorizontalTextPosition(JLabel.CENTER);
		jl.setBorder(new EmptyBorder(4, 4, 4, 4));
		return jl;
	}

	/**
	 * add the listener all containers instances of <code>soruceClazz</code> inside of this component content panel.
	 * 
	 * @param sourceClazz - the class source of the property
	 * @param propertyName - the name
	 * @param listener - the listener interested in recive notification
	 */
	public void addChangeListener(String propertyName, PropertyChangeListener listener) {
		List<Container> cnts = SwingUtils.collectAllContainers(this);
		// avoid to add listerner to the "listener" itselft and other class disticg to TUIListPanel
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
		effect.setDirection(Direction.right);
		showPanel(homePanel);
	}

	@org.jdesktop.application.Action
	public void next(ActionEvent event) {
		navAction = true;
		effect.setDirection(Direction.left);
//		remove(cmpList.elementAt(cmpCounter));
		showPanel(cmpList.elementAt(++cmpCounter));
	}

	@org.jdesktop.application.Action
	public void previous(ActionEvent event) {
		navAction = true;
		effect.setDirection(Direction.right);
//		remove(cmpList.elementAt(cmpCounter));
		showPanel(cmpList.elementAt(--cmpCounter));

	}

	public void showPanel(JComponent newComponent) {
		if (!navAction) {
			if (cmpCounter > -1) {
				// TODO: comented beacuse the transision animation. temporal o definitive??
				// transitionPanel.remove(cmpList.elementAt(cmpCounter));
			}
			// si se presiona otro enlace,
			// se suprimen todos los elementos posteriores a la posicion actual
			if ((cmpCounter + 1) < cmpList.size()) {
				for (int i = (cmpCounter + 1); i < cmpList.size(); i++) {
					cmpList.remove(cmpCounter + 1);
				}
			}
			cmpList.add(newComponent);
			cmpCounter = cmpList.size() - 1;
		}
		navAction = false;
		sincronizeNavigation();

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
	 * Utility method to perform {@link UIListPanel#freshen()} in an active instance of the class pass as argument.
	 * <p>
	 * Use this class for example when an arbitrary action alter the internal content of another class and you want
	 * those class uptade their internal content data .
	 * 
	 * @param clsn - active instance to refresh
	 */
	public void signalFreshgen(Class clazz) {
		SwingUtilities.invokeLater(() -> {
			Object cnt = SwingUtils.getFirst(cmpList.elementAt(cmpCounter), clazz);
			Preconditions.checkArgument(cnt instanceof TUIListPanel, "the Class %s must be instance of TUIListPanel",
					clazz.getName(), TUIListPanel.class.getName());
			TUIListPanel tuilp = (TUIListPanel) cnt;
			tuilp.freshen();
		});
	}

	/**
	 * sincroniza los estados de las acciones de nevegacion para que esten acordes con los paneles presentados
	 *
	 */
	private void sincronizeNavigation() {
		next.setEnabled(true);
		previous.setEnabled(true);
		if (cmpCounter + 1 == cmpList.size()) {
			next.setEnabled(false);
		}
		if (cmpCounter < 1) {
			previous.setEnabled(false);
		}
	}
}
