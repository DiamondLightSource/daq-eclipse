package org.eclipse.scanning.event.queues;

import java.util.List;
import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.alive.HeartbeatBean;
import org.eclipse.scanning.api.event.alive.HeartbeatEvent;
import org.eclipse.scanning.api.event.alive.IHeartbeatListener;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.QueueNameMap;
import org.eclipse.scanning.api.event.queues.QueueStatus;
import org.eclipse.scanning.api.event.queues.SizeLimitedRecorder;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queue is a concrete implementation of {@link IQueue}. It holds details of 
 * the consumer and of the consumer/queue configuration needed for the 
 * {@link IQueueService} to control the queue.
 * 
 * @author Michael Wharmby
 *
 * @param <T> Base type of atom/bean operated on by the queue, e.g. 
 *            {@link QueueAtom} or {@QueueBean}.
 */
public class Queue<T extends Queueable> implements IQueue<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(Queue.class);
	
	private final IConsumer<T> consumer;
	private ISubscriber<IHeartbeatListener> heartMonitor;
	private final QueueNameMap queueNames;
	private final String queueID;
	private QueueStatus queueStatus;
	
	private final SizeLimitedRecorder<HeartbeatBean> heartbeatRecord;
	
	public Queue(String qID, QueueNameMap qNames, IConsumer<T> cons, ISubscriber<IHeartbeatListener> mon) {
		this.consumer = (IConsumer<T>) cons;
		this.heartMonitor = mon;
		this.queueID = qID;
		this.queueNames = qNames;
		
		heartbeatRecord = new SizeLimitedRecorder<HeartbeatBean>(100);
		try {
			//Setup the heartbeat monitor
			heartMonitor.addListener(new IHeartbeatListener() {
				@Override
				public void heartbeatPerformed(HeartbeatEvent evt) {
					HeartbeatBean beat = evt.getBean();
					if (beat.getConsumerId().equals(Queue.this.getConsumerID())) {
						//Only add beans if they are from this consumer!!
						heartbeatRecord.add(beat);
					}
				}
			});
		} catch (EventException e) {
			logger.error("Failed to set heart beat listener on "+qID+" heartbeat monitor.");
		}
		
		queueStatus = QueueStatus.INITIALISED;
	}

	@Override
	public String getQueueID() {
		return queueID;
	}

	@Override
	public QueueStatus getQueueStatus() {
		return queueStatus;
	}

	@Override
	public void setQueueStatus(QueueStatus status) {
		this.queueStatus = status;
	}

	@Override
	public IConsumer<T> getConsumer() {
		return consumer;
	}

	@Override
	public UUID getConsumerID() {
		return consumer.getConsumerId();
	}

	@Override
	public QueueNameMap getQueueNames() {
		return queueNames;
	}

	@Override
	public IProcessCreator<T> getProcessor() {
		return consumer.getRunner();
	}

	@Override
	public void setProcessor(IProcessCreator<T> processor)
			throws EventException {
		consumer.setRunner(processor);
	}

	@Override
	public List<HeartbeatBean> getLatestHeartbeats() {
		return heartbeatRecord.getRecording();
	}

	@Override
	public HeartbeatBean getLastHeartbeat() {
		return heartbeatRecord.latest();
	}
	
	@Override
	public boolean clearQueues() throws EventException {
		consumer.clearQueue(queueNames.getSubmissionQueueName());
		consumer.clearQueue(queueNames.getStatusQueueName());
		
		if (consumer.getStatusSet().size() == 0 && consumer.getSubmissionQueue().size() == 0) return true;
		else return false;
	}
	
	@Override
	public void disconnect() throws EventException {
		heartMonitor.disconnect();
		consumer.disconnect();
	}
	
	@Override
	public boolean hasSubmittedJobsPending() throws EventException {
		return !consumer.getSubmissionQueue().isEmpty();
	}

}
