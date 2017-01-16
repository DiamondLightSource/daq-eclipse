package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.queues.beans.SubTaskAtom;
import org.eclipse.scanning.api.event.queues.beans.TaskBean;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.event.queues.QueueProcess;
import org.eclipse.scanning.event.queues.ServicesHolder;

public class TaskBeanProcess<T extends Queueable> extends QueueProcess<TaskBean, T> {
	
	public static final String BEAN_CLASS_NAME = TaskBean.class.getName();
	
	private AtomQueueProcessor<TaskBean, SubTaskAtom, T> atomQueueProcessor;
	
	public TaskBeanProcess(T bean, IPublisher<T> publisher, Boolean blocking) throws EventException {
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
		//...do the post-match analysis in this class!
		try {
			postMatchAnalysisLock.lockInterruptibly();
			if (isTerminated()) {
				atomQueueProcessor.terminate();
				queueBean.setMessage("Job-queue aborted before completion (requested)");
			}else if (queueBean.getPercentComplete() >= 99.49) {//99.49 to catch rounding errors
				//Completed successfully                 
				updateBean(Status.COMPLETE, 100d, "Scan completed.");
			} else {
				//Failed: latch released before completion
				updateBean(Status.FAILED, null, "Job-queue failed (caused by process Atom)");
				//As we don't know the origin of the failure, pause *this* queue
				IQueueControllerService controller = ServicesHolder.getQueueControllerService();
				controller.pauseQueue(ServicesHolder.getQueueService().getJobQueueID());
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
	
	@Override
	public Class<TaskBean> getBeanClass() {
		return TaskBean.class;
	}

	public AtomQueueProcessor<TaskBean, SubTaskAtom, T> getAtomQueueProcessor() {
		return atomQueueProcessor;
	}
	
//	public TaskBeanProcess() {
//		super();
//		atomQueueProcessor = new AtomQueueProcessor<>(this);
//	}
//
//	@Override
//	public void execute() throws EventException, InterruptedException {
//		setExecuted();
//		//Do most of the work of processing in the atomQueueProcessor...
//		atomQueueProcessor.run();
//
//		//...do the post-match analysis in here!
//		try {
//			lock.lockInterruptibly();
//			if (isTerminated()) {
//				queueBean.setMessage("Job-queue aborted before completion (requested)");
//				atomQueueProcessor.terminate();
//			} else if (queueBean.getPercentComplete() >= 99.49) {//99.49 to catch rounding errors
//				//Completed successfully
//				broadcaster.updateBean(Status.COMPLETE, 100d, "Scan completed.");
//			} else {
//				//Failed: latch released before completion
//				broadcaster.updateBean(Status.FAILED, null, "Job-queue failed (caused by process Atom)");
//				//As we don't know the origin of the failure, pause *this* queue
//				IQueueControllerService controller = ServicesHolder.getQueueControllerService();
//				controller.pauseQueue(ServicesHolder.getQueueService().getJobQueueID());
//			}
//			//And we're done, so let other processes continue
//			executionEnded();
//			
//		} finally {
//			lock.unlock();
//			
//			//This should be run after we've reported the queue final state
//			//This must be after unlock call, otherwise terminate gets stuck.
//			atomQueueProcessor.tidyQueue();
//			
//			/*
//			 * N.B. Broadcasting needs to be done last; otherwise the next 
//			 * queue may start when we're not ready. Broadcasting should not 
//			 * happen if we've been terminated.
//			 */
//			if (!isTerminated()){
//				broadcaster.broadcast();
//			}
//		}
//		
//	}
//
//	@Override
//	public void pause() throws EventException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public void resume() throws EventException {
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public Class<TaskBean> getBeanClass() {
//		return TaskBean.class;
//	}
//	
//	/*
//	 * For tests.
//	 */
//	public AtomQueueProcessor<TaskBean, SubTaskAtom> getAtomQueueProcessor() {
//		return atomQueueProcessor;
//	}

}
