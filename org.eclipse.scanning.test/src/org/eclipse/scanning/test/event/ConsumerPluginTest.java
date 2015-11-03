package org.eclipse.scanning.test.event;

import java.net.URI;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.junit.After;
import org.junit.Before;

public class ConsumerPluginTest extends AbstractConsumerTest {

	
    private static IEventService service;

	public static IEventService getService() {
		return service;
	}

	public static void setService(IEventService service) {
		ConsumerPluginTest.service = service;
	}

	@Before
	public void createServices() throws Exception {
		
		final URI uri = new URI("tcp://sci-serv5.diamond.ac.uk:61616");	
		
		eservice = ConsumerPluginTest.service;
		
		// We use the long winded constructor because we need to pass in the connector.
		// In production we would normally 
		submitter  = eservice.createSubmitter(uri, IEventService.SUBMISSION_QUEUE);
		consumer   = eservice.createConsumer(uri, IEventService.SUBMISSION_QUEUE, IEventService.STATUS_QUEUE, IEventService.STATUS_TOPIC, IEventService.HEARTBEAT_TOPIC, IEventService.KILL_TOPIC, null);
		consumer.setName("Test Consumer");
		consumer.clearStatusQueue();
	}
	
	@After
	public void dispose() throws EventException {
		submitter.disconnect();
		consumer.clearStatusQueue();
		consumer.disconnect();
	}

}
