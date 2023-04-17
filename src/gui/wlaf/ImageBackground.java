package gui.wlaf;

import java.awt.*;

import javax.swing.*;

import com.alee.laf.panel.*;

import core.*;



public class ImageBackground extends WebPanel {

	public static final ImageIcon bg = TResources.getIcon("imageBackground.png");

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		Image image = bg.getImage();
		int x = (this.getWidth() - image.getWidth(null)) / 2;
		int y = (this.getHeight() - image.getHeight(null)) / 2;
		g2d.drawImage(image, x, y, null);
	}
}