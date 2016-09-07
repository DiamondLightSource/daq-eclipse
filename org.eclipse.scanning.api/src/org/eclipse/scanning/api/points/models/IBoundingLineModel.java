package org.eclipse.scanning.api.points.models;


/**
 * A model for a scan along a straight line in two-dimensional space.
 *
 * @author Colin Palmer
 *
 */
public interface IBoundingLineModel extends IContainerModel {

	public BoundingLine getBoundingLine();

	public void setBoundingLine(BoundingLine boundingLine);
}