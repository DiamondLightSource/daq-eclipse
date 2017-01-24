package org.eclipse.scanning.example;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;

public class Services {

	private static IEventService eventService;
	private static IRunnableDeviceService runnableDeviceService;
	private static IScannableDeviceService scannableDeviceService;
	private static IPointGeneratorService pointGeneratorService;

	public static IRunnableDeviceService getRunnableDeviceService() {
		return runnableDeviceService;
	}

	public static void setRunnableDeviceService(IRunnableDeviceService runnableDeviceService) {
		Services.runnableDeviceService = runnableDeviceService;
	}

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		Services.eventService = eventService;
	}
	
	public static IPointGeneratorService getPointGeneratorService() {
		return pointGeneratorService;
	}
	
	public static void setPointGeneratorService(IPointGeneratorService pointGeneratorService) {
		Services.pointGeneratorService = pointGeneratorService;
	}

	public static IScannableDeviceService getScannableDeviceService() {
		return scannableDeviceService;
	}

	public static void setScannableDeviceService(IScannableDeviceService scannableDeviceService) {
		Services.scannableDeviceService = scannableDeviceService;
	}

}
