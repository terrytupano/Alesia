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
import java.beans.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.application.*;
import org.jdesktop.application.Task.*;

import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.extended.window.*;
import com.alee.laf.label.*;

import core.*;
import gui.wlaf.*;

/**
 * Clone of original ProgressMonitor with is dysplay in a {@link WebPopOver}. this component display the progress of a
 * {@link Callable} implementaations and allow the user hide the dialog with run in background button and cancel the
 * operaticon whit cancel button. Cancel button are available only if this object was create passing a Future != null.
 * if future = null, cancel are disabled.
 * 
 * @author terry
 * 
 */
public class TTaskMonitor extends InputBlocker implements ActionListener, PropertyChangeListener {

	private JLabel progressLabel;
	private WebPopup webPopup;
	private JProgressBar progressBar;
	private JButton cancel, background;
	private boolean allowBg = true;
	private Task task;
	private BusyPanel busyPanel;
	private Component oldGlassPanel;

	public TTaskMonitor(Task task, boolean allowBg) {
		super(task, Task.BlockingScope.WINDOW, Alesia.getInstance().getMainPanel());
		webPopup = new WebPopup<>();
		webPopup.setPadding(4);
		webPopup.setResizable(false);
		webPopup.setDraggable(false);
		webPopup.setCloseOnFocusLoss(false);
		webPopup.setCloseOnOuterAction(false);

		this.busyPanel = new BusyPanel();
		this.task = task;
		task.addPropertyChangeListener(this);
		this.progressLabel = new JLabel(task.getTitle());
		this.progressBar = new JProgressBar();
		this.allowBg = allowBg;
		progressBar.setStringPainted(false);
		Dimension d = progressBar.getPreferredSize();
		d = new Dimension(300, d.height);
		progressBar.setPreferredSize(d);
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setMaximum(100);
		progressBar.setIndeterminate(true);

		this.cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		// cancel.setEnabled(future != null);
		this.background = new JButton("Background");
		background.addActionListener(this);
		background.setEnabled(allowBg);

		JPanel jp = new JPanel(new VerticalFlowLayout());
		jp.setOpaque(false);
		jp.add(progressLabel);
		jp.add(progressBar);
		JPanel jp1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
		jp1.setOpaque(false);
		jp1.add(new JLabel(TResources.getIcon("wait", 32)));
		jp1.add(jp);

		// title bar
		// WebLabel i = new WebLabel(TResources.getIcon("wait", 16));
		WebLabel tit = new WebLabel(task.getTitle(), JLabel.CENTER);
		// t.setDrawShade(true);
		// GroupPanel gp = new GroupPanel(GroupingType.fillLast, 4, i, tit);
		// gp.setMargin(0, 0, 10, 0);
		webPopup.setLayout(new VerticalFlowLayout());
		webPopup.add(tit);
		webPopup.add(jp1);
		webPopup.add(new JLabel(" "));
		webPopup.add(new GroupPanel(GroupingType.fillFirst, true, new JLabel(), background, cancel));
	}

	public boolean getAllowBackground() {
		return allowBg;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancel) {
			task.cancel(true);
			webPopup.hidePopup();
		}
		if (e.getSource() == background) {
			webPopup.hidePopup();
		}
	}

	@Override
	protected void block() {
		oldGlassPanel = Alesia.getInstance().getMainFrame().getGlassPane();
		Alesia.getInstance().getMainFrame().setGlassPane(busyPanel);
		busyPanel.setVisible(true);
		Point c = TWebFrame.getCenter(Alesia.getInstance().getMainFrame(), webPopup);
		webPopup.showPopup(busyPanel, c);
	}

	@Override
	protected void unblock() {
		webPopup.hidePopup();
		busyPanel.setVisible(false);
		Alesia.getInstance().getMainFrame().setGlassPane(oldGlassPanel);
	}

	/**
	 * The TaskMonitor (constructor arg) tracks a "foreground" task; this method is called each time a foreground task
	 * property changes.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		String propertyName = e.getPropertyName();
		if ("started".equals(propertyName)) {
			progressBar.setEnabled(true);
			progressBar.setIndeterminate(true);
		} else if ("done".equals(propertyName)) {
			progressBar.setIndeterminate(false);
			progressBar.setEnabled(false);
			progressBar.setValue(0);
		} else if ("message".equals(propertyName)) {
			String text = (String) (e.getNewValue());
			progressLabel.setText(text);
		} else if ("progress".equals(propertyName)) {
			int value = (Integer) (e.getNewValue());
			progressBar.setEnabled(true);
			progressBar.setIndeterminate(false);
			progressBar.setValue(value);
		}
	}

	/*
	 * This component is intended to be used as a GlassPane only to consumes mouse and keyboard input.
	 */
	private static class BusyPanel extends JComponent {

		BusyPanel() {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			MouseInputListener blockMouseEvents = new MouseInputAdapter() {
			};
			addMouseMotionListener(blockMouseEvents);
			addMouseListener(blockMouseEvents);
			InputVerifier retainFocusWhileVisible = new InputVerifier() {
				public boolean verify(JComponent c) {
					return !c.isVisible();
				}
			};
			setInputVerifier(retainFocusWhileVisible);
		}
	}
}
