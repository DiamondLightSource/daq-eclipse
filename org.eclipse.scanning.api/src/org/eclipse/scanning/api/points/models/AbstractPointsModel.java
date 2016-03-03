package org.eclipse.scanning.api.points.models;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.UUID;

import org.eclipse.scanning.api.annotation.UiHidden;
import org.eclipse.scanning.api.points.annot.FieldDescriptor;

/**
 * Abstract base class for scan models, which provides property change support for the convenience of subclasses.
 *
 * @author Colin Palmer
 *
 */
public abstract class AbstractPointsModel implements IScanPathModel {

	@FieldDescriptor(visible=false)
	private String      uniqueKey;

	public AbstractPointsModel() {
		uniqueKey = UUID.randomUUID().toString();
	}

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.addPropertyChangeListener(listener);
	}
	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		this.pcs.removePropertyChangeListener(listener);
	}

	@UiHidden
	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uniqueKey == null) ? 0 : uniqueKey.hashCode());
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
		AbstractPointsModel other = (AbstractPointsModel) obj;
		if (uniqueKey == null) {
			if (other.uniqueKey != null)
				return false;
		} else if (!uniqueKey.equals(other.uniqueKey))
			return false;
		return true;
	}
}
