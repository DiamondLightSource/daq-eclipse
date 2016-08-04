package org.eclipse.scanning.event.queues.processors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.queues.IQueueBroadcaster;
import org.eclipse.scanning.api.event.queues.beans.IAtomBeanWithQueue;
import org.eclipse.scanning.api.event.queues.beans.IAtomWithChildQueue;
import org.eclipse.scanning.api.event.queues.beans.IQueueable;
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
 * @param <Q> Bean extending {@link StatusBean} from the child queue.
 * @param <T> Bean extending {@link Queueable}, the parent queue atom.
 */
public class QueueListener<P extends IQueueable, Q extends StatusBean> implements IBeanListener<Q> {
	
	private static Logger logger = LoggerFactory.getLogger(QueueListener.class);
	
	//Infrastructure
	private final IQueueBroadcaster<? extends Queueable> broadcaster;
	private final CountDownLatch processorLatch;
	
	//
	private P parent;
	private double initPercent;
	private boolean firstTime = true;
	private Map<String, ProcessStatus> children = new HashMap<>();
	private boolean childCommand;
	private final double queueCompletePercentage = 99.5;
	
	private QueueListener(IQueueBroadcaster<? extends Queueable> broadcaster, P parent, CountDownLatch procLatch, boolean fakeArg) {
		this.broadcaster = broadcaster;
		this.parent = parent;
		processorLatch = procLatch;
	}
	
	@SuppressWarnings("unchecked")
	public QueueListener(IQueueBroadcaster<? extends Queueable> broadcaster, P parent, CountDownLatch procLatch) throws EventException {
		this(broadcaster, parent, procLatch, true);
		if (parent instanceof IAtomBeanWithQueue<?>) {
			List<?> children = ((IAtomBeanWithQueue<?>)parent).getAtomQueue().getQueue();
			initChildList((List<Q>) children);//QueueAtom extends StatusBean, so this cast is OK.
		} else {
			throw new EventException("Listener has no child beans to monitor. Cannot continue.");
		}
	}
	
	public QueueListener(IQueueBroadcaster<? extends Queueable> broadcaster, P parent, CountDownLatch procLatch, Q child) {
		this(broadcaster, parent, procLatch, true);
		children.put(child.getUniqueId(), new ProcessStatus(child));
	}
	
	public QueueListener(IQueueBroadcaster<? extends Queueable> broadcaster, P parent, CountDownLatch procLatch, List<Q> children) {
		this(broadcaster, parent, procLatch, true);
		initChildList(children);
	}
	
	private void initChildList(List<Q> children) {
		double childWork, totalWork = 0d;
		for (Q child : children) {
			String childID = child.getUniqueId();
			this.children.put(childID, new ProcessStatus(child));
			
			/*
			 * Record the runtime of each child, if it is an instance of 
			 * Queueable. If not, assume each child does an equal amount 
			 * of work (this is set in the inner class {@see ProcessStatus}).
			 */
			if (child instanceof Queueable) {
				childWork = new Double(((Queueable)child).getRunTime());
				totalWork = totalWork + childWork;
				this.children.get(childID).setWorkFraction(childWork);
			}
		}
		/*
		 * Normalise the amount of work done per child.
		 */
		for(String childID : this.children.keySet()) {
			childWork = this.children.get(childID).getWorkFraction();
			this.children.get(childID).setWorkFraction(childWork/totalWork);
		}	
	}

