package plugins.hero;

import java.util.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.*;

public class TrooperPanel extends TUIFormPanel {

	public TrooperPanel() {
		addInputComponent(TUIUtils.getTWebComboBox("table.parameters", "table.parameters"));
		addInputComponent(TUIUtils.getNumericTextField("play.time", null, 5, null), false, true);
		addInputComponent(TUIUtils.getNumericTextField("play.until", null, 5, null), false, true);
		
		addInputComponent(TUIUtils.getTWebComboBox("preflopStrategy", "table.strategy"));
		addInputComponent(TUIUtils.getTWebComboBox("decisionMethod", "decisionMet"));
		
		addInputComponent(TUIUtils.getTWebComboBox("tDistributionRange", "tdisrange"));
		
		addInputComponent(TUIUtils.getWebTextField("availableActions", null, 50));
		addInputComponent(TUIUtils.getWebCheckBox("takeOportunity"));
		addInputComponent(TUIUtils.getNumericTextField("bluff", null, 5, null), false, true);
		
		addInputComponent(TUIUtils.getNumericTextField("preflopRekonAmmo.base", null, 5, null), false, true);
		addInputComponent(TUIUtils.getNumericTextField("preflopRekonAmmo.hand", null, 5, null), false, true);

		FormLayout layout = new FormLayout("right:pref, 3dlu, left:pref, 3dlu, left:pref, 3dlu, left:pref, 70dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.appendSeparator("Table parameters");
		builder.nextLine();
		builder.append(TStringUtils.getString("play.time"), getInputComponent("play.time"));
		builder.append(TStringUtils.getString("play.until"), getInputComponent("play.until"));
		builder.nextLine();
		builder.append(TStringUtils.getString("table.parameters"), getInputComponent("table.parameters"), 6);
		builder.nextLine();

		builder.appendSeparator("Trooper parameters");
		builder.nextLine();
		builder.append(TStringUtils.getString("preflopStrategy"), getInputComponent("preflopStrategy"), 6);
		builder.append(getInputComponent("takeOportunity"));
		builder.append(TStringUtils.getString("bluff"), getInputComponent("bluff"));
		builder.nextLine();
		builder.append(TStringUtils.getString("decisionMethod"), getInputComponent("decisionMethod"), 6);
		builder.append(TStringUtils.getString("tDistributionRange"), getInputComponent("tDistributionRange"), 6);
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
		// simbol if its present of "" if not
		vals.put("table.currency", tparms.length > 3 ? tparms[3] : "");
		return vals;
	}
}
