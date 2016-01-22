package org.eclipse.scanning.api.points.models;


public interface IBoundingBoxModel extends IScanPathModel {

	public BoundingBox getBoundingBox();

	public void setBoundingBox(BoundingBox boundingBox);
}