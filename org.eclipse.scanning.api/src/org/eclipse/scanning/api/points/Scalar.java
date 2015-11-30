package org.eclipse.scanning.api.points;

import java.util.Arrays;
import java.util.List;

/**
 * A single value position.
 * 
 * @author fcp94556
 *
 */
public class Scalar implements IPosition {
	
	private String name;
	private double value;
	
	public Scalar(String name, double value) {
		this.name  = name;
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

}
