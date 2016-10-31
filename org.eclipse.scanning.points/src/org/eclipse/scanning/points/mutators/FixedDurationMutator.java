package org.eclipse.scanning.points.mutators;

import org.eclipse.scanning.api.points.IMutator;
import org.eclipse.scanning.points.ScanPointGeneratorFactory;
import org.eclipse.scanning.points.ScanPointGeneratorFactory.JythonObjectFactory;

public class FixedDurationMutator implements IMutator {

	private double duration;

	public FixedDurationMutator(double duration) {
		this.duration = duration;
	}
	
	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	@Override
	public Object getMutatorAsJythonObject() {
		JythonObjectFactory fixedMutatorFactory = ScanPointGeneratorFactory.JFixedDurationMutatorFactory();
		return fixedMutatorFactory.createObject(duration);
	}
}