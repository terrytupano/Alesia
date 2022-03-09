package plugins.hero;

import java.beans.*;
import java.util.*;

import org.javalite.activejdbc.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

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
		// activeCaption
		// desktop
		// TODO: old parameter for triangular distribbution
		// addInputComponent(TUIUtils.getTWebComboBox("tau", "tdisrange", model.get("tau")));

		// WebComboBox webc = TUIUtils.getTWebComboBox("preflopCards", PreflopCardsModel.getPreflopList(),
		// model.get("preflopCards"));
		// addInputComponent(webc);
		addInputComponent(TUIUtils.getNumericTextField("tau", model, columns), false, true);
		addInputComponent(TUIUtils.getWebCheckBox("strictPreflop", model.getBoolean("strictPreflop")));
		addInputComponent(TUIUtils.getNumericTextField("reconnBase", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("reconnBand", model, columns), false, true);
		addInputComponent(TUIUtils.getWebCheckBox("takeOpportunity", model.getBoolean("takeOpportunity")));
		addInputComponent(TUIUtils.getNumericTextField("phi", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("playTime", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("playUntil", model, columns), false, true);
		String tparm = null;
		String c = model.getString("currency");
		if (c != null) {
			tparm = model.getString("buyIn") + "," + model.getString("bigBlind") + "," + model.getString("smallBlind")
					+ ("".equals(c.trim()) ? "" : "," + c);
		}
		addInputComponent(TUIUtils.getTWebComboBox("tableParameters", "table.parameters0", tparm));

		// FormLayout layout = new FormLayout("left:pref:grow");
		// DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);
		// builder.append(TUIUtils.getTitlePanel("playTime"));
		// builder.append(getInputComponent("playTime"));
		// builder.append(TUIUtils.getTitlePanel("playUntil"));
		// builder.append(getInputComponent("playUntil"));
		// builder.append(TUIUtils.getTitlePanel("tableParameters"));
		// builder.append(getInputComponent("tableParameters"));
		// builder.append(TUIUtils.getTitlePanel("tau"));
		// builder.append(getInputComponent("tau"));
		// builder.append(TUIUtils.getTitlePanel("reconnBase"));
		// builder.append(getInputComponent("reconnBase"));
		// builder.append(TUIUtils.getTitlePanel("phi"));
		// builder.append(getInputComponent("phi"));
		// builder.append(TUIUtils.getTitlePanel("takeOpportunity"));
		// builder.append(getInputComponent("takeOpportunity"));
		// builder.append(TUIUtils.getTitlePanel("strictPreflop"));
		// builder.append(getInputComponent("strictPreflop"));

		FormLayout layout = new FormLayout(
				"left:default:grow, 3dlu, left:default:grow, 3dlu, left:default:grow, 3dlu, left:default:grow");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append(TStringUtils.getString("playTime"), getInputComponent("playTime"));
		builder.append(TStringUtils.getString("playUntil"), getInputComponent("playUntil"));
		builder.nextLine();
		builder.append(TStringUtils.getString("tableParameters"), getInputComponent("tableParameters"), 5);
		builder.nextLine();
		// builder.append(TStringUtils.getString("preflopCards"), getInputComponent("preflopCards"));
		builder.append(TStringUtils.getString("tau"), getInputComponent("tau"));
		builder.nextLine();
		builder.append(TStringUtils.getString("reconnBase"), getInputComponent("reconnBase"));
		builder.append(TStringUtils.getString("reconnBand"), getInputComponent("reconnBand"));
		builder.nextLine();
		builder.append(TStringUtils.getString("phi"), getInputComponent("phi"));
		builder.nextLine();
		builder.append(getInputComponent("takeOpportunity"));
		builder.append(getInputComponent("strictPreflop"));
		setBodyComponent(builder.getPanel());

		setBodyComponent(builder.getPanel());

		setFooterActions("update");

	}

	@Override
	public Map<String, Object> getValues() {
		// parse tableparameters
		Map values = super.getValues();
		String[] tparms = values.get("tableParameters").toString().split("[,]");
		values.put("buyIn", new Double(tparms[0]));
		values.put("bigBlind", new Double(tparms[1]));
		values.put("smallBlind", new Double(tparms[2]));
		// simbol if its present of "" if not
		values.put("currency", tparms.length > 3 ? tparms[3] : "");
		return values;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object act = evt.getNewValue();
		if (TActionsFactory.ACTION_PERFORMED.equals(evt.getPropertyName()) && act != null) {
			getModel().save();
		}
	}
}
