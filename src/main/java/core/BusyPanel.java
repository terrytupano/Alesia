package core;

import java.awt.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.event.*;

import com.alee.laf.panel.*;

/**
 * This component is intended to be used as a GlassPane only to consumes mouse
 * and keyboard input plus perform a blur operation
 */
public class BusyPanel extends WebPanel {
	private BufferedImage mOffscreenImage;
	private BufferedImageOp mOperation;

	public BusyPanel() {
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
		float[] blurKernel = { ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth };
		mOperation = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, null);
	}

	@Override
	public void paint(Graphics g) {
		int w = getWidth();
		int h = getHeight();

		if (w == 0 || h == 0) {
			return;
		}

		// Only create the off screen image if the one we have
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
