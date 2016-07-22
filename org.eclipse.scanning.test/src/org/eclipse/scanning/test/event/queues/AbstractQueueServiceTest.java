package org.eclipse.scanning.test.event.queues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.IHeartbeatMonitor;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.QueueStatus;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.Constants;
import org.eclipse.scanning.event.queues.HeartbeatMonitor;
import org.eclipse.scanning.event.queues.QueueProcessCreator;
import org.eclipse.scanning.event.queues.QueueProcessorFactory;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtom;
import org.eclipse.scanning.test.event.queues.dummy.DummyAtomProcessor;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;
import org.eclipse.scanning.test.event.queues.dummy.DummyBeanProcessor;
import org.eclipse.scanning.test.event.queues.util.EventInfrastructureFactoryService;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test of methods on the {@link IQueueService} interface.
 * 
 * TODO Add test for getHeartMonitor
 * TODO Add test of removing a bean from the queue, before it's processing has started.
 * TODO Add test of pausing queue.
 * 
 * @author Michael Wharmby
 *
 */

public abstract class AbstractQueueServiceTest {
	
	protected IQueueService qServ;
	protected static String qRoot;
	protected static EventInfrastructureFactoryService infrastructureServ;
	protected URI uri;
	
	@BeforeClass
	public static void setupClass() {
		qRoot = "uk.ac.diamond.i15-1";
	}
	
	@Before
	public void setup() throws Exception {
		Constants.setNotificationFrequency(200);
		QueueProcessorFactory.registerProcessor(DummyAtomProcessor.class);
		QueueProcessorFactory.registerProcessor(DummyBeanProcessor.class);
		
		localSetup();
	}
	
	protected abstract void localSetup() throws Exception;

	@After
	public void stop() throws Exception{
		qServ.disposeService();
		localTearDown();
		
		Constants.setNotificationFrequency(2000);
	}
	
	protected abstract void localTearDown() throws Exception;
	
	@Test
	public void testServiceStartStop() throws Exception {
		List<HeartbeatBean> beats;
		DummyBean[] beans = new DummyBean[] {new DummyBean("Gero", 30), new DummyBean("Florian", 40)};
		String jqID = qServ.getJobQueueID();
		IQueue<QueueBean> jobQueue = qServ.getJobQueue();

		/*
		 * Start & stop the service twice. First time checks initialisation correct & stops normally;
		 * Second time kills the service with a disconnect.
		 */
		for (int i = 0; i < 2; i++) {
			//Before we start the queue, check for heartbeats & queue status.
			if (i == 0) {
				beats = jobQueue.getLatestHeartbeats();
				assertTrue("Found heartbeats before service started", beats.isEmpty());
				assertEquals("Job queue state is incorrect", QueueStatus.INITIALISED, qServ.getQueueStatus(jqID));
			} else {
				assertEquals("Job queue state is incorrect", QueueStatus.STOPPED, qServ.getQueueStatus(jqID));
			}

			//Start the service (and job queue), check status & whether queue is alive
			qServ.start();
			assertEquals("Job queue state is incorrect", QueueStatus.STARTED, qServ.getQueueStatus(jqID));
			Thread.sleep(300);//This has been optimised
			beats = jobQueue.getLatestHeartbeats();
			final int livingSize = beats.size();

			//Submit a bean, let the queue part run it and then stop it.
			qServ.jobQueueSubmit(beans[i]);
			if (i == 0) {
				Thread.sleep(200);
				qServ.stop(false);
				assertEquals("Active queue state is incorrect", QueueStatus.STOPPED, qServ.getQueueStatus(jqID));
				
				//Wait to see if the queue is still alive & check the state of the bean at the end
				waitForBeanFinalState(beans[i], jobQueue.getConsumer(), 5000);
				beats = jobQueue.getLatestHeartbeats();
				final int sizeAtStop = beats.size();
				assertTrue("Queue had no heart beat during its lifetime", livingSize < sizeAtStop);
				checkProcessFinalStatus(beans[i], jqID, Status.TERMINATED);
			} else {
				qServ.stop(true);
				assertEquals("Active queue state is incorrect", QueueStatus.KILLED, qServ.getQueueStatus(jqID));
				
				//Kill should disconnect the consumer.
				checkForShutdownConsumer(jobQueue.getConsumerID(), jobQueue.getHeartbeatTopicName());
			}
		}
		
		//Job queue is now in the killed state, so shouldn't be startable
		try {
			qServ.start();
			fail("Job queue should be 'KILLED' & not startable.");
		} catch (EventException ex) {
			//expected
		}
	}
	
