package hero;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import com.alee.laf.button.*;
import com.alee.laf.combobox.*;
import com.alee.laf.grouping.*;
import com.alee.laf.panel.*;
import com.alee.laf.scroll.*;
import com.alee.laf.toolbar.*;
import com.alee.managers.settings.*;
import com.alee.utils.*;

import core.*;

public class SensorArrayPanel extends WebPanel {

	private WebPanel sensorsPanel;
	private WebComboBox sensorTypeComboBox;
	private WebComboBox imageTypeComboBox;
	private WebComboBox screensComboBox;
	private SensorsArray sensorsArray;

	public SensorArrayPanel() {
		super(new BorderLayout());
		List<TSEntry> list = new ArrayList<>();

		// list of options to filter sensors
		list.add(new TSEntry("*", "All"));
		list.add(new TSEntry("villan*", "Only villans"));
		// temporal: just 8 villains
		for (int i = 1; i <= 8; i++) {
			list.add(new TSEntry("villan" + i + "*", "only villan" + i));
		}
		list.add(new TSEntry("type: textareas", "Only OCR text areas"));
		list.add(new TSEntry("type: numareas", "Only OCR numeric areas"));
		list.add(new TSEntry("type: cardareas", "Only cards areas"));
		list.add(new TSEntry("type: actions", "Only Actions areas"));
		sensorTypeComboBox = new WebComboBox(list);
		sensorTypeComboBox.addActionListener(evt -> filterSensors());

		// options to show captured or prepared images
		List<TSEntry> list2 = new ArrayList<>();
		list2.add(new TSEntry(ScreenSensor.CAPTURED, "show captured images"));
		list2.add(new TSEntry(ScreenSensor.PREPARED, "show prepared images"));
		list2.add(new TSEntry(ScreenSensor.COLORED, "show colored images"));
		this.imageTypeComboBox = new WebComboBox(list2);
		imageTypeComboBox.addActionListener(evt -> filterSensors());

		// screen shots images
		List<File> files = FileUtils.findFilesRecursively(Constants.SCREEN_SHOTS_FOLDER,
				f -> f.getName().endsWith(".png"));
		List<TEntry<File, String>> names = new ArrayList<>();
		files.forEach(f -> names.add(new TEntry<>(f, f.getName().substring(0, f.getName().length() - 4))));
		this.screensComboBox = new WebComboBox(names);
		screensComboBox.addActionListener(evt -> testSccreenShot());

		imageTypeComboBox.registerSettings(new Configuration<ComboBoxState>("SensorPanel.imageType"));
		sensorTypeComboBox.registerSettings(new Configuration<ComboBoxState>("SensorPanel.filter"));

		WebButton testPpt = TUIUtils.getButtonForToolBar(TActionsFactory.getAction("testAreasPpt"));
		WebButton testScreeen = TUIUtils.getButtonForToolBar(TActionsFactory.getAction("testAreasScreen"));

		GroupPane groupPane = TUIUtils.getGroupPane(screensComboBox, testScreeen);
		GroupPane groupPane2 = new GroupPane(sensorTypeComboBox, imageTypeComboBox);
		WebToolBar toolBar = TUIUtils.getUndecoradetToolBar(testPpt, groupPane2, groupPane);

		this.sensorsPanel = new WebPanel(new GridLayout(0, 2));
		WebScrollPane scrollPane = TUIUtils.getWebScrollPane(sensorsPanel);

		add(toolBar, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
	}

	private void testSccreenShot() {
		@SuppressWarnings("unchecked")
		TEntry<File, String> selF = (TEntry<File, String>) screensComboBox.getSelectedItem();
		sensorsArray.setReadSourceFile(selF.getKey());
	}

	/**
	 * update the visual components in this panel to the (possible) new instance of
	 * {@link Trooper} passed as argument
	 * 
	 * @param trooper - the trooper
	 */
	public void setTrooper(Trooper trooper) {
		this.sensorsArray = trooper.getSensorsArray();
		sensorsPanel.removeAll();
		List<ScreenSensor> ssl = sensorsArray.getSensors(null);
		for (ScreenSensor ss : ssl) {
			sensorsPanel.add(ss);
		}
		filterSensors();
		sensorsPanel.repaint();
	}

	private void filterSensors() {
		// on the component registerSettings(), the sensor array is null
		if (sensorsArray == null)
			return;
		sensorsPanel.setVisible(false);
		String filter = ((TSEntry) sensorTypeComboBox.getSelectedItem()).getKey();
		String sCapture = ((TSEntry) imageTypeComboBox.getSelectedItem()).getKey();

		sensorsPanel.removeAll();
		List<ScreenSensor> ssl = sensorsArray.getSensors(null);
		for (ScreenSensor ss : ssl) {
			ss.showImage(sCapture);
			// Special name or wild card string (the structure type: xxx has noting in
			// Special, just a name)
			if (filter.startsWith("type:")) {
				if (filter.equals("type: textareas") && ss.isTextArea())
					sensorsPanel.add(ss);
				if (filter.equals("type: numareas") && ss.isNumericArea())
					sensorsPanel.add(ss);
				if (filter.equals("type: cardareas") && ss.isCardArea())
					sensorsPanel.add(ss);
				if (filter.equals("type: actions") && ss.isActionArea())
					sensorsPanel.add(ss);
			} else {
				if (TStringUtils.wildCardMacher(ss.getName(), filter))
					sensorsPanel.add(ss);
			}
		}
		sensorsPanel.setVisible(true);
	}
}
