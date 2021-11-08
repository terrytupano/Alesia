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

	public TrooperPanel(boolean toolbar) {

		SimulatorClient model = SimulatorClient.findFirst("playerName = ?", "Hero");
		Map<String, ColumnMetadata> columns = SimulatorClient.getMetaModel().getColumnMetadata();
		setModel(model);

		addInputComponent(TUIUtils.getNumericTextField("play.time", null, 5, null), false, true);
		addInputComponent(TUIUtils.getNumericTextField("play.until", null, 5, null), false, true);
		addInputComponent(TUIUtils.getTWebComboBox("table.parameters", "table.parameters0"));

		addInputComponent(TUIUtils.getTWebComboBox("tDistributionRange", "tdisrange", model.get("tDistributionRange")));
		WebComboBox webc = TUIUtils.getTWebComboBox("preflopCards", PreflopCardsModel.getPreflopList(),
				model.get("preflopCards"));
		addInputComponent(webc);
		addInputComponent(TUIUtils.getWebCheckBox("takeOpportunity", model.getBoolean("takeOpportunity")));
		addInputComponent(TUIUtils.getNumericTextField("reconnBase", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("reconnBand", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("oppLowerBound", model, columns), false, true);

		FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append(TStringUtils.getString("play.time"), getInputComponent("play.time"));
		builder.append(TStringUtils.getString("play.until"), getInputComponent("play.until"));
		builder.nextLine();
		builder.append(TStringUtils.getString("table.parameters"), getInputComponent("table.parameters"), 5);
		builder.nextLine();

		builder.appendSeparator();
		builder.append(TStringUtils.getString("preflopCards"), getInputComponent("preflopCards"), 5);
		builder.nextLine();
		builder.append(getInputComponent("takeOpportunity"), 3);
		builder.append(TStringUtils.getString("oppLowerBound"), getInputComponent("oppLowerBound"));
		builder.nextLine();
		builder.append(TStringUtils.getString("tDistributionRange"), getInputComponent("tDistributionRange"), 5);
		builder.nextLine();
		builder.append(TStringUtils.getString("reconnBase"), getInputComponent("reconnBase"));
		builder.append(TStringUtils.getString("reconnBand"), getInputComponent("reconnBand"));

		if (toolbar) {
			addToolBarActions("runTrooper", "testTrooper", "stopTrooper", "pauseTrooper");
		}

		setBodyComponent(builder.getPanel());
		registreSettings("play.time", "play.until", "table.parameters");
	}

	@Override
	public Map<String, Object> getValues() {
		Map vals = super.getValues();
		Hero.parseTableParameters(vals);
		return vals;
	}
}
