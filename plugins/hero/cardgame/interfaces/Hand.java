/*
 * Copyright (c) Chris 'MD87' Smith, 2007. All rights reserved.
 *
 * This code may not be redistributed without prior permission from the
 * aforementioned copyright holder(s).
 */

package plugins.hero.cardgame.interfaces;

import plugins.hero.cardgame.*;

/**
 *
 * @author Chris
 */
public interface Hand extends Comparable<Hand> {
    
    String getFriendlyName();

    Deck getDeck();
    
}
