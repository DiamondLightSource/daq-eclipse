package org.eclipse.scanning.api.points;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.annotation.UiHidden;

public abstract class AbstractPosition implements IPosition {
	
	private int stepIndex = -1;
	protected List<Collection<String>> dimensionNames; // Dimension->Names@dimension
	
	@UiHidden
	public int getStepIndex() {
		return stepIndex;
	}

	public void setStepIndex(int stepIndex) {
		this.stepIndex = stepIndex;
	}

	public final IPosition compound(IPosition parent) {
		if (parent==null) return this; // this+null = this
		final MapPosition ret = new MapPosition();
		ret.putAll(parent);
		ret.putAll(this);
		ret.putAllIndices(parent);
		ret.putAllIndices(this);
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		final Collection<String> names   = getNames();
		for (String name : names) {
			Object val = get(name);
			if (val instanceof Number) {
			    temp = Double.doubleToLongBits(((Number)val).doubleValue());
			} else {
				temp = val.hashCode();
			}
			result = prime * result + (int) (temp ^ (temp >>> 32));
		}
  	    return result+stepIndex;
	}

	public boolean equals(Object obj, boolean checkStep) {
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		
		if (checkStep) {
			if (stepIndex != ((IPosition)obj).getStepIndex())
				return false;
		}

		final Collection<String> ours   = getNames();
		final Collection<String> theirs = ((IPosition)obj).getNames();
		if (!ours.equals(theirs)) return false;		
		for (String name : ours) {
			Object val1 = get(name);
			Object val2 = ((IPosition)obj).get(name);
			if (val1==null && val2!=null) return false;
			if (val1!=null && val2==null) return false;
			if (!val1.equals(val2)) return false;
		}
		
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, true);
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder("[");
		buf.append("stepIndex=");
		buf.append(stepIndex);
		buf.append(", ");
		final Collection<String> names   = getNames();
        for (Iterator<String> it = names.iterator(); it.hasNext();) {
			String name = it.next();
        	buf.append(name);
        	buf.append("(");
        	buf.append(getIndex(name));
        	buf.append(")");
         	buf.append("=");
        	buf.append(get(name));
        	if (it.hasNext()) buf.append(", ");
		}
    	buf.append("]");
    	return buf.toString();
	}
	

	/**
	 * This method always creates a new map of indices 
	 * @return the data indices mapped name:index
	 */
	@UiHidden
	public Map<String, Integer> getIndices() {
		final Map<String,Integer> indices = new LinkedHashMap<>(size());
		for (String name : getNames()) indices.put(name, getIndex(name));
		return indices;
	}

	public Collection<String> getDimensionNames(int dimension) {
		if (dimensionNames==null && dimension==0) return getNames();
		if (dimensionNames==null)                 return null;
		if (dimension>=dimensionNames.size())     return null;
		return dimensionNames.get(dimension);
	}

	public List<Collection<String>> getDimensionNames() {
		if (dimensionNames==null)  dimensionNames = Arrays.asList(getNames());
		return dimensionNames;
	}
	public void setDimensionNames(List<Collection<String>> dNames) {
		this.dimensionNames = dNames;
	}
	
	@Override
	public int getScanRank() {
		if (dimensionNames!=null) return dimensionNames.size();
		return 1;
	}

	@Override
	public int getIndex(int dimension) {
		final String name = getDimensionNames(dimension).iterator().next();
		return getIndex(name);
	}
}
