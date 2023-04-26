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
import javax.swing.border.*;

import org.jdesktop.application.*;

import com.alee.extended.panel.*;
import com.alee.laf.button.*;
import com.alee.laf.grouping.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.laf.toolbar.*;
import com.alee.laf.window.*;
import com.alee.managers.style.*;
import com.alee.utils.*;
import com.jgoodies.common.base.*;
import com.jgoodies.forms.layout.*;

import core.*;



/**
 * base class for application ui manage. this class is divided in tritle
 * component, body component and footer component. the base implementation
 * create a title component that consist in a title label, a 3 dot button and a
 * aditional information component. the behabior of the 3dot buttons can bi
 * setted via {@link #set3DotBehavior(int)} method. the title of this component
 * (title component and 3dot button) can be set visible/invisible leaving the
 * aditional information alone. Aditional information can be set visible or not.
 * 
 * @author terry
 *
 */
public class TUIPanel extends WebPanel {

	public class ListMouseProcessor extends MouseAdapter {

		private JComponent invoker;

		public ListMouseProcessor(JComponent in) {
			this.invoker = in;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				if (doubleClickAction != null && doubleClickAction.isEnabled()) {
					ActionEvent ae = new ActionEvent(doubleClickAction, 0, "no cmd");
					doubleClickAction.actionPerformed(ae);
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			showPopup(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			showPopup(e);
		}


		private void showPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				// verifica null porque x autorizciones, pueden no haber elementos
				if (popupMenu != null) {
					popupMenu.show(invoker, e.getX(), e.getY());
					// dynamicMenu.showMenu(invoker, e.getX(), e.getY());
				}
			}
		}
	}

	public static double ASPECT_RATION_NONE = 0.0;
	public static double ASPECT_RATION_NARROW = 1.3333;
	public static double ASPECT_RATION_DEFAULT = 1.6666;
	public static double ASPECT_RATION_WIDE = 1.7777;
	protected Vector<Action> allActions;
	private JComponent bodyJComponent, footerJComponent;
	private WebLabel titleLabel;
	// private ActionMap actionMap;
	private Box bodyMessageJComponent;

	private JLabel blkinfoLabel;

	private JEditorPane additionalInfo;
	private JPanel titlePanel;
	private WebDialog dialog;

	double aspectRatio = ASPECT_RATION_DEFAULT;
	private JPopupMenu popupMenu;
	private Action doubleClickAction;

	private WebToolBar toolBar;

