package org.eclipse.scanning.test.event;

import java.net.URI;

import org.eclipse.scanning.api.event.IEventService;
import org.junit.Before;

/**
 * Scan Event Test but with OSGi container.
 * @author Matthew Gerring
 *
 */
public class ScanEventPluginTest extends AbstractScanEventTest{

    private static IEventService service;

	public static IEventService getService() {
		return service;
	}

	public static void setService(IEventService service) {
		ScanEventPluginTest.service = service;
	}

	@Before
	public void createServices() throws Exception {
		
		final URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");	
		
		eservice = service;
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		publisher  = eservice.createPublisher(uri, IEventService.SCAN_TOPIC);		
		subscriber = eservice.createSubscriber(uri, IEventService.SCAN_TOPIC);
	}
}
