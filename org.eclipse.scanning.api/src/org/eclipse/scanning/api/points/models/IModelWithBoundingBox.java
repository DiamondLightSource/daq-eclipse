package org.eclipse.scanning.api.points.models;

public interface IModelWithBoundingBox {

	public BoundingBox getBoundingBox();

	public void setBoundingBox(BoundingBox boundingBox);

	public int hashCode();

	public boolean equals(Object obj);

}