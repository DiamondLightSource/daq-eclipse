package org.eclipse.scanning.event.queues;

import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.queues.IHeartbeatMonitor;
import org.eclipse.scanning.api.event.queues.IQueueService;

public class HeartbeatMonitor implements IHeartbeatMonitor {
	
	public HeartbeatMonitor(UUID consumerID) {
		
	}
	
	public HeartbeatMonitor(String queueID) {
		
	}
	
	public HeartbeatMonitor (String queueID, IQueueService queueService) {
		
	}

	@Override
	public HeartbeatBean getLastHeartbeat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<HeartbeatBean> getLatestHeartbeats() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHeartbeatTopic() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHeartbeatTopic(String topicName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UUID getConsumerID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setConsumerID(UUID consumerID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getQueueID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setQueueID(String queueID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getRecorderSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setRecorderSize(int beats) {
		// TODO Auto-generated method stub
		
	}
	


}
