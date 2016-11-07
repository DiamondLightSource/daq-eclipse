package org.eclipse.scanning.api.device.models;

import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.points.IPointGenerator;

public class AbstractMalcolmModel extends AbstractDetectorModel {


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((generator == null) ? 0 : generator.hashCode());
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
		AbstractMalcolmModel other = (AbstractMalcolmModel) obj;
		if (generator == null) {
			if (other.generator != null)
				return false;
		} else if (!generator.equals(other.generator))
			return false;
		return true;
	}

	/**
	 * TODO Why are generators in the malcolm model?
	 */
	@FieldDescriptor(visible=false)
	private IPointGenerator<?> generator;
	
	public IPointGenerator<?> getGenerator() {
		return generator;
	}

	public void setGenerator(IPointGenerator<?> generator) {
		this.generator = generator;
	}
	

}
