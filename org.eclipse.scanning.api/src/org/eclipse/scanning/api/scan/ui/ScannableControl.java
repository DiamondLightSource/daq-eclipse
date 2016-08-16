package org.eclipse.scanning.api.scan.ui;

import org.eclipse.scanning.api.IAncestered;
import org.eclipse.scanning.api.INameable;

public class ScannableControl implements INameable, IAncestered {

	private INameable parent;
	private String displayName;
	private String scannableName;
	private double increment=1;
	
	public ScannableControl() {
		ScannableControlFactory.getInstance().add(this);
	}
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getScannableName() {
		return scannableName;
	}
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}
	public double getIncrement() {
		return increment;
	}
	public void setIncrement(double increment) {
		this.increment = increment;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
		long temp;
		temp = Double.doubleToLongBits(increment);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScannableControl other = (ScannableControl) obj;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
			return false;
		if (Double.doubleToLongBits(increment) != Double.doubleToLongBits(other.increment))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (scannableName == null) {
			if (other.scannableName != null)
				return false;
		} else if (!scannableName.equals(other.scannableName))
			return false;
		return true;
	}
	@Override
	public String getName() {
		return scannableName;
	}
	@Override
	public void setName(String name) {
		throw new IllegalArgumentException("Cannot change the name of "+scannableName);
	}

	@Override
	public INameable getParent() {
		return parent;
	}

	@Override
	public void setParent(INameable parent) {
		this.parent = parent;
	}
}
