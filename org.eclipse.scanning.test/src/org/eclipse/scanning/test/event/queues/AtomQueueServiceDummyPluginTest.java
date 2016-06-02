package org.eclipse.scanning.test.event.queues;

import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.event.queues.AtomQueueService;
import org.junit.Before;

/**
 * Test of the concrete implementation {@link AtomQueueService} of the {@link IQueueService}.
 * Tests located in {@link AbstractQueueServiceTest}.
 * 
 * This test uses OSGi.
 * 
 * @author Michael Wharmby
 *
 */
public class AtomQueueServiceDummyPluginTest extends AbstractQueueServiceTest {
			
	private static IQueueService queueService;

	public static IQueueService getQueueService() {
		return queueService;
	}

	public synchronized void setQueueService(IQueueService queueService) {
		AtomQueueServiceDummyPluginTest.queueService = queueService; //TODO This should be using QueueServiceHolder!
	}
	
	public synchronized void unsetQueueService() {
		queueService = null;
	}
	
	@Before
	public void createService() throws Exception {
		qServ = AtomQueueServiceDummyPluginTest.queueService;
		
		//Configure IQueueService as necessary using OSGi
		qServ.setQueueRoot(qRoot);
		qServ.setURI(uri);
		
		//All set? Let's go!
		qServ.init();
	}

}
