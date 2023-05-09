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
import java.awt.event.*;
import java.beans.*;
import java.util.List;

import javax.swing.Timer;

import org.jdesktop.application.*;

import com.alee.laf.progressbar.*;

public class TTaskManager implements PropertyChangeListener {

	private WebProgressBar progressBar;
	private int poolSize = 3;
	private TaskMonitor monitor;

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

	/**
	 * return the progress bar used by this class to update the status of active task, queue task, etc.
	 * 
	 * @return progress bar
	 */
	public WebProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * return <code>true</code> if the Alesia Environment can perform one more task.
	 * 
	 * @return <code>true</code> for 1 more, <code>false</code> if the Environment is already a full capacity
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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();

		if ("started".equals(propertyName)) {

		}
		if ("done".equals(propertyName)) {

		}
		if ("message".equals(propertyName)) {
			// String text = (String) (evt.getNewValue());

		}
		if ("progress".equals(propertyName)) {
			// int value = (Integer) (evt.getNewValue());
		}
	}
}

/**
 * check the inactivity time. If this time es reach, display signin dialgog
 * 
 * @author terry
 */
class CheckInactivity implements Runnable, MouseMotionListener {
	private static long lastMouseMove;
	private static int signOut;

	CheckInactivity() {
		// signOut = SystemVariables.getintVar("signout") * 60 * 1000;
		// lastMouseMove = System.currentTimeMillis();
		// Alesia.getInstance().getMainFrame().addMouseMotionListener(this);

	}
	@Override
	public void run() {
		// if (((System.currentTimeMillis() - lastMouseMove) > signOut) && (Session.getUser() != null)) {
		if (((System.currentTimeMillis() - lastMouseMove) > signOut)) {
			// TAbstractAction.shutdown();
			// System.exit(0);
			// Exit.shutdown();
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		lastMouseMove = System.currentTimeMillis();
	}
}
