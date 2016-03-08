package org.eclipse.scanning.api.scan.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * An object of this class contains the shape and axis names
 * required to configure and populate a dataset for a scannable or detector,
 * e.g. for writing a NeXus file 
 */
public class AxisModel extends ScannableModel {

	// TODO: merge this with ScannableModel?
	
	private LinkedHashMap<String, Integer> dimensions = new LinkedHashMap<>();
	
	public AxisModel() {
	}
	
	/**
	 * Adds a dimension to this
	 * @param name
	 * @param size
	 * @return
	 */
	public AxisModel addDimension(String name, int size) {
		dimensions.put(name, size);
		return this;
	}
	
	/**
	 * Returns the shape of this axis model
	 * @return
	 */
	public int[] getShape() {
		final int[] shape = new int[dimensions.size()];
		int i = 0;
		for (Integer dimSize : dimensions.values()) {
			shape[i] = dimSize;
			i++;
		}
		return shape;
	}
	
	/**
	 * Returns the size of the dimension with the given name
	 * @param axisName name of axis
	 * @return size of dimension with given name
	 */
	public int getDimensionSize(String axisName) {
		return dimensions.get(axisName);
	}
	
	/**
	 * Returns the axis names for this model.
	 * @return axis names
	 */
	public List<String> getAxisNames() {
		return new ArrayList<String>(dimensions.keySet()); // TODO cache for efficiency?
	}
	
	public void setRank() {
		throw new UnsupportedOperationException("");
	}
	
	/**
	 * Returns the rank of this axis model.
	 * @return rank
	 */
	public int getRank() {
		return dimensions.size();
	}
	

}
