package org.eclipse.scanning.api.points;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractPosition implements IPosition {
	
	private int stepIndex = -1;
	
	public int getStepIndex() {
		return stepIndex;
	}

	public void setStepIndex(int stepIndex) {
		this.stepIndex = stepIndex;
	}

	public IPosition composite(IPosition with) {
		if (with==null) return this; // this+null = this
		final MapPosition ret = new MapPosition();
		ret.putAll(with);
		ret.putAll(this);
		ret.putAllIndices(with);
		ret.putAllIndices(this);
		return ret;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		final List<String> names   = getNames();
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
		if (getClass() != obj.getClass())
			return false;
		
		if (checkStep) {
			if (stepIndex != ((IPosition)obj).getStepIndex())
				return false;
		}

		final List<String> ours   = getNames();
		final List<String> theirs = ((IPosition)obj).getNames();
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
		final List<String> names   = getNames();
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
}
