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
package flicka;

import java.beans.*;
import java.util.*;
import java.util.function.*;

import javax.swing.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import core.*;
import datasource.*;

import gui.*;

public class RaceList extends TUIListPanel implements PropertyChangeListener {

	private Race sourceModel;

	public RaceList() {
		// actionMap = Alesia.getInstance().getContext().getActionMap(this);
		addToolBarActions(TActionsFactory.getActions("newModel", "editModel", "deleteModel"));

		// newFromTable.setIcon("newFromTable");
		setColumns("restar_lane;rehorse;rejockey;reend_pos;rejockey_weight;recps");
		setIconParameters("-1; ");
		getTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	@Override
	public TUIFormPanel getTUIFormPanel(ApplicationAction action) {
		TUIFormPanel tuifp = null;
		if (action.getName().equals("newModel")) {
			tuifp = new RaceRecord(this, new Race(), true, RaceRecord.BASIC);
		}
		if (action.getName().equals("editModel")) {
			tuifp = new RaceRecord(this, getModel(), false, RaceRecord.BASIC);
		}
		return tuifp;
	}

	public void getUIFor(AbstractAction aa) {
		// if (aa == baseNewRecord) {
		// Record rcd = getRecordModel();
		// RaceRecord.copyFields(sourceRcd, rcd, RaceRecord.EVENT);
		// pane = new RaceRecord(rcd, true, RaceRecord.BASIC);
		// }
		// if (aa == baseEditRecord) {
		// Record rcd = getRecord();
		// pane = new RaceRecord(rcd, false, RaceRecord.BASIC);
		// }
		// if (aa == newFromTable) {
		// Record rcd = getRecordModel();
		// RaceRecord.copyFields(sourceRcd, rcd, RaceRecord.EVENT);
		// recordFromTable = new RaceRecordFromTable(rcd, true);
		// pane = recordFromTable;
		// }
	}

	@Override
	public void init() {
		setMessage("flicka.msg01");
	}
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object newv = evt.getNewValue();
		if (src instanceof DBExplorer) {
			this.sourceModel = (Race) newv;
			if (sourceModel != null) {
				Date d = sourceModel.getDate("redate");
				int r = sourceModel.getInteger("rerace");
				Function<String, List<Model>> f = (par -> Race.find("redate = ? AND rerace = ?", d, r)
						.orderBy("redate DESC"));
				setDBParameters(f, Race.getMetaModel().getColumnMetadata());
			} else {
				setMessage("flicka.msg01");
			}
		}
	}
}
