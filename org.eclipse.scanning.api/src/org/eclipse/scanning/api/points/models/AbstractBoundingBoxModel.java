package org.eclipse.scanning.api.points.models;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.annotation.UiHidden;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.annotation.ui.EditType;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;


/**
 * A model for a scan within a rectangular box in two-dimensional space.
 * <p>
 * This abstract class defines the box size and the names of the two axes.
 *
 * @author Colin Palmer
 *
 */
public abstract class AbstractBoundingBoxModel extends AbstractPointsModel implements IBoundingBoxModel {

	@FieldDescriptor(edit=EditType.COMPOUND, hint="The bounding box is automatically calculated from the scan regions shown in the main plot.") // We edit this with a popup.
	private BoundingBox boundingBox;

	@FieldDescriptor(label="Fast Axis", device=DeviceType.SCANNABLE, hint="The name of the scannable in the fast direction, for instance 'stage_x'.") // TODO Right?
	private String      fastAxisName = "stage_x";

	@FieldDescriptor(label="Slow Axis", device=DeviceType.SCANNABLE, hint="The name of the scannable in the fast direction, for instance 'stage_y'.")  // TODO Right?
	private String      slowAxisName = "stage_y";

	protected AbstractBoundingBoxModel() {
		super();
	}
	
	protected AbstractBoundingBoxModel(String fastName, String slowName, BoundingBox box) {
		super();
		this.fastAxisName = fastName;
		this.slowAxisName = slowName;
		this.boundingBox  = box;
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
		boundingBox.setFastAxisName(getFastAxisName());
		boundingBox.setSlowAxisName(getSlowAxisName());
		this.pcs.firePropertyChange("boundingBox", oldValue, newValue);
	}

	@UiHidden
	public String getFastAxisName() {
		return fastAxisName;
	}
	public void setFastAxisName(String newValue) {
		String oldValue = this.fastAxisName;
		this.fastAxisName = newValue;
		if (boundingBox!=null) boundingBox.setFastAxisName(fastAxisName);
		this.pcs.firePropertyChange("fastAxisName", oldValue, newValue);
	}
	@UiHidden
	public String getSlowAxisName() {
		return slowAxisName;
	}
	public void setSlowAxisName(String newValue) {
		String oldValue = this.slowAxisName;
		this.slowAxisName = newValue;
		if (boundingBox!=null) boundingBox.setSlowAxisName(slowAxisName);
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

	@UiHidden
	@Override
	public List<String> getScannableNames() {
		return Arrays.asList(getFastAxisName(), getSlowAxisName());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()+" [boundingBox=" + boundingBox + ", fastAxisName=" + fastAxisName
				+ ", slowAxisName=" + slowAxisName + "]";
	}

}
