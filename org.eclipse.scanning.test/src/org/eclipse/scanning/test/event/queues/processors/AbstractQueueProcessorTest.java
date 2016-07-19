package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.test.event.queues.dummy.DummyBean;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractQueueProcessorTest {
	
	protected IQueueProcess<Queueable> qProc;	
	
	protected IPublisher<Queueable> statPub;
	protected String topic = "active.queue";
	
	protected CountDownLatch execLatch = new CountDownLatch(1);

	private Exception thrownException = null;

	@Before
	public void setUp() throws Exception {
		execLatch = new CountDownLatch(1);
		
		statPub = new MockPublisher<Queueable>(null, topic);
		
		localSetup();
	}
	
	protected abstract void localSetup() throws Exception;
	
	
	@After
	public void tearDown() throws Exception {
		localTearDown();
//		if ((qProc != null) && (qProc.getProcessor() != null) && !qProc.isTerminated()) qProc.terminate();
		statPub = null;
		qProc = null;
		
		execLatch = null;
		thrownException = null;
	}
	
	protected abstract void localTearDown() throws Exception;
	
	/**
	 * Test that type checking of the setProcessBean method prevents the wrong 
	 * bean being accepted by the processor.
	 * @throws Exception
	 */
	@Test
	public void testWrongBeanType() throws Exception {
		IQueueProcessor<? extends Queueable> qProcr = getTestProcessor();
		Queueable qBean = getTestBean();
		
		//This bean should be of a different type to the one acted on by the 
		//processor under test
		DummyBean absDBe = new DummyBean("Hephaestus", 600);
		if (absDBe.getClass().equals(qBean.getClass())) {
			throw new Exception("Bean received is of same type as 'wrong type' bean");
		}
		
		//Configure the processor
		try {
			qProcr.setProcessBean(absDBe);
			fail("Should not be able to supply wrong bean type");
		} catch (EventException eEx) {
			//Expected
		}
	}

	/**
	 * Tests that once setExecuted or execute called, configuration of 
	 * processor cannot be changed.
	 * @throws Exception
	 */
	@Test
	public void testChangingProcessorAfterExecution() throws Exception {
		IQueueProcessor<? extends Queueable> qProcr = getTestProcessor();
		Queueable qBean = getTestBean();
		try {
			changeBeanAfterExecution(qProcr, qBean);
			fail("Should not be able to change bean after execution start");
		} catch (EventException eEx) {
			//Expected
		}
		try {
			changeProcessAfterExecution(qProcr);
			fail("Should not be able to change bean after execution start");
		} catch (EventException eEx) {
			//Expected
		}
		
		//Try for real (create fresh Atom processor first)
		qProcr = getTestProcessor(true);
		assertFalse("Executed should initially be false", qProcr.isExecuted());
		
		//Execute
		doExecute(qProcr, qBean);
		waitForBeanStatus(qBean, Status.RUNNING, 500l);
		assertTrue("Executed should be false after start", qProcr.isExecuted());
		
		try {
			qProcr.setProcessBean(qBean);
			fail("Should not be able to set bean after execution start");
		} catch (EventException eEx) {
			//Expected
		}
		try {
			qProcr.setQueueBroadcaster(qProc);
			fail("Should not be able to set process after execution start");
		} catch (EventException eEx) {
			//Expected
		}
		//Terminate the process so we don't have to wait around long
		qProc.terminate();
		waitForBeanFinalStatus(qBean, 5000);
	}
	
	/**
	 * Prevents a processor with bean type A set on it being passed to a 
	 * process with bean type B. I think this only comes about when beans are 
	 * passed as Queueables. 
	 * @throws Exception
	 */
	@Test
	public void testDifferentBeanTypes() throws Exception {
		IQueueProcessor<? extends Queueable> qProcr = getTestProcessor();
		Queueable qBean = getTestBean();
		
		//This bean should be of a different type to the one acted on by the 
		//processor under test
		DummyBean absDBe = new DummyBean("Hephaestus", 600);
		if (absDBe.getClass().equals(qBean.getClass())) {
			throw new Exception("Bean received is of same type as 'wrong type' bean");
		}
		
		try {
			doExecute(qProcr, absDBe, qBean);
			//Need to allow thread to start before failing (otherwise, no error thrown)
			waitForBeanStatus(absDBe, Status.RUNNING, 500l);
			fail("Should not be able to execute with different bean types on processor & process");
		} catch (EventException eEx) {
			//Expected
		}
	}
	
	/**
	 * Check processor execution changes bean in such that it has executed 
	 * completely & successfully.
	 * @throws Exception
	 */
	@Test
	public void testExecution() throws Exception {
		Queueable testBean = getTestBean();
		IQueueProcessor<? extends Queueable> testProcr = getTestProcessor();
		/*
		 * After execution:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should be Status.COMPLETE and 100%
		 * - status publisher should have: 1 RUNNING bean and 1 COMPLETE bean
		 * 
		 */
		checkInitialBeanState(testBean);
		doExecute(testProcr, testBean);
		waitForExecutionEnd(10000l);
		
		checkLastBroadcastBeanStatuses(testBean, Status.COMPLETE, false);
		
		processorSpecificExecTests();
	}
	
	/**
	 * Processor specific execution tests (e.g. local to MoveAtomProcessor, 
	 * has motor move been communicated?)
	 * @param testBean
	 * @throws Exception
	 */
	protected abstract void processorSpecificExecTests() throws Exception;
	
	/**
	 * Check processor termination kills bean such that it appears terminated.
	 * @throws Exception
	 */
	@Test
	public void testTermination() throws Exception {
		Queueable testBean = getTestBean();
		IQueueProcessor<? extends Queueable> testProcr = getTestProcessor();
		/*
		 * On terminate:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should Status.TERMINATED and not be 100% complete
		 * - status publisher should have a TERMINATED bean
		 * 
		 * 
		 * N.B. MoveAtomProcessorTest uses MockPostioner, which pauses for 100ms 
		 * does something then pauses for 150ms.
		 */
		checkInitialBeanState(testBean);
		doExecute(testProcr, testBean);
		waitToTerminate();
		qProc.terminate();
		waitForBeanFinalStatus(testBean, 5000l);
		
		checkLastBroadcastBeanStatuses(testBean, Status.TERMINATED, false);
		
		processorSpecificTermTests();
	}
	
	/**
	 * Processor specific termination tests, e.g. that it cleans up safely.
	 * @param testBean
	 * @throws Exception
	 */
	protected abstract void processorSpecificTermTests() throws Exception;
	
	/**
	 * Check a failure reported to the processor causes processing to stop 
	 * incomplete and report bean as failed.
	 * @throws Exception
	 */
	@Test
	public void testFailure() throws Exception {
		Queueable failBean = getFailBean();
		IQueueProcessor<? extends Queueable> testProcr = getTestProcessor();
		/*
		 * On failure:
		 * - first bean in statPub should be Status.RUNNING
		 * - last bean in statPub should Status.FAILED and not be 100% complete
		 */
		checkInitialBeanState(failBean);
		doExecute(testProcr, failBean);
		causeFail();
//		waitForBeanState(failBean, Status.FAILED, true, 10000l);
		waitForBeanFinalStatus(failBean, 5000l);
		
		checkLastBroadcastBeanStatuses(failBean, Status.FAILED, false);
		
		processorSpecificFailTests();
	}
	
	/**
	 * A bean specifically configured to cause a failure.
	 * @return
	 */
	protected abstract Queueable getFailBean();
	
	/**
	 * Take action during execution necessary to simulate a failure.
	 */
	protected abstract void causeFail() throws Exception;
	
	/**
	 * Wait until a state within the processor has been reached 
	 * when terminate can be fired.
	 */
	protected abstract void waitToTerminate() throws Exception;
	/**
	 * Processor specific failure tests, e.g. aborting of processing tasks.
	 * @param testBean
	 * @throws Exception
	 */
	protected abstract void processorSpecificFailTests() throws Exception;
	
	/**
	 * These methods provide the queue bean & processor pair to test.
	 */
	protected IQueueProcessor<? extends Queueable> getTestProcessor() {
		return getTestProcessor(false);
	};
	protected abstract IQueueProcessor<? extends Queueable> getTestProcessor(boolean makeNew);
	protected abstract Queueable getTestBean();
	
	protected void doExecute(IQueueProcessor<? extends Queueable> qProcr, Queueable bean) throws Exception {
		doExecute(qProcr, bean, bean);
	}
	
	/**
	 * Generic method for running a queue processor. When complete, it releases the execLatch.
	 * waitForExecutionEnd(timeoutMS) should be placed directly after this call.
	 * @param qProcr
	 * @param procBean
	 * @param procrBean
	 * @throws Exception
	 */
	protected void doExecute(IQueueProcessor<? extends Queueable> qProcr, Queueable procBean, Queueable procrBean) throws Exception {
		qProc = new QueueProcess<Queueable>(procBean, statPub, true);
		
		//Configure the QueueProcess & QueueProcessor
		qProcr.setQueueBroadcaster(qProc);
		qProcr.setProcessBean(procrBean);
		qProc.setProcessor(qProcr);
		
		Thread th = new Thread(new Runnable() {
			public void run() {
				try {
					qProc.execute();
					execLatch.countDown();
				} catch (Exception e) {
					thrownException = new Exception(e);
				}
			}
		});
		th.setDaemon(true);
		th.setPriority(Thread.MAX_PRIORITY);
		th.start();
	}
	
	/**
	 * This is designed to throw an exception (testing the locking of config change behaviour).
	 * Should be surrounded by a try/catch block, with a fail in the try immediately after the call.
	 * @param qProcr
	 * @param procrBean
	 * @throws Exception
	 */
	protected void changeBeanAfterExecution(IQueueProcessor<? extends Queueable> qProcr, Queueable procrBean) throws EventException {
		qProcr.setExecuted();
		qProcr.setProcessBean(procrBean);
	}

	/**
	 * This is designed to throw an exception (testing the locking of config change behaviour).
	 * Should be surrounded by a try/catch block, with a fail in the try immediately after the call.
	 * @param qProcr
	 * @throws EventException
	 */
	protected void changeProcessAfterExecution(IQueueProcessor<? extends Queueable> qProcr) throws EventException {
		qProcr.setExecuted();
		qProcr.setQueueBroadcaster(qProc);
	}
	
	protected void waitForBeanStatus(Queueable bean, Status state, long timeout) throws Exception {
		waitForBeanState(bean, state, false, timeout);
	}
	
	protected void waitForBeanFinalStatus(Queueable bean, long timeout) throws Exception {
		waitForBeanState(bean, null, true, timeout);
	}
	
	private void waitForBeanState(Queueable bean, Status state, boolean isFinal, long timeout) throws Exception {
		Queueable lastBean= ((MockPublisher<Queueable>)statPub).getLastBean();
		long startTime = System.currentTimeMillis();
		long runTime = 0;
		
		while (runTime <= timeout) {
			if ((lastBean != null) && (lastBean.getUniqueId().equals(bean.getUniqueId()))) {
				if ((lastBean.getStatus().equals(state)) || (lastBean.getStatus().isFinal() && isFinal)) {
					return;
				}
			}
			Thread.sleep(50);
			runTime = System.currentTimeMillis() - startTime;
			if (thrownException != null) {
				throw new EventException(thrownException);
			}
			lastBean = ((MockPublisher<Queueable>)statPub).getLastBean();
		}
		String beanStatus;
		if (lastBean == null) {
			beanStatus = "~~ bean is null ~~";
		} else {
			beanStatus = lastBean.getStatus().toString();
		}
		throw new Exception("Bean state not reached before timeout (was: "+beanStatus+").");
	}
	
	protected void waitForExecutionEnd(Long timeoutMS) throws Exception {
		boolean unLatched = execLatch.await(timeoutMS, TimeUnit.MILLISECONDS);
		if (!unLatched) {
			if (thrownException == null) {
				fail("Execution did not complete before timeout");
			} else {
				throw new Exception(thrownException);
			}
		}
	}
	
	protected void checkInitialBeanState(Queueable bean) {
		assertEquals("Wrong initial status", Status.NONE, bean.getStatus());
		assertEquals("Should not be non-zero percent complete", 0d, bean.getPercentComplete(), 0);
	}

	/**
	 * Check the statuses of the first n beans depending on the number of 
	 * statuses and percentages supplied.
	 * @param bean with ID expected for broadcast beans
	 * @param beanStatuses
	 * @param beanPercent
	 */
	protected void checkFirstBroadcastBeanStatuses(Queueable bean, Status[] beanStatuses, Double[] beanPercent) throws Exception {
		assert(beanStatuses.length == beanPercent.length);
		
		List<Queueable> broadcastBeans = getBroadcastBeans();
		
		for (int i = 0; i < beanStatuses.length; i++) {
			Queueable broadBean = broadcastBeans.get(i);
			
			if (!broadBean.getUniqueId().equals(bean.getUniqueId())){
				throw new EventException(i+"th bean is not the bean we were looking for");
			}
			assertEquals(i+"th bean has wrong status", beanStatuses[i], broadBean.getStatus());
			assertEquals(i+"th bean has wrong percent", beanPercent[i], broadBean.getPercentComplete(), 0);
			if (i > 0){
				assertEquals(i+"th bean has wrong previous status", beanStatuses[i-1], broadBean.getPreviousStatus());
			}
		}
	}
	
	/**
	 * Check the statuses of the first last and (optionally) the penultimate 
	 * bean,  for any processor outcome, depending on the supplied state.
	 * @param bean
	 * @param state
	 * @param prevBean
	 * @throws EventException
	 */
	protected void checkLastBroadcastBeanStatuses(Queueable bean, Status state, boolean prevBean) throws EventException {
		Double percentComplete = -1d;
		Status previousBeanState = null;
		if (state.equals(Status.NONE)) {
			percentComplete = 0d;
		} else if (state.equals(Status.COMPLETE)) {
			percentComplete = 100d;
			previousBeanState = Status.RUNNING;
		} else if (state.equals(Status.TERMINATED)) {
			previousBeanState = Status.REQUEST_TERMINATE;
		}
		
		Queueable lastBean, penultimateBean, firstBean;
		List<Queueable> broadcastBeans = getBroadcastBeans();
		
		//First bean should be RUNNING.
		firstBean = broadcastBeans.get(0);
		if (!firstBean.getUniqueId().equals(bean.getUniqueId())){
			throw new EventException("First bean is not the bean we were looking for");
		}
		assertEquals("First bean should be running", Status.RUNNING, firstBean.getStatus());
		
		//Get last bean - needed for penultimate bean analysis too
		lastBean = broadcastBeans.get(broadcastBeans.size()-1);
		double lastBPercComp = lastBean.getPercentComplete();
		
		//Penultimate bean should have status depending on the above if/else block
		if (prevBean) {
			penultimateBean = broadcastBeans.get(broadcastBeans.size()-2);
			if (!penultimateBean.getUniqueId().equals(bean.getUniqueId())){
				throw new EventException("Penultimate bean is not the bean we were looking for");
			}
			assertEquals("Second to last bean has wrong status", previousBeanState, penultimateBean.getStatus());
			double penuBPercComp = penultimateBean.getPercentComplete();
			assertTrue("Percent complete greater than last bean's", lastBPercComp >= penuBPercComp);
			assertTrue("The percent complete is not between 0% & 100% (is: "+lastBPercComp+")", ((penuBPercComp > 0d) && (penuBPercComp < 100d))); 
		}
		
		//Last bean should have status in args and percent complete defined in if/else block
		if (!lastBean.getUniqueId().equals(bean.getUniqueId())){
			throw new EventException("Last bean is not the bean we were looking for");
		}
		assertTrue("Last bean is not final", lastBean.getStatus().isFinal());
		assertEquals("Last bean has wrong status", state, lastBean.getStatus());
		if (percentComplete > 0) {
			assertEquals("Last bean has wrong percent complete", percentComplete, lastBPercComp, 0);
		} else {
			assertTrue("The percent complete is not between 0% & 100% (is: "+lastBPercComp+")", ((lastBPercComp > 0d) && (lastBPercComp < 100d)));
		}
		
	}
	
	protected List<Queueable> getBroadcastBeans() {
		List<Queueable> broadcastBeans  = ((MockPublisher<Queueable>)statPub).getBroadcastBeans();
		if (broadcastBeans.size() == 0) fail("No beans broadcast to Publisher");
		return broadcastBeans;
	}

}
