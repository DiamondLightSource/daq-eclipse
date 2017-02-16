package org.eclipse.scanning.test.event.queues;

import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.event.queues.QueueControllerService;
import org.eclipse.scanning.event.queues.QueueService;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.junit.BeforeClass;

import org.eclipse.scanning.connector.activemq.ActivemqConnectorService;

public class QueueServiceIntegrationTest extends QueueServiceIntegrationPluginTest {
	

	@BeforeClass
	public static void fakeOSGiSetup() {
		setUpNonOSGIActivemqMarshaller();
		
		IEventService evServ =  new EventServiceImpl(new ActivemqConnectorService());
		ServicesHolder.setEventService(evServ);
		
		IQueueService qServ = new QueueService();
		ServicesHolder.setQueueService(qServ);
		
		IQueueControllerService qCont = new QueueControllerService();
		ServicesHolder.setQueueControllerService(qCont);
		
	}

}
