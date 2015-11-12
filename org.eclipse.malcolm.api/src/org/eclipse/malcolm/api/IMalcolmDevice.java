package org.eclipse.malcolm.api;

import java.util.Map;

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
        IMalcolmService service = ... // OSGi service
        IMalcolmConnection        connection = service.createConnection("tcp://127.0.0.1:7800");

		IMalcolmDevice zebra =  connection.getDevice("zebra");
	    Map<String, Object> config = new HashMap<String,Object>(2);
		config.put("PC_BIT_CAP", 1);
		config.put("PC_TSPRE", "ms");
		
		zebra.configure(config);
		zebra.run(); // blocks until finished
		
		final State state = zebra.getState();
        // ... We did something!
</p>        
</code>

 * @author Matthew Gerring
 *
 */
public interface IMalcolmDevice extends IMalcolmEventPublisher, IMalcolmStateManager {
	
	/**
	 * allowed in any state. Can be used to check a param dict for validity. Will always report the same result whatever
	 * the current state of the device. Will raise exception if it is invalid
	 * 
	 * @return true if valid, false if invalid values in legal config, exception if illegal config e.g. not allowed parameters.
	 * 
	 * @throws MalcolmDeviceException if params are not a just not allowed.
	 */
	public Map<String, Object> validate(Map<String, Object> params) throws MalcolmDeviceException;
	
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
	public void configure(Map<String, Object> params) throws MalcolmDeviceException;
	
	/**
	 * Allowed when the device is in Ready or Paused state. Will block until the device is in a rest state.
	 * A device which has a blocking run active cannot call run again or an exception will be thrown.
	 * A paused device must have resume() called on the same thread.
	 */
	public void run() throws MalcolmDeviceException;
	
	/**
	 * Convenience function that does an abort() or reset() if needed, then a configure(params) and a run()
	 */
	public void configureRun(Map<String, Object> params) throws MalcolmDeviceException;
	
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
