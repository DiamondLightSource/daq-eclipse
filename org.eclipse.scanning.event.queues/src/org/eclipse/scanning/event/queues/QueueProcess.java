package org.eclipse.scanning.event.queues;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.AbstractLockingPausableProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueBroadcaster;
import org.eclipse.scanning.api.event.queues.IQueueProcess;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO Update java-doc
 * Generic class for processing a queue item, irrespective of its concrete 
 * type. The concrete type should be identified by the 
 * {@link QueueProcessCreator} and  this class then instantiated with the 
 * {@link IQueueProcessor} associated with that type. This class uses 
 * {@link AbstractLockingPausableProcess} to provide generic pause, resume & terminate
 * functions, with bean specific methods called on the processors.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Type acted on by queue consumer (this will probably be a 
 * super-type of the actual bean class). 
 */
public abstract class QueueProcess<Q extends Queueable, T extends Queueable> extends AbstractLockingPausableProcess<T> implements IQueueProcess<Q, T>, IQueueBroadcaster<T> {
	
	private static Logger logger = LoggerFactory.getLogger(QueueProcess.class);
	
	protected final Q queueBean;
	protected boolean blocking = true, executed = false, terminated = false, finished = false;
	
	protected final CountDownLatch processLatch = new CountDownLatch(1);
	
	//Post-match analysis lock, ensures correct execution order of execute 
	//method & control (e.g. terminate) methods 
	protected final ReentrantLock postMatchAnalysisLock;
	protected final Condition analysisDone;
	
	@SuppressWarnings("unchecked")
	protected QueueProcess(T bean, IPublisher<T> publisher, Boolean blocking) throws EventException {
		super(bean, publisher);
		
		this.blocking = blocking;
		if (bean.getClass().equals(getBeanClass())) {
			this.queueBean = (Q)bean;
		} else {
			logger.error("Cannot set bean: Bean type "+bean.getClass().getSimpleName()+" not supported by "+getClass().getSimpleName()+".");
			throw new EventException("Unsupported bean type");
		}
		
		postMatchAnalysisLock = new ReentrantLock();
		analysisDone = postMatchAnalysisLock.newCondition();
	}
	
	@Override
	public void execute() throws EventException, InterruptedException {
		run();
		processLatch.await();
		postMatchAnalysis();
	}
	
	protected abstract void run() throws EventException, InterruptedException;
	protected abstract void postMatchAnalysis() throws EventException, InterruptedException;
	
	@Override
	public void updateBean(Status newStatus, Double newPercent, String newMessage) {
		if (newStatus != null) {
			bean.setPreviousStatus(bean.getStatus());
			bean.setStatus(newStatus);
		}
		if (newPercent != null) bean.setPercentComplete(newPercent);
		if (newMessage != null) bean.setMessage(newMessage);
		
		if ((newStatus == null) && (newPercent == null) && (newMessage == null)) {
			logger.warn("Bean updating prior to broadcast did not make any changes.");
		}
	}

	@Override
	public void broadcast(Status newStatus, Double newPercent, String newMessage) throws EventException {
		updateBean(newStatus, newPercent, newMessage);
		broadcast();
	}

	@Override
	public void broadcast() throws EventException {
		if (publisher != null) {
			publisher.broadcast(bean);
		}
	}

	@Override
	public boolean isExecuted() {
		return executed;
	}

	@Override
	public boolean isTerminated() {
		return terminated;
	}
	
	@Override
	public Q getQueueBean() {
		return queueBean;
	}
	
	/**
	 * Called at the end of post-match analysis to report the process finished
	 */
	protected void executionEnded() {
		finished = true;
		analysisDone.signal();
	}
	
	/**
	 * Called when we would need to wait if post-match analysis hasn't yet run.
	 * @throws InterruptedException 
	 */
	protected void continueIfExecutionEnded() throws InterruptedException {
		if (finished) return;
		else {
			analysisDone.await();
		}
	}
	
	public CountDownLatch getProcessLatch() {
		return processLatch;
	}
	
//	public QueueProcess(T bean, IPublisher<T> publisher, boolean blocking) {
//		super(bean, publisher);
//		this.blocking = blocking; //TODO
//	}
//	
//	@Override
//	public void execute() throws EventException, InterruptedException {
//		executed = true;
//		processor.execute();
//	}
//
//	/*
//	 * The following methods (doPause, doResume and doTerminate) are called by 
//	 * the pause, resume and terminate methods of AbstractPausableProcess 
//	 * (which override the default methods of IQueueProcess API). 
//	 * e.g. @see org.eclipse.scanning.api.event.core.AbstractPausableProcess#doTerminate()
//	 */
//	@Override
//	public void doPause() throws EventException {
//		processor.pause();
//	}
//	
//	@Override
//	public void doResume() throws Exception {
//		processor.resume();
//	}
//	
//	@Override
//	public void doTerminate() throws EventException {
//		terminated = true;
//		processor.terminate();
//	}
//	

//	
//	@Override
//	public void broadcast(Status newStatus, Double newPercent, String newMessage) throws EventException {
//		if (publisher != null && processor != null) {
//			updateBean(newStatus, newPercent, newMessage);
//			publisher.broadcast(bean);
//		}		
//	}
//
//	@Override
//	public void broadcast() throws EventException {
//		if (publisher != null) {
//			publisher.broadcast(bean);
//		}
//	}
//
//	@Override
//	public IQueueProcessor<? extends Queueable> getProcessor() {
//		return processor;
//	}
//
//	@Override
//	public void setProcessor(IQueueProcessor<? extends Queueable> processor) throws EventException {
//		if (isExecuted()) throw new EventException("Cannot chance processor after execution started");
//		//This should stop bean type mismatches. A second catch should be included in the execute() of the IQueueProcessor
//		if (!(bean.getClass().equals(processor.getBeanClass()))) throw new EventException("Cannot set processor - incorrect bean type");
//		this.processor = processor;
//	}
//	
//	@Override
//	public boolean isExecuted() {
//		return executed;
//	}
//
//	@Override
//	public boolean isTerminated() {
//		return terminated;
//	}
//
//	public boolean isBlocking() {
//		return blocking;
//	}
//
//	public void setBlocking(boolean blocking) {
//		this.blocking = blocking;
//	}
//	
//	public void setExecuted() {
//		executed = true;
//	}

}
