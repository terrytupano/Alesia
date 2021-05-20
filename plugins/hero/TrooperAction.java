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

package plugins.hero;

/**
 * this class encapsule all required values for the trooper to perform an action.
 * 
 */
public class TrooperAction {

	public static final TrooperAction FOLD = new TrooperAction("fold", 0d);
	public static final TrooperAction CHECK = new TrooperAction("call", 0d);

	/**
	 * the name for this action
	 */
	public final String name;
	/**
	 * the amount (cost)
	 */
	public final double amount;
	/**
	 * the string secuence for {@link RobotActuator} class
	 */
	public final String robotCommand;
	/**
	 * cmputed expected value for this action
	 */
	public double expectedValue;

	/**
	 * Constructor. the robot comand for this action will be the name
	 * 
	 * @param name The action's name.
	 * @param amount - the cost for this action.
	 */
	public TrooperAction(String name, double amount) {
		this(name, name, amount);
	}

	/**
	 * Constructor.
	 * 
	 * @param name The action's name.
	 * @param robotCmd The command secuence for the {@link RobotActuator} class
	 * @param amount The action's amount.
	 */
	public TrooperAction(String name, String robotCmd, double amount) {
		this.name = name;
		this.robotCommand = robotCmd;
		this.amount = amount;
	}

	@Override
	public String toString() {
		return String.format("%s %4.2f", name, amount);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TrooperAction))
			return false;
		return ((TrooperAction) obj).toString().equals(toString());
	}
}
