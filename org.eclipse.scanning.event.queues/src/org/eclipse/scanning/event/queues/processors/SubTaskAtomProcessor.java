package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.api.event.status.Status;

public class SubTaskAtomProcessor extends AbstractQueueProcessor<SubTaskAtom> {
	
	private AtomQueueProcessor<SubTaskAtom, QueueAtom> atomQueueProcessor;
	
	public SubTaskAtomProcessor() {
		super();
		atomQueueProcessor = new AtomQueueProcessor<>(this);
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		setExecuted();
		//Do most of the work of processing in the atomQueueProcessor...
		atomQueueProcessor.run();

		//...do the post-match analysis in here!
		try {
			lock.lockInterruptibly();
			if (isTerminated()) {
				queueBean.setMessage("Active-queue aborted before completion (requested)");
				atomQueueProcessor.terminate();
			} else if (queueBean.getPercentComplete() >= 99.5) {
				//Completed successfully
				broadcaster.broadcast(Status.COMPLETE, 100d, "Scan completed.");
			} else {
				//Failed: latch released before completion
				broadcaster.broadcast(Status.FAILED, "Active-queue failed (caused by process Atom)");
			}

			//This should be run after we've reported the queue final state
			atomQueueProcessor.tidyQueue();

			//And we're done, so let other processes continue
			analysisDone.signal();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void pause() throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() throws EventException {
		// TODO Auto-generated method stub

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
	
	/*
	 * For tests.
	 */
	public AtomQueueProcessor<SubTaskAtom, QueueAtom> getAtomQueueProcessor() {
		return atomQueueProcessor;
	}

}