	@Test
	public void testRegistrationSystem() throws EventException {
		try {
			qServ.registerNewActiveQueue();
			fail("Expecting EventException, since qServ not started");
		} catch (EventException ex) {
			//Expected
		}

		qServ.start();
		Map<String, IQueue<QueueAtom>> qList;
		
		for (int i = 0; i < 5; i++) {
			//Create a fake queue
			qServ.registerNewActiveQueue();
			qList = qServ.getAllActiveQueues();
			assertEquals(i + 1, qList.size());

			//Check the name of the next queue will be incremented
			assertTrue(qList.containsKey(qRoot+".active-queue-"+i));
		}
		
		List<String> qNameList = qServ.getAllActiveQueueIDs();
		final int nQueues = qNameList.size();
		int i =1;
		for (String qID : qNameList) {
			//De-register active queues
			qServ.deRegisterActiveQueue(qID, false);
			
			qNameList = qServ.getAllActiveQueueIDs();
			assertEquals(nQueues - (i), qNameList.size());
			try {
				qServ.getActiveQueue(qID);
				fail("Queue not deregistered!");
			} catch (IllegalArgumentException ex) {
				//expected
			}
			i++;
		}
	}
	
	@Test
	public void testStartStopActiveQueue() throws Exception {
		List<HeartbeatBean> beats;
		DummyAtom[] atoms = new DummyAtom[]{new DummyAtom("Cuthbert", 30), new DummyAtom("Rupert", 40)};
		qServ.start();
		String aqID = qServ.registerNewActiveQueue();
		IQueue<QueueAtom> activeQueue = qServ.getActiveQueue(aqID);

		/*
		 * Start & stop the active queue twice. First time checks initialisation correct & stops normally;
		 * Second time kills the active queue with a disconnect.
		 */
		for (int i = 0; i < 2; i++) {
			//Before we start the queue, check for heartbeats & queue status.
			if (i == 0) {
				beats = activeQueue.getLatestHeartbeats();
				assertTrue("Found heartbeats before service started", beats.isEmpty());
				assertEquals("Job queue state is incorrect", QueueStatus.INITIALISED, qServ.getQueueStatus(aqID));
			} else {
				assertEquals("Active queue state is incorrect", QueueStatus.STOPPED, qServ.getQueueStatus(aqID));
			}

			//Start the service (and job queue), check status & whether queue is alive
			qServ.startActiveQueue(aqID);
			assertEquals("Active queue state is incorrect", QueueStatus.STARTED, qServ.getQueueStatus(aqID));
			Thread.sleep(500);//This has been optimised
			beats = activeQueue.getLatestHeartbeats();
			final int livingSize = beats.size();
			assertTrue("Queue has no heartbeat after starting", livingSize > 0);

			//Submit a bean, let the queue part run it and then stop it.
			qServ.activeQueueSubmit(atoms[i], aqID);
			if (i == 0) {
				Thread.sleep(200);//TODO
				qServ.stopActiveQueue(aqID, false);
				assertEquals("Active queue state is incorrect", QueueStatus.STOPPED, qServ.getQueueStatus(aqID));

				//Wait to see if the queue is still alive & check the state of the bean at the end
				waitForBeanFinalState(atoms[0], qServ.getActiveQueue(aqID).getConsumer(), 5000);
				
				beats = activeQueue.getLatestHeartbeats();
				final int sizeAtStop = beats.size();
				assertTrue("Queue had no heart beat during its lifetime", livingSize < sizeAtStop);
				checkProcessFinalStatus(atoms[i], aqID, Status.TERMINATED);
			} else {
				qServ.stopActiveQueue(aqID, true);
				assertEquals("Active queue state is incorrect", QueueStatus.KILLED, qServ.getQueueStatus(aqID));

				//Kill should disconnect the consumer.
				checkForShutdownConsumer(activeQueue.getConsumerID(), activeQueue.getHeartbeatTopicName());
			}
		}
		
		//Active queue is now in the killed state, so shouldn't be startable
		try {
			qServ.startActiveQueue(aqID);
			fail("Active queue should be 'KILLED' & not startable.");
		} catch (EventException ex) {
			//expected
		}
	}
	
