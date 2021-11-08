package core.datasource.model;

import java.util.*;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
@Table("simulatorClients")
@CompositePK({"playerName"})
public class SimulatorClient extends Model {

	public Map<String, Object> getAttributes2() {
		return super.getAttributes();
	}
}
