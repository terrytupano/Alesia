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
package core;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;

import org.jdesktop.application.*;

import com.alee.laf.button.*;
import com.alee.laf.grouping.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.laf.toolbar.*;
import com.alee.laf.window.*;
import com.alee.managers.style.*;
import com.alee.utils.*;
import com.jgoodies.common.base.*;

public class TUIPanel extends WebPanel {

	protected Vector<Action> allActions;
	private JComponent bodyComponent;
	private WebPanel footerPanel;

	private WebDialog dialog;

	double aspectRatio = TUIUtils. ASPECT_RATION_DEFAULT;
	private WebToolBar toolBar;
	private WebLabel titleLabel;
	private WebLabel descriptionLabel;

	public TUIPanel() {
		super(new BorderLayout());
		this.allActions = new Vector<>();
		this.toolBar = TUIUtils.getWebToolBar();
		this.titleLabel = TUIUtils.getH1Label("terry");
		this.descriptionLabel = TUIUtils.getH3Label("Descrption for terry");
		this.footerPanel = new WebPanel();
		BoxLayout layout = new BoxLayout(footerPanel, BoxLayout.LINE_AXIS);
		footerPanel.setLayout(layout);
		footerPanel.add(Box.createHorizontalGlue());

		// set the toolbar visible only on add actions
		toolBar.setVisible(false);

		bodyComponent = new WebLabel("Terry");
		footerPanel.add(new WebButton("Terry"));
		WebPanel northPanel = TUIUtils.getInFormLayout(titleLabel, descriptionLabel, toolBar);
		northPanel.setBorder(TUIUtils.DOUBLE_EMPTY_BORDER);

		add(northPanel, BorderLayout.NORTH);
		add(bodyComponent, BorderLayout.CENTER);
		add(footerPanel, BorderLayout.SOUTH);
	}

	/**
	 * add to the current {@link #footerPanel} a {@link GroupPane} with all actions
	 * passed as argument.
	 * 
	 * @param actions - the actions to add
	 */
	public void addFooterActions(Action... actions) {
		Vector<JComponent> components = new Vector<>();
		for (Action act : actions) {
			allActions.add(act);
			TUIUtils.overRideIcons(TUIUtils.TOOL_BAR_ICON_SIZE, Color.black, act);
			WebButton wb = new WebButton(act);
			components.add(wb);
		}

		SwingUtils.equalizeComponentsWidth(components);
		GroupPane groupPane = new GroupPane(components.toArray(new WebButton[0]));
		groupPane.setBackground(Color.black);
		groupPane.setOpaque(true);
//		groupPane.setBorder(TUIUtils.STANDAR_EMPTY_BORDER);
		footerPanel.add(groupPane);
	}

	/**
	 * set an standard footer area for components intended to input data.
	 * <p>
	 * NOTE: the actions bust be located in {@link TActionsFactory} class
	 * 
	 * @param actions list of actions
	 */
	public void addFooterActions(String... actions) {
		List<Action> alist = TActionsFactory.getActions(actions);
		addFooterActions(alist.toArray(new Action[0]));
	}

	/**
	 * add a new action at the end of the toolbar panel
	 * 
	 * @param action - the action
	 */
	public void addToolBarAction(Action action) {
		allActions.add(action);
		WebButton wb = TUIUtils.getButtonForToolBar(action);
		toolBar.add(wb);
		toolBar.setVisible(true);
	}

	public void addToolBarAction(String action) {
		addToolBarAction(TActionsFactory.getAction(action));
	}

	public void addToolBarActions(Action... actions) {
		GroupPane toolBarPane = new GroupPane();
		for (Action action : actions) {
			allActions.add(action);
			WebButton save = TUIUtils.getButtonForToolBar(action);
			toolBarPane.add(save);
		}
		toolBar.add(toolBarPane);
		toolBar.setVisible(true);
	}

	public void addToolBarActions(ActionMap map) {
		ArrayList<javax.swing.Action> actions = new ArrayList<>();
		for (Object key : map.keys()) {
			actions.add(map.get(key));
		}
		addToolBarActions(actions);
	}

	public void addToolBarActions(List<Action> actions) {
		addToolBarActions(actions.toArray(new Action[0]));
	}

	public void addToolBarActions(String... actions) {
		addToolBarActions(TActionsFactory.getActions(actions));
	}

