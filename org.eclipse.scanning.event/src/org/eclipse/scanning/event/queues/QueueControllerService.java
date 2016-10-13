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
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public class QueueControllerService implements IQueueControllerService {
	
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
		// TODO Auto-generated method stub
		
//		List<T> statusSet = queueService.getQueue(queueID).getConsumer().getStatusSet();
//		for (T liveBean : statusSet) {
//			if (liveBean.getUniqueID().equals(bean.getUniqueID()) && 
//					liveBean.getStatus().isPaused()) {
//				throw new EventException()
//			}
//		}
	}

	@Override
	public <T extends Queueable> void resume(T bean, String queueID) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends Queueable> void terminate(T bean, String queueID) throws EventException {
		// TODO Auto-generated method stub

	}
	
	private <T extends Queueable> boolean isStatusAlreadySet(String queueID, T bean) {
		return false;
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
