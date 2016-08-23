package org.eclipse.scanning.api.scan.ui;

import java.util.List;

import org.eclipse.scanning.api.INameable;

public class ControlGroup extends AbstractControl implements INameable {

	private List<ControlNode> controls;
	
	public ControlGroup() {
		ControlFactory.getInstance().add(this);
	}

	public List<ControlNode> getControls() {
		return controls;
	}

	public void setControls(List<ControlNode> controls) {
		this.controls = controls;
		for (ControlNode scannableControl : controls) {
			scannableControl.setParent(this);
		}
		this.setChildren(controls.toArray(new ControlNode[controls.size()]));
	}
}
