package org.eclipse.scanning.api.points.models;

import org.eclipse.scanning.api.annotation.MinimumValue;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;


/**
 * A model for a raster scan within a rectangular box in two-dimensional space, which evenly fills the box with a grid
 * defined by the numbers of points (along each of the two axes) set in this model.
 *
 * @author Colin Palmer
 *
 */
public class GridModel extends AbstractBoundingBoxModel {

	@FieldDescriptor(label="Columns (fast)", 
			         max=100000, 
			         min=1, 
			         hint="The number of points that the grid should run over, the fast direction.")
	private int fastAxisPoints = 5;
	
	@FieldDescriptor(label="Rows (slow)", 
			         max=100000, 
			         min=1, 
			         hint="The number of points that the grid should run over, the slow direction.")
	private int slowAxisPoints = 5;
	
	@FieldDescriptor(label="Snake")
	private boolean snake = false;
	
	public GridModel() {
		// We are a bean
	}
	
	public GridModel(String fastName, String slowName) {
		setFastAxisName(fastName);
		setSlowAxisName(slowName);
	}

	@Override
	public String getName() {
		return "Grid";
	}
	@MinimumValue("1")
	public int getFastAxisPoints() {
		return fastAxisPoints;
	}
	public void setFastAxisPoints(int newValue) {
		int oldValue = this.fastAxisPoints;
		this.fastAxisPoints = newValue;
		this.pcs.firePropertyChange("fastAxisPoints", oldValue, newValue);
	}
	@MinimumValue("1")
	public int getSlowAxisPoints() {
		return slowAxisPoints;
	}
	public void setSlowAxisPoints(int newValue) {
		int oldValue = this.slowAxisPoints;
		this.slowAxisPoints = newValue;
		this.pcs.firePropertyChange("slowAxisPoints", oldValue, newValue);
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
		result = prime * result + fastAxisPoints;
		result = prime * result + slowAxisPoints;
		result = prime * result + (snake ? 1231 : 1237);
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
		GridModel other = (GridModel) obj;
		if (fastAxisPoints != other.fastAxisPoints)
			return false;
		if (slowAxisPoints != other.slowAxisPoints)
			return false;
		if (snake != other.snake)
			return false;
		return true;
	}
}
