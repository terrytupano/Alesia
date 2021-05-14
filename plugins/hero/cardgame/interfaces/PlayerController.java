/*
 * Copyright (c) Chris 'MD87' Smith, 2007-2008. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.cardgame.interfaces;

import plugins.hero.cardgame.*;
import plugins.hero.cardgame.Player.*;

/**
 * Defines the standard methods required for a player controller.
 * 
 * @author Chris
 */
public interface PlayerController {
   
    /**
     * Determine whether the player will call, raise or fold.
     * 
     * @param callAmount The amount the player needs to put in the pot to call
     * @param canRaise Whether the player can raise or not
     * @return The player's selected action
     */
    CallRaiseFold doCallRaiseFold(final int callAmount, boolean canRaise);
    
    /**
     * Determine whether the player will open or check.
     * 
     * @return The player's selected action
     */
    OpenCheck doOpenCheck();
    
    /**
     * Indicates whether this player's cards should be shown locally.
     * 
     * @return True if the cards should be shown, false otherwise
     */
    boolean shouldShowCards();
    
    /**
     * Sets the player that this controller is controlling.
     * 
     * @param player The player that this controller is controlling
     */
    void setPlayer(final Player player);
    
    /**
     * Sets the game that the player is playing in.
     * 
     * @param game The game that is being played
     */
    void setGame(final Game game);
    
    /**
     * Determine the amount that the player wishes to open or raise by.
     * 
     * @param minimum The minimum raise or opening amount
     * @return The player's chosen amount
     */
    int getRaise(final int minimum);
    
    /**
     * Determines if this controller represents a local human or not.
     * 
     * @return True iff the controller represents a local human
     */
    boolean isLocalHuman();
    
    /**
     * Determines which cards (if any) this controller wishes to discard.
     * 
     * @param minimum The minimum number of cards to be discarded
     * @param maximum The maximum number of cards to be discarded
     * @return A deck containing the cards that are being discarded
     */
    Deck discardCards(final int minimum, final int maximum);

}
