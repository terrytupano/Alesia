package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("flicka")
@Table("flickastat")
@CompositePK({ "stdate", "strace", "stfield", "stdecision" })
public class FlickaStat extends Model {

}
