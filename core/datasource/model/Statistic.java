package core.datasource.model;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("AlesiaDatabase")
@Table("STATISTIC")
@CompositePK({"TABLEPARAMS", "STREET", "NAME"})
public class Statistic extends Model {

}
