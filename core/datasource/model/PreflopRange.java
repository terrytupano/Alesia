package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
@Table("PREFLOPRANGES")
@CompositePK({"rangeName", "card" })
public class PreflopRange extends Model {

}
