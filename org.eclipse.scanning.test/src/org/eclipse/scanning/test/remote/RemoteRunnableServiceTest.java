package org.eclipse.scanning.test.remote;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IDisconnectable;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.scan.event.IPositioner;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.event.remote.RemoteServiceFactory;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.scannable.MockScannableConnector;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
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

public class RemoteRunnableServiceTest extends BrokerTest {

	private static  IRunnableDeviceService    dservice;
	private static  IEventService             eservice;
	private static AbstractResponderServlet<?>  dservlet, pservlet;

	@BeforeClass
	public static void createServices() throws Exception {
		
		System.out.println("Create Services");
		RemoteServiceFactory.setTimeout(1, TimeUnit.MINUTES); // Make test easier to debug.

		// Set up stuff because we are not in OSGi with a test
		// DO NOT COPY TESTING ONLY
		dservice = new RunnableDeviceServiceImpl(new MockScannableConnector());
		MandelbrotDetector mandy = new MandelbrotDetector();
		final DeviceInformation<MandelbrotModel> info = new DeviceInformation<MandelbrotModel>(); // This comes from extension point or spring in the real world.
		info.setName("mandelbrot");
		info.setLabel("Example Mandelbrot");
		info.setDescription("Example mandelbrot device");
		info.setId("org.eclipse.scanning.example.detector.mandelbrotDetector");
		info.setIcon("org.eclipse.scanning.example/icon/mandelbrot.png");
		mandy.setDeviceInformation(info);
		((RunnableDeviceServiceImpl)dservice)._register("mandelbrot", mandy);
				
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		eservice = new EventServiceImpl(new ActivemqConnectorService()); // Do not copy this get the service from OSGi!

		Services.setRunnableDeviceService(dservice);
		Services.setEventService(eservice);
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

	@AfterClass
	public static void cleanup() throws EventException {
		dservlet.disconnect();
		pservlet.disconnect();
	}
	
	private         IRunnableDeviceService    rservice;

	@Before
	public void createService() throws EventException {
		rservice = eservice.createRemoteService(uri, IRunnableDeviceService.class);
		System.out.println("Made remote service "+rservice+" ... "+rservice.getClass());
	}
	
	@After
	public void disposeService() throws EventException {
		((IDisconnectable)rservice).disconnect();
	}

	@Test
	public void checkNotNull() throws Exception {
		assertTrue(rservice!=null);
	}
	
	@Test
	public void testDrivePositioner() throws Exception {
		
		IPositioner pos1 = dservice.createPositioner();
		IPositioner pos2 = rservice.createPositioner();
		
		// Set them up the same.
		pos1.setPosition(new MapPosition("test", 0, 0));
		pos2.setPosition(new MapPosition("test", 0, 0));
		
		pos1.setPosition(new MapPosition("test", 0, Math.PI));
		assertTrue(pos2.getPosition().getValue("test")==Math.PI);
	}

	@Test
	public void testAbort() throws Exception {
        
		IPositioner pos1 = dservice.createPositioner();
		if (rservice==null) rservice = eservice.createRemoteService(uri, IRunnableDeviceService.class);
		IPositioner pos2 = rservice.createPositioner();
		pos1.setPosition(new MapPosition("x", 0, 0));
		pos2.setPosition(new MapPosition("x", 0, 0));

		pos1.setPosition(new MapPosition("x", 0, 10)); // Should take 1 seconds
		pos2.abort();

		assertTrue(pos2.getPosition().getValue("x")==10); // Should reach 10 despite abort because setPosition is blocking.

	}
	
	// TODO Why does this pass locally an not on travis?
	//@Test
	public void testDeviceNames() throws Exception {
		
		Collection<String> names1 = dservice.getRunnableDeviceNames();
		Collection<String> names2 = rservice.getRunnableDeviceNames();
		assertTrue(names1!=null);
		assertTrue(names2!=null);
		assertTrue(names1.containsAll(names2));
		assertTrue(names2.containsAll(names1));
	}

	@Test
	public void testGetRunnableDevice() throws Exception {
		
		IRunnableDevice<?> dev1 = dservice.getRunnableDevice("mandelbrot");
		IRunnableDevice<?> dev2 = rservice.getRunnableDevice("mandelbrot");
		assertTrue(dev1!=null);
		assertTrue(dev2!=null);
		
		assertTrue(dev1.getLevel()==(dev2.getLevel()));
		assertTrue(dev1.getDeviceState().equals(dev2.getDeviceState()));
	
		final Object mod1 = dev1.getModel();
		final Object mod2 = dev2.getModel();
		assertTrue(mod1!=null);
		assertTrue(mod2!=null);
		assertTrue(mod1.equals(mod2));
	}

	
}
