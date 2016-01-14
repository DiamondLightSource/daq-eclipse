package org.eclipse.scanning.api.points;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Multiple values to the position, backed by a Map
 * @author Matthew Gerring
 *
 */
public class MapPosition extends AbstractPosition {
	
	private Map<String, Object>  values;
	private Map<String, Integer> indices;
	
	public MapPosition() {
		values  = new LinkedHashMap<String, Object>(7);
		indices = new LinkedHashMap<String, Integer>(7);
	}

	public MapPosition(Map<String, Object> map) {
		values = map;
		indices = new LinkedHashMap<String, Integer>(7);
	}
	
	public MapPosition(String name, Integer index, Object value) {
		this();
		values.put(name, value);
		indices.put(name, index);
	}

	/**
	 * Define the values as a comma separate list of values of the form:
	 * name1:position1:value1, name2:position2:value2, ... etc. namen:valuen
	 * 
	 * The value string must parse as a double or a NumberFormatException is thrown
	 * 
	 * Spaces between commas and between names and values will be trimed
	 * 
	 * @param value
	 */
	public MapPosition(String value) throws NumberFormatException {
		
		values  = new LinkedHashMap<String, Object>(7);
		indices = new LinkedHashMap<String, Integer>(7);
		
		String[] pairs = value.split(",");
		for (String pair : pairs) {
			String[] nv = pair.trim().split("\\:");
		    indices.put(nv[0].trim(), Integer.parseInt(nv[1].trim()));
		    values.put(nv[0].trim(), Double.parseDouble(nv[2].trim()));
		}
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

	@Override
	public int getIndex(String name) {
		if (indices==null)              return -1;
		if (!indices.containsKey(name)) return -1; // Autoboxing protection.
		return indices.get(name);
	}

	public Object putIndex(String key, Integer value) {
		if (indices==null) indices = new LinkedHashMap<String, Integer>(7);
		return indices.put(key, value);
	}
	
	public void putAllIndices(IPosition pos) {
		if (indices==null) indices = new LinkedHashMap<String, Integer>(7);
		final List<String> names = pos.getNames();
		for (String name : names) {
			indices.put(name, pos.getIndex(name));
		}
	}
	@Override
	public Map<String, Integer> getIndices() {
		return indices;
	}

}
