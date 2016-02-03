package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.ScanningServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.junit.Before;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class ScanTest extends AbstractScanTest {
	
	
	@Before
	public void setup() throws ScanningException {
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		connector = new MockScannableConnector();
		sservice  = new ScanningServiceImpl(connector);
		gservice  = new PointGeneratorFactory();
		eservice  = new EventServiceImpl(new ActivemqConnectorService());
	}
}
