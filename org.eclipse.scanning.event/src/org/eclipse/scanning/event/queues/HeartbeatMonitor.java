package org.eclipse.scanning.event.queues;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.HeartbeatEvent;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.IHeartbeatMonitor;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.SizeLimitedRecorder;
import org.eclipse.scanning.api.event.queues.beans.Queueable;

public class HeartbeatMonitor implements IHeartbeatMonitor {

	private URI uri;
	private String heartbeatTopic;

	private ISubscriber<IHeartbeatListener> monitor;
	private SizeLimitedRecorder<HeartbeatBean> heartbeatRecord;

	private UUID monitoredConsumerID;
	private String queueID = null;

	/**
	 * Set up directly from a given consumer ID.
	 * 
	 * @param uri Location of the JMS broker.
	 * @param heartbeatTopic on which {@link HeartbeatBeans} are published.
	 * @param consumerID UUID to listen for.
	 * @throws EventException In case listener cannot be added correctly.
	 */
	public HeartbeatMonitor(URI uri, String heartbeatTopic, UUID consumerID) throws EventException {
		this.uri = uri;
		this.heartbeatTopic = heartbeatTopic;
		monitoredConsumerID = consumerID;
		setUpMonitor();
	}

	/**
	 * Set up using an {@link IQueue} instance.
	 * 
	 * @param uri Location of the JMS broker.
	 * @param heartbeatTopic on which {@link HeartbeatBeans} are published.
	 * @param queue {@link IQueue} object to monitor
	 * @throws EventException In case listener cannot be added correctly.
	 */
	public HeartbeatMonitor(URI uri, String heartbeatTopic, 
			IQueue<? extends Queueable> queue) throws EventException {
		this.uri = uri;
		this.heartbeatTopic = heartbeatTopic;
		monitoredConsumerID = queue.getConsumerID();
		setUpMonitor();
		queueID = queue.getQueueID();
	}

	/**
	 * Set up using the queueID string name and the {@link IQueueService} which
	 *  maintains this queue.
	 * 
	 * @param  uri Location of the JMS broker.
	 * @param heartbeatTopic on which {@link HeartbeatBeans} are published.
	 * @param queueID String name of queue in {@link IQueueService}
	 * @param queueService {@link IQueueService} with reference to the 
	 *                     {@link IQueue} to monitor.
	 * @throws EventException In case listener cannot be added correctly.
	 */
	public HeartbeatMonitor(URI uri, String heartbeatTopic, String queueID, 
			IQueueService queueService) throws EventException {
		this.uri = uri;
		this.heartbeatTopic = heartbeatTopic;

		//Get consumerID through the IQueueService
		if (queueID.equals(queueService.getJobQueueID())) {
			monitoredConsumerID = queueService.getJobQueue().getConsumerID();
		} else {
			monitoredConsumerID = queueService.getActiveQueue(queueID).getConsumerID();
		}
		setUpMonitor();
		setQueueID(queueID);
	}

	private void setUpMonitor() throws EventException {
		heartbeatRecord = new SizeLimitedRecorder<>(100);
		
		IEventService evServ = QueueServicesHolder.getEventService();
		monitor = evServ.createSubscriber(uri, heartbeatTopic);
		monitor.addListener(new IHeartbeatListener() {
			@Override
			public void heartbeatPerformed(HeartbeatEvent evt) {
				HeartbeatBean beat = evt.getBean();
				if (beat.getConsumerId().equals(monitoredConsumerID)) {
					//Only add beans if they are from this consumer!!
					heartbeatRecord.add(beat);
				}
			}
		});
	}

	@Override
	public HeartbeatBean getLastHeartbeat() {
		return heartbeatRecord.latest();
	}

	@Override
	public List<HeartbeatBean> getLatestHeartbeats() {
		return heartbeatRecord.getRecording();
	}

	@Override
	public String getHeartbeatTopic() {
		return heartbeatTopic;
	}

	@Override
	public void setHeartbeatTopic(String topicName) {
		heartbeatTopic = topicName;
	}

	@Override
	public UUID getConsumerID() {
		return monitoredConsumerID;
	}

	@Override
	public void setConsumerID(UUID consumerID) {
		monitoredConsumerID = consumerID;
	}

	@Override
	public String getQueueID() {
		return queueID;
	}

	@Override
	public void setQueueID(String queueID) {
		this.queueID = queueID;
	}

	@Override
	public int getRecorderCapacity() {
		return heartbeatRecord.getCapacity();
	}

	@Override
	public void setRecorderCapacity(int beats) {
		heartbeatRecord.setCapacity(beats);
	}

}
