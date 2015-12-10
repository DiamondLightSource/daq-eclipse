package org.eclipse.scanning.api.malcolm;

import org.eclipse.scanning.api.scan.IRunnableDevice;


/**
 * This service talks to the middleware layer state machine for controlling
 * the scan. If working inside Diamond there is a wiki here:
 * http://confluence.diamond.ac.uk/display/MAP/WP4+Middlelayer+Design+Specification
 * 
 * A version of this design is also in Malcolm.pdf in this package.
 * 
 * The name malcolm is chosen for the middleware layer between the Java OSGi server and
 * the hardware - inspired by the sitcom 'Malcolm in the Middle' 
 * https://en.wikipedia.org/wiki/Malcolm_in_the_Middle
 * 
 * This interface attempts to mirror how the hardware in the python looks, hence the
 * fact that it does not look very 'Java'. So instead of setPaused(boolean) we have
 * pause() and resume().
 * 
 * Usage:
 * <code>
 <p>
        IMalcolmService service = ... // OSGi service<br>
        IMalcolmConnection        connection = service.createConnection("tcp://127.0.0.1:7800");<br>
<br>
		IMalcolmDevice zebra =  connection.getDevice("zebra");<br>
	    Map<String, Object> config = new HashMap<String,Object>(2);<br>
		config.put("PC_BIT_CAP", 1);<br>
		config.put("PC_TSPRE", "ms");<br>
		<br>
		zebra.configure(config);<br>
		zebra.run(); // blocks until finished<br>
		<br>
		final State state = zebra.getState();<br>
        // ... We did something!<br>
</p>        
</code>

 * <img src="./doc/device_state.png" />

 * @author Matthew Gerring
 *
 */
public interface IMalcolmDevice<T> extends IRunnableDevice<T>, IMalcolmEventPublisher, ILatchableDevice {
	
	// TODO setAttribute, getAttribute
	
	// TODO Table of 'positions' in configure map. Needed for wrapping with external dimension, sets the position in nexus file to write.
	// Name (String), Type desrc (Map<String, int>), Value (double[] or numeric list), String unit (can be null/unset)
	// List of rows in the object to serialize.
	// Sending this to Malcolm for the info for a write, positions of frames 
	// Examples from Tom's tests 
	
	
	/**
	 * allowed in any state. Can be used to check a param dict for validity. Will always report the same result whatever
	 * the current state of the device. Will raise exception if it is invalid
	 * 
	 * @return true if valid, false if invalid values in legal config, exception if illegal config e.g. not allowed parameters.
	 * 
	 * @throws MalcolmDeviceException if params are not a just not allowed.
	 */
	public T validate(T params) throws MalcolmDeviceException;
	
	/**
	 * Allowed in any state except Fault. Will abort the current operation. Will block until the device is in a reset state.
	 */
	public void abort() throws MalcolmDeviceException;
	
	/**
	 * Allowed from Fault. Will try to reset the device into Idle state. Will block until the device is in a rest state.
	 */
	public void reset() throws MalcolmDeviceException; 
	
	/**
	 * Allowed when the device is in Ready or Paused state. Will block until the device is in a rest state.
	 */
	public void configure(T params) throws MalcolmDeviceException;
	
	/**
	 * Allowed when the device is in Ready or Paused state. Will block until the device is in a rest state.
	 * A device which has a blocking run active cannot call run again or an exception will be thrown.
	 * A paused device must have resume() called on the same thread.
	 */
	public void run() throws MalcolmDeviceException;
		
	/**
	 * Allowed when the device is in Running state. Will block until the device is in a rest state. 
	 * 
	 * When paused the same thread must call resume() or abort() which has paused or an
	 * IllegalMonitorState Exception will be thrown.
	 */
	public void pause() throws MalcolmDeviceException;
	
	/**
	 * Allowed when the device is in Paused state. Will block until the device is unpaused.
	 * 
	 * When paused the same thread must call resume() or abort() which has paused or an
	 * 
	 * IllegalMonitorState Exception will be thrown.
	 */
	public void resume() throws MalcolmDeviceException;

	/**
	 * Attempts to determine if the device is locked doing something like a configure or a run.
	 * 
	 * @return true if not in locked state, otherwise false.
	 */
	public boolean isLocked() throws MalcolmDeviceException ;

}
