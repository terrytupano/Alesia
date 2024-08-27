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

		boolean isHero = model.isHero();

		addInputComponent(TUIUtils.getWebTextField("trooper", model, columns), true, !isHero);
		addInputComponent(TUIUtils.getWebTextField("description", model, columns), true, true);

		// only for hero
		if (isHero) {
			addInputComponent(TUIUtils.getNumericTextField("playTime", model, columns), false, true);
			addInputComponent(TUIUtils.getNumericTextField("playUntil", model, columns), false, true);
		}

		addInputComponent(TUIUtils.getNumericTextField("alpha", model, columns), true, true);
		addInputComponent(TUIUtils.getNumericTextField("tau", model, columns), true, true);

		setTitleDescriptionFrom("trooper", "description");

		JComponent component = TUIUtils.getFormListItems(getInputComponents());

		// String tit = TStringUtils.getString(fieldName);
		// String msg = TStringUtils.getString(fieldName + ".tt");

		FormLayout layout = new FormLayout(
				"right:pref, 3dlu, left:pref,3dlu, right:pref, 3dlu, left:pref, default:grow", "");
		DefaultFormBuilder builder = new DefaultFormBuilder(layout).border(Borders.DIALOG).rowGroupingEnabled(true);

		// builder.appendSeparator("Battle parameters");
		// builder.append(TStringUtils.getString("playTime"),
		// getInputComponent("playTime"));
		// builder.append(TStringUtils.getString("playUntil"),
		// getInputComponent("playUntil"));

		builder.appendSeparator("Preflop selection");
		builder.append(TStringUtils.getString("alpha"), getInputComponent("alpha"));
		builder.append(TStringUtils.getString("tau"), getInputComponent("tau"));

		JPanel panel = builder.getPanel();
		setBodyComponent(component);
		addFooterActions("update");
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Object act = evt.getNewValue();
		if (TActionsFactory.ACTION_PERFORMED.equals(evt.getPropertyName()) && act != null) {
			getModel().save();
		}
	}
}
