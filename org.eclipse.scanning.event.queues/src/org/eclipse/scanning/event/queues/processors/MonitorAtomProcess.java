package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.MonitorAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.event.queues.QueueProcessFactory;

/**
 * MonitorAtomProcess reads back a single value from a monitor. It will use 
 * the view detector methods discussed that should be available as part of the
 * Mapping project. TODO!!!!
 * 
 * TODO Implement class!!!
 * TODO Rehash java-doc once implemented
 * 
 * @author Michael Wharmby
 *
 * @param <T> The {@link Queueable} specified by the {@link IConsumer} 
 *            instance using this MonitorAtomProcess. This will be 
 *            {@link QueueAtom}.
 */
public class MonitorAtomProcess<T extends Queueable> extends QueueProcess<MonitorAtom, T> {
	
	/**
	 * Used by {@link QueueProcessFactory} to identify the bean type this 
	 * {@link QueueProcess} handles.
	 */
	public static final String BEAN_CLASS_NAME = MonitorAtom.class.getName();

	public MonitorAtomProcess(T bean, IPublisher<T> publisher, Boolean blocking) throws EventException {
		super(bean, publisher, blocking);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void run() throws EventException, InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void postMatchAnalysis() throws EventException, InterruptedException {
		try {
			postMatchAnalysisLock.lockInterruptibly();

			if (isTerminated()) {
				broadcast("Move aborted before completion (requested).");//FIXME Change message
				return;
			}

			if (queueBean.getPercentComplete() >= 99.5) {
				//Clean finish
				broadcast(Status.COMPLETE, 100d, "Device move(s) completed.");//FIXME Change message
			} else {
				//Scan failed
				//TODO Set message? Or is it set elsewhere?
				//TODO Instruct Monitor to abort?
				broadcast(Status.FAILED);
			} 
		} finally {
			//And we're done, so let other processes continue
			executionEnded();

			postMatchAnalysisLock.unlock();

			/*
			 * N.B. Broadcasting needs to be done last; otherwise the next 
			 * queue may start when we're not ready. Broadcasting should not 
			 * happen if we've been terminated.
			 */
			if (!isTerminated()) {
				broadcast();
			}
		}
	}
	
	@Override
	public void doTerminate() throws EventException {
		try {
			//Reentrant lock ensures execution method (and hence post-match 
			//analysis) completes before terminate does
			postMatchAnalysisLock.lockInterruptibly();

			//TODO Additional abort action, not handled as part of run()?
			terminated = true;

			//Wait for post-match analysis to finish
			continueIfExecutionEnded();
		} catch (InterruptedException iEx) {
			throw new EventException(iEx);
		} finally {
			postMatchAnalysisLock.unlock();
		}
	}
	
	@Override
	public void doPause() throws EventException {
		//TODO!
	}

	@Override
	public void doResume() throws EventException {
		//TODO!
	}
	
	@Override
	public Class<MonitorAtom> getBeanClass() {
		return MonitorAtom.class;
	}

}
