package org.eclipse.scanning.test.remote;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IDisconnectable;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.event.remote.RemoteServiceFactory;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.server.servlet.AbstractResponderServlet;
import org.eclipse.scanning.server.servlet.DeviceServlet;
import org.eclipse.scanning.server.servlet.PositionerServlet;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class RemoteScannableServiceTest extends BrokerTest {

	private static IScannableDeviceService      cservice;
	private static IEventService                eservice;
	private static AbstractResponderServlet<?>  dservlet, pservlet;

	@BeforeClass
	public static void createServices() throws Exception {
		
		System.out.println("Create Services");
		RemoteServiceFactory.setTimeout(1, TimeUnit.MINUTES); // Make test easier to debug.

		// Set up stuff because we are not in OSGi with a test
		// DO NOT COPY TESTING ONLY
		cservice = new MockScannableConnector();
				
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		eservice = new EventServiceImpl(new ActivemqConnectorService()); // Do not copy this get the service from OSGi!

		Services.setEventService(eservice);
		Services.setConnector(cservice);
		System.out.println("Set connectors");
		
		dservlet = new DeviceServlet();
		dservlet.setBroker(uri.toString());
		dservlet.setRequestTopic(IEventService.DEVICE_REQUEST_TOPIC);
		dservlet.setResponseTopic(IEventService.DEVICE_RESPONSE_TOPIC);
		dservlet.connect();
		
		pservlet = new PositionerServlet();
		pservlet.setBroker(uri.toString());
		pservlet.setRequestTopic(IEventService.POSITIONER_REQUEST_TOPIC);
		pservlet.setResponseTopic(IEventService.POSITIONER_RESPONSE_TOPIC);
		pservlet.connect();
		System.out.println("Made Servlets");

	}
	
	private        IScannableDeviceService      rservice;
	
	@Before
	public void createService() throws EventException {
		rservice = eservice.createRemoteService(uri, IScannableDeviceService.class);
		System.out.println("Made remote service "+rservice+" ... "+rservice.getClass());
	}
	
	@After
	public void disposeService() throws EventException {
		((IDisconnectable)rservice).disconnect();
	}

	
	@AfterClass
	public static void cleanup() throws EventException {
		dservlet.disconnect();
		pservlet.disconnect();
	}

	@Test
	public void checkNotNull() throws Exception {
		assertTrue(rservice!=null);
	}
	
	@Test
	public void testScannableNames() throws Exception {
		
		Collection<String> names1 = cservice.getScannableNames();
		Collection<String> names2 = rservice.getScannableNames();
		assertTrue(names1!=null);
		assertTrue(names2!=null);
		assertTrue(names1.containsAll(names2));
		assertTrue(names2.containsAll(names1));
	}

	@Test
	public void testGetScannable() throws Exception {
		
		IScannable<Double> xNex1 = cservice.getScannable("xNex");
		IScannable<Double> xNex2 = rservice.getScannable("xNex");
		assertTrue(xNex1!=null);
		assertTrue(xNex2!=null);
	}
	
	@Test
	public void testScannablePositionLocal() throws Exception {
		
		IScannable<Double> xNex1 = cservice.getScannable("xNex");
		IScannable<Double> xNex2 = rservice.getScannable("xNex");
		scannableValues(xNex1, xNex2);
	}
	
	@Test
	public void testScannablePositionRemote() throws Exception {
		
		IScannable<Double> xNex1 = cservice.getScannable("xNex");
		IScannable<Double> xNex2 = rservice.getScannable("xNex");
		scannableValues(xNex2, xNex1);
	}


	private void scannableValues(IScannable<Double> setter, IScannable<Double> getter) throws Exception {
		assertTrue(setter!=null);
		assertTrue(getter!=null);
		
		for (int i = 0; i < 10; i++) {
			setter.setPosition(i*10d);
			System.out.println("Set "+setter.getName()+" to value "+(i*10d)+" It's value is "+setter.getPosition());
			assertTrue(getter.getPosition()==(i*10d));
			System.out.println("The value of "+setter.getName()+" was also "+getter.getPosition());
		}
	}

}
