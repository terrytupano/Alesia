package hero;

import java.awt.*;

import javax.swing.*;

import core.*;

public class Constants {

	/** table Related images. */
	public static final String HERO_RESOURCES = TResources.USER_DIR + "/target/classes/hero/resources";
	public static final String SCREEN_SHOTS_FOLDER = TResources.USER_DIR + "/screenShots/";
	public static final String UOA_ICON_CARS = "/cards/";
	public static final String PLAY_CARDS = "/playCards/";
	public static final String TESSDATA = Constants.HERO_RESOURCES + "/tessdata/";
	public static final String PPT_FILE = Constants.HERO_RESOURCES + "/ps-10-win11.ppt";
	public static final Icon BUTTON_PRESENT_ICON = TResources.getIcon("/dealer_button.png");
	public static final Icon BUTTON_ABSENT_ICON = TResources.getIcon("/dealer_placeholder.png");
	public static final Icon CARD_PLACEHOLDER_ICON = TResources.getIcon("/playCards/placeholder.png");
	public static final Icon CARD_BACK_ICON = TResources.getIcon("/playCards/cardback.png");
	public static final int CARD_WIDTH = CARD_BACK_ICON.getIconWidth();
	public static final int CARD_HEIGHT = CARD_BACK_ICON.getIconHeight();
	public static final Dimension CARD_DIMENSION = new Dimension(CARD_WIDTH, CARD_HEIGHT);

	// the factor to compute buyIn in from information readed from window title (poker star table)
	public static final int BUYIN_FACTOR = 100;
	public static final int TOURNAMENT_BUYIN = 10;
}
