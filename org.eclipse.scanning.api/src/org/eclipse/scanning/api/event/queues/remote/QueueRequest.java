package org.eclipse.scanning.api.event.queues.remote;

import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.queues.IQueue;
import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.queues.beans.Queueable;
import org.eclipse.scanning.api.event.status.Status;

/**
 * A QueueRequest contains a request for data about the {@link IQueueService} 
 * from the client. From the {@link QueueRequestType}, the 
 * {@link QueueResponseProcess} decides what data it will populate to fulfil 
 * the request, before returning it to the client.
 * 
 * @author Michael Wharmby
 *
 */
public class QueueRequest extends IdBean {

	/**
	 * Version ID for serialization. Should be updated when class changed. 
	 */
	private static final long serialVersionUID = 20161107L;

	//Values for request construction
	private QueueRequestType requestType;
	private String beanID;//Unique ID of bean to interrogate
	private String queueID;//ID of queue as set in IQueueService where beanID should be found
	private boolean startQueueService = false;
	private boolean stopQueueService = false;
	private boolean forceStop = false;
	
	//Values to be completed by responses
	private String jobQueueID;//jobQueue of IQueueService
	private String commandSetName, commandTopicName, heartbeatTopicName;//QueueService configured destinations
	private Status beanStatus;//State of a the bean in the queue
	private IQueue<? extends Queueable> queue;

	//Request variables
	public QueueRequestType getRequestType() {
		return requestType;
	}
	public void setRequestType(QueueRequestType requestType) {
		this.requestType = requestType;
	}
	public String getBeanID() {
		return beanID;
	}
	public void setBeanID(String beanID) {
		this.beanID = beanID;
	}
	public String getQueueID() {
		return queueID;
	}
	public void setQueueID(String queueID) {
		this.queueID = queueID;
	}
	public boolean isStartQueueService() {
		return startQueueService;
	}
	public void setStartQueueService(boolean startQueueService) {
		this.startQueueService = startQueueService;
	}
	public boolean isStopQueueService() {
		return stopQueueService;
	}
	public void setStopQueueService(boolean stopQueueService) {
		this.stopQueueService = stopQueueService;
	}
	public boolean isForceStop() {
		return forceStop;
	}
	public void setForceStop(boolean forceStop) {
		this.forceStop = forceStop;
	}
	//Response variables
	public String getJobQueueID() {
		return jobQueueID;
	}
	public void setJobQueueID(String jobQueueID) {
		this.jobQueueID = jobQueueID;
	}
	public String getCommandSetName() {
		return commandSetName;
	}
	public void setCommandSetName(String commandSetName) {
		this.commandSetName = commandSetName;
	}
	public String getCommandTopicName() {
		return commandTopicName;
	}
	public void setCommandTopicName(String commandTopicName) {
		this.commandTopicName = commandTopicName;
	}
	public String getHeartbeatTopicName() {
		return heartbeatTopicName;
	}
	public void setHeartbeatTopicName(String heartbeatTopicName) {
		this.heartbeatTopicName = heartbeatTopicName;
	}
	public void setBeanStatus(Status beanStatus) {
		this.beanStatus = beanStatus;
	}
	public Status getBeanStatus() {
		return beanStatus;
	}
	public IQueue<? extends Queueable> getQueue() {
		return queue;
	}
	public void setQueue(IQueue<? extends Queueable> queue) {
		this.queue = queue;
	}
	
	@Override
	public String toString() {
		return "QueueRequest [requestType=" + requestType + ", beanID=" + beanID + ", queueID=" + queueID
				+ ", startQueueService=" + startQueueService + ", stopQueueService=" + stopQueueService + ", forceStop="
				+ forceStop + ", jobQueueID=" + jobQueueID + ", commandSetName=" + commandSetName
				+ ", commandTopicName=" + commandTopicName + ", heartbeatTopicName=" + heartbeatTopicName
				+ ", beanStatus=" + beanStatus + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((beanID == null) ? 0 : beanID.hashCode());
		result = prime * result + ((beanStatus == null) ? 0 : beanStatus.hashCode());
		result = prime * result + ((commandSetName == null) ? 0 : commandSetName.hashCode());
		result = prime * result + ((commandTopicName == null) ? 0 : commandTopicName.hashCode());
		result = prime * result + (forceStop ? 1231 : 1237);
		result = prime * result + ((heartbeatTopicName == null) ? 0 : heartbeatTopicName.hashCode());
		result = prime * result + ((jobQueueID == null) ? 0 : jobQueueID.hashCode());
		result = prime * result + ((queueID == null) ? 0 : queueID.hashCode());
		result = prime * result + ((requestType == null) ? 0 : requestType.hashCode());
		result = prime * result + (startQueueService ? 1231 : 1237);
		result = prime * result + (stopQueueService ? 1231 : 1237);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueueRequest other = (QueueRequest) obj;
		if (beanID == null) {
			if (other.beanID != null)
				return false;
		} else if (!beanID.equals(other.beanID))
			return false;
		if (beanStatus != other.beanStatus)
			return false;
		if (commandSetName == null) {
			if (other.commandSetName != null)
				return false;
		} else if (!commandSetName.equals(other.commandSetName))
			return false;
		if (commandTopicName == null) {
			if (other.commandTopicName != null)
				return false;
		} else if (!commandTopicName.equals(other.commandTopicName))
			return false;
		if (forceStop != other.forceStop)
			return false;
		if (heartbeatTopicName == null) {
			if (other.heartbeatTopicName != null)
				return false;
		} else if (!heartbeatTopicName.equals(other.heartbeatTopicName))
			return false;
		if (jobQueueID == null) {
			if (other.jobQueueID != null)
				return false;
		} else if (!jobQueueID.equals(other.jobQueueID))
			return false;
		if (queueID == null) {
			if (other.queueID != null)
				return false;
		} else if (!queueID.equals(other.queueID))
			return false;
		if (requestType != other.requestType)
			return false;
		if (startQueueService != other.startQueueService)
			return false;
		if (stopQueueService != other.stopQueueService)
			return false;
		return true;
	}

}
