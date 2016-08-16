package org.eclipse.scanning.api.scan.ui;

import java.util.Arrays;

import org.eclipse.scanning.api.INamedNode;

abstract class AbstractControl implements INamedNode {

	private INamedNode parent;
	@Override
	public INamedNode getParent() {
		return parent;
	}

	@Override
	public void setParent(INamedNode parent) {
		this.parent = parent;
	}
	
	private INamedNode[] children;

	@Override
	public boolean hasChildren() {
		return children!=null&&children.length>0;
	}

	public INamedNode[] getChildren() {
		return children;
	}

	public void setChildren(INamedNode[] children) {
		this.children = children;
	}

	private String name;
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		AbstractControl other = (AbstractControl) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
