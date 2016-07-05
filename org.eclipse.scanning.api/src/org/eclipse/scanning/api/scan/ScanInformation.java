package org.eclipse.scanning.api.scan;

import java.util.Arrays;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 * 
 * Holds state of whole scan. May be used in annotated methods like &#64;ScanStart
 * to provide information about whole scan. Should not be used to hold transient
 * state during the scan. One should be created per run.
 * 
 * @author Matthew Gerring
 *
 */
public class ScanInformation {

	private IRunnableDevice<?> parent;
	private ScanModel          model;
	private int                size;
	private int[]              shape;
	public IRunnableDevice<?> getParent() {
		return parent;
	}
	public void setParent(IRunnableDevice<?> parent) {
		this.parent = parent;
	}
	public ScanModel getModel() {
		return model;
	}
	public void setModel(ScanModel model) {
		this.model = model;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int[] getShape() {
		return shape;
	}
	public void setShape(int[] shape) {
		this.shape = shape;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((model == null) ? 0 : model.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + Arrays.hashCode(shape);
		result = prime * result + size;
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
		ScanInformation other = (ScanInformation) obj;
		if (model == null) {
			if (other.model != null)
				return false;
		} else if (!model.equals(other.model))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (!Arrays.equals(shape, other.shape))
			return false;
		if (size != other.size)
			return false;
		return true;
	}
}
