
package hero;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.Action;
import javax.swing.event.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;
import org.jdesktop.application.Task.*;

import com.alee.laf.grouping.*;
import com.alee.laf.list.*;
import com.alee.laf.panel.*;
import com.alee.managers.style.*;

import core.*;
import datasource.*;
import hero.ozsoft.*;
import hero.ozsoft.bots.*;
import hero.ozsoft.gui.*;

public class GameSimulatorPanel extends TUIFormPanel implements ListSelectionListener {

	private PokerSimulatorPanel pokerSimulatorPanel;
	private WebList trooperList;
	private List<Table> tables;

	public GameSimulatorPanel() {
		this.tables = new ArrayList<>();
		this.pokerSimulatorPanel = new PokerSimulatorPanel();

		// to today, i need only 1 simulation parameter
		LazyList<SimulationParameters> list = SimulationParameters.findAll();
		SimulationParameters model = list.isEmpty() ? new SimulationParameters() : list.get(0);

		Map<String, ColumnMetadata> columns = SimulationParameters.getMetaModel().getColumnMetadata();
		setModel(model);

		addInputComponent(TUIUtils.getWebTextField("simulationName", model, columns), true, true);
		addInputComponent(TUIUtils.getWebTextField("simulationVariable", model, columns), true, true);
		addInputComponent(TUIUtils.getNumericTextField("simulationsHands", model, columns), true, true);
		addInputComponent(TUIUtils.getSwitch("pauseOnHero", model.getBoolean("pauseOnHero")));
		addInputComponent(TUIUtils.getComboBox("speed", "simulation.speed", model.getString("speed")));

		TActionsFactory.insertActions(this);
//		addToolBarActions(UPDATECHANGES, CANCELCHANGES);
//		addToolBarActions("backrollHistory", "startSimulation");

//		setTitleDescriptionFrom("trooper", "description");
		WebPanel panel = TUIUtils.getFormListItems(getInputComponents());
		GroupPane groupPane = TUIUtils.getPlayStopToggleButtons("startSimulation", "stopSimulation", "pauseSimulation");

		addToolBarAction("backrollHistory");
		getToolBar().add(groupPane);

//		ListItem item = new ListItem(TActionsFactory.getAction("showPokerSimulator"));
//		panel.add(item, FormLayout.LINE);

		setBodyComponent(panel);
	}

	@org.jdesktop.application.Action
	public void showPokerSimulator() {
		Alesia.getMainPanel().showPanel(pokerSimulatorPanel);
	}

	@org.jdesktop.application.Action
	public void backrollHistory() {
		LazyList<SimulationResult> results = SimulationResult.find("trooper = ? AND hands = ?", "Hero", 0);
		ArrayList<String> names = new ArrayList<>();
		results.forEach(sr -> names.add(sr.getString("name")));
		String[] possibleValues = names.toArray(new String[0]);
		Object selectedValue = JOptionPane.showInputDialog(Alesia.getMainFrame(), "Choose one", "Input",
				JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
		if (selectedValue != null) {
			String resultName = selectedValue.toString();

			// Retrieve the first element of the statistical series to detect, the type of
			// graph
			Alesia.getInstance().openDB("hero");
			SimulationResult sample = SimulationResult.findFirst("name = ? AND trooper = ?", resultName, "Hero");
			JDialog chartDialog;
			if (sample.get("aditionalValue") != null) {
				chartDialog = new SingeVariableSimulationLineChart(resultName);
			} else {
				chartDialog = new MultiVariableSimulationBarChar(resultName);
			}
			Rectangle bound = Alesia.getMainFrame().getBoundByFactor(0.7);
			chartDialog.setSize(bound.getSize());
//			chartDialog.pack();
			GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
			chartDialog.setLocationRelativeTo(Alesia.getMainFrame());
			chartDialog.setVisible(true);
		}
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

	@org.jdesktop.application.Action
	public void pauseSimulation() {
		tables.forEach(t -> t.pause(true));
	}

	public void setTrooper(Trooper trooper) {
		pokerSimulatorPanel.setTrooper(trooper);
	}

	@org.jdesktop.application.Action(block = BlockingScope.ACTION)
	// @org.jdesktop.application.Action(block = BlockingScope.WINDOW)
	// @org.jdesktop.application.Action
	public Task<Void, Void> startSimulation(ActionEvent event) {
		try {
			// save current changes
			if (!validateFields())
				return null;

			// check if are active simulations
			if (!tables.isEmpty()) {
				Alesia.showNotification("hero.msg05");
				return null;
			}
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
			LazyList<TrooperParameter> tparms = TrooperParameter.findAll().orderBy("chair");

			TrooperParameter hero = TrooperParameter.findFirst("trooper = ?", "Hero");
			int buy = hero.getDouble("buyIn").intValue();
			int bb = hero.getDouble("bigBlind").intValue();
			Table table = new Table(TableType.NO_LIMIT, buy, bb);
			for (TrooperParameter tparm : tparms) {
				if (tparm.getBoolean("isActive")) {
					String tName = tparm.getString("trooper");
					String bCls = tparm.getString("client");
					Class<?> cls = Class.forName("hero.ozsoft.bots." + bCls);
					// Constructor cons = cls.getConstructor(String.class);
					// Bot bot = (Bot) cons.newInstance(name);
//					 @SuppressWarnings("deprecation")
					Bot bot = (Bot) cls.newInstance();
					Trooper trooper = bot.getSimulationTrooper(table, tparm);
					Player player = new Player(tName, buy, bot, tparm.getInteger("chair"));
					table.addPlayer(player);
					if ("Hero".equals(tName))
						setTrooper(trooper);
				}
			}

			TableDialog dialog = new TableDialog(table);
			dialog.setVisible(true);
			tables.add(table);
			return table;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@org.jdesktop.application.Action
	public void stopSimulation() {
		tables.forEach(t -> t.cancel(true));
		tables.clear();
		System.out.println("GameSimulatorPanel.stopSimulation()");
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
