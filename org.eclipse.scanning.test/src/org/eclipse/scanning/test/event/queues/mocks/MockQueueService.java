package org.eclipse.scanning.test.event.queues.mocks;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;

public class MockQueueService implements IQueueService {
	
	private IQueue<QueueBean> jobQueue;
	private String jobQueueID;
	
	private String commandTopicName, commandQueueName;
	
	private Map<String, IQueue<QueueAtom>> activeQueues = new HashMap<>();
	private List<String> activeQueueIDs = new ArrayList<>();
	private int nrActiveQueues = 0;
	
	private URI uri;
	
	private boolean active = false, forced = false;
	
	public MockQueueService() {
		jobQueue = null;
		jobQueueID = "mock-job-queue";
		commandTopicName = "mock-command-topic";
		commandQueueName = "mock-command-queue";
		try {
			uri = new URI("mock.uri");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MockQueueService(IQueue<QueueBean> mockJobQueue) {
		this.jobQueue = mockJobQueue;
		jobQueueID = mockJobQueue.getQueueID();
		commandTopicName = mockJobQueue.getCommandTopicName();
		commandQueueName = mockJobQueue.getCommandSetName();
		try {
			uri = new URI("mock.uri");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public MockQueueService(IQueue<QueueBean> mockJobQueue, IQueue<QueueAtom> mockActiveQueue) {
		this(mockJobQueue);
		activeQueues.put(mockActiveQueue.getQueueID(), mockActiveQueue);
		activeQueueIDs.add(mockActiveQueue.getQueueID());
		nrActiveQueues = 1;
	}

	@Override
	public void init() throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disposeService() throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() throws EventException {
		if (jobQueue == null) throw new EventException("QueueService not supposed to be started with no job-queue!");
		jobQueue.getConsumer().start();
		for (String queueID : activeQueues.keySet()) {
			activeQueues.get(queueID).getConsumer().start();
		}
		
		active = true;
	}

	@Override
	public void stop(boolean force) throws EventException {
		active = false;
		forced = force;

	}

	@Override
	public String registerNewActiveQueue() throws EventException {
		String aqID = activeQueueIDs.get(nrActiveQueues);
		nrActiveQueues++;
		return aqID;
	}

	@Override
	public void deRegisterActiveQueue(String queueID, boolean force) throws EventException {
			nrActiveQueues--;
	}

	@Override
	public void startActiveQueue(String queueID) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopActiveQueue(String queueID, boolean force) throws EventException {
		// TODO Auto-generated method stub

	}



	@Override
	public IQueue<QueueBean> getJobQueue() {
		return jobQueue;
	}

	@Override
	public String getJobQueueID() {
		return jobQueueID;
	}

	@Override
	public Set<String> getAllActiveQueueIDs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isActiveQueueRegistered(String queueID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IQueue<QueueAtom> getActiveQueue(String queueID) {
		return activeQueues.get(queueID);
	}

	@Override
	public String getQueueRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQueueRoot(String queueRoot) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public URI getURI() {
		return uri;
	}
	
	@Override
	public String getURIString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setURI(URI uri) throws EventException {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void setURI(String uri) throws EventException {
		// TODO Auto-generated method stub
	}
	
	public void addActiveQueue(IQueue<QueueAtom> queue) {
		activeQueues.put(queue.getQueueID(), queue);
	}

	@Override
	public String getHeartbeatTopicName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCommandTopicName() {
		return commandTopicName;
	}

	@Override
	public boolean isActive() {
		return active;
	}
	
	@Override
	public String getCommandSetName() {
		return commandQueueName;
	}
	
	public boolean isForced() {
		return forced;
	}
}
