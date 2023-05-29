package datasource;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
public class SimulationParameters extends Model {

	public static SimulationParameters getSimulationParameters() {
		return SimulationParameters.findById(1);
	}

	public boolean isSingleVariable() {
		return getString("simulationVariable").split(",").length == 1;
	}
	
	public void cleanSimulation() {
		SimulationResult.delete("simulation_parameter_id = ?", getId());
	}
}
