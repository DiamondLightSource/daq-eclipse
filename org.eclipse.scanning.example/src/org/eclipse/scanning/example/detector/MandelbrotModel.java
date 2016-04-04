package org.eclipse.scanning.example.detector;

import org.eclipse.scanning.api.device.models.IDetectorModel;

public class MandelbrotModel implements IDetectorModel {

	// Parameters controlling iteration and termination of the Julia/Mandelbrot algorithm
	private int    maxIterations;
	private double escapeRadius;

	// Parameters controlling the dimensions and size of the 1D and 2D Julia set datasets
	private int    columns; // for the 2D dataset, from -maxRealCoordinate to +maxRealCoordinate
	private int    rows;    // for the 2D dataset, from -maxImaginaryCoordinate to +maxImaginaryCoordinate
	private int    points;  // for the 1D dataset, from 0 to maxRealCoordinate
	private double maxRealCoordinate;
	private double maxImaginaryCoordinate;

	// The names of the scannables used to determine the position to calculate
	private String realAxisName;
	private String imaginaryAxisName;

	/**
	 * The name of the detector device
	 */
	private String name;

	/**
	 * The exposure time. If calculation is shorter than this, time is artificially added to make the detector respect
	 * the time that is set.
	 */
	private double exposureTime; // Seconds

	public MandelbrotModel() {
		maxIterations = 500;
		escapeRadius = 10.0;
		columns = 301;
		rows = 241;
		points = 1000;
		maxRealCoordinate = 1.5;
		maxImaginaryCoordinate = 1.2;
		name = "mandelbrot_detector";
		realAxisName = "x";
		imaginaryAxisName = "y";
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
	public double getMaxRealCoordinate() {
		return maxRealCoordinate;
	}
	public void setMaxRealCoordinate(double maxX) {
		this.maxRealCoordinate = maxX;
	}
	public double getMaxImaginaryCoordinate() {
		return maxImaginaryCoordinate;
	}
	public void setMaxImaginaryCoordinate(double maxY) {
		this.maxImaginaryCoordinate = maxY;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columns;
		long temp;
		temp = Double.doubleToLongBits(escapeRadius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(exposureTime);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + maxIterations;
		temp = Double.doubleToLongBits(maxRealCoordinate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxImaginaryCoordinate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + points;
		result = prime * result + rows;
		result = prime * result + ((realAxisName == null) ? 0 : realAxisName.hashCode());
		result = prime * result + ((imaginaryAxisName == null) ? 0 : imaginaryAxisName.hashCode());
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
		if (Double.doubleToLongBits(exposureTime) != Double
				.doubleToLongBits(other.exposureTime))
			return false;
		if (maxIterations != other.maxIterations)
			return false;
		if (Double.doubleToLongBits(maxRealCoordinate) != Double
				.doubleToLongBits(other.maxRealCoordinate))
			return false;
		if (Double.doubleToLongBits(maxImaginaryCoordinate) != Double
				.doubleToLongBits(other.maxImaginaryCoordinate))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (points != other.points)
			return false;
		if (rows != other.rows)
			return false;
		if (realAxisName == null) {
			if (other.realAxisName != null)
				return false;
		} else if (!realAxisName.equals(other.realAxisName))
			return false;
		if (imaginaryAxisName == null) {
			if (other.imaginaryAxisName != null)
				return false;
		} else if (!imaginaryAxisName.equals(other.imaginaryAxisName))
			return false;
		return true;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRealAxisName() {
		return realAxisName;
	}
	public void setRealAxisName(String xName) {
		this.realAxisName = xName;
	}
	public String getImaginaryAxisName() {
		return imaginaryAxisName;
	}
	public void setImaginaryAxisName(String yName) {
		this.imaginaryAxisName = yName;
	}
	
	public double getExposureTime() {
		return exposureTime;
	}

	public void setExposureTime(double exposure) {
		this.exposureTime = exposure;
	}

}
