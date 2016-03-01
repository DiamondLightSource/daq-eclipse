package org.eclipse.scanning.api.points.models;


/**
 * A model for a scan at a single two-dimensional point.
 *
 * @author Colin Palmer
 *
 */
public interface IPointModel extends IScanPathModel {

	public double getX();

	public void setX(double x);

	public double getY();

	public void setY(double y);
}