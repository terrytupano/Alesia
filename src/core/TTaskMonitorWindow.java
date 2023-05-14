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
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.util.concurrent.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.application.*;
import org.jdesktop.application.Task.*;

import com.alee.extended.window.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;

/**
 * Clone of original ProgressMonitor with is display in a {@link WebPopOver}.
 * this component display the progress of a {@link Callable} implementations and
 * allow the user hide the dialog with run in background button and cancel the
 * Operation whit cancel button. Cancel button are available only if this object
 * was create passing a Future != null. if future = null, cancel are disabled.
 * 
 * @author terry
 * 
 */
public class TTaskMonitorWindow extends InputBlocker implements PropertyChangeListener {

	private WebLabel progressLabel;
	private JProgressBar progressBar;
	private Task<?, ?> task;
	private BusyPanel busyPanel;
	private Component oldGlassPanel;
	private JDialog dialog;

	public TTaskMonitorWindow(Task<?, ?> task) {
		super(task, Task.BlockingScope.WINDOW, Alesia.getMainPanel());
		TActionsFactory.insertActions(this);

		this.busyPanel = new BusyPanel();
		this.task = task;
		task.addPropertyChangeListener(this);

		this.progressLabel = new WebLabel(task.getMessage());
		this.progressBar = new JProgressBar();
		progressBar.setStringPainted(false);
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		progressBar.setMaximum(100);
		progressBar.setIndeterminate(true);

		JLabel leftLabel = new JLabel(TResources.getIcon("wait.png", 32));

		JPanel centerPanel = TUIUtils.getLineLayoutPanel(leftLabel, progressLabel, progressBar, new JLabel());
		centerPanel.setBorder(TUIUtils.STANDAR_EMPTY_BORDER);

		// TODO: temp. use TUIUtils.setDialogAspectRatio or
		// Alesia.getMainFrame().getBoundByFactor
		centerPanel.setPreferredSize(new Dimension(340, 0));
		setContent(centerPanel);
	}

	public void setContent(Component dialogContent) {
		this.dialog = TUIUtils.getDialog(task.getTitle(), dialogContent, "cancelTask");

		// cancel the associated table on close
		WindowAdapter wa = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				unblock();
			};
		};

		dialog.addWindowListener(wa);
	}

	@org.jdesktop.application.Action
	public void cancelTask() {
		unblock();
	}

	@Override
	protected void block() {
		oldGlassPanel = Alesia.getMainFrame().getGlassPane();
		Alesia.getMainFrame().setGlassPane(busyPanel);
		busyPanel.setVisible(true);
		dialog.setLocationRelativeTo(Alesia.getMainFrame());
		dialog.setVisible(true);
	}

	@Override
	protected void unblock() {
		dialog.dispose();
		task.cancel(true);
		busyPanel.setVisible(false);
		Alesia.getMainFrame().setGlassPane(oldGlassPanel);
	}

	/**
	 * The TaskMonitor (constructor arg) tracks a "foreground" task; this method is
	 * called each time a foreground task property changes.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		String propertyName = e.getPropertyName();
		if (Task.PROP_STARTED.equals(propertyName)) {
			progressBar.setEnabled(true);
			progressBar.setIndeterminate(true);
		} else if (Task.PROP_DONE.equals(propertyName)) {
			progressBar.setIndeterminate(false);
			progressBar.setEnabled(false);
			progressBar.setValue(0);
		} else if (Task.PROP_MESSAGE.equals(propertyName)) {
			String text = (String) (e.getNewValue());
			progressLabel.setText(text);
			// } else if (Task.PROP_COMPLETED.equals(propertyName)) {
		} else if ("progress".equals(propertyName)) {
			int value = (Integer) (e.getNewValue());
			progressBar.setEnabled(true);
			progressBar.setIndeterminate(false);
			progressBar.setValue(value);
			progressBar.setStringPainted(true);
		}
	}
}
