package org.eclipse.scanning.example.malcolm;

import org.eclipse.scanning.api.points.IPointGenerator;

public class ExampleMalcolmModel {

	private double exposure;
	private IPointGenerator<?> generator;

	public double getExposure() {
		return exposure;
	}

	public void setExposure(double exposure) {
		this.exposure = exposure;
	}

	public IPointGenerator<?> getGenerator() {
		return generator;
	}

	public void setGenerator(IPointGenerator<?> generator) {
		this.generator = generator;
	}
}