package org.eclipse.scanning.api.scan.ui;

import org.eclipse.scanning.api.INameable;

public class ControlNode extends AbstractControl implements INameable {

	private double increment=1;
	
	public ControlNode() {
	}
	public ControlNode(String name, double increment) {
		this();
		setName(name);
		this.increment     = increment;
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
