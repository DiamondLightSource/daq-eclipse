package org.eclipse.scanning.api.points;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A position with no value. Can be used in a scan to expose detectors where we do not
 * wish to move any scannables.
 * 
 * @author Matthew Gerring
 *
 */
public class StaticPosition extends AbstractPosition {

	private static final long serialVersionUID = 8325962136123756800L;
	
	@Override
	public int size() {
		return 0;
	}

	@Override
	public List<String> getNames() {
		return Collections.emptyList();
	}

	@Override
	public Object get(String name) {
		return null;
	}

	@Override
	public int getIndex(String name) {
		return 0;
	}

	@Override
	public List<Collection<String>> getDimensionNames() {
		return Collections.emptyList();
	}

	@Override
	public int getScanRank() {
		return 0;
	}

}
