package datasource;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

@DbName("hero")
@CompositePK({ "trooper" })
public class TrooperParameter extends Model {

	public static TrooperParameter getHero() {
		return TrooperParameter.findByCompositeKeys("Hero");
	}
	
	public boolean isHero() {
		return "Hero".equals(getString("trooper"));
	}
}
