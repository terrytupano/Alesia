package core;
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


import java.applet.*;
import java.awt.Dialog.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.Action;

import org.apache.commons.logging.impl.*;
import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.alee.extended.image.*;
import com.alee.extended.layout.*;
import com.alee.extended.panel.*;
import com.alee.extended.window.*;
import com.alee.laf.*;
import com.alee.laf.button.*;
import com.alee.laf.label.*;
import com.alee.managers.notification.*;
import com.alee.managers.settings.*;
import com.alee.managers.style.*;
import com.alee.utils.*;

import flicka.*;
import gui.*;
import gui.wlaf.*;
import hero.*;

/**
 * Entrada aplicacion
 * 
 * @author Terry
 */
public class Alesia extends Application {

	public TWebFrame mainFrame;
	public static Logger logger;

	public ArrayList<Skin> skins;
	public TTaskManager taskManager;

	private AudioClip newMsg, errMsg;
	private TDockingContainer mainPanel;
	private DB alesiaDB;

	public static synchronized Alesia getInstance() {
		Application a = Application.getInstance();
		return (Alesia) a;
	}

	public TDockingContainer getMainPanel() {
		return mainPanel;
	}

	public ResourceMap getResourceMap() {
		return getInstance().getContext().getResourceMap();
	}

	public TWebFrame getMainFrame() {
		return mainFrame;
	}

	/**
	 * inicio de aplicacion
	 * 
	 * @param arg - argumentos de entrada
	 */
	public static void main(String[] args) {
//		Locale.setDefault(Locale.US);
		Application.launch(Alesia.class, args);
	}

	/**
	 * Open and return an instacen of {@link DB} for the given prefix. The connection parameters must be in the
	 * database.properties file or similar.
	 * 
	 * @param name - prefix name of the conneciton parameters
	 * @return instance of {@link DB}
	 * @see #getDBProperties()
	 */
	public void openDB(String name) {
		// if the database is allready open, do nothig
		List<String> conNames = DB.getCurrrentConnectionNames();
		if (conNames.contains(name))
			return;

		Properties orgPrp = getDBProperties();

		// remove all properties except those who star whit "name"
		Set<Object> keys = orgPrp.keySet();
		keys.removeIf(k -> !k.toString().startsWith(name));
		Properties properties = new Properties();

		// build a new propert list whiout the prefix. keeping only the property and
		// value spected by the jdbc lib
		keys.forEach(k -> properties.put(k.toString().substring(name.length() + 1), orgPrp.get(k)));

		// mandatory parameters
		String drv = properties.getProperty("driver");
		properties.remove("driver");
		String url = properties.getProperty("url");
		properties.remove("url");

		@SuppressWarnings("resource")
		DB db = new DB(name);
		db.open(drv, url, properties);
	}

