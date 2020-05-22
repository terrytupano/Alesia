package plugins.hero;

import java.util.*;

import com.alee.laf.text.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;
import com.kitfox.svg.*;

import core.*;
import gui.*;

public class TrooperPanel extends TUIFormPanel {

	public TrooperPanel() {
		addInputComponent(TUIUtils.getNumericTextField("table.buyIn", null, 5, "#.00"), true, true);
		addInputComponent(TUIUtils.getNumericTextField("table.bigBlid", null, 5, "#.00"), true, true);
		addInputComponent(TUIUtils.getNumericTextField("table.smallBlid", null, 5, "#.00"), true, true);
		addInputComponent(TUIUtils.getTWebComboBox("preflopStrategy", "table.strategy"));
		addInputComponent(TUIUtils.getTWebComboBox("minHandPotential", "handRanks"));
		addInputComponent(TUIUtils.getTWebComboBox("oddCalculation", "oddMethod"));
		addInputComponent(TUIUtils.getNumericTextField("preflopRekonAmmo.base", null, 5, null), true, true);
		addInputComponent(TUIUtils.getNumericTextField("preflopRekonAmmo.hand", null, 5, null), true, true);

		addInputComponent(TUIUtils.getWebFormattedTextField("test", null, 20, "##+##*preflop"), true, true);

		FormLayout layout = new FormLayout("right:pref, 3dlu, left:pref, 3dlu, left:pref, 3dlu, left:pref", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.appendSeparator("Table parameters");
		builder.nextLine();
		builder.append(TStringUtils.getString("table.parameters"));
		builder.append(getInputComponent("table.buyIn"));
		builder.append(getInputComponent("table.bigBlid"));
		builder.append(getInputComponent("table.smallBlid"));
		builder.nextLine();

		builder.appendSeparator("Trooper parameters");
		builder.nextLine();
		builder.append(TStringUtils.getString("preflopStrategy"), getInputComponent("preflopStrategy"), 5);
		builder.append(TStringUtils.getString("minHandPotential"), getInputComponent("minHandPotential"), 5);
		builder.append(TStringUtils.getString("oddCalculation"), getInputComponent("oddCalculation"), 5);
		builder.nextLine();

		builder.appendSeparator("pre flop rekon parameteres");
		builder.nextLine();
		builder.append("maxRekonAmmo = ");
		builder.append(getInputComponent("preflopRekonAmmo.base"));
		builder.append(" + ");
		builder.append(getInputComponent("preflopRekonAmmo.hand"));
		builder.append("pre flop Function", getInputComponent("test"), 5);

		setToolBar("runTrooper", "testTrooper", "stopTrooper", "pauseTrooper");
		setBodyComponent(builder.getPanel());
		registreSettings();
		
	}
	@Override
	public Hashtable<String, Object> getValues() {
		Hashtable<String, Object> ht = super.getValues();
		System.out.println("TrooperPanel.getValues()"+ht.get("test"));
		return ht;
	}
}
