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

import java.util.*;
import java.util.function.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import core.*;
import datasource.*;

import gui.*;

public class StatisticsList extends TUIListPanel {

	public StatisticsList() {
		addToolBarActions(TActionsFactory.getActions("runSimulation", "deleteModel"));
		setColumns("stdate;strace;stfield;stsignature;stdecision;stevent;stdistance;ststdev;stmean");
	}

	@Override
	public void init() {
		Function<String, List<Model>> f = (par -> FlickaStat.findAll());
		setDBParameters(f, FlickaStat.getMetaModel().getColumnMetadata());
	}

	@Override
	public TUIFormPanel getTUIFormPanel(ApplicationAction action) {
		return null;
	}
}
