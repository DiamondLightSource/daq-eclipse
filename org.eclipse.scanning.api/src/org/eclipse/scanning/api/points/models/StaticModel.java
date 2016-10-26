package org.eclipse.scanning.api.points.models;


/**
 * A model for one or more positions where nothing is moved. This can be used to expose detectors
 * without moving any scannables.
 * 
 * @author Matthew Gerring
 */
public class StaticModel extends AbstractPointsModel {

	private int size = 1;
	
	public StaticModel() {
		setName("Static");
	}
	
	public StaticModel(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + size;
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
		StaticModel other = (StaticModel) obj;
		if (size != other.size)
			return false;
		return true;
	}
	
}
