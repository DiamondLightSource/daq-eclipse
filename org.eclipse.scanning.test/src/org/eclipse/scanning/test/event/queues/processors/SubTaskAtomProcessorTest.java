package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.SubTaskBean;
import org.eclipse.scanning.event.queues.processors.SubTaskAtomProcessor;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;
import org.eclipse.scanning.test.event.queues.mocks.MockEventService;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueService;
import org.eclipse.scanning.test.event.queues.mocks.MockSubmitter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SubTaskAtomProcessorTest {
	
	private SubTaskBean stAt;
	private SubTaskAtomProcessor stAtProcr;
	private ProcessorTestInfrastructure pti;
	
	private MockQueueService mockQServ;
	private MockSubmitter<QueueAtom> mockSub;
	private MockEventService mockEvServ;
	
	
	@Before
	public void setUp() {
		pti = new ProcessorTestInfrastructure();
		
		//Create processor & test atom
		stAt = new SubTaskBean("Test queue sub task bean");
		stAt.setBeamline("I15-1(test)");
		stAt.setHostName("afakeserver.diamond.ac.uk");
		stAt.setUserName(System.getProperty("user.name"));
		DummyAtom atomA = new DummyAtom("Hildebrand", 300);
		DummyAtom atomB = new DummyAtom("Yuri", 1534);
		DummyAtom atomC = new DummyAtom("Ingrid", 654);
		stAt.queue().add(atomA);
		stAt.queue().add(atomB);
		stAt.queue().add(atomC);
		
		//Configure the processor Mock queue infrastructure
		mockSub = new MockSubmitter<>();
		mockQServ = new MockQueueService();
		mockQServ.setMockSubmitter(mockSub);
		QueueServicesHolder.setQueueService(mockQServ);
		
		mockEvServ = new MockEventService();
		QueueServicesHolder.setEventService(mockEvServ);
	}
	
	@After
	public void tearDown() {
		QueueServicesHolder.unsetEventService(mockEvServ);
		mockEvServ = null;
		
		QueueServicesHolder.unsetQueueService(mockQServ);
		mockQServ = null;
		mockSub = null;
		
		pti = null;
	}
	
	@Test
	public void testExecution() throws Exception {
		stAtProcr = new SubTaskAtomProcessor();
		
		pti.executeProcessor(stAtProcr, stAt);
		
		System.out.println("INFO: Sleeping for 50ms to give the processor time to run...");
		Thread.sleep(50);
		
		stAtProcr.getQueueBroadcaster().broadcast(Status.RUNNING, 99.5d, "Running finished.");
		pti.endExecution(stAtProcr);
		
		
		//These are the statuses & percent completes reported by the processor as it sets up the run
		Status[] reportedStatuses = new Status[]{Status.RUNNING, Status.RUNNING,
				Status.RUNNING, Status.RUNNING};
		Double[] reportedPercent = new Double[]{0d, 1d, 
				4d, 5d};
		
		pti.checkFirstBroadcastBeanStatuses(stAt, reportedStatuses, reportedPercent);
		pti.checkLastBroadcastBeanStatuses(stAt, Status.COMPLETE, true);
		
		testSubmittedBeans(mockSub);
	}
	
	protected void testSubmittedBeans(MockSubmitter<QueueAtom> ms) throws Exception {
		List<QueueAtom> submittedBeans = ms.getQueue();
		assertTrue("No beans in the final status set", submittedBeans.size() != 0);
		for (QueueAtom dummy : submittedBeans) {
			//First check beans are in final state
			assertTrue("Final bean "+dummy.getName()+" is not final",dummy.getStatus().isFinal());
			//Check the properties of the ScanAtom have been correctly passed down
			assertFalse("No beamline set", dummy.getBeamline() == null);
			assertEquals("Incorrect beamline", stAt.getBeamline(), dummy.getBeamline());
			assertFalse("No hostname set", dummy.getHostName() == null);
			assertEquals("Incorrect hostname", stAt.getHostName(), dummy.getHostName());
			assertFalse("No username set", dummy.getUserName() == null);
			assertEquals("Incorrect username", stAt.getUserName(), dummy.getUserName());
		}
	}

}