	@Override
	public void beanChangePerformed(BeanEvent<Q> evt) {
		boolean broadcastUpdate = false, beanCompleted = false, failed = false;
		Q bean = evt.getBean();
		String beanID = bean.getUniqueId();
		//If this bean is not from the parent ignore it.
		if (!children.containsKey(beanID)) return;
		
		if (!children.get(beanID).isOperating()) { 
			if (bean.getStatus().isRunning()) {
				children.get(beanID).setOperating(true);
			} else {
				//The bean should be running if it's operating. Something's wrong.
				logger.warn("'"+bean.getName()+"' is not set to operating, but is running (Status = "+bean.getStatus()+").");	
			}
		}
		
		//Whatever happens update the message on the parent
		parent.setMessage("'"+bean.getName()+"': "+bean.getMessage());
		
		//The percent complete changed, update the parent
		if (bean.getPercentComplete() != children.get(beanID).getPercentComplete()) {
			//First time we need to change the parent percent, get its initial value
			if (firstTime) {
				initPercent = parent.getPercentComplete();
				firstTime = false;
			}
			double childPercent = bean.getPercentComplete();
			double childContribution = (queueCompletePercentage - initPercent) * children.get(beanID).getWorkFraction();
			double parentPercent = parent.getPercentComplete() + childContribution / 100 * (childPercent - children.get(beanID).getPercentComplete());
			parent.setPercentComplete(parentPercent);
			children.get(beanID).setPercentComplete(childPercent);
			broadcastUpdate = true;
		}
		
		/*
		 * If the status of the child changed, test if we need to update the
		 * parent. Transitions handled: (for each, set queue message)
		 * -> PAUSED (from elsewhere): REQUEST_PAUSE parent
		 * -> TERMINATED (from elsewhere): REQUEST_TERMINATE parent
		 * -> COMPLETE
		 * -> RESUMED/RUNNING from PAUSED: REQUEST_RESUME
		 * -> FAILED: FAILED (TODO for TaskBean, pause consumer on completion)
		 */
		if (bean.getStatus() != children.get(beanID).getStatus()) {
			//Update the status of the process
			children.get(beanID).setStatus(bean.getStatus());
			
			if (bean.getStatus().isRunning() || bean.getStatus().isResumed()) {
				//RESUMED/RUNNING
				if (parent.getStatus().isPaused()) {
					// -parent is paused => unpause it
					parent.setStatus(Status.REQUEST_RESUME);
					((IAtomWithChildQueue)parent).setQueueMessage("Resume requested from '"+bean.getName()+"'");
					childCommand = true;
					broadcastUpdate = true;
				} else {
					// -DEFAULT for normal running
					((IAtomWithChildQueue)parent).setQueueMessage("Running...");
					childCommand = false;
					broadcastUpdate = true;
				}
			} else if (bean.getStatus().isPaused()) {
				//PAUSE
				parent.setStatus(Status.REQUEST_PAUSE);
				((IAtomWithChildQueue)parent).setQueueMessage("Pause requested from '"+bean.getName()+"'");
				childCommand = true;
				broadcastUpdate = true;
			} else if (bean.getStatus().isTerminated()) {
				//TERMINATE
				parent.setStatus(Status.REQUEST_TERMINATE);
				((IAtomWithChildQueue)parent).setQueueMessage("Termination requested from '"+bean.getName()+"'");
				childCommand = true;
				broadcastUpdate = true;
			} else if (bean.getStatus().isFinal()) {
				//FINAL states
				children.get(beanID).setOperating(false);
				beanCompleted = true;
				if (bean.getStatus().equals(Status.COMPLETE)) {
					((IAtomWithChildQueue)parent).setQueueMessage("'"+bean.getName()+"' completed successfully.");
					broadcastUpdate = true;
				} else {
					//Status.FAILED or unhandled state
					((IAtomWithChildQueue)parent).setQueueMessage("Failure caused by '"+bean.getName()+"'");
					broadcastUpdate = true;
					childCommand = true;
					failed = true;
				}
			}
		}
		
		//If we have an update to broadcast, do it!
		if (broadcastUpdate) {
			try {
				broadcaster.childQueueBroadcast();
			} catch (EventException evEx) {
				logger.error("Broadcasting '"+bean.getName()+"' failed with: "+evEx.getMessage());
			}
		}
		
		/*
		 * If no beans are still operating and all beans have concluded, 
		 * release the latch.
		 */
		if (beanCompleted) {
			boolean concluded = true, operating = false;
			for (String childID : children.keySet()) {
				concluded = concluded && children.get(childID).isConcluded();
				operating = operating || children.get(childID).isOperating();
			}
			if (concluded && !operating || failed) {
				if (!failed) {
					parent.setMessage("Running finished.");
					((IAtomWithChildQueue)parent).setQueueMessage("All child processes complete.");
				}
				try {
					broadcaster.childQueueBroadcast();
				} catch (EventException evEx) {
					logger.error("Broadcasting completed message failed with: "+evEx.getMessage());
				}
				processorLatch.countDown();
			}
		}
	}
	
