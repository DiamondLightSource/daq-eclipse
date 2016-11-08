package org.eclipse.scanning.test;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Arrays;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.usage.SystemUsage;
import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.example.classregistry.ScanningExampleClassRegistry;
import org.eclipse.scanning.points.classregistry.ScanningAPIClassRegistry;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

/**
 * Doing this works better than using vm:// uris.
 * 
 * Please do not use vm:// as it does not work when many tests are started and stopped
 * in a big unit testing system because each test uses the same in VM broker.
 *
 *
 *  TODO Should have static start of broker or per test start for problematic tests
 * 
 * @author Matthew Gerring.
 *
 */
public class BrokerTest extends TmpTest {

	protected static URI uri;     

	private static BrokerDelegate delegate;

	@BeforeClass
	public final static void startBroker() throws Exception {
		delegate = new BrokerDelegate();
		delegate.start();
		uri      = delegate.getUri();
	}
	
	public final static void setUpNonOSGIActivemqMarshaller() {
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(
				Arrays.asList(new ScanningAPIClassRegistry(),
						new ScanningExampleClassRegistry(),
						new ScanningTestClassRegistry()),
				Arrays.asList(new PointsModelMarshaller())
				));
	}

	@AfterClass
	public final static void stopBroker() throws Exception {
		delegate.stop();
	}



}
