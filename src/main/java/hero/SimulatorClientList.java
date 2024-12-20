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
package hero;

import java.util.*;
import java.util.function.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import core.*;
import datasource.*;

import gui.*;

public class SimulatorClientList extends TUIListPanel {

	public SimulatorClientList() {
//		addToolBarActions("newModel", "editModel", "deleteModel");
		setColumns("trooper;chair;tau");
		// setIconParameters("0;gender-;rehorsegender");
	}

	@Override
	public TUIFormPanel getTUIFormPanel(ApplicationAction action) {
		return null;
	}

	@Override
	public void init() {
		// setMessage("flicka.msg01");
		Function<String, List<Model>> funtion = (par -> TrooperParameter.findAll());
		setDBParameters(funtion, TrooperParameter.getMetaModel().getColumnMetadata());
		// getWebTable().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}
}
