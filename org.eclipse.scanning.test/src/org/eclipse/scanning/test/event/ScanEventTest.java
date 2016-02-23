package org.eclipse.scanning.test.event;

import java.net.URI;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.junit.Before;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;
import uk.ac.diamond.json.JsonMarshaller;

/**
 * Designed to be run outside OSGi
 * 
 * @author Matthew Gerring
 *
 */
public class ScanEventTest extends AbstractScanEventTest{
	

	@Before
	public void createServices() throws Exception {
		
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		ActivemqConnectorService.setJsonMarshaller(new JsonMarshaller());
		eservice = new EventServiceImpl(new ActivemqConnectorService()); // Do not copy this get the service from OSGi!
		
		// Use in memory broker removes requirement on network and external ActiveMQ process
		// http://activemq.apache.org/how-to-unit-test-jms-code.html
		final URI uri = new URI("vm://localhost?broker.persistent=false");
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		publisher  = eservice.createPublisher(uri, IEventService.SCAN_TOPIC);		
		subscriber = eservice.createSubscriber(uri, IEventService.SCAN_TOPIC);
	}

}
