package plugins.hero;

import java.beans.*;
import java.util.*;

import org.apache.poi.hwpf.model.types.*;
import org.javalite.activejdbc.*;

import com.alee.laf.combobox.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.model.*;
import gui.*;
import plugins.hero.utils.*;

/**
 * Pannel with all configuration parameters for the trooper.
 * 
 * @author terry
 *
 */
public class TrooperPanel extends TUIFormPanel implements PropertyChangeListener {

	public TrooperPanel(SimulatorClient model) {
		addPropertyChangeListener(TActionsFactory.ACTION_PERFORMED, this);

		Map<String, ColumnMetadata> columns = SimulatorClient.getMetaModel().getColumnMetadata();
		setModel(model);

		// TODO: old parameter for triangular distribbution
		// addInputComponent(TUIUtils.getTWebComboBox("tau", "tdisrange", model.get("tau")));

		WebComboBox webc = TUIUtils.getTWebComboBox("preflopCards", PreflopCardsModel.getPreflopList(),
				model.get("preflopCards"));
		addInputComponent(webc);
		addInputComponent(TUIUtils.getNumericTextField("tau", model, columns), false, true);
		addInputComponent(TUIUtils.getWebCheckBox("strictPreflop", model.getBoolean("strictPreflop")));
		addInputComponent(TUIUtils.getNumericTextField("reconnBase", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("reconnBand", model, columns), false, true);
		addInputComponent(TUIUtils.getWebCheckBox("takeOpportunity", model.getBoolean("takeOpportunity")));
		addInputComponent(TUIUtils.getNumericTextField("oppLowerBound", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("playTime", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("playUntil", model, columns), false, true);

		String c = model.getString("currency");
		String tparm = model.getString("buyIn") + "," + model.getString("bigBlind") + ","
				+ model.getString("smallBlind") + ("".equals(c.trim()) ? "" : "," + c);
		addInputComponent(TUIUtils.getTWebComboBox("tableParameters", "table.parameters0", tparm));

		FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append(TStringUtils.getString("playTime"), getInputComponent("playTime"));
		builder.append(TStringUtils.getString("playUntil"), getInputComponent("playUntil"));
		builder.nextLine();
		builder.append(TStringUtils.getString("tableParameters"), getInputComponent("tableParameters"), 5);
		builder.nextLine();
		builder.append("Preflop cards", getInputComponent("preflopCards"));
		builder.append("Tau", getInputComponent("tau"));
		builder.nextLine();
		builder.append("Reconn: Base", getInputComponent("reconnBase"));
		builder.append("Reconn: Band", getInputComponent("reconnBand"));
		builder.nextLine();
		builder.append("oppLowerBound", getInputComponent("oppLowerBound"));
		builder.nextLine();
		builder.append(getInputComponent("takeOpportunity"));
		builder.append(getInputComponent("strictPreflop"));
		setBodyComponent(builder.getPanel());
	}

	@Override
	public Map<String, Object> getValues() {

		// parse tableparameters to oll implementation format
		Map values = super.getValues();
		String[] tparms = values.get("tableParameters").toString().split("[,]");
		values.put("table.buyIn", new Double(tparms[0]));
		values.put("table.bigBlid", new Double(tparms[1]));
		values.put("table.smallBlid", new Double(tparms[2]));
		// simbol if its present of "" if not
		values.put("table.currency", tparms.length > 3 ? tparms[3] : "");
		return super.getValues();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		System.out.println(evt);
	}
}
