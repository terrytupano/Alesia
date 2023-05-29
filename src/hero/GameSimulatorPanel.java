
package hero;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;
import org.jdesktop.application.Task.*;

import com.alee.laf.button.*;
import com.alee.laf.grouping.*;
import com.alee.laf.list.*;
import com.alee.laf.panel.*;
import com.alee.laf.text.*;
import com.alee.managers.style.*;

import core.*;
import datasource.*;
import hero.ozsoft.*;
import hero.ozsoft.bots.*;

public class GameSimulatorPanel extends TUIFormPanel implements ListSelectionListener {

	private PokerSimulatorPanel pokerSimulatorPanel;
	private WebList trooperList;
	private TroopersTable troopersTable;
	private WebTextField shuffleTextField;

	public GameSimulatorPanel() {
		this.pokerSimulatorPanel = new PokerSimulatorPanel();
		TActionsFactory.insertActions(this);

		// to today, i need only 1 simulation parameter
		LazyList<SimulationParameters> list = SimulationParameters.findAll();
		SimulationParameters model = list.isEmpty() ? new SimulationParameters() : list.get(0);

		Map<String, ColumnMetadata> columns = SimulationParameters.getMetaModel().getColumnMetadata();
		setModel(model);

		addInputComponent(TUIUtils.getWebTextField("simulationName", model, columns), true, true);
		addInputComponent(TUIUtils.getNumericTextField("simulationsHands", model, columns), true, true);
		addInputComponent(TUIUtils.getSpinner("numOfTasks", model, 1, TTaskManager.CORE_POOL_SIZE));

		shuffleTextField = TUIUtils.getWebTextField("simulationVariable", model, columns);
		WebButton shuffleButton = TUIUtils.getSmallButton("shuffleVariable");
		GroupPane groupPane = new GroupPane(shuffleTextField, shuffleButton);
		groupPane.setName("simulationVariable");

		List<JComponent> components = getInputComponents();
		components.add(groupPane);

		WebPanel panel = TUIUtils.getFormListItems(components);

		addToolBarActions("bankrollHistory", "deleteAndStartSimulation", "startSimulation");

//		ListItem item = new ListItem(TActionsFactory.getAction("showPokerSimulator"));
//		panel.add(item, FormLayout.LINE);

		this.troopersTable = new TroopersTable();

		WebPanel panel2 = new WebPanel(new BorderLayout());
		panel2.add(panel, BorderLayout.NORTH);
		panel2.add(troopersTable, BorderLayout.CENTER);

		setBodyComponent(panel2);
	}

