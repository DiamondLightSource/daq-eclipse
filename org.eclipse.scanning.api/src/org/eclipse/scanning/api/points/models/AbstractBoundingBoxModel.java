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
	private String      fastAxisName = "x";

	@FieldDescriptor(label="Slow", hint="The name of the scannable in the fast direction, for instance 'y'.")  // TODO Right?
	private String      slowAxisName = "y";

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
	public String getFastAxisName() {
		return fastAxisName;
	}
	public void setFastAxisName(String newValue) {
		String oldValue = this.fastAxisName;
		this.fastAxisName = newValue;
		this.pcs.firePropertyChange("fastAxisName", oldValue, newValue);
	}
	@UiHidden
	public String getSlowAxisName() {
		return slowAxisName;
	}
	public void setSlowAxisName(String newValue) {
		String oldValue = this.slowAxisName;
		this.slowAxisName = newValue;
		this.pcs.firePropertyChange("slowAxisName", oldValue, newValue);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((boundingBox == null) ? 0 : boundingBox.hashCode());
		result = prime * result + ((fastAxisName == null) ? 0 : fastAxisName.hashCode());
		result = prime * result + ((slowAxisName == null) ? 0 : slowAxisName.hashCode());
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
		if (fastAxisName == null) {
			if (other.fastAxisName != null)
				return false;
		} else if (!fastAxisName.equals(other.fastAxisName))
			return false;
		if (slowAxisName == null) {
			if (other.slowAxisName != null)
				return false;
		} else if (!slowAxisName.equals(other.slowAxisName))
			return false;
		return true;
	}
}
