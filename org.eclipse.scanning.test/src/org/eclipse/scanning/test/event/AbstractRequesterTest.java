package org.eclipse.scanning.test.event;

import org.eclipse.scanning.api.device.IDeviceService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.core.IResponder;
import org.eclipse.scanning.api.event.scan.DeviceRequest;
import org.junit.Test;

public class AbstractRequesterTest {

	protected IDeviceService            dservice;
	protected IEventService             eservice;
	protected IRequester<DeviceRequest> requester;
	protected IResponder<DeviceRequest> responder;


	@Test
	public void testSimpleRequest() throws Exception {
		
		DeviceRequest req = new DeviceRequest();
		DeviceRequest res = requester.post(req);
		
		if (res.getDevices().size()<1) throw new Exception("There were no devices found and at least the mandelbrot example should have been!");
	}


}
