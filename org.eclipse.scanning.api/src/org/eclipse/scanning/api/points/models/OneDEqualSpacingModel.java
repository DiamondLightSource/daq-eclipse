package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.annotation.MinimumValue;
import org.eclipse.scanning.api.annotation.UiHidden;

/**
 * A model for a scan along a straight line in two-dimensional space, dividing the line into the number of points given
 * in this model.
 *
 * @author Colin Palmer
 *
 */
public class OneDEqualSpacingModel extends AbstractPointsModel implements IBoundingLineModel {

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	private BoundingLine boundingLine;
	private int points = 5;

	private String xName = "x";
	private String yName = "y";

	@Override
	public String getName() {
		return "Equal Spacing";
	}
	@MinimumValue("1")
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		int oldValue = this.points;
		this.points = points;
		this.pcs.firePropertyChange("points", oldValue, points);
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((boundingLine == null) ? 0 : boundingLine.hashCode());
		result = prime * result + points;
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
		OneDEqualSpacingModel other = (OneDEqualSpacingModel) obj;
		if (boundingLine == null) {
			if (other.boundingLine != null)
				return false;
		} else if (!boundingLine.equals(other.boundingLine))
			return false;
		if (points != other.points)
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
