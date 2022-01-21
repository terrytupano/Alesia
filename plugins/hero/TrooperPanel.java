package plugins.hero;

import java.util.*;

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
public class TrooperPanel extends TUIFormPanel {

	public TrooperPanel(SimulatorClient model) {

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

		FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append("Preflop cards", getInputComponent("preflopCards"));
		builder.append("Tau", getInputComponent("tau"));
		builder.nextLine();
		builder.append(getInputComponent("takeOpportunity"), 3);
		builder.append(TStringUtils.getString("oppLowerBound"), getInputComponent("oppLowerBound"));
		builder.nextLine();
		// builder.append(TStringUtils.getString("tau"), getInputComponent("tau"), 5);
		// builder.nextLine();
		builder.append(TStringUtils.getString("reconnBase"), getInputComponent("reconnBase"));
		builder.append(TStringUtils.getString("reconnBand"), getInputComponent("reconnBand"));

		setBodyComponent(builder.getPanel());
	}
}
