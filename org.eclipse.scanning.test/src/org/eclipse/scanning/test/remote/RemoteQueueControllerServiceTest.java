package org.eclipse.scanning.test.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IDisconnectable;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.event.EventServiceImpl;
import org.eclipse.scanning.event.queues.Queue;
import org.eclipse.scanning.event.queues.QueueControllerService;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.event.remote.RemoteServiceFactory;
import org.eclipse.scanning.server.servlet.Services;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.diamond.daq.activemq.connector.ActivemqConnectorService;

public class RemoteQueueControllerServiceTest extends BrokerTest {

	private static IQueueControllerService      qservice;
	private        IQueueControllerService      rservice;
	private static IEventService                eservice;

	@BeforeClass
	public static void createServices() throws Exception {
		
		System.out.println("Create Services");
		RemoteServiceFactory.setTimeout(1, TimeUnit.MINUTES); // Make test easier to debug.
		
		// We wire things together without OSGi here 
		// DO NOT COPY THIS IN NON-TEST CODE!
		setUpNonOSGIActivemqMarshaller();
		eservice = new EventServiceImpl(new ActivemqConnectorService()); // Do not copy this get the service from OSGi!

		// Set up stuff because we are not in OSGi with a test
		// DO NOT COPY TESTING ONLY

		Services.setEventService(eservice);
		ServicesHolder.setEventService(eservice);
		System.out.println("Set connectors");
		
		String jqID = "mock-job-queue";
		String jqSubmQ = jqID+IQueue.SUBMISSION_QUEUE_SUFFIX;
		String aqID = "mock-active-queue";
		String aqSubmQ = aqID+IQueue.SUBMISSION_QUEUE_SUFFIX;
		IQueue<QueueBean> mockJobQ = new Queue<>(jqID, uri);
		IQueue<QueueAtom> mockActiveQ = new Queue<>(aqID, uri);
		MockQueueService mockQServ = new MockQueueService(mockJobQ, mockActiveQ);
		mockQServ.setCommandTopicName("mock-cmd-topic");
		//Clear queues to avoid class cast errors (a StatusBean is prepopulated for another test elsewhere...)
		mockQServ.getJobQueue().clearQueues();
		mockQServ.getActiveQueue(aqID).clearQueues();
		ServicesHolder.setQueueService(mockQServ);
		//Check that our job- and active-consumers do values which are different IDs
		assertFalse("job-queue consumer ID should not be null", mockQServ.getQueue(jqID).getConsumerID() == null);
		assertFalse("active-queue consumer ID should not be null", mockQServ.getQueue(aqID).getConsumerID() == null);
		assertFalse("job- & active-queue IDs identical", mockQServ.getQueue(jqID).getConsumerID() == mockQServ.getQueue(aqID).getConsumerID());
		
		//A bit of boilerplate to start the service under test
		qservice = new QueueControllerService();
		qservice.init();

	}
	
	@Before
	public void createService() throws EventException {
		rservice = eservice.createRemoteService(uri, IQueueControllerService.class);
	}
	
	@After
	public void disposeService() throws EventException {
		((IDisconnectable)rservice).disconnect();
	}

	@Test
	public void checkNotNull() throws Exception {
		assertNotNull(rservice);
	}
	

}
