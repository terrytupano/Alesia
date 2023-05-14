package core;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.text.*;

import org.javalite.activejdbc.*;

import com.alee.api.data.*;
import com.alee.extended.button.*;
import com.alee.extended.date.*;
import com.alee.extended.overlay.*;
import com.alee.laf.label.*;
import com.alee.managers.settings.*;
import com.alee.managers.settings.Configuration;
import com.alee.managers.tooltip.*;
import com.jgoodies.common.base.*;
import com.jgoodies.validation.*;
import com.jgoodies.validation.view.*;

import gui.*;
import gui.wlaf.*;

/**
 * notes: the method {@link #getValue(String)} and {@link #getValues()} method
 * only will return the values for components inserted in this calss using
 * {@link #addInputComponent(JComponent, boolean, boolean)} methods. this allow
 * sublcass manualy append more ui component that are no related whit this form.
 * 
 * @author terry
 *
 */
public class TUIFormPanel extends TUIPanel {

	public static final String ACCEPTCHANGES = "acceptChanges";
	public static final String CANCELCHANGES = "cancelChanges";
	public static final String UPDATECHANGES = "updateChanges";

	private static final String MY_LABEL = "myLabel";
	private static final String MY_WEB_OVERLAY = "myWebOverlay";

	private TValidationMessage mandatory, listEmpty;
	private HashMap<String, JComponent> fieldComponetMap;
	private ValidationResult validationResult;
	private Model model;
	private HashMap<String, Object> temporalStorage;
	private List<String> componentsAsInserted;

	public TUIFormPanel() {
		this.componentsAsInserted = new ArrayList<>();
		this.fieldComponetMap = new HashMap<String, JComponent>();
		this.temporalStorage = new HashMap<>();
		this.validationResult = new ValidationResult();
		this.mandatory = new TValidationMessage("validationMessage.mandatory");
		this.listEmpty = new TValidationMessage("validationMessage.listEmpty");
	}

	protected void addInputComponent(JComponent cmp) {
		addInputComponent(cmp, false, true);
	}

	public void addInputComponent(JComponent component, boolean required, boolean enable) {
		String name = component.getName();
		Preconditions.checkNotNull(name, "The component hast no name.");
		addInputComponent(name, component, required, enable);
	}

	protected void addInputComponent(String fieldName, JComponent component, boolean required, boolean enable) {
		fieldComponetMap.put(fieldName, component);
		componentsAsInserted.add(fieldName);
		JLabel jl = TUIUtils.getLabel(fieldName, required, enable);
		WebOverlay overlay = new WebOverlay(component);
		component.putClientProperty(MY_LABEL, jl);
		component.putClientProperty("isRequired", required);
		component.putClientProperty(MY_WEB_OVERLAY, overlay);
		setEnable(fieldName, enable);
		ValidationComponentUtils.setMandatory(component, required);
	}

	public void checkComboBoxes() {
		for (JComponent jcmp : fieldComponetMap.values()) {
			if (jcmp instanceof JComboBox) {
				JComboBox<?> comboBox = (JComboBox<?>) jcmp;
				if (comboBox.getItemCount() == 0) {
					setValidationMessage(comboBox, listEmpty);
				}
			}
		}
	}

	private void checkDateFields() {
		List<JComponent> jcmplist = new ArrayList<>(fieldComponetMap.values());
		for (JComponent jcmp : jcmplist) {
			if (jcmp instanceof WebDateField && jcmp.isEnabled()) {
				WebDateField wdf = (WebDateField) jcmp;
				Color bgc = UIManager.getColor("TextField.background");
				wdf.setBackground(bgc);
				// Date dat = wdf.getDate();
				// boolean req = (Boolean) jcmp.getClientProperty("isRequired");
				// boolean de = false;
				// requerired and parse errror or parse error and not blank
				// if ((req && de) || (de && !ds.equals(""))) {
				// showAplicationException(msg18);
				// wdf.setBackground(msg18.getErrorColor());
				// }
			}
		}
	}

	private void checkExtendedJLabel() {
		// List<JComponent> jcmplist = new List(fields.values());
		// for (JComponent jcmp : jcmplist) {
		// JComponent icmp = getInternal(jcmp);
		// boolean req = (Boolean) jcmp.getClientProperty("isRequired");
		// if (icmp instanceof ExtendedJLabel && req) {
		// if (!((ExtendedJLabel) icmp).isValueSet()) {
		// reasons.add(msg17);
		// }
		// }
		// }
	}

