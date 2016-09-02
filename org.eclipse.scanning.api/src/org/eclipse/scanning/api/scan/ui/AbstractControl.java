package org.eclipse.scanning.api.scan.ui;

import java.util.Arrays;

import org.eclipse.scanning.api.INamedNode;

abstract class AbstractControl implements INamedNode {
	
	// Should not serialize parent.
	private String parentName;
	private String name;
	private String displayName;
	private INamedNode[] children;

	public void add() { // Called by spring
		ControlTree.getInstance().add(this);
	}

	@Override
	public String getParentName() {
		return parentName;
	}

	@Override
	public void setParentName(String pName) {
		this.parentName = pName;
	}
	

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDisplayName() {
		if (displayName==null) return getName();
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(children);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parentName == null) ? 0 : parentName.hashCode());
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
		if (!Arrays.equals(children, other.children))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parentName == null) {
			if (other.parentName != null)
				return false;
		} else if (!parentName.equals(other.parentName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+" [name=" + name + "]";
	}

}
