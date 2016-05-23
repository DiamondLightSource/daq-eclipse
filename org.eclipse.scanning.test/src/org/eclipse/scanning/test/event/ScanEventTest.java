package org.eclipse.scanning.test.event;

import java.net.URI;

import org.eclipse.dawnsci.json.MarshallerService;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.points.serialization.PointsModelMarshaller;
import org.junit.Before;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

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
		ActivemqConnectorService.setJsonMarshaller(new MarshallerService(new PointsModelMarshaller()));
		eservice = new EventServiceImpl(new ActivemqConnectorService()); // Do not copy this get the service from OSGi!
				
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		publisher  = eservice.createPublisher(uri, IEventService.SCAN_TOPIC);		
		subscriber = eservice.createSubscriber(uri, IEventService.SCAN_TOPIC);
	}

}
