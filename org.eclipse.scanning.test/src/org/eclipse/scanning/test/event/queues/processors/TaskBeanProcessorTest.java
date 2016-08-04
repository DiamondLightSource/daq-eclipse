package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.event.queues.beans.TaskBean;
import org.eclipse.scanning.event.queues.processors.TaskBeanProcessor;
import org.eclipse.scanning.test.event.queues.mocks.MockEventService;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueService;
import org.eclipse.scanning.test.event.queues.mocks.MockSubmitter;
import org.eclipse.scanning.test.event.queues.util.TestAtomQueueBeanMaker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TaskBeanProcessorTest {
	
	private TaskBean tBe;
	private TaskBeanProcessor tBeProcr;
	private ProcessorTestInfrastructure pti;
	
	private MockQueueService mockQServ;
	private MockSubmitter<QueueAtom> mockSub;
	private MockEventService mockEvServ;
	
	
	@Before
	public void setUp() {
		pti = new ProcessorTestInfrastructure();
		
		//Create processor & test atom
		tBe = new TaskBean("Test queue sub task bean");
		tBe.setBeamline("I15-1(test)");
		tBe.setHostName("afakeserver.diamond.ac.uk");
		tBe.setUserName(System.getProperty("user.name"));
		SubTaskAtom atomA = TestAtomQueueBeanMaker.makeDummySubTaskBeanA();
		SubTaskAtom atomB = TestAtomQueueBeanMaker.makeDummySubTaskBeanB();
		SubTaskAtom atomC = TestAtomQueueBeanMaker.makeDummySubTaskBeanC();
		tBe.queue().add(atomA);
		tBe.queue().add(atomB);
		tBe.queue().add(atomC);
		
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
		tBeProcr = new TaskBeanProcessor();
		
		pti.executeProcessor(tBeProcr, tBe);
		
		System.out.println("INFO: Sleeping for 50ms to give the processor time to run...");
		Thread.sleep(50);
		
		tBeProcr.getQueueBroadcaster().broadcast(Status.RUNNING, 99.5d, "Running finished.");
		pti.endExecution(tBeProcr);
		
		
		//These are the statuses & percent completes reported by the processor as it sets up the run
		Status[] reportedStatuses = new Status[]{Status.RUNNING, Status.RUNNING,
				Status.RUNNING, Status.RUNNING};
		Double[] reportedPercent = new Double[]{0d, 1d, 
				4d, 5d};
		
		pti.checkFirstBroadcastBeanStatuses(tBe, reportedStatuses, reportedPercent);
		pti.checkLastBroadcastBeanStatuses(tBe, Status.COMPLETE, true);
		
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
			assertEquals("Incorrect beamline", tBe.getBeamline(), dummy.getBeamline());
			assertFalse("No hostname set", dummy.getHostName() == null);
			assertEquals("Incorrect hostname", tBe.getHostName(), dummy.getHostName());
			assertFalse("No username set", dummy.getUserName() == null);
			assertEquals("Incorrect username", tBe.getUserName(), dummy.getUserName());
		}
	}

}
