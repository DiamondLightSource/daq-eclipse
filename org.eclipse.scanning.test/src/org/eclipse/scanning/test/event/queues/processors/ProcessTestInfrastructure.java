package org.eclipse.scanning.test.event.queues.processors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.event.queues.ServicesHolder;
import org.eclipse.scanning.test.event.queues.dummy.DummyHasQueue;
import org.eclipse.scanning.test.event.queues.mocks.MockConsumer;
import org.eclipse.scanning.test.event.queues.mocks.MockEventService;
import org.eclipse.scanning.test.event.queues.mocks.MockPublisher;

public class ProcessorTestInfrastructure {
	
	private URI uri = null;
	private String topic = null;
	private MockPublisher<Queueable> statPub = new MockPublisher<>(uri, topic);
	
	private QueueProcess<Queueable> qProc;
	
	private Exception threadException;
	private long execTime = 50;
	private final CountDownLatch analysisLatch = new CountDownLatch(1);;
	
	/**
	 * Generic method for running a queue processor. When complete, it releases the execLatch.
	 * waitForExecutionEnd(timeoutMS) should be placed directly after this call.
	 * @param qProcr
	 * @param procBean
	 * @param procrBean
	 * @throws Exception
	 */
	public void executeProcessor(IQueueProcessor<? extends Queueable> qProcr, Queueable procrBean) throws Exception {
		qProc = new QueueProcess<>(procrBean, statPub, true);
		
		//Configure the QueueProcess & QueueProcessor
		qProcr.setQueueBroadcaster(qProc);
		qProcr.setProcessBean(procrBean);
		qProc.setProcessor(qProcr);
		
		//Check the bean doesn't have some weird initial state set on it:
		assertEquals("Wrong initial status", Status.NONE, procrBean.getStatus());
		assertEquals("Should not be non-zero percent complete", 0d, procrBean.getPercentComplete(), 0);
		
		Thread th = new Thread(new Runnable() {
			public void run() {
				try {
					qProc.execute();
				} catch (Exception e) {
					threadException = e;
				} finally {
					analysisLatch.countDown();
				}
			}
		});
		th.setDaemon(true);
		th.setPriority(Thread.MAX_PRIORITY);
		th.start();
		
		System.out.println("INFO: Sleeping for "+execTime+"ms to give the processor time to run...");
		Thread.sleep(execTime);
	}
	
	public void exceptionCheck() throws Exception {
		System.out.println("INFO: Waiting for thread to finish...");
		long timeout = 2000;
		boolean released = analysisLatch.await(timeout, TimeUnit.MILLISECONDS);
		if (threadException != null) {
			throw threadException;
		}
		
		if (!released) {
			fail("Thread running the QueueProcess didn't complete within "+timeout+"ms");
		}
	}
	
	/**
	 * Check the statuses of the first n beans depending on the number of 
	 * statuses and percentages supplied.
	 * @param bean with ID expected for broadcast beans
	 * @param beanStatuses
	 * @param beanPercent
	 */
	protected void checkFirstBroadcastBeanStatuses(Queueable bean, Status[] beanStatuses, Double[] beanPercent) throws Exception {
		if (beanStatuses.length != beanPercent.length) fail("Number of Statuses and percents to test must be equal");
		
		List<Queueable> broadcastBeans = getBroadcastBeans();
		if (broadcastBeans.size() < beanStatuses.length) fail("Too few beans broadcast (expected: "+beanStatuses.length+"; was: "+broadcastBeans.size()+")");
		
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
		assertTrue("Last bean is not final (was: "+lastBean.getStatus()+")", lastBean.getStatus().isFinal());
		assertEquals("Last bean has wrong status", state, lastBean.getStatus());
		if (percentComplete > 0) {
			assertEquals("Last bean has wrong percent complete", percentComplete, lastBPercComp, 0);
		} else {
			assertTrue("The percent complete is not between 0% & 100% (is: "+lastBPercComp+")", ((lastBPercComp > 0d) && (lastBPercComp < 100d)));
		}
		
	}
	
	@SuppressWarnings("unchecked")
	protected void checkLastBroadcastChildBeanStatus(Status state, String[] childBeanNames) {
		MockPublisher<?> mp = (MockPublisher<?>) ServicesHolder.getEventService().createPublisher(null, null);
		List<DummyHasQueue> childBeans = (List<DummyHasQueue>) getPublishedBeans(mp);
		
		List<String> childNames = Arrays.asList(childBeanNames);
		boolean childBeanBroadcast = false;
		for (DummyHasQueue child : childBeans) {
			//We check that the bean is from the child queue and that it has correct state
			if (!childNames.contains(child.getName())) continue;
			//We've got a child bean
			childBeanBroadcast = true;
			if (child.getStatus() == state) return;
		}
		
		//If we got child beans, but none final, that's a fail, as is no child beans.
		if (childBeanBroadcast) {
			fail("No child bean broadcast with Status "+state);
		} else {
			fail("No child beans broadcast");
		}		
	}
	
	public void checkConsumersStopped(MockEventService mockEvServ, IQueueService qServ) {
		Map<String, MockConsumer<? extends StatusBean>> consumers = mockEvServ.getRegisteredConsumers();
		
		for (Map.Entry<String, MockConsumer<? extends StatusBean>> entry : consumers.entrySet()) {
			//We don't need to check the job-queue
			if (entry.getKey().equals(qServ.getJobQueueID())) continue;
			assertTrue("Consumer was not stopped (this was expected)", entry.getValue().isStopped());
		}
		
	}
	
	/**
	 * Request all the broadcast beans from the mock publisher.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Queueable> getBroadcastBeans() {
		return (List<Queueable>) getPublishedBeans(statPub);
	}
	
	public Queueable getLastBroadcastBean() {
		return (Queueable)getLastPublishedBean(statPub);
	}
	
	public List<?> getPublishedBeans(MockPublisher<?> mockPub) {
		List<?> publBeans = mockPub.getBroadcastBeans();
		if (publBeans.size() == 0) fail("No beans broadcast to publisher");
		return publBeans;
	}
	
	public IdBean getLastPublishedBean(MockPublisher<?> mockPub) {
		List<?> publBeans = getPublishedBeans(mockPub);
		return (IdBean) publBeans.get(publBeans.size()-1);
	}
	
	public QueueProcess<Queueable> getQProc() {
		return qProc;
	}
	
	public Exception getProcThreadException() {
		return threadException;
	}
	
	public void resetInfrastructure() {
		statPub = new MockPublisher<>(uri, topic);
		qProc = null;
	}

}
