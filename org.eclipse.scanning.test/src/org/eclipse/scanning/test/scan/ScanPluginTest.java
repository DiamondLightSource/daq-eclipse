package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertNotNull;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;

public class ScanPluginTest extends AbstractScanTest {
	
	private static IScanningService  scanningService;
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
		sservice  = ScanPluginTest.scanningService;
		gservice  = ScanPluginTest.generatorService;
		eservice  = ScanPluginTest.eventService;
		
		assertNotNull(scanningService);
		assertNotNull(generatorService);
		assertNotNull(eventService);
		
		if (sservice instanceof ScanningServiceImpl) {
			((ScanningServiceImpl)sservice).setDeviceService(connector);
		}
	}

	public static IScanningService getScanningService() {
		return scanningService;
	}

	public static void setScanningService(IScanningService scanningService) {
		ScanPluginTest.scanningService = scanningService;
	}

	public static IPointGeneratorService getGeneratorService() {
		return generatorService;
	}

	public static void setGeneratorService(IPointGeneratorService generatorService) {
		ScanPluginTest.generatorService = generatorService;
	}
	
	
}
