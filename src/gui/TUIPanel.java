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
package gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;

import org.jdesktop.application.*;

import com.alee.extended.panel.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.laf.toolbar.*;
import com.alee.laf.window.*;
import com.alee.managers.style.*;
import com.alee.utils.*;
import com.jgoodies.common.base.*;
import com.jgoodies.forms.layout.*;

import core.*;

public class TUIPanel extends WebPanel {

	public static double ASPECT_RATION_NONE = 0.0;
	public static double ASPECT_RATION_NARROW = 1.3333;
	public static double ASPECT_RATION_DEFAULT = 1.6666;
	public static double ASPECT_RATION_WIDE = 1.7777;
	protected Vector<Action> allActions;
	private JComponent bodyComponent, footerComponent;

	private WebDialog dialog;

	double aspectRatio = ASPECT_RATION_DEFAULT;
	private WebToolBar toolBar;
	private WebLabel titleLabel;
	private WebLabel descriptionLabel;

	public TUIPanel() {
		super(new BorderLayout());
		this.allActions = new Vector<>();
		this.toolBar = TUIUtils.getWebToolBar();
		this.titleLabel = TUIUtils.getH1Title("terry");
		this.descriptionLabel = TUIUtils.getH3Label("Descrption for terry");

		// set the toolbar visible only on add actions
		toolBar.setVisible(false);

		bodyComponent = new WebLabel("Terry");
		footerComponent = new WebButton("Terry");
		WebPanel northPanel = TUIUtils.getInFormLayout(titleLabel, descriptionLabel, toolBar);
		northPanel.setBorder(TUIUtils.DOUBLE_EMPTY_BORDER);

		add(northPanel, BorderLayout.NORTH);
		add(bodyComponent, BorderLayout.CENTER);
		add(footerComponent, BorderLayout.SOUTH);
	}

	/**
	 * set the title and description values for this panel
	 * 
	 * @param title - the title
	 * @param description - the description
	 */
	public void setTitleDescription(String title, String description) {
		titleLabel.setText(title);
		if (description != null)
			descriptionLabel.setText(description);
	}

	/**
	 * add a new action at the end of the toolbar panel
	 * 
	 * @param action - the action
	 */
	public void addToolBarAction(Action action) {
		allActions.add(action);
		WebButton wb = TUIUtils.getWebButtonForToolBar(action);
		toolBar.add(wb);
		toolBar.setVisible(true);
	}

	public void addToolBarActions(Action... actions) {
		addToolBarActions(Arrays.asList(actions));
	}

	/**
	 * perform {@link #addToolBarActions(List)} whit all the actions inside map
	 * argument
	 * 
	 * @param map - instance of actionMap
	 */
	public void addToolBarActions(ActionMap map) {
		ArrayList<javax.swing.Action> actions = new ArrayList<>();
		for (Object key : map.keys()) {
			actions.add(map.get(key));
		}
		addToolBarActions(actions);
	}

	public void addToolBarActions(List<Action> actions) {
		for (Action act : actions) {
			addToolBarAction(act);
		}
	}

	/**
	 * perform {@link #addToolBarActions(List)} with the actions name found in
	 * {@link TActionsFactory}
	 * 
	 * @param actions - action name array
	 */
	public void addToolBarActions(String... actions) {
		addToolBarActions(TActionsFactory.getActions(actions));
	}

