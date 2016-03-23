package org.eclipse.scanning.api.points.models;

import org.eclipse.scanning.api.annotation.FieldDescriptor;

/**
 * A model for a raster scan within a rectangular box in two-dimensional space, beginning at the box's start
 * coordinates and moving in X and Y steps of the sizes set in this model.
 *
 * @author Colin Palmer
 *
 */
public class RasterModel extends AbstractBoundingBoxModel {

	@FieldDescriptor(label="Fast Step", scannable="xName", hint="The step in the fast direction to take in the units of the fast scannable.")
	private double xStep = 1;
	
	@FieldDescriptor(label="Slow Step", scannable="yName", hint="The step in the slow direction to take in the units of the slow scannable.")
	private double yStep = 1;
	
	@FieldDescriptor(label="Snake", hint="Snake: left->right->left etc. Nonsnake left->right, repeat") 
	private boolean snake = false;
	
	public RasterModel() {
		
	}
	public RasterModel(String xName, String yName) {
		super(xName, yName);
	}

	@Override
	public String getName() {
		return "Raster";
	}
	public double getxStep() {
		return xStep;
	}
	public void setxStep(double xStep) {
		double oldValue = this.xStep;
		this.xStep = xStep;
		this.pcs.firePropertyChange("xStep", oldValue, xStep);
	}
	public double getyStep() {
		return yStep;
	}
	public void setyStep(double yStep) {
		double oldValue = this.yStep;
		this.yStep = yStep;
		this.pcs.firePropertyChange("yStep", oldValue, yStep);
	}
	/**
	 * <pre>
	 * snake = true
	 * -------------------->
	 *                     |
	 * <--------------------
	 * |
	 * --------------------> etc.
     * 
     * snake = false
     * -------------------->
     * -------------------->
     * -------------------->
     * </pre>
	**/
	public boolean isSnake() {
		return snake;
	}
	public void setSnake(boolean snake) {
		boolean oldValue = this.snake;
		this.snake = snake;
		this.pcs.firePropertyChange("snake", oldValue, snake);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (snake ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(xStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(yStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		RasterModel other = (RasterModel) obj;
		if (snake != other.snake)
			return false;
		if (Double.doubleToLongBits(xStep) != Double
				.doubleToLongBits(other.xStep))
			return false;
		if (Double.doubleToLongBits(yStep) != Double
				.doubleToLongBits(other.yStep))
			return false;
		return true;
	}
}