	@Test
	public void testActiveQueueDisposal() throws Exception {
		DummyAtom atomA = new DummyAtom("Heinrich", 545);
		DummyAtom atomB = new DummyAtom("Hermann", 545);
		
		qServ.start();
		String aqID = qServ.registerNewActiveQueue();
		qServ.activeQueueSubmit(atomA, aqID);
		qServ.activeQueueSubmit(atomB, aqID);
		qServ.startActiveQueue(aqID);
		UUID aqConsID = qServ.getActiveQueue(aqID).getConsumerID();
		Thread.sleep(300);//TODO
		try {
			qServ.disposeQueue(aqID, false);
			fail("Shouldn't be able to dispose a running queue.");
		} catch (EventException ex) {
			//Expected
		}
		
		qServ.stopActiveQueue(aqID, false);
		qServ.disposeQueue(aqID, false);
		
		assertTrue("queue ID not found in registry!", qServ.getAllActiveQueues().containsKey(aqID));
		assertEquals("Job queue has wrong state", QueueStatus.DISPOSED, qServ.getQueueStatus(aqID));
		assertFalse("Consumer should not be active", qServ.getActiveQueue(aqID).getConsumer().isActive());

		checkForShutdownConsumer(aqConsID, qServ.getHeartbeatTopicName());
	}
	
	@Test
	public void testActiveQueueKilling() throws Exception {
		DummyAtom atomA = new DummyAtom("Gordon", 545);
		DummyAtom atomB = new DummyAtom("Lars", 545);
		
		qServ.start();
		String aqID = qServ.registerNewActiveQueue();
		qServ.activeQueueSubmit(atomA, aqID);
		qServ.activeQueueSubmit(atomB, aqID);
		qServ.startActiveQueue(aqID);
		Thread.sleep(150);
		
		qServ.killQueue(aqID, false, false); //Need to set disconnect false to allow post-match analysis!
		waitForBeanFinalState(atomA, qServ.getActiveQueue(aqID).getConsumer(), 5000);
		
		assertTrue("queue ID not found in registry!", qServ.getAllActiveQueues().containsKey(aqID));
		assertEquals("Job queue has wrong state", QueueStatus.KILLED, qServ.getQueueStatus(aqID));
		
		checkForStoppedConsumer(qServ.getActiveQueue(aqID));
		IConsumer<QueueAtom> aqCons = qServ.getActiveQueue(aqID).getConsumer();
		List<QueueAtom> submQ = aqCons.getSubmissionQueue();
		List<QueueAtom> statQ = qServ.getActiveQueueStatusSet(aqID);
		
		assertFalse("Submit queue is empty after kill (one unconsumed bean should still be here).", submQ.isEmpty());
		assertEquals("Expecting exactly one unconsumed bean", 1, submQ.size());
		assertFalse("Status queue is not empty after kill (one bean, killed in the prime of its life should be here)", statQ.isEmpty());
		assertEquals("Expecting exactly one final state bean", 1, statQ.size());

		checkBeanFinalStatus(atomA, statQ.get(0), Status.TERMINATED);
		checkBeanFinalStatus(atomB, submQ.get(0), Status.SUBMITTED);
	}
	
	@Test
	public void testBeanSubmission() throws Exception {
		List<? extends Queueable> atomBeanSubmitQueue;
		
		//Create beans to submit
		DummyBean dbA = new DummyBean("Anatole", 70);
		DummyBean dbB = new DummyBean("Pierre", 704);
		DummyAtom daA = new DummyAtom("Helene", 10);
		DummyAtom daB = new DummyAtom("Marya", 588);
		DummyAtom daC = new DummyAtom("Andrei", 679);
		
		//Job Queue
		String jqID = qServ.getJobQueueID();
		qServ.jobQueueSubmit(dbA);
		qServ.jobQueueSubmit(dbB);
		atomBeanSubmitQueue = qServ.getJobQueue().getConsumer().getSubmissionQueue();
		assertEquals("Two beans submitted, but fewer in submit queue", 2, atomBeanSubmitQueue.size());

		//Active queue
		qServ.start();
		String aqID = qServ.registerNewActiveQueue();
		qServ.activeQueueSubmit(daA, aqID);
		qServ.activeQueueSubmit(daB, aqID);
		atomBeanSubmitQueue = qServ.getActiveQueue(aqID).getConsumer().getSubmissionQueue();
		assertEquals("Two beans submitted, but fewer in submit queue", 2, atomBeanSubmitQueue.size());

		//Try to cause an error
		try {
			qServ.activeQueueSubmit(daC, null);
			fail("job queue shouldn't accept atoms.");
		} catch (IllegalArgumentException ex) {
			//Expected, continue
		}
		try {
			qServ.activeQueueSubmit(daC, jqID); //Using job queue id
			fail("job queue shouldn't accept atoms.");
		} catch (IllegalArgumentException ex) {
			//Expected, continue
		}
	}
	