	protected void computeAndSetInitialDialogSize() {
		if (getPreferredSize().width <= 0) {
			dialog.pack();
			return;
		}
		// dialog.addNotify();
		int targetWidth = Sizes.dialogUnitXAsPixel(getPreferredSize().width, dialog);
		dialog.setSize(targetWidth, 2147483647);
		dialog.validate();
		invalidateComponentTree(this);
		Dimension dialogPrefSize = dialog.getPreferredSize();
		int targetHeight = dialogPrefSize.height;
		dialog.setSize(targetWidth, targetHeight);
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
		dialog = new WebDialog(StyleId.dialogDecorated, Alesia.getInstance().getMainFrame());

		dialog.setModal(true);
		dialog.setResizable(false);
		dialog.setContentPane(this);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		computeAndSetInitialDialogSize();
		if (setAspectRatio)
			setDialogAspectRatio();
		else
			dialog.pack();
		dialog.setLocationRelativeTo(Alesia.getInstance().getMainFrame());
		return dialog;
	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public JComponent getBodyComponent() {
		return bodyComponent;
	}

	public WebToolBar getToolBar() {
		return toolBar;
	}

	private void invalidateComponentTree(Component c) {
		invalidate();
		// if (c instanceof Container) {
		// Container container = (Container) c;
		// for (Component child : container.getComponents())
		// invalidateComponentTree(child);
		// container.invalidate();
		// }
	}

	@org.jdesktop.application.Action
	public void refreshList(ActionEvent event) {

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

	public void setBodyComponent(JComponent body, boolean withBorder) {
		if (bodyComponent != null) {
			remove(bodyComponent);
		}
		this.bodyComponent = body;
		if (withBorder)
			bodyComponent.setBorder(TUIUtils.STANDAR_EMPTY_BORDER);

		add(body, BorderLayout.CENTER);
	}

	public void setBodyComponent(JComponent body) {
		setBodyComponent(body, false);
	}

	protected void setDialogAspectRatio() {
		int targetHeight;
		Dimension size;
		if (getAspectRatio() == ASPECT_RATION_NONE)
			return;
		do {
			size = dialog.getSize();
			targetHeight = (int) Math.round(size.width / getAspectRatio());
			if (size.height == targetHeight)
				return;
			if (size.height < targetHeight) {
				dialog.setSize(size.width, targetHeight);
				return;
			}
			dialog.setSize(size.width + 10, size.height);
			dialog.validate();
			invalidateComponentTree(this);
			Dimension dialogPrefSize = dialog.getPreferredSize();
			int newPrefHeight = dialogPrefSize.height;
			dialog.setSize((dialog.getSize()).width, newPrefHeight);
		} while (size.height > targetHeight);
	}

	/**
	 * Enable/Disable all the actions present in this component acordint to
	 * parametars pass as arguments.
	 * <p>
	 * For example. the class {@link TUIFormPanel} has the <code>Acept </code>
	 * action. this action has a paremeter
	 * <code>acept.Action.isCommint = true</code> that mark this action as an action
	 * for commit changes to the sistem.
	 * <p>
	 * call this metodo
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
	 * set an standard footer area for components intended to input data.
	 * 
	 * @param actions Actions to add
	 */
	public void setFooterActions(Action... actions) {
		Vector<JComponent> components = new Vector<>();
		// component to stretch
		components.add(new WebLabel());
		for (Action act : actions) {
			allActions.add(act);
			TUIUtils.overRideIcons(TUIUtils.TOOL_BAR_ICON_SIZE, Color.black, act);
			WebButton wb = new WebButton(act);
			components.add(wb);
		}

		GroupPanel groupPanel = new GroupPanel(GroupingType.fillFirst, true,
				components.toArray(new JComponent[components.size()]));
		SwingUtils.equalizeComponentsWidth(components);
		groupPanel.setBorder(TUIUtils.STANDAR_EMPTY_BORDER);
		setFooterComponent(groupPanel);
	}

	/**
	 * set an standard footer area for components intended to input data.
	 * <p>
	 * NOTE: the actions bust be located in {@link TActionsFactory} class
	 * 
	 * @param actions list of actions
	 */
	public void setFooterActions(String... actions) {
		List<Action> alist = TActionsFactory.getActions(actions);
		setFooterActions(alist.toArray(new Action[0]));
	}

	public void setFooterComponent(JComponent footer) {
		if (footerComponent != null) {
			remove(footerComponent);
		}
		this.footerComponent = footer;
//		// add decoration
//		footerComponent.setOpaque(true);
//		footerComponent.setBackground(getBackground().brighter());
//		Border border = footer.getBorder();
//		MatteBorder border2 = new MatteBorder(1, 0, 0, 0, Color.GRAY);
//		CompoundBorder border3 = new CompoundBorder(border2, border);
//		footerComponent.setBorder(border3);
		add(footerComponent, BorderLayout.SOUTH);
	}

	/**
	 * Same as {@link #setMessage(String, boolean, Object...)} but set the toolbar
	 * no visible
	 * 
	 * @param msgId   - message id for text
	 * @param msgData - Sustitution data
	 */
	public void setMessage(String msgId, Object... msgData) {
		setMessage(msgId, false, msgData);
	}

	@org.jdesktop.application.Action
	public void treeDot(ActionEvent event) {

	}

}
