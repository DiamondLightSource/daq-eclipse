package org.eclipse.scanning.api.event.scan;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

public class SampleData {
	
	@FieldDescriptor(label="Sample Name", hint="The name of the sample.\nMay be used in the file name written by acqusition.", fieldPosition=1, regex="[a-zA-Z0-9_]+")
	private String name;
	
	@FieldDescriptor(label="Description", hint="The description of the sample.\nWill be entered in the nexus file during scanning.", fieldPosition=2)
	private String description;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
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
		SampleData other = (SampleData) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
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
	public String getDescription() {
		return description;
	}
	public void setDescription(String decription) {
		this.description = decription;
	}
}