	@Test
	public void testAtomBeanTermination() throws Exception {
		DummyBean bean = new DummyBean("Enrique", 200);
		DummyAtom atom = new DummyAtom("Fidel", 564);
		String jqID = qServ.getJobQueueID();
		qServ.start();
		
		//Submit a bean to the queue and allow it to start processing
		qServ.jobQueueSubmit(bean);
		Thread.sleep(150);  //This is optimised
		
		//Request termination
		bean.setStatus(Status.REQUEST_TERMINATE);
		qServ.jobQueueTerminate(bean);
		waitForBeanFinalState(bean, qServ.getJobQueue().getConsumer(), 5000);
		
		//Check it has terminated
		checkProcessFinalStatus(bean, jqID, Status.TERMINATED);
		
		//Do the same, but for active queue
		String aqID = qServ.registerNewActiveQueue();
		qServ.startActiveQueue(aqID);
		
		//Submit an atom to the queue and allow it to start processing
		qServ.activeQueueSubmit(atom, aqID);
		Thread.sleep(150); //This is optimised

		//Request termination
		atom.setStatus(Status.REQUEST_TERMINATE);
		qServ.activeQueueTerminate(atom, aqID);
		waitForBeanFinalState(atom, qServ.getActiveQueue(aqID).getConsumer(), 5000);

		//Check it has terminated
		checkProcessFinalStatus(atom, aqID, Status.TERMINATED);
	}

	@Test
	public void testChangingConfigWhilstStarted() throws Exception {
		qServ.start();
		
		//Change URI
		try {
			qServ.setURI(new URI("fishy.fishy"));
			fail("Shouldn't be able to change URI whilst service started");
		} catch (UnsupportedOperationException ex) {
			//Expected
		}
		
		//Change qroot
		try {
			qServ.setQueueRoot("fishy.fishy");
			fail("Shouldn't be able to change Queue root whilst service started");
		} catch (UnsupportedOperationException ex) {
			//Expected
		}
		
		//Change the jobQueueProcessor or the activeQueueProcessor
		try {
			qServ.setJobQueueProcessor(new QueueProcessCreator<QueueBean>(false));
			fail("Shouldn't be able to change the queue processor whilst service is started.");
		} catch (EventException e) {
			//expected
		}
		try {
			qServ.setActiveQueueProcessor(new QueueProcessCreator<QueueAtom>(false));
			fail("Shouldn't be able to change the queue processor whilst service is started.");
		} catch (EventException e) {
			//expected
		}
	}
	
	@Test
	public void testIsActiveQueueRegistered() throws Exception {
		qServ.start();
		String aqID = qServ.registerNewActiveQueue();
		
		assertTrue("Active queue reported not registered", qServ.isActiveQueueRegistered(aqID));
	}
	
	@Test
	public void testChangeQueueRootChangesControlTopics() throws Exception {
		final String initHeartbeatT = qServ.getHeartbeatTopicName();
		final String initCmdT = qServ.getCommandTopicName();
		
		String newQRoot = "uk.ac.diamond.i15-1.differentTest";
		qServ.setQueueRoot(newQRoot);
		
		assertFalse("Heartbeat topic name not changed", qServ.getHeartbeatTopicName().equals(initHeartbeatT));
		assertFalse("Command topic name not changed", qServ.getCommandTopicName().equals(initCmdT));
		
		assertEquals("Heartbeat topic has an unexpected name", newQRoot + IQueueService.HEARTBEAT_TOPIC_SUFFIX, qServ.getHeartbeatTopicName());
		assertEquals("Command topic has an unexpected name", newQRoot+IQueueService.COMMAND_TOPIC_SUFFIX, qServ.getCommandTopicName());
	}
	
