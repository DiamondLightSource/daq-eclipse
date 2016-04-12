package org.eclipse.scanning.test.event;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.scanning.api.device.IDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.server.servlet.ConfigureServlet;
import org.eclipse.scanning.server.servlet.DeviceServlet;
import org.junit.Test;

public class AbstractRequesterTest {

	protected IDeviceService            dservice;
	protected IEventService             eservice;
	protected IRequester<DeviceRequest> requester;
	protected IResponder<DeviceRequest> responder;
	
	protected void connect(IEventService eservice) throws Exception {
		
		// Use in memory broker removes requirement on network and external ActiveMQ process
		// http://activemq.apache.org/how-to-unit-test-jms-code.html
		final URI uri = new URI("vm://localhost?broker.persistent=false");
		
		DeviceServlet dservlet = new DeviceServlet();
		dservlet.setBroker(uri.toString());
		dservlet.setRequestTopic(IEventService.REQUEST_TOPIC);
		dservlet.setResponseTopic(IEventService.RESPONSE_TOPIC);
		dservlet.connect();
		
		ConfigureServlet cservlet = new ConfigureServlet();
		cservlet.setBroker(uri.toString());
		cservlet.setTopic("org.eclipse.scanning.server.servlet.configure");
		cservlet.connect();
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		requester  = eservice.createRequestor(uri, IEventService.REQUEST_TOPIC, IEventService.RESPONSE_TOPIC);

	}


	@Test
	public void testGetDevices() throws Exception {
		
		DeviceRequest req = new DeviceRequest();
		DeviceRequest res = requester.post(req);
		
		if (res.getDevices().size()<1) throw new Exception("There were no devices found and at least the mandelbrot example should have been!");
	}

	@Test
	public void testGetNamedDeviceModel() throws Exception {
		DeviceRequest req = new DeviceRequest();
		req.setDeviceName("mandelbrot");
		DeviceRequest res = requester.post(req);
		if (res.getDevices().size()!=1) throw new Exception("There were no devices found and at least the mandelbrot example should have been!");
	}
	
	@Test
	public void testInvalidName() throws Exception {
		DeviceRequest req = new DeviceRequest();
		req.setDeviceName("fred");
		DeviceRequest res = requester.post(req);
		if (!res.isEmpty()) throw new Exception("There should have been no devices found!");
	}

	@Test
	public void testMandelModel() throws Exception {
		
		DeviceRequest req = new DeviceRequest();
		req.setDeviceName("mandelbrot");
		DeviceRequest res = requester.post(req);
		
		DeviceInformation<MandelbrotModel> info = (DeviceInformation<MandelbrotModel>)res.getDeviceInformation();
		if (info==null) throw new Exception("There were no devices found and at least the mandelbrot example should have been!");
		
		MandelbrotModel model = info.getModel();
		assertTrue(model.getExposureTime()==0); // We do not set an exposure as part of the test.
		
		// Now we will reconfigure the device
		
	}

}
