
package hero.ozsoft;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.alee.extended.layout.*;
import com.alee.laf.list.*;
import com.alee.laf.panel.*;
import com.alee.laf.tabbedpane.*;
import com.alee.managers.style.*;

import core.*;
import datasource.*;
import gui.*;
import hero.*;

public class GameSimulatorPanel extends TUIFormPanel implements ListSelectionListener {

//	private SimulatorClientList clientList;
	private TrooperPanel trooperPanel;
	private WebPanel trooperPanelContainer;
	private PokerSimulatorPanel pokerSimulatorPanel;
	private WebList trooperList;

	public GameSimulatorPanel() {
//		SimulatorClient hero = SimulatorClient.findFirst("trooper = ?", "Hero");
		trooperPanel = new TrooperPanel(new TrooperParameter());
		TUIUtils.setEnabledRecursively(trooperPanel, false);

		trooperPanelContainer = new WebPanel();
		trooperPanelContainer.add(trooperPanel);
//		clientList = new SimulatorClientList();
		this.pokerSimulatorPanel = new PokerSimulatorPanel();

		this.trooperList = getTrooperList();
		// trooper panel + list of Clients
		WebPanel panel = new WebPanel(new VerticalFlowLayout());
		panel.add(trooperList);
		panel.add(trooperPanelContainer);
		WebTabbedPane wtp = new WebTabbedPane();
		wtp.add(panel, "Simulation parameters");
		wtp.add(pokerSimulatorPanel, "PockerSimulator");

		setBodyComponent(wtp);
		setFooterActions("backrollHistory", "startSimulation");
		registreSettings();
	}

	public void setTrooper(Trooper trooper) {
		pokerSimulatorPanel.setTrooper(trooper);
	}

	private WebList getTrooperList() {
		TrooperParameter trooperParameters[] = TrooperParameter.findAll().orderBy("chair")
				.toArray(new TrooperParameter[0]);
		WebList list = new WebList(StyleId.listTransparent, trooperParameters);
		list.addListSelectionListener(this);
		list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		list.setVisibleRowCount(4);
		list.addPropertyChangeListener(e-> {
			if("ancestor".equals(e.getPropertyName())) {
				Dimension dimension = list.getSize();
				list.setFixedCellWidth((int) dimension.getWidth() / 2);
			}	
		});
		TrooperCellRenderer cellRenderer = new TrooperCellRenderer();
		ComponentAdapter adapter = new ComponentAdapter() {
			private void computeSize(ComponentEvent e) {
				Dimension dimension = e.getComponent().getSize();
				list.setFixedCellWidth((int) dimension.getWidth() / 2);
			}
			@Override
			public void componentShown(ComponentEvent e) {
				computeSize(e);
			}
			@Override
			public void componentResized(ComponentEvent e) {
				computeSize(e);				
			}
		};
		list.addComponentListener(adapter);
		list.setCellRenderer(cellRenderer);
		return list;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (trooperList == e.getSource()) {
			trooperPanelContainer.setVisible(false);
			trooperPanelContainer.removeAll();
			TrooperParameter model = (TrooperParameter) trooperList.getSelectedValue();
			boolean ena = true;
			if (model == null) {
				model = new TrooperParameter();
				ena = false;
			}
			trooperPanel = new TrooperPanel(model);
			TUIUtils.setEnabledRecursively(trooperPanel, ena);
			trooperPanelContainer.add(trooperPanel);
			trooperPanelContainer.setVisible(true);
		}
	}
}
