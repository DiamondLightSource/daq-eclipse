package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.annotation.MinimumValue;
import org.eclipse.scanning.api.annotation.UiHidden;

/**
 * A model for a scan along a straight line in two-dimensional space, starting at the beginning of the line and moving
 * in steps of the size given in this model.
 *
 * @author Colin Palmer
 *
 */
public class OneDStepModel extends AbstractPointsModel implements IBoundingLineModel {

    private String xName = "x";
    private String yName = "y";
    
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
	private double step = 1;

	private String xName = "x";
	private String yName = "y";

	@Override
	public String getName() {
		return "Step";
	}
	@MinimumValue("0")
	public double getStep() {
		return step;
	}
	public void setStep(double step) {
		double oldValue = this.step;
		this.step = step;
		this.pcs.firePropertyChange("step", oldValue, step);
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
<<<<<<< HEAD
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
=======
    public String getxName() {
        return xName;
    }
    public void setxName(String xName) {
        String oldValue = this.xName;
        this.xName = xName;
        this.pcs.firePropertyChange("xName", oldValue, xName);
    }
    public String getyName() {
        return yName;
    }
    public void setyName(String yName) {
        String oldValue = this.yName;
        this.yName = yName;
        this.pcs.firePropertyChange("yName", oldValue, yName);
    }
>>>>>>> Refactor OneDStepGenerator to use LineGenerator, adjust *Model
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((boundingLine == null) ? 0 : boundingLine.hashCode());
		long temp;
		temp = Double.doubleToLongBits(step);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		OneDStepModel other = (OneDStepModel) obj;
		if (boundingLine == null) {
			if (other.boundingLine != null)
				return false;
		} else if (!boundingLine.equals(other.boundingLine))
			return false;
		if (Double.doubleToLongBits(step) != Double.doubleToLongBits(other.step))
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
