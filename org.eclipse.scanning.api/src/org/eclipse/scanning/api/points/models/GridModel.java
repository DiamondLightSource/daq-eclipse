package org.eclipse.scanning.api.points.models;

public class GridModel implements IPathModelWithBoundingBox {
	
	private BoundingBox boundingBox;
	private int columns = 1;
	private int rows = 1;
	
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
	private boolean snake = false;
	
	private transient double xStep = 0.0;
	private transient double yStep = 0.0;
	
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
	public void setSnake(boolean biDirectional) {
		this.snake = biDirectional;
	}
	public double getxStep() {
		return xStep;
	}
	public void setxStep(double xStep) {
		this.xStep = xStep;
	}
	public double getyStep() {
		return yStep;
	}
	public void setyStep(double yStep) {
		this.yStep = yStep;
	}
	@Override
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
	@Override
	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((boundingBox == null) ? 0 : boundingBox.hashCode());
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
		if (boundingBox == null) {
			if (other.boundingBox != null)
				return false;
		} else if (!boundingBox.equals(other.boundingBox))
			return false;
		if (columns != other.columns)
			return false;
		if (rows != other.rows)
			return false;
		if (snake != other.snake)
			return false;
		return true;
	}
}
