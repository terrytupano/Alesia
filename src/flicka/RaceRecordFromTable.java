package flicka;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;

import core.*;
import datasource.*;

import gui.*;
import gui.table.*;

public class RaceRecordFromTable extends TUIFormPanel implements ActionListener {

	private JTable jTable;
	private Race rModel;
	private String[] columns;

	public RaceRecordFromTable(Race rcd) {
		this.rModel = rcd;
		// table columns
		columns = new String[]{"restar_lane", "rehorse", "rejockey", "rejockey_weight", "reend_pos", "recps", "reobs",
				"retrainer"};

		// table data
		Object[][] data = new Object[14][columns.length];
		for (int r = 0; r < 14; r++) {
			for (int c = 0; c < columns.length; c++) {
				data[r][c] = rModel.get(columns[c]);
			}
		}

		this.jTable = new JTable(data, columns);
		ExcelAdapter ea = new ExcelAdapter(jTable);
		ea.setPasteActionListener(this);
		jTable.setPreferredScrollableViewportSize(new Dimension(950, 250));
		// jTable.getModel().addTableModelListener(this);
		jTable.setCellSelectionEnabled(true);
		// jTable.setColumnSelectionAllowed(true);
		setBodyComponent(new JScrollPane(jTable));
		addFooterActions("acept", "cancel");
		preValidate();
	}

	public void updateRecords() {
		TableModel model = jTable.getModel();
		for (int r = 0; r < model.getRowCount(); r++) {
			if (model.getValueAt(r, 1).toString().length() == 0) {
				continue;
			}
			Race rcd = new Race();
			rcd.copyFrom(rModel);
			rcd.set("restar_lane", new Integer(model.getValueAt(r, 0).toString()));
			rcd.set("rehorse", model.getValueAt(r, 1).toString());
			rcd.set("rejockey", model.getValueAt(r, 2).toString());
			rcd.set("rejockey_weight", new Integer(model.getValueAt(r, 3).toString()));
			rcd.set("reend_pos", new Integer(model.getValueAt(r, 4).toString()));
			rcd.set("recps", new Double(model.getValueAt(r, 5).toString()));
			rcd.set("reobs", model.getValueAt(r, 6).toString());
			rcd.set("retrainer", model.getValueAt(r, 7).toString());
			rcd.save();
		}
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		// super.actionPerformed(ae);

		TableModel model = jTable.getModel();
		for (int r = 0; r < model.getRowCount(); r++) {
			if (model.getValueAt(r, 1).toString().length() == 0) {
				continue;
			}

			// uppercase for horse names
			String ho = model.getValueAt(r, 1).toString().toUpperCase();

			// check if horse exist. if not, mark
			if (Race.findFirst("rehorse = ?", ho) == null) {
				ho = ">>" + ho;
			}
			model.setValueAt(ho, r, 1);

			// Check and format jockey
			String[] jos = model.getValueAt(r, 2).toString().split("[ ]");
			String jo = jos[0] + " ";
			for (int c = 1; c < jos.length; c++) {
				String j = jos[c];
				jo += (j.length() > 2) ? j + " " : "";
			}
			if (Race.findFirst("rejockey = ?", jo) == null) {
				jo = ">>" + jo;
			}
			model.setValueAt(jo, r, 2);

			// change fractions by decimal representation
			String[] cp = model.getValueAt(r, 5).toString().split("[ ]");
			String cps = cp[0];
			if (cp.length > 1) {
				cps += (cp[1].equals("1/4") ? ".25" : "");
				cps += (cp[1].equals("1/2") ? ".50" : "");
				cps += (cp[1].equals("3/4") ? ".75" : "");
				model.setValueAt(cps, r, 5);
			}
		}
	}
}