	public static Map<String, Object> showDialog(TUIFormPanel content, double withFactor, double heightFactor) {

		// standar behavior: if the title of the tuipanel is visible, this method remove
		// the string and put in as this
		// dialog title
		String popOvertext = " ";
		if (content.isTitleVisible()) {
			popOvertext = content.getTitleText();
			content.setTitleVisible(false);
		}
		final WebPopOver popOver = new WebPopOver(Alesia.getInstance().getMainFrame());
		popOver.setModalityType(ModalityType.TOOLKIT_MODAL);
		popOver.setMovable(false);
		popOver.setLayout(new VerticalFlowLayout());
		final WebImage icon = new WebImage(TUIUtils.getSmallFontIcon('\uf00d'));
		final WebLabel titleLabel = new WebLabel(popOvertext, WebLabel.CENTER);
		final WebButton closeButton = new WebButton(TUIUtils.getSmallFontIcon('\uf00d'), new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popOver.dispose();
			}
		});
		popOver.setUndecorated(true);
		GroupPanel tit = new GroupPanel(GroupingType.fillMiddle, 4, icon, titleLabel, closeButton);
		tit.setMargin(0, 0, 10, 0);
		popOver.add(tit);
		popOver.add(content);

		// popOver.setLocationRelativeTo(Alesia.getInstance().getMainFrame());
		popOver.pack();
		popOver.setLocationRelativeTo(Alesia.getInstance().getMainFrame());
		popOver.setVisible(true);
		// popOver.show(Alesia.getInstance().getMainFrame());

		return content.getValues();
	}

	public static void showNotification(String messageId, Object... arguments) {
		TValidationMessage tm = new TValidationMessage(messageId, arguments);
		WebInnerNotification npop = NotificationManager.showInnerNotification(Alesia.getInstance().getMainFrame(),
				tm.formattedText(), tm.getIcon());
		// ae.getExceptionIcon(), NotificationOption.accept);
		npop.setDisplayTime(tm.getMiliSeconds());
		if (messageId.equals("notification.msg00")) {
			Alesia.getInstance().errMsg.play();
		} else {
			Alesia.getInstance().newMsg.play();
		}
	}

	/**
	 * Look in the Alesia.properties file, look for the property "Alesia.database.file.name" and load an return the list
	 * of all prperties found in that file. This file contain all data base connection information.
	 * 
	 * @return all properties found in the database properties files
	 */
	private Properties getDBProperties() {
		try {
			// active jdbc propertie files pointed form main alesia property file
			Properties activeprp = new Properties();
			// TODO: convert to urls to allow more access support ???
			File fp = new File(System.getProperty("Alesia.database.file.name"));
			activeprp.load(new FileInputStream(fp));
			return activeprp;
		} catch (Exception e) {
			ExceptionDialog.showDialog(e);
		}
		return null;
	}

	/**
	 * retrive global identificator from <code>wmic</code>
	 * 
	 * @param gid - gobal id
	 * @param vn - variable name
	 * 
	 * @return variable value
	 */
	private String getWmicValue(String gid, String vn) {
		String rval = null;
		try {
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(new String[]{"wmic", gid, "get", vn});
			InputStream is = process.getInputStream();
			Scanner sc = new Scanner(is);
			while (sc.hasNext()) {
				String next = sc.next();
				if (vn.equals(next)) {
					rval = sc.next().trim();
					break;
				}
			}
			is.close();
			sc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return rval;
	}


	public void restarApplication() {
		try {
			shutdown();
			// espera para hasta q finalizen todos los subsys
			Thread.sleep(2000);
			Runtime.getRuntime().exec("cmd /c start /MIN restart.bat");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * check for another active instance of Alesia looking the current active windows. if another instance is found,
	 * this mehtod send cmd commands and end this currentexecution.
	 * 
	 * @see TResources#getActiveWindows(String)
	 * @see TResources#performCMDOWCommand(String, String)
	 */
	private void checkActiveInstance() {
		// active window method spect wildcard
		String tit = getResourceMap().getString("title");
		ArrayList<TSEntry> winds = TResources.getActiveWindows(tit);
		if (!winds.isEmpty()) {
			Alesia.logger.warning("Another active instance found. /act /res commands.");
			TResources.performCMDOWCommand(winds.get(0).getKey(), "/ACT /RES");
			System.exit(0);
		}
	}

	@Override
	protected void initialize(String[] args) {

		// Loggin configuration. this step is performed here for conbenence
		// TODO: For slf4j: the bridge betwen slf4j is set using the slf4j-jdk14-1.7.25
		// jar lib
		// for Apache: Set the system property
		// For Alesia. look on the configuration file. if the file exist, i use it

		System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
		System.setProperty("org.apache.commons.logging.Log", Jdk14Logger.class.getName());

		File logc = new File("logging.properties");
		if (logc.exists()) {
			System.setProperty("java.util.logging.config.file", logc.getName());
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (Exception e) {
				ExceptionDialog.showDialog(e);
			}
		}
		logger = Logger.getLogger("Alesia");
		logger.info("Wellcome to Alesia.");

		// update alesia.propertyes to eviorement variables. this step is performed here
		// for convenience
		try {
			Properties prp = new Properties();
			prp.load(new FileInputStream(new File("Alesia.properties")));
			Properties sysprp = System.getProperties();
			prp.keySet().forEach(key -> sysprp.put(key, prp.get(key)));
		} catch (Exception e) {
			Alesia.logger.log(Level.SEVERE, "Exception found loading propertys from file system.", e);
			// ExceptionDialog.showDialog(e);
			System.exit(-1);
		}

		System.out.println(getWmicValue("bios", "SerialNumber"));
		System.out.println(getWmicValue("cpu", "SystemName"));
		checkActiveInstance();
		TResources.init();
		TUIUtils.init();
		TStringUtils.init();

		// load all actions into the application
		new TActionsFactory();

		newMsg = Applet.newAudioClip(getClass().getResource("/core/resources/newMsg.wav"));
		errMsg = Applet.newAudioClip(getClass().getResource("/core/resources/errMsg.wav"));

		// System.setProperty("org.apache.commons.logging.Log",
		// Jdk14Logger.class.getName());

		// parse app argument parameters and append to tpreferences to futher uses
		// TODO: do something
		// for (String arg : args) {
		// String[] kv = arg.split("=");
		// }
	}

	@Override
	protected void ready() {
		mainFrame.setSplashIncrementText("Starting task manager ...");
		this.taskManager = new TTaskManager();
		// requestAutentication();

		mainPanel = new TDockingContainer();
		HomePanel homePanel = mainPanel.getHomePanel();

		// load left panel actions
		mainFrame.setSplashIncrementText("Loading plugins ...");
		ArrayList<Action> alist = new ArrayList<>();		
		Hero hero = new Hero();
		alist.addAll(hero.getUI());
		Flicka flicka = new Flicka();
		alist.addAll(flicka.getUI());
		
		homePanel.setActions(alist);
		Alesia.getInstance().getMainFrame().setContentPane(mainPanel);
	}

	@Override
	protected void shutdown() {
		Alesia.logger.info("Preapering to leave the application ...");
		if (alesiaDB != null)
			alesiaDB.close();
		Alesia.logger.info("Bye !!!");
	}

	@Override
	protected void startup() {
		Alesia.logger.info("Starting Up ...");

		SettingsManager.setDefaultSettingsDir(FileUtils.getWorkingDirectoryPath());
		SettingsManager.setDefaultSettingsGroup("Alesia");
		SettingsManager.setSaveOnChange(true);

		// Process our custom alias first
		XmlUtils.processAnnotations(GalaxyBackground.class);

		// Initializing L&F
		WebLookAndFeel.install();
		// WebLookAndFeel.install(WebLightSkin.class);
		// WebLookAndFeel.install(FlatSkin.class);
		// WebLookAndFeel.install(MaterialSkin.class);
		// WebLookAndFeel.install(WebDarkSkin.class);
		// StyleManager.addExtensions(new XmlSkinExtension(new ClassResource(Alesia.class,
		// "resources/SimpleExtension.xml")));
		// ProprietaryUtils.setupAATextInfo(UIManager.getDefaults());

		// TODO: no languaje manajer for now. still using old school i18n
		// Configurting languages
		// LanguageManager.addDictionary ( new Dictionary ( Alesia.class,
		// "language/demo-language.xml" ) );
		// LanguageManager.addLanguageListener ( new LanguageLocaleUpdater () );

		// TODO: remove the antialaisin shint form the lookandfell
		// UIDefaults defaults = UIManager.getDefaults();

		// Turn off the anti-aliasing function of the text
		// defaultTextRenderingHints only works on the title bar of the form
		// textRenderingHints works on the controls in the form
		// StyleConstants.defaultTextRenderingHints = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
		// RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		// StyleConstants.textRenderingHints = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
		// RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		mainFrame = new TWebFrame();
		mainFrame.setVisible(true);
	}
}
