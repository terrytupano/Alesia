package plugins.hero.pokerEnlighter;

import javax.swing.*;
import javax.swing.plaf.nimbus.*;

import plugins.hero.pokerEnlighter.gui.*;

/**
 * Main entry point of Poker Enlighter. Contains all relevant information about the program.
 * 
 * @author Radu Murzea
 * 
 * @version 1.0
 */
public final class PokerEnlighter implements Runnable {
	/**
	 * Specifies the minimum major version needed to run this program.
	 */
	public static final int MAJOR_VERSION = 7;

	/**
	 * Specifies the minimum minor version needed to run this program.
	 */
	public static final int MINOR_VERSION = 4;

	/**
	 * Represents the full version string of this current release.
	 */
	public static final String FULL_VERSION = "2.4 build 558";

	/**
	 * The build number of this release.
	 */
	public static final int BUILD_NUMBER = 558;

	/**
	 * Version string of the JDK version used to compile this release.
	 */
	public static final String COMPILED_WITH = "Java 7 Update 51";

	/**
	 * String containing the compile date of this release.
	 */
	public static final String BUILD_DATE = "28 Sep 2014 02:28 PM UTC";

	// if an error occurs in this class, these Strings will be used in the error dialog that appears
	private static final String errorTitle = "Program error";
	private static final String errorContent = "The program encountered an error at startup: ";

	// references the options and the dictionary
	private static OptionsContainer options;
	private static PEDictionary dictionary;

	/**
	 * The main method. It will check the minimum version required to run this release, it will retrieve all options, it
	 * will set the chosen Look & Feel and will display the GUI.
	 * 
	 * @param args command-line arguments. Any values provided here will be ignored.
	 */
	public static void main(String[] args) {
		// check if the minimum version is ok
		if (!SystemUtils.checkVersion(MAJOR_VERSION, MINOR_VERSION)) {
			final String title = "Poker Enlighter Minimum Version Error";

			final StringBuilder message = new StringBuilder();
			message.append("JVM version detected: ");
			message.append(System.getProperty("java.version"));
			message.append(". Minimum version required: ");
			message.append(MAJOR_VERSION);
			message.append(" Update ");
			message.append(MINOR_VERSION);
			message.append(".");

			GUIUtilities.showErrorDialog(null, message.toString(), title);

			return;
		}

		options = OptionsContainer.getOptionsContainer();

		// get the language
		try {
			Language language = null;
			switch (options.getLanguage()) {
				case "English" :
					language = Language.ENGLISH;
					break;
				case "Romanian" :
					language = Language.ROMANIAN;
					break;
				default :
					throw new RuntimeException("invalid language");
			}

			dictionary = PEDictionary.forLanguage(language);
		} catch (Exception ex) {
			GUIUtilities.showErrorDialog(null, errorContent + ex.getMessage(), errorTitle);

			return;
		}

		// show the GUI on the EDT
		SwingUtilities.invokeLater(new PokerEnlighter());
	}

	/**
	 * Will be called by the main method.
	 * 
	 * @since 1.0
	 */
	@Override
	public void run() {
		// set the look & feel
		try {
			setLNF(options.getLookAndFeel());
		} catch (Exception ex) {
			GUIUtilities.showErrorDialog(null, errorContent + ex.getMessage(), errorTitle);
		}

		// create and show the GUI
		GUI g = GUI.getGUI(dictionary);
		g.setLocationToCenterOfScreen();
		g.setVisible(true);
	}

	/**
	 * Sets the Look & Feel specified as parameter.
	 * 
	 * @param lnf the Look & Feel
	 * 
	 * @throws Exception if the Look & Feel is not among the accepted values.
	 * 
	 * @since 1.1
	 */
	public void setLNF(String lnf) throws Exception {
		LookAndFeel lnfObject = null;
		lnfObject = new NimbusLookAndFeel();
		UIManager.setLookAndFeel(lnfObject);
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
	}
}