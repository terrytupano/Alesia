package burlap.part3;

import java.util.*;

import burlap.mdp.core.state.*;
import burlap.mdp.core.state.annotations.*;

@DeepCopyState
public class EXGridState implements MutableState {

	public int x;
	public int y;
	
	private final static List<Object> keys = Arrays.asList(ExampleGridWorld.VAR_X, ExampleGridWorld.VAR_Y);
	public EXGridState(int x, int y) {
		this.x = x;
		this.y = y;
	}
	@Override
	public List<Object> variableKeys() {
		return keys;
	}

	@Override
	public Object get(Object variableKey) {
		if (variableKey.equals(ExampleGridWorld.VAR_X))
			return x;
		if (variableKey.equals(ExampleGridWorld.VAR_Y))
			return y;
		throw new UnknownKeyException(variableKey);
	}

	@Override
	public State copy() {
		// because the java data field in this class are data primitives, this methos will return a deep copy. this will
		// not affect the values of this class in future modification of the copy
		return new EXGridState(x, y);
	}

	@Override
	public MutableState set(Object variableKey, Object value) {
		if (keys.contains(variableKey))
			throw new UnknownKeyException(variableKey);

		if (variableKey.equals(ExampleGridWorld	.VAR_X))
			this.x = StateUtilities.stringOrNumber(variableKey).intValue();
		if (variableKey.equals(ExampleGridWorld.VAR_Y))
			this.y = StateUtilities.stringOrNumber(variableKey).intValue();

		return this;
	}

	@Override
	public String toString() {
		return StateUtilities.stateToString(this);
	}
}
