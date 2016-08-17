package org.eclipse.scanning.api.scan.ui;

import java.util.List;

import org.eclipse.scanning.api.INameable;

public class ControlGroup extends AbstractControl implements INameable {

	private List<Control> controls;
	
	public ControlGroup() {
		ControlFactory.getInstance().add(this);
	}

	public List<Control> getControls() {
		return controls;
	}

	public void setControls(List<Control> controls) {
		this.controls = controls;
		for (Control scannableControl : controls) {
			scannableControl.setParent(this);
		}
		this.setChildren(controls.toArray(new Control[controls.size()]));
	}
}
