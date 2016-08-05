package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.AtomQueueServiceUtils;
import org.eclipse.scanning.event.queues.beans.SubTaskAtom;

public class SubTaskAtomProcessor extends AbstractQueueProcessor<SubTaskAtom> {
	
	private AtomQueueProcessor<SubTaskAtom, QueueAtom> atomQueueProcessor;
	
	public SubTaskAtomProcessor() {
		atomQueueProcessor = new AtomQueueProcessor<>(this);
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		setExecuted();
		//Do most of the work of processing in the atomQueueProcessor...
		atomQueueProcessor.run();
		
		//...do the post-match analysis in here!
		if (isTerminated()) {
			broadcaster.broadcast("Active-queue aborted before completion (requested)");
			AtomQueueServiceUtils.terminateQueueProcess(atomQueueProcessor.getActiveQueueName(), queueBean);
		} else if (queueBean.getPercentComplete() >= 99.5) {
			//Completed successfully
			broadcaster.broadcast(Status.COMPLETE, 100d, "Scan completed.");
		}
		
		//This should be run after we've reported the queue final state
		atomQueueProcessor.tidyQueue();
	}

	@Override
	public void pause() throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void terminate() throws EventException {
		setTerminated();
		processorLatch.countDown();
	}

	@Override
	public Class<SubTaskAtom> getBeanClass() {
		return SubTaskAtom.class;
	}

}
