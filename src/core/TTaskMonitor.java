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

import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.extended.window.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;



/**
 * Clone of original ProgressMonitor with is display in a {@link WebPopOver}. this component display the progress of a
 * {@link Callable} implementations and allow the user hide the dialog with run in background button and cancel the
 * Operation whit cancel button. Cancel button are available only if this object was create passing a Future != null.
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
	private WindowAdapter adapter;

	public TTaskMonitor(Task task, boolean allowBg) {
		super(task, Task.BlockingScope.WINDOW, Alesia.getMainPanel());
		webPopup = new WebPopup<>();
		webPopup.setPadding(4);
		webPopup.setResizable(false);
		webPopup.setDraggable(false);
		webPopup.setCloseOnFocusLoss(false);
		webPopup.setCloseOnOuterAction(false);
//		webPopup.setAlwaysOnTop(true);
//		webPopup.setAnimate(false);

		// TODO: verify ProgersMonitor to corrert visivility problem in
		// ProgressMonitor
		adapter = new WindowAdapter() {
			public void windowDeiconified(WindowEvent e) {
//				System.out.println("windowDeiconified");
			}
		    public void windowActivated(WindowEvent e) {
		    	webPopup.hidePopup();
				showPopUp();
		    }


		};
		Alesia.getMainFrame().addWindowListener(adapter);

		this.busyPanel = new BusyPanel();
		this.task = task;
		task.addPropertyChangeListener(this);
		this.progressLabel = new JLabel(task.getTitle());
		this.progressBar = new JProgressBar();
		this.allowBg = allowBg;
		progressBar.setStringPainted(false);
		Dimension d = progressBar.getPreferredSize();
		d = new Dimension(300, d.height + 4); // FIXME: d.height+4 ???
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
			Alesia.getMainFrame().removeWindowListener(adapter);
		}
		if (e.getSource() == background) {
			webPopup.hidePopup();
		}
	}

	private void  showPopUp() {
		// FIXME: why i need to do this ???????? im using the right component????
		Rectangle recAle = Alesia.getMainFrame().getBounds();
		Rectangle recPop = new Rectangle(webPopup.getPreferredSize());
		int x = (int) (recAle.getCenterX() - recPop.getCenterX());
		int y = (int) (recAle.getCenterY() - recPop.getHeight() * 2);
		webPopup.showPopup(Alesia.getMainFrame(), x, y);
	}
	@Override
	protected void block() {
		oldGlassPanel = Alesia.getMainFrame().getGlassPane();
		Alesia.getMainFrame().setGlassPane(busyPanel);
		busyPanel.setVisible(true);
		showPopUp();
	}

	@Override
	protected void unblock() {
		webPopup.hidePopup();
		busyPanel.setVisible(false);
		Alesia.getMainFrame().removeWindowListener(adapter);
		Alesia.getMainFrame().setGlassPane(oldGlassPanel);
	}

	/**
	 * The TaskMonitor (constructor arg) tracks a "foreground" task; this method is called each time a foreground task
	 * property changes.
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

	/*
	 * This component is intended to be used as a GlassPane only to consumes mouse and keyboard input plus perform a
	 * blurr operation
	 */
	private static class BusyPanel extends WebPanel {
		private BufferedImage mOffscreenImage;
		private BufferedImageOp mOperation;

		BusyPanel() {
			setOpaque(false);
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

			float ninth = 1.0f / 9.0f;
			float[] blurKernel = {ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth};
			mOperation = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, null);
		}

		@Override
		public void paint(Graphics g) {
			int w = getWidth();
			int h = getHeight();

			if (w == 0 || h == 0) {
				return;
			}

			// Only create the offscreen image if the one we have
			// is the wrong size.
			if (mOffscreenImage == null || mOffscreenImage.getWidth() != w || mOffscreenImage.getHeight() != h) {
				mOffscreenImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
			}

			Graphics2D ig2 = mOffscreenImage.createGraphics();
			ig2.setClip(g.getClip());
			Alesia.getMainFrame().getContentPane().paint(ig2);
			ig2.dispose();
			Graphics2D g2 = (Graphics2D) g;
			g2.drawImage(mOffscreenImage, mOperation, 0, 0);
		}
	}
}
