package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;
import org.eclipse.scanning.test.event.queues.mocks.MockQueue;
import org.eclipse.scanning.test.event.queues.mocks.MockQueueService;

public class ProcessorTestInfrastructure {
	
	private MockPublisher<Queueable> statPub;
	
	private Exception threadException;
	
	/**
	 * Generic method for running a queue processor. When complete, it releases the execLatch.
	 * waitForExecutionEnd(timeoutMS) should be placed directly after this call.
	 * @param qProcr
	 * @param procBean
	 * @param procrBean
	 * @throws Exception
	 */
	public void executeProcessor(IQueueProcessor<? extends Queueable> qProcr, Queueable procrBean) throws Exception {
		QueueProcess<Queueable>qProc = new QueueProcess<>(procrBean, statPub, true);
		
		//Configure the QueueProcess & QueueProcessor
		qProcr.setQueueBroadcaster(qProc);
		qProcr.setProcessBean(procrBean);
		qProc.setProcessor(qProcr);
		
		final CountDownLatch threadLatch = new CountDownLatch(1);
		
		//Check the bean doesn't have some weird initial state set on it:
		assertEquals("Wrong initial status", Status.NONE, procrBean.getStatus());
		assertEquals("Should not be non-zero percent complete", 0d, procrBean.getPercentComplete(), 0);
		
		Thread th = new Thread(new Runnable() {
			public void run() {
				try {
					qProc.execute();
					threadLatch.countDown();
				} catch (Exception e) {
					threadException = new Exception(e);
				}
			}
		});
		th.setDaemon(true);
		th.setPriority(Thread.MAX_PRIORITY);
		th.start();
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
	
	/**
	 * Request all the broadcast beans from the mock publisher.
	 * @return
	 */
	public List<Queueable> getBroadcastBeans() {
		List<Queueable> broadcastBeans  = ((MockPublisher<Queueable>)statPub).getBroadcastBeans();
		if (broadcastBeans.size() == 0) fail("No beans broadcast to Publisher");
		return broadcastBeans;
	}
	
	public Exception getProcThreadException() {
		return threadException;
	}

}
