package gui.list;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

import javax.imageio.*;
import javax.swing.*;

class IconListItem implements Serializable {
	  private static final long serialVersionUID = 1L;
	  public final ImageIcon icon;
	  public final ImageIcon selectedIcon;
	  public final String title;

	  protected IconListItem(String title, String path) {
	    this.title = title;
	    Image image = makeImage(path);
	    this.icon = new ImageIcon(image);
	    ImageProducer ip = new FilteredImageSource(image.getSource(), new SelectedImageFilter());
	    this.selectedIcon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(ip));
	  }

	  public static Image makeImage(String path) {
	    ClassLoader cl = Thread.currentThread().getContextClassLoader();
	    return Optional.ofNullable(cl.getResource(path)).map(url -> {
	      try (InputStream s = url.openStream()) {
	        return ImageIO.read(s);
	      } catch (IOException ex) {
	        return makeMissingImage();
	      }
	    }).orElseGet(IconListItem::makeMissingImage);
	  }

	  private static Image makeMissingImage() {
	    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
	    int iw = missingIcon.getIconWidth();
	    int ih = missingIcon.getIconHeight();
	    BufferedImage bi = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = bi.createGraphics();
	    missingIcon.paintIcon(null, g2, (32 - iw) / 2, (32 - ih) / 2);
	    g2.dispose();
	    return bi;
	  }
	}