	protected void checkProcessFinalStatus(Queueable bean, String queueID, Status expected) throws Exception {
		List<? extends Queueable> statusSet = qServ.getStatusSet(queueID);
		int i = 0;
		while (statusSet.size() == 0) {
			Thread.sleep(50);
			statusSet = qServ.getStatusSet(queueID);
			i=i+50;
			if (i >= 1000) fail("No beans reported in status set while waiting for final status.");
		}
		StatusBean complete = statusSet.get(0);
		checkBeanFinalStatus(bean, complete, expected);
	}
	
	protected void checkBeanFinalStatus(StatusBean bean, StatusBean complete, Status expected) {
		if (expected.equals(Status.SUBMITTED)) {
			assertEquals("Submitted and completed beans are different!", bean, complete);
		} else {
			assertTrue("Submitted bean and complete bean are identical!", !bean.equals(complete));
		}
		assertEquals("The bean in the queue has the wrong final state!", expected, complete.getStatus());
		if (expected.equals(Status.COMPLETE)) {
			assertTrue("The percent complete is not 100!", complete.getPercentComplete()==100);
		} else {
			assertTrue("The percent complete is 100!", complete.getPercentComplete()!=100);
		}
	}

	protected void checkForShutdownConsumer(UUID qConsID, String heartbeatTopicName) throws Exception {
		
		//Wait first to give consumer time to stop
		//This has been optimised. Expect a heartbeat every 200ms (we set this!), so this should be long 
		//enough *not* to hear anything
		Thread.sleep(350);
		
		//Check the queue has been stopped
		IHeartbeatMonitor hbm = new HeartbeatMonitor(uri, heartbeatTopicName, qConsID);
		
		//Wait to see if we hear a beat
		Thread.sleep(500);//Heartbeats occur every 200ms (we set this!), so need to wait this long.
		assertTrue("Heartbeat detected, consumer not disconnected", hbm.getRecorder().isEmpty());
	}
	
	protected void checkForStoppedConsumer(IQueue<? extends Queueable> queue) throws Exception {
		assertFalse("Consumer should not be active", queue.getConsumer().isActive());
		
		HeartbeatBean b1 = queue.getLastHeartbeat();
		//Wait to see if we hear a beat
		Thread.sleep(500);//Heartbeats occur every 200ms (we set this!), so need to wait this long.
		HeartbeatBean b2 = queue.getLastHeartbeat();
		
		assertEquals("Consumer is still alive - beats 2secs apart are different.", b1, b2);
		
	}
	
	protected void waitForBeanFinalState(Queueable bean, IConsumer<? extends Queueable> cons, long timeout) throws Exception {
		waitForBeanState(bean, cons, null, true, timeout);
	}
	
	protected void waitForBeanState(Queueable bean, IConsumer<? extends Queueable> cons, Status state, boolean isFinal, long timeout) throws Exception {
		final CountDownLatch statusLatch = new CountDownLatch(1);
		ISubscriber<IBeanListener<Queueable>> statusSubsc = infrastructureServ.makeSubscriber(cons.getStatusTopicName());
		statusSubsc.addListener(new IBeanListener<Queueable>() {

			@Override
			public void beanChangePerformed(BeanEvent<Queueable> evt) {
				Queueable evtBean = evt.getBean();
				if (evtBean.getUniqueId().equals(bean.getUniqueId())) {
					if ((evtBean.getStatus() == state) || (evtBean.getStatus().isFinal() && isFinal)) {
						statusLatch.countDown();
					}
				}
			}
			
		});
		if (!(cons.getStatusSet().isEmpty() || cons.getStatusSet().size() == 0)) { 
			//Extra "size()" test seems superfluous, but the "isEmpty()" was not picked up in testing...
			Status lastBeanState = cons.getStatusSet().get(0).getStatus();
			if ((lastBeanState == state) || (lastBeanState.isFinal() && isFinal)) statusLatch.countDown();
		}
		
		boolean released = statusLatch.await(timeout, TimeUnit.MILLISECONDS);
		if (released) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~\n Final state reached\n~~~~~~~~~~~~~~~~~~~~~~~~~");
		} else {
			System.out.println("#########################\n No final state reported\n#########################");
		}
		statusSubsc.disconnect();
	}

}
