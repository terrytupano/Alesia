package gui.wlaf;

import java.awt.*;

import javax.swing.*;

import com.alee.painter.decoration.*;
import com.alee.painter.decoration.background.*;
import com.thoughtworks.xstream.annotations.*;

import core.*;

@XStreamAlias("GalaxyBackground")
public class GalaxyBackground<E extends JComponent, D extends IDecoration<E, D>, I extends AbstractBackground<E, D, I>>
		extends
			AbstractBackground<E, D, I> {

	public static final ImageIcon bg = 	TResources.getIcon("galaxy2");

	@Override
	public void paint(Graphics2D g2d, Rectangle bounds, E c, D d, Shape shape) {
		// Fill-in the shape, color doesn't matter
		g2d.fill(shape);

		// Drawing background image with SRC_IN composite
		final Composite oc = g2d.getComposite();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
		g2d.drawImage(bg.getImage(), 0, 0, c);
		g2d.setComposite(oc);
	}
}