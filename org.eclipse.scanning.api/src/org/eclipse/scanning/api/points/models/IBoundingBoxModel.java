package org.eclipse.scanning.api.points.models;

/**
 * A model for a scan within a rectangular box in two-dimensional space.
 *
 * @author Colin Palmer
 * @author Matthew Gerring
 */
public interface IBoundingBoxModel extends IMapPathModel {

	public BoundingBox getBoundingBox();
	public void setBoundingBox(BoundingBox boundingBox);
	
}