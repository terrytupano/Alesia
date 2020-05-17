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

import java.beans.*;
import java.util.*;
import java.util.function.*;

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import core.datasource.model.*;
import gui.*;

public class JockeyHistory extends TUIListPanel implements PropertyChangeListener {

	public JockeyHistory() {
//		setToolBar(new ExportToFileAction(this, ""));
		setColumns("redate;rerace;redistance;restar_lane;rehorse;reend_pos;recps");
	}

	@Override
	public void init() {
		setMessage("flicka.msg02");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object src = evt.getSource();
		Object newv = evt.getNewValue();
		Model rcd = (Model) newv;

		String jockey = null;
		Date date = null;
		if (src instanceof RaceList) {
			if (rcd != null) {
				jockey = rcd.getString("rejockey");
				date = rcd.getDate("redate");
			}
		}
		if (src instanceof PDistributionList) {
			if (rcd != null && rcd.getString("pdfield").equals("rejockey")) {
				jockey = rcd.getString("pdvalue");
				date = rcd.getDate("pddate");
			}
		}
		if (jockey != null) {
			final String jockeyf = jockey;
			final Date datef = date;
			Function<String, List<Model>> f = (par -> Race.find("rejockey = ? AND rerace < ?", jockeyf, datef)
					.orderBy("redate DESC"));
			setDBParameters(f, Race.getMetaModel().getColumnMetadata());
		} else {
			setMessage("flicka.msg02");
		}
	}

	@Override
	public TUIFormPanel getTUIFormPanel(ApplicationAction action) {
		return null;
	}
}
