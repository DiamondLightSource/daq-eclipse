package org.eclipse.scanning.api.points.models;

import org.eclipse.scanning.api.annotation.FieldDescriptor;
import org.eclipse.scanning.api.annotation.UiHidden;


/**
 * A model for a scan within a rectangular box in two-dimensional space.
 * <p>
 * This abstract class defines the box size and the names of the two axes.
 *
 * @author Colin Palmer
 *
 */
public abstract class AbstractBoundingBoxModel extends AbstractPointsModel implements IBoundingBoxModel {

	@FieldDescriptor(visible=false)
	private BoundingBox boundingBox;
	
	@FieldDescriptor(label="Fast", hint="The name of the scannable in the fast direction, for instance 'x'.") // TODO Right?
	private String      xName = "x";
	
	@FieldDescriptor(label="Slow", hint="The name of the scannable in the fast direction, for instance 'y'.")  // TODO Right?
	private String      yName = "y";

	protected AbstractBoundingBoxModel() {
		super();
	}

	@Override
	@UiHidden
	public BoundingBox getBoundingBox() {
		return boundingBox;
	}
	@Override
	public void setBoundingBox(BoundingBox newValue) {
		BoundingBox oldValue = this.boundingBox;
		this.boundingBox = newValue;
		this.pcs.firePropertyChange("boundingBox", oldValue, newValue);
	}
	@UiHidden
	public String getxName() {
		return xName;
	}
	public void setxName(String newValue) {
		String oldValue = this.xName;
		this.xName = newValue;
		this.pcs.firePropertyChange("xName", oldValue, newValue);
	}
	@UiHidden
	public String getyName() {
		return yName;
	}
	public void setyName(String newValue) {
		String oldValue = this.yName;
		this.yName = newValue;
		this.pcs.firePropertyChange("yName", oldValue, newValue);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((boundingBox == null) ? 0 : boundingBox.hashCode());
		result = prime * result + ((xName == null) ? 0 : xName.hashCode());
		result = prime * result + ((yName == null) ? 0 : yName.hashCode());
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
