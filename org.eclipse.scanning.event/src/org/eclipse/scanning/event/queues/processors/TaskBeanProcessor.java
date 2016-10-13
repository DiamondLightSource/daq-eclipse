package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.AtomQueueServiceUtils;
import org.eclipse.scanning.event.queues.QueueServicesHolder;
import org.eclipse.scanning.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.event.queues.beans.TaskBean;

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
				AtomQueueServiceUtils.terminateQueueProcess(atomQueueProcessor.getActiveQueueName(), queueBean);
			} else if (queueBean.getPercentComplete() >= 99.5) {
				//Completed successfully
				broadcaster.broadcast(Status.COMPLETE, 100d, "Scan completed.");
			} else {
				//Failed: latch released before completion
				broadcaster.broadcast(Status.FAILED, "Job-queue failed (caused by process Atom)");
				AtomQueueServiceUtils.pauseQueue(QueueServicesHolder.getQueueService().getJobQueueID());
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

	@Override
	public Class<TaskBean> getBeanClass() {
		return TaskBean.class;
	}

}