	/**
	 * Mark the last command status change of parent as resulting from a 
	 * command from  a child process (to prevent instruction loops).
	 * 
	 * @return true if last command to parent came from a child.
	 */
	public boolean isChildCommand() {
		return childCommand;
	}
	
//	private U bean;
//	private AbstractQueueProcessor<U> proc;
//	
//	private String beanUID;
//	private double latestPercent = 0;
//	private Status latestStatus = Status.NONE;
//	private double beanInitPercent;
//	
//	private boolean firstRun = true, beanFinal = false;
//	
//	public QueueListener(U bean, AbstractQueueProcessor<U> proc, String beanUID, double beanInitPercent) {
//		this.bean = bean;
//		this.proc = proc;
//		this.beanUID = beanUID;
//		this.beanInitPercent = beanInitPercent;
//	}

//	@Override
//	public void beanChangePerformed(BeanEvent<Q> evt) {
//		if (beanFinal) return;
//		
//		T qBean = evt.getBean();
//		if (qBean.getUniqueId().equals(beanUID)) {
//			//Update scan percent complete
//			if(qBean.getPercentComplete() != latestPercent) {
//				//TODO This might need changing if using time to determine completeness.
//				latestPercent = qBean.getPercentComplete();
//				double newPercent = (100 - beanInitPercent) * (latestPercent / 100);
//				bean.setPercentComplete(beanInitPercent + newPercent);
//			}
//			
//			//Update scan status
//			if (!qBean.getStatus().equals(latestStatus)) {
//				latestStatus = qBean.getStatus();
//				if (latestStatus.isRunning()) {
//					if (firstRun == true) {
//						//Nothing to do, this happens on the first pass.
//						firstRun = false;
//					} else if (bean.getStatus().isPaused()) {
//						//Resume requested elsewhere and this process paused
//						try {
//							String msg = "Resume called from '"+qBean.getName()+"'"; 
//						if (qBean.getMessage() != null) {
//							msg = msg+" with message: '"+qBean.getMessage()+"'";
//							}
//							((IAtomWithChildQueue)bean).setQueueMessage(msg);
//							proc.resume();
//						} catch(EventException evEx) {
//							((IAtomWithChildQueue)bean).setQueueMessage("Failed to resume paused process.");
//							logger.error("Failed to resume paused process.");
//						}
//					} else {
//						weirdStatus(qBean.getName());
//					}
//				} else if (latestStatus.isPaused() && !bean.getStatus().isPaused()) {
//					//Pause requested elsewhere
//					try {
//						String msg = "Pause called from '"+qBean.getName()+"'"; 
//						if (qBean.getMessage() != null) {
//							msg = msg+" with message: '"+qBean.getMessage()+"'";
//						}
//						((IAtomWithChildQueue)bean).setQueueMessage(msg);
//						proc.pause();
//					} catch(EventException evEx) {
//						((IAtomWithChildQueue)bean).setQueueMessage("Failed to pause process.");
//						logger.error("Failed to pause process.");
//					}
//				} else if (latestStatus.isTerminated()) {
//					if (bean.getStatus().isTerminated()) {
//						//If bean is terminated already, there's nothing to do
//					} else {
//						//Terminate requested elsewhere
//						String msg = "Terminate called from '"+qBean.getName()+"'"; 
//						if (qBean.getMessage() != null) {
//							msg = msg+" with message: '"+qBean.getMessage()+"'";
//						}
//						((IAtomWithChildQueue)bean).setQueueMessage(msg);
//						bean.setStatus(Status.REQUEST_TERMINATE);
//					}
//				} else if (latestStatus == Status.COMPLETE) {
//					bean.setStatus(Status.COMPLETE);
//					bean.setPercentComplete(100d);
//				} else if (latestStatus.isFinal()) {
//					String msg = "Error in execution of '"+qBean.getName()+"'."; 
//					if (qBean.getMessage() != null) {
//						msg = msg+" Message: '"+qBean.getMessage()+"'";
//					}
//					((IAtomWithChildQueue)bean).setQueueMessage(msg);
//					bean.setStatus(Status.FAILED);
//				} else {
//					weirdStatus(qBean.getName());
//				}
//				
//				//This will stop the while loop in execute() so needs to be last
//				if (latestStatus.isFinal() && !latestStatus.isRequest()) {
//					beanFinal = true;
//					proc.setRunComplete(true);
//				}
//			}
//			//Don't know the current state or percent complete, so don't set them.
//			try {
//				proc.broadcast(bean, null, null);
//			} catch(EventException evEx) {
//				logger.error("Broadcasting bean failed with: "+evEx.getMessage());
//			}
//			return;
//		} else return;//Ignore other beans
//	}
//	
//	private void weirdStatus(String qBeanName) {
//		((IAtomWithChildQueue)bean).setQueueMessage("Received unexpected status from '"+qBeanName+"' bean (Scan bean status: '"+latestStatus+"'; Queue bean status: '"+bean.getStatus()+"') . Continuing...");
//		logger.debug("Unexpected, but not error causing bean Status. Continuing...");
//	}
	
	private class ProcessStatus {
		
		private Status status;
		private double percentComplete;
		private double workFraction = 1d;
		private boolean operating = false;
		
		public ProcessStatus(StatusBean bean) {
			status = bean.getStatus();
			percentComplete = bean.getPercentComplete();
		}

		public double getPercentComplete() {
			return percentComplete;
		}
		
		public void setPercentComplete(double percentComplete) {
			this.percentComplete = percentComplete;
		}
		
		public double getWorkFraction() {
			return workFraction;
		}
		
		public void setWorkFraction(double workFraction) {
			this.workFraction = workFraction;
		}
		
		public Status getStatus() {
			return status;
		}
		
		public void setStatus(Status status) {
			this.status = status;
		}
		
		public boolean isOperating() {
			return operating;
		}
		
		public void setOperating(boolean operating) {
			this.operating = operating;
		}
		
		/**
		 * Test whether the process has actually been started (NONE is being 
		 * used to indicate nothing done yet).
		 * @return true if process state is final & is NONE
		 */
		public boolean isConcluded() {
			return status.isFinal() && status != Status.NONE;
		}

		@Override
		public String toString() {
			return "ProcessStatus [percentComplete=" + percentComplete + ", workFraction=" + workFraction + ", status="
					+ status + ", active=" + operating + "]";
		}
	}

}
