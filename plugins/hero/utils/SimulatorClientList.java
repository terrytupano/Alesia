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
package plugins.hero.utils;

import java.util.*;
import java.util.function.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.model.*;
import gui.*;

public class SimulatorClientList extends TUIListPanel {

	public SimulatorClientList() {
		addToolBarActions("newModel", "editModel", "deleteModel");
		setColumns("playerName;client;observationMethod;isActive");
		// setIconParameters("0;gender-;rehorsegender");
	}

	@Override
	public TUIFormPanel getTUIFormPanel(ApplicationAction action) {
		boolean isNew = false;
		SimulatorClient model;
		if (action.getName().equals("newModel")) {
			model = SimulatorClient.create();
			isNew = true;
		} else
			model = (SimulatorClient) getModel();
//		return new SimulatorClientModel(model, isNew);
		return createTUIFormPanel(model, isNew);
	}

	@Override
	public void init() {
		// setMessage("flicka.msg01");
		Function<String, List<Model>> funtion = (par -> SimulatorClient.findAll());
		setDBParameters(funtion, SimulatorClient.getMetaModel().getColumnMetadata());
		// getWebTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	private TUIFormPanel createTUIFormPanel(SimulatorClient model, boolean newr) {
		TUIFormPanel formPanel = new TUIFormPanel();
		formPanel.setModel(model);
		Map<String, ColumnMetadata> columns = SimulatorClient.getMetaModel().getColumnMetadata();
		formPanel.addInputComponent(TUIUtils.getWebTextField("playerName", model, columns), true, true);
		formPanel.addInputComponent(TUIUtils.getTWebComboBox("client", model, "botClient"), false, true);
		formPanel.addInputComponent(TUIUtils.getJCheckBox("isActive", model), false, true);
		formPanel.addInputComponent(TUIUtils.getTWebComboBox("observationMethod", model, "ObservationMth"), false, true);
		formPanel.addInputComponent(TUIUtils.getNumericTextField("alpha", model, columns), false, true);
		formPanel.addInputComponent(TUIUtils.getNumericTextField("tau", model, columns), false, true);
		
		FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);
		builder.append(formPanel.getLabel("playerName"), formPanel.getInputComponent("playerName"));
		builder.append(formPanel.getLabel("client"), formPanel.getInputComponent("client"));
		builder.append(formPanel.getLabel("observationMethod"), formPanel.getInputComponent("observationMethod"));
		builder.nextLine();
		builder.append(formPanel.getLabel("alpha"), formPanel.getInputComponent("alpha"));
		builder.append(formPanel.getLabel("tau"), formPanel.getInputComponent("tau"));
		builder.append(formPanel.getInputComponent("isActive"));

		formPanel.setBodyComponent(builder.build());
		formPanel.setFooterActions("acept", "cancel");
		formPanel.preValidate();
		return formPanel;
	}

}
