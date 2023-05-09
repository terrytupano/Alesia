package hero;

import java.beans.*;
import java.util.*;

import org.javalite.activejdbc.*;

import com.jgoodies.forms.builder.*;

import core.*;
import datasource.*;
import gui.*;

/**
 * Panel with all configuration parameters for the a simulation.
 * 
 * @author terry
 * 
 */
public class SimulationParametersPanel extends TUIFormPanel implements PropertyChangeListener {

	public SimulationParametersPanel() {
		// to today, i need only 1 simulation parameter
		LazyList<SimulationParameters > list = SimulationParameters.findAll();
		SimulationParameters model = list.isEmpty() ? new SimulationParameters() : list.get(0);
		
		addPropertyChangeListener(TActionsFactory.ACTION_PERFORMED, this);
		Map<String, ColumnMetadata> columns = SimulationParameters.getMetaModel().getColumnMetadata();
		setModel(model);

		addInputComponent(TUIUtils.getWebTextField("simulationName", model, columns), true, true);
		addInputComponent(TUIUtils.getWebTextField("simulationVariable", model, columns), true, true);
		addInputComponent(TUIUtils.getNumericTextField("simulationsHands", model, columns), true, true);
		addInputComponent(TUIUtils.getWebSwitch("pauseOnHero", model.getBoolean("pauseOnHero")));
		addInputComponent(TUIUtils.getTWebComboBox("speed", "simulation.speed", model.getString("speed")));
		
		DefaultFormBuilder builder =  TUIUtils.getOneLineFormBuilder();
		
		builder.append(TStringUtils.getString("simulationName"), getInputComponent("simulationName"));
		builder.append(TStringUtils.getString("simulationVariable"), getInputComponent("simulationVariable"));
		builder.append(TStringUtils.getString("simulationsHands"), getInputComponent("simulationsHands"));
		builder.append(TStringUtils.getString("pauseOnHero"), getInputComponent("pauseOnHero"));
		builder.append(TStringUtils.getString("speed"), getInputComponent("speed"));

		setBodyComponent(builder.getPanel());
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
