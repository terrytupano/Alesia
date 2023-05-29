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

package hero.ozsoft.bots;

import java.util.*;

import hero.ozsoft.*;
import hero.ozsoft.actions.*;

/**
 * Dummy Texas Hold'em poker bot that perform a random action from the available
 * actions
 *
 * 
 */
public class RandomBot extends Bot {

	private Random random = new Random();

	@Override
	public PlayerAction act(int minBet, int currentBet, Set<PlayerAction> allowedActions) {
		int act = random.nextInt(allowedActions.size());
		return allowedActions.toArray(new PlayerAction[0])[act];
	}

	@Override
	public void handStarted(Player dealer) {

	}
}
