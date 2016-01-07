package org.eclipse.scanning.example.detector;

import java.io.IOException;

public class MandelbrotModel {

	private OutputDimensions outputDimensions = OutputDimensions.TWO_D;

	private int    maxIterations;
	private double escapeRadius;
	private int    columns;
	private int    rows;
	private int    points;
	private double maxx;
	private double maxy;
	private String name;
	private String xName;
	private String yName;
	
	public MandelbrotModel() throws IOException {
		
		maxIterations = 500;
		escapeRadius  = 10.0;
		columns       = 301;
		rows          = 241;
		points        = 1000;
		maxx          = 1.5;
		maxy          = 1.2;
		name          = "mandelbrot_detector";
		xName         = "x";
		yName         = "y";
	}
	
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
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime
				* result
				+ ((outputDimensions == null) ? 0 : outputDimensions.hashCode());
		result = prime * result + points;
		result = prime * result + rows;
		result = prime * result + ((xName == null) ? 0 : xName.hashCode());
		result = prime * result + ((yName == null) ? 0 : yName.hashCode());
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
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (outputDimensions != other.outputDimensions)
			return false;
		if (points != other.points)
			return false;
		if (rows != other.rows)
			return false;
		if (xName == null) {
			if (other.xName != null)
				return false;
		} else if (!xName.equals(other.xName))
			return false;
		if (yName == null) {
			if (other.yName != null)
				return false;
		} else if (!yName.equals(other.yName))
			return false;
		return true;
	}
	public OutputDimensions getOutputDimensions() {
		return outputDimensions;
	}
	public void setOutputDimensions(OutputDimensions outputDimensions) {
		this.outputDimensions = outputDimensions;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getxName() {
		return xName;
	}
	public void setxName(String xName) {
		this.xName = xName;
	}
	public String getyName() {
		return yName;
	}
	public void setyName(String yName) {
		this.yName = yName;
	}
	
}
