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
	
	@Before
	public void setUp() {
		pti = new ProcessorTestInfrastructure();
		
		//Configure the processor Mock queue infrastructure
		mockSub = new MockSubmitter<>();
		mockQServ = new MockQueueService();
		mockQServ.setMockSubmitter(mockSub);
		QueueServicesHolder.setQueueService(mockQServ);
	}
	
	@After
	public void tearDown() {
		QueueServicesHolder.unsetQueueService(mockQServ);
		mockQServ = null;
		mockSub = null;
		
		pti = null;
	}
	
	@Test
	public void testExecution() throws Exception {
		
		pti.executeProcessor(stAtProcr, stAt);
		
		System.out.println("\n\n*******************\nSleeping for 500ms - do we need to???\n*******************\n\n");
		Thread.sleep(500);
		stAtProcr.getProcessorLatch().countDown();
		
		
		//These are the statuses & percent completes reported by the processor as it sets up the run
		Status[] reportedStatuses = new Status[]{Status.RUNNING, Status.RUNNING,
				Status.RUNNING, Status.RUNNING, Status.RUNNING};
		Double[] reportedPercent = new Double[]{0d, 0d, 
				1.25d, 2.5d, 5d};
		
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
			assertFalse("No name set", dummy.getName() == null);
			assertEquals("Incorrect name", stAt.getName(), dummy.getName());
			assertFalse("No username set", dummy.getUserName() == null);
			assertEquals("Incorrect username", stAt.getUserName(), dummy.getUserName());
		}
	}

}
