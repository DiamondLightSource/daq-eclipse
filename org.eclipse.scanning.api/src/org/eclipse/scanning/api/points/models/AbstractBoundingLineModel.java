package org.eclipse.scanning.api.points.models;

import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.annotation.UiHidden;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;

public class AbstractBoundingLineModel extends AbstractPointsModel implements IBoundingLineModel {
	
	@FieldDescriptor(editable=false) // We edit this with a popup.
	private BoundingLine boundingLine;
	
	private String xName = "stage_x";
	private String yName = "stage_y";
	@UiHidden
	public String getxName() {
		return xName;
	}
	public void setxName(String xName) {
		String oldValue = this.xName;
		this.xName = xName;
		this.pcs.firePropertyChange("xName", oldValue, xName);
	}
	@UiHidden
	public String getyName() {
		return yName;
	}
	public void setyName(String yName) {
		String oldValue = this.yName;
		this.yName = yName;
		this.pcs.firePropertyChange("yName", oldValue, yName);
	}
	@UiHidden
	@Override
	public List<String> getScannableNames() {
		return Arrays.asList(xName, yName);
	}
	
	@Override
	@UiHidden
	public BoundingLine getBoundingLine() {
		return boundingLine;
	}
	@Override
	public void setBoundingLine(BoundingLine boundingLine) {
		BoundingLine oldValue = this.boundingLine;
		this.boundingLine = boundingLine;
		this.pcs.firePropertyChange("boundingLine", oldValue, boundingLine);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((boundingLine == null) ? 0 : boundingLine.hashCode());
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
		AbstractBoundingLineModel other = (AbstractBoundingLineModel) obj;
		if (boundingLine == null) {
			if (other.boundingLine != null)
				return false;
		} else if (!boundingLine.equals(other.boundingLine))
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
