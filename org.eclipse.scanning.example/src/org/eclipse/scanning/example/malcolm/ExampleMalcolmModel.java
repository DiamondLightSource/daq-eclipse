package org.eclipse.scanning.example.malcolm;

import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.IPointGenerator;

public class ExampleMalcolmModel implements IDetectorModel{

	private double exposure;
	private IPointGenerator<?> generator;

	public double getExposureTime() {
		return exposure;
	}

	public void setExposureTime(double exposure) {
		this.exposure = exposure;
	}

	public IPointGenerator<?> getGenerator() {
		return generator;
	}

	public void setGenerator(IPointGenerator<?> generator) {
		this.generator = generator;
	}
}