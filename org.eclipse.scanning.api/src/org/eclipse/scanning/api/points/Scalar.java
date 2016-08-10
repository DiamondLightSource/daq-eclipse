package org.eclipse.scanning.api.points;

import java.util.Arrays;
import java.util.List;

/**
 * A single value position.
 * 
 * @author Matthew Gerring
 *
 */
public class Scalar extends AbstractPosition {
	
	private String name;
	private double value;
	private int    index;
	
	public Scalar(String name, int index, double value) {
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

	public double getValue() {
		return value;
	}
	
	@Override
	public int getIndex(String name) {
		return name.equals(this.name) ? index : -1;
	}

}
