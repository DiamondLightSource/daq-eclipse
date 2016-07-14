package org.eclipse.scanning.test.event.queues;

import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.event.queues.AtomQueueService;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.test.event.queues.util.EventInfrastructureFactoryService;

/**
 * Test of the concrete implementation {@link AtomQueueService} of the {@link IQueueService}.
 * Tests located in {@link AbstractQueueServiceTest}.
 * @author wnm24546
 *
 */
public class AtomQueueServiceTest extends AbstractQueueServiceTest {
	
	protected void localSetup() throws Exception {
		//Set up IEventService
		infrastructureServ = new EventInfrastructureFactoryService();
		infrastructureServ.start(true);
		QueueServicesHolder.setEventService(infrastructureServ.getEventService());
		uri = infrastructureServ.getURI();
		
		//Create QueueService with 2 argument constructor for tests
		qServ = new AtomQueueService(qRoot, uri);

		//All set? Let's go!
		qServ.init();
	}
	
	protected void localTearDown() throws Exception {
		infrastructureServ.stop();
	}
	
}
