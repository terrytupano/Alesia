package datasource;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
public class SimulationParameters extends Model {

	public static SimulationParameters getSimulationParameters() {
		return SimulationParameters.findById(1);
	}
}