	public void checkTextComponent() {
		for (JComponent jcmp : fieldComponetMap.values()) {
			if (jcmp.isEnabled() && jcmp instanceof JTextComponent) {
				JTextComponent textComponent = (JTextComponent) jcmp;
				if (ValidationComponentUtils.isMandatoryAndBlank(textComponent)) {
					setValidationMessage(textComponent, mandatory);
				}
			}
		}
	}

	/**
	 * return the internal value stored in the {@link JComponent} asociated whit
	 * this field id. This method assume the standar Object <--> Component relation.
	 * for example:
	 * <ul>
	 * <li>Number <--> {@link JFormattedTextField}
	 * <li>Boolean <--> {@link JCheckBox} or {@link JRadioButton}
	 * <li>String <--> {@link JTextArea} or {@link JTextField}
	 * </ul>
	 * 
	 * @param field - internal field id
	 * @return value
	 */
	private Object getFieldValue(String field) {
		JComponent jcmp = fieldComponetMap.get(field);

		if (jcmp instanceof JScrollPane) {
			JViewport jvp = (JViewport) ((JScrollPane) jcmp).getViewport();
			jcmp = (JComponent) jvp.getView();
		}
		Object val = null;

		// date field
		if (jcmp instanceof WebDateField) {
			WebDateField wdf = ((WebDateField) jcmp);
			return wdf.getDate();
		}

		// test: fast replace for jformatetextfield
		if (jcmp instanceof NumericTextField) {
			try {
				val = ((NumericTextField) jcmp).getNumberValue();
				return val;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		// numeros
		if (jcmp instanceof JFormattedTextField) {
			JFormattedTextField jftf = ((JFormattedTextField) jcmp);
			val = jftf.getValue();
			// try {
			// jftf.commitEdit();
			// val = jftf.getValue();
			// } catch (ParseException e) {
			// // TEST: print statck trace
			// e.printStackTrace();
			// }
			return val;
		}
		// texto
		if (jcmp instanceof JTextComponent) {
			val = ((JTextComponent) jcmp).getText();
			return val;
		}
		// 170911: property table
		if (jcmp instanceof TPropertyJTable) {
			val = ((TPropertyJTable) jcmp).getProperties();
			return val;
		}
		// 180212: TWebFileChooserField string representation of File or *none if no
		// file selected
		if (jcmp instanceof TWebFileChooserField) {
			String s = ((TWebFileChooserField) jcmp).getSelectedFile();
			val = s == null ? "*none" : s;
			return val;
		} 
		// selector de registros
		// if (jcmp instanceof AssistedJTextField) {

		// }
		// html text
		// if (jcmp instanceof HTMLEditor) {
		// val = ((HTMLEditor) jcmp).getText();
		// return val;
		// }

		// boolean (WebSwitch)
		if (jcmp instanceof WebSwitch) {
			val = ((WebSwitch) jcmp).isSelected();
			return val;
		}

		// JSpinner only for integer values
		if (jcmp instanceof JSpinner) {
			val = (Integer) ((JSpinner) jcmp).getValue();
			return val;
		}

		// boolean
		if (jcmp instanceof JCheckBox) {
			val = ((JCheckBox) jcmp).isSelected();
			return val;
		}
		if (jcmp instanceof JRadioButton) {
			val = ((JRadioButton) jcmp).isSelected();
			return val;
		}
		// campo solo de salida. si el valor es instancia de LTEntry, retorna clave
		// if (jcmp instanceof ExtendedJLabel) {
		// val = ((ExtendedJLabel) jcmp).getValue();
		// if (val instanceof TEntry) {
		// val = ((TEntry) val).getKey();
		// }
		// return val;
		// }

		// key element in JComboBox Tentry list.
		if (jcmp instanceof JComboBox) {
			Object obj = ((JComboBox<?>) jcmp).getSelectedItem();
			if (obj instanceof TSEntry) {
				val = ((TSEntry) obj).getKey();
			} else {
				throw new NullPointerException("JCombobox value is not instance of TEntry" + field);
			}
			return val;
		}

		// Nothing found
		throw new NullPointerException("No value fount for field " + field);
	}

	/**
	 * return the {@link WebOverlay} associated with the input component ....
	 * 
	 * @param field - name of the component
	 * 
	 * @return {@link WebOverlay}
	 */
	public WebOverlay getInputComponent(String field) {
		JComponent jcmp = fieldComponetMap.get(field);
		Preconditions.checkNotNull(jcmp, "Component identified as %s was not found.", field);
		WebOverlay overlay = (WebOverlay) jcmp.getClientProperty(MY_WEB_OVERLAY);
		return overlay;
	}

	/**
	 * set the title/description for this panel based of the value retrieved from
	 * the value of the model.
	 * 
	 * @param titleFieldName       - title field value
	 * @param descriptionFieldName - description field value
	 */
	public void setTitleDescriptionFrom(String titleFieldName, String descriptionFieldName) {
		String titleString = model.getString(titleFieldName);
		String descriptionString = descriptionFieldName == null ? null : model.getString(descriptionFieldName);
		setTitleDescription(titleString, descriptionString);
	}

	/**
	 * return a list with all input components as those were inserted during
	 * {@link #addInputComponent(JComponent, boolean, boolean)} phase
	 * 
	 * @return - list of input components
	 */
	public List<JComponent> getInputComponents() {
		List<JComponent> components = new ArrayList<>();
		for (String cName : componentsAsInserted) {
			components.add(fieldComponetMap.get(cName));
		}
		return components;
	}

	public JLabel getLabel(String field) {
		JComponent jcmp = fieldComponetMap.get(field);
		Preconditions.checkNotNull(jcmp, "Component identified as %1$ not found.", field);
		return (JLabel) jcmp.getClientProperty(MY_LABEL);
	}

	/**
	 * return the instance of the {@link Model} set whit all values introduced as
	 * input in this component. The model must be set initially using the method
	 * {@link #setModel(Model)}. All field values will be copied as are in the input
	 * window. Only attributes present in the input window will be copied to the
	 * model.
	 * 
	 * @return the Model with all attributes set.
	 */
	public Model getModel() {
		if (model == null)
			return null;
		Map<String, Object> vals = getValues();
		model.fromMap(vals);
		return model;
	}

	/**
	 * return the internal fields. This list is for store purpose. Some
	 * implementation of this class needs to store parameters that has no visual
	 * component associated with, but will be needed futher.
	 * 
	 * @see #getValues()
	 */
	public Map<String, Object> getTemporalStorage() {
		return temporalStorage;
	}

	/**
	 * Return a new {@link Hashtable} with all values setted by this GUI. this
	 * method also will return all stored parameters in the temporal storage buffer
	 * for this class. Fields whit values <code>null</code> will not be returned
	 * 
	 * @return Hashtable with fields name and values found in this UI
	 * @see #getTemporalStorage()
	 */
	public Map<String, Object> getValues() {
		HashMap<String, Object> map = new HashMap<>(temporalStorage);
		for (String field : fieldComponetMap.keySet()) {
			Object val = getFieldValue(field);
			map.put(field, val);
		}
		return map;
	}

	/**
	 * Enable commint actions. Commit action are actions with the property
	 * <code>.isCommint = true</code> as property (for example, see Acept)
	 * 
	 * @param enable - true of false for enable/disable action public void
	 *               setEnabledCommintActions(boolean enable) { for (Action a :
	 *               allActions) { ApplicationAction aa = (ApplicationAction) a;
	 *               String isc = aa.getResourceMap().getString(aa.getName() +
	 *               ".Action.isCommint"); if (isc != null && isc.equals("true")) {
	 *               aa.setEnabled(enable); } } // actions.stream().filter((a) ->
	 *               a.getValue("isCommint").equals("true")).forEach(a ->
	 *               a.setEnabled(enable)); }
	 */

	/**
	 * Enable/disable <code>Action.scope = element</code> actions. "element" actions
	 * are action than act over an element of a list. (for example, editModelAction)
	 * those action must be enabled/disables if the user select or not an elemento
	 * form the list.
	 * 
	 * @param enable - true of false for enable/disable action public void
	 *               setEnabledElementActions(boolean enable) {
	 *               setEnableActions("scope", "element", enable); }
	 */

	/**
	 * check all input components inside this form checking form input error. if
	 * any, the {@link #validationResult} contain all error messages. if this method
	 * return <code>false</code>, the input information is not consistent and should
	 * not be saved.
	 * 
	 * @return <code>true</code> OK. <code>false</code> the information should not
	 *         be saved.
	 */
	public boolean validateFields() {
		validationResult = new ValidationResult();

		setEnableActions("isCommint", "true", false);

		checkComboBoxes();
		checkTextComponent();
		checkDateFields();
		checkExtendedJLabel();

		// 180212: lazy implemetation of filechooser
		// TODO: complete implementation. code copied from oll framework

		// if (chooserField != null && chooserField.isEnabled()) {
		// boolean req = ((Boolean)
		// component_isRequired.get(chooserField)).booleanValue();
		// String sf = chooserField.getSelectedFile();
		// if (req && sf == null) {
		// showAplicationExceptionMsg("ui.msg22");
		// return;
		// }
		// }

		// error found?
		if (!validationResult.isEmpty()) {
			return false;
		}

		// all ok
		setEnableActions("isCommint", "true", true);
		return true;
	}

	/**
	 * shortcut to save the current model. this method perform
	 * {@link #validateFields()} and save the model if all is ok. if not, this
	 * method show a notification indicating something is wrong.
	 * 
	 * @return <code>true</code> model saved, <code>false</code> no ok.
	 */
	public boolean save() {
		boolean save = validateFields();
		if (save)
			getModel().save();
		else
			Alesia.showNotification("validationMessage.updateError");
		return save;
	}

	/**
	 * Registers all input component for settings auto-save. All WebComponent that
	 * Support {@link SettingsMethods} will be registered. the client property
	 * <code>settingsProcessor</code> must be setter whit a valid instance of
	 * {@link Configuration}.
	 * 
	 * <p>
	 * To get a valid web component whit the correct configuration, use any of the
	 * factory methods in {@link TUIUtils}
	 * 
	 * @see SettingsMethods#registerSettings(Configuration)
	 */
	public void registreSettings() {
		Collection<JComponent> cmps = fieldComponetMap.values();
		ArrayList<String> mths = new ArrayList<>();
		for (JComponent cmp : cmps) {
			mths.add(cmp.getName());
		}
		registreSettings(mths.toArray(new String[0]));
	}

	/**
	 * Registers the given list of input component for settings auto-save.
	 * 
	 * @param names - list of components
	 * @see SettingsMethods#registerSettings(Configuration)
	 * @see #registreSettings()
	 */
	public void registreSettings(String... names) {
		for (String name : names) {
			JComponent cmp = fieldComponetMap.get(name);
			if (cmp instanceof SettingsMethods) {
				Configuration<?> cnf = (Configuration<?>) cmp.getClientProperty("settingsProcessor");
				((SettingsMethods) cmp).registerSettings(cnf);
			}
		}
	}

	/**
	 * the the enable/disable status for the couple {@link JLabel} and
	 * {@link JComponent} asociated whit this field name.
	 * 
	 * @param field  - field name of the input component
	 * @param enable - enable/disable value
	 */
	public void setEnable(String field, boolean enable) {
		JComponent jcmp = fieldComponetMap.get(field);
		Preconditions.checkNotNull(jcmp, "Component identified as %1$ not found.", field);
		JLabel jl = (JLabel) jcmp.getClientProperty(MY_LABEL);
		jl.setEnabled(enable);
		jcmp.setEnabled(enable);
	}

	/**
	 * set the {@link Model} for this component if this component will be used as
	 * Data base CRUD operations
	 * 
	 * @param model - the model
	 */
	public void setModel(Model model) {
		this.model = model;
	}

	private void setValidationMessage(JComponent component, TValidationMessage message) {
		validationResult.add(message);
		WebLabel overlayLabel = new WebLabel(message.getIcon());
		overlayLabel.setToolTip(message.formattedText(), TooltipWay.left);
		WebOverlay overlay = (WebOverlay) component.getClientProperty(MY_WEB_OVERLAY);
		overlay.addOverlay(
				new AlignedOverlay(overlayLabel, BoxOrientation.right, BoxOrientation.top, new Insets(0, 0, 0, 3)));
		message.playSound();
	}
}
