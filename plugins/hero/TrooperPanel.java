package plugins.hero;

import java.util.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.*;

public class TrooperPanel extends TUIFormPanel {

	public TrooperPanel() {
		// addInputComponent(TUIUtils.getNumericTextField("table.buyIn", null, 5, "#.00"), true, true);
		// addInputComponent(TUIUtils.getNumericTextField("table.bigBlid", null, 5, "#.00"), true, true);
		// addInputComponent(TUIUtils.getNumericTextField("table.smallBlid", null, 5, "#.00"), true, true);
		// addInputComponent(TUIUtils.getTWebComboBox("table.currency", "table.currency"));
		addInputComponent(TUIUtils.getTWebComboBox("table.parameters", "table.parameters"));
		addInputComponent(TUIUtils.getTWebComboBox("preflopStrategy", "table.strategy"));
		addInputComponent(TUIUtils.getTWebComboBox("minHandPotential", "handRanks"));
		addInputComponent(TUIUtils.getTWebComboBox("oddCalculation", "oddMethod"));
		addInputComponent(TUIUtils.getNumericTextField("preflopRekonAmmo.base", null, 5, null), true, true);
		addInputComponent(TUIUtils.getNumericTextField("preflopRekonAmmo.hand", null, 5, null), true, true);
		addInputComponent(TUIUtils.getWebCheckBox("takeOportunity"));
		addInputComponent(TUIUtils.getWebTextField("availableActions", null, 50));

		FormLayout layout = new FormLayout("right:pref, 3dlu, left:pref, 3dlu, left:pref, 3dlu, left:pref", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.appendSeparator("Table parameters");
		builder.nextLine();
		builder.append(TStringUtils.getString("table.parameters"), getInputComponent("table.parameters"), 5);
		builder.nextLine();

		builder.appendSeparator("Trooper parameters");
		builder.nextLine();
		builder.append(TStringUtils.getString("preflopStrategy"), getInputComponent("preflopStrategy"), 5);
		builder.append(TStringUtils.getString("minHandPotential"), getInputComponent("minHandPotential"), 5);
		builder.append(TStringUtils.getString("oddCalculation"), getInputComponent("oddCalculation"), 5);
		builder.append(TStringUtils.getString("availableActions"), getInputComponent("availableActions"), 5);
		builder.append(getInputComponent("takeOportunity"));
		builder.nextLine();

		builder.appendSeparator("pre flop rekon parameteres");
		builder.nextLine();
		builder.append("maxRekonAmmo = ");
		builder.append(getInputComponent("preflopRekonAmmo.base"));
		builder.append(" + ");
		builder.append(getInputComponent("preflopRekonAmmo.hand"));

		setToolBar("runTrooper", "testTrooper", "stopTrooper", "pauseTrooper");
		setBodyComponent(builder.getPanel());
		registreSettings();
	}

	@Override
	public Hashtable<String, Object> getValues() {
		Hashtable vals = super.getValues();
		String[] tparms = vals.get("table.parameters").toString().split("[,]");
		vals.put("table.buyIn", new Double(tparms[0]));
		vals.put("table.bigBlid", new Double(tparms[1]));
		vals.put("table.smallBlid", new Double(tparms[2]));
		// simbol if is present of "" if not
		vals.put("table.currency", tparms.length > 3 ? tparms[3] : "");
		return vals;
	}
}
