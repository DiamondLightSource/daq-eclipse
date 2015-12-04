package org.eclipse.scanning.example.detector;

import org.eclipse.scanning.example.detector.MandelbrotDetector.OutputDimensions;

public class MandelbrotModel {

	private OutputDimensions outputDimensions = OutputDimensions.TWO_D;

	private int    maxIterations = 500;
	private double escapeRadius  = 10.0;
	private int    columns = 301;
	private int    rows = 241;
	private int    points = 1000;
	private double maxx = 1.5;
	private double maxy = 1.2;
	public int getMaxIterations() {
		return maxIterations;
	}
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}
	public double getEscapeRadius() {
		return escapeRadius;
	}
	public void setEscapeRadius(double escapeRadius) {
		this.escapeRadius = escapeRadius;
	}
	public int getColumns() {
		return columns;
	}
	public void setColumns(int columns) {
		this.columns = columns;
	}
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public double getMaxx() {
		return maxx;
	}
	public void setMaxx(double maxx) {
		this.maxx = maxx;
	}
	public double getMaxy() {
		return maxy;
	}
	public void setMaxy(double maxy) {
		this.maxy = maxy;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columns;
		long temp;
		temp = Double.doubleToLongBits(escapeRadius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + maxIterations;
		temp = Double.doubleToLongBits(maxx);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime
				* result
				+ ((outputDimensions == null) ? 0 : outputDimensions.hashCode());
		result = prime * result + points;
		result = prime * result + rows;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MandelbrotModel other = (MandelbrotModel) obj;
		if (columns != other.columns)
			return false;
		if (Double.doubleToLongBits(escapeRadius) != Double
				.doubleToLongBits(other.escapeRadius))
			return false;
		if (maxIterations != other.maxIterations)
			return false;
		if (Double.doubleToLongBits(maxx) != Double
				.doubleToLongBits(other.maxx))
			return false;
		if (Double.doubleToLongBits(maxy) != Double
				.doubleToLongBits(other.maxy))
			return false;
		if (outputDimensions != other.outputDimensions)
			return false;
		if (points != other.points)
			return false;
		if (rows != other.rows)
			return false;
		return true;
	}
	public OutputDimensions getOutputDimensions() {
		return outputDimensions;
	}
	public void setOutputDimensions(OutputDimensions outputDimensions) {
		this.outputDimensions = outputDimensions;
	}

	
}
