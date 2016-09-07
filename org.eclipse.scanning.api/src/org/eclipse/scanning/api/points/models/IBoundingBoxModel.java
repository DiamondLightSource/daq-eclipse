package org.eclipse.scanning.api.points.models;


/**
 * A model for a scan within a rectangular box in two-dimensional space.
 *
 * @author Colin Palmer
 *
 */
public interface IBoundingBoxModel extends IContainerModel {

	public BoundingBox getBoundingBox();

	public void setBoundingBox(BoundingBox boundingBox);
}