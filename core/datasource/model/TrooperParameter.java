package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
//@Table("simulatorClients")
@CompositePK({"trooper"})
public class TrooperParameter extends Model {

}
