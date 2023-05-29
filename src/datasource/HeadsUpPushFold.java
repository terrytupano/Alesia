package datasource;

import org.javalite.activejdbc.*;
import org.javalite.activejdbc.annotations.*;

import com.jgoodies.common.base.*;

import hero.*;
import hero.UoAHandEval.*;

/**
 * SB (9.0bb): T3s BB (6.0bb): Q2o
 * 
 * The effective stack size is the smaller of the two, before posting any
 * blinds. So the relevant stack size for both players in this hand is 6.0 big
 * blinds.
 * 
 * To find the strategy for SB's T3s, check the green area (suited hands) of the
 * "Pusher" chart. The value for T3s is 7.7bb, and since that is larger than the
 * current effective stacks the hand is a push in the Nash Equilibrium strategy.
 * 
 * To determine if the BB should call with his Q2o, check the orange area
 * (offsuit hands) of the "Caller" chart and locate Q2o. The value there is
 * 5.6bb. The effective stacks in the current hand are larger than the value for
 * Q2o, so this hand is a fold in the Nash Equilibrium strategy.
 */
@DbName("hero")
@Table("heads_up_push_fold")
//@CompositePK({"rangeName", "card" })
public class HeadsUpPushFold extends Model {

	/**
	 * this method return the push factor for hero
	 * 
	 * @param bigBlinds - hero's # of big blind
	 * @param cards     - hero's hand
	 * 
	 * @return the factor
	 */
	public static double getPush(double bigBlinds, UoAHand cards) {
		return getFactor(bigBlinds, cards, "push");
	}

	/**
	 * this method return the call factor for hero to call
	 * 
	 * @param bigBlinds - hero's # of big blind
	 * @param cards     hero's hand
	 * 
	 * @return the factor
	 */
	public static double getCall(double bigBlinds, UoAHand cards) {
		return getFactor(bigBlinds, cards, "call");
	}

	/**
	 * this method return the factor for hero to push or call accordion to hero's
	 * position in the table. if hero is small blind, the action is push. if hero is
	 * big blind, the action must be call
	 * 
	 * @param bigBlinds - hero's # of big blind
	 * @param cards     - hero's hand
	 * @param action    - push or call
	 * 
	 * @return the factor
	 */
	public static double getFactor(double bigBlinds, UoAHand cards, String action) {
		// the table contain data until 200 bb
		Preconditions.checkArgument(bigBlinds >= 1 && bigBlinds <= 200, "the bigBlinds argument can't not be > 200bb.");
		long stack = Math.round(bigBlinds);
		HeadsUpPushFold headsUpPushFold = HeadsUpPushFold.findFirst("stack = ? AND action = ?", stack, action);
		String cards2 = PreflopCardsModel.getStringCard(cards);
		double factor = headsUpPushFold.getDouble(cards2);
		return factor;
	}

}
