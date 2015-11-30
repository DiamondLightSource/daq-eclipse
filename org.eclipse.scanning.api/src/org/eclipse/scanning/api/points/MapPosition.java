package org.eclipse.scanning.api.points;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Multiple values to the position, backed by a Map
 * @author fcp94556
 *
 */
public class MapPosition extends AbstractPosition {
	
	private Map<String, Object> values;
	
	public MapPosition() {
		values = new LinkedHashMap<String, Object>(7);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public List<String> getNames() {
		return new ArrayList<String>(values.keySet());
	}

	@Override
	public Object get(String name) {
		return values.get(name);
	}

	public Object put(String key, Object value) {
		return values.put(key, value);
	}
	
	public void putAll(IPosition pos) {
		final List<String> names = pos.getNames();
		for (String name : names) {
			values.put(name, pos.get(name));
		}
	}

}
