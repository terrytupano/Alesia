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

import org.jdesktop.application.*;

import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.extended.window.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;

import core.*;

/**
 * Clone of original ProgressMonitor with is dysplay in a {@link WebPopOver}. this component display the progress of a
 * {@link Callable} implementaations and allow the user hide the dialog with run in background button and cancel the
 * operaticon whit cancel button. Cancel button are available only if this object was create passing a Future != null.
 * if future = null, cancel are disabled.
 * 
 * @author terry
 * 
 */
public class TTaskMonitor2 extends WebPopOver implements ActionListener, PropertyChangeListener, input {

	private JLabel progressLabel;
	private JProgressBar progressBar;
	private JButton cancel, background;
	private boolean allowBg = true;
	private Task task;

	public TTaskMonitor2(Task task, boolean allowBg) {
		super(Alesia.mainFrame);
		setModal(true);
		setMovable(false);
		
		this.task = task;
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
//		cancel.setEnabled(future != null);
		this.background = new JButton("Background");
		background.addActionListener(this);
		background.setEnabled(allowBg);

		JPanel jp = new JPanel(new VerticalFlowLayout());
		jp.setOpaque(false);
		jp.add(progressLabel);
		jp.add(progressBar);
		JPanel jp1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4,4));
		jp1.setOpaque(false);
		jp1.add(new JLabel(TResources.getIcon("wait", 32)));
		jp1.add(jp);

		// title bar
//		WebLabel i = new WebLabel(TResources.getIcon("wait", 16));
		WebLabel tit = new WebLabel(task.getTitle(), JLabel.CENTER);
//		t.setDrawShade(true);
//		GroupPanel gp = new GroupPanel(GroupingType.fillLast, 4, i, tit);
//		gp.setMargin(0, 0, 10, 0);
		setLayout(new VerticalFlowLayout());
		add(tit);
		add(jp1);
		add(new JLabel(" "));
		add(new GroupPanel(GroupingType.fillFirst, true, new JLabel(), background, cancel));
	}

	public boolean getAllowBackground() {
		return allowBg;
	}
	/**
	 * set the progress for this component. the numeric value are in 0-100 range. 0 value set the progress var in
	 * indeterminate state.
	 * 
	 * @param v - value for JProgressBar (0-100). 0 to indeterminate.
	 * @param tid - bundle id for text component. can be <code>null</code>
	 */
	public void setProgress(int v, String tid) {
		if (tid != null) {
			progressLabel.setText(TStringUtils.getString(tid));
		}
		progressBar.setValue(v);
		progressBar.setStringPainted(v > 0);
		progressBar.setIndeterminate(v == 0);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancel) {
			task.cancel(true);
			dispose();
		}
		if (e.getSource() == background) {
			dispose();
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == task) {
		if(evt.getNewValue().equals("progress"))	
			setProgress((Integer) evt.getNewValue(), null);
		}		
	}
}