	@org.jdesktop.application.Action
	public void bankrollHistory() {
		LazyList<SimulationParameters> parameters = SimulationParameters.findAll();
		ArrayList<String> names = new ArrayList<>();
		parameters.forEach(sp -> names.add(sp.getString("simulationName")));
		String[] possibleValues = names.toArray(new String[0]);
		Object selectedValue = JOptionPane.showInputDialog(Alesia.getMainFrame(), "Choose one", "Input",
				JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
		if (selectedValue != null) {
			String resultName = selectedValue.toString();
			showBankrollHistoryImpl(resultName);
		}
	}

	public boolean checkVariables() {
		boolean ok = true;
		Map<String, ColumnMetadata> columns = TrooperParameter.getMetaModel().getColumnMetadata();
		String[] variables2 = shuffleTextField.getText().split(",");
		for (String variable : variables2) {
			if (!columns.containsKey(variable)) {
				Alesia.showNotification("simulation.msg01", variable);
				ok = false;
			}
		}
		return ok;
	}

	@org.jdesktop.application.Action
	public Task<Void, Void> deleteAndStartSimulation() {
		// save current changes
		if (!save())
			return null;
		SimulationParameters parameters = (SimulationParameters) getModel();
		parameters.cleanSimulation();
		return startSimulation();
	}

	@Override
	public Model getModel() {
		Model model = super.getModel();
		model.set("simulationVariable", shuffleTextField.getText());
		return model;
	}

	private WebList getTrooperList() {
		TrooperParameter trooperParameters[] = TrooperParameter.findAll().orderBy("chair")
				.toArray(new TrooperParameter[0]);
		WebList list = new WebList(StyleId.listTransparent, trooperParameters);
		list.addListSelectionListener(this);
//		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(4);
		list.addPropertyChangeListener(e -> {
			if ("ancestor".equals(e.getPropertyName())) {
				Dimension dimension = list.getSize();
				list.setFixedCellWidth((int) dimension.getWidth() / 2);
			}
		});
		TrooperCellRenderer cellRenderer = new TrooperCellRenderer();
		ComponentAdapter adapter = new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				computeSize(e);
			}

			@Override
			public void componentShown(ComponentEvent e) {
				computeSize(e);
			}

			private void computeSize(ComponentEvent e) {
				Dimension dimension = e.getComponent().getSize();
				list.setFixedCellWidth((int) dimension.getWidth() / 2);
			}
		};
		list.addComponentListener(adapter);
		list.setCellRenderer(cellRenderer);
		return list;
	}

	public void setTrooper(Trooper trooper) {
		pokerSimulatorPanel.setTrooper(trooper);
	}

	@org.jdesktop.application.Action
	public void showBankrollHistory() {
		showBankrollHistoryImpl(getModel().getString("simulationName"));
	}

	private void showBankrollHistoryImpl(String simulationName) {
		// Retrieve the first element of the statistical series to detect, the type of
		// graph
		Alesia.openDB();
		SimulationParameters parameters = SimulationParameters.findFirst("simulationName = ?", simulationName);
		if (parameters == null) {
			Alesia.showNotification("hero.msg06", simulationName);
			return;
		}

		JDialog chartDialog;

		if (parameters.isSingleVariable()) {
			chartDialog = new SingeVariableSimulationLineChart(simulationName);
		} else {
			chartDialog = new MultiVariableSimulationBarChar(simulationName);
		}
		Rectangle bound = Alesia.getMainFrame().getBoundByFactor(0.7);
		chartDialog.setSize(bound.getSize());
		GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		chartDialog.setLocationRelativeTo(Alesia.getMainFrame());
		chartDialog.setVisible(true);

	}

	@org.jdesktop.application.Action
	public void showPokerSimulator() {
		Alesia.getMainPanel().showPanel(pokerSimulatorPanel);
	}

	@org.jdesktop.application.Action
	public void shuffleVariable() {
		boolean ok = checkVariables();
		if (!ok)
			return;
		LazyList<TrooperParameter> parameters = TrooperParameter.findAll();
		String[] variables = shuffleTextField.getText().split(",");
		for (String variable : variables) {
			List<Integer> integers = Table.getShuffleList();
			for (TrooperParameter trooperParameter : parameters) {
				trooperParameter.set(variable, integers.remove(0));
				trooperParameter.save();
			}
		}
		troopersTable.refresh();
	}

	@org.jdesktop.application.Action(block = BlockingScope.WINDOW)
	public Task<Void, Void> startSimulation() {
		try {
			// save current changes
			if (!save())
				return null;

			// check max task
			if (!Alesia.getTaskManager().suporMoreTask()) {
				Alesia.showNotification("hero.msg03");
				return null;
			}
			// check for hero client
			if (TrooperParameter.find("name = ?", "Hero") == null) {
				Alesia.showNotification("hero.msg01");
				return null;
			}
			// check min num of players
			if (TrooperParameter.count("isActive = ?", true) < 2) {
				Alesia.showNotification("hero.msg02");
				return null;
			}

			// WARNING: order by chair is important. this is take into account in simulation
			// & in tableDialog players place
			LazyList<TrooperParameter> tparms = TrooperParameter.findAll().orderBy("chair");
			SimulationParameters parameters = (SimulationParameters) getModel();
			TaskGroup taskGroup = new TaskGroup();
			Table oneTable = null;
			int numOfTask = parameters.getInteger("numOfTasks");

			for (int i = 0; i < numOfTask; i++) {
				Table table = new Table(parameters);
				for (TrooperParameter trooperParm : tparms) {
					if (trooperParm.getBoolean("isActive")) {
						String tName = trooperParm.getString("trooper");
						String clazz = trooperParm.getString("client");
						Class<?> cls = Class.forName("hero.ozsoft.bots." + clazz);
						int cash = parameters.getInteger("buyIn");
						// Constructor cons = cls.getConstructor(String.class);
						// Bot bot = (Bot) cons.newInstance(name);
						// @SuppressWarnings("deprecation")
						Bot bot = (Bot) cls.newInstance();
						Trooper trooper = bot.getSimulationTrooper(table, trooperParm, parameters);
						Player player = new Player(tName, cash, bot, trooperParm.getInteger("chair"));
						table.addPlayer(player);
						if (trooperParm.isHero())
							setTrooper(trooper);
					}
				}
				if (numOfTask == 1)
					oneTable = table;
				else
					taskGroup.addTable(table);
			}

			// if there is only 1 task, show the table dialog
			if (numOfTask == 1) {
				oneTable.getTableDialog();
				return oneTable;
			} else
				return taskGroup;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean validateFields() {
		boolean ok = super.validateFields();
		boolean ok1 = checkVariables();
		return ok && ok1;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (trooperList == e.getSource() && !e.getValueIsAdjusting()) {
			TrooperParameter model = (TrooperParameter) trooperList.getSelectedValue();
			if (model == null)
				return;

			TrooperPanel trooperPanel = new TrooperPanel(model);
			Alesia.getMainPanel().showInScrollPanel(trooperPanel);
		}
	}
}
