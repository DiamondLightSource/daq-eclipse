package org.eclipse.scanning.example.detector;

import org.eclipse.scanning.api.annotation.UiComesAfter;
import org.eclipse.scanning.api.annotation.UiSection;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.device.models.AbstractDetectorModel;

public class MandelbrotModel extends AbstractDetectorModel {

	// Parameters controlling iteration and termination of the Julia/Mandelbrot algorithm
	@FieldDescriptor(label="Maximum Iterations", 
	         minimum=1, 
	         hint="Iterations to use.")
	private int    maxIterations;
	
	@FieldDescriptor(label="Escape Radius", 
	         minimum=0, 
	         hint="The radius of escape for the mandelbrot algorithm.")
	private double escapeRadius;

	// Parameters controlling the dimensions and size of the 1D and 2D Julia set datasets
	@FieldDescriptor(label="Columns", 
	         maximum=100000, 
	         minimum=1, 
	         hint="The number of points that the grid should run over, the x direction.")
	private int    columns; // for the 2D dataset, from -maxRealCoordinate to +maxRealCoordinate
	
	@FieldDescriptor(label="Rows", 
	         maximum=100000, 
	         minimum=1, 
	         hint="The number of points that the grid should run over, the y direction.")
	private int    rows;    // for the 2D dataset, from -maxImaginaryCoordinate to +maxImaginaryCoordinate
	
	@FieldDescriptor(label="Points", 
	         validif="points<=(maxRealCoordinate*maxIterations)", 
	         minimum=1, 
	         hint="Points of Mandelbrot.")
	private int    points;  // for the 1D dataset, from 0 to maxRealCoordinate
	
	@FieldDescriptor(label="Max. Real Coordinate")
	private double maxRealCoordinate;
	
	@FieldDescriptor(label="Max. Imaginary Coordinate")
	private double maxImaginaryCoordinate;

	// The names of the scannables used to determine the position to calculate
	@FieldDescriptor(label="Real Axis Name")
	private String realAxisName;
	
	@FieldDescriptor(label="Imaginary Axis Name")
	private String imaginaryAxisName;


	public MandelbrotModel() {
		maxIterations = 500;
		escapeRadius = 10.0;
		columns = 301;
		rows = 241;
		points = 1000;
		maxRealCoordinate = 1.5;
		maxImaginaryCoordinate = 1.2;
		setName("mandelbrot");
		setExposureTime(0.1d);
		realAxisName = "stage_x";
		imaginaryAxisName = "stage_y";
	}
	
	public MandelbrotModel(String r, String i) {
	    this();
	    this.realAxisName = r;
	    this.imaginaryAxisName = i;
	}


	@UiSection("Algorithm parameters")
	public int getMaxIterations() {
		return maxIterations;
	}
	public void setMaxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
	}
	@UiComesAfter("maxIterations")
	public double getEscapeRadius() {
		return escapeRadius;
	}
	public void setEscapeRadius(double escapeRadius) {
		this.escapeRadius = escapeRadius;
	}
	@UiSection("Julia set size")
	@UiComesAfter("escapeRadius")
	public int getColumns() {
		return columns;
	}
	public void setColumns(int columns) {
		this.columns = columns;
	}
	@UiComesAfter("columns")
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
	@UiComesAfter("rows")
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	@UiComesAfter("points")
	public double getMaxRealCoordinate() {
		return maxRealCoordinate;
	}
	public void setMaxRealCoordinate(double maxX) {
		this.maxRealCoordinate = maxX;
	}
	@UiComesAfter("maxRealCoordinate")
	public double getMaxImaginaryCoordinate() {
		return maxImaginaryCoordinate;
	}
	public void setMaxImaginaryCoordinate(double maxY) {
		this.maxImaginaryCoordinate = maxY;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + columns;
		long temp;
		temp = Double.doubleToLongBits(escapeRadius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((imaginaryAxisName == null) ? 0 : imaginaryAxisName.hashCode());
		temp = Double.doubleToLongBits(maxImaginaryCoordinate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + maxIterations;
		temp = Double.doubleToLongBits(maxRealCoordinate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + points;
		result = prime * result + ((realAxisName == null) ? 0 : realAxisName.hashCode());
		result = prime * result + rows;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MandelbrotModel other = (MandelbrotModel) obj;
		if (columns != other.columns)
			return false;
		if (Double.doubleToLongBits(escapeRadius) != Double.doubleToLongBits(other.escapeRadius))
			return false;
		if (imaginaryAxisName == null) {
			if (other.imaginaryAxisName != null)
				return false;
		} else if (!imaginaryAxisName.equals(other.imaginaryAxisName))
			return false;
		if (Double.doubleToLongBits(maxImaginaryCoordinate) != Double.doubleToLongBits(other.maxImaginaryCoordinate))
			return false;
		if (maxIterations != other.maxIterations)
			return false;
		if (Double.doubleToLongBits(maxRealCoordinate) != Double.doubleToLongBits(other.maxRealCoordinate))
			return false;
		if (points != other.points)
			return false;
		if (realAxisName == null) {
			if (other.realAxisName != null)
				return false;
		} else if (!realAxisName.equals(other.realAxisName))
			return false;
		if (rows != other.rows)
			return false;
		return true;
	}

	@UiSection("Device details")
	@UiComesAfter("maxImaginaryCoordinate")
	public String getName() {
		return super.getName();
	}
	@UiComesAfter("name")
	public String getRealAxisName() {
		return realAxisName;
	}
	public void setRealAxisName(String xName) {
		this.realAxisName = xName;
	}
	@UiComesAfter("realAxisName")
	public String getImaginaryAxisName() {
		return imaginaryAxisName;
	}
	public void setImaginaryAxisName(String yName) {
		this.imaginaryAxisName = yName;
	}

	@UiComesAfter("imaginaryAxisName")
	public double getExposureTime() {
		return super.getExposureTime();
	}
}
