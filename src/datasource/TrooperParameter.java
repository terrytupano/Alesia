package datasource;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
@CompositePK({"trooper"})
public class TrooperParameter extends Model {

}
