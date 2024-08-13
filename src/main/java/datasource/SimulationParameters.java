package datasource;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
public class SimulationParameters extends Model {

	public static SimulationParameters getSimulationParameters() {
		return SimulationParameters.findById(1);
	}

	public String[] getVariables() {
		String[] variables = getString("simulationVariable").split(",");
		return variables;
	}

	public int getVariablesToSimulate() {
		return getVariables().length;
	}

	public boolean isSingleVariable() {
		return getVariablesToSimulate() == 1;
	}

	public void cleanSimulation() {
		SimulationResult.delete("simulation_parameter_id = ?", getId());
	}
}
