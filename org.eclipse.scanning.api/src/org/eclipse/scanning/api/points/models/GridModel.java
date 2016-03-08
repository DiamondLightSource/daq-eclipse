package org.eclipse.scanning.api.points.models;

import org.eclipse.scanning.api.annotation.MinimumValue;


/**
 * A model for a raster scan within a rectangular box in two-dimensional space, which evenly fills the box with a grid
 * defined by the number columns and rows set in this model.
 *
 * @author Colin Palmer
 *
 */
public class GridModel extends AbstractBoundingBoxModel {

	private int columns = 5;
	private int rows = 5;
	private boolean snake = false;

	public GridModel() {
	}
	public GridModel(String xName, String yName) {
		super(xName, yName);
	}

	@Override
	public String getName() {
		return "Grid";
	}
	@MinimumValue("1")
	public int getColumns() {
		return columns;
	}
	public void setColumns(int columns) {
		int oldValue = this.columns;
		this.columns = columns;
		this.pcs.firePropertyChange("columns", oldValue, columns);
	}
	@MinimumValue("1")
	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		int oldValue = this.rows;
		this.rows = rows;
		this.pcs.firePropertyChange("rows", oldValue, rows);
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
		result = prime * result + columns;
		result = prime * result + rows;
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
		if (columns != other.columns)
			return false;
		if (rows != other.rows)
			return false;
		if (snake != other.snake)
			return false;
		return true;
	}
}
