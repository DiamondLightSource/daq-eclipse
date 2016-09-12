package org.eclipse.scanning.api.scan.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.INameable;
import org.eclipse.scanning.api.INamedNode;

public class ControlGroup extends AbstractControl implements INameable {

	
	public ControlGroup() {
		
	}

	public List<INamedNode> getControls() {
		return Arrays.asList(getChildren());
	}

	public void setControls(List<INamedNode> controls) {
		for (INamedNode scannableControl : controls) {
			scannableControl.setParentName(getName());
		}
		this.setChildren(controls.toArray(new INamedNode[controls.size()]));
	}
}
