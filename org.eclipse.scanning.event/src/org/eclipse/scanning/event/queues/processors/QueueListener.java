package org.eclipse.scanning.event.queues.processors;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.queues.beans.IAtomWithChildQueue;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QueueListener provides a bridge between one queue atom and its dependent 
 * queue. When an event in the child queue causes the listener to fire, it 
 * reads the {@link Status} and percent complete of the bean causing the event 
 * and updates the parent bean appropriately.
 * 
 * The QueueListener is used in the ScanAtomProcessor and also in the 
 * AtomQueueProcessor in the first instance.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Bean extending {@link StatusBean} from the child queue.
 * @param <U> Bean extending {@link Queueable}, the parent queue atom.
 */
class QueueListener<T extends StatusBean, U extends Queueable> implements IBeanListener<T> {
	
	private static Logger logger = LoggerFactory.getLogger(QueueListener.class);
	
	private U bean;
	private AbstractQueueProcessor<U> proc;
	
	private String beanUID;
	private double latestPercent = 0;
	private Status latestStatus = Status.NONE;
	private double beanInitPercent;
	
	private boolean firstRun = true;
	
	public QueueListener(U bean, AbstractQueueProcessor<U> proc, String beanUID, double beanInitPercent) {
		this.bean = bean;
		this.proc = proc;
		this.beanUID = beanUID;
		this.beanInitPercent = beanInitPercent;
	}

	@Override
	public void beanChangePerformed(BeanEvent<T> evt) {
		T qBean = evt.getBean();
		if (qBean.getUniqueId().equals(beanUID)) {
			//Update scan percent complete
			if(qBean.getPercentComplete() != latestPercent) {
				//TODO This might need changing if using time to determine completeness.
				latestPercent = qBean.getPercentComplete();
				double newPercent = (100 - beanInitPercent) * (latestPercent / 100);
				bean.setPercentComplete(beanInitPercent + newPercent);
			}
			
			//Update scan status
			if (!qBean.getStatus().equals(latestStatus)) {
				latestStatus = qBean.getStatus();
				if (latestStatus.isRunning()) {
					if (firstRun == true) {
						//Nothing to do, this happens on the first pass.
						firstRun = false;
					} else if (bean.getStatus().isPaused()) {
						//Resume requested elsewhere and this process paused
						try {
							String msg = "Resume called from '"+qBean.getName()+"'"; 
						if (qBean.getMessage() != null) {
							msg = msg+" with message: '"+qBean.getMessage()+"'";
							}
							((IAtomWithChildQueue)bean).setQueueMessage(msg);
							proc.resume();
						} catch(EventException evEx) {
							((IAtomWithChildQueue)bean).setQueueMessage("Failed to resume paused process.");
							logger.error("Failed to resume paused process.");
						}
					} else {
						weirdStatus(qBean.getName());
					}
				} else if (latestStatus.isPaused() && !bean.getStatus().isPaused()) {
					//Pause requested elsewhere
					try {
						String msg = "Pause called from '"+qBean.getName()+"'"; 
						if (qBean.getMessage() != null) {
							msg = msg+" with message: '"+qBean.getMessage()+"'";
						}
						((IAtomWithChildQueue)bean).setQueueMessage(msg);
						proc.pause();
					} catch(EventException evEx) {
						((IAtomWithChildQueue)bean).setQueueMessage("Failed to pause process.");
						logger.error("Failed to pause process.");
					}
				} else if (latestStatus.isTerminated()) {
					if (bean.getStatus().isTerminated()) {
						//If bean is terminated already, there's nothing to do
					} else {
						//Terminate requested elsewhere
						String msg = "Terminate called from '"+qBean.getName()+"'"; 
						if (qBean.getMessage() != null) {
							msg = msg+" with message: '"+qBean.getMessage()+"'";
						}
						((IAtomWithChildQueue)bean).setQueueMessage(msg);
						bean.setStatus(Status.REQUEST_TERMINATE);
					}
				} else if (latestStatus == Status.COMPLETE) {
					bean.setStatus(Status.COMPLETE);
					bean.setPercentComplete(100d);
				} else if (latestStatus.isFinal()) {
					String msg = "Error in execution of '"+qBean.getName()+"'."; 
					if (qBean.getMessage() != null) {
						msg = msg+" Message: '"+qBean.getMessage()+"'";
					}
					((IAtomWithChildQueue)bean).setQueueMessage(msg);
					bean.setStatus(Status.FAILED);
				} else {
					weirdStatus(qBean.getName());
				}
				
				//This will stop the while loop in execute() so needs to be last
				if (latestStatus.isFinal() && !latestStatus.isRequest()) proc.setRunComplete(true);
			}
			//Don't know the current state or percent complete, so don't set them.
			try {
				proc.broadcast(bean, null, null);
			} catch(EventException evEx) {
				logger.error("Broadcasting bean failed with: "+evEx.getMessage());
			}
			return;
		} else return;//Ignore other beans
	}
	
	private void weirdStatus(String qBeanName) {
		((IAtomWithChildQueue)bean).setQueueMessage("Received unexpected status from '"+qBeanName+"' bean (Scan bean status: '"+latestStatus+"'; Queue bean status: '"+bean.getStatus()+"') . Continuing...");
		logger.debug("Unexpected, but not error causing bean Status. Continuing...");
	}

}
