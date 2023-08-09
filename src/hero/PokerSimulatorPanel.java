package hero;

import java.awt.*;
import java.beans.*;
import java.util.*;
import java.util.List;

import com.alee.laf.panel.*;

import core.*;
import gui.*;

public class PokerSimulatorPanel extends WebPanel implements PropertyChangeListener {

	public static String sensorArray = "sensorArray.";
	public static String simulator = "simulator.";
	public static String trooper = "trooper.";
	public static String aa = "aa.";
	public static List<String> groupHeader = Arrays.asList(sensorArray, simulator, trooper, aa);

	private TConsoleTextArea consoleTextArea;
	private ActionsBarChart actionsBarChart;

	public PokerSimulatorPanel() {
		this(null);
	}

	public PokerSimulatorPanel(Trooper trooper) {
		super(new BorderLayout());
		// the order in this list is important to order the result
		Collections.sort(groupHeader);
		this.consoleTextArea = new TConsoleTextArea("%-22s %-50s");
		consoleTextArea.setHeader(groupHeader);
		this.actionsBarChart = new ActionsBarChart();		
		add(consoleTextArea, BorderLayout.CENTER);
		add(actionsBarChart.getChartPanel(), BorderLayout.SOUTH);
		setTrooper(trooper);
	}

	public void setTrooper(Trooper trooper) {
		if (trooper != null)
			trooper.addPropertyChangeListener(this);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void propertyChange(PropertyChangeEvent evt) {
		if (Trooper.STEP.equals(evt.getPropertyName())) {
			Map<String, Object> map = (Map<String, Object>) evt.getNewValue();

			// from the orderer map, remove the group heather. just for visual purpose
//			Map<String, Object> map3 = new HashMap<>(map);			
//			for (String header : groupHeader) {
//				map3.forEach((k, v) -> {
//					String newKey = k.startsWith(header) ? k.replace(header, "") : k;
//					map3.put(newKey, v);
//					map3.remove(k);
//				});
//			}
			consoleTextArea.print(map);
			// setActionsData(map);
		}
	}

	/**
	 * set the action related information.
	 * 
	 * @param aperformed - the action performed by the {@link Trooper}
	 * @param actions    list of {@link TEntry} where each key is an instance of
	 *                   {@link TrooperAction} and the value is the probability for
	 *                   this action to be selected
	 */
	@SuppressWarnings("unchecked")
	public void setActionsData(Map<String, Object> map) {
//		public void setActionsData(TrooperAction aperformed, Vector<TEntry<TrooperAction, Double>> actions) {
		TrooperAction actionPerformed = (TrooperAction) map.get(Trooper.ACTION_PERFORMED);
		actionsBarChart.setCategoryMarker(actionPerformed);

		Vector<TEntry<TrooperAction, Double>> actions = (Vector<TEntry<TrooperAction, Double>>) map
				.get(Trooper.ACTIONS);
		actionsBarChart.setDataSet(actions);
	}
}
