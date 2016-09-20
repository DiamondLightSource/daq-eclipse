package org.eclipse.scanning.api.scan.ui;

import org.eclipse.scanning.api.INameable;

public class ControlFileNode extends AbstractControl implements INameable {

	private String file;
	
	public ControlFileNode() {
	}
	public ControlFileNode(String name) {
		this();
		setName(name);
	}
	public ControlFileNode(String parentName, String name) {
		this(name);
		setParentName(parentName);
	}

	
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	
}
