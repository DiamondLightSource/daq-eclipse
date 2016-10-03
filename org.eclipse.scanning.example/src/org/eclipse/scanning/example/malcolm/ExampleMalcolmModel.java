package org.eclipse.scanning.example.malcolm;

import org.eclipse.scanning.api.points.IPointGenerator;

public class ExampleMalcolmModel {

	private float exposure;
	private IPointGenerator<?> generator;

	public float getExposure() {
		return exposure;
	}

	public void setExposure(float exposure) {
		this.exposure = exposure;
	}

	public IPointGenerator<?> getGenerator() {
		return generator;
	}

	public void setGenerator(IPointGenerator<?> generator) {
		this.generator = generator;
	}
}