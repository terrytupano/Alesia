package plugins.hero;

import java.util.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.*;

public class TrooperPanel extends TUIFormPanel {

	public TrooperPanel(boolean toolbar) {
		addInputComponent(TUIUtils.getTWebComboBox("table.parameters", "table.parameters0"));
		addInputComponent(TUIUtils.getNumericTextField("play.time", null, 5, null), false, true);
		addInputComponent(TUIUtils.getNumericTextField("play.until", null, 5, null), false, true);

		addInputComponent(TUIUtils.getTWebComboBox("preflopStrategy", "table.strategy"));
		addInputComponent(TUIUtils.getTWebComboBox("decisionMethod", "decisionMet0"));

		addInputComponent(TUIUtils.getTWebComboBox("tDistributionRange", "tdisrange"));

		addInputComponent(TUIUtils.getWebTextField("availableActions", null, 50));
		addInputComponent(TUIUtils.getWebCheckBox("takeOportunity"));
		addInputComponent(TUIUtils.getNumericTextField("bluff", null, 5, null), false, true);

		addInputComponent(TUIUtils.getNumericTextField("preflopRekonAmmo.base", null, 5, null), false, true);
		addInputComponent(TUIUtils.getNumericTextField("preflopRekonAmmo.hand", null, 5, null), false, true);

		FormLayout layout = new FormLayout("right:pref, 3dlu, left:pref, 3dlu, left:pref, 3dlu, left:90dlu", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append(TStringUtils.getString("play.time"), getInputComponent("play.time"));
		builder.append(TStringUtils.getString("play.until"), getInputComponent("play.until"));
		builder.nextLine();
		builder.append(TStringUtils.getString("table.parameters"), getInputComponent("table.parameters"), 5);
		builder.nextLine();

		builder.append(TStringUtils.getString("preflopStrategy"), getInputComponent("preflopStrategy"), 5);
		builder.nextLine();
		builder.append(getInputComponent("takeOportunity"),7);
		builder.nextLine();
		builder.append(TStringUtils.getString("bluff"), getInputComponent("bluff"));
		builder.nextLine();
		builder.append(TStringUtils.getString("decisionMethod"), getInputComponent("decisionMethod"), 5);
		builder.append(TStringUtils.getString("tDistributionRange"), getInputComponent("tDistributionRange"), 5);
		builder.nextLine();

		builder.append("maxRekonAmmo = ");
		builder.append(getInputComponent("preflopRekonAmmo.base"));
		builder.append(" + ");
		builder.append(getInputComponent("preflopRekonAmmo.hand"));

		if (toolbar)
			setToolBar("runTrooper", "testTrooper", "stopTrooper", "pauseTrooper");
		
		setBodyComponent(builder.getPanel());
		registreSettings();
	}

	@Override
	public Hashtable<String, Object> getValues() {
		Hashtable vals = super.getValues();
		Hero.parseTableParameters(vals);
		return vals;
	}
}
