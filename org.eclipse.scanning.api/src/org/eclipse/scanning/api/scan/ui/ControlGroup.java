package org.eclipse.scanning.api.scan.ui;

import java.util.List;

import org.eclipse.scanning.api.INameable;

public class ControlGroup extends AbstractControl implements INameable {

	private List<ControlNode> controls;
	
	public ControlGroup() {
		
	}

	public List<ControlNode> getControls() {
		return controls;
	}

	public void setControls(List<ControlNode> controls) {
		this.controls = controls;
		for (ControlNode scannableControl : controls) {
			scannableControl.setParentName(getName());
		}
		this.setChildren(controls.toArray(new ControlNode[controls.size()]));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((controls == null) ? 0 : controls.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ControlGroup other = (ControlGroup) obj;
		if (controls == null) {
			if (other.controls != null)
				return false;
		} else if (!controls.equals(other.controls))
			return false;
		return true;
	}
}
