package org.eclipse.scanning.api.sequence;

import org.eclipse.scanning.api.event.status.Status;

public interface ISequencer {
	
		
	/**
	 * Return the current status of the sequencer.
	 * @return
	 */
	public Status getStatus();

	/**
	 * Blocking call to execute the scan
	 * 
	 * @throws SequenceException
	 */
	public void execute() throws SequenceException;
	
	/**
	 * Call to terminate the scan before it has finished.
	 * 
	 * @throws SequenceException
	 */
	public void terminate() throws SequenceException;


	/**
	 * Name of the sequencer
	 * @return
	 */
	public String getName();
	public void setName(String name);

}
