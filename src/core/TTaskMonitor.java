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
import java.beans.*;

import javax.swing.*;

import org.jdesktop.application.*;

import com.alee.laf.button.*;
import com.alee.laf.label.*;

public class TTaskMonitor implements PropertyChangeListener {

	private WebLabel progressLabel;
	private JProgressBar progressBar;
	private Task<?, ?> task;
	private WebButton cancelButton;
	private JPanel taskPanel;

	public TTaskMonitor(Task<?, ?> task) {
		TActionsFactory.insertActions(this);
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
		this.cancelButton = TUIUtils.getSmallButton("cancelTask");
		this.taskPanel = TUIUtils.getLineLayoutPanel(leftLabel, progressLabel, progressBar, cancelButton);
		taskPanel.setBorder(TUIUtils.STANDAR_EMPTY_BORDER);

		// TODO: temp. use TUIUtils.setDialogAspectRatio or
		// Alesia.getMainFrame().getBoundByFactor
		taskPanel.setPreferredSize(new Dimension(340, 0));
	}

	public JPanel getTaskPanel() {
		return taskPanel;
	}

	@org.jdesktop.application.Action
	public void cancelTask() {
		task.cancel(true);
	}

	/**
	 * The TaskMonitor (constructor arg) tracks a "foreground" task; this method is
	 * called each time a foreground task property changes.
	 */
	public void propertyChange(PropertyChangeEvent e) {
		String propertyName = e.getPropertyName();
		if (Task.PROP_STARTED.equals(propertyName)) {
			progressLabel.setText("Task status: " + propertyName);
			progressBar.setEnabled(true);
			progressBar.setIndeterminate(true);
		}
		if (Task.PROP_COMPLETED.equals(propertyName) || Task.PROP_DONE.equals(propertyName)
				|| Task.PROP_USERCANCANCEL.equals(propertyName)) {
			progressLabel.setText("Task status: " + propertyName);
			progressBar.setEnabled(false);
		}
		if (Task.PROP_MESSAGE.equals(propertyName)) {
			String text = (String) (e.getNewValue());
			progressLabel.setText(text);
		}
		if ("progress".equals(propertyName)) {
			int value = (Integer) (e.getNewValue());
			progressBar.setEnabled(true);
			progressBar.setIndeterminate(false);
			progressBar.setValue(value);
			progressBar.setStringPainted(true);
		}
	}

}
