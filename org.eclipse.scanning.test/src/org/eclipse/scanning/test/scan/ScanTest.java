package org.eclipse.scanning.test.scan;

import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.PointGeneratorFactory;
import org.eclipse.scanning.sequencer.DeviceServiceImpl;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.eclipse.scanning.test.scan.mock.MockScannableConnector;
import org.eclipse.scanning.test.scan.mock.MockScannableModel;
import org.eclipse.scanning.test.scan.mock.MockWritableDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandelbrotDetector;
import org.eclipse.scanning.test.scan.mock.MockWritingMandlebrotModel;
import org.junit.Before;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class ScanTest extends AbstractScanTest {
	
	
	@Before
	public void setup() throws ScanningException {
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		connector = new MockScannableConnector();
		sservice  = new DeviceServiceImpl(connector);
		DeviceServiceImpl impl = (DeviceServiceImpl)sservice;
		impl._register(MockDetectorModel.class, MockWritableDetector.class);
		impl._register(MockWritingMandlebrotModel.class, MockWritingMandelbrotDetector.class);
		
		gservice  = new PointGeneratorFactory();
		eservice  = new EventServiceImpl(new ActivemqConnectorService());
	}
}
