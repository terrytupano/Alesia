package gui.wlaf;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import com.alee.extended.behavior.*;
import com.alee.extended.layout.*;
import com.alee.laf.label.*;
import com.alee.laf.panel.*;
import com.alee.laf.rootpane.*;
import com.alee.laf.window.*;
import com.alee.managers.settings.*;
import com.alee.managers.style.*;

import core.*;

public class TWebFrame extends WebFrame {

	private JComponent splashPanel;
	private WebLabel splashIncrementLabel;

	public TWebFrame() {
		super();
//		super(StyleId.of("galaxy"));

		// StyleId.panelTransparent.set((JComponent) getContentPane());
		// final WebPanel container = new WebPanel(StyleId.panelTransparent, new AlignLayout());
		// add(container);
		WebPanel container = new ImageBackground();
		container.setLayout(new AlignLayout());
		setContentPane(container);
		
		final ComponentMoveBehavior moveBehavior = new ComponentMoveBehavior(container);
		moveBehavior.install();

		splashPanel = buildSplash();
		container.add(splashPanel, SwingConstants.CENTER + "," + SwingConstants.CENTER);

		setTitle(TStringUtils.getString("title"));
		Vector v = new Vector();

		v.add(TResources.getIcon("target-icon", 16).getImage());
		v.add(TResources.getIcon("target-icon", 32).getImage());
		setIconImages(v);
		WindowAdapter ad = new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				Alesia.getInstance().exit();
			}
		};
		addWindowListener(ad);
		registerSettings(new Configuration<WindowState>("TWebFrame"));
	}

	/**
	 * center the r2 rectangle based on r1 coordenates
	 * 
	 * @param r1 - base rectangle
	 * @param r2 - rectangle to center
	 */
	public static void center(Rectangle r1, Rectangle r2) {
		r2.x = (r1.width - r2.width) / 2;
		r2.y = (r1.height - r2.height) / 2;
	}

	public static Point getCenter(Component c1, Component c2) {
		Rectangle r1 = c1.getBounds();
		Rectangle r2 = new Rectangle(c2.getBounds());
		TWebFrame.center(r1, r2);
		return new Point(r2.x - (r2.width), r2.y - (r2.height / 2));
	}
	/**
	 * Calc new rectangle resulting that is centered based on src rectangle
	 * 
	 * @param src - orginial rectangle
	 * @param dim - new size of orginal rectangle
	 * 
	 * @return center rectangle
	 */
	public static Rectangle zoomIn(Rectangle src, Dimension dim) {
		Rectangle dest = new Rectangle(src);
		dest.setSize(dim);
		int hx = (dim.width - src.width) / 2;
		int hy = (dim.height - src.height) / 2;
		dest.x -= hx;
		dest.y -= hy;
		return dest;
	}
	private static Font getFont(final int style, final float size) {
		return (new JLabel()).getFont().deriveFont(style, size);
	}
	/**
	 * Return a {@link Rectangle} center on this frame {@link GraphicsConfiguration} and size based on factor f
	 * 
	 * @param factor - size factor
	 * @return bounds
	 */
	public Rectangle getBoundByFactor(double factor) {
		return getBoundByFactor(factor, factor);
	}
	/**
	 * Return a {@link Rectangle} center on this frame {@link GraphicsConfiguration} and size based on the sith and
	 * height arguments.
	 * 
	 * @param withFactor - width factor
	 * @param heightFactor - height factor
	 * @return center rectangle
	 */
	public Rectangle getBoundByFactor(double withFactor, double heightFactor) {
		Rectangle gcr = getGraphicsConfiguration().getBounds();
		Rectangle rr = new Rectangle(0, 0, (int) (gcr.width * withFactor), (int) (gcr.height * heightFactor));
		center(gcr, rr);
		return rr;
	}

	public void setSplashIncrementText(String text) {
		splashIncrementLabel.setText(text);
	}

	private JComponent buildSplash() {
		WebLabel splashTitleLabel = new WebLabel(StyleId.labelShadow, TStringUtils.getString("name"), WebLabel.CENTER);
		splashTitleLabel.setFont(new Font("MagistralC", Font.PLAIN, 50));

		WebLabel splashSubtitleLabel = new WebLabel(StyleId.labelShadow, TStringUtils.getString("description"),
				WebLabel.CENTER);
		splashSubtitleLabel.setFont(getFont(Font.PLAIN, 15));
		splashIncrementLabel = new WebLabel("---", WebLabel.CENTER);

		WebPanel form = new WebPanel(StyleId.panelTransparent, new FormLayout(false, false, 10, 10));
		form.add(splashTitleLabel, FormLayout.LINE);
		form.add(splashSubtitleLabel, FormLayout.LINE);
		form.add(splashIncrementLabel, FormLayout.LINE);
		return form;
	}
}
