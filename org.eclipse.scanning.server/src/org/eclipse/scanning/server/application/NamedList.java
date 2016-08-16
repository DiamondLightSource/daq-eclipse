package org.eclipse.scanning.server.application;

import java.util.List;

class NamedList {
	
	private List<String> refs;
	private String       name;
	public NamedList(String name, List<String> refs) {
		this.name = name;
		this.refs = refs;
	}
	public List<String> getRefs() {
		return refs;
	}
	public void setRefs(List<String> refs) {
		this.refs = refs;
	}
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
		result = prime * result + ((refs == null) ? 0 : refs.hashCode());
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
		NamedList other = (NamedList) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (refs == null) {
			if (other.refs != null)
				return false;
		} else if (!refs.equals(other.refs))
			return false;
		return true;
	}
	
}
