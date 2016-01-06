package org.eclipse.scanning.api.event.core;

import java.util.UUID;

import org.eclipse.scanning.api.event.EventException;

/**
 * This interface is designed to make it easy to listen to all events and
 * easy to listen to a specific scan.
 * 
 <code>
 
    // Listen to all events
    IEventService service = ...
    IScanSubscriber sub = service.createSubscriber(...)
    IScanListener listener = new IScanListener.Stub() {
		@Override
		public void scanEventPerformed(ScanEvent evt) {
			// ... Everything that happens
		}
    };
    sub.addScanListener(listener);
    
    // Listen to specific events for a given scan
     IScanListener listener2 = new IScanListener.Stub() {
		@Override
		public void scanEventPerformed(ScanEvent evt) {
			// ... Everything that happens
		}
    };
    sub.addScanListener(scanId, listener2);
    
//    NOTE If a listener is registered it will then be associated with
//    the scan it is registered with only. It should be unregistered and
//    readded. So for instance:
    sub.addScanListener(scanId, listener); // registers for scanId but removes it as general listener.
    
    // Removes the Object listener from the general listeners and defines it 
    // as a listener of the scanId. This listener would have to be readded 
    // using addScanListener(listener) to use it again as a general listener.
 
</code>
   
 * 
 * @author Matthew Gerring
 *
 */
public interface ISubscriber<T> extends ITopicConnection {

	/**
	 * Adds a listener which is notified when events are broadcast.
	 * The listener works event if the manager is running on a client
	 * and the broadcast is happening from a server because through JMS or
	 * similar messaging system which this service and manager are hiding.
	 * 
	 * @param listener
	 * @throws event exception if the remote event cannot be connected to.
	 */
	public void addListener(T listener) throws EventException;
	
	/**
	 * Removes a listener such that events are no longer sent to it.
	 * 
	 * @param listener
	 */
	public void removeListener(T listener);

	/**
	 * Register events for a given scanId to be reported.
	 * @param id
	 * @param listener
	 * @throws event exception if the remote event cannot be connected to.
	 */
	public void addListener(String id, T listener) throws EventException;
	
	/**
	 * Unregister events for a given scanId to be reported.
	 * @param id
	 * @param listener
	 */
	public void removeListener(String id, T listener);
}
