package org.eclipse.scanning.api.event.queues.remote;

import org.eclipse.scanning.api.event.IdBean;
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
	private static final long serialVersionUID = 20161027L;

	private QueueRequestType requestType;

	private String beanID;
	private String queueID;
	private String jobQueueID;
	private Status beanStatus;

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
	public String getJobQueueID() {
		return jobQueueID;
	}
	public void setJobQueueID(String jobQueueID) {
		this.jobQueueID = jobQueueID;
	}
	public Status getBeanStatus() {
		return beanStatus;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((beanID == null) ? 0 : beanID.hashCode());
		result = prime * result + ((beanStatus == null) ? 0 : beanStatus.hashCode());
		result = prime * result + ((jobQueueID == null) ? 0 : jobQueueID.hashCode());
		result = prime * result + ((queueID == null) ? 0 : queueID.hashCode());
		result = prime * result + ((requestType == null) ? 0 : requestType.hashCode());
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
		return true;
	}

}
