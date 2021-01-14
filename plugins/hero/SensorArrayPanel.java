package plugins.hero;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import com.alee.laf.combobox.*;
import com.alee.laf.panel.*;
import com.alee.managers.settings.*;

import core.*;
import gui.*;

public class SensorArrayPanel extends TUIPanel {

	private WebPanel sensorsPanel;
	private WebComboBox sensorTypeComboBox;
	private WebComboBox imageTypeComboBox;
	private SensorsArray sensorsArray;

	public SensorArrayPanel() {

		// list of options to filter sensors
		sensorTypeComboBox = new WebComboBox();
		sensorTypeComboBox.addItem(new TEntry("*", "All"));
		sensorTypeComboBox.addItem(new TEntry("villan*", "Only villans"));
		// temporal: just 5 villans
		for (int i = 1; i <= 8; i++) {
			sensorTypeComboBox.addItem(new TEntry("villan" + i + "*", "only villan" + i));
		}
		sensorTypeComboBox.addItem(new TEntry("type: textareas", "Only OCR text areas"));
		sensorTypeComboBox.addItem(new TEntry("type: numareas", "Only OCR numeric areas"));
		sensorTypeComboBox.addItem(new TEntry("type: cardareas", "Only cards areas"));
		sensorTypeComboBox.addItem(new TEntry("type: actions", "Only Actions areas"));
		sensorTypeComboBox.addActionListener(evt -> filterSensors());

		// options to show captured or prepared images
		this.imageTypeComboBox = new WebComboBox();
		imageTypeComboBox.addItem(new TEntry(ScreenSensor.CAPTURED, "show captured images"));
		imageTypeComboBox.addItem(new TEntry(ScreenSensor.PREPARED, "show prepared images"));
		imageTypeComboBox.addItem(new TEntry(ScreenSensor.COLORED, "show colored images"));
		imageTypeComboBox.addActionListener(evt -> filterSensors());

		imageTypeComboBox.registerSettings(new Configuration<ComboBoxState>("SensorPanel.imageType"));
		sensorTypeComboBox.registerSettings(new Configuration<ComboBoxState>("SensorPanel.filter"));

		setToolBar("testAreas");
		getToolBarPanel().add(sensorTypeComboBox, imageTypeComboBox);

		this.sensorsPanel = new WebPanel(new GridLayout(0, 2));
		JScrollPane ajsp = new JScrollPane(sensorsPanel);

		setBodyComponent(ajsp);
	}

	public void updateArray() {
		this.sensorsArray = Hero.sensorsArray;
		sensorsPanel.removeAll();
		List<ScreenSensor> ssl = sensorsArray.getSensors(null);
		for (ScreenSensor ss : ssl) {
			sensorsPanel.add(ss);
		}
		filterSensors();
		sensorsPanel.repaint();
	}

	private void filterSensors() {
		// on the component registerSettings(), the sensorarray is null
		if (sensorsArray == null)
			return;
		sensorsPanel.setVisible(false);
		String filter = ((TEntry) sensorTypeComboBox.getSelectedItem()).getKey().toString();
		String sCapture = ((TEntry) imageTypeComboBox.getSelectedItem()).getKey().toString();

		sensorsPanel.removeAll();
		List<ScreenSensor> ssl = sensorsArray.getSensors(null);
		for (ScreenSensor ss : ssl) {
			ss.showImage(sCapture);
			// spetial name or wildcard string (the structure type: xxx has noting in spetial, just a name)
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
