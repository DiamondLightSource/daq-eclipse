package org.eclipse.scanning.test.event;

import static org.junit.Assert.assertTrue;

import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.event.Constants;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.server.servlet.DeviceServlet;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AbstractRequesterTest extends BrokerTest {

	protected IRunnableDeviceService            dservice;
	protected IEventService             eservice;
	protected IRequester<DeviceRequest> requester;
	protected IResponder<DeviceRequest> responder;
	
	@Before
	public void start() throws Exception {
		
	   	Constants.setNotificationFrequency(200); // Normally 2000
	   	Constants.setReceiveFrequency(100);
	}
	
	@After
	public void stop() throws Exception {
		
    	Constants.setNotificationFrequency(2000); // Normally 2000
    	if (requester!=null) requester.disconnect();
    	if (responder!=null) responder.disconnect();
	}

	protected void connect(IEventService eservice, IRunnableDeviceService dservice) throws Exception {
		
		this.eservice = eservice;
		this.dservice = dservice;
		
		DeviceServlet dservlet = new DeviceServlet();
		dservlet.setBroker(uri.toString());
		dservlet.setRequestTopic(IEventService.REQUEST_TOPIC);
		dservlet.setResponseTopic(IEventService.RESPONSE_TOPIC);
		dservlet.connect();
				
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		requester  = eservice.createRequestor(uri, IEventService.REQUEST_TOPIC, IEventService.RESPONSE_TOPIC);

	}
	
	@Test
	public void simpleSerialize() throws Exception {
		
		DeviceRequest in = new DeviceRequest();
        String json = eservice.getEventConnectorService().marshal(in);
		DeviceRequest back = eservice.getEventConnectorService().unmarshal(json, DeviceRequest.class);
        assertTrue(in.equals(back));
	}

	// @Test
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
	public void testMandelModelConfigure() throws Exception {
		
		DeviceRequest req = new DeviceRequest();
		req.setDeviceName("mandelbrot");
		DeviceRequest res = requester.post(req);
		
		DeviceInformation<MandelbrotModel> info = (DeviceInformation<MandelbrotModel>)res.getDeviceInformation();
		if (info==null) throw new Exception("There were no devices found and at least the mandelbrot example should have been!");
		
		MandelbrotModel model = info.getModel();
		assertTrue(model.getExposureTime()==0); // We do not set an exposure as part of the test.
		assertTrue(info.getState()==DeviceState.IDLE); // We do not set an exposure as part of the test.
		
		// Now we will reconfigure the device
		// and send a new request
		req = new DeviceRequest();
		req.setDeviceName("mandelbrot");
		model.setExposureTime(100);
		model.setEscapeRadius(15);
		req.setDeviceModel(model);
		
		res = requester.post(req);
        
		info = (DeviceInformation<MandelbrotModel>)res.getDeviceInformation();
		if (info==null) throw new Exception("There were no devices found and at least the mandelbrot example should have been!");
		assertTrue(model.getExposureTime()==100); // We do not set an exposure as part of the test.
		assertTrue(model.getEscapeRadius()==15); // We do not set an exposure as part of the test.
		assertTrue(info.getState()==DeviceState.READY); // We do not set an exposure as part of the test.
	}

}
