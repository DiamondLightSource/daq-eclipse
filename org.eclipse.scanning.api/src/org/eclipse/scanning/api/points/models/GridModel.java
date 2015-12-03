package org.eclipse.scanning.api.points.models;

public class GridModel implements IBoundingBoxModel {
	
	private IBoundingBoxModel boundingBox = new BoundingBoxModel();
	private int columns = 1;
	private int rows = 1;
	
	/** snake = true<br>
	  -------------------->  <br>
	  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     | <br>
	  <--------------------  <br>
      |                             <br>
      --------------------> etc. <br>
      <br>
      snake = false<br>
 	  --------------------> <br>
	  --------------------> <br>
	  --------------------> <br>
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
	
	/** snake = true<br>
	  -------------------->  <br>
	  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;     | <br>
	  <--------------------  <br>
    |                             <br>
    --------------------> etc. <br>
    <br>
    snake = false<br>
	  --------------------> <br>
	  --------------------> <br>
	  --------------------> <br>
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
	public double getWidth() {
		return boundingBox.getWidth();
	}
	@Override
	public void setWidth(double width) {
		boundingBox.setWidth(width);
	}
	@Override
	public double getHeight() {
		return boundingBox.getHeight();
	}
	@Override
	public void setHeight(double height) {
		boundingBox.setHeight(height);
	}
	@Override
	public boolean isParentRectangle() {
		return boundingBox.isParentRectangle();
	}
	@Override
	public void setParentRectangle(boolean isParentRectangle) {
		boundingBox.setParentRectangle(isParentRectangle);
	}
	@Override
	public double getxStart() {
		return boundingBox.getxStart();
	}
	@Override
	public void setxStart(double xStart) {
		boundingBox.setxStart(xStart);
	}
	@Override
	public double getyStart() {
		return boundingBox.getyStart();
	}
	@Override
	public void setyStart(double yStart) {
		boundingBox.setyStart(yStart);
	}
	@Override
	public double getAngle() {
		return boundingBox.getAngle();
	}
	@Override
	public void setAngle(double angle) {
		boundingBox.setAngle(angle);
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
