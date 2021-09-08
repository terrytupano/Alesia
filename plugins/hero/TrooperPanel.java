package plugins.hero;

import java.util.*;

import com.alee.laf.combobox.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
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
		addInputComponent(TUIUtils.getTWebComboBox("table.parameters", "table.parameters0"));
		addInputComponent(TUIUtils.getNumericTextField("play.time", null, 5, null), false, true);
		addInputComponent(TUIUtils.getNumericTextField("play.until", null, 5, null), false, true);

		WebComboBox webc = TUIUtils.getTWebComboBox("preflopCards", PreflopCardsModel.getPreflopList(), null);
		addInputComponent(webc);

		addInputComponent(TUIUtils.getTWebComboBox("decisionMethod", "decisionMet0"));

		addInputComponent(TUIUtils.getTWebComboBox("tDistributionRange", "tdisrange"));

		addInputComponent(TUIUtils.getWebTextField("availableActions", null, 50));
		addInputComponent(TUIUtils.getWebCheckBox("takeOportunity"));
		addInputComponent(TUIUtils.getNumericTextField("upperBoundBluff", null, 5, null), false, true);

		addInputComponent(TUIUtils.getNumericTextField("preflopRekonAmmo.base", null, 5, null), false, true);
		addInputComponent(TUIUtils.getNumericTextField("preflopRekonAmmo.hand", null, 5, null), false, true);

		FormLayout layout = new FormLayout("right:pref, 3dlu, left:pref, 3dlu, left:pref, 3dlu, left:90dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append(TStringUtils.getString("play.time"), getInputComponent("play.time"));
		builder.append(TStringUtils.getString("play.until"), getInputComponent("play.until"));
		builder.nextLine();
		builder.append(TStringUtils.getString("table.parameters"), getInputComponent("table.parameters"), 5);
		builder.nextLine();

		builder.append(TStringUtils.getString("preflopCards"), getInputComponent("preflopCards"), 5);
		builder.nextLine();
		builder.append(getInputComponent("takeOportunity"), 7);
		builder.nextLine();
		builder.append(TStringUtils.getString("upperBoundBluff"), getInputComponent("upperBoundBluff"));
		builder.nextLine();
		builder.append(TStringUtils.getString("decisionMethod"), getInputComponent("decisionMethod"), 5);
		builder.append(TStringUtils.getString("tDistributionRange"), getInputComponent("tDistributionRange"), 5);
		builder.nextLine();

		builder.append("maxRekonAmmo = ");
		builder.append(getInputComponent("preflopRekonAmmo.base"));
		builder.append(" + ");
		builder.append(getInputComponent("preflopRekonAmmo.hand"));

		if (toolbar) {
			addToolBarActions("runTrooper", "testTrooper", "stopTrooper", "pauseTrooper");
		}

		setBodyComponent(builder.getPanel());
		registreSettings();
	}

	@Override
	public Map<String, Object> getValues() {
		Map vals = super.getValues();
		Hero.parseTableParameters(vals);
		return vals;
	}
}
