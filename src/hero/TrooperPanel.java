package hero;

import java.beans.*;
import java.util.*;

import javax.swing.*;

import org.javalite.activejdbc.*;
import org.jfree.ui.tabbedui.*;

import com.alee.extended.layout.*;
import com.alee.laf.panel.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import datasource.*;

import gui.*;
import gui.jgoodies.*;

/**
 * Panel with all configuration parameters for the trooper.
 * 
 * @author terry
 * 
 */
public class TrooperPanel extends TUIFormPanel implements PropertyChangeListener {

	public TrooperPanel(TrooperParameter model) {
		addPropertyChangeListener(TActionsFactory.ACTION_PERFORMED, this);
		Map<String, ColumnMetadata> columns = TrooperParameter.getMetaModel().getColumnMetadata();
		setModel(model);

		addInputComponent(TUIUtils.getNumericTextField("tau", model, columns), true, true);
		addInputComponent(TUIUtils.getWebSwitch("strictPreflop", model.getBoolean("strictPreflop")));
		addInputComponent(TUIUtils.getNumericTextField("reconnBase", model, columns), true, true);
		addInputComponent(TUIUtils.getNumericTextField("reconnBand", model, columns), true, true);
		addInputComponent(
				TUIUtils.getTWebComboBox("takeOpportunity", "take.oportutiny", model.getString("takeOpportunity")));
		addInputComponent(TUIUtils.getWebSwitch("ammoFormula", model.getBoolean("ammoFormula")));
		addInputComponent(TUIUtils.getNumericTextField("phi", model, columns), true, true);
		addInputComponent(TUIUtils.getNumericTextField("phi4", model, columns), true, true);
		addInputComponent(TUIUtils.getNumericTextField("playTime", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("playUntil", model, columns), false, true);

		JComponent component = TUIUtils.getListItems(getComponents());
		setBodyComponent(component);
		setFooterActions("update");
	}

	// @Override
	// public Map<String, Object> getValues() {
	// // parse tableparameters
	// Map<String, Object> values = super.getValues();
	// String[] tparms = values.get("tableParameters").toString().split("[,]");
	// values.put("buyIn", Double.parseDouble(tparms[0]));
	// values.put("bigBlind", Double.parseDouble(tparms[1]));
	// values.put("smallBlind", Double.parseDouble(tparms[2]));
	// // simbol if its present of "" if not
	// values.put("currency", tparms.length > 3 ? tparms[3] : "");
	// return values;
	// }

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object act = evt.getNewValue();
		if (TActionsFactory.ACTION_PERFORMED.equals(evt.getPropertyName()) && act != null) {
			getModel().save();
		}
	}
}
