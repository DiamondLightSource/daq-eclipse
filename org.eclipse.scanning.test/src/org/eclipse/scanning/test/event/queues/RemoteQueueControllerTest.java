package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IResponseCreator;
import org.eclipse.scanning.api.event.core.IResponseProcess;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.queues.remote.QueueRequest;
import org.eclipse.scanning.api.event.queues.remote.QueueRequestType;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.Queue;
import org.eclipse.scanning.event.queues.QueueService;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.MockConsumer;
import org.eclipse.scanning.test.event.queues.mocks.MockEventService;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueService;
import org.junit.Before;
import org.junit.Test;

public class RemoteQueueControllerTest {
	
	private DummyBean dummy;
	private MockPublisher<QueueRequest> mockPub;
	private MockConsumer<Queueable> mockCons = new MockConsumer<>();
	private MockEventService mockEvServ;
	private IQueueService qServ;
	
	private IResponseCreator<QueueRequest> qResponseCreator;	
	
	@Before
	public void setUp() throws EventException {
		//A bean to interrogate
		dummy = new DummyBean(); //Should have a uID & Status.NONE
		mockCons.addToSubmitQueue(dummy);
		
		//Set up all the underlying queue service infrastructure
		mockPub = new MockPublisher<>(null, null);
		mockEvServ = new MockEventService();
		mockEvServ.setMockConsumer(mockCons);
		
		//This is the REAL queue service, because the Mock is too complex
		qServ = new QueueService();
		qServ.setQueueRoot("fake-q-root");
		qServ.setUri("file:///foo/bar");
		qServ.init();
	}
	
	@Test
	public void testResponseGetJobQueueID() throws EventException {
		//Create bean status request for job-queue ID
		QueueRequest qReq = new QueueRequest();
		qReq.setType(QueueRequestType.JOB_QUEUE_ID);
		
		//Create the response & process the request
		IResponseProcess<QueueRequest> responseProc = qResponseCreator.createResponder(qReq, mockPub);
		QueueRequest qAns = responseProc.process(qReq);
		
		//Check response is correct for fake queue service
		String realJobQueueID = "fake-q-root"+IQueueService.JOB_QUEUE_SUFFIX;
		assertEquals("Response queue ID is incorrect", realJobQueueID, qAns.getJobQueueID());
	}
		
	@Test
	public void testResponseGetBeanStatus() {
		dummy.setStatus(Status.SUBMITTED);
		
		
		//Create bean status request & post
		QueueRequest qReq = new QueueRequest();
		qReq.setType(QueueRequestType.BEAN_STATUS);
		qReq.setQueueID("fake-q-root"+IQueueService.JOB_QUEUE_SUFFIX);
		qReq.setBeanID(dummy.getUniqueId());
		
		
		//Check response from server
		assertEquals("Response has wrong bean status", Status.SUBMITTED, qReq.getBeanStatus());
	}

}
