package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
@Table("SIMULATORSTATISTICS")
@CompositePK({"name", "value"})
public class SimulatorStatistic extends Model {

}
