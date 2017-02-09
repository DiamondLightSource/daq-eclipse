package org.eclipse.scanning.api.malcolm.attributes;

import java.util.Arrays;

/**
 * 
 * Encapsulates an attribute as read from a malcolm device
 * 
 * @author Matt Taylor
 *
 */
public abstract class MalcolmAttribute {
	private String name;
	private String description;
	private String[] tags;
	private boolean writeable;
	private String label;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String[] getTags() {
		return tags;
	}
	public void setTags(String[] tags) {
		this.tags = tags;
	}
	public boolean isWriteable() {
		return writeable;
	}
	public void setWriteable(boolean writeable) {
		this.writeable = writeable;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public abstract Object getValue();

	@Override
	public String toString() {
		return "MalcolmAttribute [name=" + name + ", description=" + description + ", tags=" + Arrays.toString(tags)
				+ ", writeable=" + writeable + ", label=" + label + "]";
	}
}
