package org.eclipse.scanning.malcolm.core;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.malcolm.connector.IMalcolmConnectorService;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;

public class Services {
	
	private static IRunnableDeviceService runnableDeviceService;
	
	private static IMalcolmConnectorService<MalcolmMessage> connectorService; 

	public static IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public static void setRunnableDeviceService(IRunnableDeviceService runnableDeviceService) {
		Services.runnableDeviceService = runnableDeviceService;
	}
	
	public static IMalcolmConnectorService<MalcolmMessage> getConnectorService() {
		return Services.connectorService;
	}
	
	public static void setConnectorService(IMalcolmConnectorService<MalcolmMessage> connectorService) {
		Services.connectorService = connectorService;
	}

}
