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

import javax.swing.*;

import org.jdesktop.application.*;
import org.jdesktop.application.Task.*;

public class TaskDialog extends InputBlocker {

	private BusyPanel busyPanel;
	private Component oldGlassPanel;
	private JDialog dialog;
	private TaskGroup taskGroup;

	public TaskDialog(TaskGroup taskGroup) {
		super(taskGroup, Task.BlockingScope.WINDOW, Alesia.getMainPanel());
		TActionsFactory.insertActions(this);
		this.taskGroup = taskGroup;
//		taskGroup.addPropertyChangeListener(this);
		taskGroup.setInputBlocker(this);
		this.busyPanel = new BusyPanel();
	}

	@org.jdesktop.application.Action
	public void cancelTask() {
		unblock();
	}

	@Override
	protected void block() {
		oldGlassPanel = Alesia.getMainFrame().getGlassPane();
		Alesia.getMainFrame().setGlassPane(busyPanel);
		this.dialog = TUIUtils.getDialog("Tasks", taskGroup.getTaskPanel(), "cancelTask");

		// cancel the associated table on close
		WindowAdapter wa = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				unblock();
			};
		};
		dialog.addWindowListener(wa);

		busyPanel.setVisible(true);
		dialog.setLocationRelativeTo(Alesia.getMainFrame());
		dialog.setVisible(true);
	}

	@Override
	protected void unblock() {
		dialog.dispose();
		taskGroup.cancel(false);
		busyPanel.setVisible(false);
		Alesia.getMainFrame().setGlassPane(oldGlassPanel);
	}

}
