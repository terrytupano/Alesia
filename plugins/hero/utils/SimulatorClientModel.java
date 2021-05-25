package plugins.hero.utils;

import java.util.*;

import org.javalite.activejdbc.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import core.datasource.model.*;
import gui.*;

public class SimulatorClientModel extends TUIFormPanel {

	public SimulatorClientModel(SimulatorClient model, boolean newr) {
		setModel(model);
		Map<String, ColumnMetadata> columns = SimulatorClient.getMetaModel().getColumnMetadata();
		addInputComponent(TUIUtils.getWebTextField("name", model, columns), true, true);
		addInputComponent(TUIUtils.getTWebComboBox("client", model, "botClient"), false, true);
		addInputComponent(TUIUtils.getJCheckBox("isActive", model), false, true);
		addInputComponent(TUIUtils.getNumericTextField("aggression", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("tightness", model, columns), false, true);

		FormLayout layout = new FormLayout("left:pref, 3dlu, left:pref, 7dlu, left:pref, 3dlu, left:pref", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG);
		builder.append(getLabel("name"), getInputComponent("name"));
		builder.append(getLabel("client"), getInputComponent("client"));
		builder.append(getLabel("aggression"), getInputComponent("aggression"));
		builder.append(getLabel("tightness"), getInputComponent("tightness"));
		builder.append(getInputComponent("isActive"), 7);
		setBodyComponent(builder.build());
		setFooterActions("acept", "cancel");
		preValidate();
	}
}
