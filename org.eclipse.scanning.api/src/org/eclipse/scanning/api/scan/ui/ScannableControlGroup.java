package org.eclipse.scanning.api.scan.ui;

import java.util.List;

import org.eclipse.scanning.api.INameable;

public class ScannableControlGroup implements INameable {

	private String name;
	private List<ScannableControl> controls;
	
	public ScannableControlGroup() {
		ScannableControlFactory.getInstance().add(this);
	}

	public List<ScannableControl> getControls() {
		return controls;
	}

	public void setControls(List<ScannableControl> controls) {
		this.controls = controls;
		for (ScannableControl scannableControl : controls) {
			scannableControl.setParent(this);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((controls == null) ? 0 : controls.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ScannableControlGroup other = (ScannableControlGroup) obj;
		if (controls == null) {
			if (other.controls != null)
				return false;
		} else if (!controls.equals(other.controls))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
