package org.eclipse.scanning.api.points.models;


public interface IBoundingLineModel extends IScanPathModel {

	public BoundingLine getBoundingLine();

	public void setBoundingLine(BoundingLine boundingLine);
}