	public final WebDialog createDialog(boolean setAspectRatio) {
		// Preconditions.checkState(EventQueue.isDispatchThread(), "You must create and
		// show dialogs from the
		// Event-Dispatch-Thread (EDT).");
		// checkWindowTitle(title);
		if (dialog != null) {
			// dialog.setTitle(" ");
			return dialog;
		}
		dialog = new WebDialog(StyleId.dialogDecorated, Alesia.getMainFrame());

		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setContentPane(this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//		computeAndSetInitialDialogSize();
		if (setAspectRatio)
			TUIUtils.setDialogAspectRatio(dialog, aspectRatio);
		else
			dialog.pack();
		dialog.setLocationRelativeTo(Alesia.getMainFrame());
		return dialog;
	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public JComponent getBodyComponent() {
		return bodyComponent;
	}

	public WebPanel getFooterPanel() {
		return footerPanel;
	}

	public String getString(String stringKey) {
		return TStringUtils.getString(stringKey);
	}

	public WebToolBar getToolBar() {
		return toolBar;
	}

	public void setAllEnabledBut(boolean enabled, String... names) {
		Component[] cmps = toolBar.getComponents();
		List<String> listNames = Arrays.asList(names);
		for (Component cmp : cmps) {
			if (cmp instanceof AbstractButton) {
				ApplicationAction act = (ApplicationAction) ((AbstractButton) cmp).getAction();
				if (!listNames.contains(act.getName()))
					act.setEnabled(enabled);
			} else {
				if (!listNames.contains(cmp.getName()))
					cmp.setEnabled(enabled);
			}
		}
	}

	public final void setAspectRatio(double customValue) {
		Preconditions.checkArgument((customValue >= 0.0D),
				"The aspect ratio must positive, or ASPECT_RATION_NONE to disable the feature.");
		this.aspectRatio = customValue;
	}

	public void setBodyComponent(JComponent body) {
		setBodyComponent(body, false);
	}

	public void setBodyComponent(JComponent body, boolean withBorder) {
		if (bodyComponent != null) {
			remove(bodyComponent);
		}
		this.bodyComponent = body;
		if (withBorder)
			bodyComponent.setBorder(TUIUtils.STANDAR_EMPTY_BORDER);

		add(body, BorderLayout.CENTER);
	}

	/**
	 * Enable/Disable all the actions present in this component according to
	 * Parameters pass as arguments.
	 * <p>
	 * For example. the class {@link TUIFormPanel} has the <code>Accept </code>
	 * action. this action has a parameter
	 * <code>acept.Action.isCommint = true</code> that mark this action as an action
	 * for commit changes to the system.
	 * <p>
	 * call this method
	 * <code>enableInternalActions("isCommint", "true", false)</code> means that all
	 * actions whit property <code>.isCommit = true</code> will be disabled
	 * 
	 * @param property - indicate the property of the action to look for
	 * @param value    - the value of the param property must be equal tho this
	 *                 value
	 * @param enable   - boolean value to enable or disable de action.
	 */
	protected void setEnableActions(String property, String value, boolean enable) {
		for (Action a : allActions) {
			ApplicationAction aa = (ApplicationAction) a;
			String isc = aa.getResourceMap().getString(aa.getName() + ".Action." + property);
			if (isc != null && isc.equals(value))
				aa.setEnabled(enable);
		}
	}

	/**
	 * replace the current {@link #footerPanel} in the current layout with this new
	 * component
	 * <p>
	 * NOTE: the all {@link #footerPanel} is not more visible and out control of
	 * this class
	 * 
	 * @param footer - the new footer component
	 */
	public void setFooterComponent2(JComponent footer) {
		remove(footerPanel);
		add(footer, BorderLayout.SOUTH);
	}

	/**
	 * Same as {@link #setMessage(String, boolean, Object...)} but set the toolbar
	 * no visible
	 * 
	 * @param msgId   - message id for text
	 * @param msgData - Substitution data
	 */
	public void setMessage(String msgId, Object... msgData) {
//		setMessage(msgId, false, msgData);
	}

	/**
	 * set the title and description values for this panel
	 * 
	 * @param title       - the title
	 * @param description - the description
	 */
	public void setTitleDescription(String title, String description) {
		titleLabel.setText(title);
		if (description != null)
			descriptionLabel.setText(description);
	}

	/**
	 * set the title/description for this panel based on an action. the information
	 * is read from the action.s properties
	 * 
	 * @param action - the action
	 */
	public void setTitleDescriptionFromAction(String action) {
		String title = getString(action + ".Action.text");
		String description = getString(action + ".Action.shortDescription");
		setTitleDescription(title, description);
	}

}
