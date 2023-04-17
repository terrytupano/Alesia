package datasource;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
@Table("GAMES")
@CompositePK({"TABLEPARAMS", "NAME" })
public class Game extends Model {

}
