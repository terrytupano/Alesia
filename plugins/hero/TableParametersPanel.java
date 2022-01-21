package plugins.hero;

import java.util.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import gui.*;

public class TableParametersPanel extends TUIFormPanel {

	public TableParametersPanel() {
		addInputComponent(TUIUtils.getNumericTextField("play.time", null, 5, null), false, true);
		addInputComponent(TUIUtils.getNumericTextField("play.until", null, 5, null), false, true);
		addInputComponent(TUIUtils.getTWebComboBox("table.parameters", "table.parameters0"));

		FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);

		builder.append(TStringUtils.getString("play.time"), getInputComponent("play.time"));
		builder.append(TStringUtils.getString("play.until"), getInputComponent("play.until"));
		builder.nextLine();
		builder.append(TStringUtils.getString("table.parameters"), getInputComponent("table.parameters"), 5);
		builder.nextLine();

		setBodyComponent(builder.getPanel());
		registreSettings("play.time", "play.until", "table.parameters");
	}

	@Override
	public Map<String, Object> getValues() {
		Map values = super.getValues();
		
		// parse parameters
		String[] tparms = values.get("table.parameters").toString().split("[,]");
		values.put("table.buyIn", new Double(tparms[0]));
		values.put("table.bigBlid", new Double(tparms[1]));
		values.put("table.smallBlid", new Double(tparms[2]));
		// simbol if its present of "" if not
		values.put("table.currency", tparms.length > 3 ? tparms[3] : "");

		return values;
	}
}
