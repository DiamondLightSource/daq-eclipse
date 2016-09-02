package org.eclipse.scanning.api.points;

import java.util.Arrays;
import java.util.List;

/**
 * A single value position. Instances of this class are immutable.
 * 
 * @param <T> the type of the value of this position 
 * @author Matthew Gerring
 */
public class Scalar<T> extends AbstractPosition {
	
	private static final long serialVersionUID = 9078732007535500363L;
	
	private final String name;
	private final int index;
	private final T value;
	
	public Scalar(String name, int index, T value) {
		this.name  = name;
		this.index = index;
		this.value = value;
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public List<String> getNames() {
  	    return Arrays.asList(new String[]{name});
	}

	@Override
	public Object get(String name) {
		return name.equals(this.name) ? value : null;
	}

	public T getValue() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	public Class<T> getType() {
		return (Class<T>) value.getClass();
	}
	
	@Override
	public int getIndex(String name) {
		return name.equals(this.name) ? index : -1;
	}

}
