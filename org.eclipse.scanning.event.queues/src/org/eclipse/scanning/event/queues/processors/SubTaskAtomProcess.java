package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.api.event.queues.beans.TaskBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;

public class SubTaskAtomProcess<T extends Queueable> extends QueueProcess<SubTaskAtom, T> {
	
	public static final String BEAN_CLASS_NAME = TaskBean.class.getName();
	
	private AtomQueueProcessor<SubTaskAtom, QueueAtom, T> atomQueueProcessor;
	
	public SubTaskAtomProcess(T bean, IPublisher<T> publisher, Boolean blocking) throws EventException {
		super(bean, publisher, blocking);
		atomQueueProcessor = new AtomQueueProcessor<>(this);
	}

	@Override
	protected void run() throws EventException, InterruptedException {
		executed = true;
		//Do most of the work of processing in the atomQueueProcessor...
		atomQueueProcessor.run();
	}
	
	@Override 
	protected void postMatchAnalysis() throws EventException, InterruptedException {
		//...do the post-match analysis in here!
		try {
			postMatchAnalysisLock.lockInterruptibly();
			if (isTerminated()) {
				atomQueueProcessor.terminate();
				queueBean.setMessage("Active-queue aborted before completion (requested)");
			}else if (queueBean.getPercentComplete() >= 99.49) {//99.49 to catch rounding errors
				//Completed successfully
				updateBean(Status.COMPLETE, 100d, "Scan completed.");
			} else {
				//Failed: latch released before completion
				updateBean(Status.FAILED, null, "Active-queue failed (caused by process Atom)");
			}
		} finally {
			//This should be run after we've reported the queue final state
			//This must be after unlock call, otherwise terminate gets stuck.
			atomQueueProcessor.tidyQueue();
			
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

			terminated = true;
			processLatch.countDown();
			//Wait for post-match analysis to finish
			continueIfExecutionEnded();
		} catch (InterruptedException iEx) {
			throw new EventException(iEx);
		} finally {
			postMatchAnalysisLock.unlock();
		}
	}



//	@Override
//	public void terminate() throws EventException {
//		//Reentrant lock ensures execution method (and hence post-match 
//		//analysis) complete before terminate does
//		try{
//			lock.lockInterruptibly();
//
//			setTerminated();
//			processorLatch.countDown();
//
//			//Wait for post-match analysis to finish
//			analysisDone.await();
//		} catch (InterruptedException iEx) {
//			throw new EventException(iEx);
//		} finally {
//			lock.unlock();
//		}
//	}

	@Override
	public Class<SubTaskAtom> getBeanClass() {
		return SubTaskAtom.class;
	}
	
	public AtomQueueProcessor<SubTaskAtom, QueueAtom, T> getAtomQueueProcessor() {
		return atomQueueProcessor;
	}

}
