package org.eclipse.scanning.test.event.queues;

import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.event.queues.AtomQueueService;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.test.event.queues.util.EventInfrastructureFactoryService;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test of the concrete implementation {@link AtomQueueService} of the {@link IQueueService}.
 * Tests located in {@link AbstractQueueServiceTest}.
 * @author wnm24546
 *
 */
public class AtomQueueServiceTest extends AbstractQueueServiceTest {
	
	@BeforeClass
	public static void initialise() throws Exception {
		//Set up IEventService
		infrastructureServ = new EventInfrastructureFactoryService();
		infrastructureServ.start(true);
		QueueServicesHolder.setEventService(infrastructureServ.getEventService());
	}
	
	@AfterClass
	public static void shutdown() throws Exception {
		infrastructureServ.stop();
	}
	
	protected void localSetup() throws Exception {
		//Create QueueService with 2 argument constructor for tests
		uri = infrastructureServ.getURI();
		qServ = new AtomQueueService(qRoot, uri);

		//All set? Let's go!
		qServ.init();
	}
	
	protected void localTearDown() throws Exception {
		//Nothing to do
	}
	
}
