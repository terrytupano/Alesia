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
package plugins.flicka;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Action;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;
import org.jdesktop.application.Task.*;

import com.alee.api.data.*;
import com.alee.extended.dock.*;
import com.alee.extended.tab.*;
import com.alee.laf.window.*;
import com.alee.utils.*;

import core.*;
import core.datasource.model.*;
import gui.*;

public class Flicka extends TPlugin {

	protected static ActionMap actionMap;

	public Flicka() {
		actionMap = Alesia.getInstance().getContext().getActionMap(this);
		Alesia.getInstance().openDB("flicka");
		TActionsFactory.insertActions(actionMap);
	}

	@Override
	public ArrayList<javax.swing.Action> getUI(String type) {
		ArrayList<Action> alist = new ArrayList<>();
		alist.add(actionMap.get("flicka"));
		return alist;
	}

	@org.jdesktop.application.Action(block = BlockingScope.APPLICATION)
	public Task<Void, Void> runMultiSimulation(ActionEvent event) {
		AbstractButton src = (AbstractButton) event.getSource();
		TUIListPanel tuilp = SwingUtils.getFirstParent(src, TUIListPanel.class);
		Model models[] = tuilp.getModels();
		// MultipleSimulationTask t = new MultipleSimulationTask(models);
		// TTaskMonitor ttm = new TTaskMonitor(t);
		DoNothingTask t = new DoNothingTask();
		TTaskMonitor ttm = new TTaskMonitor(t, true);
		t.setInputBlocker(ttm);
		return t;
	}

	@org.jdesktop.application.Action
	public void raceFromTable(ActionEvent event) {
		TUIListPanel tuilp = SwingUtils.getFirstParent((JComponent) event.getSource(), TUIListPanel.class);
		RaceRecordFromTable rmft = new RaceRecordFromTable((Race) tuilp.getModel());
		WebDialog dlg = rmft.createDialog(false);
		dlg.setVisible(true);
		ApplicationAction aa = (ApplicationAction) rmft.getClientProperty("actionPerformed");
		if (aa != null && aa.getName().equals("acept")) {
			rmft.updateRecords();
		}
	}

	@org.jdesktop.application.Action
	public void runSimulation(ActionEvent event) {
		AbstractButton src = (AbstractButton) event.getSource();
		TUIListPanel tuilp = SwingUtils.getFirstParent(src, TUIListPanel.class);
		Model model = tuilp.getModel();
		String fieldPx = (tuilp instanceof DBExplorer) ? "re" : "st";
		int race = model.getInteger(fieldPx + "race");
		Date date = model.getDate(fieldPx + "date");
		Selector.runSimulation(race, date);
	}

	@org.jdesktop.application.Action
	public void countEndPositions(ActionEvent event) {
		DBExplorer instance = SwingUtils.getFirstParent((AbstractButton) event.getSource(), DBExplorer.class);
		Selector.checkReend_posAndRecpsFields(instance.getModels());
	}

	private WebDockableFrame getWebDockableFrame(JComponent cmp, String title, String iconName) {
		WebDockableFrame frame = new WebDockableFrame(cmp.getClass().getName(), title);
		frame.setIcon(TResources.getSmallIcon(iconName));
		frame.setClosable(false);
		frame.setMaximizable(false);
		frame.setFloatable(false);
		frame.add(cmp);
		return frame;
	}
	@org.jdesktop.application.Action
	public void flicka(ActionEvent event) {
		WebDockablePane dockablePane = new WebDockablePane();
		dockablePane.setSidebarVisibility(SidebarVisibility.never);

		WebDockableFrame DBE = getWebDockableFrame(new DBExplorer(), "Flicka RESLR explorer", "DBExplorer");
		DBE.setPosition(CompassDirection.west);

		WebDockableFrame races = getWebDockableFrame(new RaceList(), "Selected races", "trojan_horse");
		races.setPosition(CompassDirection.east);

		WebDockableFrame pdistri = getWebDockableFrame(new PDistributionList(), "Probability distribution", "barchart");
		races.setPosition(CompassDirection.south);

		WebDocumentPane<DocumentData> tabs = new WebDocumentPane<>();
		tabs.openDocument(new DocumentData<JComponent>(HorseHistory.class.getName(), TResources.getSmallIcon("donut"),
				"Horse history", new HorseHistory()));
		tabs.openDocument(new DocumentData<JComponent>(JockeyHistory.class.getName(),
				TResources.getSmallIcon("user_racer"), "Jockey history", new JockeyHistory()));

		dockablePane.addFrame(DBE);
		dockablePane.addFrame(races);
		dockablePane.addFrame(pdistri);
		dockablePane.setContent(tabs);

		dockablePane.registerSettings(
				new com.alee.managers.settings.Configuration<DockablePaneState>("FlickaDockablePane"));

		Alesia.getInstance().getMainPanel().setContentPanel(dockablePane);
	}

	/**
	 * filter the reslr table returning one element according to the field argument. the valid field argument are
	 * rehorse or rejockey
	 * 
	 * @param field - field for filtering
	 * @return subset of reslr table with one element of the fileter argument
	 */
	public static TEntry[] getElemets(String field, String emptyF) {
		LazyList<Race> races = Race.findAll();
		Vector<String> tmpList = new Vector<String>();
		Vector<TEntry> reslrr = new Vector<TEntry>();
		if (emptyF != null) {
			reslrr.add(TStringUtils.getTEntry(emptyF));
		}
		for (Race race : races) {
			String ele = race.getString(field);
			if (!tmpList.contains(ele)) {
				tmpList.add(ele);
				reslrr.add(new TEntry(ele, ele));
			}
		}
		TEntry[] te = new TEntry[reslrr.size()];
		reslrr.copyInto(te);
		return te;
	}

	/**
	 * return the reslr list filter by field parameter. e.g: if field = rehorse, the return list contain ony one element
	 * of reslr file ......
	 * 
	 * @param field - fiel to filter list
	 * 
	 * @return public static ServiceRequest getFilterServiceReques(String field) { Vector<String> tmpList = new
	 *         Vector<String>(); Vector<Record> reslr = ConnectionManager.getAccessTo("reslr").search(null, "redate
	 *         DESC"); Vector<Record> reslrr = new Vector<Record>(); for (Record rcd : reslr) { String ele = (String)
	 *         rcd.getFieldValue(field); if (!tmpList.contains(ele)) { tmpList.add(ele); reslrr.add(rcd); } }
	 *         ServiceRequest sr = new ServiceRequest(ServiceRequest.CLIENT_GENERATED_LIST, "reslr", reslrr);
	 *         sr.setParameter(ServiceResponse.RECORD_MODEL, ConnectionManager.getAccessTo("reslr").getModel()); return
	 *         sr; }
	 */
}
