package datasource;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
//@Table("SIMULATORSTATISTICS")
//@CompositePK({"name", "value"})
public class SimulationResult extends Model {

    public static String ACTIVE = "A";
    public static String RETIRED = "R";
}
