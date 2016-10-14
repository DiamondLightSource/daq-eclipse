package org.eclipse.scanning.api.event.queues.beans;

import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;

/**
 * Base class for all atoms/beans which will be handled by the 
 * {@link IQueueService}.
 * 
 * @author Michael Wharmby
 *
 */
public abstract class Queueable extends StatusBean {
	
	private static final long serialVersionUID = 5067469791394115873L;
	
	protected long runTime;
	protected String beamline;

	protected Queueable() {
		super();
		setStatus(Status.NONE);
		setPreviousStatus(Status.NONE);
	}
	
	public String getBeamline() {
		return beamline;
	}
	
	public void setBeamline(String beamline) {
		this.beamline = beamline;
	}

	public long getRunTime() {
		return runTime;
	}

	public void setRunTime(long runTime) {
		this.runTime = runTime;
	}

	public void merge(Queueable with) {
		super.merge(with);
		this.runTime = with.runTime;
		this.beamline = with.beamline;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (runTime ^ (runTime >>> 32));
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
		Queueable other = (Queueable) obj;
		if (runTime != other.runTime)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String clazzName = this.getClass().getSimpleName();
		return clazzName + "[previousStatus=" + previousStatus + ", status="
				+ status + ", name=" + name + ", message=" + message
				+ ", percentComplete=" + percentComplete + ", userName="
				+ userName + ", hostName=" + hostName + ", submissionTime=" 
				+ submissionTime + ", properties=" + getProperties()
		        + ", id=" + getUniqueId() + "]";
	}

}
