package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.beans.SubTaskAtom;

public class SubTaskAtomProcessor extends AbstractQueueProcessor<SubTaskAtom> {
	
	private AtomQueueProcessor<SubTaskAtom, QueueAtom> atomQueueProcessor;
	
	public SubTaskAtomProcessor() {
		atomQueueProcessor = new AtomQueueProcessor<>(this);
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		//Do most of the work of processing in the atomQueueProcessor...
		atomQueueProcessor.run();
		
		//...do the post-match analysis in here!
//		if (queueBean.getPercentComplete() >= 99.5) {
			//Completed successfully
			broadcaster.broadcast(Status.COMPLETE, 100d, "Scan completed.");
//		}

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
		// TODO Auto-generated method stub

	}

	@Override
	public Class<SubTaskAtom> getBeanClass() {
		return SubTaskAtom.class;
	}

}
