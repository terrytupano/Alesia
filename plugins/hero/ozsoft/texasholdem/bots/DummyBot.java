// This file is part of the 'texasholdem' project, an open source
// Texas Hold'em poker application written in Java.
//
// Copyright 2009 Oscar Stigter
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package plugins.hero.ozsoft.texasholdem.bots;

import java.util.*;

import plugins.hero.ozsoft.texasholdem.*;
import plugins.hero.ozsoft.texasholdem.actions.*;

/**
 * Dummy Texas Hold'em poker bot that always just checks or calls. <br />
 * <br />
 * 
 * This bot allowed for perfectly predictable behavior.
 * 
 * @author Oscar Stigter
 */
public class DummyBot extends Bot {

	public DummyBot() {

	}
	@Override
	public void messageReceived(String message) {
		// Not implemented.
	}

	@Override
	public void joinedTable(TableType type, int bigBlind, List<Player> players) {
		// Not implemented.
	}

	@Override
	public void handStarted(Player dealer) {
		// Not implemented.
	}

	@Override
	public void actorRotated(Player actor) {
		// Not implemented.
	}

	@Override
	public void playerUpdated(Player player) {
		// Not implemented.
	}

	@Override
	public void boardUpdated(List<Card> cards, int bet, int pot) {
		// Not implemented.
	}

	@Override
	public void playerActed(Player player) {
		// Not implemented.
	}

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		if (allowedActions.contains(PlayerAction.CHECK)) {
			return PlayerAction.CHECK;
		} else {
			return PlayerAction.CALL;
		}
	}

}
