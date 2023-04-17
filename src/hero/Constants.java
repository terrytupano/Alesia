package hero;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import core.*;

public class Constants {

	/** table Related images. */
	public static final String UOA_ICON_CARS = "/cards";
	public static final String PLAY_CARDS = "/playCards";
	public static final Icon BUTTON_PRESENT_ICON = TResources.getIcon("/dealer_button");
	public static final Icon BUTTON_ABSENT_ICON = TResources.getIcon("/dealer_placeholder");
	public static final Icon CARD_PLACEHOLDER_ICON = TResources.getIcon("/playCards/placeholder");
	public static final Icon CARD_BACK_ICON = TResources.getIcon("/playCards/cardback");
	public static final Map<String, ImageIcon> CARDS_BUFFER = new HashMap<String, ImageIcon>();
	public static final int CARD_WIDTH = CARD_BACK_ICON.getIconWidth();
	public static final int CARD_HEIGHT = CARD_BACK_ICON.getIconHeight();
	public static final Dimension CARD_DIMENSION = new Dimension(CARD_WIDTH, CARD_HEIGHT);

}
