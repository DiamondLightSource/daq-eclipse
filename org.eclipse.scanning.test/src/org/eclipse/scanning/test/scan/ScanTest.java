package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.scan.IScanner;
import org.eclipse.scanning.api.scan.IScanningService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.sequencer.ScannerServiceImpl;
import org.junit.Before;
import org.junit.Test;

public class ScanTest {

	
	private IScanningService service;
	private IScanner         scanner;
	
	@Before
	public void setup() throws ScanningException {
		service = new ScannerServiceImpl();
		scanner = service.createScanner();
	}
	
	@Test
	public void testSimpleScan() {
		
		
	}
}
