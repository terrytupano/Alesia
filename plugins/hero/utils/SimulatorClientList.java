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

import core.datasource.model.*;
import gui.*;

public class SimulatorClientList extends TUIListPanel {

	public SimulatorClientList() {
		addToolBarActions("newModel", "editModel", "deleteModel");
		setColumns("name;client;isActive");
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
		return new SimulatorClientModel(model, isNew);
	}

	@Override
	public void init() {
		// setMessage("flicka.msg01");
		Function<String, List<Model>> funtion = (par -> SimulatorClient.findAll());
		setDBParameters(funtion, SimulatorClient.getMetaModel().getColumnMetadata());
//		getWebTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
}
