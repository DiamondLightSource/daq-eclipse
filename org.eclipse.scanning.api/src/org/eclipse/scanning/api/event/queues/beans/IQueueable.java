package org.eclipse.scanning.api.event.queues.beans;

import org.eclipse.scanning.api.event.queues.IQueueService;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.event.status.StatusBean;

/**
 * Interface to allow implementing classes to be put inside an 
 * {@link IAtomQueue} & an {@link IQueueService} active queue.
 * 
 * This interface describes two fields missing from {@link StatusBean} which 
 * are desirable for the operation of the queue. Classes to be run in the 
 * {@link IQueueService} should be extended from {@link StatusBean} and 
 * implement this interface.
 * 
 * @author Michael Wharmby
 *
 */
public interface IQueueable {
	
	/**
	 * Get the name of the beamline on which this IQueueable was submitted.
	 *
	 * @return String name of the beamline
	 */
	public String getBeamline();
	
	/**
	 * Change the name of the beamline on which this IQueueable was submitted.
	 *
	 * @param String name of the beamline
	 */
	public void setBeamline(String beamline);

	public String getHostName();
	
	public void setHostName(String userName);
	
	public String getMessage();
	
	public void setMessage(String message);
	
	public double getPercentComplete();
	
	public void setPercentComplete(double percentComplete);
	
	public void setPreviousStatus(Status status);
	
	public Status getPreviousStatus();
	
	/**
	 * Get the amount of time (in ms) necessary to complete the processes in 
	 * this {@link IQueueAtom}.
	 * 
	 * @return long number of ms to complete processes
	 */
	public long getRunTime();

	/**
	 * Set the expected time (in ms) to complete the processes in this 
	 * {@link IQueueAtom}.
	 * 
	 * @param runtime long number of ms to complete processes
	 */
	public void setRunTime(long runTime);

	public Status getStatus();
	
	public void setStatus(Status status);
	
	public String getUserName();

	public void setUserName(String userName);
	
}
