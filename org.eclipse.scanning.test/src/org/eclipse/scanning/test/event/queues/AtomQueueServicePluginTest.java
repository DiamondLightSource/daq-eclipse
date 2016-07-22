package org.eclipse.scanning.test.event.queues;

import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.event.queues.AtomQueueService;
import org.eclipse.scanning.test.event.queues.util.EventInfrastructureFactoryService;

/**
 * Test of the concrete implementation {@link AtomQueueService} of the {@link IQueueService}.
 * Tests located in {@link AbstractQueueServiceTest}.
 * 
 * This test uses OSGi.
 * 
 * @author Michael Wharmby
 *
 */
public class AtomQueueServicePluginTest extends AbstractQueueServiceTest {
			
	private static IQueueService queueService;
	private EventInfrastructureFactoryService infrastructureServ;

	public static IQueueService getQueueService() {
		return queueService;
	}

	public synchronized void setQueueService(IQueueService queueService) {
		AtomQueueServicePluginTest.queueService = queueService; //TODO This should be using QueueServiceHolder!
	}
	
	public synchronized void unsetQueueService() {
		queueService = null;
	}
	
	protected void localSetup() throws Exception {
		//Set up Event service infrastructure
		infrastructureServ = new EventInfrastructureFactoryService();
		infrastructureServ.start(false);
		
		//Configure IQueueService as necessary using OSGi
		qServ.setQueueRoot(qRoot);
		qServ.setURI(uri);

		//All set? Let's go!
		qServ.init();
	}
	
	protected void localTearDown() throws Exception {
		infrastructureServ.stop();
	}
	

}
