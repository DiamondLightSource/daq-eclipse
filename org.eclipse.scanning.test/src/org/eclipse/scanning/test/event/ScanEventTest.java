package org.eclipse.scanning.test.event;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.HeartbeatEvent;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.eclipse.scanning.api.event.scan.State;
import org.eclipse.scanning.event.EventServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

/**
 * Designed to be run outside OSGi
 * 
 * @author fcp94556
 *
 */
public class ScanEventTest extends AbstractScanEventTest{
	

	@Before
	public void createServices() throws Exception {
		
		eservice = new EventServiceImpl(); // Do not copy this get the service from OSGi!
		
		final URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");	
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		publisher  = eservice.createPublisher(uri, IEventService.SCAN_TOPIC, new ActivemqConnectorService());		
		subscriber = eservice.createSubscriber(uri, IEventService.SCAN_TOPIC, new ActivemqConnectorService());
	}
	
	@After
	public void dispose() throws EventException {
		publisher.disconnect();
		subscriber.disconnect();
	}

}
