package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.api.event.queues.beans.TaskBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.ServicesHolder;

public class TaskBeanProcessor extends AbstractQueueProcessor<TaskBean> {
	
	private AtomQueueProcessor<TaskBean, SubTaskAtom> atomQueueProcessor;
	
	public TaskBeanProcessor() {
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
				queueBean.setMessage("Job-queue aborted before completion (requested)");
				atomQueueProcessor.terminate();
			} else if (queueBean.getPercentComplete() >= 99.49) {//99.49 to catch rounding errors
				//Completed successfully
				broadcaster.broadcast(Status.COMPLETE, 100d, "Scan completed.");
			} else {
				//Failed: latch released before completion
				broadcaster.broadcast(Status.FAILED, "Job-queue failed (caused by process Atom)");
				//As we don't know the origin of the failure, pause *this* queue
				IQueueControllerService controller = ServicesHolder.getQueueControllerService();
				controller.pauseQueue(ServicesHolder.getQueueService().getJobQueueID());
			}
			//And we're done, so let other processes continue
			executionEnded();
			
		} finally {
			lock.unlock();
			
			//This should be run after we've reported the queue final state
			//This must be after unlock call, otherwise terminate gets stuck.
			atomQueueProcessor.tidyQueue();
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

	@Override
	public Class<TaskBean> getBeanClass() {
		return TaskBean.class;
	}
	
	/*
	 * For tests.
	 */
	public AtomQueueProcessor<TaskBean, SubTaskAtom> getAtomQueueProcessor() {
		return atomQueueProcessor;
	}

}
