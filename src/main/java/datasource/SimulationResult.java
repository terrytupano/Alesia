package datasource;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
public class SimulationResult extends Model {

    public static String ACTIVE = "A";
    public static String RETIRED = "R";
}
