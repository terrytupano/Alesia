package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
@Table("GAMES")
@CompositePK({"SESSION", "TABLEPARAMS", "NAME" })
public class Game extends Model {

}
