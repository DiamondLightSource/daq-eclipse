package org.eclipse.scanning.example.malcolm;

import org.eclipse.scanning.api.device.models.MalcolmModel;

public class ExampleMalcolmModel extends MalcolmModel {

	private float exposure;

	public float getExposure() {
		return exposure;
	}

	public void setExposure(float exposure) {
		this.exposure = exposure;
	}

}