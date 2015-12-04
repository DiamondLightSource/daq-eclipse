package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.points.IGeneratorService;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;

public class ScanPluginTest extends AbstractScanTest {
	
	private static IScanningService  scanningService;
	private static IGeneratorService generatorService;

	@Before
	public void setup() throws ScanningException {
		sservice  = ScanPluginTest.scanningService;
		gservice  = ScanPluginTest.generatorService;
		connector = new MockScannableConnector();
	}

	public static IScanningService getScanningService() {
		return scanningService;
	}

	public static void setScanningService(IScanningService scanningService) {
		ScanPluginTest.scanningService = scanningService;
	}

	public static IGeneratorService getGeneratorService() {
		return generatorService;
	}

	public static void setGeneratorService(IGeneratorService generatorService) {
		ScanPluginTest.generatorService = generatorService;
	}
	
	
}
