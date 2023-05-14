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
import java.util.List;

import javax.swing.Timer;

import org.jdesktop.application.*;

import com.alee.laf.progressbar.*;

public class TTaskManager {

	private WebProgressBar progressBar;
	private int poolSize = 10;
	private TaskMonitor monitor;
	private TaskService taskService;

	public TTaskManager() {
		this.monitor = Alesia.getInstance().getContext().getTaskMonitor();
		this.taskService = Alesia.getInstance().getContext().getTaskService();
		if (progressBar == null) {
			progressBar = new WebProgressBar(0, poolSize);
			progressBar.setStringPainted(true);
			// Values updater
			Timer t = new Timer(1000, (evt) -> updateTaskBar());
			t.setRepeats(true);
			t.start();
//			ComponentUpdater.install(progressBar, "", 1000, (evt) -> updateTaskBar());
		}
	}

	public TaskService getTaskService() {
		return taskService;
	}

	/**
	 * return the progress bar used by this class to update the status of active
	 * task, queue task, etc.
	 * 
	 * @return progress bar
	 */
	public WebProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * return <code>true</code> if the Alesia Environment can perform one more task.
	 * 
	 * @return <code>true</code> for 1 more, <code>false</code> if the Environment
	 *         is already a full capacity
	 */
	public boolean suporMoreTask() {
		List<Task> tasks = monitor.getTasks();
		int ac = (int) tasks.stream().filter(t -> t.isStarted()).count();
		return ac < poolSize;
	}

	private void updateTaskBar() {
		List<Task> tasks = monitor.getTasks();
		int ac = (int) tasks.stream().filter(t -> t.isStarted()).count();
		int qz = (int) tasks.stream().filter(t -> t.isPending()).count();
		progressBar.setEnabled(ac > 0);
		progressBar.setValue(ac);
		progressBar.setString("Actives: " + ac + " waiting: " + qz);
		float f = Math.abs((float) ((ac * .3 / poolSize) - .3)); // from green to red
		Color c = new Color(Color.HSBtoRGB(f, .85f, .85f));
		progressBar.setForeground(c);
	}
} 
