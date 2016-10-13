package org.eclipse.scanning.event.queues;

import java.net.URI;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.PauseBean;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.queues.IQueueControllerService;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.IQueueable;

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
	public <T extends IQueueable> void submit(T bean, String queueID) {
		
	}

	@Override
	public <T extends IQueueable> void remove(T bean, String queueID) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends IQueueable> void reorder(T bean, int move, String queueID) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public <T extends IQueueable> void pause(T bean, String queueID) throws EventException {
		// TODO Auto-generated method stub
	}

	@Override
	public <T extends IQueueable> void resume(T bean, String queueID) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends IQueueable> void terminate(T bean, String queueID) throws EventException {
		// TODO Auto-generated method stub

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
	public void killQueue(String queueID, boolean disconnect, boolean exitProcess) {
		// TODO Auto-generated method stub

	}

}
