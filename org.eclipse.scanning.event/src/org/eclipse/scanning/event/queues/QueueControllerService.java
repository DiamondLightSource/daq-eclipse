package org.eclipse.scanning.event.queues;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.KillBean;
import org.eclipse.scanning.api.event.alive.PauseBean;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueControllerService implements IQueueControllerService {
	
	private static final Logger logger = LoggerFactory.getLogger(QueueControllerService.class);
	
	private final IEventService eventService;
	private final IQueueService queueService;
	
	private final String commandSetName, commandTopicName;
	private final URI uri;
	
	public QueueControllerService() {
		//Set up services
		eventService = ServicesHolder.getEventService();
		queueService = ServicesHolder.getQueueService();
		
		//Get the queue service configuration
		commandSetName = queueService.getCommandSetName();
		commandTopicName = queueService.getCommandTopicName();
		uri = queueService.getURI();
	}

	@Override
	public void start() throws EventException {
		queueService.start();
	}

	@Override
	public void stop(boolean force) throws EventException {
		queueService.stop(force);
	}

	@Override
	public <T extends Queueable> void submit(T bean, String queueID) {
		// TODO Auto-generated method stub
	}

	@Override
	public <T extends Queueable> void remove(T bean, String queueID) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends Queueable> void reorder(T bean, int move, String queueID) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends Queueable> void pause(T bean, String queueID) throws EventException {
		//Check that bean has not already been paused & has moved beyond submission
		Queueable queuedBean = getBeanFromConsumer(bean, queueID);
		if (queuedBean == null) {
			logger.error("No bean '"+bean.getName()+"' in '"+queueID+"' queue");
			throw new EventException("Bean not in this queue.");
		} else if (queuedBean.getStatus().isPaused()) {
			logger.warn("Attempted to pause already paused bean '"+bean.getName()+"'.");
			throw new EventException("Bean is already paused.");
		} else if (queuedBean.getStatus().equals(Status.SUBMITTED)) {
			logger.warn("Attempted to pause bean '"+bean.getName()+"' still in submission queue.");
			throw new EventException("Bean not being processed, only submitted.");
		}
		
		//Command the process (checks bean type first)
		publishQueueProcessCommand(bean, queueID, Status.REQUEST_PAUSE);
	}

	@Override
	public <T extends Queueable> void resume(T bean, String queueID) throws EventException {
		//Check that bean has not already been paused & has moved beyond submission
		Queueable queuedBean = getBeanFromConsumer(bean, queueID);
		if (queuedBean == null) {
			logger.error("No bean '"+bean.getName()+"' in '"+queueID+"' queue");
			throw new EventException("Bean not in this queue.");
		} else if (queuedBean.getStatus().isResumed() || queuedBean.getStatus().isRunning()) {
			logger.warn("Attempted to resume already resumed bean '"+bean.getName()+"'.");
			throw new EventException("Bean is already resumed.");
		} else if (queuedBean.getStatus().equals(Status.SUBMITTED)) {
			logger.warn("Attempted to resume bean '"+bean.getName()+"' still in submission queue.");
			throw new EventException("Bean not being processed, only submitted.");
		}

		//Command the process (checks bean type first)
		publishQueueProcessCommand(bean, queueID, Status.REQUEST_RESUME);
	}

	@Override
	public <T extends Queueable> void terminate(T bean, String queueID) throws EventException {
		//Check that bean has not already been paused & has moved beyond submission
		Queueable queuedBean = getBeanFromConsumer(bean, queueID);
		if (queuedBean == null) {
			logger.error("No bean '"+bean.getName()+"' in '"+queueID+"' queue");
			throw new EventException("Bean not in this queue.");
		} else if (queuedBean.getStatus().isTerminated()) {
			logger.warn("Attempted to terminate already terminated bean '"+bean.getName()+"'.");
			throw new EventException("Bean is already terminated.");
		} else if (queuedBean.getStatus().equals(Status.SUBMITTED)) {
			logger.warn("Attempted to terminate bean '"+bean.getName()+"' still in submission queue. Will remove instead.");
			remove(bean, queueID);
		}

		//Command the process (checks bean type first)
		publishQueueProcessCommand(bean, queueID, Status.REQUEST_TERMINATE);
	}
	
	private <T extends Queueable> Queueable getBeanFromConsumer(T bean, String queueID) throws EventException {
		List<Queueable> consumerQueue;
		
		//Search for bean in StatusSet (do this first as it's more likely for this operation)
		consumerQueue = queueService.getQueue(queueID).getConsumer().getStatusSet();
		for (Queueable queuedBean : consumerQueue) {
			if (queuedBean.getUniqueId().equals(bean.getUniqueId())) {
					return queuedBean;
			}
		}
		
		//If it's not been returned, look in the submit queue
		consumerQueue = queueService.getQueue(queueID).getConsumer().getSubmissionQueue();
		for (Queueable queuedBean : consumerQueue) {
			if (queuedBean.getUniqueId().equals(bean.getUniqueId())) {
				return queuedBean;
			}
		}
		
		//Return null if it's not there
		return null;
	}
	
	private <T extends Queueable> void publishQueueProcessCommand(T bean, String queueID, Status state) throws EventException {
		//Check bean is right type for the queue
		if (queueID.equals(queueService.getJobQueueID()) && 
				(!(bean instanceof QueueBean))) {
			throw new IllegalArgumentException("Job-queue cannot handle non-QueueBeans");
		} else {
			if (!(bean instanceof QueueAtom)) {
				throw new IllegalArgumentException("Active-queue cannot handle non-QueueAtoms");
			}
		}
		
		//Set up the publisher
		String statusTopicName = queueService.getQueue(queueID).getStatusTopicName();
		IPublisher<T> commander = eventService.createPublisher(uri, statusTopicName);
		
		//Set the bean status, publish and disconnect
		bean.setStatus(state);
		commander.broadcast(bean);
		commander.disconnect();
	}

	@Override
	public void pauseQueue(String queueID) throws EventException {
		doPauseResume(queueID, true);
	}

	@Override
	public void resumeQueue(String queueID) throws EventException {
		doPauseResume(queueID, false);
	}
	
	private void doPauseResume(String queueID, boolean pause) throws EventException {
		IPublisher<PauseBean> pausenator = eventService.createPublisher(uri, commandTopicName);
		pausenator.setStatusSetName(commandSetName);
		
		//Create the PauseBean & configure
		PauseBean pauser = new PauseBean();
		UUID consumerID = queueService.getQueue(queueID).getConsumerID();
		pauser.setConsumerId(consumerID);
		pauser.setPause(pause);
		pausenator.broadcast(pauser);
		pausenator.disconnect();
	}

	@Override
	public void killQueue(String queueID, boolean disconnect, boolean exitProcess) throws EventException {
		IPublisher<KillBean> killenator = eventService.createPublisher(uri, commandTopicName);
		killenator.setStatusSetName(commandSetName);
		
		//Create KillBean & configure
		KillBean killer = new KillBean();
		UUID consumerID = queueService.getQueue(queueID).getConsumerID();
		killer.setConsumerId(consumerID);
		killer.setDisconnect(disconnect);
		killer.setExitProcess(exitProcess);
		killenator.broadcast(killer);
		killenator.disconnect();
	}

}
