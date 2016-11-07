package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IResponseCreator;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;
import org.eclipse.scanning.api.event.queues.remote.QueueRequestType;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueControllerService;
import org.eclipse.scanning.event.queues.QueueService;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.event.queues.remote.QueueResponseCreator;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.MockConsumer;
import org.eclipse.scanning.test.event.queues.mocks.MockEventService;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueueResponseProcessTest {
	
	private DummyBean submDummy, statDummy;
	private MockPublisher<QueueRequest> mockPub;
	private MockConsumer<Queueable> mockCons = new MockConsumer<>();
	private MockEventService mockEvServ;
	
	private IQueueService qServ;
	private IQueueControllerService qControl;
	
	private QueueRequest qReq, qAns;
	private IResponseCreator<QueueRequest> qResponseCreator;
	private IResponseProcess<QueueRequest> responseProc;
	
	@Before
	public void setUp() throws EventException {
		//A bean to interrogate
		submDummy = new DummyBean(); //Should have a uID & Status.NONE
		mockCons.addToSubmitQueue(submDummy);
		statDummy = new DummyBean();
		mockCons.addToStatusSet(statDummy);
		
		//Set up all the underlying queue service infrastructure
		mockPub = new MockPublisher<>(null, null);
		mockEvServ = new MockEventService();
		mockEvServ.setMockConsumer(mockCons);
		ServicesHolder.setEventService(mockEvServ);
		
		//This is the REAL queue service, because the Mock is too complex
		qServ = new QueueService();
		qServ.setQueueRoot("fake-q-root");
		qServ.setUri("file:///foo/bar");
		qServ.init();
		ServicesHolder.setQueueService(qServ);
		qControl = new QueueControllerService();
		qControl.init();
		ServicesHolder.setQueueControllerService(qControl);
		
		//Create the QueueResponseProcess creator
		qResponseCreator = new QueueResponseCreator();
	}
	
	@After
	public void tearDown() {
		qReq = null;
		qAns = null;
	}
	
	@Test
	public void testResponseGetJobQueueID() throws EventException {
		//Create bean status request for job-queue ID
		qReq = new QueueRequest();
		qReq.setRequestType(QueueRequestType.JOB_QUEUE_ID);
		
		//Create the response & process the request
		responseProc = qResponseCreator.createResponder(qReq, mockPub);
		qAns = responseProc.process(qReq);
		
		//Check response is correct for fake queue service
		String realJobQueueID = "fake-q-root"+IQueueService.JOB_QUEUE_SUFFIX;
		assertEquals("Response queue ID is incorrect", realJobQueueID, qAns.getJobQueueID());
	}
		
	@Test
	public void testResponseGetBeanStatus() throws EventException {
		/*
		 * Beans are already in the job-queue.
		 */
		submDummy.setStatus(Status.SUBMITTED);
		statDummy.setStatus(Status.RUNNING);
		
		/*
		 * Try getting a bean which is in the submit queue
		 */
		//Create bean status request & post
		qReq = new QueueRequest();
		qReq.setRequestType(QueueRequestType.BEAN_STATUS);
		qReq.setQueueID("fake-q-root"+IQueueService.JOB_QUEUE_SUFFIX);
		qReq.setBeanID(submDummy.getUniqueId());
		
		//Create the response & process the request
		responseProc = qResponseCreator.createResponder(qReq, mockPub);
	
		//Check response from server
		assertEquals("Response has wrong bean status", Status.SUBMITTED, qAns.getBeanStatus());
		
		/*
		 * Same as above, but get a bean which is in the status queue
		 */
		//Create bean status request & post
		qReq = new QueueRequest();
		qReq.setRequestType(QueueRequestType.BEAN_STATUS);
		qReq.setQueueID("fake-q-root"+IQueueService.JOB_QUEUE_SUFFIX);
		qReq.setBeanID(statDummy.getUniqueId());
		
		//Create the response & process the request
		responseProc = qResponseCreator.createResponder(qReq, mockPub);

		//Check response from server
		assertEquals("Response has wrong bean status", Status.RUNNING, qAns.getBeanStatus());
	}
	
	//Test getting full queue config
	//Test starting/stopping queueservice

}
