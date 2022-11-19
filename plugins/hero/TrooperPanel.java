package plugins.hero;

import java.awt.*;
import java.beans.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.javalite.activejdbc.*;

import com.alee.extended.layout.*;
import com.alee.laf.panel.*;
import com.alee.managers.style.*;
import com.alee.utils.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.FormLayout;

import core.*;
import core.datasource.model.*;
import gui.*;

/**
 * Pannel with all configuration parameters for the trooper.
 * 
 * @author terry
 * 
 */
public class TrooperPanel extends TUIFormPanel implements PropertyChangeListener {

	public TrooperPanel(TrooperParameter model) {
		addPropertyChangeListener(TActionsFactory.ACTION_PERFORMED, this);
		Map<String, ColumnMetadata> columns = TrooperParameter.getMetaModel().getColumnMetadata();
		setModel(model);

		addInputComponent(TUIUtils.getNumericTextField("tau", model, columns), false, true);
		addInputComponent(TUIUtils.getWebSwitch("strictPreflop", model.getBoolean("strictPreflop")));
		addInputComponent(TUIUtils.getNumericTextField("reconnBase", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("reconnBand", model, columns), false, true);
		addInputComponent(
				TUIUtils.getTWebComboBox("takeOpportunity", "take.oportutiny", model.getString("takeOpportunity")));
		addInputComponent(TUIUtils.getNumericTextField("phi", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("phi4", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("playTime", model, columns, "#.##"), false, true);
		addInputComponent(TUIUtils.getNumericTextField("playUntil", model, columns, "#.##"), false, true);

		FormLayout layout = new FormLayout("fill:10dlu:grow");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);
		// DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		// builder.append(TUIUtils.getTitleTextLabel("playTime"), getInputComponent("playTime"));
		builder.append(TUIUtils.getConfigLinePanel("playTime", getInputComponent("playTime")));
		builder.nextLine();
		builder.append(TUIUtils.getConfigLinePanel("playUntil", getInputComponent("playUntil")));
		builder.nextLine();
		builder.append(TUIUtils.getConfigLinePanel("strictPreflop", getInputComponent("strictPreflop")));
		builder.nextLine();
		builder.append(TUIUtils.getConfigLinePanel("tau", getInputComponent("tau")));
		builder.nextLine();
		builder.append(TUIUtils.getConfigLinePanel("reconnBase", getInputComponent("reconnBase")));
		builder.nextLine();
		builder.append(TUIUtils.getConfigLinePanel("reconnBand", getInputComponent("reconnBand")));
		builder.nextLine();
		builder.append(TUIUtils.getConfigLinePanel("takeOpportunity", getInputComponent("takeOpportunity")));
		builder.nextLine();
		builder.append(TUIUtils.getConfigLinePanel("phi", getInputComponent("phi")));
		builder.nextLine();
		builder.append(TUIUtils.getConfigLinePanel("phi4", getInputComponent("phi4")));
		builder.nextLine();

		setBodyComponent(builder.getPanel());

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
