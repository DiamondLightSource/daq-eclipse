package org.eclipse.scanning.api.points.models;

public abstract class AbstractBoundingBoxModel implements IBoundingBoxModel {

	private BoundingBox boundingBox;
	private String      xName = "x";
	private String      yName = "y";
	
	@Override
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
	@Override
	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((boundingBox == null) ? 0 : boundingBox.hashCode());
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
		AbstractBoundingBoxModel other = (AbstractBoundingBoxModel) obj;
		if (boundingBox == null) {
			if (other.boundingBox != null)
				return false;
		} else if (!boundingBox.equals(other.boundingBox))
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

}
