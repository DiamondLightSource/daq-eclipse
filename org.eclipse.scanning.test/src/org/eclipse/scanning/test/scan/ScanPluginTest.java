package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertNotNull;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;

public class ScanPluginTest extends AbstractScanTest {
	
	private static IRunnableDeviceService  scanningService;
	private static IPointGeneratorService generatorService;
	private static IEventService     eventService;

	public static IEventService getEventService() {
		return eventService;
	}

	public static void setEventService(IEventService eventService) {
		ScanPluginTest.eventService = eventService;
	}

	@Before
	public void setup() throws ScanningException {
		connector = new MockScannableConnector();
		dservice  = ScanPluginTest.scanningService;
		gservice  = ScanPluginTest.generatorService;
		eservice  = ScanPluginTest.eventService;
		
		assertNotNull(scanningService);
		assertNotNull(generatorService);
		assertNotNull(eventService);
		
		if (dservice instanceof RunnableDeviceServiceImpl) {
			((RunnableDeviceServiceImpl)dservice).setDeviceConnectorService(connector);
		}
	}

	public static IRunnableDeviceService getScanningService() {
		return scanningService;
	}

	public static void setScanningService(IRunnableDeviceService scanningService) {
		ScanPluginTest.scanningService = scanningService;
	}

	public static IPointGeneratorService getGeneratorService() {
		return generatorService;
	}

	public static void setGeneratorService(IPointGeneratorService generatorService) {
		ScanPluginTest.generatorService = generatorService;
	}
	
	
}
