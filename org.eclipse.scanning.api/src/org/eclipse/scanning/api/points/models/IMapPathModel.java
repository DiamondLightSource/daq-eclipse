package org.eclipse.scanning.api.points.models;

/**
 * A model for a scan in two-dimensional space.
 */
public interface IMapPathModel extends IScanPathModel {
	
	public String getFastAxisName();
	public void setFastAxisName(String newValue);
	
	public String getSlowAxisName();
	public void setSlowAxisName(String newValue);

}
