package org.eclipse.scanning.api.scan;

import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;

/**
 * 
 * A scanner behaves a lot like a malcolm device - should it just be one?
 * 
 * Anatomy of a CPU scan (non-malcolm)
 * 
 *  
 *  ___________
 *  |         |
 * _|         |___________  collectData() Tell detector to collect
 *            ___________
 *            |         |
 * ___________|         |_  readout() Tell detector to readout
 * 
 *            ________
 *            |      |
 * ___________|      |____  moveTo()  Scannables move motors to new position
 * 
 * 

 * @author Matthew Gerring
 *
 */
public interface IScanner {
		
	/**
	 * Return the current status of the sequencer.
	 * @return
	 */
	public Status getState();
	
	/**
	 * Scan the points in this iterator, moving each position to its required
	 * scannable using moveTo(...) and using the level value to order the moves.
	 * 
	 * TODO FIXME, not sure if this will be the final configure(...) erasure.
	 * 
	 * @param list
	 * @param parser
	 */
	public void configure(Iterable<IPosition> list, IParser<?> parser) throws ScanningException ;

	/**
	 * Blocking call to execute the scan
	 * 
	 * @throws ScanningException
	 */
	public void run() throws ScanningException;
	
	/**
	 * Call to terminate the scan before it has finished.
	 * 
	 * @throws ScanningException
	 */
	public void abort() throws ScanningException;

	/**
	 * Allowed when the device is in Running state. Will block until the device is in a rest state. 
	 * 
	 * When paused the same thread must call resume() or abort() which has paused or an
	 * IllegalMonitorState Exception will be thrown.
	 */
	public void pause() throws ScanningException;
	
	/**
	 * Allowed when the device is in Paused state. Will block until the device is unpaused.
	 * 
	 * When paused the same thread must call resume() or abort() which has paused or an
	 * 
	 * IllegalMonitorState Exception will be thrown.
	 */
	public void resume() throws ScanningException;
}
