package org.eclipse.scanning.event.queues.processors;

import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueBroadcaster;
import org.eclipse.scanning.api.event.queues.IQueueProcessor;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parent queue processor class, containing implementations of broadcast 
 * methods used by the concrete {@link IQueueProcessor} instances. Also 
 * contains boolean getters & setters for changes of execution state of the 
 * processor.
 * 
 * @author Michael Wharmby
 *
 * @param <P> Bean type implementing {@link Queueable} which will be processed 
 *            by the concrete {@link IQueueProcessor} instance.
 */
public abstract class AbstractQueueProcessor <P extends Queueable> implements IQueueProcessor<P> {

	private static Logger logger = LoggerFactory.getLogger(AbstractQueueProcessor.class);

	private boolean executed = false, terminated = false;

	protected P queueBean;
	protected IQueueBroadcaster<? extends Queueable> broadcaster;
	protected final CountDownLatch processorLatch = new CountDownLatch(1);

	@Override
	public P getProcessBean(){
		return queueBean;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Queueable> void setProcessBean(T bean) throws EventException {
		if (isExecuted()) {
			logger.error("Cannot change bean to be processed after execution has started.");
			throw new EventException("Cannot change bean to be processed after execution has started");
		}
		if (bean.getClass().equals(getBeanClass())) {
			this.queueBean = (P)bean;
		} else {
			logger.error("Cannot set bean: Bean type "+bean.getClass().getSimpleName()+" not supported by "+getClass().getSimpleName()+".");
			throw new EventException("Unsupported bean type");
		}
	}

	@Override
	public IQueueBroadcaster<? extends Queueable> getQueueBroadcaster() {
		return broadcaster;
	}

	@Override
	public void setQueueBroadcaster(IQueueBroadcaster<? extends Queueable> broadcaster) throws EventException {
		if (isExecuted()) {
			logger.error("Cannot change broadcaster after execution has started.");
			throw new EventException("Cannot change broadcaster after execution has started");
		}
		this.broadcaster = broadcaster;
	}

	@Override
	public boolean isExecuted() {
		return executed;
	}

	@Override
	public void setExecuted() {
		executed = true;
	}
	
	@Override
	public boolean isTerminated() {
		return terminated;
	}

	@Override
	public void setTerminated() {
		terminated = true;
	}
	
	public CountDownLatch getProcessorLatch() {
		return processorLatch;
	}

}
