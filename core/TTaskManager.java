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
import java.beans.*;
import java.util.List;

import javax.swing.*;

import org.jdesktop.application.*;

import com.alee.laf.progressbar.*;
import com.alee.managers.settings.*;
import com.alee.utils.swing.*;

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
			ComponentUpdater.install(progressBar, "", 1000, (evt) -> updateTaskBar());
		}
	}

	/**
	 * return the progress bar used by this class to update the status of active task, queque task, etc.
	 * 
	 * @return progress bar
	 */
	public WebProgressBar getProgressBar() {
		return progressBar;
	}

	private void updateTaskBar() {
		List<Task> tasks = monitor.getTasks();
		int ac = (int) tasks.stream().filter(t -> t.isStarted()).count();
		int qz = (int) tasks.stream().filter(t -> t.isPending()).count();
		progressBar.setEnabled(ac > 0);
		progressBar.setValue(ac);
		progressBar.setString("Activas: " + ac + " en espera: " + qz);
		float f = Math.abs((float) ((ac * .3 / poolSize) - .3)); // from green to red
		Color c = new Color(Color.HSBtoRGB(f, .85f, .85f));
		// progressBar.setProgressTopColor(c);
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
