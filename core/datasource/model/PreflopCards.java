package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
@Table("PREFLOPCARDS")
@CompositePK({"rangeName", "card" })
public class PreflopCards extends Model {

}
