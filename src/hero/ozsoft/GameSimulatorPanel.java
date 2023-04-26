
package hero.ozsoft;

import java.awt.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.border.*;

import org.javalite.activejdbc.*;

import com.alee.extended.layout.*;
import com.alee.laf.list.*;
import com.alee.laf.panel.*;
import com.alee.laf.scroll.*;
import com.alee.laf.tabbedpane.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import datasource.*;
import gui.*;
import gui.jgoodies.*;
import hero.*;

public class GameSimulatorPanel extends TUIFormPanel implements PropertyChangeListener {

//	private SimulatorClientList clientList;
	private TrooperPanel trooperPanel;
	private WebPanel pockerSimulatorPanel, trooperPanelContainer;

	public GameSimulatorPanel() {
//		SimulatorClient hero = SimulatorClient.findFirst("trooper = ?", "Hero");
		trooperPanel = new TrooperPanel(new TrooperParameter());
		TUIUtils.setEnabled(trooperPanel, false);

		trooperPanelContainer = new WebPanel();
		trooperPanelContainer.add(trooperPanel);
//		clientList = new SimulatorClientList();
		this.pockerSimulatorPanel = new WebPanel(new BorderLayout());

		// trooper panel + list of Clients
		WebPanel panel = new WebPanel(new VerticalFlowLayout());
//		panel.add(trooperPanel);
		panel.add(getWebList());
		WebTabbedPane wtp = new WebTabbedPane();
		wtp.add(panel, "Simulation parameters");
		wtp.add(pockerSimulatorPanel, "PockerSimulator");

		setBodyComponent(wtp);
		setFooterActions("backrollHistory", "startSimulation");
		registreSettings();
	}

	public void updatePokerSimulator(PokerSimulator simulator) {
		setVisible(false);
		pockerSimulatorPanel.removeAll();
		pockerSimulatorPanel.add(simulator.getReportPanel(), BorderLayout.CENTER);
		setVisible(true);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (TUIListPanel.MODEL_SELECTED.equals(evt.getPropertyName())) {
			trooperPanelContainer.setVisible(false);
			trooperPanelContainer.removeAll();
			TrooperParameter model = (TrooperParameter) evt.getNewValue();
			boolean ena = true;
			if (model == null) {
				model = new TrooperParameter();
				ena = false;
			}
			trooperPanel = new TrooperPanel(model);
			TUIUtils.setEnabled(trooperPanel, ena);
			trooperPanelContainer.add(trooperPanel);
			trooperPanelContainer.setVisible(true);
		}
	}

	private WebScrollPane getWebList() {
//		Function<String, List<Model>> funtion = (par -> TrooperParameter.findAll());
		TrooperParameter trooperParameters[] = TrooperParameter.findAll().toArray(new TrooperParameter[0]);
		WebList list = new WebList(trooperParameters);
		list.setVisibleRowCount(4);
		TrooperParameterCellRenderer cellRenderer = new TrooperParameterCellRenderer("robot.png", "trooper", "takeOpportunity");
		list.setCellRenderer(cellRenderer);
		WebScrollPane scrollPane = new WebScrollPane(list); 
//		clientList.addPropertyChangeListener(TUIListPanel.MODEL_SELECTED, this);
		return scrollPane;
	}

	public static class TrooperParameterCellRenderer extends DefaultListCellRenderer {

		private ListItem listItem;
		private String icon, titleField, messageField;

		public TrooperParameterCellRenderer(String icon, String titleField, String messageField) {
			this.listItem = new ListItem();
			this.icon = icon;
			this.titleField = titleField;
			this.messageField = messageField;
			listItem.setIcon(TResources.getIcon(icon, 20));
		}

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			Model model = (Model) value;
			listItem.setBackground(getBackground());
			listItem.setLine(model.getString(titleField), model.getString(messageField));
			return listItem;
		}
	}
}
