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

import org.javalite.activejdbc.*;
import org.jdesktop.application.*;

import core.*;
import datasource.*;

import gui.*;

public class HorseHistory extends TUIListPanel implements PropertyChangeListener {

	public HorseHistory() {
//		setToolBar(new ExportToFileAction(this, ""));
		setColumns("redate;rerace;redistance;restar_lane;rejockey;reend_pos;recps");
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

		String horse = null;
		Date date = null;
		if (src instanceof RaceList) {
			if (rcd != null) {
				horse = rcd.getString("rehorse");
				date = rcd.getDate("redate");
			}
		}
		if (src instanceof PDistributionList) {
			if (rcd != null && rcd.getString("pdfield").equals("rehorse")) {
				horse = rcd.getString("pdvalue");
				date = rcd.getDate("pddate");
			}
		}
		if (horse != null) {
			final String horsef = horse;
			final Date datef = date;
			Function<String, List<Model>> f = (par -> Race.find("rehorse = ? AND rerace < ?", horsef, datef)
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
