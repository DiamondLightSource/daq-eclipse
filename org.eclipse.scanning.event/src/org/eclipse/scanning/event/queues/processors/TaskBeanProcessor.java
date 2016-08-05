package org.eclipse.scanning.event.queues.processors;

import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.AtomQueueServiceUtils;
import org.eclipse.scanning.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.event.queues.beans.TaskBean;

public class TaskBeanProcessor extends AbstractQueueProcessor<TaskBean> {
	
	private AtomQueueProcessor<TaskBean, SubTaskAtom> atomQueueProcessor;
	
	private final ReentrantLock lock;
//	private final Condition analysing;
	private volatile boolean locked;
	
	public TaskBeanProcessor() {
		atomQueueProcessor = new AtomQueueProcessor<>(this);
		lock = new ReentrantLock();
//		analysing = lock.newCondition();
	}

	@Override
	public void execute() throws EventException, InterruptedException {
		setExecuted();
		//Do most of the work of processing in the atomQueueProcessor...
		atomQueueProcessor.run();

		//...do the post-match analysis in here!
		if (isTerminated()) {
			broadcaster.broadcast("Job-queue aborted before completion (requested)");
			AtomQueueServiceUtils.terminateQueueProcess(atomQueueProcessor.getActiveQueueName(), queueBean);
		} else if (queueBean.getPercentComplete() >= 99.5) {
			//Completed successfully
			broadcaster.broadcast(Status.COMPLETE, 100d, "Scan completed.");
		}
		
		//This should be run after we've reported the queue final state
		atomQueueProcessor.tidyQueue();
		
		//And we're done, so let other processes continue
		if (locked) {
			//FIXME This doesn't work! Why?!
//			analysing.signal();
			locked = false;
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
	public void terminate() throws EventException {
		//Reentrant lock ensures execution method (and hence tidy up) complete 
		//before terminate
		try{
			lock.lock();
			locked = true;
			
			setTerminated();
			processorLatch.countDown();
	
			//FIXME This doesn't work! Why?!
//			analysing.await();
			
			while (locked){
				Thread.sleep(100);
			}
		} catch (InterruptedException iEx) {
			throw new EventException(iEx);
		} finally {
			lock.unlock();
			locked = false;
		}
	}

	@Override
	public Class<TaskBean> getBeanClass() {
		return TaskBean.class;
	}

}
