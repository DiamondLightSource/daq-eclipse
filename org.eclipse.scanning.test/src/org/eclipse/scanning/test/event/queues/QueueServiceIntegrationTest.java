package org.eclipse.scanning.test.event.queues;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.event.queues.QueueControllerService;
import org.eclipse.scanning.event.queues.QueueService;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.MockEventService;
import org.junit.Before;
import org.junit.Test;

public class QueueServiceIntegrationTest extends BrokerTest {
	
	private static IQueueService queueService;
	private static IQueueControllerService queueControl;
	private String qRoot = "fake-queue-root";
	
	//These won't be needed in the plugin test
	private MockEventService mockEvServ = new MockEventService();
	
	private DummyBean dummy;
	
	@Before
	public void setup() throws Exception {
		setupServicesHolder();
		
		//Configure the queue service
		queueService = ServicesHolder.getQueueService();
		queueService.setURI(uri);
		queueService.setQueueRoot(qRoot);
		queueService.init();
		
		//Configure the queue controller service
		queueControl = ServicesHolder.getQueueControllerService();
		queueControl.start();
	}
	
	private void setupServicesHolder() {
		ServicesHolder.setEventService(mockEvServ);
		
		IQueueService qServ = new QueueService();
		ServicesHolder.setQueueService(qServ);
		
		IQueueControllerService qCont = new QueueControllerService();
		ServicesHolder.setQueueControllerService(qCont);
		
	}
	
	@Test
	public void testRunningBean() throws EventException {
		dummy = new DummyBean("Bob", 50);
	}

}
