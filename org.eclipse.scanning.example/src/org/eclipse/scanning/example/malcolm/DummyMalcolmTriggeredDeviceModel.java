package org.eclipse.scanning.example.malcolm;

import org.eclipse.scanning.api.device.models.AbstractDetectorModel;

public class DummyMalcolmTriggeredDeviceModel extends AbstractDetectorModel {
	
	private String malcolmDeviceName = null;
	
	public DummyMalcolmTriggeredDeviceModel() {
		// Do nothing 
	}
	
	public DummyMalcolmTriggeredDeviceModel(String malcolmDeviceName) {
		this.malcolmDeviceName = malcolmDeviceName;
	}

	public void setMalcolmDeviceName(String malcolmDeviceName) {
		this.malcolmDeviceName = malcolmDeviceName;
	}
	
	public String getMalcolmDeviceName() {
		return malcolmDeviceName;
	}

}
