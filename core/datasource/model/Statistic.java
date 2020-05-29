package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
@Table("STATISTIC")
@CompositePK({"SESSION", "TABLEPARAMS", "STREET", "NAME"})
public class Statistic extends Model {

}