	public TUIPanel() {
		super(new BorderLayout());
		this.allActions = new Vector<>();
		this.titleLabel = new WebLabel(" ");
		titleLabel.setFont(TUIUtils.H1_Font);
		// actionMap = Alesia.getInstance().getContext().getActionMap((TUIPanel) this);
		this.toolBar = TUIUtils.getWebToolBar();

		// tilte label + 3dot button
		this.titlePanel = new WebPanel(StyleId.panelTransparent);
		titlePanel.setLayout(new BorderLayout());
		// titlePanel.add(titleLabel, BorderLayout.CENTER);
		titlePanel.add(toolBar, BorderLayout.CENTER);
		// titlePanel.add(treeDotButton, BorderLayout.EAST);

		this.additionalInfo = TUIUtils.getJEditorPane(null, null);
		additionalInfo.setPreferredSize(new Dimension(0, 48));

		// noListPanel are used to display a message when instances of this component
		// show a list of elements and
		// such list has no elements to display.
		this.bodyMessageJComponent = Box.createVerticalBox();
		this.blkinfoLabel = new JLabel();
		blkinfoLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
		bodyMessageJComponent.add(Box.createVerticalStrut(8));
		bodyMessageJComponent.add(blkinfoLabel);
		bodyMessageJComponent.add(Box.createVerticalGlue());

		WebPanel north = new WebPanel(StyleId.panelTransparent);
		north.setLayout(new BorderLayout());
		north.add(titlePanel, BorderLayout.NORTH);
		north.add(additionalInfo, BorderLayout.CENTER);

		// by default
		showAditionalInformation(false);

		add(north, BorderLayout.NORTH);
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

	/**
	 * add a new action at the end of the toolbar panel
	 * 
	 * @param action - the action
	 */
	public void addToolBarAction(Action action) {
		allActions.add(action);
		WebButton wb = TUIUtils.getWebButtonForToolBar(action);
		ApplicationAction aa = (ApplicationAction) action;
		String sco = aa.getResourceMap().getString(aa.getName() + ".Action.scope");

		// auto add the property TActionsFactory.TUILISTPANEL
		// if (sco != null && (sco.equals("element") || sco.equals("list"))) {
		// aa.putValue(TActionsFactory.TUILISTPANEL, this);
		// }

		// action for popup menu
		if (sco != null && sco.equals("element")) {
			JMenuItem jmi = new JMenuItem(action);
			jmi.setIcon(null);
			// temp: doble click for editModel
			if (aa.getName().equals("editModel")) {
				this.doubleClickAction = action;
				jmi.setFont(jmi.getFont().deriveFont(Font.BOLD));
			}
			popupMenu.add(jmi);
		}
		toolBar.add(wb);
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

	/**
	 * set the toolbar for this component. This toolbar will replace the title label
	 * of this component. Use thid method when you need a full toolbar available for
	 * component that requirer many actions (like editors). other whise, use the
	 * 3dot bar.
	 * 
	 * @param actions actions to set inside the bar.
	 */
	public void addToolBarActions(List<Action> actions) {
		// toolBarPanel.removeAll();
//		popupMenu = new JPopupMenu();
		// ArrayList<JComponent> componets = new ArrayList<>();
		for (Action act : actions) {
			addToolBarAction(act);
		}

		// 171231: append some standar actions for list sublcases
		// toolBarPanel.add(TUIUtils.getWebButtonForToolBar(actionMap.get("filterList")),
		// LineLayout.END);
		// toolBarPanel.add(TUIUtils.getWebButtonForToolBar(actionMap.get("refreshList")),
		// LineLayout.END);
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
		// standar behavior: if the title of the tuipanel is visible, this method remove
		// the string and put in as this
		// dialog title
		if (isTitleVisible()) {
			dialog.setTitle(getTitleText());
			setTitleVisible(false);
		}

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

	@org.jdesktop.application.Action
	public void filterList(ActionEvent event) {

	}

	public double getAspectRatio() {
		return aspectRatio;
	}

	public String getTitleText() {
		return titleLabel.getText();
	}

	public WebToolBar getToolBar() {
		return toolBar;
	}

	public boolean isTitleVisible() {
		return titleLabel.isVisible();
	}

	@org.jdesktop.application.Action
	public void refreshList(ActionEvent event) {

	}

	public void set3DotBehavior(int behavior) {

	}

	public final void setAspectRatio(double customValue) {
		Preconditions.checkArgument((customValue >= 0.0D),
				"The aspect ratio must positive, or ASPECT_RATION_NONE to disable the feature.");
		this.aspectRatio = customValue;
	}

	public void setBodyComponent(JComponent body) {
		if (bodyJComponent != null) {
			remove(bodyJComponent);
		}
		this.bodyJComponent = body;
		add(body, BorderLayout.CENTER);
	}

	public void setDescription(String tId) {
		additionalInfo.setText(Alesia.getInstance().getResourceMap().getString(tId));
	}

	/**
	 * set an standard footer area for components intended to input data.
	 * 
	 * @param actions Actions to add
	 */
	public void setFooterActions(Action... actions) {
		Vector<JComponent> lst = new Vector<>();
		for (Action act : actions) {
			allActions.add(act);
			TUIUtils.overRideIcons(TUIUtils.TOOL_BAR_ICON_SIZE, Color.black, act);
			WebButton wb = new WebButton(act);
			// ApplicationAction aa = (ApplicationAction) act;
			// String sco = aa.getResourceMap().getString(aa.getName() + ".Action.scope");
			lst.add(wb);
		}

		GroupPane pane = new GroupPane((JComponent[]) lst.toArray(new JComponent[lst.size()]));
		GroupPanel anel = new GroupPanel(GroupingType.fillFirst, true, new WebLabel(), pane);
		SwingUtils.equalizeComponentsWidth(pane.getComponents());
		anel.setBorder(TUIUtils.STANDAR_EMPTY_BORDER);
		setFooterComponent(anel);
	}

	/**
	 * set an standar footer area for components intendet to input data.
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
		if (footerJComponent != null) {
			remove(footerJComponent);
		}
		this.footerJComponent = footer;
		// add decoration
		footerJComponent.setOpaque(true);
		footerJComponent.setBackground(getBackground().brighter());
		Border border = footer.getBorder();
		MatteBorder border2 = new MatteBorder(1, 0, 0, 0, Color.GRAY);
		CompoundBorder border3 = new CompoundBorder(border2, border);
		footerJComponent.setBorder(border3);
		add(footerJComponent, BorderLayout.SOUTH);
	}

	/**
	 * replace the {@link JComponent} set using the metod
	 * {@link #setBodyComponent(JComponent)} and present a new componet to display
	 * the selected mensaje. If the msgId parameter is <code>null</code>, hide the
	 * message componet and present the body component
	 * 
	 * @param messageId     - message id for text
	 * @param visibleTB - indicati if the toolbarpanel will be visible or not
	 *                  visible
	 * @param msgData   - Sustitution data
	 * 
	 * @see UIComponentPanel#getToolBar()
	 */
	public void setMessage(String messageId, boolean visibleTB, Object... msgData) {
		if (messageId == null) {
			toolBar.setVisible(true);
			remove(bodyMessageJComponent);
			add(bodyJComponent, BorderLayout.CENTER);
		} else {
			TValidationMessage message = new TValidationMessage(messageId, msgData);
//			blkinfoLabel.setIcon(te.getIcon());
			blkinfoLabel.setText(message.formattedText());
			// blkinfoLabel.setVerticalTextPosition(JLabel.TOP);

			toolBar.setVisible(visibleTB);
			if (bodyJComponent != null)
				remove(bodyJComponent);
			add(bodyMessageJComponent, BorderLayout.CENTER);
		}
		repaint();
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

	public void setTitle(String txtId) {
		titleLabel.setText(TStringUtils.getString(txtId));
	}

	public void setTitleComponent(JComponent title) {
		add(title, BorderLayout.NORTH);
	}

	public void setTitleVisible(boolean aFlag) {
		this.titlePanel.setVisible(aFlag);
	}

	public void showAditionalInformation(boolean aFlag) {
		this.additionalInfo.setVisible(aFlag);
	}

	@org.jdesktop.application.Action
	public void treeDot(ActionEvent event) {

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

}
