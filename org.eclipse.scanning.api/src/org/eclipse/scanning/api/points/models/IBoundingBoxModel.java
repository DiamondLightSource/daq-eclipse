package org.eclipse.scanning.api.points.models;

/**
 * A model for a scan within a rectangular box in two-dimensional space.
 *
 * @author Colin Palmer
 * @author Matthew Gerring
 */
public interface IBoundingBoxModel extends IScanPathModel {

	public BoundingBox getBoundingBox();
	public void setBoundingBox(BoundingBox boundingBox);
	
	public String getFastAxisName();
	public void setFastAxisName(String newValue);
	
	public String getSlowAxisName();
	public void setSlowAxisName(String newValue);

}