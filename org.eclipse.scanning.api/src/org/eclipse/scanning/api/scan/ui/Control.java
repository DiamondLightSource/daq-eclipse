package org.eclipse.scanning.api.scan.ui;

import org.eclipse.scanning.api.INameable;

public class Control extends AbstractControl implements INameable {

	private String displayName;
	private String scannableName;
	private double increment=1;
	
	public Control() {
		ControlFactory.getInstance().add(this);
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
	public String getName() {
		return scannableName;
	}
	@Override
	public void setName(String name) {
		throw new IllegalArgumentException("Cannot change the name of "+scannableName);
	}
}
