package org.eclipse.scanning.api.points;

import java.util.List;

/**
 * 
 * A poisition with no value
 * 
 * @author Matthew Gerring
 *
 */
public class EmptyPosition extends AbstractPosition implements IPosition {

	@Override
	public int size() {
		return 0;
	}

	@Override
	public List<String> getNames() {
		return null;
	}

	@Override
	public Object get(String name) {
		return null;
	}

	@Override
	public int getIndex(String name) {
		return 0;
	}

}
