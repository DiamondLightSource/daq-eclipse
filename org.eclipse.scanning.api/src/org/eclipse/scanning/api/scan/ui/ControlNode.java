package org.eclipse.scanning.api.scan.ui;

import org.eclipse.scanning.api.INameable;

public class ControlNode extends AbstractControl implements INameable {

	private String displayName;
	private double increment=1;
	
	public ControlNode() {
		ControlFactory.getInstance().add(this);
	}
	public ControlNode(String name, double increment) {
		this();
		setName(name);
		this.increment     = increment;
	}

	@Override
	public String getDisplayName() {
		if (displayName==null) return getScannableName();
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getScannableName() {
		return getName();
	}
	public void setScannableName(String scannableName) {
		setName(scannableName);
	}
	public double getIncrement() {
		return increment;
	}
	public void setIncrement(double increment) {
		this.increment = increment;
	}

}
