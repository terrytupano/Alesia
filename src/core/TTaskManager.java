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
import java.util.concurrent.*;

import javax.swing.Timer;

import org.jdesktop.application.*;

import com.alee.laf.progressbar.*;

public class TTaskManager {

	public final static int CORE_POOL_SIZE = 5;
	private final static int poolSize = 3 + CORE_POOL_SIZE; // 3 from default
	private WebProgressBar progressBar;
	private TaskMonitor monitor;
//	private TaskService taskService;

	public TTaskManager() {
		this.monitor = Alesia.getInstance().getContext().getTaskMonitor();
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
		ThreadPoolExecutor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, // corePool size
				7, // maximumPool size
				1L, TimeUnit.SECONDS, // non-core threads time to live
				new LinkedBlockingQueue<Runnable>());
		TaskService taskService = new TaskService("Simulations service", executor);
//		this.taskService = Alesia.getInstance().getContext().getTaskService();
		Alesia.getInstance().getContext().addTaskService(taskService);
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
		@SuppressWarnings("rawtypes")
		List<Task> tasks = monitor.getTasks();
		int ac = (int) tasks.stream().filter(t -> t.isStarted()).count();
		return ac < poolSize;
	}

	private void updateTaskBar() {
		@SuppressWarnings("rawtypes")
		List<Task> tasks = monitor.getTasks();
		long ac = tasks.stream().filter(t -> t.isStarted()).count();
		long qz = tasks.stream().filter(t -> t.isPending()).count();
		progressBar.setEnabled(ac > 0);
		progressBar.setValue((int) ac);
		progressBar.setString("     Actives: " + ac + " waiting: " + qz + "     "); // add some space
		float f = Math.abs((float) ((ac * .3 / poolSize) - .3)); // from green to red
		Color c = new Color(Color.HSBtoRGB(f, .85f, .85f));
		progressBar.setForeground(c);
	}
}
