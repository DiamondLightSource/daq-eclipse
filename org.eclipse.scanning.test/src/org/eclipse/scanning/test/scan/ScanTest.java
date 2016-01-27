package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;

public class ScanTest extends AbstractScanTest {
	
	
	@Before
	public void setup() throws ScanningException {
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		sservice  = new ScanningServiceImpl();
		connector = new MockScannableConnector();
		gservice  = new PointGeneratorFactory();
		eservice  = new EventServiceImpl();
	}
}
