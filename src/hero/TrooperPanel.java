package hero;

import java.beans.*;
import java.util.*;

import javax.swing.*;

import org.javalite.activejdbc.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import core.*;
import datasource.*;
import gui.*;

/**
 * Panel with all configuration parameters for the trooper.
 * 
 * @author terry
 * 
 */
public class TrooperPanel extends TUIFormPanel implements PropertyChangeListener {

	public TrooperPanel(TrooperParameter model) {
		addPropertyChangeListener(TActionsFactory.ACTION_PERFORMED, this);
		Map<String, ColumnMetadata> columns = TrooperParameter.getMetaModel().getColumnMetadata();
		setModel(model);

		addInputComponent(TUIUtils.getNumericTextField("tau", model, columns), true, true);
		addInputComponent(TUIUtils.getWebSwitch("strictPreflop", model.getBoolean("strictPreflop")));
		addInputComponent(
				TUIUtils.getTWebComboBox("takeOpportunity", "take.oportutiny", model.getString("takeOpportunity")));
		addInputComponent(TUIUtils.getWebSwitch("ammoFormula", model.getBoolean("ammoFormula")));
		addInputComponent(TUIUtils.getNumericTextField("phi", model, columns), true, true);
		addInputComponent(TUIUtils.getNumericTextField("phi4", model, columns), true, true);

		addInputComponent(TUIUtils.getNumericTextField("reconnBase", model, columns), true, true);
		addInputComponent(TUIUtils.getNumericTextField("reconnBand", model, columns), true, true);

		addInputComponent(TUIUtils.getNumericTextField("playTime", model, columns), false, true);
		addInputComponent(TUIUtils.getNumericTextField("playUntil", model, columns), false, true);

//		JComponent component = TUIUtils.getListItems(getInputComponents());
//		String tit = TStringUtils.getString(fieldName);
//		String msg = TStringUtils.getString(fieldName + ".tt");

		FormLayout layout = new FormLayout("right:pref, 3dlu, left:pref,3dlu, right:pref, 3dlu, left:pref, default:grow", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG).rowGroupingEnabled(true);

		builder.appendSeparator("Battle parameters");
		builder.append(TStringUtils.getString("playTime"), getInputComponent("playTime"));
		builder.append(TStringUtils.getString("playUntil"), getInputComponent("playUntil"));

		builder.appendSeparator("Preflop selection");
		builder.append(TStringUtils.getString("strictPreflop"), getInputComponent("strictPreflop"));
		builder.append(TStringUtils.getString("tau"), getInputComponent("tau"));
		builder.append(TStringUtils.getString("reconnBase"), getInputComponent("reconnBase"));
		builder.append(TStringUtils.getString("reconnBand"), getInputComponent("reconnBand"));

		builder.appendSeparator("Oportunities");
		builder.append(TStringUtils.getString("phi"), getInputComponent("phi"));
		builder.append(TStringUtils.getString("phi4"), getInputComponent("phi4"));
//		builder.nextLine();
		builder.append(TStringUtils.getString("takeOpportunity"), getInputComponent("takeOpportunity"), 5);

		JPanel panel = builder.getPanel();
		setBodyComponent(panel);
		setFooterActions("update");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object act = evt.getNewValue();
		if (TActionsFactory.ACTION_PERFORMED.equals(evt.getPropertyName()) && act != null) {
			getModel().save();
		}
	}
}
