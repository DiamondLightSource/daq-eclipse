package org.eclipse.scanning.test.event.queues.mocks;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.queues.IHeartbeatMonitor;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.QueueStatus;
import org.eclipse.scanning.api.event.queues.beans.QueueAtom;
import org.eclipse.scanning.api.event.queues.beans.QueueBean;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public class MockQueueService implements IQueueService {
	
	private IQueue<QueueBean> jobQueue;
	private String jobQueueID;
	
	private Map<String, IQueue<QueueAtom>> activeQueues;
	
	public MockQueueService(IQueue<QueueBean> mockOne) {
		this.jobQueue = mockOne;
		jobQueueID = mockOne.getQueueID();
		
		activeQueues = new HashMap<>();
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
		jobQueue.getConsumer().start();
		for (String queueID : activeQueues.keySet()) {
			activeQueues.get(queueID).getConsumer().start();
		}

	}

	@Override
	public void stop(boolean force) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public String registerNewActiveQueue() throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deRegisterActiveQueue(String queueID, boolean force) throws EventException {
		// TODO Auto-generated method stub

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
	public QueueStatus getQueueStatus(String queueID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disposeQueue(String queueID, boolean nullify) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void killQueue(String queueID, boolean disconnect, boolean exitProcess) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void jobQueueSubmit(QueueBean bean) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void activeQueueSubmit(QueueAtom atom, String queueID) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void jobQueueTerminate(QueueBean bean) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public void activeQueueTerminate(QueueAtom atom, String queueID) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public IProcessCreator<QueueBean> getJobQueueProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setJobQueueProcessor(IProcessCreator<QueueBean> procCreate) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public IProcessCreator<QueueAtom> getActiveQueueProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setActiveQueueProcessor(IProcessCreator<QueueAtom> procCreate) throws EventException {
		// TODO Auto-generated method stub

	}

	@Override
	public List<QueueBean> getJobQueueStatusSet() throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<QueueAtom> getActiveQueueStatusSet(String queueID) throws EventException {
		// TODO Auto-generated method stub
		return null;
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
	public List<String> getAllActiveQueueIDs() {
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
	public Map<String, IQueue<QueueAtom>> getAllActiveQueues() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IEventService getEventService() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
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
	public <T extends Queueable> void submit(T atomBean, String submitQ) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends Queueable> void terminate(T atomBean, String statusT) throws EventException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IHeartbeatMonitor getHeartMonitor(String queueID) throws EventException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeartbeatTopicName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCommandTopicName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

}
